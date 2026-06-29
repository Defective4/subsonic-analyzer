package io.github.defective4.audioanalyzer.app.proxy.virtual;

import java.awt.Color;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import io.github.defective4.audioanalyzer.app.App;
import io.github.defective4.audioanalyzer.config.ProxyPlaylistConfig;
import io.github.defective4.audioanalyzer.config.ProxyVirtualLibConfig;
import io.github.defective4.audioanalyzer.ml.Repository;
import io.github.defective4.audioanalyzer.ml.model.Track;
import io.github.defective4.audioanalyzer.ml.mood.CompositeMood;
import io.github.defective4.audioanalyzer.subsonic.SubsonicAPI;
import io.github.defective4.audioanalyzer.subsonic.model.Album;
import io.github.defective4.audioanalyzer.subsonic.model.Playlist;
import io.github.defective4.audioanalyzer.subsonic.model.Song;

public class VirtualLibraryManager {
    private static final DateFormat FMT = new SimpleDateFormat("yyyy-MM-ddZ");

    private final ProxyVirtualLibConfig config;
    private final VirtualCoverManager coverManager = new VirtualCoverManager();

    private final Map<String, Map<String, Playlist>> generatedPlaylists = new HashMap<>();
    private final Random rand = new Random();

    private final Repository repo;
    private final String targetBaseURL;

    public VirtualLibraryManager(Repository repo, String targetBaseURL, ProxyVirtualLibConfig config)
            throws MalformedURLException {
        this.repo = repo;
        this.targetBaseURL = URI.create(targetBaseURL).toURL().toString();
        this.config = config;
    }

    public Map<String, Playlist> generateOrGetPlaylists(SubsonicAPI api) throws IOException, SQLException {
        String user = api.getUsername();
        if (!generatedPlaylists.containsKey(user)) {
            HashMap<String, Playlist> map = new LinkedHashMap<>();
            if (config.generateFromRecents()) {
                Playlist recent = generatePlaylistFromRecents(api, config.fromRecentsLimit());
                map.put(recent.id, recent);
            }
            for (ProxyPlaylistConfig pls : config.playlists()) {
                Playlist playlist = generateMoodPlaylist(api, pls.limit(), pls.getMood(), pls.getIcon(), pls.getColor(),
                        "virt_" + pls.mood(), pls.name(), pls.getIconColor());
                map.put(playlist.id, playlist);
            }
            generatedPlaylists.put(user, Collections.unmodifiableMap(map));
        }
        return Collections.unmodifiableMap(generatedPlaylists.get(user));
    }

    public VirtualCoverManager getCoverManager() {
        return coverManager;
    }

    private Playlist generateMoodPlaylist(SubsonicAPI api, int limit, CompositeMood mood, String coverIcon, Color color,
            String id, String name, Color iconColor) throws SQLException, IOException {
        List<Track> allTracks = new ArrayList<>(repo.getAllTracks(true));
        Collections.shuffle(allTracks, rand);
        List<Track> tracks = allTracks.stream().filter(mood::matches).limit(limit).toList();
        return generatePlaylist(api, null, tracks, id, name, coverIcon, color, iconColor);
    }

    private Playlist generatePlaylist(SubsonicAPI api, String cover, List<Track> tracks, String id, String name,
            String coverIcon, Color color, Color iconColor) throws IOException {
        List<JsonObject> similar = new ArrayList<>();
        for (Track track : tracks) {
            similar.add(api.getRawSongData(track.id()));
        }

        Playlist fromRecent = new Playlist(id, name);
        fromRecent.comment = config.defaultPlaylistDescription();
        fromRecent.duration = similar.stream()
                .mapToInt(s -> s.has("duration") ? s.getAsJsonPrimitive("duration").getAsInt() : 0).sum();
        fromRecent.entry = similar.toArray(JsonObject[]::new);
        fromRecent.isPublic = true;
        fromRecent.owner = config.defaultPlaylistAuthor();
        fromRecent.songCount = similar.size();

        String date = FMT.format(new Date(System.currentTimeMillis()));
        fromRecent.changed = date;
        fromRecent.created = date;
        if (cover == null && !similar.isEmpty()) {
            JsonElement e = similar.get(0).get("coverArt");
            if (e != null) cover = e.getAsString();
        }
        if (!similar.isEmpty()) {
            fromRecent.coverArt = fromRecent.id + "_c";
            coverManager.generateAndSaveCover(api, similar, fromRecent.coverArt, coverIcon, color, iconColor);
        }

        return fromRecent;
    }

    private Playlist generatePlaylistFromRecents(SubsonicAPI api, int limit) throws IOException, SQLException {
        List<Album> albums = api.getAlbumList(5, 0, "recent");
        List<Song> allSongs = new ArrayList<>();
        for (Album album : albums) allSongs.addAll(api.getMusicDirectory(album.id));

        List<Song> songs = allSongs.stream().sorted((o1, o2) -> {
            long l1 = o1.playCount;
            long l2 = o2.playCount;
            return l1 > l2 ? -1 : l2 > l1 ? 1 : 0;
        }).limit(5).toList();

        List<Track> baseTracks = new ArrayList<>();
        for (Song song : songs) {
            repo.getTrackById(song.id).ifPresent(baseTracks::add);
        }

        List<Track> all = repo.getAllTracks(true);
        List<Track> similarTracks = new ArrayList<>();

        int perTrack = Math.ceilDiv(limit, baseTracks.size());
        for (Track track : baseTracks) {
            App.sortTrackStream(true, all.stream(), track).limit(perTrack).forEach(t -> {
                if (!similarTracks.contains(t)) similarTracks.add(t);
            });
        }

        return generatePlaylist(api, albums.get(0).coverArt, similarTracks, "virt_recent", config.fromRecentsName(),
                config.getFromRecentsIcon(), config.getFromRecentsCoverColor(), config.getFromRecentsIconColor());
    }
}
