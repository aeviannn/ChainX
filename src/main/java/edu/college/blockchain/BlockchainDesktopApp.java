package edu.college.blockchain;

import javafx.animation.ScaleTransition;
import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

/** JavaFX desktop interface for the existing educational blockchain backend. */
public class BlockchainDesktopApp extends Application {
    private static final String ORANGE = "#C66A32";
    private static final String BASE = "#101010";
    private static final String CARD = "#191919";
    private static final String SECONDARY = "#242424";
    private static final String MUTED = "#A3A3A3";
    private static final String TEXT = "#E6E1DA";
    private static final String BORDER = "#303030";
    private static final Object BUTTON_SCALE_TRANSITION = new Object();
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("MMM d, yyyy  ·  HH:mm:ss")
            .withZone(ZoneId.systemDefault());

    private BlockchainManager manager;
    private Path dataFile = Path.of("data", "blockchain.json");
    private BorderPane root;
    private VBox content;
    private Label statusLabel;
    private Label validityLabel;
    private String activePage = "Dashboard";
    private boolean mining;

    @Override
    public void start(Stage stage) {
        manager = new BlockchainManager(3);
        statusLabel = label("Ready.", 10, MUTED, false);
        if (Files.exists(dataFile)) {
            try {
                manager.replaceChain(BlockchainJsonStore.load(dataFile));
                statusText("Loaded " + manager.getChain().size() + " block(s) from " + dataFile + ".", false);
            } catch (IOException | IllegalArgumentException exception) {
                statusText("Saved chain could not be loaded: " + exception.getMessage(), true);
            }
        } else {
            statusText("Started a new chain. Save location: " + dataFile + ".", false);
        }

        root = new BorderPane();
        root.setStyle("-fx-background-color: " + BASE + ";");
        VBox main = new VBox(0);
        main.setStyle("-fx-background-color: " + BASE + ";");
        main.getChildren().addAll(createHeader(), createNavigation(), new Separator(), createContentHost(), createStatusBar());
        VBox.setVgrow(main.getChildren().get(3), Priority.ALWAYS);
        root.setCenter(main);
        showPage("Dashboard");

        Scene scene = new Scene(root, 1000, 700);
        stage.setTitle("CHAINEX  /  Blockchain Management System");
        stage.setMinWidth(700);
        stage.setMinHeight(500);
        stage.setScene(scene);
        stage.show();
    }

    private Node createHeader() {
        HBox header = new HBox(16);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(12, 20, 8, 20));
        VBox brand = new VBox(1, label("CHAINEX", 20, TEXT, true),
                label("BLOCKCHAIN MANAGEMENT SYSTEM", 9, MUTED, true));
        Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);
        validityLabel = pill("●  CHAIN VALID", "#302116", ORANGE);
        header.getChildren().addAll(brand, spacer, validityLabel);
        return header;
    }

    private Node createNavigation() {
        HBox nav = new HBox(8);
        nav.setAlignment(Pos.CENTER_LEFT);
        nav.setPadding(new Insets(0, 20, 8, 20));
        nav.getChildren().addAll(
                navButton("Dashboard", () -> showPage("Dashboard")),
                navButton("Explorer", () -> showPage("Blockchain Explorer")),
                navButton("Tampering Demo", () -> showPage("Tampering Demo")),
                navButton("Storage", () -> showPage("Storage")));
        return nav;
    }

    private Node createContentHost() {
        content = new VBox(11);
        content.setPadding(new Insets(14, 20, 18, 20));
        content.setFillWidth(true);
        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: " + BASE + "; -fx-background-color: " + BASE + "; -fx-border-color: transparent;");
        return scroll;
    }

    private Node createStatusBar() {
        HBox bar = new HBox(8, dot(ORANGE), statusLabel);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(7, 20, 7, 20));
        bar.setStyle("-fx-background-color: " + CARD + "; -fx-border-color: " + BORDER + "; -fx-border-width: 1 0 0 0;");
        return bar;
    }

    private Button navButton(String text, Runnable action) {
        Button button = new Button(text);
        button.setPadding(new Insets(6, 10, 6, 10));
        styleButton(button, false);
        button.setOnAction(event -> action.run());
        return button;
    }

    private void showPage(String page) {
        activePage = page;
        refreshValidityIndicator();
        content.getChildren().clear();
        switch (page) {
            case "Dashboard" -> dashboard();
            case "Blockchain Explorer" -> explorer();
            case "Validate Chain" -> validatePage();
            case "Tampering Demo" -> tamperPage();
            case "Storage" -> storagePage();
            default -> dashboard();
        }
    }

    private void dashboard() {
        List<Block> chain = manager.getChain();
        List<String> problems = BlockchainValidator.validate(manager.getChain(), manager.getDifficulty());
        FlowPane metrics = new FlowPane(10, 8);
        metrics.setMaxWidth(Double.MAX_VALUE);
        VBox total = metricCard("TOTAL BLOCKS", String.valueOf(chain.size()), "Blocks stored in this chain");
        VBox difficulty = metricCard("MINING DIFFICULTY", manager.getDifficulty() + " zeros", "Required leading zeroes");
        VBox status = metricCard("CHAIN STATUS", problems.isEmpty() ? "Valid" : "Issues found",
                problems.isEmpty() ? "All checks passed" : problems.size() + " validation issue(s)");
        String latest = chain.isEmpty() ? "—" : "#" + chain.get(chain.size() - 1).getIndex();
        VBox latestBlock = metricCard("LATEST BLOCK", latest, chain.isEmpty() ? "No blocks available" : shortData(chain.get(chain.size() - 1).getData()));
        metrics.getChildren().addAll(total, difficulty, status, latestBlock);
        content.getChildren().add(metrics);

        content.getChildren().add(label("BLOCKCHAIN EXPLORER", 12, TEXT, true));
        VBox blockList = new VBox(7);
        VBox details = detailCard();
        populateBlockList(blockList, details);
        content.getChildren().add(blockList);
        details.setVisible(false);
        details.setManaged(false);
        content.getChildren().add(details);

        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER_LEFT);
        Button add = primaryButton("Add Block");
        add.setOnAction(event -> showAddBlockDialog());
        Button validate = secondaryButton("Validate");
        validate.setOnAction(event -> showValidationDialog());
        Button save = secondaryButton("Save Chain");
        save.setOnAction(event -> saveCurrentChain());
        actions.getChildren().addAll(add, validate, save);
        content.getChildren().add(actions);
    }

    private void explorer() {
        content.getChildren().addAll(label("Blockchain Explorer", 20, TEXT, true),
                label(manager.getChain().size() + " blocks  ·  Select a block to view its details.", 12, MUTED, false));
        VBox blockList = new VBox(7);
        VBox details = detailCard();
        populateBlockList(blockList, details);
        details.setVisible(false);
        details.setManaged(false);
        content.getChildren().addAll(blockList, details);
    }

    private void populateBlockList(VBox blockList, VBox details) {
        List<Block> chain = manager.getChain();
        for (int i = chain.size() - 1; i >= 0; i--) {
            Block block = chain.get(i);
            blockList.getChildren().add(blockRow(block, details));
        }
        if (chain.isEmpty()) blockList.getChildren().add(label("No blocks are available.", 12, MUTED, false));
    }

    private HBox blockRow(Block block, VBox details) {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(11, 14, 11, 14));
        row.setStyle("-fx-background-color: " + CARD + "; -fx-background-radius: 7; -fx-border-color: " + BORDER + "; -fx-border-radius: 7; -fx-cursor: hand;");
        Label index = pill("#" + block.getIndex(), "#302116", ORANGE);
        VBox info = new VBox(4,
                label(block.getData().replace('\n', ' '), 12, TEXT, true),
                label("Hash  " + shortHash(block.getHash()), 10, MUTED, false));
        HBox.setHgrow(info, Priority.ALWAYS);
        Label miningStatus = pill(isBlockMined(block) ? "MINED" : "INVALID",
                isBlockMined(block) ? "#202820" : "#302116",
                isBlockMined(block) ? "#9BCC9B" : ORANGE);
        row.getChildren().addAll(index, info, miningStatus);
        row.setOnMouseClicked(event -> showBlockDetails(block, details));
        row.setOnMouseEntered(event -> row.setStyle("-fx-background-color: " + SECONDARY + "; -fx-background-radius: 7; -fx-border-color: " + BORDER + "; -fx-border-radius: 7; -fx-cursor: hand;"));
        row.setOnMouseExited(event -> row.setStyle("-fx-background-color: " + CARD + "; -fx-background-radius: 7; -fx-border-color: " + BORDER + "; -fx-border-radius: 7; -fx-cursor: hand;"));
        return row;
    }

    private VBox detailCard() {
        VBox details = cardBox();
        details.setPadding(new Insets(14));
        return details;
    }

    private void showBlockDetails(Block block, VBox details) {
        details.setVisible(true);
        details.setManaged(true);
        details.getChildren().setAll(
                label("BLOCK #" + block.getIndex(), 12, ORANGE, true),
                detailLine("Data", block.getData()),
                detailLine("Timestamp", DATE.format(Instant.ofEpochMilli(block.getTimestamp()))),
                detailLine("Mining status", isBlockMined(block) ? "Mined successfully" : "Hash or proof of work does not match"),
                detailLine("Nonce", String.valueOf(block.getNonce())),
                detailLine("Previous hash", block.getPreviousHash()),
                detailLine("Hash", block.getHash()));
    }

    private HBox detailLine(String title, String value) {
        HBox line = new HBox(12, label(title, 10, MUTED, true), label(value, 11, TEXT, false));
        line.setAlignment(Pos.TOP_LEFT);
        return line;
    }

    private boolean isBlockMined(Block block) {
        return block.getHash().equals(block.calculateHash())
                && block.getHash().startsWith("0".repeat(manager.getDifficulty()));
    }

    private void showAddBlockDialog() {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Add Block");
        dialog.setHeaderText("Enter the information to store in a new block.");
        dialog.initOwner(root.getScene().getWindow());
        TextArea data = new TextArea();
        data.setPromptText("Type a record");
        data.setWrapText(true);
        data.setPrefRowCount(4);
        data.setPrefColumnCount(38);
        data.setStyle(inputStyle());
        ButtonType mine = new ButtonType("Add and Mine", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(mine, ButtonType.CANCEL);
        dialog.getDialogPane().setContent(data);
        styleDialog(dialog);
        dialog.setResultConverter(button -> button == mine ? data.getText() : null);
        dialog.showAndWait().ifPresent(this::mineBlock);
    }

    private void mineBlock(String record) {
        if (record == null || record.isBlank()) {
            statusText("Enter some block data first.", true);
            return;
        }
        if (mining) {
            statusText("Mining is already in progress.", true);
            return;
        }
        mining = true;
        statusText("Mining block… The interface remains available.", false);
        Task<Block> task = new Task<>() {
            @Override
            protected Block call() {
                return manager.addBlock(record);
            }
        };
        task.setOnSucceeded(event -> {
            mining = false;
            Block block = task.getValue();
            boolean saved = saveQuietly();
            showPage(activePage);
            statusText(saved
                    ? "Block #" + block.getIndex() + " added, mined and saved."
                    : "Block #" + block.getIndex() + " was mined, but could not be saved.", !saved);
        });
        task.setOnFailed(event -> {
            mining = false;
            showPage(activePage);
            statusText("Could not mine block: " + task.getException().getMessage(), true);
        });
        Thread thread = new Thread(task, "block-miner");
        thread.setDaemon(true);
        thread.start();
    }

    private void saveCurrentChain() {
        try {
            BlockchainJsonStore.save(dataFile, manager.getChain());
            statusText("Saved " + manager.getChain().size() + " block(s) to " + dataFile.toAbsolutePath() + ".", false);
        } catch (IOException | IllegalArgumentException exception) {
            statusText("Could not save the chain: " + exception.getMessage(), true);
        }
    }

    private void showValidationDialog() {
        List<String> problems = BlockchainValidator.validate(manager.getChain(), manager.getDifficulty());
        Alert alert = new Alert(problems.isEmpty() ? Alert.AlertType.INFORMATION : Alert.AlertType.WARNING);
        alert.initOwner(root.getScene().getWindow());
        alert.setTitle("Blockchain Validation");
        alert.setHeaderText(problems.isEmpty() ? "The blockchain is valid." : "Validation found " + problems.size() + " issue(s).");
        alert.setContentText(problems.isEmpty()
                ? "All block hashes, links, indexes and mining requirements passed."
                : String.join("\n", problems));
        styleDialog(alert);
        statusText(problems.isEmpty() ? "Validation passed." : "Validation found " + problems.size() + " issue(s).", !problems.isEmpty());
        alert.showAndWait();
    }

    private void styleDialog(Dialog<?> dialog) {
        DialogPane pane = dialog.getDialogPane();
        pane.setStyle("-fx-background-color: " + CARD + "; -fx-border-color: " + BORDER + ";");
        for (ButtonType type : pane.getButtonTypes()) {
            Node button = pane.lookupButton(type);
            if (button instanceof Button control) styleButton(control, type.getButtonData() == ButtonBar.ButtonData.OK_DONE);
        }
        dialog.setOnShown(event -> {
            Node header = pane.lookup(".header-panel");
            if (header != null) header.setStyle("-fx-background-color: " + CARD + "; -fx-border-color: " + BORDER + ";");
            for (Node node : pane.lookupAll(".label")) node.setStyle("-fx-text-fill: " + TEXT + ";");
        });
    }

    private void refreshValidityIndicator() {
        if (validityLabel == null) return;
        boolean valid = BlockchainValidator.validate(manager.getChain(), manager.getDifficulty()).isEmpty();
        validityLabel.setText(valid ? "●  CHAIN VALID" : "●  VALIDATION ISSUES");
        validityLabel.setStyle("-fx-background-color: " + (valid ? "#202820" : "#302116")
                + "; -fx-background-radius: 20; -fx-text-fill: " + (valid ? "#9BCC9B" : ORANGE)
                + "; -fx-font-size: 9px; -fx-font-weight: bold; -fx-padding: 6 9 6 9;");
    }

    private void validatePage() {
        List<String> problems = BlockchainValidator.validate(manager.getChain(), manager.getDifficulty());
        VBox result = cardBox();
        result.setPadding(new Insets(14));
        result.getChildren().add(label(problems.isEmpty() ? "VALID" : "INVALID",
                16, problems.isEmpty() ? "#9BCC9B" : ORANGE, true));
        if (!problems.isEmpty()) {
            for (String problem : problems) result.getChildren().add(label("•  " + problem, 12, TEXT, false));
        } else {
            result.getChildren().add(label("All blockchain checks passed.", 11, MUTED, false));
        }
        content.getChildren().add(result);
    }

    private void tamperPage() {
        content.getChildren().add(label("Tampering Demo", 18, TEXT, true));
        VBox form = cardBox();
        form.setPadding(new Insets(14));
        form.setMaxWidth(600);
        ComboBox<Integer> index = new ComboBox<>();
        for (Block block : manager.getChain()) index.getItems().add((int) block.getIndex());
        if (!index.getItems().isEmpty()) index.getSelectionModel().select(index.getItems().size() - 1);
        index.setPromptText("Select a block");
        index.setStyle(inputStyle());
        TextField replacement = new TextField();
        replacement.setPromptText("Replacement data");
        replacement.setStyle(inputStyle());
        Button tamper = primaryButton("Change Block");
        Label result = label("No changes made yet.", 11, MUTED, false);
        VBox outcome = new VBox(4);
        outcome.setVisible(false);
        outcome.setManaged(false);
        tamper.setOnAction(e -> {
            if (index.getValue() == null || replacement.getText().isBlank()) { statusText("Choose a block and enter replacement data.", true); return; }
            try {
                manager.tamperBlockForDemo(index.getValue(), replacement.getText());
                refreshValidityIndicator();
                boolean saved = saveQuietly();
                result.setText(saved ? "Block #" + index.getValue() + " changed and saved."
                        : "Block #" + index.getValue() + " changed, but could not be saved.");
                List<String> problems = BlockchainValidator.validate(manager.getChain(), manager.getDifficulty());
                outcome.getChildren().setAll(label(problems.isEmpty() ? "VALID" : "INVALID",
                        13, problems.isEmpty() ? "#9BCC9B" : ORANGE, true));
                for (String issue : problems) outcome.getChildren().add(label("•  " + issue, 11, TEXT, false));
                outcome.setVisible(true);
                outcome.setManaged(true);
                if (saved) statusText("Block changed and saved. The chain is now invalid.", false);
            } catch (RuntimeException exception) { statusText(exception.getMessage(), true); }
        });
        form.getChildren().addAll(index, replacement, tamper, result, outcome);
        content.getChildren().add(form);
    }

    private void storagePage() {
        content.getChildren().add(label("Storage", 18, TEXT, true));
        VBox fileCard = cardBox();
        fileCard.setPadding(new Insets(14));
        TextField path = new TextField(dataFile.toString());
        path.setStyle(inputStyle());
        HBox pathRow = new HBox(8);
        HBox.setHgrow(path, Priority.ALWAYS);
        Button browse = secondaryButton("Browse…");
        browse.setOnAction(e -> { FileChooser chooser = new FileChooser(); chooser.setTitle("Choose blockchain JSON file"); chooser.setInitialFileName(dataFile.getFileName().toString()); java.io.File selected = chooser.showSaveDialog(root.getScene().getWindow()); if (selected != null) path.setText(selected.toPath().toString()); });
        pathRow.getChildren().addAll(path, browse);
        Label state = label(storageSummary(), 11, MUTED, false);
        Button save = primaryButton("Save");
        save.setOnAction(e -> {
            try {
                dataFile = Path.of(path.getText().trim());
                BlockchainJsonStore.save(dataFile, manager.getChain());
                state.setText(storageSummary());
                statusText("Saved " + manager.getChain().size() + " block(s).", false);
            } catch (IOException | IllegalArgumentException ex) {
                statusText("Save failed: " + ex.getMessage(), true);
            }
        });
        Button load = secondaryButton("Load");
        load.setOnAction(e -> {
            try {
                dataFile = Path.of(path.getText().trim());
                List<Block> blocks = BlockchainJsonStore.load(dataFile);
                manager.replaceChain(blocks);
                statusText("Loaded " + blocks.size() + " block(s)"
                        + (BlockchainValidator.validate(manager.getChain(), manager.getDifficulty()).isEmpty()
                        ? " · valid." : " · validation issues found."), false);
                showPage("Storage");
            } catch (IOException | IllegalArgumentException ex) {
                statusText("Load failed: " + ex.getMessage(), true);
            }
        });
        Button reset = secondaryButton("Reset Chain");
        reset.setOnAction(e -> {
            manager.reset();
            boolean saved = saveQuietly();
            showPage("Dashboard");
            if (saved) statusText("New chain created and saved.", false);
        });
        HBox controls = new HBox(8, save, load, reset);
        fileCard.getChildren().addAll(label("CHAIN FILE", 10, MUTED, true), pathRow, state, controls);
        content.getChildren().add(fileCard);
    }

    private boolean saveQuietly() {
        try {
            BlockchainJsonStore.save(dataFile, manager.getChain());
            return true;
        } catch (IOException | IllegalArgumentException exception) {
            statusText("Chain changed but could not be saved to " + dataFile + ": " + exception.getMessage(), true);
            return false;
        }
    }

    private String storageSummary() {
        List<String> problems = BlockchainValidator.validate(manager.getChain(), manager.getDifficulty());
        return manager.getChain().size() + " block(s)  ·  "
                + (problems.isEmpty() ? "valid chain" : problems.size() + " validation issue(s)");
    }

    private VBox metricCard(String title, String value, String note) {
        VBox box = cardBox();
        box.setPadding(new Insets(11, 13, 11, 13));
        box.setPrefWidth(220);
        box.setMinHeight(88);
        box.getChildren().addAll(label(title, 10, MUTED, true),
                label(value, 21, TEXT, true),
                label(note, 9, MUTED, false));
        return box;
    }

    private String shortData(String data) {
        String singleLine = data.replace('\n', ' ').trim();
        return singleLine.length() > 42 ? singleLine.substring(0, 42) + "…" : singleLine;
    }

    private VBox cardBox() {
        VBox box = new VBox(9);
        box.setStyle("-fx-background-color: " + CARD + "; -fx-background-radius: 8; -fx-border-color: " + BORDER + "; -fx-border-radius: 8;");
        return box;
    }

    private Label label(String text, int size, String color, boolean bold) {
        Label label = new Label(text);
        label.setWrapText(true);
        label.setStyle("-fx-font-size: " + size + "px; -fx-text-fill: " + color + "; "
                + (bold ? "-fx-font-weight: bold;" : ""));
        return label;
    }

    private Label dot(String color) { Label dot = new Label("●"); dot.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 9px;"); return dot; }
    private Label pill(String text, String background, String foreground) { Label l = label(text, 9, foreground, true); l.setPadding(new Insets(6, 9, 6, 9)); l.setStyle("-fx-background-color: " + background + "; -fx-background-radius: 20; -fx-text-fill: " + foreground + "; -fx-font-size: 9px; -fx-font-weight: bold;"); return l; }
    private Button primaryButton(String text) { Button b = new Button(text); b.setPadding(new Insets(10, 16, 10, 16)); styleButton(b, true); return b; }
    private Button secondaryButton(String text) { Button b = new Button(text); b.setPadding(new Insets(9, 14, 9, 14)); styleButton(b, false); return b; }
    private String inputStyle() { return "-fx-control-inner-background: " + BASE + "; -fx-text-fill: " + TEXT + "; -fx-prompt-text-fill: " + MUTED + "; -fx-background-color: " + BASE + "; -fx-border-color: " + BORDER + "; -fx-border-radius: 6; -fx-background-radius: 6;"; }
    private String shortHash(String hash) { return hash.length() > 18 ? hash.substring(0, 18) + "…" : hash; }

    private void styleButton(Button button, boolean primary) {
        String normalBackground = primary ? ORANGE : SECONDARY;
        String normalForeground = primary ? BASE : TEXT;
        String normal = buttonStyle(normalBackground, normalForeground, BORDER, primary, false);
        String hover = buttonStyle(primary ? "#D17645" : "#302A26", normalForeground, ORANGE, primary, true);
        String pressed = buttonStyle(primary ? "#A95227" : "#1B1B1B", TEXT, ORANGE, primary, true);
        String focused = buttonStyle(normalBackground, normalForeground, ORANGE, primary, true);
        button.setStyle(normal);
        button.setOnMouseEntered(event -> {
            button.setStyle(button.isFocused() ? focused : hover);
            animateButtonScale(button, 1.01);
        });
        button.setOnMouseExited(event -> {
            if (!button.isArmed()) button.setStyle(button.isFocused() ? focused : normal);
            animateButtonScale(button, 1.0);
        });
        button.setOnMousePressed(event -> {
            button.setStyle(pressed);
            animateButtonScale(button, 0.985);
        });
        button.setOnMouseReleased(event -> {
            button.setStyle(button.isFocused() ? focused : button.isHover() ? hover : normal);
            animateButtonScale(button, button.isHover() ? 1.01 : 1.0);
        });
        button.focusedProperty().addListener((observable, wasFocused, focusedNow) -> {
            if (!button.isArmed()) button.setStyle(focusedNow ? focused : button.isHover() ? hover : normal);
        });
    }

    private String buttonStyle(String background, String foreground, String border, boolean primary, boolean accented) {
        String borderStyle = primary
                ? "-fx-border-color: transparent; -fx-border-width: 0;"
                : "-fx-border-color: " + (accented ? ORANGE : border) + "; -fx-border-width: 1;";
        String effect = accented ? "-fx-effect: dropshadow(gaussian, " + ORANGE + ", 3, 0.18, 0, 0);" : "";
        return "-fx-background-color: " + background + "; -fx-text-fill: " + foreground + "; "
                + "-fx-background-radius: 6; -fx-border-radius: 6; " + borderStyle
                + (primary ? "-fx-font-weight: bold;" : "") + "-fx-cursor: hand;" + effect;
    }

    private void animateButtonScale(Button button, double scale) {
        Object current = button.getProperties().get(BUTTON_SCALE_TRANSITION);
        if (current instanceof ScaleTransition transition) transition.stop();
        ScaleTransition transition = new ScaleTransition(Duration.millis(110), button);
        transition.setToX(scale);
        transition.setToY(scale);
        button.getProperties().put(BUTTON_SCALE_TRANSITION, transition);
        transition.play();
    }

    private void statusText(String text, boolean error) {
        statusLabel.setText(text);
        statusLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: " + (error ? "#E48A64" : "#C6C2BD") + ";");
    }

    @Override
    public void stop() { if (mining) System.out.println("Mining continues only while the desktop application is open."); }

    public static void main(String[] args) { launch(args); }
}
