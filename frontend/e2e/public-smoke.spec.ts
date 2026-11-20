import { test, expect } from "@playwright/test";
import { captureUiEvidence } from "./helpers/evidence";

test("public catalog, accessible navigation and glassmorphism theme", async ({ page }, info) => {
  await page.goto("/");
  await expect(page.getByRole("heading", { name: /Thoughtful stays/i })).toBeVisible();
  await expect(page.getByRole("heading", { name: /Room collections/i })).toBeVisible();
  await captureUiEvidence(page, "01-public-catalog-light", info);
  await expect(page.getByText("Reservations, customer login, dynamic admin navigation and invoices are under development.")).toHaveCount(0);

  // Theme switching stays supported on mobile and desktop and remains accessible.
  await page.getByRole("button", { name: "Switch to dark theme" }).click();
  await expect(page.locator("html")).toHaveAttribute("data-theme", "dark");
  await captureUiEvidence(page, "02-public-catalog-dark", info);
  await page.getByRole("button", { name: "Switch to light theme" }).click();
  await expect(page.locator("html")).toHaveAttribute("data-theme", "light");

  if (info.project.name === "mobile-chromium") {
    await page.getByRole("button", { name: "Open menu" }).click();
    await expect(page.getByRole("navigation")).toBeVisible();
    await captureUiEvidence(page, "03-mobile-navigation", info);
    await page.getByRole("button", { name: "Close menu", exact: true }).last().click();
  }
  await page.goto("/availability");
  await expect(page.getByRole("heading", { name: /Find your next/i })).toBeVisible();
  await expect(page.getByRole("button", { name: "Find rooms" })).toBeVisible();
  await captureUiEvidence(page, "04-availability-search", info);
  if (info.project.name === "mobile-chromium") {
    const overflow = await page.evaluate(() =>
      Math.max(document.documentElement.scrollWidth, document.body.scrollWidth) - window.innerWidth);
    expect(overflow, "Mobile availability must not cause horizontal page overflow").toBeLessThanOrEqual(2);
  }
});

test("login uses keyboard-accessible inputs and no staff operations are public", async ({ page }, info) => {
  await page.goto("/login");
  await expect(page.getByRole("heading", { name: "Sign in" })).toBeVisible();
  await expect(page.getByLabel("Email address")).toBeVisible();
  await expect(page.getByLabel("Password")).toBeVisible();
  await captureUiEvidence(page, "05-login-form", info);
  await page.goto("/admin/rooms");
  await expect(page).toHaveURL(/\/login/);
});

test("keyboard skip link and mobile drawer focus return", async ({ page }, info) => {
  await page.goto("/");
  await page.keyboard.press("Tab");
  const skip = page.getByRole("link", { name: "Skip to main content" });
  await expect(skip).toBeFocused();
  await page.keyboard.press("Enter");
  await expect(page.locator("#main-content")).toBeFocused();

  if (info.project.name === "mobile-chromium") {
    const opener = page.getByRole("button", { name: "Open menu" });
    await opener.click();
    await expect(opener).toHaveAttribute("aria-expanded", "true");
    const drawer = page.getByRole("dialog", { name: "Main navigation" });
    await expect(drawer).toBeVisible();
    await expect(page.getByRole("button", { name: "Close menu", exact: true })).toBeFocused();
    await page.keyboard.press("Shift+Tab");
    await expect(drawer.locator("a").last()).toBeFocused();
    await page.keyboard.press("Tab");
    await expect(page.getByRole("button", { name: "Close menu", exact: true })).toBeFocused();
    await page.keyboard.press("Escape");
    await expect(opener).toHaveAttribute("aria-expanded", "false");
    await expect(opener).toBeFocused();
    await expect(drawer).toBeHidden();
  }
});

test("selected visual theme survives navigation and browser reload", async ({ page }) => {
  await page.goto("/");
  await page.getByRole("button", { name: "Switch to dark theme" }).click();
  await expect(page.locator("html")).toHaveAttribute("data-theme", "dark");

  await page.goto("/availability");
  await expect(page.locator("html")).toHaveAttribute("data-theme", "dark");
  await expect(page.getByRole("button", { name: "Switch to light theme" })).toBeVisible();

  await page.reload();
  await expect(page.locator("html")).toHaveAttribute("data-theme", "dark");

  await page.getByRole("button", { name: "Switch to light theme" }).click();
  await expect(page.locator("html")).toHaveAttribute("data-theme", "light");
  await page.goto("/");
  await expect(page.locator("html")).toHaveAttribute("data-theme", "light");
});

test("mobile header stays inside its container at 320, 375 and 393 pixels", async ({ page }) => {
  for (const width of [320,375,393]) {
    await page.setViewportSize({width,height:844});
    await page.goto("/availability");
    const compact = page.locator(".topbar-title-compact");
    await expect(compact).toBeVisible();
    const bounds = await page.locator(".topbar").evaluate(header => {
      const h=header.getBoundingClientRect();
      const title=header.querySelector(".topbar-title")!.getBoundingClientRect();
      const actions=header.querySelector(".topbar-actions")!.getBoundingClientRect();
      return {
        headerTop:h.top, headerBottom:h.bottom, headerRight:h.right,
        titleTop:title.top, titleBottom:title.bottom, actionsRight:actions.right,
        documentWidth:document.documentElement.scrollWidth,
        viewportWidth:document.documentElement.clientWidth
      };
    });
    expect(bounds.titleTop).toBeGreaterThanOrEqual(bounds.headerTop-2);
    expect(bounds.titleBottom).toBeLessThanOrEqual(bounds.headerBottom+2);
    expect(bounds.actionsRight).toBeLessThanOrEqual(bounds.headerRight+2);
    expect(bounds.documentWidth-bounds.viewportWidth).toBeLessThanOrEqual(2);
  }
});
