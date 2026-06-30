package io.github.defective4.audioanalyzer.subsonic;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import javax.imageio.ImageIO;

import org.slf4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import io.github.defective4.audioanalyzer.app.proxy.AnalyzerProxy;
import io.github.defective4.audioanalyzer.exception.SubsonicException;
import io.github.defective4.audioanalyzer.subsonic.model.Album;
import io.github.defective4.audioanalyzer.subsonic.model.Playlist;
import io.github.defective4.audioanalyzer.subsonic.model.Song;
import io.github.defective4.audioanalyzer.subsonic.model.SubsonicError;
import io.github.defective4.audioanalyzer.subsonic.model.SubsonicResponse;
import io.github.defective4.audioanalyzer.util.MD5;
import io.javalin.http.Context;

public class SubsonicAPI {
    private static final String CLIENT_ID = "audio-analyzer";
    private static final String VERSION = "1.16.1";

    private final String baseURL;
    private final String clientId;
    private final Gson gson = new GsonBuilder().create();
    private final char[] password;
    private final String salt;
    private final String token;

    private final String username;

    private final String version;

    public SubsonicAPI(String username, char[] password, String baseURL) throws MalformedURLException {
        this(baseURL, password, username, VERSION, CLIENT_ID, null, null);
    }

    public SubsonicAPI(String baseURL, char[] password, String username, String version, String clientId, String token,
            String salt) throws MalformedURLException {
        if (!baseURL.endsWith("/")) baseURL = baseURL + "/";

        this.baseURL = URI.create(baseURL).toURL().toString() + "rest/";
        this.password = password;
        this.username = Objects.requireNonNull(username);
        this.version = Objects.requireNonNull(version);
        this.clientId = Objects.requireNonNull(clientId);
        this.token = token;
        this.salt = salt;

        if ((password == null || password.length == 0) && (token == null || salt == null))
            throw new IllegalArgumentException("Missing password, token, or salt argument");
    }

    public SubsonicAPI(String baseURL, Context ctx) throws MalformedURLException {
        this(baseURL, AnalyzerProxy.getParams(ctx));
    }

    public SubsonicAPI(String baseURL, Map<String, String> props) throws MalformedURLException {
        this(baseURL, props.getOrDefault("p", "").toCharArray(), props.get("u"), props.get("v"), props.get("c"),
                props.get("t"), props.get("s"));
    }

    public Playlist createPlaylist(String name) throws IOException {
        return gson.fromJson(createPlaylistRaw(name), Playlist.class);
    }

    public JsonObject createPlaylistRaw(String name) throws IOException {
        return requestRaw("createPlaylist", Map.of("name", name)).getAsJsonObject("playlist");
    }

    public void deletePlaylist(String id) throws IOException {
        request("deletePlaylist", Map.of("id", id));
    }

    public InputStream download(Song entity) throws IOException {
        return URI.create(baseURL + "download" + constructQueryString(Map.of("id", entity.id))).toURL().openStream();
    }

    public List<Album> getAlbumList(int limit, int offset) throws IOException {
        return getAlbumList(limit, offset, "newest");
    }

    public List<Album> getAlbumList(int limit, int offset, String type) throws IOException {
        return List.of(gson.fromJson(requestRaw("getAlbumList2", Map.of("type", type, "size", limit, "offset", offset))
                .getAsJsonObject("albumList2").get("album"), Album[].class));
    }

    public List<Album> getAllAlbums(Logger logger, String filterAlbumArtist) throws IOException {
        List<Album> albums = new ArrayList<>();
        int offset = 0;
        int i = 0;
        while (true) {
            List<Album> as = getAlbumList(500, offset);
            logger.info("Downloaded chunk %s".formatted(++i));
            albums.addAll(as);
            if (as.size() == 500)
                offset += 500;
            else
                break;
        }
        return Collections.unmodifiableList(filterAlbumArtist == null ? albums
                : albums.stream().filter(a -> a.artist.equalsIgnoreCase(filterAlbumArtist)).toList());
    }

    public List<Song> getAllMusic(Logger logger, String filterArtist, String filterAlbumArtist) throws IOException {
        List<Song> songs = new ArrayList<>();
        List<Album> albums = getAllAlbums(logger, filterAlbumArtist);
        int i = 0;
        for (Album album : albums) {
            if (++i % 100 == 0) logger.info("Downloaded metadata for %s out of %s albums".formatted(i, albums.size()));
            songs.addAll(getMusicDirectory(album.id));
        }
        return Collections.unmodifiableList(filterArtist == null ? songs
                : songs.stream().filter(s -> s.artist.equalsIgnoreCase(filterArtist)).toList());
    }

    public BufferedImage getCoverArt(String id) throws IOException {
        try (InputStream in = URI.create(baseURL + "getCoverArt" + constructQueryString(Map.of("id", id))).toURL()
                .openStream()) {
            return ImageIO.read(in);
        }
    }

    public List<Song> getMusicDirectory(String id) throws IOException {
        return List.of(gson.fromJson(
                requestRaw("getMusicDirectory", Map.of("id", id)).getAsJsonObject("directory").get("child"),
                Song[].class));
    }

    public Playlist getPlaylist(String id) throws IOException {
        return gson.fromJson(requestRaw("getPlaylist", Map.of("id", id)).getAsJsonObject("playlist"), Playlist.class);
    }

    public List<Playlist> getPlaylists() throws IOException {
        return List.of(gson.fromJson(requestRaw("getPlaylists", Map.of()).getAsJsonObject("playlists").get("playlist"),
                Playlist[].class));
    }

    public JsonObject getRawSongData(String id) throws IOException {
        return requestRaw("getSong", Map.of("id", id)).getAsJsonObject("song");
    }

    public String getUsername() {
        return username;
    }

    public SubsonicResponse ping() throws IOException {
        return request("ping", Map.of());
    }

    public void updatePlaylist(String id, List<String> songToAdd, int songToRemove, boolean isPublic)
            throws IOException {
        Map<String, Object> map = new HashMap<>();
        if (songToAdd != null) map.put("songIdToAdd", songToAdd);
        if (songToRemove >= 0) map.put("songIndexToRemove", songToRemove);
        map.put("playlistId", id);
        map.put("public", isPublic);
        request("updatePlaylist", map);
    }

    private String computeToken(String salt) {
        return MD5.hash(new String(getPassword()) + salt);
    }

    private String constructQueryString(Map<String, Object> queryParameters) {
        Map<String, Object> params = new HashMap<>();
        String salt = generateSalt();
        params.put("u", username);
        if (token != null && salt != null) {
            params.put("t", token);
            params.put("s", this.salt);
        } else {
            params.put("t", computeToken(salt));
            params.put("s", salt);
        }
        params.put("v", version);
        params.put("c", clientId);
        params.put("f", "json");
        StringBuilder queryBuilder = new StringBuilder("?");
        for (Entry<String, Object> entry : queryParameters.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof List<?> ls) {
                for (Object obj : ls) {
                    queryBuilder.append("%s=%s&".formatted(entry.getKey(),
                            URLEncoder.encode(obj.toString(), StandardCharsets.UTF_8)));
                }
            } else {
                params.put(entry.getKey(), value);
            }
        }
        params.forEach((k, v) -> queryBuilder
                .append(String.format("%s=%s&", k, URLEncoder.encode(v.toString(), StandardCharsets.UTF_8))));
        String queryString = queryBuilder.toString();
        return queryString.substring(0, queryBuilder.length() - 1);
    }

    private char[] getPassword() {
        if (new String(password).startsWith("enc:")) {
            char[] decoded = new char[(password.length - 4) / 2];
            byte[] dBytes = HexFormat.of().parseHex(password, 4, password.length);
            for (int i = 0; i < dBytes.length; i++) {
                decoded[i] = (char) dBytes[i];
            }
            return decoded;
        }
        return password;
    }

    private SubsonicResponse request(String path, Map<String, Object> queryParameters)
            throws JsonSyntaxException, SubsonicException, IOException {
        SubsonicResponse response = gson.fromJson(requestRaw(path, queryParameters), SubsonicResponse.class);
        SubsonicError error = response.error();
        if (error != null) throw new SubsonicException("Error %s: %s".formatted(error.code(), error.message()), error);
        return response;
    }

    private JsonObject requestRaw(String path, Map<String, Object> queryParameters)
            throws IOException, SubsonicException {
        HttpURLConnection con = null;
        try {
            String queryString = constructQueryString(queryParameters);
            con = (HttpURLConnection) URI.create(baseURL + path + queryString).toURL().openConnection();
            try (Reader reader = new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8)) {
                JsonElement json = JsonParser.parseReader(reader).getAsJsonObject().get("subsonic-response");
                return json.getAsJsonObject();
            }
        } finally {
            if (con != null) con.disconnect();
        }
    }

    private static String generateSalt() {
        return Long.toHexString(System.currentTimeMillis());
    }
}
