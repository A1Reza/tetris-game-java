package model.breaks;

import controller.Brick;
import controller.GameBoard;
import javafx.scene.paint.Color;

import java.awt.*;


public class Brick_Z extends Brick {
    public Brick_Z(GameBoard gameBoard, Point point) {
        super(gameBoard, point);
        setIsFilled(new boolean[][]{
                {false, false, false},
                {true, true, false},
                {false, true, true},
        });
        setColor(Color.YELLOW);
    }

}
