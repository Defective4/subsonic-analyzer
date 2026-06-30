package io.github.defective4.audioanalyzer.config;

public record CronTasksConfig(String regenerateVirtualLibrary) {
    public CronTasksConfig() {
        this("0 0 * * *");
    }
}
