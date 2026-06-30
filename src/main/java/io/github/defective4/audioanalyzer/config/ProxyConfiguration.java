package io.github.defective4.audioanalyzer.config;

import java.util.Objects;

public record ProxyConfiguration(ProxyVirtualLibConfig virtLibrary, CronConfiguration cron, Boolean enableAutoDJ,
        boolean enableManualDynamicPlaylists) {

    public ProxyConfiguration(ProxyVirtualLibConfig virtLibrary, CronConfiguration cron, Boolean enableAutoDJ,
            boolean enableManualDynamicPlaylists) {
        this.virtLibrary = Objects.requireNonNull(virtLibrary);
        this.cron = Objects.requireNonNull(cron);
        this.enableAutoDJ = enableAutoDJ == null ? true : enableAutoDJ;
        this.enableManualDynamicPlaylists = enableManualDynamicPlaylists;
    }

    public ProxyConfiguration() {
        this(new ProxyVirtualLibConfig(), new CronConfiguration(), true, true);
    }
}
