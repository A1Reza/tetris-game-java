package controller;

import enums.Direction;
import enums.State;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputDialog;
import javafx.scene.effect.Lighting;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import model.Player;
import model.breaks.*;
import view.Tetris;

import java.awt.*;
import java.util.*;


public class GameBoard {
    private final int row = 20;
    private final int column = 15;
    private final boolean[][] isFilled = new boolean[row][column];
    private State state;
    private final Color bgColor;
    private int level;
    private final int brickSize = 20;
    private final Tetris tetris;
    private Pane nextPane;
    Rectangle[][] rectangles = new Rectangle[column][row];

    public GameBoard(int level, Tetris tetris) {
        this.level = level;
        this.tetris = tetris;
        bgColor = tetris.getSettingsManager().getBgColor();

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
        state = State.START;
        pieceOrigin = new Point(5, 2);
        if (nextPieces.size() <= 1) {
            Collections.addAll(nextPieces, 0, 1, 2, 3, 4, 5, 6, 7, 8);
            Collections.shuffle(nextPieces);
        }
        brick = bricks.get(nextPieces.get(0));
        brick.setRotation(0);
        brick.paint(pane, pieceOrigin, false);
        nextPieces.remove(0);
        //next brick
        Brick brickNext = bricks.get(nextPieces.get(0));
        brickNext.setRotation(0);
        brickNext.paint(nextPane, new Point(0, 0), true);

        startThread();
    }

    Timer timer;

    private void startThread() {
        int period = 1000 / level;
        timer = new Timer();
        class Timer extends TimerTask {
            public void run() {
                if (isGameOver) {
                    timer.cancel();
                    return;
                }
                Platform.runLater(() -> {
                    if (moveDown(false)) {
                        if (state == State.STOP)
                            timer.cancel();
//                        Platform.runLater(GameBoard.this::repaint);
                        repaint();
                    } else {
                        timer.cancel();
                    }
                });

            }
        }
        timer.schedule(new Timer(), 1000, period);
    }

    public void stop() {
        state = State.STOP;
    }


//    public boolean isFilled(int x, int y) {
//        return true;
//    }
//
//    public void fill(int x, int y) {
//
//    }

    private Point pieceOrigin;
    private final ArrayList<Integer> nextPieces = new ArrayList<>();
    private final ArrayList<Brick> bricks = new ArrayList<>();
    private Color[][] well;

    private Pane pane;

    public void paint(Pane gamePane) {
        this.pane = gamePane;
        well = new Color[column][row];
        for (int i = 0; i < column; i++) {
            for (int j = 0; j < row; j++) {
                if (i == 0 || i == column - 1 || j == row - 1) {
                    well[i][j] = Color.GRAY;
                } else {
                    well[i][j] = bgColor;
                }
            }
        }

        gamePane.setMinSize(brickSize * column, brickSize * row);
        gamePane.setMaxSize(brickSize * column, brickSize * row);
        Rectangle rectangle = new Rectangle(brickSize * column, brickSize * row);
        rectangle.setFill(Color.color(0, 0, 0, 0.8));
//        rectangle.setFill(manager.getBgColor());
        gamePane.getChildren().add(rectangle);

        addRects();

    }


    private void repaint() {
//        addRects();
        for (int i = 1; i < column - 1; i++) {
            for (int j = 1; j < row - 1; j++) {
                if (rectangles[i][j].getFill() == well[i][j]) {
//                    rectangles[i][j].setFill(well[i][j]);
                    pane.getChildren().remove(rectangles[i][j]);
                    pane.getChildren().add(rectangles[i][j]);
                }
            }
        }

        brick.paint(pane, pieceOrigin, false);
        setLevelUp();
        refreshLabels();
    }

    private void setLevelUp() {
        int lvl;
        if (tetris.getScore() > 0) {
            int removedLines = tetris.getScore() / tetris.getSettingsManager().getScorePerRow();
            if (removedLines == 3) {
                lvl = 2;
            } else if (removedLines == 6) {
                lvl = 3;
            } else if (removedLines == 9) {
                lvl = 4;
            } else if (removedLines == 12) {
                lvl = 5;
            } else if (removedLines == 15) {
                lvl = 6;
            } else if (removedLines == 20) {
                lvl = 7;
            } else if (removedLines == 25) {
                lvl = 8;
            } else if (removedLines == 30) {
                lvl = 9;
            } else if (removedLines >= 40) {
                lvl = 10;
            } else {
                return;
            }

            lvl = lvl + tetris.getSettingsManager().getStartLevel() - 1;

            level = lvl;
            tetris.setLevel(lvl);
        }
    }

    private void addRects() {
        Lighting lighting = new Lighting();
        for (int i = 0; i < column; i++) {
            for (int j = 0; j < row; j++) {
                Rectangle rectangle = new Rectangle(brickSize * i, brickSize * j, brickSize - 1, brickSize - 1);
                rectangle.setFill(well[i][j]);
                rectangle.setEffect(lighting);
                pane.getChildren().add(rectangle);
                rectangles[i][j] = rectangle;
            }
        }

        for (int i = 0; i < column; i++) {
            Rectangle rectangle = new Rectangle(brickSize * i, 0, brickSize - 1, brickSize - 1);
            rectangle.setFill(Color.GRAY);
            rectangle.setEffect(lighting);
            pane.getChildren().add(rectangle);
        }
    }

    public Color[][] getWell() {
        return well;
    }

    private Brick brick;

    public boolean move(Direction direction, Point pieceOrigin) {
        if (direction == Direction.LEFT) {
            if (brick.collidesAt(pieceOrigin.x + -1, pieceOrigin.y)) {
                pieceOrigin.x += -1;
                return true;
            }
        } else if (direction == Direction.RIGHT) {
            if (brick.collidesAt(pieceOrigin.x + +1, pieceOrigin.y)) {
                pieceOrigin.x += +1;
                return true;
            }
        }
        return false;
    }

    public boolean moveDown(boolean isFromEnter) {
        if (brick.collidesAt(pieceOrigin.x, pieceOrigin.y + 1)) {
            if (isFromEnter) {
                while (brick.collidesAt(pieceOrigin.x, pieceOrigin.y + 1)) {
                    pieceOrigin.y += +1;
                }
            } else {
                pieceOrigin.y += +1;
            }
            return true;
        } else {
            fixToWell();

            //cancel timer for press enter bug fix
            if (timer != null) {
                timer.cancel();
            }

            return false;
        }
    }

    boolean isGameOver = false;

    private void fixToWell() {
        if (isGameOver) {
            return;
        }
        if (brick.getPoint().y == 2) {
            isGameOver = true;
            setupGameOver();
            return;
        }

        brick.freeze(well);
        start();
    }

    private void setupGameOver() {
        if (tetris.getHeart() == 1) {
            if (!isHighScore()) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Game Over");
                alert.setHeaderText(null);
                alert.setContentText("YOU LOSE AND NOT IN TOP 10!");
                alert.showAndWait();
                tetris.setHeart(3);
                tetris.setScore(0);
                refreshLabels();
                restart();
            }
        } else {
            tetris.setHeart(tetris.getHeart() - 1);
            refreshLabels();
            restart();
        }
    }

    private void refreshLabels() {
        lblScore.setText("Score: " + tetris.getScore());
        lblHeart.setText("Heart: " + tetris.getHeart());
        lblLevel.setText("level: " + tetris.getLevel());
    }

    private void restart() {
        pieceOrigin = new Point(5, 2);
        isGameOver = false;
        level = tetris.getSettingsManager().getStartLevel();
        tetris.setLevel(level);
        paint(pane);
        start();
    }

    private boolean isHighScore() {
        ScoreManager manager = tetris.getScoreManager();
        manager.load();
        ArrayList<Player> players = manager.getPlayers();

        if (players.size() < 10) {
            return saveScore(manager);
        } else {
            //sort players list des on score
            players.sort(Comparator.comparing(Player::getScore).reversed());

            //on top 10
            if (players.get(players.size() - 1).getScore() < tetris.getScore()) {
                return saveScore(manager);
            }
        }
        return false;
    }

    private boolean saveScore(ScoreManager manager) {
        String name = showInputNameDialog();
        if (!name.equals("")) {
            manager.addPlayer(name, tetris.getScore());
            manager.save();
        }
        return true;
    }

    private String showInputNameDialog() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("New High Score");
        dialog.setHeaderText(" *** New High Score :" + tetris.getScore() + " ***");
        dialog.setContentText("Please enter your name:");
        Optional<String> result = dialog.showAndWait();
        return result.orElse("");
    }

    public void sceneKeyListeners(KeyEvent event) {
        if (event.getCode() == KeyCode.UP) {
            if (brick.rotate(-1)) {
                repaint();
            }
        } else if (event.getCode() == KeyCode.DOWN) {
            if (brick.rotate(+1)) {
                repaint();
            }
        } else if (event.getCode() == KeyCode.LEFT) {
            if (move(Direction.LEFT, pieceOrigin)) {
                repaint();
            }
        } else if (event.getCode() == KeyCode.RIGHT) {
            if (move(Direction.RIGHT, pieceOrigin)) {
                repaint();
            }
        } else if (event.getCode() == KeyCode.ENTER) {
            if (moveDown(true)) {
                repaint();
            }
//          score += 1;
        }
    }

    public int getRow() {
        return row;
    }

    public void setWell(Color[][] well) {
        this.well = well;
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

    private Label lblScore;
    private Label lblHeart;
    private Label lblLevel;

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