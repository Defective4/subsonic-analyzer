package io.github.defective4.audioanalyzer.config;

import java.awt.Color;
import java.util.Objects;

import io.github.defective4.audioanalyzer.ml.mood.CompositeMood;
import io.github.defective4.audioanalyzer.ml.mood.MoodTypes;
import io.github.defective4.audioanalyzer.util.FontAwesomeIcons;

public record ProxyPlaylistConfig(String name, String mood, String icon, String color, Integer limit,
        String iconColor) {
    public ProxyPlaylistConfig(String name, String mood, String icon, String color, Integer limit, String iconColor) {
        this.name = Objects.requireNonNull(name);
        this.mood = Objects.requireNonNull(mood);
        this.icon = icon;
        this.color = color;
        this.limit = limit == null ? 30 : limit;
        this.iconColor = iconColor;
        getMood();
        if (icon != null) getIcon();
        if (color != null) getColor();
        if (iconColor != null) getIconColor();
    }

    public Color getIconColor() {
        return iconColor == null ? Color.black : Color.decode(iconColor);
    }

    public CompositeMood getMood() {
        return Objects.requireNonNull(MoodTypes.getMood(mood.toLowerCase()));
    }

    public Color getColor() {
        return color == null ? Color.white : Color.decode(color);
    }

    public String getIcon() {
        return icon == null ? null : FontAwesomeIcons.getIcon(icon).orElseThrow();
    }
}
