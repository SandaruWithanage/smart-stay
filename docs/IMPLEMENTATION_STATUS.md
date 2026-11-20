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

## Milestone 7 — Reservation amendments (proposed feature)

- [x] Role-scoped, ownership-protected same-room reservation editing.
- [x] Provisional 24-hour original check-in cutoff; 48-hour notice for new check-in date.
- [x] Transactional room/booking locking and overlap revalidation.
- [x] Invoice recalculation with original advance preserved; append-only before/after history.
- [x] Shared reusable React modification form and staff/guest edit links.
- [x] Java domain, persistence and HTTP coverage for cutoff, money, conflicts and ownership.
- [ ] Lecturer confirmation of editable scope, time and refund treatment.

## Milestone 8 — Assessment deliverables (report preparation)

- [x] UML class-diagram source with domain/service associations and explicit Kingston student-ID placeholder.
- [x] OOP/data-structure/algorithm design rationale tied to the implemented Java code.
- [x] Test case matrix linked to actual JUnit/Playwright test names and verified CI evidence.
- [x] Mandatory hand-in checklist and real UI screenshot capture plan.
- [x] Windows Git-tracked source ZIP and source-code appendix export scripts.
- [ ] Insert exact student ID into every UML class name and export readable image.
- [ ] Capture and review genuine UI/code screenshots.
- [ ] Integrate diagram, listings and observed test evidence in the final single report.
- [ ] Confirm final moderated brief, due date, academic integrity statement and lecturer assumptions.

## Milestone 9 — Browser screenshot evidence

- [x] CI-generated real browser screenshots for light/dark public pages, mobile navigation, room inventory, guest booking requests, staff approval queue and invoices.
- [x] Screenshot artifact stored in GitHub Actions for report selection (synthetic test data).
- [x] Mobile availability page horizontal-overflow assertion added to Playwright smoke tests.
- [ ] Confirm screenshots meet the final assessment's resolution/caption requirements and manually inspect the A4 print layout.

## Milestone 10 — Keyboard and A4 print QA

- [x] Skip link, focus-visible form fields, focus-managed mobile navigation and Escape closing.
- [x] Browser assertions for drawer keyboard cycle/focus restoration and printer-only invoice layout.
- [x] Real A4 PDF generated in CI from a synthetic guest's invoice.
- [x] Guest reservation mobile-width document-overflow assertion.
- [ ] Manually inspect the generated A4 PDF and review assistive-technology announcements.

## Milestone 11 — Persistent visual theme

- [x] Aqua Frost / Spatial Glass selection remembered across page navigation and reloads.
- [x] Fresh sessions use the operating system's preferred color scheme until manually overridden.
- [x] Theme-only browser storage, guarded against storage access failures.
- [x] Cross-tab storage changes synchronize visual preferences without exposing profile/session data.
- [x] Playwright regression coverage for visual preference on navigation and reload.
