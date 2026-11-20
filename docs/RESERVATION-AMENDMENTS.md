# SmartStay reservation modification (coursework rule)

## Requirement and interpretation

The CI6115 draft case study states that reservations cannot be **modified or cancelled within 24 hours of check-in**. The specific editable fields are not defined. Our provisional implementation supports **same-room check-in/check-out dates and guest count**, with the room unit/type fixed. Lecturer confirmation of this restriction remains outstanding.

At modification time:
- A reservation must be **CONFIRMED**, with an active invoice.
- It must be at least 24 hours until its **original** scheduled check-in, using hotel-local 14:00 as the provisional arrival time.
- If the check-in date changes, the 48-hour new-booking notice also applies to the new date.
- The room must still be operational.
- The revised stay must not conflict with another confirmed reservation. PostgreSQL's existing exclusion constraint is the final concurrency safeguard.
- A room-first pessimistic lock protects the booking update from concurrent room changes; the reservation row is then locked.
- Existing **LKR 10,000 advance is retained**, not charged a second time.
- Discounts, extra guests and tax are recalculated through the existing Java `PricingCalculator`, not frontend math.

Changes update the current invoice snapshot and append an immutable `reservation_amendments` row with previous/new dates, guest counts, gross totals, actor and timestamp. The same invoice ID remains associated with the reservation. Identical amendments are no-ops.

## Routes and backend boundary

`PUT /api/reservations/{id}` accepts:
```json
{"checkIn":"2030-09-10","checkOut":"2030-09-15","guestCount":3}
```
Guests require current database `reservation.modify.own` plus exact linked-guest ownership. Administrators require `reservation.modify.any`.

`GET /api/reservations/{id}/amendments` returns up to 50 recent amendment summaries; it requires corresponding reservation read permission and ownership. No client-selected actor ID is accepted.

Frontend: `/reservations/{id}/modify`, plus permission-filtered **Modify** actions in staff and guest reservation lists. Shared `FormField`, `SubmitButton`, `Feedback` and `DataTable` are reused. The confirmation invoice displays the updated totals.

## Validation

- Persistence tests: repricing from two nights to five nights, discount, existing advance retained, old/new totals recorded, no-op history, conflict rejection, cancelled reservation denied.
- Unit tests: modification cutoff 25 hours allowed, 23 hours rejected, cancelled state rejected.
- HTTP tests: owner can modify; cross-guest modification and amendment history are forbidden; staff with current role grant may modify.
- Browser E2E remains the higher-level click-through coverage, subject to CI health.

## Unresolved business-rule details

This does **not** offer room-type upgrades or switching physical room units, nor online payment/refund settlement. The change history currently records before/after totals rather than every individual invoice line. The exact modification policy and time-of-day are provisional pending lecturer guidance.
