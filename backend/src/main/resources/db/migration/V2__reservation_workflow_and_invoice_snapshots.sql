-- Milestone 2: preserve the recorded charge basis and track cancellation liability.
-- This migration builds on the V1 schema before any authenticated bookings are enabled.
ALTER TABLE reservations
  ADD COLUMN cancelled_at TIMESTAMPTZ,
  ADD COLUMN refund_status VARCHAR(20) NOT NULL DEFAULT 'NOT_APPLICABLE',
  ADD COLUMN cancellation_refund_due NUMERIC(12,2) NOT NULL DEFAULT 0
    CHECK (cancellation_refund_due >= 0),
  ADD CONSTRAINT ck_reservation_refund_status
    CHECK (refund_status IN ('NOT_APPLICABLE','PENDING','SETTLED'));

ALTER TABLE invoices
  ADD COLUMN room_id_snapshot VARCHAR(32) NOT NULL DEFAULT '',
  ADD COLUMN room_type_snapshot VARCHAR(32) NOT NULL DEFAULT '',
  ADD COLUMN bed_type_snapshot VARCHAR(20) NOT NULL DEFAULT '',
  ADD COLUMN daily_rate_snapshot NUMERIC(12,2) NOT NULL DEFAULT 0,
  ADD COLUMN guest_name_snapshot VARCHAR(200) NOT NULL DEFAULT '',
  ADD COLUMN nights INTEGER NOT NULL DEFAULT 1 CHECK (nights > 0),
  ADD COLUMN guest_count INTEGER NOT NULL DEFAULT 1 CHECK (guest_count > 0),
  ADD COLUMN voided BOOLEAN NOT NULL DEFAULT FALSE;

-- Defaults are only used when migrating the previous empty-invoice foundation.
ALTER TABLE invoices
  ALTER COLUMN room_id_snapshot DROP DEFAULT,
  ALTER COLUMN room_type_snapshot DROP DEFAULT,
  ALTER COLUMN bed_type_snapshot DROP DEFAULT,
  ALTER COLUMN daily_rate_snapshot DROP DEFAULT,
  ALTER COLUMN guest_name_snapshot DROP DEFAULT,
  ALTER COLUMN nights DROP DEFAULT,
  ALTER COLUMN guest_count DROP DEFAULT;

CREATE INDEX idx_room_operational ON rooms(active, operational_status, room_type);
