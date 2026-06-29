package io.github.defective4.audioanalyzer.config;

import java.awt.Color;
import java.util.Objects;

import io.github.defective4.audioanalyzer.ml.mood.CompositeMood;
import io.github.defective4.audioanalyzer.ml.mood.MoodTypes;
import io.github.defective4.audioanalyzer.util.FontAwesomeIcons;

public record ProxyPlaylistConfig(String name, String mood, String icon, String color, int limit) {
    public ProxyPlaylistConfig(String name, String mood, String icon, String color, int limit) {
        this.name = name;
        this.mood = mood;
        this.icon = icon;
        this.color = color;
        this.limit = limit;
        getMood();
        getIcon();
        getColor();
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
