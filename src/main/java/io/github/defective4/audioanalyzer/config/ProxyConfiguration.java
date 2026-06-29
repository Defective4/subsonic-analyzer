package io.github.defective4.audioanalyzer.config;

public record ProxyConfiguration(ProxyVirtualLibConfig virtLibrary) {
    public ProxyConfiguration() {
        this(new ProxyVirtualLibConfig());
    }
}
