# Development status — booking persistence milestone

## Delivered in foundation PR #1

- [x] Monorepo, Spring Boot 4.1.2, Next.js Aqua Frost, PostgreSQL/Flyway, core Java pricing/date algorithms.
- [x] Read-only package catalog, preview-only permission-filtered navigation, CI.

## New in stacked milestone PR #2

- [x] JPA entities/repositories for guests, rooms, packages, reservations, invoices.
- [x] Flyway V2 invoice snapshots and explicit cancellation refund obligation.
- [x] Transactional room management, guest registration and booking service operations.
- [x] Row-level room locking plus database overlap exclusion constraint.
- [x] Public read-only date/type room-availability API.
- [x] Protected routes for guest records, room CRUD, reservation search/cancel and invoices.
- [x] Cached reference package catalog served from database rather than hardcoded values.
- [x] PostgreSQL integration tests for booking lifecycle and financial snapshots.
- [x] Privileged routes intentionally remain denied until real JWT and ABAC are added.

## Not yet implemented

- [ ] JWT login/refresh, registration ownership, DB-driven RBAC/ABAC, audit and session invalidation.
- [ ] Authenticated and accessible privileged REST operations (currently denied by design).
- [ ] Authenticated frontend guest registration, booking/cancellation and admin room forms.
- [ ] Real payment capture or refund transfer (not claimed).
- [ ] Full coursework executed test report, lecturer-approved assumptions and ID-prefixed UML.

## Next phase

Implement JWT authentication and database-managed roles/permissions with resource ownership before exposing sensitive endpoints. Then connect front-end workflows, add security tests and admin navigation configuration. The original coursework remains the priority; do not expand into unrelated hotel features.

## Current assumptions

Read `docs/ASSUMPTIONS.md`. All timing, extra-guest billing and tax interpretations remain provisional.

## New in stacked PR #3

- [x] PostgreSQL identity accounts, roles, permission grants, allowlisted navigation metadata and configuration revision.
- [x] Guest login/register with BCrypt + short-lived signed JWT (15 min).
- [x] JWT issuer/audience/token-version validation and immediate DB grant re-evaluation.
- [x] Server-enforced guest and reservation ownership checks, admin role grants and navigation editor APIs.
- [x] Aqua Frost login/register, private workspace, dynamic sidebar and admin access-control interface.
- [x] Next.js same-origin BFF session handlers storing JWT only in HttpOnly SameSite cookies.
- [x] PostgreSQL-backed HTTP security tests for unauthenticated, forbidden, cross-guest and revoked sessions.

## Remaining priorities after PR #3

- [ ] Booking creation and invoice viewing UX for authenticated guests and hotel administrators.
- [ ] Guest self-booking pending/payment-confirmation workflow without fake advance capture.
- [ ] Real admin room CRUD screens, complete booking and cancellation flows.
- [ ] CI browser E2E coverage, audit logs, MFA, password reset and rate limiting.
- [ ] University-approved assumptions and final UML, code listing and test report.

**Security limitation:** This coursework implementation must not be publicly exposed before anti-abuse and operational hardening.
