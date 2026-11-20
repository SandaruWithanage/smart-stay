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
3. Set a random JWT signing key and the matching database password for Spring Boot (the Java process does not automatically read the root `.env`):
   - Generate a key with `openssl rand -base64 32`, or use the PowerShell command in `docs/SECURITY-SETUP.md`.
   - PowerShell: `$env:SMARTSTAY_JWT_SECRET_BASE64="your-generated-random-base64-key"`
   - PowerShell: `$env:DB_PASSWORD="your-local-password"`
   - Bash: `export DB_PASSWORD=your-local-password`
4. Run backend: `cd backend && mvn spring-boot:run` (JDK 21+, Maven 3.6.3+).
5. Run frontend in another terminal: `cd frontend && npm install && npm run dev` (Node 22+).
6. Open `http://localhost:3000`.
7. Run tests with PostgreSQL available: `cd backend && mvn test`; frontend: `cd frontend && npm run typecheck && npm run build`.

## Milestone 3: Authentication and dynamic navigation

- Guest registration/sign-in at `/register` and `/login`, authenticated workspace at `/workspace`.
- Admins can edit supported role grants and navigation items at `/admin/access`.
- Signed JWT (15 min), current DB permission checks and guest-resource ownership checks.
- Opt-in one-time admin bootstrap through `SMARTSTAY_BOOTSTRAP_ADMIN_EMAIL` and `SMARTSTAY_BOOTSTRAP_ADMIN_PASSWORD` (16+ characters), both supplied as environment variables to the Spring Boot process. Remove bootstrap values after provisioning.
- See `docs/SECURITY-SETUP.md` for setup, routes, security boundaries and limitations.

## Milestone 4: Stitch-inspired shared hotel workflows

The user-provided Stitch ZIP includes Aqua Frost and Spatial Glass design references. The application now reuses common `Surface`, `PageHeader`, `MetricCard`, `DataTable`, `FormField`, `StatusBadge`, `Feedback`, `InvoiceBreakdown` and form action controls.

| Page | Access | Purpose |
| --- | --- | --- |
| `/admin/rooms` | room.read / create / update / delete | Create, edit, search, and retire rooms |
| `/admin/reservations` | reservation.read.any | View/search staff reservation ledger, cancel eligible bookings |
| `/admin/reservations/new` | reservation.create.any | Manually confirm desk bookings and advance receipt |
| `/my/reservations` | reservation.read.own | Guest's own linked booking history |
| `/reservations/{id}/invoice` | invoice.read.own / any | Itemized invoice with A4 print layout |

The administrator should first create room units; **no demo rooms or personal records are seeded**. Staff may look up an existing guest using NIC/passport through a protected **POST** endpoint or create a new desk guest. Newly created desk guests are not automatically linked to login accounts.

### Staff workflow

1. Bootstrap an admin once (see `docs/SECURITY-SETUP.md`), then sign in.
2. Open **Room inventory** and create a room with an explicit approved capacity.
3. Open **New desk reservation** and select an available room unit.
4. Look up an existing guest by document number, or enter new guest identity details.
5. Enter check-in, check-out and guest count. Confirm **only after LKR 10,000 has actually been received**.
6. Confirm and review the stored invoice; print or save PDF via the browser.
7. Review and cancel eligible stays from **Reservation ledger**.

The date-availability search does not reserve rooms. Final overlap and notice checks run in Java, with a PostgreSQL exclusion constraint. Declared advances are ledger records, not electronic payments. Cancellation leaves a pending refund obligation, not a completed transfer.

Design/reference mapping: `docs/UX-STITCH-REFERENCE.md`. Implementation status: `docs/IMPLEMENTATION_STATUS.md`.

## Milestone 5: Booking requests and semantic action colors

Guest requests now follow a staff-review queue instead of falsely recording online payment.
A pending request does not reserve room inventory. Staff explicitly verify receipt of the
LKR 10,000 advance before a green **Approve** action creates a confirmed reservation and invoice.

- Guest: `/availability` → `/booking-requests/new` → `/my/booking-requests`.
- Staff: `/admin/booking-requests` for green approval or red rejection with a mandatory reason.
- Existing destructive actions (retire/cancel) remain red; routine search/save stays Aqua Frost teal.
- Configurable navigation and database permissions are updated by Flyway V6.
- Run `cd frontend && npm run test:ui` for semantic color/contrast tests.

Details and unverified coursework assumptions: `docs/SEMANTIC-COLORS-AND-BOOKING-REQUESTS.md`.

## Milestone 7: Reservation amendments

Staff and guests with the correct database permissions can modify confirmed
reservations at `/reservations/{id}/modify`. This supports same-room date and
guest-count changes before the provisional 24-hour check-in cutoff.

The Java service revalidates overlap, the 48-hour notice if check-in changes, and
recalculates the detailed invoice. The original LKR 10,000 advance is **not charged
again**. Amendments are stored in an append-only PostgreSQL history table.

See `docs/RESERVATION-AMENDMENTS.md`. Assumption O-07 still needs lecturer confirmation.

## Available API

| Method | Endpoint | Status |
| --- | --- | --- |
| GET | `/api/public/room-packages` | Public, DB-backed and cached |
| GET | `/api/public/rooms/available?checkIn=2030-05-10&checkOut=2030-05-12&roomType=DELUXE` | Public, live query against persisted rooms and confirmed bookings |
| POST | `/api/guests` | Staff guest registration with `guest.create` |
| GET | `/api/guests/{id}` | Own record or privileged guest-read permission |
| POST | `/api/guests/lookup` | Staff exact-document lookup with `guest.read.any` |
| GET | `/api/admin/reservations` | Staff latest 50, `reservation.read.any` |
| GET | `/api/me/reservations` | Own latest 50, `reservation.read.own` |
| GET/POST/PUT/DELETE | `/api/admin/rooms` | JWT + current room permissions required |
| GET/POST | `/api/reservations` | JWT + current permission and ownership requirements |
| GET/POST | `/api/reservations/{id}/invoice`, `/{id}/cancel` | JWT + owner or administrator permission |

Public catalog and room availability remain anonymously readable; authenticated operations now enforce the current database grants and resource ownership. Guest booking **history** is now available; independent guest self-booking remains pending a safe advance-collection workflow.

**Money handling:** Creation requires staff confirmation of a manually received LKR 10,000 advance, but no actual payment gateway exists. Cancellation records the refundable advance as PENDING and voids the invoice; no refund transfer occurs.

## Coursework notes

The university brief is marked draft. Room capacity, 48h vs two-calendar-day interpretation, check-in time, extra guest charging basis and taxation need lecturer confirmation. Record assumptions in `docs/ASSUMPTIONS.md` and keep your UML, tests, source listings and AI acknowledgement aligned to the final work.

**Booking request APIs:** Authenticated `GET/POST /api/booking-requests`,
`POST /api/booking-requests/{id}/withdraw`, and administrator
`GET /api/admin/booking-requests`,
`POST /api/admin/booking-requests/{id}/approve` or `/reject`.
