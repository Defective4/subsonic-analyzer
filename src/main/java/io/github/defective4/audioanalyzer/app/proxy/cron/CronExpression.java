package io.github.defective4.audioanalyzer.app.proxy.cron;

public class CronExpression {
    private final CronField minute, hour, dayOfMonth, month, dayOfWeek;

    public CronExpression(String expression) {
        String[] parts = expression.split(" ");
        if (parts.length != 5) throw new IllegalArgumentException("The cron expression requires 5 fields");
        minute = new CronField(parts[0], 59);
        hour = new CronField(parts[1], 23);
        dayOfMonth = new CronField(parts[2], 31);
        month = new CronField(parts[3], 12, 1);
        dayOfWeek = new CronField(parts[4], 6);
    }

    @Override
    public String toString() {
        return "%s %s %s %s %s".formatted(minute, hour, dayOfMonth, month, dayOfWeek);
    }
}
