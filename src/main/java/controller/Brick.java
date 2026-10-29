package controller;

import enums.Direction;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;

public class Brick {
    private Point point;
    private int rotation;
    private Color color;
    // Matrices use [x][y], matching the desktop board's coordinates.
    private boolean[][] isFilled;
    private boolean[][] spawnShape;
    private final GameBoard gameBoard;
    private final ArrayList<Point> listCurrent = new ArrayList<>();

    public Brick(GameBoard gameBoard) { this(gameBoard, new Point()); }
    public Brick(GameBoard gameBoard, Point point) { this.gameBoard = gameBoard; this.point = point; }
    public Point getPoint() { return point; }
    public void setPoint(Point point) { this.point = point; }
    public Color getColor() { return color; }
    public void setColor(Color color) { this.color = color; }
    public boolean[][] getIsFilled() { return isFilled; }

    public void setIsFilled(boolean[][] shape) {
        spawnShape = copy(shape);
        isFilled = copy(shape);
        makePointsFromBooleansForCurrent();
    }

    private static boolean[][] copy(boolean[][] matrix) {
        return Arrays.stream(matrix).map(boolean[]::clone).toArray(boolean[][]::new);
    }

    public void setRotation(int value) {
        rotation = Math.floorMod(value, 4);
        isFilled = copy(spawnShape);
        for (int i = 0; i < rotation; i++) isFilled = rotated(isFilled, 1);
        makePointsFromBooleansForCurrent();
    }

    public void paint(Pane pane, Point origin, boolean preview) {
        if (pane == null) return;
        pane.getChildren().clear();
        int size = preview ? 18 : gameBoard.getBrickSize();
        // Previews never change the live piece's origin or orientation.
        boolean[][] shape = preview ? spawnShape : isFilled;
        for (int x = 0; x < shape.length; x++) {
            for (int y = 0; y < shape[x].length; y++) {
                if (!shape[x][y]) continue;
                Rectangle r = new Rectangle((x + origin.x) * size, (y + origin.y) * size, size - 2, size - 2);
                r.setArcWidth(4); r.setArcHeight(4); r.setFill(color);
                pane.getChildren().add(r);
            }
        }
    }

    public void makePointsFromBooleansForCurrent() {
        listCurrent.clear();
        for (int x = 0; x < isFilled.length; x++) {
            for (int y = 0; y < isFilled[x].length; y++) {
                if (isFilled[x][y]) listCurrent.add(new Point(x, y));
            }
        }
    }

    public boolean rotate(int direction) {
        if (direction == 0) return false;
        boolean[][] before = isFilled;
        isFilled = rotated(before, direction);
        makePointsFromBooleansForCurrent();
        for (int dx : new int[]{0, -1, 1, -2, 2}) {
            if (collidesAt(point.x + dx, point.y)) {
                point.x += dx;
                rotation = Math.floorMod(rotation + Integer.signum(direction), 4);
                return true;
            }
        }
        isFilled = before;
        makePointsFromBooleansForCurrent();
        return false;
    }

    private boolean[][] rotated(boolean[][] matrix, int direction) {
        int size = matrix.length;
        boolean[][] result = new boolean[size][size];
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                result[x][y] = direction > 0 ? matrix[y][size - x - 1] : matrix[size - y - 1][x];
            }
        }
        return result;
    }

    public void freeze(Color[][] well) {
        for (Point p : listCurrent) well[point.x + p.x][point.y + p.y] = color;
        clearRows();
    }

    /** True when the shape fits. Null cells are empty regardless of the background color. */
    public boolean collidesAt(int x, int y) {
        for (Point p : listCurrent) {
            int wx = p.x + x, wy = p.y + y;
            if (wx < 1 || wx >= gameBoard.getColumn() - 1 || wy < 0 || wy >= gameBoard.getRow() - 1
                    || gameBoard.getWell()[wx][wy] != null) return false;
        }
        return true;
    }

    private void clearRows() {
        Color[][] well = gameBoard.getWell();
        int clears = 0;
        for (int y = gameBoard.getRow() - 2; y >= 0; y--) {
            boolean full = true;
            for (int x = 1; x < gameBoard.getColumn() - 1; x++) {
                if (well[x][y] == null) { full = false; break; }
            }
            if (!full) continue;
            for (int row = y; row > 0; row--) {
                for (int x = 1; x < gameBoard.getColumn() - 1; x++) well[x][row] = well[x][row - 1];
            }
            for (int x = 1; x < gameBoard.getColumn() - 1; x++) well[x][0] = null;
            clears++;
            y++;
        }
        gameBoard.getTetris().setScore(gameBoard.getTetris().getScore()
                + clears * gameBoard.getTetris().getSettingsManager().getScorePerRow());
    }

    public boolean move(Direction direction, Point origin) {
        int dx = direction == Direction.LEFT ? -1 : direction == Direction.RIGHT ? 1 : 0;
        int dy = direction == Direction.DOWN ? 1 : 0;
        if (!collidesAt(origin.x + dx, origin.y + dy)) return false;
        origin.translate(dx, dy);
        return true;
    }
}
