package controller;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import model.Player;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.ResourceBundle;


public class ScoreManager implements Initializable {
    private final ArrayList<Player> players = new ArrayList<>();

    public void addPlayer(String name, int score) {
        players.add(new Player(name, score));
    }


    public ArrayList<Player> getPlayers() {
        return players;
    }

    public void clear() {
        players.clear();
        save();
    }

    public void load() {
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader("scores.txt"));
            while (bufferedReader.ready()) {
                String[] split = bufferedReader.readLine().split("%");
                String name = split[0];
                String score = split[1];
                players.add(new Player(name, Integer.parseInt(score)));
            }

        } catch (IOException e) {
            new File("scores.txt");
        }
    }

    public void save() {
        try {
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter("scores.txt"));
            for (Player player : players) {
                bufferedWriter.append(player.getName()).
                        append("%").
                        append(String.valueOf(player.getScore()));
                bufferedWriter.newLine();
            }
            bufferedWriter.flush();
            bufferedWriter.close();
        } catch (IOException e) {
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
//        colRank.setCellValueFactory(new PropertyValueFactory<>("rank"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colScore.setCellValueFactory(new PropertyValueFactory<>("score"));

        tableScores.setItems(getPlayersObservable());

        tableScores.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);


        //Set rank column
        colRank.setCellValueFactory(p -> new ReadOnlyObjectWrapper<>(
                tableScores.getItems().indexOf(p.getValue()) + 1 + ""));

        //sort by score desc
        colScore.setSortType(TableColumn.SortType.DESCENDING);
        tableScores.getSortOrder().add(colScore);
        tableScores.sort();
        colScore.setSortable(false);

        tableScores.setPlaceholder(new Label("Start A Game To Add New Score!"));
        tableScores.setFixedCellSize(26);
//        tableScores.prefHeightProperty()
//                .bind(Bindings.size(tableScores.getItems()).multiply(tableScores.getFixedCellSize()).
//                        add(20));
    }

    private ObservableList<Player> getPlayersObservable() {
        ObservableList<Player> list = FXCollections.observableArrayList();
        players.sort(Comparator.comparing(Player::getScore).reversed());
        if (players.size() > 10) {
            for (int i = 0; i < 10; i++) {
                list.add(players.get(i));
            }
        } else {
            list.addAll(players);
        }
        return list;
    }

}
