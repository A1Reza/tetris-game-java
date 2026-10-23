package model.breaks;

import controller.Brick;
import controller.GameBoard;
import javafx.scene.paint.Color;

import java.awt.*;


public class Brick_I extends Brick {
    public Brick_I(GameBoard gameBoard, Point point) {
        super(gameBoard, point);
        setIsFilled(new boolean[][]{
                {false, true, false},
                {false, true, false},
                {false, true, false},
        });
        setColor(Color.BLUE);
    }


}
