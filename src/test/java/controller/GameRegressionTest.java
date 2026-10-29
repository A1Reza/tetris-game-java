package controller;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import model.Player;
import model.breaks.Brick_I;
import model.breaks.Brick_T;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import view.Tetris;

import java.awt.Point;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class GameRegressionTest {
    @TempDir static Path home;
    private static String originalHome;
    private Tetris app;
    private GameBoard board;
    private Pane falling;

    @BeforeAll static void startJavaFx() throws Exception {
        originalHome = System.getProperty("user.home");
        // Windows keeps native DLLs locked until JVM exit; keep that cache outside @TempDir.
        System.setProperty("javafx.cachedir", Path.of("target", "javafx-cache").toAbsolutePath().toString());
        System.setProperty("user.home", home.toString());
        CountDownLatch started = new CountDownLatch(1);
        Platform.startup(() -> { Platform.setImplicitExit(false); started.countDown(); });
        assertTrue(started.await(15, TimeUnit.SECONDS));
    }
    @AfterAll static void stopJavaFx() {
        Platform.exit();
        System.setProperty("user.home", originalHome);
    }
    private static <T> T fx(Callable<T> work) throws Exception {
        FutureTask<T> task = new FutureTask<>(work);
        Platform.runLater(task);
        return task.get(15, TimeUnit.SECONDS);
    }
    @BeforeEach void setUp() throws Exception {
        fx(() -> {
            app = new Tetris(); Tetris.tetris = app; app._init();
            app.getScoreManager().clear();
            board = new GameBoard(1, app); app.setGameBoard(board);
            falling = new Pane(); board.setFallingPane(falling); board.setNextPane(new Pane()); board.paint(new Pane());
            return null;
        });
    }
    @AfterEach void cleanUp() throws Exception {
        fx(() -> { app.stop(); board.stop(); return null; });
    }
    private static KeyEvent key(KeyCode code) {
        return new KeyEvent(KeyEvent.KEY_PRESSED, "", "", code, false, false, false, false);
    }
    private String fallingCoordinates() {
        return falling.getChildren().stream().map(node -> {
            Rectangle r = (Rectangle) node;
            return r.getX() + "," + r.getY() + ":" + r.getFill();
        }).toList().toString();
    }
    private long occupied() {
        long count = 0;
        for (int x = 1; x < board.getColumn()-1; x++) {
            for (int y = 0; y < board.getRow()-1; y++) if (board.getWell()[x][y] != null) count++;
        }
        return count;
    }

    @Test void pauseResumesTheSamePieceAndRepeatedStartDoesNotReplaceIt() throws Exception {
        fx(() -> {
            board.start(); board.sceneKeyListeners(key(KeyCode.LEFT)); board.moveDown(false);
            String before = fallingCoordinates();
            board.stop(); assertFalse(board.moveDown(false)); board.start();
            assertEquals(before, fallingCoordinates()); board.start(); assertEquals(before, fallingCoordinates());
            return null;
        });
    }
    @Test void hardDropLocksImmediatelyAndDownMovesWithoutRotating() throws Exception {
        fx(() -> {
            board.start(); String before = fallingCoordinates();
            board.sceneKeyListeners(key(KeyCode.DOWN));
            assertNotEquals(before, fallingCoordinates()); assertEquals(0, occupied());
            board.sceneKeyListeners(key(KeyCode.SPACE));
            assertTrue(occupied() >= 3); assertTrue(occupied() <= 5);
            return null;
        });
    }
    @Test void backgroundColoredBlocksRemainSolid() throws Exception {
        fx(() -> {
            Brick brick = new Brick(board, new Point(4,5)); brick.setIsFilled(new boolean[][]{{true}});
            assertTrue(brick.collidesAt(4,5)); board.getWell()[4][5] = board.getBgColor();
            assertFalse(brick.collidesAt(4,5)); assertFalse(brick.collidesAt(0,5)); assertFalse(brick.collidesAt(4,19));
            return null;
        });
    }
    @Test void previewNeverMovesOrRotatesTheActiveBrick() throws Exception {
        fx(() -> {
            Brick brick = new Brick_T(board,new Point(4,5)); brick.rotate(1);
            String before = Arrays.deepToString(brick.getIsFilled());
            brick.paint(new Pane(),new Point(0,0),true);
            assertEquals(new Point(4,5),brick.getPoint()); assertEquals(before,Arrays.deepToString(brick.getIsFilled()));
            return null;
        });
    }
    @Test void spawnResetsRotationAndDirectionsAreInverse() throws Exception {
        fx(() -> {
            Brick brick = new Brick_T(board,new Point(4,5)); String original = Arrays.deepToString(brick.getIsFilled());
            assertTrue(brick.rotate(1)); assertNotEquals(original,Arrays.deepToString(brick.getIsFilled()));
            assertTrue(brick.rotate(-1)); assertEquals(original,Arrays.deepToString(brick.getIsFilled()));
            brick.rotate(1); brick.setRotation(0); assertEquals(original,Arrays.deepToString(brick.getIsFilled()));
            return null;
        });
    }
    @Test void straightTetrominoContainsFourBlocksAndRotates() throws Exception {
        fx(() -> {
            Brick brick = new Brick_I(board,new Point(4,5)); Pane preview = new Pane();
            brick.paint(preview,new Point(0,0),true); assertEquals(4,preview.getChildren().size());
            assertTrue(brick.rotate(1)); brick.freeze(board.getWell()); assertEquals(4,occupied());
            return null;
        });
    }
    @Test void adjacentFullRowsShiftTheStackAndAwardBothRows() throws Exception {
        fx(() -> {
            for (int x = 1; x < board.getColumn()-1; x++) { board.getWell()[x][17] = Color.RED; board.getWell()[x][18] = Color.RED; }
            board.getWell()[3][16] = Color.BLUE;
            Brick brick = new Brick(board,new Point(4,10)); brick.setIsFilled(new boolean[][]{{true}}); brick.setColor(Color.GREEN);
            brick.freeze(board.getWell());
            assertEquals(20,app.getScore()); assertEquals(Color.BLUE,board.getWell()[3][18]); assertNull(board.getWell()[3][0]);
            return null;
        });
    }
    @Test void topRowIsClearedRatherThanDuplicated() throws Exception {
        fx(() -> {
            for (int x = 1; x < board.getColumn()-1; x++) board.getWell()[x][0] = Color.RED;
            Brick brick = new Brick(board,new Point(4,5)); brick.setIsFilled(new boolean[][]{{true}}); brick.setColor(Color.GREEN); brick.freeze(board.getWell());
            assertEquals(10,app.getScore()); assertNull(board.getWell()[4][0]); assertEquals(1,occupied());
            return null;
        });
    }
    @Test void scoreNamesCannotCorruptTheSaveFormatAndOnlyTopTenRemain() throws Exception {
        fx(() -> {
            ScoreManager manager = app.getScoreManager();
            for (int i = 0; i < 12; i++) manager.addPlayer("Player%\n"+i,i*10);
            manager.save(); manager.load(); assertEquals(10,manager.getPlayers().size());
            assertEquals(110,manager.getPlayers().getFirst().getScore());
            assertFalse(manager.getPlayers().getFirst().getName().contains("%"));
            return null;
        });
    }
    @Test void scoreTableSortsNumerically() throws Exception {
        fx(() -> {
            ScoreManager manager = app.getScoreManager(); manager.addPlayer("A",90); manager.addPlayer("B",1000); manager.addPlayer("C",200); manager.save();
            Parent root = FXMLLoader.load(Tetris.class.getResource("/view/scores_table.fxml"));
            @SuppressWarnings("unchecked") TableView<Player> table = (TableView<Player>) root.lookup("#tableScores");
            assertEquals(1000,table.getItems().getFirst().getScore());
            return null;
        });
    }
    @Test void allViewsLoadAndNewGameResetsScoreAndLives() throws Exception {
        fx(() -> {
            for (String name : new String[]{"main_menu","settings","scores_table","game"}) {
                app.setScore(999); app.setHeart(1);
                Parent root = FXMLLoader.load(Tetris.class.getResource("/view/"+name+".fxml"));
                Scene scene = new Scene(root,640,600);
                scene.getStylesheets().add(Tetris.class.getResource("/view/style.css").toExternalForm());
                root.applyCss(); root.layout(); assertTrue(root.getBoundsInLocal().getWidth() > 0);
                if (name.equals("game")) { assertEquals(0,app.getScore()); assertEquals(3,app.getHeart()); }
            }
            return null;
        });
    }
    @Test void invalidSettingsInputDoesNotOverwriteValidSavedSettings() throws Exception {
        fx(() -> {
            Parent root = FXMLLoader.load(Tetris.class.getResource("/view/settings.fxml"));
            TextField level = (TextField) root.lookup("#lblStartLevel");
            level.setText("7"); level.setText("");
            SettingsManager manager = new SettingsManager(); assertEquals(7,manager.getStartLevel());
            level.setText("1");
            return null;
        });
    }
}
