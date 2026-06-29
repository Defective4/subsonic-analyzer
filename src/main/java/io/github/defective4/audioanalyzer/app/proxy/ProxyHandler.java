package io.github.defective4.audioanalyzer.app.proxy;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.imageio.ImageIO;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import io.github.defective4.audioanalyzer.app.App;
import io.github.defective4.audioanalyzer.app.proxy.virtual.VirtualLibraryManager;
import io.github.defective4.audioanalyzer.ml.Repository;
import io.github.defective4.audioanalyzer.ml.model.Track;
import io.github.defective4.audioanalyzer.subsonic.SubsonicAPI;
import io.github.defective4.audioanalyzer.subsonic.model.Playlist;
import io.javalin.http.ContentType;
import io.javalin.http.Context;

public class ProxyHandler {

    private final Gson gson = new Gson();
    private final VirtualLibraryManager libraryManager;
    private final Map<String, List<String>> proposedSongs = new HashMap<>();
    private final String targetBaseURL;

    public ProxyHandler(VirtualLibraryManager libraryManager, String targetBaseURL) {
        this.libraryManager = libraryManager;
        this.targetBaseURL = targetBaseURL;
    }

    public boolean getCoverArt(Context ctx) {
        String id = ctx.queryParam("id");
        if (id != null) {
            try {
                Optional<BufferedImage> image = libraryManager.getCoverManager().getCachedImage(id);
                if (image.isPresent()) {
                    ctx.status(200);
                    ctx.contentType(ContentType.IMAGE_PNG);
                    try (OutputStream os = ctx.outputStream()) {
                        ImageIO.write(image.get(), "png", os);
                        return true;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public void getPlaylist(Map<String, String> props, JsonObject obj)
            throws MalformedURLException, IOException, SQLException {
        String id = props.get("id");
        if (id == null) return;
        SubsonicAPI api = new SubsonicAPI(targetBaseURL, props);
        Playlist playlist = libraryManager.generateOrGetPlaylists(api).get(id);
        if (playlist != null) {
            obj.addProperty("status", "ok");
            obj.remove("error");
            obj.add("playlist", gson.toJsonTree(playlist));
        }
    }

    public void getPlaylists(Map<String, String> props, JsonObject obj)
            throws MalformedURLException, IOException, SQLException {
        SubsonicAPI api = new SubsonicAPI(targetBaseURL, props);
        JsonArray pls = obj.getAsJsonObject("playlists").getAsJsonArray("playlist");
        if (pls == null) {
            pls = new JsonArray();
            obj.getAsJsonObject("playlists").add("playlist", pls);
        }
        for (Playlist p : libraryManager.generateOrGetPlaylists(api).values()) {
            pls.add(gson.toJsonTree(p));
        }
    }

    public void getSimilarSongs(Repository repo, Map<String, String> props, JsonObject obj)
            throws SQLException, MalformedURLException, IOException {
        String id = props.get("id");
        if (id == null) return;
        String uname = props.get("u");
        if (uname == null) return;
        proposedSongs.computeIfAbsent(uname, t -> new ArrayList<>()).add(id);

        Optional<Track> trackOpt = repo.getTrackById(id);
        if (trackOpt.isPresent()) {
            int limit = Integer.parseInt(props.getOrDefault("count", "50"));
            SubsonicAPI api = new SubsonicAPI(targetBaseURL, props);
            Track track = trackOpt.get();
            List<Track> tracks = App.sortTrackStream(true, repo.getAllTracks(true).stream(), track)
                    .filter(t -> !t.id().equals(track.id())).filter(t -> !proposedSongs.get(uname).contains(t.id()))
                    .limit(limit).toList();
            JsonArray array = new JsonArray(limit);
            for (Track similar : tracks) {
                String sid = similar.id();
                proposedSongs.get(uname).add(sid);
                array.add(api.getRawSongData(sid));
            }
            obj.getAsJsonObject("similarSongs").add("song", array);
        }
    }

}
