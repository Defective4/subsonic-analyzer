package io.github.defective4.audioanalyzer.subsonic.model;

public class Song extends Entity {
    public String album;
    public String albumId;
    public String artist;
    public String artistId;
    public double averageRating;
    public int bitRate;
    public long bookmarkPosition;
    public String contentType;
    public String coverArt;
    public String created;
    public int discNumber;
    public int duration;
    public String genre;
    public boolean isDir;
    public boolean isVideo;
    public int originalHeight;
    public int originalWidth;
    public String parent;
    public String path;
    public long playCount;
    public long size;
    public String starred;
    public String suffix;
    public String title;
    public int track;
    public String transcodedContentType;
    public String transcodedSuffix;
    public String type;
    public int userRating;
    public int year;

    public Song() {}

    public Song(String id, boolean isDir, String title) {
        super.id = id;
        this.isDir = isDir;
        this.title = title;
    }

}
