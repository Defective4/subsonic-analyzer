package io.github.defective4.audioanalyzer.config;

import java.awt.Color;
import java.util.List;

import io.github.defective4.audioanalyzer.util.FontAwesomeIcons;

public record ProxyVLibConfig(boolean enableVirtualLibrary, boolean generateFromRecents, int fromRecentsLimit,
        String fromRecentsIcon, String fromRecentsCoverColor, String fromRecentsName, String fromRecentsIconColor,
        List<ProxyPlaylistConfig> playlists) {
    public ProxyVLibConfig() {
        this(true, true, 30, "history", "#00ffff", "From your recent sessions", "#000000",
                List.of(new ProxyPlaylistConfig("Mix to study to", "study", "book_open", "#00ff55", 30, "#000000"),
                        new ProxyPlaylistConfig("Happy mix", "happy", "heart", "#ff0000", 30, "#000000")));
        getFromRecentsIcon();
        getFromRecentsCoverColor();
        getFromRecentsIconColor();
    }

    public Color getFromRecentsIconColor() {
        return Color.decode(fromRecentsIconColor);
    }

    public Color getFromRecentsCoverColor() {
        return Color.decode(fromRecentsCoverColor);
    }

    public String getFromRecentsIcon() {
        return FontAwesomeIcons.getIcon(fromRecentsIcon).orElseThrow();
    }
}
