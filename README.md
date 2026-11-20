# SmartStay

SmartStay Hotel Booking System, developed against the CI6115 Programming III coursework brief and our provisional SRS.

## Architecture

- `backend/`: Java 21 compilation baseline, Spring Boot 4.1.2 modular monolith (MVC/service/domain/persistence per module)
- `frontend/`: Next.js 16 + TypeScript, responsive Aqua Frost glassmorphism
- `docker-compose.yml`: local PostgreSQL 18, backed by Flyway migrations
- `docs/`: architecture decisions, assumption register and milestone status

## Getting started

1. Copy `.env.example` to `.env`. Change the development password. Do not commit secrets.
2. Start PostgreSQL in the repo root: `docker compose up -d db`.
3. Set the matching database password for Spring Boot (it does not automatically read the root `.env`):
   - PowerShell: `$env:DB_PASSWORD="your-local-password"`
   - Bash: `export DB_PASSWORD=your-local-password`
4. Run backend: `cd backend && mvn spring-boot:run` (JDK 21+, Maven 3.6.3+).
5. Run frontend in another terminal: `cd frontend && npm install && npm run dev` (Node 22+).
6. Open `http://localhost:3000`.
7. Run tests with PostgreSQL available: `cd backend && mvn test`; frontend: `cd frontend && npm run typecheck && npm run build`.

## Available API

| Method | Endpoint | Status |
| --- | --- | --- |
| GET | `/api/public/room-packages` | Public, DB-backed and cached |
| GET | `/api/public/rooms/available?checkIn=2030-05-10&checkOut=2030-05-12&roomType=DELUXE` | Public, live query against persisted rooms and confirmed bookings |
| POST/GET | `/api/guests` | Implemented but HTTP **denied** until authentication |
| GET/POST/PUT/DELETE | `/api/admin/rooms` | Implemented but HTTP **denied** until authorization |
| GET/POST | `/api/reservations` | Implemented but HTTP **denied** until JWT ownership and permissions |
| GET/POST | `/api/reservations/{id}/invoice`, `/{id}/cancel` | Implemented as service / controller; HTTP denied |

Only public catalog and room availability are currently accessible over HTTP. Booking creation, cancellation and invoice persistence are verified by **backend integration tests**, not yet by authenticated user journeys.

**Money handling:** Creation requires staff confirmation of a manually received LKR 10,000 advance, but no actual payment gateway exists. Cancellation records the refundable advance as PENDING and voids the invoice; no refund transfer occurs.

## Coursework notes

The university brief is marked draft. Room capacity, 48h vs two-calendar-day interpretation, check-in time, extra guest charging basis and taxation need lecturer confirmation. Record assumptions in `docs/ASSUMPTIONS.md` and keep your UML, tests, source listings and AI acknowledgement aligned to the final work.
