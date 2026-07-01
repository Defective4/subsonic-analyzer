package io.github.defective4.audioanalyzer.config.proxy;

import java.util.Objects;

public record ProxyConfiguration(ProxyServerConfig server, ProxyVirtualLibConfig virtLibrary, CronConfiguration cron,
        Boolean enableAutoDJ, boolean enableManualDynamicPlaylists) {

    public ProxyConfiguration(ProxyServerConfig server, ProxyVirtualLibConfig virtLibrary, CronConfiguration cron,
            Boolean enableAutoDJ, boolean enableManualDynamicPlaylists) {
        this.server = server;
        this.virtLibrary = Objects.requireNonNull(virtLibrary);
        this.cron = Objects.requireNonNull(cron);
        this.enableAutoDJ = enableAutoDJ == null ? true : enableAutoDJ;
        this.enableManualDynamicPlaylists = enableManualDynamicPlaylists;
    }

    public ProxyConfiguration() {
        this(new ProxyServerConfig(), new ProxyVirtualLibConfig(), new CronConfiguration(), true, true);
    }
}
