package io.github.defective4.audioanalyzer.subsonic.model;

public record SubsonicResponse(String status, SubsonicError error, String version, String type, String serverVersion,
        boolean openSubsonic) {
}
