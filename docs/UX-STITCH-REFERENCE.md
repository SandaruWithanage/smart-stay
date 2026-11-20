# SmartStay UI / UX: Stitch design integration

Source archive supplied by project owner on 20 November 2026:
`stitch_application_development_srs_project.zip`. The source contains screen screenshots, HTML exports, the SmartStay SRS Markdown, and two design token documents.

## Visual baseline

**Aqua Frost** is the user's selected principal visual theme:
- Canvas `#E3FDFD`, frosted glass tint `#CBF1F5`.
- Secondary hover tint `#A6E3E9`, brand aqua `#71C9CE`.
- Accessible primary CTA `#176B74` and main ink `#0F3E46`.
- Transparent glass on shell and supporting panels, but opaque form fields, record tables and invoices.

**Futuristic Spatial Glass** is the optional dark mode for operational consoles:
- Ambient charcoal `#10131a`, layered `rgba(25,33,49,.72)` glass.
- Aqua `#0df2c9` active controls with dark ink; restrained violet atmospheric accent.
- Higher-contrast and reduced-transparency fallback for information-dense screens.

Theme colors are exposed through shared CSS variables in `frontend/src/app/globals.css`, not duplicated across pages.

## Stitch reference -> implemented feature

| Uploaded reference | SmartStay implementation |
| --- | --- |
| `smartstay_rooms_packages_1` | Existing `/` and `/availability` package listings |
| `smartstay_admin_room_inventory_management_dark_spatial_glass` | `/admin/rooms`, searchable inventory, create/edit/retire |
| `smartstay_admin_booking_reservation_ledger_dark_spatial_glass` | `/admin/reservations`, bounded search ledger, cancel |
| `smartstay_reservation_calculation` | Reusable `InvoiceBreakdown`, desk booking form and final invoice |
| `smartstay_official_guest_invoice_1` | `/reservations/[id]/invoice` A4-safe print view |
| `smartstay_guest_profile_booking_history_management_dark_spatial_glass` | `/my/reservations`, ownership-filtered booking history |
| `smartstay_security_dynamic_rbac_abac_matrix_dark_spatial_glass` | Existing `/admin/access` and server-controlled navigation |
| `smartstay_front_desk_check_in_folio_checkout_dark_spatial_glass` | Visual reference only: check-in/folio workflow not yet required |
| `smartstay_front_desk_daily_housekeeping_turn_down_queue_dark_spatial_glass` | Visual reference only: housekeeping is not in coursework brief |

No irrelevant image from the uploaded ZIP is used as hotel artwork.

## Common component inventory

`frontend/src/components/ui/hotel-ui.tsx`:
- `Surface`, `PageHeader`, `SectionHeader`, `PrimaryLink`
- `MetricCard`, `StatusBadge`, `EmptyState`, `Feedback`
- `FormField`, generic `DataTable`

`frontend/src/components/ui/confirm-submit.tsx`:
- Form submit pending states and destructive confirmation

`frontend/src/components/ui/invoice-breakdown.tsx`:
- Shared itemized billing presentation, no duplicated invoice math

`frontend/src/components/ui/print-button.tsx`:
- Client print operation, hidden during A4 print rendering

`frontend/src/lib/hotel.ts`: strongly typed room/reservation/invoice models, room labels and monetary/date formatting.
`frontend/src/lib/server-hotel.ts`: shared authenticated backend fetching and server page-access guard.

## Product and safety rules

- Never create fake booking metrics or sample guest PII on real management pages.
- Navigation is server-filtered using database RBAC permissions, with a trusted route registry.
- Action permission checks remain in Java, not in the sidebar or React forms.
- The backend owns reservation overlap, check-in deadline and cancellation cutoff.
- Desk booking only records the advance after explicit manual receipt confirmation.
- A desk-created guest is **not** automatically linked to a guest login without identity verification.
- Cancellation records pending refund liability; does not transfer money.
- Do not create real payment provider UI until there is a real payment integration.
- Responsive data tables may scroll horizontally; no information should be clipped in print.
