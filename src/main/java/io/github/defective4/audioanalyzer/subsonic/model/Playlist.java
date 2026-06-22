package io.github.defective4.audioanalyzer.subsonic.model;

import com.google.gson.annotations.SerializedName;

public class Playlist extends Entity {
    public String changed;
    public String comment;
    public String coverArt;
    public String created;
    public Integer duration;
    public Song[] entry;
    @SerializedName("public")
    public Boolean isPublic;
    public String name;
    public String owner;
    public Integer songCount;

    public Playlist() {}

    public Playlist(String id, String name) {
        this.name = name;
        super.id = id;
    }

}
