package io.github.defective4.audioanalyzer.subsonic.model;

import com.google.gson.annotations.SerializedName;

public class Playlist extends Entity {
    public String changed;
    public String comment;
    public String coverArt;
    public String created;
    public int duration;
    public Song[] entry;
    @SerializedName("public")
    public boolean isPublic;
    public String name;
    public String owner;
    public int songCount;
}
