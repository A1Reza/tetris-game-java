import { Game, SHAPES, COLORS } from "./engine.js";

const $ = (id) => document.getElementById(id);
const storage = {
  read(key, fallback) {
    try {
      return JSON.parse(localStorage.getItem(key)) ?? fallback;
    } catch {
      return fallback;
    }
  },
  write(key, value) {
    try {
      localStorage.setItem(key, JSON.stringify(value));
    } catch {
      /* Play still works without storage. */
    }
  },
};
const saved = storage.read("tetris.preferences", {});
const preferences = {
  level: Math.max(1, Math.min(10, Math.floor(Number(saved?.level) || 1))),
  ghost: saved?.ghost !== false,
  sound: saved?.sound === true,
};
const rawScores = storage.read("tetris.scores", []);
let scores = (Array.isArray(rawScores) ? rawScores : [])
  .filter(
    (item) =>
      item &&
      Number.isSafeInteger(item.score) &&
      item.score >= 0 &&
      typeof item.date === "string" &&
      Number.isFinite(Date.parse(item.date)),
  )
  .sort((a, b) => b.score - a.score)
  .slice(0, 10);
let best = scores[0]?.score ?? 0;
export let game = new Game({ startLevel: preferences.level });
let recorded = false;
let audioContext;
let resumeAfterDialog = false;
let previousStatus;
let previousPieces = 0;
let previousLines = 0;
let clearUntil = 0;
const heldKeys = new Map();
const board = $("board").getContext("2d");
board.scale(2, 2);

function text(id, value) {
  const el = $(id);
  if (el.textContent !== String(value)) el.textContent = value;
}
function tone(frequency, duration = 0.07) {
  if (!preferences.sound) return;
  try {
    audioContext ??= new (window.AudioContext || window.webkitAudioContext)();
    void audioContext.resume().catch(() => {});
    const oscillator = audioContext.createOscillator(),
      gain = audioContext.createGain();
    oscillator.type = "sine";
    oscillator.frequency.value = frequency;
    gain.gain.setValueAtTime(0.04, audioContext.currentTime);
    gain.gain.exponentialRampToValueAtTime(
      0.001,
      audioContext.currentTime + duration,
    );
    oscillator.connect(gain);
    gain.connect(audioContext.destination);
    oscillator.start();
    oscillator.stop(audioContext.currentTime + duration);
    oscillator.onended = () => {
      oscillator.disconnect();
      gain.disconnect();
    };
  } catch {
    /* Audio is optional. */
  }
}
function setSound(enabled) {
  preferences.sound = enabled;
  $("sound-icon").setAttribute(
    "href",
    enabled ? "#icon-volume" : "#icon-muted",
  );
  $("sound-button").setAttribute("aria-pressed", String(enabled));
  $("sound-button").setAttribute(
    "aria-label",
    enabled ? "Mute sound" : "Enable sound",
  );
  storage.write("tetris.preferences", preferences);
}
function block(ctx, x, y, size, type, ghost = false) {
  const px = x + 1.5,
    py = y + 1.5,
    s = size - 3;
  if (ghost) {
    ctx.strokeStyle = COLORS[type] + "85";
    ctx.lineWidth = 1;
    ctx.fillStyle = COLORS[type] + "0b";
    ctx.fillRect(px, py, s, s);
    ctx.strokeRect(px + 0.5, py + 0.5, s - 1, s - 1);
    return;
  }
  ctx.fillStyle = COLORS[type];
  ctx.beginPath();
  ctx.roundRect(px, py, s, s, 2);
  ctx.fill();
  ctx.fillStyle = "#ffffff25";
  ctx.fillRect(px + 2, py + 2, s - 4, 2);
  ctx.fillStyle = "#122c3020";
  ctx.fillRect(px + 2, py + s - 4, s - 4, 2);
}
function drawPiece(ctx, piece, y, ghost = false) {
  piece.matrix.forEach((row, dy) =>
    row.forEach((filled, dx) => {
      if (filled && y + dy >= 0)
        block(ctx, (piece.x + dx) * 30, (y + dy) * 30, 30, piece.type, ghost);
    }),
  );
}
function preview(ctx, type, cy, size = 23) {
  const cells = [];
  SHAPES[type].forEach((row, y) =>
    row.forEach((filled, x) => {
      if (filled) cells.push([x, y]);
    }),
  );
  const minX = Math.min(...cells.map((p) => p[0])),
    maxX = Math.max(...cells.map((p) => p[0]));
  const minY = Math.min(...cells.map((p) => p[1])),
    maxY = Math.max(...cells.map((p) => p[1]));
  const left = (180 - (maxX - minX + 1) * size) / 2;
  const top = cy - ((maxY - minY + 1) * size) / 2;
  cells.forEach(([x, y]) =>
    block(ctx, left + (x - minX) * size, top + (y - minY) * size, size, type),
  );
}
function drawPreviews() {
  const next = $("next").getContext("2d");
  next.clearRect(0, 0, 180, 210);
  game.queue
    .slice(0, 3)
    .forEach((type, i) => preview(next, type, 36 + i * 70, i ? 21 : 25));
  for (const id of ["hold", "hold-mobile"]) {
    const hold = $(id).getContext("2d");
    hold.clearRect(0, 0, 180, 90);
    if (game.held) preview(hold, game.held, id === "hold" ? 45 : 40, 24);
    else {
      hold.fillStyle = "#aab29d";
      hold.font = "20px sans-serif";
      hold.textAlign = "center";
      hold.fillText("—", 90, 50);
    }
  }
  $("hold")
    .closest(".hold-card")
    .classList.toggle("unavailable", !game.canHold);
  text(
    "hold-caption",
    !game.held
      ? "Save a piece for later."
      : game.canHold
        ? "Ready when you need it."
        : "One swap per piece.",
  );
}
function newGame() {
  game = new Game({ startLevel: preferences.level });
  game.start();
  recorded = false;
  previousPieces = previousLines = 0;
  heldKeys.clear();
  drawPreviews();
  update();
}
function togglePlay() {
  if (game.status === "over") newGame();
  else if (game.status === "running") {
    game.pause();
    heldKeys.clear();
  } else game.start();
  update();
}
function recordGame() {
  if (recorded) return;
  recorded = true;
  if (game.score > 0) {
    scores.push({ score: game.score, date: new Date().toISOString() });
    scores.sort((a, b) => b.score - a.score);
    scores = scores.slice(0, 10);
    storage.write("tetris.scores", scores);
  }
  best = Math.max(best, game.score);
}
function update() {
  if (game.status === "over") recordGame();
  text("score", String(game.score).padStart(6, "0"));
  text("best", Math.max(best, game.score).toLocaleString());
  text("level", String(game.level).padStart(2, "0"));
  text("lines", String(game.lines).padStart(2, "0"));
  text("progress-label", `${10 - (game.lines % 10)} lines to level up`);
  $("progress").style.width = `${(game.lines % 10) * 10}%`;
  if (game.pieces !== previousPieces) {
    tone(game.lastClear ? 640 : 180, game.lastClear ? 0.2 : 0.06);
    previousPieces = game.pieces;
    drawPreviews();
  }
  if (game.lines !== previousLines) {
    clearUntil = performance.now() + 320;
    previousLines = game.lines;
    text(
      "announcer",
      `${game.lastClear} ${game.lastClear === 1 ? "line" : "lines"} cleared. Score ${game.score}. Level ${game.level}.`,
    );
    const labels = [
      "",
      "A little more breathing room.",
      "Two lines. Twice as nice.",
      "Three lines. Finding your rhythm.",
      "Four lines. A perfect little moment.",
    ];
    text(
      "tip",
      labels[game.lastClear] ||
        "Keep your stack low and leave a little room to breathe.",
    );
  }
  $("board-frame").classList.toggle("cleared", performance.now() < clearUntil);
  if (previousStatus !== game.status) {
    previousStatus = game.status;
    const running = game.status === "running";
    $("overlay").hidden = running;
    text(
      "game-status",
      {
        ready: "READY WHEN YOU ARE",
        running: "IN THE FLOW",
        paused: "TAKING A BREATHER",
        over: "A GOOD RUN",
      }[game.status],
    );
    $("game-status-dot").style.background = running ? "#7b9c70" : "#b4a586";
    text(
      "pause-label",
      {
        ready: "Start game",
        running: "Pause game",
        paused: "Resume game",
        over: "Play again",
      }[game.status],
    );
    $("pause-button").setAttribute("aria-label", $("pause-label").textContent);
    $("pause-icon").setAttribute(
      "href",
      running ? "#icon-pause" : "#icon-play",
    );
    if (!running) {
      const content = {
        ready: [
          "TAKE A BREATHER. PLAY A LITTLE.",
          "Get into\nthe flow.",
          "Clear lines. Make space.\nSee how far you can go.",
          "Let’s play",
          "or press Enter to start",
        ],
        paused: [
          "A MOMENT TO YOURSELF",
          "No rush.\nWe’ll wait.",
          "Your blocks aren’t going anywhere.\nPick up right where you left off.",
          "Keep playing",
          "or press P to resume",
        ],
        over: [
          "EVERY END IS A NEW BEGINNING",
          "Nice\nstacking.",
          `${game.score.toLocaleString()} points. ${game.lines} lines.\nThere’s always one more game.`,
          "Play again",
          "or press Enter for a fresh start",
        ],
      }[game.status];
      text("overlay-eyebrow", content[0]);
      $("overlay-title").replaceChildren(
        ...content[1]
          .split("\n")
          .flatMap((line, i) =>
            i
              ? [document.createElement("br"), document.createTextNode(line)]
              : [document.createTextNode(line)],
          ),
      );
      $("overlay-description").replaceChildren(
        ...content[2]
          .split("\n")
          .flatMap((line, i) =>
            i
              ? [document.createElement("br"), document.createTextNode(line)]
              : [document.createTextNode(line)],
          ),
      );
      $("overlay-action").querySelector("span").textContent = content[3];
      text("overlay-hint", content[4]);
      text(
        "announcer",
        game.status === "over"
          ? `Game over. Score ${game.score}. ${game.lines} lines cleared.`
          : $("game-status").textContent,
      );
    }
  }
}
function draw() {
  board.fillStyle = "#203630";
  board.fillRect(0, 0, 300, 600);
  board.strokeStyle = "#ffffff06";
  board.lineWidth = 1;
  for (let x = 0; x <= 10; x++) {
    board.beginPath();
    board.moveTo(x * 30 + 0.5, 0);
    board.lineTo(x * 30 + 0.5, 600);
    board.stroke();
  }
  for (let y = 0; y <= 20; y++) {
    board.beginPath();
    board.moveTo(0, y * 30 + 0.5);
    board.lineTo(300, y * 30 + 0.5);
    board.stroke();
  }
  game.board.forEach((row, y) =>
    row.forEach((type, x) => {
      if (type) block(board, x * 30, y * 30, 30, type);
    }),
  );
  if (game.status !== "over") {
    if (preferences.ghost && game.status !== "ready")
      drawPiece(board, game.active, game.ghostY(), true);
    drawPiece(board, game.active, game.active.y);
  }
}
function action(name) {
  const actions = {
    left: () => game.move(-1),
    right: () => game.move(1),
    down: () => game.down(true),
    rotate: () => game.rotate(),
    counter: () => game.rotate(-1),
    drop: () => game.hardDrop(),
    hold: () => game.hold(),
  };
  if (actions[name]?.()) {
    if (name === "rotate" || name === "counter") tone(340, 0.035);
    if (name === "hold") drawPreviews();
    update();
  }
}
function openDialog(id) {
  if (document.querySelector("dialog[open]")) return;
  resumeAfterDialog = game.status === "running";
  game.pause();
  heldKeys.clear();
  update();
  $(id).showModal();
}
document.querySelectorAll("dialog").forEach((dialog) => {
  dialog
    .querySelectorAll(".close-dialog")
    .forEach((button) =>
      button.addEventListener("click", () => dialog.close()),
    );
  dialog.addEventListener("close", () => {
    if (resumeAfterDialog) game.start();
    resumeAfterDialog = false;
    update();
  });
  dialog.addEventListener("click", (event) => {
    if (event.target === dialog) {
      const rect = dialog.getBoundingClientRect();
      if (
        event.clientX < rect.left ||
        event.clientX > rect.right ||
        event.clientY < rect.top ||
        event.clientY > rect.bottom
      )
        dialog.close();
    }
  });
});
$("overlay-action").addEventListener("click", togglePlay);
$("pause-button").addEventListener("click", togglePlay);
$("sound-button").addEventListener("click", () => {
  setSound(!preferences.sound);
  tone(440);
});
$("restart-button").addEventListener("click", () => {
  if (game.status === "ready" || game.status === "over") newGame();
  else openDialog("restart-dialog");
});
$("confirm-restart").addEventListener("click", () => {
  resumeAfterDialog = false;
  newGame();
  $("restart-dialog").close();
});
$("settings-button").addEventListener("click", () => {
  $("start-level").value = String(preferences.level);
  $("ghost-setting").checked = preferences.ghost;
  $("sound-setting").checked = preferences.sound;
  openDialog("settings-dialog");
});
$("settings-form").addEventListener("submit", (event) => {
  event.preventDefault();
  preferences.level = Number($("start-level").value);
  preferences.ghost = $("ghost-setting").checked;
  setSound($("sound-setting").checked);
  // Before the first move, the selected starting level can apply immediately.
  if (game.status === "ready") {
    game = new Game({ startLevel: preferences.level });
    drawPreviews();
  }
  $("settings-dialog").close();
  update();
});
$("scores-button").addEventListener("click", () => {
  const list = $("scores-list");
  list.replaceChildren();
  if (!scores.length) {
    const empty = document.createElement("p");
    empty.className = "empty-scores";
    empty.textContent =
      "A blank canvas. Finish your first game and your high scores will appear here.";
    list.append(empty);
  } else
    scores.forEach((entry, i) => {
      const li = document.createElement("li"),
        rank = document.createElement("span"),
        date = document.createElement("time"),
        score = document.createElement("strong");
      rank.className = "rank";
      rank.textContent = String(i + 1).padStart(2, "0");
      date.dateTime = entry.date;
      date.textContent = new Date(entry.date).toLocaleDateString(undefined, {
        month: "short",
        day: "numeric",
        year: "numeric",
      });
      score.textContent = entry.score.toLocaleString();
      li.append(rank, date, score);
      list.append(li);
    });
  openDialog("scores-dialog");
});
const keyActions = {
  ArrowLeft: "left",
  ArrowRight: "right",
  ArrowDown: "down",
  ArrowUp: "rotate",
  KeyX: "rotate",
  KeyZ: "counter",
  Space: "drop",
  Enter: "drop",
  KeyC: "hold",
  ShiftLeft: "hold",
  ShiftRight: "hold",
};
document.addEventListener("keydown", (event) => {
  if (
    document.querySelector("dialog[open]") ||
    /^(INPUT|SELECT|TEXTAREA|A)$/.test(event.target.tagName)
  )
    return;
  if (
    event.target.tagName === "BUTTON" &&
    ["Enter", "Space"].includes(event.code)
  )
    return;
  if (["KeyP", "Escape"].includes(event.code)) {
    event.preventDefault();
    if (!event.repeat) togglePlay();
    return;
  }
  if (!keyActions[event.code]) return;
  event.preventDefault();
  if (event.repeat) return;
  if (
    (event.code === "Enter" || event.code === "Space") &&
    game.status !== "running"
  ) {
    togglePlay();
    return;
  }
  const name = keyActions[event.code];
  action(name);
  if (["left", "right", "down"].includes(name))
    heldKeys.set(event.code, { name, elapsed: 0, next: 150 });
});
document.addEventListener("keyup", (event) => heldKeys.delete(event.code));
document.querySelectorAll("[data-action]").forEach((button) => {
  const stop = (event) => {
    heldKeys.delete("touch-" + event.pointerId);
  };
  button.addEventListener("pointerdown", (event) => {
    event.preventDefault();
    button.setPointerCapture(event.pointerId);
    const name = button.dataset.action;
    action(name);
    if (["left", "right", "down"].includes(name))
      heldKeys.set("touch-" + event.pointerId, { name, elapsed: 0, next: 150 });
  });
  ["pointerup", "pointercancel", "lostpointercapture"].forEach((name) =>
    button.addEventListener(name, stop),
  );
  button.addEventListener("click", (event) => {
    if (event.detail === 0) action(button.dataset.action);
  });
});
function autoPause() {
  game.pause();
  heldKeys.clear();
  update();
}
window.addEventListener("blur", autoPause);
document.addEventListener("visibilitychange", () => {
  if (document.hidden) {
    resumeAfterDialog = false;
    autoPause();
  }
});
setSound(preferences.sound);
drawPreviews();
update();
let last = performance.now();
function frame(now) {
  const dt = Math.min(now - last, 100);
  last = now;
  if (game.status === "running") {
    heldKeys.forEach((key) => {
      key.elapsed += dt;
      while (key.elapsed >= key.next) {
        action(key.name);
        key.next += key.name === "down" ? 35 : 50;
      }
    });
    game.tick(dt);
    update();
  }
  draw();
  requestAnimationFrame(frame);
}
requestAnimationFrame(frame);
