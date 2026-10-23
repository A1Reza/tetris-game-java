module tetris {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;

    opens view       to javafx.fxml, javafx.graphics;
    opens controller to javafx.fxml;
    opens model      to javafx.fxml;

    exports view;
}