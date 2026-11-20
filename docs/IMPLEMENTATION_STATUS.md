# Development status — foundation milestone

## Implemented in this feature branch

- [x] GitHub monorepo, environment template, editor config, ignore rules.
- [x] Spring Boot modular backend bootstrap and read-only public room-package API.
- [x] PostgreSQL 18 Docker Compose and Flyway V1 schema with official package seeds.
- [x] Database exclusion constraint for confirmed reservation date ranges.
- [x] Pure Java stay interval, availability checking and booking timing rules.
- [x] Pure Java `BigDecimal` pricing with discount, tax, advance and refund-due lines.
- [x] JUnit tests for pricing and date boundaries.
- [x] Next.js responsive Aqua Frost app shell, accessible menus/theme toggle, room-package cards.
- [x] Typed safe route-key registry and permission-filtered **preview-only** navigation.
- [x] GitHub CI workflow to build/test the backend and typecheck/build the frontend.

## Not yet implemented (do not present as delivered)

- [ ] Guest registration/login, JWT creation/validation and password handling.
- [ ] Database-driven roles, permissions, resource-specific ABAC, revocation and audit.
- [ ] Authenticated server-provided UI manifest and administrator navigation editor.
- [ ] Room CRUD repository, room capacity approval and status management.
- [ ] Actual date-based search and booking transaction services exposed via REST.
- [ ] Advance recording/settlement and complete invoice persistence + print output.
- [ ] Cancellation endpoint and refund bookkeeping.
- [ ] UI forms for guest and administrator workflows.
- [ ] Cache integration for DB-backed reference data.
- [ ] Comprehensive 40-case verification evidence and final ID-prefixed UML/report.

## Next implementation slice

1. JPA mappings for room, guest, reservation, and invoice; repository tests with PostgreSQL.
2. Transactional availability search + reservation service, including concurrent booking integration test.
3. Room CRUD API and guest registration.
4. AuthN/AuthZ module; deny by default until permission and ownership enforcement is proven.
5. Wire real Next.js booking flow to authenticated backend.
6. Implement DB-managed UI manifest/permission editor after core hotel functionality.

## Review gate

- CI Maven tests and Next typecheck/build green.
- PostgreSQL V1 migration runs on empty database.
- Documentation distinguishes explicit brief rules from assumptions.
- No admin or customer-specific records exposed by unsecured APIs.
