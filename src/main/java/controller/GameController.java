package controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import util.ChangeScene;
import util.StageProperty;
import view.Tetris;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class GameController implements Initializable {

    @FXML
    private Pane gamePane, paneNextBrick, paneFalling;
    @FXML
    private Button btnStart, btnBack;
    @FXML
    private Label lblLevel, lblScore, lblHeart;

    private Tetris tetris;
    private boolean isGameStarted = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        tetris = Tetris.tetris;
        tetris._init();

        // Refresh settings from disk so background-color changes take effect.
        tetris.getSettingsManager().load(false);

        GameBoard gameBoard = new GameBoard(tetris.getLevel(), tetris);
        tetris.setGameBoard(gameBoard);
        gameBoard.setNextPane(paneNextBrick);
        gameBoard.setFallingPane(paneFalling);
        gameBoard.paint(gamePane);

        btnStart.setOnAction(event -> {
            if (!isGameStarted && btnStart.getText().equals("Start")) {
                tetris.start();
                btnStart.setText("Stop");
                isGameStarted = true;
            } else {
                btnStart.setText("Start");
                tetris._stop();
                isGameStarted = false;
            }
            btnStart.setFocusTraversable(false);
            btnBack.setFocusTraversable(false);
            gamePane.requestFocus();
        });

        lblLevel.setText("level: " + tetris.getLevel());
        lblScore.setText("Score: " + tetris.getScore());
        lblHeart.setText("Heart: " + tetris.getHeart());

        gameBoard.setLblScore(lblScore);
        gameBoard.setLblHeart(lblHeart);
        gameBoard.setLblLevel(lblLevel);

        btnBack.setOnAction(event -> {
            try {
                ChangeScene.setScene(gamePane, StageProperty.MAIN_MENU_PATH, "Tetris");
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    @FXML
    private void sceneKeyListeners(KeyEvent event) {
        if (isGameStarted) {
            event.consume();
            tetris.getGameBoard().sceneKeyListeners(event);
        }
    }
}