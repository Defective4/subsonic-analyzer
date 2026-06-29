package io.github.defective4.audioanalyzer.app.proxy;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.zip.GZIPOutputStream;

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
import io.github.defective4.audioanalyzer.subsonic.model.SubsonicError;
import io.github.defective4.audioanalyzer.subsonic.model.SubsonicResponse;
import io.javalin.http.ContentType;
import io.javalin.http.Context;

public class ProxyHandler {

    private final Gson gson = new Gson();
    private SubsonicResponse lastResponse = new SubsonicResponse(null, null, null, "none", "navidrome", "none", true);
    private final VirtualLibraryManager libraryManager;
    private final Map<String, List<String>> proposedSongs = new HashMap<>();
    private final String targetBaseURL;

    public ProxyHandler(VirtualLibraryManager libraryManager, String targetBaseURL) {
        this.libraryManager = libraryManager;
        this.targetBaseURL = targetBaseURL;
    }

    public boolean deletePlaylist(Context ctx) {
        String id = getParam(ctx, "id");
        try {
            if (isVirtualPlaylist(id, new SubsonicAPI(targetBaseURL, ctx))) {
                SubsonicResponse resp = new SubsonicResponse("failed",
                        new SubsonicError(70, "Can't delete virtual playlists"), null, lastResponse.version(),
                        lastResponse.type(), lastResponse.serverVersion(), lastResponse.openSubsonic());
                writeResponse(ctx, resp);
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

                if (!getParams(ctx, "songIndexToRemove").isEmpty()) {
                    writeResponse(ctx,
                            new SubsonicResponse("failed",
                                    new SubsonicError(70, "Can't remove songs from virtual playlists"), pls,
                                    lastResponse.version(), lastResponse.type(), lastResponse.serverVersion(),
                                    lastResponse.openSubsonic()));
                    return true;
                } else if (!getParams(ctx, "songIdToAdd").isEmpty()) {
                    writeResponse(ctx, new SubsonicResponse("failed",
                            new SubsonicError(70, "Can't add songs to virtual playlists"), pls, lastResponse.version(),
                            lastResponse.type(), lastResponse.serverVersion(), lastResponse.openSubsonic()));
                    return true;
                }

                writeResponse(ctx, new SubsonicResponse("ok", null, pls, lastResponse.version(), lastResponse.type(),
                        lastResponse.serverVersion(), lastResponse.openSubsonic()));
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

    private void writeResponse(Context ctx, SubsonicResponse resp) {
        ctx.status(200);
        ctx.contentType(ContentType.APPLICATION_JSON);
        JsonObject obj = new JsonObject();
        obj.add("subsonic-response", gson.toJsonTree(resp));
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
