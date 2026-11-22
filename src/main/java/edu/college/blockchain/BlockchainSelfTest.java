package edu.college.blockchain;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/** A dependency-free smoke test that can be run with --self-test. */
public final class BlockchainSelfTest {
    private BlockchainSelfTest() {
    }

    public static void run() {
        Path temporaryFile = null;
        try {
            check(StringUtil.applySha256("abc").equals(
                    "ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad"),
                    "SHA-256 returns the expected digest.");

            BlockchainManager manager = new BlockchainManager(2);
            manager.addBlock("First test record\nwith a newline and \"quotes\".");
            check(BlockchainValidator.validate(manager.getChain(), 2).isEmpty(),
                    "A newly mined chain validates.");

            temporaryFile = Files.createTempFile("java-blockchain-self-test", ".json");
            BlockchainJsonStore.save(temporaryFile, manager.getChain());
            BlockchainManager restored = new BlockchainManager(2);
            restored.replaceChain(BlockchainJsonStore.load(temporaryFile));
            check(BlockchainValidator.validate(restored.getChain(), 2).isEmpty(),
                    "A chain survives JSON save and load.");

            restored.tamperBlockForDemo(1, "Changed record");
            check(!BlockchainValidator.validate(restored.getChain(), 2).isEmpty(),
                    "Changing old data is detected.");

            System.out.println("All self-checks passed: SHA-256, mining, validation, JSON persistence, tamper detection.");
        } catch (IOException exception) {
            throw new IllegalStateException("Self-check could not use a temporary JSON file.", exception);
        } finally {
            if (temporaryFile != null) {
                try {
                    Files.deleteIfExists(temporaryFile);
                } catch (IOException ignored) {
                    // The operating system will clean its temporary directory later.
                }
            }
        }
    }

    private static void check(boolean condition, String description) {
        if (!condition) {
            throw new AssertionError("Self-check failed: " + description);
        }
    }
}
