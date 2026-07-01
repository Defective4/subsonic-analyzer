package io.github.defective4.audioanalyzer.app.proxy;

import static io.javalin.apibuilder.ApiBuilder.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import io.github.defective4.audioanalyzer.app.proxy.virtual.VirtualLibraryManager;
import io.github.defective4.audioanalyzer.config.proxy.CronTasksConfig;
import io.github.defective4.audioanalyzer.config.proxy.ProxyConfiguration;
import io.github.defective4.audioanalyzer.cron.CronExpression;
import io.github.defective4.audioanalyzer.cron.CronTask;
import io.github.defective4.audioanalyzer.cron.Crontab;
import io.github.defective4.audioanalyzer.ml.Repository;
import io.github.defective4.audioanalyzer.subsonic.model.SubsonicResponse;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.json.JavalinGson;

public class AnalyzerProxy {
    private final ProxyConfiguration config;
    private final Crontab crontab;
    private final Gson gson = new Gson();
    private final Map<String, Function<Context, Boolean>> interceptors = new HashMap<>();
    private final Javalin javalin;
    private final VirtualLibraryManager libraryManager;

    private final String localHost;
    private final int localPort;
    private final ProxyHandler proxyHandler;
    private final Map<String, ResponseModifier> replacers;
    private final Repository repo;
    private final String targetBaseURL;

    public AnalyzerProxy(String targetBaseURL, int localPort, String localHost, Repository repo,
            ProxyConfiguration config) throws MalformedURLException {
        this.config = config;
        this.targetBaseURL = URI.create(targetBaseURL).toURL().toString();
        this.localPort = localPort;
        this.localHost = localHost;
        javalin = Javalin.create(cfg -> {
            cfg.jsonMapper(new JavalinGson());
            cfg.routes.apiBuilder(() -> {
                get("*", ctx -> relayRequest(ctx));
                post("*", ctx -> relayRequest(ctx));
            });
        });
        this.repo = repo;
        libraryManager = new VirtualLibraryManager(repo, targetBaseURL, config.virtLibrary());

        proxyHandler = new ProxyHandler(libraryManager, targetBaseURL);
        replacers = new HashMap<>();
        if (config.enableManualDynamicPlaylists()) {
            interceptors.put("/rest/createPlaylist", proxyHandler::createPlaylist);
        }

        if (config.virtLibrary().enableVirtualLibrary()) {
            interceptors.putAll(Map.of("/rest/getCoverArt", proxyHandler::getCoverArt, "/rest/deletePlaylist",
                    proxyHandler::deletePlaylist, "/rest/updatePlaylist", proxyHandler::updatePlaylist));
            replacers.putAll(Map.of("/rest/getPlaylist", proxyHandler::getPlaylist, "/rest/getPlaylists",
                    proxyHandler::getPlaylists));
        }

        if (config.enableAutoDJ())
            replacers.put("/rest/getSimilarSongs", (props, obj) -> proxyHandler.getSimilarSongs(repo, props, obj));

        if (config.cron().enabled()) {
            crontab = new Crontab();
            CronTasksConfig tasks = config.cron().tasks();
            if (tasks.regenerateVirtualLibrary() != null) crontab.addTask(
                    new CronTask(new CronExpression(tasks.regenerateVirtualLibrary()), () -> libraryManager.clear()));
        } else {
            crontab = null;
        }
    }

    public void start() {
        javalin.start(localHost, localPort);
    }

    private void relayRequest(Context ctx) throws IOException {
        HttpURLConnection con = null;
        try {
            String ctxPath = ctx.path();
            if (ctxPath.endsWith(".view")) ctxPath = ctxPath.substring(0, ctxPath.length() - ".view".length());
            boolean cont = true;
            if (interceptors.containsKey(ctxPath)) {
                cont = !interceptors.get(ctxPath).apply(ctx);
            }
            if (cont) {
                ResponseModifier replacer = replacers.get(ctxPath);
                con = (HttpURLConnection) URI
                        .create(targetBaseURL + ctx.path() + (ctx.queryString() == null ? "" : "?" + ctx.queryString()))
                        .toURL().openConnection();
                con.setRequestMethod(ctx.method().name());
                copyHeaders(ctx.headerMap(), con);
                String body = ctx.body();
                if (body != null && !body.isEmpty()) {
                    con.setDoOutput(true);
                    try (Writer wr = new OutputStreamWriter(con.getOutputStream())) {
                        wr.write(body);
                    }
                }
                ctx.status(con.getResponseCode());
                copyHeaders(con.getHeaderFields(), ctx);
                try (InputStream in = con.getResponseCode() >= 400 ? con.getErrorStream() : con.getInputStream()) {
                    if (replacer != null && con.getResponseCode() < 300) {
                        Map<String, String> params = getParams(ctx);

                        boolean gzip = isGzip(con);
                        try (Reader reader = new InputStreamReader(gzip ? new GZIPInputStream(in) : in);
                                Writer writer = new OutputStreamWriter(
                                        gzip ? new GZIPOutputStream(ctx.outputStream()) : ctx.outputStream())) {
                            JsonObject obj = JsonParser.parseReader(reader).getAsJsonObject();
                            try {
                                JsonObject resp = obj.getAsJsonObject("subsonic-response");
                                proxyHandler.setLastResponse(gson.fromJson(resp, SubsonicResponse.class));
                                replacer.modify(params, resp);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            writer.write(gson.toJson(obj));
                        }
                    } else {
                        copyStream(ctx, in);
                    }
                }
            }
        } finally {
            if (con != null) con.disconnect();
        }
    }

    public static Map<String, String> getParams(Context ctx) {
        Map<String, List<String>> unresolvedParameters = new HashMap<>();
        unresolvedParameters.putAll(ctx.queryParamMap());
        unresolvedParameters.putAll(ctx.formParamMap());
        Map<String, String> params = new HashMap<>();

        for (Entry<String, List<String>> entry : unresolvedParameters.entrySet()) {
            if (!entry.getValue().isEmpty()) params.put(entry.getKey(), entry.getValue().get(0));
        }

        return Collections.unmodifiableMap(params);
    }

    public static boolean isGzip(Context ctx) {
        String header = ctx.header("Accept-Encoding");
        return header != null && header.contains("gzip");
    }

    private static void copyHeaders(Map<String, List<String>> headerFields, Context ctx) {
        for (Entry<String, List<String>> entry : headerFields.entrySet()) {
            if (entry.getKey() != null && !"Content-Length".equalsIgnoreCase(entry.getKey())) {
                ctx.header(entry.getKey(), String.join("; ", entry.getValue().toArray(String[]::new)));
            }
        }
    }

    private static void copyHeaders(Map<String, String> headerMap, HttpURLConnection con) {
        for (Entry<String, String> entry : headerMap.entrySet()) {
            if (!entry.getKey().contentEquals("Content-Length"))
                con.setRequestProperty(entry.getKey(), entry.getValue());
        }
    }

    private static void copyStream(Context ctx, InputStream in) throws IOException {
        try (OutputStream out = ctx.outputStream()) {
            byte[] buffer = new byte[4096];
            while (true) {
                int read = in.read(buffer);
                if (read <= 0) break;
                out.write(buffer, 0, read);
            }
        }
    }

    private static boolean isGzip(HttpURLConnection con) {
        return con.getHeaderFields().getOrDefault("Content-Encoding", List.of()).stream()
                .anyMatch(v -> v.contains("gzip"));
    }
}
