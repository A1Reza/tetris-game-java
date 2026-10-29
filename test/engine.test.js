import test from "node:test";
import assert from "node:assert/strict";
import { Game, SHAPES, rotated } from "../web/engine.js";

function playing(options) {
  const game = new Game(options);
  game.start();
  return game;
}
function piece(game, type, x, y, rotation = 0) {
  let matrix = SHAPES[type].map((row) => [...row]);
  for (let i = 0; i < rotation; i++) matrix = rotated(matrix);
  game.active = { type, matrix, x, y, rotation };
}

test("every bag contains each of the seven pieces exactly once", () => {
  const game = playing({ random: () => 0.42 });
  const sequence = [game.active.type];
  for (let i = 0; i < 27; i++) {
    game.spawn();
    sequence.push(game.active.type);
  }
  for (let i = 0; i < 28; i += 7)
    assert.equal(new Set(sequence.slice(i, i + 7)).size, 7);
});
test("all tetrominoes contain four blocks", () => {
  for (const shape of Object.values(SHAPES))
    assert.equal(shape.flat().filter(Boolean).length, 4);
});
test("clockwise and counterclockwise rotations are inverses", () => {
  for (const shape of Object.values(SHAPES)) {
    assert.deepEqual(rotated(rotated(shape), -1), shape);
    assert.deepEqual(rotated(rotated(rotated(rotated(shape)))), shape);
  }
});
test("pause and resume preserve the active piece, gravity progress, queue, and board", () => {
  const game = playing();
  game.tick(300);
  game.move(-1);
  const before = structuredClone({
    active: game.active,
    board: game.board,
    queue: game.queue,
    fallTime: game.fallTime,
  });
  game.pause();
  game.tick(2000);
  assert.equal(game.move(1), false);
  assert.equal(game.rotate(), false);
  assert.equal(game.hardDrop(), false);
  assert.equal(game.hold(), false);
  game.start();
  assert.deepEqual(
    {
      active: game.active,
      board: game.board,
      queue: game.queue,
      fallTime: game.fallTime,
    },
    before,
  );
});
test("left and right walls reject moves without changing position", () => {
  const game = playing();
  piece(game, "O", 0, 5);
  assert.equal(game.move(-1), false);
  assert.equal(game.active.x, 0);
  piece(game, "O", 8, 5);
  assert.equal(game.move(1), false);
  assert.equal(game.active.x, 8);
});
test("settled blocks prevent movement and falling", () => {
  const game = playing();
  piece(game, "O", 3, 16);
  game.board[18][3] = "I";
  game.board[16][5] = "I";
  assert.equal(game.down(), false);
  assert.equal(game.move(1), false);
});
test("hard drop immediately locks exactly one piece and spawns its successor", () => {
  const game = playing();
  piece(game, "O", 4, 0);
  const next = game.queue[0];
  assert.equal(game.hardDrop(), true);
  assert.equal(game.pieces, 1);
  assert.equal(game.active.type, next);
  assert.equal(game.active.y, 0);
  assert.equal(game.board.flat().filter(Boolean).length, 4);
  assert.equal(game.score, 36);
});
test("hard drop also locks a piece already on the floor", () => {
  const game = playing();
  piece(game, "O", 4, 18);
  game.hardDrop();
  assert.equal(game.pieces, 1);
  assert.equal(game.score, 0);
});
test("ghost finds the landing row without mutating game state", () => {
  const game = playing();
  piece(game, "O", 4, 1);
  game.board[15][4] = "J";
  const before = structuredClone(game.active);
  assert.equal(game.ghostY(), 13);
  assert.deepEqual(game.active, before);
});
test("soft drop awards one point per successful cell", () => {
  const game = playing();
  piece(game, "O", 4, 17);
  assert.equal(game.down(true), true);
  assert.equal(game.down(true), false);
  assert.equal(game.score, 1);
});
test("hold is allowed once per piece and restores the held piece in spawn orientation", () => {
  const game = playing();
  piece(game, "T", 2, 7);
  game.rotate();
  assert.equal(game.hold(), true);
  assert.equal(game.held, "T");
  assert.equal(game.hold(), false);
  game.hardDrop();
  assert.equal(game.hold(), true);
  assert.equal(game.active.type, "T");
  assert.equal(game.active.y, 0);
  assert.equal(game.active.rotation, 0);
  assert.deepEqual(game.active.matrix, SHAPES.T);
});
test("single-line clear shifts the stack down and keeps a 20-row independent grid", () => {
  const game = playing();
  game.board[19].fill("J");
  game.board[19][4] = game.board[19][5] = null;
  game.board[17][1] = "T";
  piece(game, "O", 4, 18);
  game.hardDrop();
  assert.equal(game.lines, 1);
  assert.equal(game.score, 100);
  assert.equal(game.board[18][1], "T");
  assert.equal(game.board.length, 20);
  game.board[0][0] = "J";
  assert.equal(game.board[1][0], null);
});
test("four adjacent lines clear together and award a level-scaled Tetris bonus", () => {
  const game = playing({ startLevel: 2 });
  for (let y = 16; y < 20; y++) {
    game.board[y].fill("L");
    game.board[y][4] = null;
  }
  piece(game, "I", 2, 16, 1);
  game.hardDrop();
  assert.equal(game.lines, 4);
  assert.equal(game.lastClear, 4);
  assert.equal(game.score, 1600);
  assert.equal(game.board.flat().filter(Boolean).length, 0);
});
test("level is based on cleared lines, and gravity accelerates at a level boundary", () => {
  const game = playing();
  game.lines = 9;
  const interval = game.interval;
  game.board[19].fill("J");
  game.board[19][4] = game.board[19][5] = null;
  piece(game, "O", 4, 18);
  game.hardDrop();
  assert.equal(game.level, 2);
  assert.equal(game.score, 100);
  assert.ok(game.interval < interval);
});
test("drop points cannot increase the level", () => {
  const game = playing();
  game.score = 10000;
  game.hardDrop();
  assert.equal(game.level, 1);
});
test("spawn collision ends the game without overwriting the existing stack", () => {
  const game = playing();
  game.board[0].fill("Z");
  game.board[1].fill("Z");
  const before = structuredClone(game.board);
  game.spawn("T");
  assert.equal(game.status, "over");
  assert.deepEqual(game.board, before);
  game.start();
  assert.equal(game.status, "over");
});
test("hold detects a blocked spawn", () => {
  const game = playing();
  piece(game, "O", 0, 10);
  game.held = "T";
  game.board[0][4] = "Z";
  game.hold();
  assert.equal(game.status, "over");
});
test("a piece locked above the skyline ends the game", () => {
  const game = playing();
  piece(game, "O", 4, -1);
  game.lock();
  assert.equal(game.status, "over");
});
test("I piece can kick away from the left wall when rotating", () => {
  const game = playing();
  piece(game, "I", -2, 5, 1);
  assert.ok(game.fits(game.active));
  assert.equal(game.rotate(), true);
  assert.ok(game.fits(game.active));
  assert.equal(game.active.rotation, 2);
});
test("T piece can kick off the floor when rotating", () => {
  const game = playing();
  piece(game, "T", 3, 18);
  assert.ok(game.fits(game.active));
  assert.equal(game.rotate(), true);
  assert.ok(game.fits(game.active));
  assert.equal(game.active.y, 17);
});
test("blocked rotation rolls back position, matrix, and orientation", () => {
  const game = playing();
  piece(game, "T", 3, 5);
  game.board.forEach((row) => row.fill("Z"));
  game.active.matrix.forEach((row, y) =>
    row.forEach((filled, x) => {
      if (filled) game.board[y + 5][x + 3] = null;
    }),
  );
  const before = structuredClone(game.active);
  assert.equal(game.rotate(), false);
  assert.deepEqual(game.active, before);
});
test("gravity uses a lock delay, and locks without player input", () => {
  const game = playing();
  piece(game, "O", 4, 18);
  game.tick(400);
  assert.equal(game.pieces, 0);
  game.tick(60);
  assert.equal(game.pieces, 1);
});
test("grounded movement cannot reset the lock delay indefinitely", () => {
  const game = playing();
  piece(game, "O", 4, 18);
  for (let i = 0; i < 16; i++) game.move(i % 2 ? -1 : 1);
  assert.equal(game.lockResets, 15);
  game.tick(460);
  assert.equal(game.pieces, 1);
});
test("invalid elapsed time cannot corrupt gravity", () => {
  const game = playing();
  const before = structuredClone(game.active);
  [NaN, Infinity, -100, 0].forEach((ms) => game.tick(ms));
  assert.deepEqual(game.active, before);
});
