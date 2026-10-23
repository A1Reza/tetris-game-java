package model.breaks;

import controller.Brick;
import controller.GameBoard;
import javafx.scene.paint.Color;

import java.awt.*;


public class Brick_T extends Brick {
    public Brick_T(GameBoard gameBoard, Point point) {
        super(gameBoard, point);
        setIsFilled(new boolean[][]{
                {false, false, false},
                {false, true, false},
                {true, true, true},
        });
        setColor(Color.CYAN);
    }

}
