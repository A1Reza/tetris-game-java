package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.paint.Color;
import view.Tetris;

import java.io.*;
import java.net.URL;
import java.nio.file.*;
import java.util.ResourceBundle;

public class SettingsManager implements Initializable {

    private static final Path DATA_DIR =
            Paths.get(System.getProperty("user.home"), ".tetris");
    private static final Path SETTING_FILE = DATA_DIR.resolve("setting.txt");

    private int scorePerRow;
    private int startLevel;
    private Color bgColor;

    public SettingsManager() {
        load(false);
    }

    @FXML
    private javafx.scene.control.TextField lblScorePerRow, lblStartLevel;
    @FXML
    private Button btnClearScore;
    @FXML
    private ColorPicker cpBgColor;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        load(true);

        lblScorePerRow.textProperty().addListener((obs, oldV, newV) -> save());
        lblStartLevel.textProperty().addListener((obs, oldV, newV) -> save());

        cpBgColor.setOnAction((ActionEvent e) -> {
            bgColor = cpBgColor.getValue();
            save();
        });

        if (bgColor != null) {
            cpBgColor.setValue(bgColor);
        }

        btnClearScore.setOnAction(e -> Tetris.tetris.getScoreManager().clear());
    }

    public void load(boolean flag) {
        try {
            Files.createDirectories(DATA_DIR);
            if (!Files.exists(SETTING_FILE)) {
                throw new IOException("no settings file yet");
            }
            try (BufferedReader br = Files.newBufferedReader(SETTING_FILE)) {
                String score = br.readLine();
                String color = br.readLine();
                String level = br.readLine();
                setScorePerRow(Integer.parseInt(score));
                setBgColor(Color.valueOf(color));
                setStartLevel(Integer.parseInt(level));

                if (flag && lblScorePerRow != null) {
                    lblScorePerRow.setText(score);
                    cpBgColor.setValue(Color.valueOf(color));
                    lblStartLevel.setText(level);
                }
            }
        } catch (Exception e) {
            setScorePerRow(10);
            setBgColor(Color.web("#cccccc"));
            setStartLevel(1);
        }
    }

    public void save() {
        if (lblScorePerRow == null || cpBgColor == null || lblStartLevel == null) return;

        Color chosen = cpBgColor.getValue();
        if (chosen != null) bgColor = chosen;

        try {
            Files.createDirectories(DATA_DIR);
            try (BufferedWriter bw = Files.newBufferedWriter(SETTING_FILE)) {
                bw.write(lblScorePerRow.getText().trim());
                bw.newLine();
                bw.write(bgColor.toString());
                bw.newLine();
                bw.write(lblStartLevel.getText().trim());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int getScorePerRow() {
        return scorePerRow;
    }

    public void setScorePerRow(int scorePerRow) {
        this.scorePerRow = (scorePerRow >= 10 && scorePerRow <= 1000) ? scorePerRow : 10;
    }

    public int getStartLevel() {
        return startLevel;
    }

    public void setStartLevel(int startLevel) {
        this.startLevel = (startLevel >= 1 && startLevel <= 10) ? startLevel : 1;
    }

    public Color getBgColor() {
        return bgColor;
    }

    public void setBgColor(Color bgColor) {
        this.bgColor = bgColor;
    }
}