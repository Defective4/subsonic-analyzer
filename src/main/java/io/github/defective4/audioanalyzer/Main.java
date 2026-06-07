package io.github.defective4.audioanalyzer;

import io.github.defective4.audioanalyzer.subsonic.SubsonicAPI;
import io.github.defective4.audioanalyzer.subsonic.model.SubsonicResponse;

public class Main {
    public static void main(String[] args) {
        try {
            SubsonicAPI api = new SubsonicAPI("navidrome", "navidrome".toCharArray(), "https://music.raspberry.local");
            SubsonicResponse resp = api.getArtists();
            System.out.println(resp.artists().index()[0].artist()[0].id());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
