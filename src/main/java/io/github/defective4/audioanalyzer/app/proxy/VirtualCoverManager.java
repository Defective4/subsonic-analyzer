package io.github.defective4.audioanalyzer.app.proxy;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import com.google.gson.JsonObject;

import io.github.defective4.audioanalyzer.subsonic.SubsonicAPI;
import io.github.defective4.audioanalyzer.util.ImageUtil;

public class VirtualCoverManager {

    private final File cacheDir;

    public VirtualCoverManager() {
        try {
            cacheDir = Files.createTempDirectory("an").toFile();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public void generateAndSaveCover(SubsonicAPI api, List<JsonObject> songs, String id) throws IOException {
        List<BufferedImage> imgs = new ArrayList<>(4);
        int j = 0;
        for (JsonObject obj : songs) {
            if (obj.has("coverArt")) {
                String artId = obj.get("coverArt").getAsString();
                imgs.add(ImageUtil.crop(api.getCoverArt(artId)));
                if (++j > 3) break;
            }
        }

        BufferedImage merged = new BufferedImage(512, 512, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = merged.createGraphics();
        g2.addRenderingHints(Map.of(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY));
        for (int i = 0; i < imgs.size(); i++) {
            int x = i % 2 * 256;
            int y = i / 2 * 256;

            g2.drawImage(imgs.get(i), x, y, 256, 256, null);
        }

        File target = new File(cacheDir, id + ".png");
        target.deleteOnExit();
        ImageIO.write(merged, "png", target);
    }
}
