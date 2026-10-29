package controller;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import model.Player;

import java.io.*;
import java.net.URL;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.ResourceBundle;

public class ScoreManager implements Initializable {

    private static final Path DATA_DIR =
            Paths.get(System.getProperty("user.home"), ".tetris");
    private static final Path SCORES_FILE = DATA_DIR.resolve("scores.txt");
    private static final Path TMP_FILE = DATA_DIR.resolve("scores.txt.tmp");

    private final ArrayList<Player> players = new ArrayList<>();

    public void addPlayer(String name, int score) {
        String safeName = name == null ? "" : name.replaceAll("[%\\r\\n]", " ").trim();
        if (safeName.isEmpty() || score < 0) return;
        players.add(new Player(safeName.substring(0, Math.min(safeName.length(), 40)), score));
        players.sort(Comparator.comparingInt(Player::getScore).reversed());
        if (players.size() > 10) players.subList(10, players.size()).clear();
    }

    public ArrayList<Player> getPlayers() {
        return players;
    }

    public void clear() {
        players.clear();
        save();
    }

    public void load() {
        players.clear();
        try {
            Files.createDirectories(DATA_DIR);
            if (!Files.exists(SCORES_FILE)) {
                System.out.println("[ScoreManager] no scores file yet at " + SCORES_FILE);
                return;
            }
            try (BufferedReader br = Files.newBufferedReader(SCORES_FILE)) {
                String line;
                while ((line = br.readLine()) != null) {
                    if (line.isBlank()) continue;
                    String[] parts = line.split("%", 2);
                    if (parts.length < 2) continue;
                    try {
                        players.add(new Player(parts[0], Integer.parseInt(parts[1].trim())));
                    } catch (NumberFormatException nfe) {
                        System.err.println("[ScoreManager] skipping bad line: " + line);
                    }
                }
            }
            System.out.println("[ScoreManager] loaded " + players.size()
                    + " entries from " + SCORES_FILE);
        } catch (Exception e) {
            System.err.println("[ScoreManager] load FAILED: " + e);
            e.printStackTrace();
        }
    }

    public void save() {
        try {
            Files.createDirectories(DATA_DIR);
            try (BufferedWriter bw = Files.newBufferedWriter(
                    TMP_FILE,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.WRITE)) {
                for (Player p : players) {
                    bw.write(p.getName() + "%" + p.getScore());
                    bw.newLine();
                }
            }
            try {
                Files.move(TMP_FILE, SCORES_FILE,
                        StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(TMP_FILE, SCORES_FILE, StandardCopyOption.REPLACE_EXISTING);
            }
            System.out.println("[ScoreManager] saved " + players.size()
                    + " entries to " + SCORES_FILE);
        } catch (IOException e) {
            System.err.println("[ScoreManager] save FAILED: " + e);
            e.printStackTrace();
        }
    }

    @FXML
    private TableView<Player> tableScores;
    @FXML
    private TableColumn<Player, String> colRank;
    @FXML
    private TableColumn<Player, String> colName;
    @FXML
    private TableColumn<Player, String> colScore;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        load();

        // 1. Name column
        colName.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getName()));

        // 2. Score column
        colScore.setCellValueFactory(cellData ->
                new SimpleStringProperty(String.valueOf(cellData.getValue().getScore())));
        colScore.setComparator(Comparator.comparingInt(Integer::parseInt));

        // 3. Rank column (calculated based on current row index)
        colRank.setCellValueFactory(cellData ->
                new ReadOnlyObjectWrapper<>(String.valueOf(
                        tableScores.getItems().indexOf(cellData.getValue()) + 1)));

        // Populate and sort
        ObservableList<Player> items = getPlayersObservable();
        tableScores.setItems(items);
        tableScores.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        // Let JavaFX handle the sorting cleanly
        colScore.setSortType(TableColumn.SortType.DESCENDING);
        tableScores.getSortOrder().add(colScore);

        // We don't need to disable sorting or call tableScores.sort() manually
        // because setItems() + SortOrder handles it automatically.

        tableScores.setPlaceholder(new Label("Start A Game To Add New Score!"));
        tableScores.setFixedCellSize(26);
    }

    private ObservableList<Player> getPlayersObservable() {
        // Sort descending by score
        players.sort(Comparator.comparingInt(Player::getScore).reversed());

        // Take top 10
        ObservableList<Player> list = FXCollections.observableArrayList();
        int count = Math.min(players.size(), 10);
        for (int i = 0; i < count; i++) {
            list.add(players.get(i));
        }
        return list;
    }
}
