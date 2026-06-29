package io.github.defective4.audioanalyzer.config;

import java.awt.Color;
import java.util.List;

import io.github.defective4.audioanalyzer.util.FontAwesomeIcons;

public record ProxyVLibConfig(boolean enablePlaylistEngine, boolean generateFromRecents, int fromRecentsLimit,
        String fromRecentsIcon, String fromRecentsColor, String fromRecentsName, List<ProxyPlaylistConfig> playlists) {
    public ProxyVLibConfig() {
        this(true, true, 30, "history", "#00ffff", "From your recent sessions", List.of(
                new ProxyPlaylistConfig("Mix to study to", "study", "book_open", "#00ff55", 30),
                new ProxyPlaylistConfig("Happy mix", "happy", "heart", "#ff0000", 30)
                ));
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
