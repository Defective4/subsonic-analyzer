package io.github.defective4.audioanalyzer.app.proxy.cron;

import java.util.Arrays;
import java.util.Objects;

public class CronField {

    private final int[] list;
    private final int max, min;
    private final Integer rangeMin, rangeMax;
    private final Integer value;

    public CronField(String field, int max) {
        this(field, max, 0);
    }

    public CronField(String field, int max, int min) {
        this.min = min;
        this.max = max;
        Objects.requireNonNull(field);
        Integer value = null;
        Integer rangeMin = null;
        Integer rangeMax = null;
        int[] list = null;
        try {
            value = parseInteger(field);
        } catch (NumberFormatException e) {}

        if (value == null) {
            if (field.contains("-")) {
                String[] parts = field.split("-");
                if (parts.length != 2) throw new IllegalArgumentException("Invalid range: " + field);
                rangeMin = parseInteger(parts[0]);
                rangeMax = parseInteger(parts[1]);
            } else if (field.contains(",")) {
                list = Arrays.stream(field.split(",")).mapToInt(this::parseInteger).toArray();
            }
        }

        this.list = list;
        this.rangeMax = rangeMax;
        this.rangeMin = rangeMin;
        this.value = value;
    }

    public boolean matches(int value) {
        if (this.value != null) return this.value == value;
        if (list != null) return Arrays.stream(list).anyMatch(i -> i == value);
        if (rangeMin != null && rangeMax != null) return value >= rangeMin && value <= rangeMax;
        return true;
    }

    @Override
    public String toString() {
        return value != null ? String.valueOf(value)
                : list != null ? String.join(",", Arrays.stream(list).mapToObj(String::valueOf).toArray(String[]::new))
                        : rangeMin != null && rangeMax != null ? "%s-%s".formatted(rangeMin, rangeMax) : "*";
    }

    private int parseInteger(String part) {
        int val = Integer.parseInt(part);
        if (val > max) throw new IllegalArgumentException("The field can't be bigger than %s".formatted(max));
        if (val < min) throw new IllegalArgumentException("The field can't be less than %s".formatted(min));
        return val;
    }

}
