package io.github.defective4.audioanalyzer.config;

import java.awt.Color;
import java.util.Objects;

import io.github.defective4.audioanalyzer.ml.mood.CompositeMood;
import io.github.defective4.audioanalyzer.ml.mood.MoodTypes;
import io.github.defective4.audioanalyzer.util.FontAwesomeIcons;

public record ProxyPlaylistConfig(String name, String mood, String icon, String color, int limit, String iconColor) {
    public ProxyPlaylistConfig(String name, String mood, String icon, String color, int limit, String iconColor) {
        this.name = name;
        this.mood = mood;
        this.icon = icon;
        this.color = color;
        this.limit = limit;
        this.iconColor = iconColor;
        getMood();
        getIcon();
        getColor();
        getIconColor();
    }

    public Color getIconColor() {
        return Color.decode(iconColor);
    }

    public CompositeMood getMood() {
        return Objects.requireNonNull(MoodTypes.getMood(mood.toLowerCase()));
    }

    public Color getColor() {
        return Color.decode(color);
    }

    public String getIcon() {
        return FontAwesomeIcons.getIcon(icon).orElseThrow();
    }
}
