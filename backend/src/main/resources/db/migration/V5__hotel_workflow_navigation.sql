-- Extend the server-owned, permission-filtered navigation registry.
-- Route keys are predefined in the Next.js client; no arbitrary URL or component execution.
INSERT INTO navigation_items
  (id, label, route_key, icon_key, sort_order, required_permission, enabled)
VALUES
  ('admin-rooms','Room inventory','roomInventory','rooms',32,'room.read',TRUE),
  ('admin-reservations','Reservation ledger','reservationLedger','calendar',36,'reservation.read.any',TRUE),
  ('desk-booking','New desk reservation','deskBooking','plus',40,'reservation.create.any',TRUE),
  ('guest-history','My reservations','myReservations','calendar',42,'reservation.read.own',TRUE)
ON CONFLICT (id) DO NOTHING;

UPDATE ui_configuration_revision SET version = version + 1 WHERE id=1;
