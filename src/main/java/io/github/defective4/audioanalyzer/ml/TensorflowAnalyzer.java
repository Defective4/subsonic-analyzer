package io.github.defective4.audioanalyzer.ml;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import com.google.gson.Gson;

import io.github.defective4.audioanalyzer.ml.model.AnalysisResponse;

public class TensorflowAnalyzer {
    private final String analyzerEndpoint;
    private final Gson gson = new Gson();

    public TensorflowAnalyzer(String analyzerEndpoint) throws MalformedURLException {
        URI.create(analyzerEndpoint).toURL();
        this.analyzerEndpoint = analyzerEndpoint;
    }

    public AnalysisResponse requestAnalysis(String filePath) throws IOException {
        HttpURLConnection con = null;
        try {
            con = (HttpURLConnection) URI
                    .create(analyzerEndpoint + "?audioPath=" + URLEncoder.encode(filePath, StandardCharsets.UTF_8))
                    .toURL().openConnection();
//            try (Reader reader = new InputStreamReader(con.getInputStream())) {
//                JsonObject obj = JsonParser.parseReader(reader).getAsJsonObject();
//                obj.asMap().forEach((k, v) -> map.put(k.replace("-", "_").replace(".pb", ""), v.getAsFloat()));
//            }
            try (Reader reader = new InputStreamReader(con.getInputStream())) {
                return gson.fromJson(reader, AnalysisResponse.class);
            }
        } finally {
            if (con != null) con.disconnect();
        }
    }
}
