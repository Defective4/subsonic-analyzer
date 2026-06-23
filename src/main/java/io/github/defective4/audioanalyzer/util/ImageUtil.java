package io.github.defective4.audioanalyzer.util;

import java.awt.image.BufferedImage;

public class ImageUtil {
    private ImageUtil() {}

    public static BufferedImage crop(BufferedImage img) {
        int w = img.getWidth();
        int h = img.getHeight();

        BufferedImage cropped = null;
        if (w > h) {
            cropped = new BufferedImage(h, h, img.getType());
            cropped.createGraphics().drawImage(img, (-w + h) / 2, 0, null);
        } else if (h > w) {
            cropped = new BufferedImage(w, w, img.getType());
            cropped.createGraphics().drawImage(img, 0, (-h + w) / 2, null);
        }

        return cropped == null ? img : cropped;
    }
}
