package controller;

import enums.Direction;
import enums.State;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import model.Player;
import model.breaks.*;
import view.Tetris;

import java.awt.Point;
import java.util.*;

public class GameBoard {
    private final int row = 20;
    private final int column = 15;
    private State state;
    private Color bgColor;
    private int level;
    private final int brickSize = 20;
    private final Tetris tetris;

    private Pane nextPane;
    private Pane fallingPane;
    private final Rectangle[][] rectangles = new Rectangle[column][row];

    private Point pieceOrigin;
    private final ArrayList<Integer> nextPieces = new ArrayList<>();
    private final ArrayList<Brick> bricks = new ArrayList<>();
    private Color[][] well;
    private Pane pane;
    private Brick brick;
    private Timeline timer;
    private volatile boolean isGameOver = false;
    private volatile boolean isProcessingCollision = false;
    private boolean gameOverPopupShown = false;

    private Label lblScore, lblHeart, lblLevel;

    public GameBoard(int level, Tetris tetris) {
        this.level = level;
        this.tetris = tetris;
        this.bgColor = tetris.getSettingsManager().getBgColor();

        pieceOrigin = new Point(5, 2);
        bricks.add(new Brick_0(this, pieceOrigin));
        bricks.add(new Brick_I(this, pieceOrigin));
        bricks.add(new Brick_J(this, pieceOrigin));
        bricks.add(new Brick_L(this, pieceOrigin));
        bricks.add(new Brick_L3(this, pieceOrigin));
        bricks.add(new Brick_PI(this, pieceOrigin));
        bricks.add(new Brick_S(this, pieceOrigin));
        bricks.add(new Brick_T(this, pieceOrigin));
        bricks.add(new Brick_Z(this, pieceOrigin));
    }

    public void start() {
        if (isGameOver) return;
        if (state == State.START) return;
        if (state == State.STOP && brick != null) {
            state = State.START;
            startThread();
            return;
        }
        state = State.START;
        spawnPiece();
        if (!isGameOver) startThread();
    }

    private void spawnPiece() {
        pieceOrigin = new Point(5, 2);

        while (nextPieces.size() < 2) {
            ArrayList<Integer> bag = new ArrayList<>();
            for (int i = 0; i < bricks.size(); i++) bag.add(i);
            Collections.shuffle(bag);
            nextPieces.addAll(bag);
        }

        brick = bricks.get(nextPieces.remove(0));
        brick.setRotation(0);
        brick.setPoint(pieceOrigin);

        // Spawn-time collision check: if the new piece cannot be placed at all,
        // the game is truly over.
        brick.makePointsFromBooleansForCurrent();
        if (!brick.collidesAt(pieceOrigin.x, pieceOrigin.y)) {
            // Can't even place the new piece -> game over
            isGameOver = true;
            isProcessingCollision = false;
            setupGameOver();
            return;
        }

        Brick brickNext = bricks.get(nextPieces.get(0));
        brickNext.paint(nextPane, new Point(0, 0), true);
        repaint();
    }

    private void drawFallingBrick() {
        if (fallingPane == null) return;
        brick.paint(fallingPane, pieceOrigin, false);
    }

    private void startThread() {
        if (timer != null) timer.stop();
        timer = new Timeline(new KeyFrame(Duration.millis(1000.0 / Math.max(level, 1)), event -> {
            if (state == State.START && !isGameOver && !isProcessingCollision) moveDown(false);
        }));
        timer.setCycleCount(Timeline.INDEFINITE);
        timer.play();
    }

    public void stop() {
        state = State.STOP;
        if (timer != null) timer.stop();
    }

    public void paint(Pane gamePane) {
        this.pane = gamePane;
        this.bgColor = tetris.getSettingsManager().getBgColor();

        well = new Color[column][row];
        for (int i = 0; i < column; i++) {
            for (int j = 0; j < row; j++) {
                if (i == 0 || i == column - 1 || j == row - 1) {
                    well[i][j] = Color.web("#344b40");
                } else {
                    well[i][j] = null;
                }
            }
        }

        gamePane.setMinSize(brickSize * column, brickSize * row);
        gamePane.setMaxSize(brickSize * column, brickSize * row);
        if (fallingPane != null) {
            fallingPane.setMinSize(brickSize * column, brickSize * row);
            fallingPane.setMaxSize(brickSize * column, brickSize * row);
        }

        // FIX: clear previous content so restarts don't duplicate rectangles
        gamePane.getChildren().clear();

        Rectangle background = new Rectangle(brickSize * column, brickSize * row);
        background.setFill(Color.color(0, 0, 0, 0.8));
        gamePane.getChildren().add(background);

        addRects();
    }

    private void repaint() {
        for (int i = 0; i < column; i++) {
            for (int j = 0; j < row; j++) {
                if (rectangles[i][j] != null) {
                    rectangles[i][j].setFill(well[i][j] == null ? bgColor : well[i][j]);
                }
            }
        }
        drawFallingBrick();
        setLevelUp();
        refreshLabels();
    }

    private void setLevelUp() {
        int lvl;
        int scorePerRow = tetris.getSettingsManager().getScorePerRow();
        int removedLines = scorePerRow > 0 ? tetris.getScore() / scorePerRow : 0;

        if (removedLines >= 40) lvl = 10;
        else if (removedLines >= 30) lvl = 9;
        else if (removedLines >= 25) lvl = 8;
        else if (removedLines >= 20) lvl = 7;
        else if (removedLines >= 15) lvl = 6;
        else if (removedLines >= 12) lvl = 5;
        else if (removedLines >= 9) lvl = 4;
        else if (removedLines >= 6) lvl = 3;
        else if (removedLines >= 3) lvl = 2;
        else return;

        lvl = lvl + tetris.getSettingsManager().getStartLevel() - 1;
        boolean changed = level != lvl;
        level = lvl;
        tetris.setLevel(lvl);
        if (changed && state == State.START) startThread();
    }

    private void addRects() {
        for (int i = 0; i < column; i++) {
            for (int j = 0; j < row; j++) {
                Rectangle rectangle = new Rectangle(
                        brickSize * i, brickSize * j, brickSize - 1, brickSize - 1);
                rectangle.setFill(well[i][j] == null ? bgColor : well[i][j]);
                rectangle.setArcWidth(3);
                rectangle.setArcHeight(3);
                pane.getChildren().add(rectangle);
                rectangles[i][j] = rectangle;
            }
        }
    }

    public Color[][] getWell() {
        return well;
    }

    public void setWell(Color[][] well) {
        this.well = well;
    }

    public boolean move(Direction direction, Point pieceOrigin) {
        if (isGameOver) return false;
        if (direction == Direction.LEFT) {
            if (brick.collidesAt(pieceOrigin.x - 1, pieceOrigin.y)) {
                pieceOrigin.x -= 1;
                return true;
            }
        } else if (direction == Direction.RIGHT) {
            if (brick.collidesAt(pieceOrigin.x + 1, pieceOrigin.y)) {
                pieceOrigin.x += 1;
                return true;
            }
        }
        return false;
    }

    public boolean moveDown(boolean isFromEnter) {
        if (isGameOver || state != State.START || brick == null) return false;
        if (isFromEnter) {
            while (brick.collidesAt(pieceOrigin.x, pieceOrigin.y + 1)) pieceOrigin.y++;
            fixToWell();
            return true;
        }
        if (brick.collidesAt(pieceOrigin.x, pieceOrigin.y + 1)) {
            pieceOrigin.y += 1;
            repaint();
            return true;
        } else {
            fixToWell();
            return false;
        }
    }

    private void fixToWell() {
        if (isGameOver || isProcessingCollision) return;
        isProcessingCollision = true;

        brick.freeze(well);
        isProcessingCollision = false;
        spawnPiece();
    }

    private void setupGameOver() {
        // Stop the timer immediately so nothing else runs while we show UI.
        if (timer != null) {
            timer.stop();
            timer = null;
        }
        state = State.STOP;

        if (gameOverPopupShown) {
            return;
        }
        gameOverPopupShown = true;

        if (tetris.getHeart() <= 1) {
            boolean saved = isHighScore();
            if (!saved) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Nice stacking");
                alert.setHeaderText(null);
                alert.setContentText("A good run! Your score: " + tetris.getScore() + ". Ready for another?");
                alert.showAndWait();
            }
            tetris.setHeart(3);
            tetris.setScore(0);
            refreshLabels();
            restart();
        } else {
            tetris.setHeart(tetris.getHeart() - 1);
            refreshLabels();
            restart();
        }
    }

    private void refreshLabels() {
        if (lblScore != null) lblScore.setText("Score: " + tetris.getScore());
        if (lblHeart != null) lblHeart.setText("Lives: " + tetris.getHeart());
        if (lblLevel != null) lblLevel.setText("Level: " + tetris.getLevel());
    }

    private void restart() {
        pieceOrigin = new Point(5, 2);
        isGameOver = false;
        isProcessingCollision = false;
        gameOverPopupShown = false;
        level = tetris.getSettingsManager().getStartLevel();
        tetris.setLevel(level);
        brick = null;
        nextPieces.clear();
        paint(pane);
        start();
    }

    private boolean isHighScore() {
        ScoreManager manager = tetris.getScoreManager();
        manager.load();
        ArrayList<Player> players = manager.getPlayers();

        if (players.size() >= 10) {
            players.sort(Comparator.comparing(Player::getScore).reversed());
            int tenth = players.get(9).getScore();
            if (tetris.getScore() <= tenth) return false;
        }
        return saveScore(manager);
    }

    private boolean saveScore(ScoreManager manager) {
        String name = showInputNameDialog();
        if (name.isEmpty()) return false;
        manager.addPlayer(name, tetris.getScore());
        manager.save();
        return true;
    }

    private String showInputNameDialog() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("New High Score");
        dialog.setHeaderText(" *** New High Score :" + tetris.getScore() + " ***");
        dialog.setContentText("Please enter your name:");
        Optional<String> result = dialog.showAndWait();
        return result.orElse("").trim();
    }

    public void sceneKeyListeners(KeyEvent event) {
        if (isGameOver || isProcessingCollision || state != State.START || brick == null) return;

        if (event.getCode() == KeyCode.UP) {
            if (brick.rotate(+1)) repaint();
        } else if (event.getCode() == KeyCode.DOWN) {
            moveDown(false);
        } else if (event.getCode() == KeyCode.Z) {
            if (brick.rotate(-1)) repaint();
        } else if (event.getCode() == KeyCode.LEFT) {
            if (move(Direction.LEFT, pieceOrigin)) repaint();
        } else if (event.getCode() == KeyCode.RIGHT) {
            if (move(Direction.RIGHT, pieceOrigin)) repaint();
        } else if (event.getCode() == KeyCode.ENTER || event.getCode() == KeyCode.SPACE) {
            if (moveDown(true)) repaint();
        }
    }

    public int getRow() {
        return row;
    }

    public int getBrickSize() {
        return brickSize;
    }

    public int getColumn() {
        return column;
    }

    public Color getBgColor() {
        return bgColor;
    }

    public Tetris getTetris() {
        return tetris;
    }

    public void setNextPane(Pane nextPane) {
        this.nextPane = nextPane;
    }

    public void setFallingPane(Pane fallingPane) {
        this.fallingPane = fallingPane;
    }

    public void setLblScore(Label lblScore) {
        this.lblScore = lblScore;
    }

    public void setLblHeart(Label lblHeart) {
        this.lblHeart = lblHeart;
    }

    public void setLblLevel(Label lblLevel) {
        this.lblLevel = lblLevel;
    }
}
