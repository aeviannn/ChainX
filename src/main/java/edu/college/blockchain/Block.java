package edu.college.blockchain;

/**
 * One record in the chain. The hash includes all fields that should be protected,
 * including the previous block's hash and the nonce used by proof of work.
 */
public class Block {
    private final long index;
    private final long timestamp;
    private String data;
    private final String previousHash;
    private String hash;
    private long nonce;

    public Block(long index, long timestamp, String data, String previousHash, long nonce, String hash) {
        this.index = index;
        this.timestamp = timestamp;
        this.data = data;
        this.previousHash = previousHash;
        this.nonce = nonce;
        this.hash = hash;
    }

    /** Creates a new, not-yet-mined block. */
    public Block(long index, long timestamp, String data, String previousHash) {
        this(index, timestamp, data, previousHash, 0L, "");
        this.hash = calculateHash();
    }

    /** Calculates a deterministic fingerprint from this block's contents. */
    public String calculateHash() {
        // Prefix the data with its length so separators inside user text are unambiguous.
        String contents = index + "|" + timestamp + "|" + previousHash + "|" + nonce
                + "|" + data.length() + ":" + data;
        return StringUtil.applySha256(contents);
    }

    /** Tries nonce values until the hash begins with the requested zeroes. */
    public void mineBlock(int difficulty) {
        if (difficulty < 0 || difficulty > 6) {
            throw new IllegalArgumentException("Difficulty must be between 0 and 6.");
        }

        String targetPrefix = "0".repeat(difficulty);
        nonce = 0L;
        do {
            nonce++;
            hash = calculateHash();
        } while (!hash.startsWith(targetPrefix));
    }

    /** Used only by the menu's tampering lesson; the old hash is intentionally left unchanged. */
    public void tamperDataForDemo(String replacementData) {
        data = replacementData;
    }

    public long getIndex() {
        return index;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getData() {
        return data;
    }

    public String getPreviousHash() {
        return previousHash;
    }

    public String getHash() {
        return hash;
    }

    public long getNonce() {
        return nonce;
    }
}
