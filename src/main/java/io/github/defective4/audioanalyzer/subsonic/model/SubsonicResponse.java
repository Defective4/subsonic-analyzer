package io.github.defective4.audioanalyzer.subsonic.model;

public record SubsonicResponse(String status, AlbumList albumList, SongList directory) {

}
