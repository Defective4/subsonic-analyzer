package io.github.defective4.audioanalyzer.config;

import io.github.defective4.audioanalyzer.app.proxy.cron.CronExpression;

public record CronTasksConfig(String regenerateVirtualLibrary) {

    public CronTasksConfig(String regenerateVirtualLibrary) {
        this.regenerateVirtualLibrary = regenerateVirtualLibrary;
        if (regenerateVirtualLibrary != null) new CronExpression(regenerateVirtualLibrary);
    }

    public CronTasksConfig() {
        this("0 0 * * *");
    }
}
