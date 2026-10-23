package model.breaks;

import controller.Brick;
import controller.GameBoard;
import javafx.scene.paint.Color;

import java.awt.*;


public class Brick_0 extends Brick {

    public Brick_0(GameBoard gameBoard, Point point) {
        super(gameBoard, point);
        setIsFilled(new boolean[][]{
                {false, false, false},
                {false, true, true},
                {false, true, true},
        });
        setColor(Color.GREEN);
    }


}
