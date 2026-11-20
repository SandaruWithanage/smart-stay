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
