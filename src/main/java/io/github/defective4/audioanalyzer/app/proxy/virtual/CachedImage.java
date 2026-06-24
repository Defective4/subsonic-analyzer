package io.github.defective4.audioanalyzer.app.proxy.virtual;

import java.awt.image.BufferedImage;

public record CachedImage(BufferedImage image, long ttl) {
}
