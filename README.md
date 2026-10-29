# Tetris — One more line.

A refreshed JavaFX desktop game and a browser edition you can play without installing Java.

**Web game:** [a1reza.github.io/tetris-game-java](https://a1reza.github.io/tetris-game-java/)

## Browser edition

A responsive arcade with a warm, minimal interface, a dark playing field, and keyboard and touch controls. Built with HTML, CSS, JavaScript, and Canvas; no server or runtime packages are needed for the published game.

- Standard seven-piece bag on a 10 × 20 board.
- Three upcoming pieces, hold, and an optional ghost piece.
- Clockwise and counterclockwise rotation with SRS wall and floor kicks.
- Immediate hard drop and a 450 ms lock delay with a limit on movement resets.
- Line-clear scoring, faster gravity every 10 lines, and configurable starting level.
- Pause/resume, automatic pause when leaving the tab, and a restart dialog.
- Optional synthesized sound, saved preferences, and a personal top 10 in browser storage.
- Accessible buttons and dialogs, keyboard focus states, reduced-motion support, and status announcements.

### Run locally

Requires Node.js 24 or newer for development and tests.

```sh
npm ci
npm run dev
```

Open **http://127.0.0.1:4173**. The web game itself does not need Java or Maven.

```sh
npm test                    # Game-rule regression tests
npx playwright install chromium
npm run test:e2e            # Desktop and mobile browser checks
npm run build              # Copy the publishable site to dist/
```

On Linux, install browser dependencies with `npx playwright install --with-deps chromium`.

### Controls

| Key           | Action                            |
| ------------- | --------------------------------- |
| ← / →         | Move                              |
| ↑ / X         | Rotate clockwise                  |
| Z             | Rotate counterclockwise           |
| ↓             | Soft drop                         |
| Space / Enter | Hard drop; start a game when idle |
| C / Shift     | Hold piece                        |
| P / Escape    | Pause or resume                   |

Touch controls appear on phones and other touch devices. Each cleared line contributes toward the next level; drop points do not. Clearing one, two, three, or four lines awards 100, 300, 500, or 800 points multiplied by the current level. Soft drop awards one point per cell; hard drop awards two.

Scores and preferences are local to this browser and device. Play still works when browser storage or audio is unavailable. Web fonts have local system fallbacks.

## GitHub Pages deployment

The workflow in [`.github/workflows/pages.yml`](.github/workflows/pages.yml) runs the web rule tests, desktop/mobile browser checks, Java regression tests, and a static build. It deploys `dist/` to GitHub Pages after checks pass on a push to `main`. Pull requests run checks without publishing. You can also deploy `main` from the workflow's **Run workflow** button.

1. Push these changes to GitHub's `main` branch.
2. In **Settings → Pages → Build and deployment**, select **GitHub Actions** as the source. This repository was already configured that way when this workflow was added.
3. Follow **Actions → Test and deploy Tetris**. After a successful run, the game is served at **https://a1reza.github.io/tetris-game-java/**.

The site uses relative asset URLs, so it also works under a repository subpath or a custom domain. A fork gets its own Pages URL. The workflow follows [GitHub's custom Pages workflow guidance](https://docs.github.com/en/pages/getting-started-with-github-pages/using-custom-workflows-with-github-pages).

## Java desktop edition

The original game remains a native JavaFX application, with a redesigned menu and game screen matching the web edition's colors. It keeps its nine-shape variant, three lives, configurable points per row, and local named scores. Its 15 × 20 grid includes walls and a floor, leaving a 13 × 19 playable area.

Requires **JDK 25** and **Maven 3.9+**. Maven resolves JavaFX 25.0.2.

```sh
mvn clean compile
mvn javafx:run
```

```sh
mvn test                   # JavaFX and game regression tests
mvn verify                 # Tests plus JAR packaging
```

JavaFX tests need a graphical display. On a headless Linux system, use `xvfb-run -a mvn verify`, as the workflow does.

Desktop controls are ← / → to move, ↑ to rotate clockwise, Z to rotate counterclockwise, ↓ to soft drop, Space / Enter to hard drop, and P / Escape to pause. The desktop variant does not include hold.

Desktop fixes include preserving the active piece on resume, instant hard-drop locking, correct Down-arrow behavior, resetting piece orientation on spawn, a four-block I piece, keeping previews from moving the active piece, updating gravity when leveling up, cleaning up the game timer on exit, color-independent collision detection, clearing the top row correctly, numeric score ordering, safe score names, and validation before saving settings.

Settings and named scores are saved under `~/.tetris/` as `setting.txt` and `scores.txt`. Desktop and browser saves are separate.

## Layout

```text
web/                           Static browser game and UI
scripts/                       Local web server and static build
test/                          Web rule and browser tests
src/main/java/                 JavaFX controllers and piece definitions
src/main/resources/view/       Desktop FXML screens and stylesheet
src/test/java/                 Desktop regression tests
.github/workflows/pages.yml    Tests and GitHub Pages deployment
```
