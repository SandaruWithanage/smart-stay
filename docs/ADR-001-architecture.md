# ADR-001 — SmartStay architecture foundation

**Status:** Accepted provisionally for implementation (20 Nov 2026)

**Context:** CI6115 coursework awards substantial marks for functional OOP implementation, UML, algorithms, data structures and test cases. User-selected solution uses a modern web frontend and a Java backend with dynamic RBAC/ABAC UI in later stages.

## Decisions

- One Spring Boot modular monolith with per-feature Java packages. Layers inside each feature: API/controller, service, domain, persistence.
- Next.js 16 + React 19 + TypeScript, responsive Aqua Frost glassmorphism using colors #E3FDFD, #CBF1F5, #A6E3E9 and #71C9CE.
- PostgreSQL 18 relational database, Flyway migrations, JPA mapping as modules progress.
- Java 21 compiler target (portable LTS baseline), runnable on newer compatible JDK including 25.
- Pure domain services for overlap, timing and billing, with isolated JUnit tests.
- Spring Security JWT and DB-configured RBAC with constrained typed ABAC in a later milestone; **no Passport.js**.
- Protected APIs deny access by default until actual authorization is implemented. Public package listing is read-only.
- Lightweight Spring Cache + Caffeine for low-risk reference data only. No final availability or allow/deny decisions from stale caches.
- Dynamic navigation from a backend-filtered typed manifest, mapped by frontend route keys from an allowlist; no executable UI code from DB.

## Deliberate constraints

- Not a microservice architecture.
- Does not require a standalone Redis cluster, external auth server, AI features or hotel integrations.
- Server authorization does not rely on hidden buttons or sidebar entries.
- Permission and reservation changes must be auditable and invalidate relevant cached UI configuration.
- Payment capture/advance refund settlement is not implied without an actual financial integration.

## Risks

A full stack has much more authored code to document for the coursework. The final moderated brief may limit web UI acceptance; seek lecturer approval early. Core guest-room-booking-invoice rules always take precedence over configurable admin UI polish.
