# SmartStay: Semantic colors and guest request workflow

**Status:** Implemented for review, not a new coursework-mandated payment feature.
**Reference:** Owner-supplied Stitch `aqua_frost/DESIGN.md` and `futuristic_spatial_glass/DESIGN.md`, and current shared SmartStay UI components.

## Color analysis and decision

| Meaning | Aqua Frost light | Spatial Glass dark | Usage |
| --- | --- | --- | --- |
| Primary, ordinary navigation | `#176B74` with white text | `#0DF2C9` with dark text | Save, search, navigate |
| Approve / success action | `#047857` with white text | `#34D399` with `#10131A` text | Approve after verification |
| Reject / destructive action | `#BE123C` with white text | `#FDA4AF` with `#10131A` text | Reject, retire, cancel |
| Success message / badge | `#ECFDF5`, text `#065F46` | Dark translucent green, text `#A7F3D0` | Confirmed, approved |
| Error message / badge | `#FFF1F2`, text `#9F1239` | Dark translucent rose, text `#FDA4AF` | Rejected, cancelled |
| Pending / caution | `#FFFBEB`, text `#92400E` | Dark translucent amber, text `#FCD34D` | Requests awaiting review |

For normal-size button labels, calculated contrast ratios:
- Light approval: green on white **5.48:1**.
- Light rejection: rose red on white **6.29:1**.
- Dark approval: green on charcoal **9.67:1**.
- Dark rejection: pastel red on charcoal **9.83:1**.

Never use a color without descriptive label and icon. Keep invoice and data table surfaces legible and use CSS semantic variables rather than hardcoded per-page colors. The supplied Aqua Frost design already uses green for available rooms and red for maintenance; the selected variants harmonize with those tokens.

## New request states

```text
Guest selects available room + dates
    -> PENDING booking request
       -> staff confirms actual advance receipt -> APPROVED + CONFIRMED reservation + invoice
       -> staff provides rejection reason -> REJECTED
       -> submitting guest withdraws -> WITHDRAWN
```

**PENDING does not hold inventory, initiate payment, or guarantee the quoted room rate.** The room can be booked by another guest while requests await review. When authorized staff approve:
1. Authenticate as `booking_request.review`.
2. Confirm actual receipt of LKR 10,000 (manual desk operation).
3. Lock the pending request and call the existing booking service in the same database transaction.
4. Revalidate the 48-hour booking notice, room operation status, request room/type, and confirmed-reservation overlap.
5. Only if all checks pass: persist reservation and invoice and change request to APPROVED with reviewer and timestamp.

If the room is no longer available, approval fails and the request remains PENDING for staff to reject or resolve; **do not incorrectly claim payment has been settled/refunded**.

## Permission and navigation mapping

| Permission | Guest | Administrator |
| --- | --- | --- |
| `booking_request.create.own` | Yes | Granted but requires linked guest |
| `booking_request.read.own` | Yes | Granted but only own user ID |
| `booking_request.withdraw.own` | Yes, pending own only | Own only |
| `booking_request.review` | No | Yes |

Backend permission checks are independent of sidebar visibility. Request identity is resolved from JWT user ID and linked guest record, never from guest IDs in a form. The admin queue is capped to 50 recent requests.

Routes:
- `/booking-requests/new`: request a stay from the public date-availability search
- `/my/booking-requests`: own status history and withdrawal
- `/admin/booking-requests`: staff review with green approval and red rejection

## Deliberate limitations

This coursework implementation is **not a real online payment processor**. Staff must verify funds independently; approving simply records their confirmation and creates the same reservation/invoice as the desk workflow. Pending requests expire only through manual rejection/withdrawal in this iteration. There is no mail/SMS notification or refund transfer. No public deployment without rate limiting, MFA, email verification and audit/operational hardening.
