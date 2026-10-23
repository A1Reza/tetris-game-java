package util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.IOException;

public class ChangeScene {
    public static void setScene(Pane pane, String fxmlPath, String title) throws IOException {
        Stage stage = (Stage) pane.getScene().getWindow();
        Pane root = FXMLLoader.load(ChangeScene.class.getResource(fxmlPath));
        root.setStyle(StageProperty.STYLE_EFFECT);
        Scene scene = new Scene(root, StageProperty.WIDTH, StageProperty.HEIGHT);
        scene.getStylesheets().add(
                ChangeScene.class.getResource(StageProperty.STYLE_PATH).toExternalForm());
        stage.setTitle(title);
        stage.setScene(scene);
    }
}