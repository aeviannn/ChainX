package edu.college.blockchain;

import java.util.ArrayList;
import java.util.List;

/** Checks the links, stored hashes, and proof of work for every block. */
public final class BlockchainValidator {
    public static final String GENESIS_PREVIOUS_HASH = "0";

    private BlockchainValidator() {
    }

    /** Returns an empty list when valid, otherwise readable reasons for each problem. */
    public static List<String> validate(List<Block> chain, int difficulty) {
        List<String> problems = new ArrayList<>();
        if (chain == null || chain.isEmpty()) {
            problems.add("The chain has no blocks.");
            return problems;
        }
        if (difficulty < 0 || difficulty > 6) {
            problems.add("Difficulty must be between 0 and 6.");
            return problems;
        }

        String requiredPrefix = "0".repeat(difficulty);
        for (int i = 0; i < chain.size(); i++) {
            Block current = chain.get(i);
            if (current == null) {
                problems.add("Block " + i + " is missing.");
                continue;
            }

            if (current.getIndex() != i) {
                problems.add("Block " + i + " has an unexpected index.");
            }
            if (!current.getHash().equals(current.calculateHash())) {
                problems.add("Block " + i + " data or hash was changed.");
            }
            if (!current.getHash().startsWith(requiredPrefix)) {
                problems.add("Block " + i + " does not meet the proof-of-work difficulty.");
            }

            String expectedPreviousHash;
            if (i == 0) {
                expectedPreviousHash = GENESIS_PREVIOUS_HASH;
            } else if (chain.get(i - 1) == null) {
                problems.add("Block " + i + " cannot link because the previous block is missing.");
                continue;
            } else {
                expectedPreviousHash = chain.get(i - 1).getHash();
            }
            if (!current.getPreviousHash().equals(expectedPreviousHash)) {
                problems.add("Block " + i + " does not link to the previous block.");
            }
        }
        return problems;
    }
}
