package edu.college.blockchain;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Scanner;

/** Console application and entry point for the learning project. */
public class NoobChain {
    private static final int DEFAULT_DIFFICULTY = 3;
    private static final Path DEFAULT_DATA_FILE = Path.of("data", "blockchain.json");

    private NoobChain() {
    }

    public static void main(String[] args) {
        if (List.of(args).contains("--self-test")) {
            BlockchainSelfTest.run();
            return;
        }

        int difficulty = readIntegerOption(args, "--difficulty=", DEFAULT_DIFFICULTY);
        Path dataFile = readPathOption(args, "--data=", DEFAULT_DATA_FILE);
        if (difficulty < 0 || difficulty > 6) {
            System.err.println("Difficulty must be between 0 and 6.");
            return;
        }

        BlockchainManager manager = new BlockchainManager(difficulty);
        if (Files.exists(dataFile)) {
            try {
                manager.replaceChain(BlockchainJsonStore.load(dataFile));
                System.out.println("Loaded " + manager.getChain().size() + " block(s) from " + dataFile + ".");
                showValidation(manager);
            } catch (IOException | IllegalArgumentException exception) {
                System.out.println("Could not load saved chain: " + exception.getMessage());
                System.out.println("Starting with a new genesis block instead.");
            }
        } else {
            System.out.println("Started a new chain. Its genesis block has been mined.");
        }

        runMenu(manager, dataFile);
    }

    private static void runMenu(BlockchainManager manager, Path dataFile) {
        try (Scanner scanner = new Scanner(System.in)) {
            boolean running = true;
            while (running) {
                printMenu();
                System.out.print("Choose an option: ");
                String choice = scanner.nextLine().trim();
                try {
                    switch (choice) {
                        case "1" -> addBlock(scanner, manager, dataFile);
                        case "2" -> displayChain(manager);
                        case "3" -> showValidation(manager);
                        case "4" -> tamperWithBlock(scanner, manager, dataFile);
                        case "5" -> saveChain(manager, dataFile);
                        case "6" -> loadChain(manager, dataFile);
                        case "7" -> resetChain(manager, dataFile);
                        case "0" -> running = false;
                        default -> System.out.println("Please choose a number from 0 to 7.");
                    }
                } catch (IOException | IllegalArgumentException exception) {
                    System.out.println("Action failed: " + exception.getMessage());
                }
            }
        }

        try {
            BlockchainJsonStore.save(dataFile, manager.getChain());
            System.out.println("Saved chain to " + dataFile + ". Goodbye!");
        } catch (IOException exception) {
            System.out.println("Goodbye! The chain could not be saved: " + exception.getMessage());
        }
    }

    private static void printMenu() {
        System.out.println("\n=== Java Blockchain ===");
        System.out.println("1. Add and mine a block");
        System.out.println("2. Display the chain");
        System.out.println("3. Validate the chain");
        System.out.println("4. Demonstrate tampering");
        System.out.println("5. Save now");
        System.out.println("6. Reload saved chain");
        System.out.println("7. Start a new chain");
        System.out.println("0. Save and exit");
    }

    private static void addBlock(Scanner scanner, BlockchainManager manager, Path dataFile) throws IOException {
        System.out.print("Enter the record to store: ");
        String data = scanner.nextLine();
        long started = System.nanoTime();
        Block block = manager.addBlock(data);
        double seconds = (System.nanoTime() - started) / 1_000_000_000.0;
        System.out.println("Mined block " + block.getIndex() + " in " + String.format("%.3f", seconds)
                + " second(s). Hash: " + block.getHash());
        saveChain(manager, dataFile);
    }

    private static void displayChain(BlockchainManager manager) {
        for (Block block : manager.getChain()) {
            System.out.println("\nBlock " + block.getIndex());
            System.out.println("  Time:         " + block.getTimestamp());
            System.out.println("  Data:         " + block.getData());
            System.out.println("  Previous hash: " + block.getPreviousHash());
            System.out.println("  Hash:         " + block.getHash());
            System.out.println("  Nonce:        " + block.getNonce());
        }
    }

    private static void showValidation(BlockchainManager manager) {
        List<String> problems = BlockchainValidator.validate(manager.getChain(), manager.getDifficulty());
        if (problems.isEmpty()) {
            System.out.println("Chain is valid. Every block hash, link, and proof of work checks out.");
        } else {
            System.out.println("Chain is INVALID:");
            problems.forEach(problem -> System.out.println("  - " + problem));
        }
    }

    private static void tamperWithBlock(Scanner scanner, BlockchainManager manager, Path dataFile) throws IOException {
        displayChain(manager);
        System.out.print("Block index to change (0 to " + (manager.getChain().size() - 1) + "): ");
        int index = Integer.parseInt(scanner.nextLine().trim());
        System.out.print("Replacement data: ");
        String replacement = scanner.nextLine();
        manager.tamperBlockForDemo(index, replacement);
        System.out.println("The data changed but the old hash did not. Validation should now fail:");
        showValidation(manager);
        saveChain(manager, dataFile);
    }

    private static void saveChain(BlockchainManager manager, Path dataFile) throws IOException {
        BlockchainJsonStore.save(dataFile, manager.getChain());
        System.out.println("Saved " + manager.getChain().size() + " block(s) to " + dataFile + ".");
    }

    private static void loadChain(BlockchainManager manager, Path dataFile) throws IOException {
        manager.replaceChain(BlockchainJsonStore.load(dataFile));
        System.out.println("Reloaded " + manager.getChain().size() + " block(s) from " + dataFile + ".");
        showValidation(manager);
    }

    private static void resetChain(BlockchainManager manager, Path dataFile) throws IOException {
        manager.reset();
        System.out.println("Created and mined a new genesis block.");
        saveChain(manager, dataFile);
    }

    private static int readIntegerOption(String[] args, String prefix, int defaultValue) {
        for (String argument : args) {
            if (argument.startsWith(prefix)) {
                try {
                    return Integer.parseInt(argument.substring(prefix.length()));
                } catch (NumberFormatException ignored) {
                    return defaultValue;
                }
            }
        }
        return defaultValue;
    }

    private static Path readPathOption(String[] args, String prefix, Path defaultValue) {
        for (String argument : args) {
            if (argument.startsWith(prefix)) {
                return Path.of(argument.substring(prefix.length()));
            }
        }
        return defaultValue;
    }
}
