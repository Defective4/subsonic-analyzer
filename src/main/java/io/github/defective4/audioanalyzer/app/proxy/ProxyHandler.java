package io.github.defective4.audioanalyzer.app.proxy;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.GZIPOutputStream;

import javax.imageio.ImageIO;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import io.github.defective4.audioanalyzer.app.App;
import io.github.defective4.audioanalyzer.app.proxy.virtual.VirtualLibraryManager;
import io.github.defective4.audioanalyzer.ml.Repository;
import io.github.defective4.audioanalyzer.ml.model.Track;
import io.github.defective4.audioanalyzer.ml.mood.CompositeMood;
import io.github.defective4.audioanalyzer.ml.mood.MoodTypes;
import io.github.defective4.audioanalyzer.subsonic.SubsonicAPI;
import io.github.defective4.audioanalyzer.subsonic.model.Playlist;
import io.github.defective4.audioanalyzer.subsonic.model.SubsonicError;
import io.github.defective4.audioanalyzer.subsonic.model.SubsonicResponse;
import io.javalin.http.ContentType;
import io.javalin.http.Context;

public class ProxyHandler {

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final Gson gson = new Gson();
    private SubsonicResponse lastResponse = new SubsonicResponse(null, null, "none", "navidrome", "none", true);
    private final VirtualLibraryManager libraryManager;
    private final Map<String, List<String>> proposedSongs = new HashMap<>();

    private final String targetBaseURL;

    public ProxyHandler(VirtualLibraryManager libraryManager, String targetBaseURL) {
        this.libraryManager = libraryManager;
        this.targetBaseURL = targetBaseURL;
    }

    public boolean createPlaylist(Context ctx) {
        String name = getParam(ctx, "name");
        if (name != null) {
            int sx = name.indexOf('[');
            int ex = name.indexOf(']');

            if (sx > -1 && ex > sx) {
                String newName = name.substring(ex + 1).trim();
                String moodName = name.substring(sx + 1, ex);

                CompositeMood mood = MoodTypes.getMood(moodName.toLowerCase());
                if (mood != null) {
                    try {
                        SubsonicAPI api = new SubsonicAPI(targetBaseURL, ctx);
                        JsonObject plsObj = new JsonObject();
                        JsonObject pls = api.createPlaylistRaw(newName);
                        plsObj.add("playlist", pls);
                        writeResponse(ctx, null, plsObj);
                        String id = pls.get("id").getAsString();
                        executorService.submit(() -> {
                            try {
                                api.updatePlaylist(id,
                                        Arrays.stream(libraryManager.generateMoodPlaylist(api, 30, mood, null, null,
                                                "none", "none", null).entry).map(obj -> obj.get("id").getAsString())
                                                .toList(),
                                        -1, false);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        });
                        return true;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    writeResponse(ctx, new SubsonicError(70, "Invalid mood name \"%s\"".formatted(moodName)));
                    return true;
                }
            }
        }
        return false;
    }

    public boolean deletePlaylist(Context ctx) {
        String id = getParam(ctx, "id");
        try {
            if (isVirtualPlaylist(id, new SubsonicAPI(targetBaseURL, ctx))) {
                writeResponse(ctx, new SubsonicError(70, "Can't delete virtual playlists"));
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean getCoverArt(Context ctx) {
        String id = getParam(ctx, "id");
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

    public SubsonicResponse getLastResponse() {
        return lastResponse;
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

    public void setLastResponse(SubsonicResponse lastResponse) {
        this.lastResponse = Objects.requireNonNull(lastResponse);
    }

    public boolean updatePlaylist(Context ctx) {
        String id = getParam(ctx, "playlistId");
        try {
            SubsonicAPI api = new SubsonicAPI(targetBaseURL, ctx);
            Playlist pls = libraryManager.generateOrGetPlaylists(api).get(id);
            if (pls != null) {
                String name = getParam(ctx, "name");
                String comment = getParam(ctx, "comment");

                if (name != null) pls.name = name;
                if (comment != null) pls.comment = comment;

                JsonObject plsObj = new JsonObject();
                plsObj.add("playlist", gson.toJsonTree(pls));

                if (!getParams(ctx, "songIndexToRemove").isEmpty() || !getParams(ctx, "songIdToAdd").isEmpty()) {
                    writeResponse(ctx, new SubsonicError(70, "Can't modify virtual playlists"));
                    return true;
                }
                writeResponse(ctx, null, plsObj);
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean isVirtualPlaylist(String id, SubsonicAPI api) throws IOException, SQLException {
        return id != null && libraryManager.generateOrGetPlaylists(api).containsKey(id);
    }

    private void writeResponse(Context ctx, SubsonicError error) {
        writeResponse(ctx, error, null);
    }

    private void writeResponse(Context ctx, SubsonicError error, JsonObject extra) {
        ctx.status(200);
        ctx.contentType(ContentType.APPLICATION_JSON);
        JsonObject obj = new JsonObject();
        SubsonicResponse resp = new SubsonicResponse(error == null ? "ok" : "failed", error, lastResponse.version(),
                lastResponse.type(), lastResponse.serverVersion(), lastResponse.openSubsonic());
        JsonObject respObj = gson.toJsonTree(resp).getAsJsonObject();
        if (extra != null) for (Entry<String, JsonElement> entry : extra.asMap().entrySet())
            respObj.add(entry.getKey(), entry.getValue());
        obj.add("subsonic-response", respObj);
        boolean gzip = AnalyzerProxy.isGzip(ctx);
        if (gzip) ctx.header("Content-Encoding", "gzip");
        try (Writer writer = new OutputStreamWriter(
                gzip ? new GZIPOutputStream(ctx.outputStream()) : ctx.outputStream())) {
            writer.write(gson.toJson(obj));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String getParam(Context ctx, String key) {
        String val = ctx.queryParam(key);
        if (val == null) val = ctx.formParam(key);
        return val;
    }

    private static List<String> getParams(Context ctx, String key) {
        List<String> val = new ArrayList<>();
        val.addAll(ctx.queryParams(key));
        val.addAll(ctx.formParams(key));
        return Collections.unmodifiableList(val);
    }

}
