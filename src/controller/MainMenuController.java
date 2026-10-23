package controller;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import util.ChangeScene;
import util.StageProperty;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;


public class MainMenuController implements Initializable {
    @FXML
    Button btnNewGame, btnScoresTable, btnSettings, btnExit;

    @FXML
    BorderPane pane;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        btnNewGame.setOnAction(event -> startNewGame());
        btnExit.setOnAction(event -> System.exit(0));
        btnSettings.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                Parent root;
                try {
                    root = FXMLLoader.load(getClass().getClassLoader().getResource
                                    ("view/settings.fxml")
                            , resources);
                    Stage stage = new Stage();
                    stage.setTitle("Settings");
                    root.setStyle(StageProperty.STYLE_EFFECT);
                    Scene scene = new Scene(root, 360, 230);
                    scene.getStylesheets().add(getClass().getResource(StageProperty.STYLE_PATH).toExternalForm());
                    stage.setScene(scene);
                    stage.setResizable(false);
                    stage.show();
                    // Hide this current window (if this is what you want)
//                    ((Node)(event.getSource())).getScene().getWindow().hide();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        btnScoresTable.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                Parent root;
                try {
                    root = FXMLLoader.load(getClass().getClassLoader().getResource
                                    ("view/scores_table.fxml")
                            , resources);
                    Stage stage = new Stage();
                    stage.setTitle("Scores Table");
//                    root.setStyle(StageProperty.STYLE_EFFECT);
                    Scene scene = new Scene(root, 510, 300);
                    scene.getStylesheets().add(getClass().getResource(StageProperty.STYLE_PATH).toExternalForm());
                    stage.setScene(scene);
                    stage.setResizable(false);
                    stage.show();
                    // Hide this current window (if this is what you want)
//                    ((Node)(event.getSource())).getScene().getWindow().hide();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void startNewGame() {
        try {
            ChangeScene.setScene(pane, StageProperty.NEW_GAME_PATH, "Tetris");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}