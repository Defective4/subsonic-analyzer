package io.github.defective4.audioanalyzer;

import io.github.defective4.audioanalyzer.subsonic.SubsonicAPI;

public class Main {
    public static void main(String[] args) {
        try {
            SubsonicAPI api = new SubsonicAPI("navidrome", "navidrome".toCharArray(), "https://music.raspberry.local");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
