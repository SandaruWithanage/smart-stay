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
    await page.getByRole("button", { name: "Close menu" }).last().click();
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
