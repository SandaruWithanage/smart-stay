-- Provisional coursework modification scope: same-room stay dates and guest count.
-- Preserve historical billing values and actor with an immutable per-amendment record.
CREATE TABLE reservation_amendments (
  id UUID PRIMARY KEY,
  reservation_id UUID NOT NULL REFERENCES reservations(id),
  changed_by UUID NOT NULL REFERENCES app_users(id),
  previous_check_in DATE NOT NULL,
  previous_check_out DATE NOT NULL,
  new_check_in DATE NOT NULL,
  new_check_out DATE NOT NULL,
  previous_guest_count INTEGER NOT NULL CHECK (previous_guest_count > 0),
  new_guest_count INTEGER NOT NULL CHECK (new_guest_count > 0),
  previous_gross_total NUMERIC(12,2) NOT NULL CHECK (previous_gross_total >= 0),
  new_gross_total NUMERIC(12,2) NOT NULL CHECK (new_gross_total >= 0),
  changed_at TIMESTAMPTZ NOT NULL,
  CONSTRAINT ck_amendment_previous_dates CHECK (previous_check_out > previous_check_in),
  CONSTRAINT ck_amendment_new_dates CHECK (new_check_out > new_check_in)
);
CREATE INDEX idx_reservation_amendment_history ON reservation_amendments(reservation_id,changed_at DESC);

INSERT INTO permissions(code,description) VALUES
 ('reservation.modify.own','Modify own confirmed reservation before cutoff'),
 ('reservation.modify.any','Modify any confirmed reservation before cutoff')
ON CONFLICT (code) DO NOTHING;
INSERT INTO role_permissions(role_code,permission_code) VALUES
 ('GUEST','reservation.modify.own'),
 ('ADMIN','reservation.modify.any')
ON CONFLICT DO NOTHING;
UPDATE ui_configuration_revision SET version = version + 1 WHERE id=1;
