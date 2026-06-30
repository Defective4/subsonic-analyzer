package io.github.defective4.audioanalyzer.config;

public record ProxyConfiguration(ProxyVirtualLibConfig virtLibrary, CronConfiguration cron) {
    public ProxyConfiguration() {
        this(new ProxyVirtualLibConfig(), new CronConfiguration());
    }
}
