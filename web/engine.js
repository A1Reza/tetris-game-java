// The game model has no browser dependencies; the same rules are exercised in Node.
export const COLORS = {
  I: "#6bcbd0",
  O: "#efc765",
  T: "#b49bd9",
  S: "#9bc58d",
  Z: "#e8887c",
  J: "#819fce",
  L: "#e9ac73",
};
export const SHAPES = {
  I: [
    [0, 0, 0, 0],
    [1, 1, 1, 1],
    [0, 0, 0, 0],
    [0, 0, 0, 0],
  ],
  O: [
    [1, 1],
    [1, 1],
  ],
  T: [
    [0, 1, 0],
    [1, 1, 1],
    [0, 0, 0],
  ],
  S: [
    [0, 1, 1],
    [1, 1, 0],
    [0, 0, 0],
  ],
  Z: [
    [1, 1, 0],
    [0, 1, 1],
    [0, 0, 0],
  ],
  J: [
    [1, 0, 0],
    [1, 1, 1],
    [0, 0, 0],
  ],
  L: [
    [0, 0, 1],
    [1, 1, 1],
    [0, 0, 0],
  ],
};
const NORMAL_KICKS = {
  "0>1": [
    [0, 0],
    [-1, 0],
    [-1, -1],
    [0, 2],
    [-1, 2],
  ],
  "1>0": [
    [0, 0],
    [1, 0],
    [1, 1],
    [0, -2],
    [1, -2],
  ],
  "1>2": [
    [0, 0],
    [1, 0],
    [1, 1],
    [0, -2],
    [1, -2],
  ],
  "2>1": [
    [0, 0],
    [-1, 0],
    [-1, -1],
    [0, 2],
    [-1, 2],
  ],
  "2>3": [
    [0, 0],
    [1, 0],
    [1, -1],
    [0, 2],
    [1, 2],
  ],
  "3>2": [
    [0, 0],
    [-1, 0],
    [-1, 1],
    [0, -2],
    [-1, -2],
  ],
  "3>0": [
    [0, 0],
    [-1, 0],
    [-1, 1],
    [0, -2],
    [-1, -2],
  ],
  "0>3": [
    [0, 0],
    [1, 0],
    [1, -1],
    [0, 2],
    [1, 2],
  ],
};
const I_KICKS = {
  "0>1": [
    [0, 0],
    [-2, 0],
    [1, 0],
    [-2, 1],
    [1, -2],
  ],
  "1>0": [
    [0, 0],
    [2, 0],
    [-1, 0],
    [2, -1],
    [-1, 2],
  ],
  "1>2": [
    [0, 0],
    [-1, 0],
    [2, 0],
    [-1, -2],
    [2, 1],
  ],
  "2>1": [
    [0, 0],
    [1, 0],
    [-2, 0],
    [1, 2],
    [-2, -1],
  ],
  "2>3": [
    [0, 0],
    [2, 0],
    [-1, 0],
    [2, -1],
    [-1, 2],
  ],
  "3>2": [
    [0, 0],
    [-2, 0],
    [1, 0],
    [-2, 1],
    [1, -2],
  ],
  "3>0": [
    [0, 0],
    [1, 0],
    [-2, 0],
    [1, 2],
    [-2, -1],
  ],
  "0>3": [
    [0, 0],
    [-1, 0],
    [2, 0],
    [-1, -2],
    [2, 1],
  ],
};
export function rotated(matrix, direction = 1) {
  const n = matrix.length;
  return matrix.map((row, y) =>
    row.map((_, x) =>
      direction === 1 ? matrix[n - 1 - x][y] : matrix[x][n - 1 - y],
    ),
  );
}
export class Game {
  constructor({ random = Math.random, startLevel = 1 } = {}) {
    this.random = random;
    this.startLevel = Math.max(1, Math.min(10, Number(startLevel) || 1));
    this.board = Array.from({ length: 20 }, () => Array(10).fill(null));
    this.queue = [];
    this.score = 0;
    this.lines = 0;
    this.level = this.startLevel;
    this.status = "ready";
    this.held = null;
    this.canHold = true;
    this.fallTime = 0;
    this.lockTime = 0;
    this.lockResets = 0;
    this.lastClear = 0;
    this.pieces = 0;
    this.refill();
    this.spawn();
  }
  get interval() {
    return Math.max(70, 900 * 0.8 ** (this.level - 1));
  }
  refill() {
    while (this.queue.length < 7) {
      const bag = Object.keys(SHAPES);
      for (let i = bag.length - 1; i > 0; i--) {
        const j = Math.floor(this.random() * (i + 1));
        [bag[i], bag[j]] = [bag[j], bag[i]];
      }
      this.queue.push(...bag);
    }
  }
  spawn(type) {
    this.refill();
    const name = type ?? this.queue.shift();
    this.refill();
    const matrix = SHAPES[name].map((row) => [...row]);
    this.active = {
      type: name,
      matrix,
      x: Math.floor((10 - matrix.length) / 2),
      y: 0,
      rotation: 0,
    };
    this.fallTime = this.lockTime = this.lockResets = 0;
    if (!this.fits(this.active)) this.status = "over";
  }
  fits(piece) {
    return piece.matrix.every((row, y) =>
      row.every((filled, x) => {
        if (!filled) return true;
        const bx = x + piece.x,
          by = y + piece.y;
        return bx >= 0 && bx < 10 && by < 20 && (by < 0 || !this.board[by][bx]);
      }),
    );
  }
  start() {
    if (this.status === "ready" || this.status === "paused")
      this.status = "running";
  }
  pause() {
    if (this.status === "running") this.status = "paused";
  }
  resetLock(wasGrounded) {
    if (wasGrounded && this.lockResets < 15) {
      this.lockTime = 0;
      this.lockResets++;
    }
  }
  move(dx) {
    if (this.status !== "running") return false;
    const grounded = !this.fits({ ...this.active, y: this.active.y + 1 });
    const candidate = { ...this.active, x: this.active.x + dx };
    if (!this.fits(candidate)) return false;
    this.active = candidate;
    this.resetLock(grounded);
    return true;
  }
  rotate(direction = 1) {
    if (this.status !== "running" || this.active.type === "O") return false;
    const from = this.active.rotation,
      to = (from + direction + 4) % 4;
    const matrix = rotated(this.active.matrix, direction);
    const kicks = (this.active.type === "I" ? I_KICKS : NORMAL_KICKS)[
      `${from}>${to}`
    ];
    const grounded = !this.fits({ ...this.active, y: this.active.y + 1 });
    for (const [dx, dy] of kicks) {
      const candidate = {
        ...this.active,
        matrix,
        rotation: to,
        x: this.active.x + dx,
        y: this.active.y + dy,
      };
      if (this.fits(candidate)) {
        this.active = candidate;
        this.resetLock(grounded);
        return true;
      }
    }
    return false;
  }
  down(manual = false) {
    if (this.status !== "running") return false;
    const candidate = { ...this.active, y: this.active.y + 1 };
    if (!this.fits(candidate)) return false;
    this.active = candidate;
    this.lockTime = 0;
    if (manual) this.score++;
    return true;
  }
  ghostY() {
    let y = this.active.y;
    while (this.fits({ ...this.active, y: y + 1 })) y++;
    return y;
  }
  hardDrop() {
    if (this.status !== "running") return false;
    const y = this.ghostY();
    this.score += (y - this.active.y) * 2;
    this.active.y = y;
    this.lock();
    return true;
  }
  hold() {
    if (this.status !== "running" || !this.canHold) return false;
    const outgoing = this.active.type;
    this.spawn(this.held ?? undefined);
    this.held = outgoing;
    this.canHold = false;
    return true;
  }
  lock() {
    let above = false;
    this.active.matrix.forEach((row, y) =>
      row.forEach((filled, x) => {
        if (!filled) return;
        const by = y + this.active.y;
        if (by < 0) above = true;
        else this.board[by][x + this.active.x] = this.active.type;
      }),
    );
    if (above) {
      this.status = "over";
      return;
    }
    const remaining = this.board.filter((row) => row.some((cell) => !cell));
    this.lastClear = 20 - remaining.length;
    this.score += [0, 100, 300, 500, 800][this.lastClear] * this.level;
    this.lines += this.lastClear;
    this.level = this.startLevel + Math.floor(this.lines / 10);
    this.board = [
      ...Array.from({ length: this.lastClear }, () => Array(10).fill(null)),
      ...remaining,
    ];
    this.pieces++;
    this.canHold = true;
    this.spawn();
  }
  tick(ms) {
    if (this.status !== "running" || !Number.isFinite(ms) || ms <= 0) return;
    // Small steps preserve gravity and lock delay even when rendering is slow.
    let remaining = Math.min(ms, 2000);
    while (remaining > 0 && this.status === "running") {
      const step = Math.min(remaining, 16);
      remaining -= step;
      this.fallTime += step;
      if (this.fallTime >= this.interval) {
        this.fallTime -= this.interval;
        this.down();
      }
      if (!this.fits({ ...this.active, y: this.active.y + 1 })) {
        this.lockTime += step;
        if (this.lockTime >= 450) this.lock();
      }
    }
  }
}
