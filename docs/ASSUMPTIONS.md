# SmartStay provisional assumption register

**Source:** SmartStay Full SRS v1.0 (20 Nov 2026), based on the CI6115 *draft* coursework. These are NOT lecturer-approved business rules.

| ID | Unresolved point | Temporary implementation choice | Confirmation required |
| --- | --- | --- | --- |
| O-01 | Room capacities unspecified | Positive integer on each room; no invented defaults | Yes |
| O-02 | Extra guest charge "per guest" vs "per guest per night" | PricingCalculator multiplies per-extra-guest by nights | Yes |
| O-03 | Two days prior to check-in | BookingRules uses >= 48 hours | Yes |
| O-04 | Check-in time and 24h cutoff | Provisional 14:00 local check-in; >= 24h cancellation allowed | Yes |
| O-05 | Tax base | Tax on discounted base + extra guest charges | Yes |
| O-06 | Refundable advance | Price breakdown exposes refund due; no payment or refund gateway | Yes |
| O-07 | Modification feature | Not implemented in first milestone | Yes |
| O-08 | Available / Reserved / Maintenance | ACTIVE/MAINTENANCE operational state; RESERVED derived per stay dates | Yes |
| O-09 | Hotel chain structure | Single-hotel inventory first | Yes |
| O-10 | Web GUI accepted by coursework | Assumed pending lecturer decision | Yes |
| O-11 | Full source code listings in report | Generated files may be excluded only with lecturer approval | Yes |
| O-12 | Due date & final moderated coursework | Unknown | Yes |

## Invariants already implemented

1. Half-open stay dates: check-in inclusive, check-out exclusive.
2. No overlap of confirmed reservations for the same room and dates; cancelled reservations do not block availability.
3. PostgreSQL `btree_gist` exclusion constraint prevents duplicate concurrent active room allocations.
4. Money uses `BigDecimal` and half-up rounding to 2 decimal places.
5. Five or more nights qualifies for 12% *base-room* discount only.
6. The four rates, breakfast flags, extras and tax rates match the brief.
7. The refundable advance is recorded separately from successful payment processing.

**Do not claim full booking functionality until persistent reservation services and transactions are implemented.**
