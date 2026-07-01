package io.github.defective4.audioanalyzer.config.proxy;

import java.net.MalformedURLException;
import java.net.URI;
import java.util.Objects;

public record ProxyServerConfig(String host, Integer port, String targetURL) {
    public ProxyServerConfig(String host, Integer port, String targetURL) {
        this.host = Objects.requireNonNull(host);
        this.port = Objects.requireNonNull(port);
        if (port > Short.MAX_VALUE || port <= 0) throw new IllegalArgumentException("Invalid port: " + port);
        try {
            this.targetURL = URI.create(targetURL).toURL().toString();
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public ProxyServerConfig() {
        this("0.0.0.0", 8080, "https://demo.navidrome.org/");
    }
}
