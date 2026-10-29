import { test, expect } from "@playwright/test";

test("start, move, pause, resume, and hard drop through the UI", async ({
  page,
}) => {
  const errors = [];
  page.on("pageerror", (error) => errors.push(error.message));
  await page.goto("/");
  await expect(
    page.getByRole("heading", { name: "One more line." }),
  ).toBeVisible();
  await page.getByRole("button", { name: "Let’s play" }).click();
  await expect(page.locator("#game-status")).toHaveText("IN THE FLOW");
  const before = await page.evaluate(
    async () => (await import("/app.js")).game.active.x,
  );
  await page.keyboard.press("ArrowLeft");
  expect(
    await page.evaluate(async () => (await import("/app.js")).game.active.x),
  ).toBe(before - 1);
  await page.keyboard.press("p");
  await expect(page.locator("#overlay-title")).toHaveText(
    "No rush.We’ll wait.",
  );
  const paused = await page.evaluate(async () =>
    JSON.stringify((await import("/app.js")).game.active),
  );
  await page.keyboard.press("ArrowDown");
  expect(
    await page.evaluate(async () =>
      JSON.stringify((await import("/app.js")).game.active),
    ),
  ).toBe(paused);
  await page.keyboard.press("p");
  await page.keyboard.press("Space");
  expect(
    await page.evaluate(async () => (await import("/app.js")).game.pieces),
  ).toBe(1);
  expect(Number(await page.locator("#score").textContent())).toBeGreaterThan(0);
  expect(errors).toEqual([]);
});
test("settings persist and dialogs preserve the current game", async ({
  page,
}) => {
  await page.goto("/");
  await page.keyboard.press("Enter");
  await page.getByRole("button", { name: "Make it yours" }).click();
  await expect(page.locator("#settings-dialog")).toBeVisible();
  expect(
    await page.evaluate(async () => (await import("/app.js")).game.status),
  ).toBe("paused");
  await page.locator("#start-level").selectOption("4");
  await page.locator("#ghost-setting").uncheck();
  await page.getByRole("button", { name: "Save preferences" }).click();
  await expect(page.locator("#game-status")).toHaveText("IN THE FLOW");
  await page.reload();
  await expect(page.locator("#level")).toHaveText("04");
  await page.getByRole("button", { name: "Make it yours" }).click();
  await expect(page.locator("#ghost-setting")).not.toBeChecked();
  await page.keyboard.press("Escape");
  await expect(page.locator("#settings-dialog")).not.toBeVisible();
});
test("restart requires a choice and resets the game", async ({ page }) => {
  await page.goto("/");
  await page.keyboard.press("Enter");
  await page.keyboard.press("Space");
  await page.getByRole("button", { name: "Restart game", exact: true }).click();
  await expect(page.locator("#restart-dialog")).toBeVisible();
  await page
    .getByRole("dialog")
    .getByRole("button", { name: "Keep playing", exact: true })
    .click();
  expect(
    await page.evaluate(async () => (await import("/app.js")).game.pieces),
  ).toBe(1);
  await page.getByRole("button", { name: "Restart game", exact: true }).click();
  await page.getByRole("button", { name: "New game", exact: true }).click();
  await expect(page.locator("#score")).toHaveText("000000");
  await expect(page.locator("#game-status")).toHaveText("IN THE FLOW");
});
test("game over saves a score once, and storage survives reload", async ({
  page,
}) => {
  await page.goto("/");
  await page.keyboard.press("Enter");
  await page.evaluate(async () => {
    const { game } = await import("/app.js");
    game.score = 1234;
    game.lines = 12;
    game.active.y = game.ghostY();
    for (let y = 0; y < 2; y++)
      for (let x = 3; x < 7; x++) game.board[y][x] = "Z";
  });
  await page.keyboard.press("Space");
  await expect(page.locator("#game-status")).toHaveText("A GOOD RUN");
  await expect(page.locator("#best")).toHaveText("1,234");
  expect(
    await page.evaluate(
      () => JSON.parse(localStorage.getItem("tetris.scores")).length,
    ),
  ).toBe(1);
  await page.reload();
  await expect(page.locator("#best")).toHaveText("1,234");
  await page.getByRole("button", { name: "High scores" }).click();
  await expect(page.locator("#scores-list li")).toHaveCount(1);
  await expect(page.locator("#scores-list strong")).toHaveText("1,234");
});
test("layout fits the viewport and touch controls work on mobile", async ({
  page,
}, testInfo) => {
  await page.goto("/");
  expect(
    await page.evaluate(
      () => document.documentElement.scrollWidth <= innerWidth,
    ),
  ).toBe(true);
  if (testInfo.project.name !== "mobile") return;
  await page.getByRole("button", { name: "Let’s play" }).click();
  await page.getByRole("button", { name: "Hold piece", exact: true }).tap();
  expect(
    await page.evaluate(async () => (await import("/app.js")).game.held),
  ).not.toBeNull();
  await page.getByRole("button", { name: "Hard drop", exact: true }).tap();
  expect(
    await page.evaluate(async () => (await import("/app.js")).game.pieces),
  ).toBe(1);
});
test("unavailable or malformed storage does not prevent play", async ({
  page,
}) => {
  await page.addInitScript(() => {
    localStorage.setItem("tetris.preferences", "null");
    localStorage.setItem("tetris.scores", '{"invalid":true}');
  });
  await page.goto("/");
  await page.keyboard.press("Enter");
  await expect(page.locator("#game-status")).toHaveText("IN THE FLOW");
});
test("leaving the tab automatically pauses the game", async ({ page }) => {
  await page.goto("/");
  await page.keyboard.press("Enter");
  await page.evaluate(() => window.dispatchEvent(new Event("blur")));
  await expect(page.locator("#game-status")).toHaveText("TAKING A BREATHER");
});
