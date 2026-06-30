package io.github.defective4.audioanalyzer.config;

public record CronConfiguration(boolean enabled, CronTasksConfig tasks) {
    public CronConfiguration() {
        this(false, new CronTasksConfig());
    }
}
