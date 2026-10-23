package controller;

import enums.Direction;
import javafx.scene.effect.Lighting;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.awt.Point;
import java.util.ArrayList;

public class Brick {
    private Point point;
    private int rotation;
    private static final int HEIGHT = 3;
    private static final int WIDTH = 3;
    private Color color;
    private boolean[][] isFilled = new boolean[3][3];
    private final GameBoard gameBoard;
    private final int scorePerRow;

    private final ArrayList<Point> listNext = new ArrayList<>();
    private final ArrayList<Point> listCurrent = new ArrayList<>();
    private boolean[][] isFilledBeforeRotation;

    public Brick(GameBoard gameBoard) {
        this.gameBoard = gameBoard;
        this.scorePerRow = gameBoard.getTetris().getSettingsManager().getScorePerRow();
    }

    public Brick(GameBoard gameBoard, Point point) {
        this.gameBoard = gameBoard;
        this.point = point;
        this.scorePerRow = gameBoard.getTetris().getSettingsManager().getScorePerRow();
    }

    public Point getPoint() {
        return point;
    }

    public void setPoint(Point point) {
        this.point = point;
    }

    public void setRotation(int rotation) {
        this.rotation = rotation;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public boolean[][] getIsFilled() {
        return isFilled;
    }

    public void setIsFilled(boolean[][] isFilled) {
        this.isFilled = isFilled;
    }

    public void paint(Pane pane, Point pieceOrigin, boolean isForNext) {
        setPoint(pieceOrigin);
        if (isForNext) {
            int size = 18;
            pane.getChildren().clear();
            drawBrickRects(pane, size, listNext);
        } else {
            pane.getChildren().clear();
            drawBrickRects(pane, gameBoard.getBrickSize(), listCurrent);
        }
    }

    private void drawBrickRects(Pane pane, int size, ArrayList<Point> list) {
        makePointsFromBooleans(list);
        for (Point p : list) {
            Rectangle r = new Rectangle(
                    (p.x + getPoint().x) * size,
                    (p.y + getPoint().y) * size,
                    size - 1, size - 1);
            r.setFill(getColor());
            r.setEffect(new Lighting());
            pane.getChildren().add(r);
        }
    }

    private void makePointsFromBooleans(ArrayList<Point> list) {
        list.clear();
        for (int i = 0; i < HEIGHT; i++) {
            for (int j = 0; j < WIDTH; j++) {
                if (getIsFilled()[i][j]) {
                    list.add(new Point(i, j));
                }
            }
        }
    }

    /**
     * Populate listCurrent from the current isFilled matrix without drawing.
     * Needed before spawn-time collision checks.
     */
    public void makePointsFromBooleansForCurrent() {
        makePointsFromBooleans(listCurrent);
    }

    public boolean rotate(int i) {
        int newRotation = (rotation + i) % 4;
        if (newRotation < 0) newRotation = 3;
        isFilledBeforeRotation = getIsFilled();
        setIsFilled(rotateClockWise(isFilledBeforeRotation));
        makePointsFromBooleans(listCurrent);
        if (collidesAt(getPoint().x, getPoint().y)) {
            rotation = newRotation;
            return true;
        }
        setIsFilled(isFilledBeforeRotation);
        makePointsFromBooleans(listCurrent);
        return false;
    }

    private boolean[][] rotateClockWise(boolean[][] matrix) {
        int size = matrix.length;
        boolean[][] ret = new boolean[size][size];
        for (int i = 0; i < size; ++i)
            for (int j = 0; j < size; ++j)
                ret[i][j] = matrix[size - j - 1][i];
        return ret;
    }

    public void freeze(Color[][] well) {
        for (Point p : listCurrent) {
            well[getPoint().x + p.x][getPoint().y + p.y] = getColor();
        }
        clearRows();
    }

    public boolean collidesAt(int x, int y) {
        for (Point p : listCurrent) {
            int wx = p.x + x;
            int wy = p.y + y;
            if (wx < 0 || wx >= gameBoard.getColumn() || wy < 0 || wy >= gameBoard.getRow()) {
                return false;
            }
            if (!gameBoard.getWell()[wx][wy].equals(gameBoard.getBgColor())) {
                return false;
            }
        }
        return true;
    }

    private void clearRows() {
        int numClears = 0;
        for (int j = gameBoard.getRow() - 2; j > 0; j--) {
            boolean gap = false;
            for (int i = 1; i < gameBoard.getColumn() - 1; i++) {
                if (gameBoard.getWell()[i][j].equals(gameBoard.getBgColor())) {
                    gap = true;
                    break;
                }
            }
            if (!gap) {
                deleteRow(j);
                j++;
                numClears++;
            }
        }
        int score = gameBoard.getTetris().getScore() + numClears * scorePerRow;
        gameBoard.getTetris().setScore(score);
    }

    private void deleteRow(int row) {
        Color[][] well = gameBoard.getWell();
        for (int j = row; j > 1; j--) {
            for (int i = 1; i < gameBoard.getColumn() - 1; i++) {
                well[i][j] = well[i][j - 1];
            }
        }
        for (int i = 1; i < gameBoard.getColumn() - 1; i++) {
            well[i][1] = gameBoard.getBgColor();
        }
    }

    public boolean move(Direction direction, Point pieceOrigin) {
        if (direction == Direction.LEFT) {
            pieceOrigin.x -= 1;
            return true;
        }
        if (direction == Direction.RIGHT) {
            pieceOrigin.x += 1;
            return true;
        }
        if (direction == Direction.DOWN) {
            pieceOrigin.y += 1;
            return true;
        }
        return false;
    }
}