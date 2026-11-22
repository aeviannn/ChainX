package edu.college.blockchain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Creates and stores a local chain of blocks. */
public class BlockchainManager {
    private final int difficulty;
    private final List<Block> chain = new ArrayList<>();

    public BlockchainManager(int difficulty) {
        if (difficulty < 0 || difficulty > 6) {
            throw new IllegalArgumentException("Difficulty must be between 0 and 6.");
        }
        this.difficulty = difficulty;
        reset();
    }

    /** Adds a record and mines it so its hash meets the configured difficulty. */
    public Block addBlock(String data) {
        if (data == null || data.isBlank()) {
            throw new IllegalArgumentException("Block data cannot be empty.");
        }
        Block previous = chain.get(chain.size() - 1);
        Block next = new Block(chain.size(), System.currentTimeMillis(), data, previous.getHash());
        next.mineBlock(difficulty);
        chain.add(next);
        return next;
    }

    /** Replaces all blocks with a fresh, mined genesis block. */
    public void reset() {
        chain.clear();
        Block genesis = new Block(0, System.currentTimeMillis(), "Genesis Block",
                BlockchainValidator.GENESIS_PREVIOUS_HASH);
        genesis.mineBlock(difficulty);
        chain.add(genesis);
    }

    /** Replaces the current chain with blocks loaded from JSON. */
    public void replaceChain(List<Block> loadedChain) {
        if (loadedChain == null || loadedChain.isEmpty()) {
            throw new IllegalArgumentException("A saved chain must contain at least its genesis block.");
        }
        chain.clear();
        chain.addAll(loadedChain);
    }

    /** For the educational tampering menu only. */
    public void tamperBlockForDemo(int index, String replacementData) {
        if (index < 0 || index >= chain.size()) {
            throw new IndexOutOfBoundsException("No block exists at index " + index + ".");
        }
        chain.get(index).tamperDataForDemo(replacementData);
    }

    public List<Block> getChain() {
        return Collections.unmodifiableList(chain);
    }

    public int getDifficulty() {
        return difficulty;
    }
}
