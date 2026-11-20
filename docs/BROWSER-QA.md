# Browser acceptance tests

Milestone 6 adds Playwright 1.65.1 (Chromium), run against real Next.js, Spring Boot and PostgreSQL in GitHub Actions.

## Coverage

- Home package rendering and desktop/mobile sidebar behavior.
- Shared Aqua Frost / Spatial Glass theme toggle and accessible controls.
- Protected route redirects to login.
- Staff login and **real database room creation**.
- Guest registration, policy-filtered sidebar and public availability.
- Guest creation of unpaid pending request, staff review with confirmed advance, and final guest invoice.
- A4 print-media invoice remains visible.

The Java integration suite continues to test booking rules, payment-claim boundary, overlap, ownership and 401/403 authorization. Browser tests complement those tests; they do not replace them.

## Run locally

1. Start PostgreSQL 18 with the project's Docker Compose database service.
2. Run the backend with `DB_URL`, `DB_PASSWORD` and a 32-byte Base64 JWT secret; provision a **test-only** admin with:
   `SMARTSTAY_BOOTSTRAP_ADMIN_EMAIL=e2e-admin@smartstay.test`
   and `SMARTSTAY_BOOTSTRAP_ADMIN_PASSWORD=TestOnlySuperSecurePassword_1234!`.
   Remove bootstrap settings afterwards. Do not use these test credentials for any nonlocal environment.
3. From `frontend/`, run `npm install` and `npx playwright install chromium`.
4. Run `npm run build && npm run start`, then in another shell `npm run test:e2e`.

The CI browser job provisions isolated ephemeral Postgres for each run. No real guest data or online payments are used. Failure screenshots/traces are uploaded as short-retention CI artifacts.

## Current outstanding QA

Manual visual review and keyboard/screen-reader accessibility audit, mobile table overflow and A4 print-preview inspection remain separate release checks.

## Real UI screenshots as CI evidence

The Playwright suite now captures **actual browser screenshots** from the isolated test instance in both Aqua Frost and Spatial Glass. It collects public/catalog/login/mobile navigation, inventory, guest request, staff approval queue, invoice before and after modification, and reservation amendment screens.

After the `browser-e2e` job succeeds, download the GitHub Actions artifact `smartstay-ui-screenshots-<commit>`. These screenshots are **not Stitch mockups**, and do not contain real hotel guests: CI uses generated synthetic test guest accounts and a temporary PostgreSQL database. They are valid execution evidence, but the author must still inspect and select legible images for the coursework document. Retention is 14 days.

A separate mobile browser assertion checks the availability screen for page-level horizontal overflow. Broad screen-reader verification and final print screenshot inspection remain manual acceptance steps.

## Milestone 10: Keyboard, mobile and A4 print regression checks

- AppShell includes a visible-on-focus skip-to-content anchor, and the main content target accepts programmatic focus.
- When the mobile menu opens, focus goes into its labelled dialog; Escape closes it, Tab/Shift+Tab cycle within the drawer, and focus returns to the menu trigger.
- Focus rings cover links, buttons, inputs, selects, textareas and editable disclosure summaries. The offscreen mobile sidebar is not keyboard-focusable when closed.
- Playwright covers these interactions on desktop and Pixel 7 Chromium.
- The full browser test verifies the printed invoice hides sidebar and print controls, checks for clipped billing rows, and **generates an A4 PDF from the real app**.
- `test-results/evidence/16-invoice-A4.pdf` is included with the actual-browser screenshots in the workflow UI-evidence artifact.
- The guest reservation ledger is checked for document-level horizontal overflow at a 390px mobile width.

These tests are automated smoke/regression coverage. Human review of the generated PDF pages, keyboard/screen-reader workflow, contrast and the final lecturer-required screenshots is still necessary.

## Persistent theme acceptance tests

`public-smoke.spec.ts` checks that a user switching between Aqua Frost light and Spatial Glass dark keeps their selection when navigating from the catalog to availability, reloading and returning. The preference is a theme-only localStorage key. An unset preference follows `prefers-color-scheme`. Browser storage failures do not break the visual toggle. No authentication tokens are placed in browser storage.

## Mobile topbar and invoice wording refinement

The earlier Pixel 7 screenshot collection revealed a genuine visual issue: the long workspace title wrapped beyond the 70px mobile topbar. The mobile view now uses a compact SmartStay title, smaller spacing and hidden eyebrow at 660px or less, with browser geometry checks at **320, 375 and 393px**. Desktop retains the full heading.

Invoice wording now uses **Guest invoice / folio**, avoiding the impression that the educational example is an issued tax invoice. The disclaimer about electronic payments and formal tax receipts remains visible.
