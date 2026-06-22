package io.github.defective4.audioanalyzer.subsonic.model;

public record SubsonicResponse(String status, SubsonicError error, Playlist playlist, String version) {
}
