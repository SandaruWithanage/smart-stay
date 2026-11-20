# SmartStay implementation status — Stitch workflows

## Completed and merged previously
- Spring Boot modular monolith, JPA/Flyway PostgreSQL, booking and billing algorithms.
- Guest JWT sign-in, PostgreSQL RBAC/ABAC ownership checks, dynamic navigation manifest.
- Aqua Frost glassmorphism frontend, private workspace and admin permission editor.

## Implemented in current feature branch
- [x] Flyway V5 navigation links to room inventory, reservation ledger, desk booking and own reservation history.
- [x] Server-side ownership-filtered `/api/me/reservations` and bounded staff `/api/admin/reservations`.
- [x] Shared UI primitives for panels, headings, metrics, filters, fields, badges and data tables.
- [x] Staff room inventory screen (create/edit/retire) using existing protected APIs.
- [x] Staff booking ledger with booking search and cancellation.
- [x] Desk reservation form for existing or new guest records, with explicit manual advance receipt confirmation.
- [x] Guest own reservation history (record IDs resolved by authenticated user; no user-supplied guest ID).
- [x] A4-friendly itemized invoice print view.
- [x] Light Aqua Frost and dark Spatial Glass operational theme tokens.
- [x] Integration test for guest-vs-desk reservation list scoping.
- [x] UX source-to-implementation mapping and common-component inventory.

## Still pending
- [ ] Verified guest self-booking/payment confirmation workflow (cannot fabricate advance receipt).
- [ ] Secure verified linking of pre-existing desk guest records to a newly registered login.
- [ ] Browser E2E testing across responsive screens, accessibility and print layout.
- [ ] Complete password recovery, email verification, MFA, rate limiting and audit-event persistence.
- [ ] Real checkout/folio settlement and housekeeping (out of coursework scope).
- [ ] Lecturer confirmations: room capacity, extra guest billing, 48h cutoff, check-in time, tax treatment and refund workflow.
- [ ] Final submitted UML class diagram (student-ID-prefixed), source listings and executed test report.

## Important operational notes

Admin must create room records first. Initial database has **no invented room inventory**; package catalog is seeded only. To see an existing guest's reservation in their private login, desk staff must use that user's linked guest ID. A separately registered desk guest has no automatic login linking yet.

This is coursework-local development, not ready for public Internet hosting. See `docs/SECURITY-SETUP.md` and `docs/UX-STITCH-REFERENCE.md`.

## Milestone 5 — Guest requests and semantic action colors (feature PR)

- [x] Reusable semantic success/approval and danger/destructive buttons, badges and feedback in both themes.
- [x] Aqua Frost and Spatial Glass color contrast tests and shared design tokens.
- [x] Flyway V6 booking request records, approval audit fields, permission grants and navigation.
- [x] Guest-request backend: submitted by current JWT identity, no payment or inventory lock.
- [x] Staff approval: request row lock, same transactional booking engine, actual-advance confirmation.
- [x] Staff rejection and own-request withdrawal without payment capture.
- [x] Guest request form/history and staff review queue using shared UI components.
- [x] Integration tests for cross-guest access, duplicate approval and request decisions.

Remain: public-deployment hardening, payment gateway, lecturer decision approvals, browser E2E and the final graded UML/report.

## Milestone 6 — UI consistency and browser E2E

- [x] Public catalog/availability shell renders authenticated navigation when signed in.
- [x] Stale “authentication coming soon” copy replaced with actual implemented features.
- [x] Playwright desktop/mobile browser checks added to CI.
- [x] Real PostgreSQL-backed guest-to-staff booking-request and invoice journey tested in Chromium.
- [x] A4 print-media invoice visibility is exercised.
- [ ] Additional manual accessibility and visual-regression checks.
