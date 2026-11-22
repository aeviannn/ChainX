package edu.college.blockchain;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/** Small hashing helper used by each block. */
public final class StringUtil {
    private StringUtil() {
        // Utility classes do not need to be instantiated.
    }

    /** Returns the SHA-256 digest of text as a 64-character hexadecimal string. */
    public static String applySha256(String text) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(text.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashBytes);
        } catch (NoSuchAlgorithmException exception) {
            // SHA-256 is required by every Java implementation, so this indicates a broken runtime.
            throw new IllegalStateException("This Java runtime does not provide SHA-256.", exception);
        }
    }
}
