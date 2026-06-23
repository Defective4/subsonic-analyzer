package io.github.defective4.audioanalyzer.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public class MD5 {

    private static final HexFormat HEX_FORMAT = HexFormat.of();
    private static final MessageDigest MD5;

    static {
        try {
            MD5 = MessageDigest.getInstance("md5");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    private MD5() {}

    public static String hash(String data) {
        MD5.reset();
        return HEX_FORMAT.formatHex(MD5.digest(data.getBytes(StandardCharsets.UTF_8)));
    }
}
