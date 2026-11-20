# Identity, permissions and navigation (milestone 3)

## Authentication

- Guest sign-up (`POST /api/auth/register`) creates a linked guest record, a separate account and the `GUEST` role.
- Login (`POST /api/auth/login`) checks BCrypt password hashes, responds with an HS256 JWT expiring after 15 minutes.
- Logout (`POST /api/auth/logout`) increases `token_version` to invalidate all existing tokens for the account.
- JWT signing key must be a **fresh, cryptographically random, Base64 32-byte secret** provided through `SMARTSTAY_JWT_SECRET_BASE64`. There is no fallback signing key.
- JWT issuer and audience are checked. JWT carries only identity and revocation version, **not** role permissions.
- The Spring Security JWT authentication converter reloads account state and current role permissions on **every authenticated request**.
- Public signup never grants `ADMIN`. Admin account creation is opt-in, one-time via environment values `SMARTSTAY_BOOTSTRAP_ADMIN_EMAIL` and `SMARTSTAY_BOOTSTRAP_ADMIN_PASSWORD` (at least 16 characters). Remove these env vars immediately after provisioning.

## RBAC / constrained ABAC

Role grants are stored in `roles`, `permissions`, `user_roles` and `role_permissions`. Administrator-granted action codes are checked at the controller boundary. Resource ownership is enforced in Java using the authenticated user's linked `guest_id` and the reservation's actual owner from PostgreSQL. A user-supplied URL ID cannot grant ownership.

- Guests can read their own guest profile, reservation and invoice and can cancel an eligible own reservation.
- Administrator can manage room records, register desk guests, confirm desk reservations and view the permitted records.
- Only administrator permission `access.manage` may change role permissions. The ADMIN role must retain it; the GUEST role cannot receive policy-editor permissions.
- Guest creation of confirmed reservations is NOT enabled: a public user cannot claim the LKR 10,000 advance was received. Later introduce a pending booking flow with verified receipt/payment.

Backend permission evaluation and guest ownership are authoritative. Frontend navigation is for presentation only.

## Navigation

`GET /api/me/ui-config` returns effective permissions and allowed items from `navigation_items`. No executable components or arbitrary external URLs are stored. Frontend maps `routeKey` into a safe known route registry.

Administrators may edit supported roles via `/api/admin/access/roles/{role}/permissions` and change existing navigation item labels, order and enabled state via `/api/admin/access/navigation/{id}`. These operations do not allow arbitrary JavaScript, SQL or frontend route registration.

## Security limitations before public deployment

- Auth endpoints need edge / per-IP rate limiting and monitoring. Current API is suitable for coursework local demonstration, **not internet production**.
- No email verification, password reset or MFA yet.
- HMAC shared secret requires secure secret storage and rotation planning; multi-node deployment benefits from asymmetric signing and key IDs.
- No refresh tokens; 15-minute sessions require reauthentication.
- Frontend should use an HttpOnly SameSite cookie in its own server boundary and proxy authorized API calls. Do not store JWT in localStorage.
- Audit logs, comprehensive access-control management UI, request-origin checks and test coverage for concurrent policy changes remain follow-up work.
- If the app is exposed publicly, add anti-automation controls, HTTPS, trusted proxy config, secure cookies, rate limits, monitoring and abuse protection first.
