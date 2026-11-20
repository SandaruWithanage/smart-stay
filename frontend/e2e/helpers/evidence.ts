import { mkdirSync } from "node:fs";
import { join } from "node:path";
import type { Page, TestInfo } from "@playwright/test";

/** Saved screenshots come from an actual isolated test database and browser session.
 * Never capture passwords, live hotel customer records or production admin panels.
 */
export async function captureUiEvidence(page: Page, name: string, info: TestInfo): Promise<void> {
  const safeName = name.toLowerCase().replace(/[^a-z0-9-]/g, "-").slice(0, 80);
  const folder = join(process.cwd(), "test-results", "evidence");
  mkdirSync(folder, { recursive: true });
  await page.screenshot({
    path: join(folder, `${safeName}-${info.project.name}.png`),
    fullPage: true,
    animations: "disabled"
  });
}
