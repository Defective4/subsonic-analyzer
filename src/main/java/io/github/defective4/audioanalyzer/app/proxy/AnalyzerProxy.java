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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import io.github.defective4.audioanalyzer.ml.Repository;
import io.javalin.Javalin;
import io.javalin.http.Context;

public class AnalyzerProxy {
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final Javalin javalin;
    private final String localHost;
    private final int localPort;
    private final Map<String, ResponseModifier> replacers;
    private final Repository repo;

    private final String targetBaseURL;

    public AnalyzerProxy(String targetBaseURL, int localPort, String localHost, Repository repo)
            throws MalformedURLException {
        this.targetBaseURL = URI.create(targetBaseURL).toURL().toString();
        this.localPort = localPort;
        this.localHost = localHost;
        javalin = Javalin.create(cfg -> { cfg.routes.apiBuilder(() -> { after(ctx -> { relayRequest(ctx); }); }); });
        this.repo = repo;
        replacers = Map.of("/rest/getSimilarSongs", (props, obj) -> {

        });
    }

    public void start() {
        javalin.start(localHost, localPort);
    }

    private void relayRequest(Context ctx) throws IOException {
        HttpURLConnection con = null;
        try {
            ResponseModifier replacer = replacers.get(ctx.path());
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
                    Map<String, List<String>> unresolvedParameters = new HashMap<>();
                    unresolvedParameters.putAll(ctx.queryParamMap());
                    unresolvedParameters.putAll(ctx.formParamMap());
                    Map<String, String> params = new HashMap<>();

                    for (Entry<String, List<String>> entry : unresolvedParameters.entrySet()) {
                        if (!entry.getValue().isEmpty()) params.put(entry.getKey(), entry.getValue().get(0));
                    }

                    boolean gzip = con.getHeaderFields().getOrDefault("Content-Encoding", List.of()).stream()
                            .anyMatch(v -> v.contains("gzip"));
                    try (Reader reader = new InputStreamReader(gzip ? new GZIPInputStream(in) : in);
                            Writer writer = new OutputStreamWriter(
                                    gzip ? new GZIPOutputStream(ctx.outputStream()) : ctx.outputStream())) {
                        JsonObject obj = JsonParser.parseReader(reader).getAsJsonObject();
                        replacer.modify(params, obj.getAsJsonObject("subsonic-response"));
                        writer.write(gson.toJson(obj));
//                        System.out.println(gson.toJson(obj));
                    }
                } else {
                    copyStream(ctx, in);
                }
            }

        } finally {
            if (con != null) con.disconnect();
        }
    }

    public static void main(String[] args) throws Exception {
        AnalyzerProxy proxy = new AnalyzerProxy("https://music.raspberry.local", 8080, "127.0.0.1",
                new Repository("jdbc:sqlite:mood.sqlite"));
        proxy.start();
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
}
