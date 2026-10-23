package model.breaks;

import controller.Brick;
import controller.GameBoard;
import javafx.scene.paint.Color;

import java.awt.*;


public class Brick_L extends Brick {
    public Brick_L(GameBoard gameBoard, Point point) {
        super(gameBoard, point);
        setIsFilled(new boolean[][]{
                {true, false, false},
                {true, false, false},
                {true, true, false},
        });
        setColor(Color.RED);
    }
}
