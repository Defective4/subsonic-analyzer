package io.github.defective4.audioanalyzer.config.proxy;

import java.awt.Color;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import io.github.defective4.audioanalyzer.expr.ExpressionConversionException;
import io.github.defective4.audioanalyzer.expr.IntegerExpressionConverter;
import io.github.defective4.audioanalyzer.expr.NumericExpression;
import io.github.defective4.audioanalyzer.ml.mood.CompositeMood;
import io.github.defective4.audioanalyzer.ml.mood.MoodTypes;
import io.github.defective4.audioanalyzer.util.FontAwesomeIcons;

public record ProxyPlaylistConfig(String name, String mood, String icon, String color, Integer limit, String iconColor,
        Map<String, String> customFilters) {
    public ProxyPlaylistConfig(String name, String mood, String icon, String color, Integer limit, String iconColor,
            Map<String, String> customFilters) {
        this.name = Objects.requireNonNull(name);
        this.mood = mood;
        this.icon = icon;
        this.color = color;
        this.limit = limit == null ? 30 : limit;
        this.iconColor = iconColor;
        this.customFilters = customFilters == null ? Map.of() : customFilters;
        if (mood == null && this.customFilters.isEmpty())
            throw new IllegalArgumentException("Either mood or customFilters are required");
        getMood();
        if (icon != null) getIcon();
        if (color != null) getColor();
        if (iconColor != null) getIconColor();
        getCustomFilters();
    }

    public Map<String, NumericExpression> getCustomFilters() {
        Map<String, NumericExpression> expr = new HashMap<>();
        IntegerExpressionConverter conv = new IntegerExpressionConverter(100);
        for (Entry<String, String> entry : customFilters.entrySet()) {
            try {
                expr.put(entry.getKey(), conv.apply(entry.getValue()));
            } catch (ExpressionConversionException e) {
                throw new IllegalArgumentException(e);
            }
        }
        return Collections.unmodifiableMap(expr);
    }

    public Color getIconColor() {
        return iconColor == null ? Color.black : Color.decode(iconColor);
    }

    public CompositeMood getMood() {
        return mood == null ? new CompositeMood(getCustomFilters())
                : Objects.requireNonNull(MoodTypes.getMood(mood.toLowerCase()));
    }

    public Color getColor() {
        return color == null ? Color.white : Color.decode(color);
    }

    public String getIcon() {
        return icon == null ? null : FontAwesomeIcons.getIcon(icon).orElseThrow();
    }
}
