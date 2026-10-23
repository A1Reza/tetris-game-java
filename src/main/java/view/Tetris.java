package view;

import controller.GameBoard;
import controller.ScoreManager;
import controller.SettingsManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import util.StageProperty;

public class Tetris extends Application {

    private SettingsManager settingsManager;
    private GameBoard gameBoard;
    private ScoreManager scoreManager;
    private int level;
    private int heart = 3;
    private int score;

    public static Tetris tetris;

    @Override
    public void start(Stage primaryStage) throws Exception {
        tetris = this;

        FXMLLoader loader = new FXMLLoader(getClass().getResource(StageProperty.MAIN_MENU_PATH));
        Parent root = loader.load();
        root.setStyle(StageProperty.STYLE_EFFECT);

        Scene scene = new Scene(root, StageProperty.WIDTH, StageProperty.HEIGHT);
        scene.getStylesheets().add(
                Tetris.class.getResource(StageProperty.STYLE_PATH).toExternalForm());

        primaryStage.setTitle("Tetris");
        primaryStage.setResizable(false);
        primaryStage.setScene(scene);
        primaryStage.show();

        _init();
    }

    public void _init() {
        settingsManager = new SettingsManager();
        setLevel(settingsManager.getStartLevel());
        scoreManager = new ScoreManager();
    }

    @Override
    public void stop() {
        // Do NOT call System.exit(0) here. Let JavaFX shut down cleanly.
    }

    public GameBoard getGameBoard() {
        return gameBoard;
    }

    public void setGameBoard(GameBoard gameBoard) {
        this.gameBoard = gameBoard;
    }

    public void save() {
        settingsManager.save();
        scoreManager.save();
    }

    public void load() {
        settingsManager.load(false);
        scoreManager.load();
    }

    public void start() {
        gameBoard.start();
    }

    public void _stop() {
        gameBoard.stop();
    }

    public static void main(String[] args) {
        launch(args);
    }

    public SettingsManager getSettingsManager() {
        return settingsManager;
    }

    public void setSettingsManager(SettingsManager settingsManager) {
        this.settingsManager = settingsManager;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getHeart() {
        return heart;
    }

    public void setHeart(int heart) {
        this.heart = heart;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public ScoreManager getScoreManager() {
        return scoreManager;
    }
}