
# SmartStay Milestone 3: Authentication and Permission-Driven UI

This branch depends on PR #2 (booking persistence). It adds real JWT authentication and a PostgreSQL-driven RBAC / constrained ABAC access model, plus an authenticated Aqua Frost workspace.

## Local startup

1. Copy `.env.example` to `.env`, set a strong local PostgreSQL password, and run `docker compose up -d db`.
2. Generate a **random** 32-byte JWT HMAC key, for example `openssl rand -base64 32`. In PowerShell:
   `$bytes = New-Object byte[] 32; [Security.Cryptography.RandomNumberGenerator]::Fill($bytes); [Convert]::ToBase64String($bytes)`
3. Set **both** `DB_PASSWORD` and `SMARTSTAY_JWT_SECRET_BASE64` as environment variables in the terminal running Spring Boot (the root `.env` is not loaded automatically by Java):
   - PowerShell: `$env:DB_PASSWORD="your-local-db-password"`
   - PowerShell: `$env:SMARTSTAY_JWT_SECRET_BASE64="your-generated-base64"`
4. One-time admin provisioning (optional):
   - `$env:SMARTSTAY_BOOTSTRAP_ADMIN_EMAIL="your-admin-email@example.org"`
   - `$env:SMARTSTAY_BOOTSTRAP_ADMIN_PASSWORD="your-unique-strong-16-plus-character-password"`
   - Start Spring Boot once; remove the two bootstrap environment variables before restarting.
5. `cd backend && mvn spring-boot:run`. In a separate terminal, `cd frontend && npm install && npm run dev`.
6. Visit `http://localhost:3000/register` for a guest account, or `/login` to sign in with an existing account. Admins with `access.manage` see `/admin/access`.

## Application APIs

`POST /api/auth/register`, `POST /api/auth/login`, `POST /api/auth/logout`, `GET /api/me/ui-config`.

Authenticated room, reservation, invoice, and guest routes validate database permissions plus ownership as appropriate. Admin access operations are `GET /api/admin/access/permissions`, `GET/PUT /api/admin/access/roles/{role}/permissions`, and `GET/PUT /api/admin/access/navigation[/{id}]`.

## Security decisions

- JWTs expire after **15 minutes** and carry a subject, issuer, audience and token version, not role grants.
- The backend validates the signature and checks **current PostgreSQL permissions on every request**.
- Logging out increments the account token version and invalidates its current tokens.
- The Next.js BFF holds JWTs in an HttpOnly SameSite=Strict cookie. JavaScript never reads the cookie value.
- Cross-guest record access is denied by backend resource-ownership checks.
- The sidebar uses a **server-filtered** navigation manifest and a predefined frontend route registry.
- Admins can edit role permissions and supported navigation labels/order/visibility via the UI; changes increment a UI configuration revision.
- Guest users cannot claim a desk advance was received. Confirmed reservation creation remains a trusted desk operation.

## Not production ready

This coursework implementation still needs anti-abuse rate limiting, email verification, password reset, MFA for admin, audit-event persistence, secret rotation and more comprehensive frontend E2E tests before hosting publicly. There is no online payment gateway. See `docs/ASSUMPTIONS.md` for unsettled lecturer decisions.

## CI

GitHub Actions runs Maven + PostgreSQL tests, including JWT/permission/ownership checks, and the Next.js typecheck and production build.
