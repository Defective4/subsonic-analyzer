package io.github.defective4.audioanalyzer.config;

import java.awt.Color;

import io.github.defective4.audioanalyzer.util.FontAwesomeIcons;

public record ProxyVLibConfig(boolean enablePlaylistEngine, boolean generateFromRecents, int fromRecentsLimit,
        String fromRecentsIcon, String fromRecentsColor) {
    public ProxyVLibConfig() {
        this(true, true, 30, "history", "#00ffff");
        getFromRecentsIcon();
        getFromRecentsColor();
    }

    public Color getFromRecentsColor() {
        return Color.decode(fromRecentsColor);
    }

    public String getFromRecentsIcon() {
        return FontAwesomeIcons.getIcon(fromRecentsIcon).orElseThrow();
    }
}
