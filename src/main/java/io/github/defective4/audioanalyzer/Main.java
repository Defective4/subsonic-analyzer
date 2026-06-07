package io.github.defective4.audioanalyzer;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import io.github.defective4.audioanalyzer.ml.Database;
import io.github.defective4.audioanalyzer.ml.TensorflowAnalyzer;
import io.github.defective4.audioanalyzer.subsonic.SubsonicAPI;
import io.github.defective4.audioanalyzer.subsonic.model.Entity;

public class Main {
    public static void main(String[] args) {
        try {
            Database db = new Database("jdbc:sqlite:./moods.sqlite");
            TensorflowAnalyzer analyzer = new TensorflowAnalyzer("http://127.0.0.1:8000/analyze");
            SubsonicAPI api = new SubsonicAPI("navidrome", "navidrome".toCharArray(), "https://music.raspberry.local");
            Path tmpDir = Files.createTempDirectory("ad");

            List<Entity> songs = List
                    .of(api.getMusicDirectory(api.getAlbumList(1, 0).albumList().album()[0].id()).directory().child());
            for (Entity song : songs) {
                Path file = Path.of(tmpDir.toString(), Path.of(song.path()).getFileName().toString());
                try (InputStream in = api.download(song)) {
                    Files.copy(in, file);
                }
                Map<String, Float> result = analyzer.requestAnalysis(file.toString());
                db.insertData(song.id(), result);
                System.out.println("Processed " + song.title());
                file.toFile().delete();
                db.commit();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
