# Tetris (JavaFX) 
 
A desktop Tetris clone built with Java 25 and JavaFX 25, using FXML for the views and Maven for the build. 
 
## Features 
 
- Ten tetromino types with a randomized bag: Brick_0, I, J, L, L3, PI, S, T, Z. 
- 15 x 20 playing field rendered as JavaFX Rectangles inside a Pane. 
- Next-piece preview in a dedicated pane beside the board. 
- Live HUD showing level, score and remaining hearts (3 at start). 
- Main menu with New Game, Scores Table, Settings and Exit. 
- Settings window for points per cleared row, starting level, background color and clearing saved scores. 
- High score table of players (name and score), persisted to disk. 
- Start / Stop button to begin or pause a round, plus a back button to the menu. 
- Game-over dialog that asks for a player name and stores the score. 
 
## Requirements 
 
- JDK 25 (maven.compiler.release is 25) 
- Maven 3.9 or newer 
- JavaFX 25.0.2, resolved by Maven (no separate SDK needed) 
 
## Build and run 
 
mvn clean compile 
mvn javafx:run 
 
The javafx-maven-plugin is configured with the main class view.Tetris. 
 
## Controls 
 
| Key | Action | 
| --- | --- | 
| Left / Right arrow | Move the falling piece horizontally | 
| Up arrow | Rotate the falling piece | 
| Down arrow | Move the piece down faster | 
| Enter | Hard drop | 
 
## Project layout 
 
src/main/java/controller - game logic and FXML controllers (GameBoard, GameController, MainMenuController, ScoreManager, SettingsManager, Brick). 
src/main/java/model - Player model and tetromino definitions in the breaks package. 
src/main/java/view - Tetris.java, the JavaFX Application entry point. 
src/main/java/enums and util - Direction/State enums, ChangeScene and StageProperty helpers. 
src/main/resources - FXML views (main_menu, game, settings, scores_table), style.css and images. 
 
## Saved data 
 
Settings and scores are stored per user in the .tetris folder under the home directory: setting.txt (points per row, background color, starting level) and scores.txt (one name%score entry per line). Defaults are used when the files are missing. 
 
## Notes 
 
- The window is fixed size and not resizable. 
- There is no automated test suite in the repository yet. 
- The build targets Java 25, so a matching JDK is required. 
 
