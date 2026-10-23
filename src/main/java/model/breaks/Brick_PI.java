package model.breaks;

import controller.Brick;
import controller.GameBoard;
import javafx.scene.paint.Color;

import java.awt.*;


public class Brick_PI extends Brick {

    public Brick_PI(GameBoard gameBoard, Point point) {
        super(gameBoard, point);
        setIsFilled(new boolean[][]{
                {false, false, false},
                {true, true, true},
                {true, false, true},
        });
        setColor(Color.MAGENTA);
    }

}
