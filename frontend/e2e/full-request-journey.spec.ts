import { test, expect } from "@playwright/test";
import { captureUiEvidence } from "./helpers/evidence";

test("real guest request -> staff approval -> confirmed invoice", async ({ page }, info) => {
  test.skip(info.project.name === "mobile-chromium", "Full transaction is covered on desktop; mobile layout uses smoke checks.");
  const roomId = "QA-" + Math.random().toString(36).slice(2, 9).toUpperCase();
  const guestEmail = `guest-${crypto.randomUUID()}@example.org`;
  const guestPassword = "TestGuestStrong!123456";

  // Bootstrap administrator exists only in CI via test-scoped environment variables.
  await page.goto("/login");
  await page.getByLabel("Email address").fill("e2e-admin@smartstay.test");
  await page.getByLabel("Password").fill(process.env.SMARTSTAY_BOOTSTRAP_ADMIN_PASSWORD ?? "");
  const loginResponse = page.waitForResponse(r => r.url().includes("/api/session/login") && r.request().method() === "POST");
  await page.getByRole("button", { name: /^Sign in$/ }).click();
  expect((await loginResponse).status()).toBe(200);
  await expect(page).toHaveURL(/\/workspace/);

  // Create an actual PostgreSQL room through the shared admin inventory form.
  await page.goto("/admin/rooms");
  await page.locator("#new-room-id").fill(roomId);
  await page.locator("#new-capacity").fill("2");
  await page.getByRole("button", { name: /Add room/ }).click();
  await expect(page).toHaveURL(/status=created/);
  await expect(page.getByRole("cell", { name: roomId })).toBeVisible();
  await captureUiEvidence(page, "06-admin-room-inventory", info);

  // Sign out, register a new guest and confirm the dynamic sidebar is permission-scoped.
  await page.getByRole("button", { name: "Sign out" }).click();
  await page.goto("/register");
  await captureUiEvidence(page, "07-guest-registration", info);
  await page.getByLabel("Full name").fill("Browser QA Guest");
  await page.getByLabel("NIC or passport number").fill("QA" + crypto.randomUUID().replaceAll("-", "").slice(0, 24));
  await page.getByLabel("Contact number").fill("+94770001122");
  await page.getByLabel("Email address").fill(guestEmail);
  await page.getByLabel(/Password \(at least/).fill(guestPassword);
  await page.getByRole("button", { name: /Create account/ }).click();
  await expect(page).toHaveURL(/\/workspace/);
  await expect(page.getByRole("link", { name: "My booking requests" })).toBeVisible();
  await expect(page.getByRole("link", { name: "Room inventory" })).toHaveCount(0);

  // Public search preserves the authenticated sidebar and selected room/dates.
  await page.goto("/availability?checkIn=2030-09-10&checkOut=2030-09-12");
  await expect(page.getByRole("link", { name: "My requests" })).toBeVisible();
  const card = page.locator(".availability-room").filter({ hasText: roomId });
  await expect(card).toBeVisible();
  await card.getByRole("link", { name: "Request this stay" }).click();
  await expect(page.getByRole("heading", { name: "Request your stay" })).toBeVisible();
  await captureUiEvidence(page, "08-guest-request-form", info);
  await page.getByRole("button", { name: "Submit booking request" }).click();
  await expect(page).toHaveURL(/\/my\/booking-requests\?status=requested/);
  await expect(page.locator("tr").filter({hasText: roomId}).getByText("Pending")).toBeVisible();
  await captureUiEvidence(page, "09-guest-pending-requests", info);

  await page.getByRole("button", { name: "Sign out" }).click();
  await page.goto("/login");
  await page.getByLabel("Email address").fill("e2e-admin@smartstay.test");
  await page.getByLabel("Password").fill(process.env.SMARTSTAY_BOOTSTRAP_ADMIN_PASSWORD ?? "");
  await page.getByRole("button", { name: /^Sign in$/ }).click();
  await expect(page).toHaveURL(/\/workspace/);
  await page.goto("/admin/booking-requests");
  const row = page.locator("tr").filter({hasText: roomId});
  await expect(row).toBeVisible();
  await captureUiEvidence(page, "10-staff-approve-reject-queue", info);
  await row.getByRole("checkbox").check();
  page.once("dialog", dialog => dialog.accept());
  await row.getByRole("button", { name: "Approve" }).click();
  await expect(page).toHaveURL(/feedback=approved/);
  await page.getByRole("combobox", { name: "Filter booking request status" }).selectOption("APPROVED");
  await page.getByRole("button", { name: "Apply filter" }).click();
  await expect(page.locator("tr").filter({hasText: roomId}).getByText("Approved")).toBeVisible();
  await captureUiEvidence(page, "11-staff-approved-request", info);

  await page.getByRole("button", { name: "Sign out" }).click();
  await page.goto("/login");
  await page.getByLabel("Email address").fill(guestEmail);
  await page.getByLabel("Password").fill(guestPassword);
  await page.getByRole("button", { name: /^Sign in$/ }).click();
  await expect(page).toHaveURL(/\/workspace/);
  await page.goto("/my/booking-requests");
  await expect(page.locator("tr").filter({hasText: roomId}).getByText("Approved")).toBeVisible();
  await page.locator("tr").filter({hasText: roomId}).getByRole("link", { name: "View invoice" }).click();
  await expect(page.locator(".ss-invoice-paper")).toBeVisible();
  await expect(page.getByText(roomId).first()).toBeVisible();
  await captureUiEvidence(page, "12-invoice-before-amendment", info);
  // Amend a confirmed stay without charging a second advance.
  await page.goto("/my/reservations");
  await page.locator("tr").filter({hasText:roomId}).getByRole("link",{name:"Modify stay"}).click();
  await expect(page.getByRole("heading",{name:"Modify reservation"})).toBeVisible();
  await captureUiEvidence(page, "13-reservation-modification", info);
  await page.locator("#checkOut").fill("2030-09-15");
  await page.getByRole("button",{name:"Save reservation changes"}).click();
  await expect(page).toHaveURL(/\/invoice\?updated=1/);
  await expect(page.getByText("5 night(s) booked")).toBeVisible();
  await captureUiEvidence(page, "14-invoice-after-amendment", info);

  await page.emulateMedia({media:"print"});
  await expect(page.locator(".ss-invoice-paper")).toBeVisible();
});
