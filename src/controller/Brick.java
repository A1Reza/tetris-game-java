package controller;

import enums.Direction;
import javafx.scene.effect.Lighting;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.awt.*;
import java.util.ArrayList;


public class Brick {
    private Point point;
    private int rotation;
    private static final int HEIGHT = 3;
    private static final int WIDTH = 3;
    private Color color;
    private boolean[][] isFilled = new boolean[3][3];

    private final GameBoard gameBoard;

    public Brick(GameBoard gameBoard) {
        this.gameBoard = gameBoard;
        scorePerRow = gameBoard.getTetris().getSettingsManager().getScorePerRow();
    }

    public Brick(GameBoard gameBoard, Point point) {
        this.gameBoard = gameBoard;
        this.point = point;
        scorePerRow = gameBoard.getTetris().getSettingsManager().getScorePerRow();
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

    ArrayList<Point> listNext = new ArrayList<>();
    ArrayList<Point> listCurrent = new ArrayList<>();
    private final int scorePerRow;

    public void paint(Pane pane, Point pieceOrigin, boolean isForNext) {
        setPoint(pieceOrigin);
        //next brick
        if (isForNext) {
            int size = 18;
            pane.getChildren().clear();
            drawBrickRects(pane, size, listNext);
        } else {
            drawBrickRects(pane, gameBoard.getBrickSize(), listCurrent);
        }

    }


    private void drawBrickRects(Pane pane, int size, ArrayList<Point> list) {
        makePointsFromBooleans(list);
        for (Point p : list) {
            Rectangle rectangle = new Rectangle((p.x + getPoint().x) * size,
                    (p.y + getPoint().y) * size,
                    size - 1, size - 1);
            rectangle.setFill(getColor());
            rectangle.setEffect(new Lighting());
            pane.getChildren().add(rectangle);
        }
    }

    private void makePointsFromBooleans(ArrayList<Point> list) {
        list.clear();
        for (int i = 0; i < HEIGHT; i++) {
            for (int j = 0; j < WIDTH; j++) {
                Point p = new Point();
                if (getIsFilled()[i][j]) {
                    p.x = i;
                    p.y = j;
                    list.add(p);
                }
            }
        }
    }

    boolean[][] isFilledBeforeRotation;

    public boolean rotate(int i) {
        int newRotation = (rotation + i) % 4;
        if (newRotation < 0) {
            newRotation = 3;
        }
        isFilledBeforeRotation = getIsFilled();
        setIsFilled(rotateClockWise(isFilledBeforeRotation));
        makePointsFromBooleans(listCurrent);
        if (collidesAt(getPoint().x, getPoint().y)) {
            rotation = newRotation;
            return true;
        } else {
            setIsFilled(isFilledBeforeRotation);
            makePointsFromBooleans(listCurrent);
            return false;
        }
    }

    private boolean[][] rotateClockWise(boolean[][] matrix) {
        int size = matrix.length;
        boolean[][] ret = new boolean[size][size];
        for (int i = 0; i < size; ++i)
            for (int j = 0; j < size; ++j)
                ret[i][j] = matrix[size - j - 1][i]; //***
        return ret;
    }

    // Make the dropping piece part of the well, so it is available for
    // collision detection.
    public void freeze(Color[][] well) {
        for (Point p : listCurrent) {
            well[getPoint().x + p.x][getPoint().y + p.y] = getColor();
        }
        clearRows();
    }


    // Collision test for the dropping piece
    public boolean collidesAt(int x, int y) {
        for (Point p : listCurrent) {
            if (gameBoard.getWell()[p.x + x][p.y + y] != gameBoard.getBgColor()) {
                return false;
            }
        }
        return true;
    }


    int score = 0;

    private void clearRows() {
        boolean gap;
        int numClears = 0;

        for (int j = gameBoard.getRow() - 2; j > 0; j--) {
            gap = false;
            for (int i = 1; i < gameBoard.getColumn() - 1; i++) {
                if (gameBoard.getWell()[i][j] == gameBoard.getBgColor()) {
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


        score = gameBoard.getTetris().getScore();

        for (int i = 0; i < numClears; i++) {
            score = score + scorePerRow;
        }

        gameBoard.getTetris().setScore(score);
    }

    private void deleteRow(int row) {
        Color[][] well = gameBoard.getWell();
        for (int j = row - 1; j > 0; j--) {
            for (int i = 1; i < gameBoard.getColumn(); i++) {
                well[i][j + 1] = well[i][j];
            }
        }
        gameBoard.setWell(well);
    }

    //can be use everywhere is needed
    // not need for game right now (moves handle in GameBoard Class)
    public boolean move(Direction direction, Point pieceOrigin) {
        if (direction == Direction.LEFT) {
            pieceOrigin.x += -1;
            return true;
        } else if (direction == Direction.RIGHT) {
            pieceOrigin.x += +1;
            return true;
        } else if (direction == Direction.DOWN) {
            pieceOrigin.y += +1;
            return true;
        }
        setPoint(pieceOrigin);
        return false;
    }

}
