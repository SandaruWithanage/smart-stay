# SmartStay

SmartStay Hotel Booking System for CI6115 Programming III. This repository implements the **proposed** [SmartStay SRS v1.0] baseline in small, reviewable milestones.

## Architecture

- `backend/`: Java 21+ / Spring Boot 4.1.2 modular monolith, with pure Java booking and billing rules, REST API and PostgreSQL persistence foundations.
- `frontend/`: Next.js 16 / React 19 / TypeScript, responsive **Aqua Frost** design system.
- `docker-compose.yml`: local PostgreSQL.
- `docs/`: architecture decisions, assumptions, and implementation status.

Java 21 is the **compilation baseline** for compatibility with commonly available development environments; the project may also be run on Java 25. This is not a Java-25-specific codebase.

## First milestone

This branch supplies the initial project scaffold, publicly readable room-package catalog, pure Java reservation overlap / timing validation, and invoice calculation logic. It does **not** yet support complete database-backed reservations, production authentication, dynamic permission changes, or real payments.

## Get started

1. Copy `.env.example` to `.env`. Never commit secrets.
2. Start PostgreSQL from the repository root: `docker compose up -d db`.
3. Set the matching backend database password in your terminal (Spring Boot does **not** automatically load the root `.env`):
   - **Windows PowerShell:** `$env:DB_PASSWORD="change-me-for-local-development"`
   - **Bash:** `export DB_PASSWORD=change-me-for-local-development`
   - Use the value actually configured in your local `.env`. `DB_URL` and `DB_USERNAME` default to the local database values.
4. Backend: `cd backend` then `mvn spring-boot:run` (JDK 21+, Maven 3.6.3+).
5. In a second terminal: `cd frontend` then `npm install` followed by `npm run dev` (Node 22+).
6. Browse `http://localhost:3000`. The public package API is `http://localhost:8080/api/public/room-packages`.
7. Run backend tests with PostgreSQL running and database variables set: `cd backend && mvn test`. Frontend type check: `cd frontend && npm run typecheck`.

## Coursework requirements

The assignment brief is **draft** and leaves room capacities, extras per-night vs once, tax base, 2-day / 24-hour cutoffs, refund workflow, web UI approval and deadline unresolved. The provisional rules are isolated in Java and tracked in `docs/ASSUMPTIONS.md`; they must not be described as lecturer-approved.

The coursework requires a comprehensive ID-prefixed UML class diagram, full authored source code listings, and executed test cases in the report. Keep the report aligned with this implementation, document AI contribution, and obtain final lecturer clarifications.

## Development policy

Use feature branches and pull requests. Prioritize booking correctness, OOP, tests and the approved SRS before extra admin UI. Public APIs are read-only in the first milestone; all privileged endpoints must remain inaccessible until server-side authentication and authorization are implemented.
