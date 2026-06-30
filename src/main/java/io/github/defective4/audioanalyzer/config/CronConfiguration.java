package io.github.defective4.audioanalyzer.config;

public record CronConfiguration(Boolean enabled, CronTasksConfig tasks) {

    public CronConfiguration(Boolean enabled, CronTasksConfig tasks) {
        this.enabled = enabled == null ? false : enabled;
        this.tasks = tasks == null ? new CronTasksConfig(null) : tasks;
    }

    public CronConfiguration() {
        this(false, new CronTasksConfig());
    }
}
