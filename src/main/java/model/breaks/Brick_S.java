package model.breaks;

import controller.Brick;
import controller.GameBoard;
import javafx.scene.paint.Color;

import java.awt.*;


public class Brick_S extends Brick {
    public Brick_S(GameBoard gameBoard, Point point) {
        super(gameBoard, point);
        setIsFilled(new boolean[][]{
                {false, true, true},
                {true, true, false},
                {false, false, false},
        });
        setColor(Color.PINK);
    }

}
