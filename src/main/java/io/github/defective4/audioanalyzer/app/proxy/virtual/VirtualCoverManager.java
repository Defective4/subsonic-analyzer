package io.github.defective4.audioanalyzer.app.proxy.virtual;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.imageio.ImageIO;

import com.google.gson.JsonObject;

import io.github.defective4.audioanalyzer.subsonic.SubsonicAPI;
import io.github.defective4.audioanalyzer.util.ImageUtil;

public class VirtualCoverManager {

    private final File cacheDir;
    private final Font font;

    public VirtualCoverManager() {
        cacheDir = new File("cache");
        cacheDir.mkdirs();

        try (InputStream in = VirtualCoverManager.class
                .getResourceAsStream("/graphics/Font Awesome 7 Free-Solid-900.otf")) {
            font = Font.createFont(Font.TRUETYPE_FONT, in).deriveFont(52f);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void generateAndSaveCover(SubsonicAPI api, List<JsonObject> songs, String id, String icon)
            throws IOException {
        List<BufferedImage> imgs = new ArrayList<>(4);
        int j = 0;
        for (JsonObject obj : songs) {
            if (obj.has("coverArt")) {
                String artId = obj.get("coverArt").getAsString();
                BufferedImage img = api.getCoverArt(artId);
                if (img == null) continue;
                imgs.add(ImageUtil.crop(img));
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

        g2.drawImage(getCoverOverlay(Color.cyan, icon), 0, 0, 512, 512, null);

        File target = new File(cacheDir, id + ".png");
        target.deleteOnExit();
        ImageIO.write(merged, "png", target);
    }

    public Optional<BufferedImage> getCachedImage(String id) throws IOException {
        File target = new File(cacheDir, id + ".png");
        if (target.isFile()) return Optional.ofNullable(ImageIO.read(target));
        return Optional.empty();
    }

    private BufferedImage getCoverOverlay(Color color, String text) throws IOException {
        try (InputStream in = VirtualCoverManager.class.getResourceAsStream("/graphics/overlay.png")) {
            BufferedImage img = ImageIO.read(in);
            for (int x = 0; x < img.getWidth(); x++) {
                for (int y = 0; y < img.getHeight(); y++) {
                    Color c = new Color(img.getRGB(x, y));
                    if (c.getAlpha() == 255 && c.getRed() == 255 && c.getGreen() == 255 && c.getBlue() == 255) {
                        img.setRGB(x, y, color.getRGB());
                    }
                }
            }
            Graphics2D g2 = img.createGraphics();
            g2.setRenderingHints(Map.of(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY,
                    RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON));
            g2.setFont(font);
            g2.setColor(Color.black);

            int w = g2.getFontMetrics().stringWidth(text);

            g2.drawString(text, 512 - w - 16, 56);
            return img;
        }
    }
}
