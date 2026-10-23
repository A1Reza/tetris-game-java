package controller;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.paint.Color;
import view.Tetris;

import java.io.*;
import java.net.URL;
import java.util.ResourceBundle;


public class SettingsManager implements Initializable {
    private int scorePerRow;
    private int startLevel;
    private Color bgColor;

    public SettingsManager() {
        load(false);
    }


    @FXML
    javafx.scene.control.TextField lblScorePerRow, lblStartLevel;

    @FXML
    Button btnClearScore;

    @FXML
    ColorPicker cpBgColor;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        load(true);

        lblScorePerRow.textProperty().addListener((observable, oldValue, newValue) -> {
            save();
        });

        lblStartLevel.textProperty().addListener((observable, oldValue, newValue) -> {
            save();
        });
        // create a event handler
        EventHandler<ActionEvent> event = new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e) {
                // color
                bgColor = cpBgColor.getValue();

                // set text of the label to RGB value of color
                save();
            }
        };

        // set listener
        cpBgColor.setOnAction(event);

        cpBgColor.setValue(getBgColor());

        btnClearScore.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                Tetris.tetris.getScoreManager().clear();
            }
        });
    }

    public void load(boolean flag) {
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader("setting.txt"));
            String score = bufferedReader.readLine();
            String color = bufferedReader.readLine();
            String level = bufferedReader.readLine();
            setScorePerRow(Integer.parseInt(score));
            setBgColor(Color.valueOf(color));
            setStartLevel(Integer.parseInt(level));


            if (flag) {
                lblScorePerRow.setText(score);
                cpBgColor.setValue(Color.valueOf(color));
                lblStartLevel.setText(level);
            }


        } catch (IOException e) {
            setScorePerRow(10);
            setBgColor(Color.valueOf("#cccccc"));
            setStartLevel(1);
        }
    }

    public void save() {
        try {
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter("setting.txt"));
            bufferedWriter.write(lblScorePerRow.getText().trim());
            bufferedWriter.newLine();
            bufferedWriter.write(cpBgColor.getValue().toString());
            bufferedWriter.newLine();
            bufferedWriter.write(lblStartLevel.getText().trim());
            bufferedWriter.flush();
            bufferedWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public int getScorePerRow() {
        return scorePerRow;
    }

    public void setScorePerRow(int scorePerRow) {
        if (scorePerRow >= 10 && startLevel <= 1000) {
            this.scorePerRow = scorePerRow;
        } else {
            this.scorePerRow = 10;
        }

    }

    public int getStartLevel() {
        return startLevel;
    }

    public void setStartLevel(int startLevel) {
        if (startLevel >= 1 && startLevel <= 10) {
            this.startLevel = startLevel;
        } else {
            this.startLevel = 1;
        }
    }

    public Color getBgColor() {
        return bgColor;
    }

    public void setBgColor(Color bgColor) {
        this.bgColor = bgColor;
    }

}
