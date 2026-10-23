package model.breaks;

import controller.Brick;
import controller.GameBoard;
import javafx.scene.paint.Color;

import java.awt.*;


public class Brick_J extends Brick {
    public Brick_J(GameBoard gameBoard, Point point) {
        super(gameBoard, point);
        setIsFilled(new boolean[][]{
                {false, false, true},
                {false, false, true},
                {false, true, true},
        });
        setColor(Color.ORANGE);
    }


}
