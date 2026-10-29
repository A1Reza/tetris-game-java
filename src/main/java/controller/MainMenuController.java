package controller;

import javafx.fxml.FXML;
import javafx.application.Platform;
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
    private Button btnNewGame, btnScoresTable, btnSettings, btnExit;
    @FXML
    private BorderPane pane;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        btnNewGame.setOnAction(e -> startNewGame());
        btnExit.setOnAction(e -> Platform.exit());
        btnSettings.setOnAction(e -> openWindow("/view/settings.fxml", "Settings", 360, 230));
        btnScoresTable.setOnAction(e -> openWindow("/view/scores_table.fxml", "Scores Table", 510, 300));
    }

    private void startNewGame() {
        try {
            ChangeScene.setScene(pane, StageProperty.NEW_GAME_PATH, "Tetris");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void openWindow(String fxmlPath, String title, int w, int h) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle(title);
            root.setStyle(StageProperty.STYLE_EFFECT);
            Scene scene = new Scene(root, w, h);
            scene.getStylesheets().add(
                    getClass().getResource(StageProperty.STYLE_PATH).toExternalForm());
            stage.setScene(scene);
            stage.setResizable(false);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
