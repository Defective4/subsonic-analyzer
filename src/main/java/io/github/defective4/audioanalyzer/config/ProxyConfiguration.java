package io.github.defective4.audioanalyzer.config;

public record ProxyConfiguration(ProxyVLibConfig virtLibrary) {
    public ProxyConfiguration() {
        this(new ProxyVLibConfig());
    }
}
