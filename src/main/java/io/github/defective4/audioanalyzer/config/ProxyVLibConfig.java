package io.github.defective4.audioanalyzer.config;

public record ProxyVLibConfig(boolean enablePlaylistEngine, boolean generateFromRecents, int generateFromRecentsLimit) {
    public ProxyVLibConfig() {
        this(true, true, 30);
    }
}
