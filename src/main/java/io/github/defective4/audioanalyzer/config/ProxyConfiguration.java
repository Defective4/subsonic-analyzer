package io.github.defective4.audioanalyzer.config;

public record ProxyConfiguration(ProxyVirtualLibConfig virtLibrary, CronConfiguration cron, boolean enableAutoDJ,
        boolean enableManualDynamicPlaylists) {
    public ProxyConfiguration() {
        this(new ProxyVirtualLibConfig(), new CronConfiguration(), true, true);
    }
}
