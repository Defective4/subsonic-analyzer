package io.github.defective4.audioanalyzer.app;

import static io.javalin.apibuilder.ApiBuilder.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import io.javalin.Javalin;
import io.javalin.http.Context;

public class AnalyzerProxy {
    private final Javalin javalin;
    private final String localHost;
    private final int localPort;
    private final String targetBaseURL;

    public AnalyzerProxy(String targetBaseURL, int localPort, String localHost) throws MalformedURLException {
        this.targetBaseURL = URI.create(targetBaseURL).toURL().toString();
        this.localPort = localPort;
        this.localHost = localHost;
        javalin = Javalin.create(cfg -> { cfg.routes.apiBuilder(() -> { after(ctx -> { relayRequest(ctx); }); }); });
    }

    public void start() {
        javalin.start(localHost, localPort);
    }

    private void relayRequest(Context ctx) throws IOException {
        HttpURLConnection con = null;
        try {
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
                copyStream(ctx, in);
            }

        } finally {
            if (con != null) con.disconnect();
        }
    }

    public static void main(String[] args) throws Exception {
        AnalyzerProxy proxy = new AnalyzerProxy("https://music.raspberry.local", 8080, "127.0.0.1");
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
