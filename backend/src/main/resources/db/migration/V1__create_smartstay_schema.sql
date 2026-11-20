-- Initial relational schema. Business rules remain authoritative in Java services.
-- The exclusion constraint also protects against concurrent overlapping reservations.
CREATE EXTENSION IF NOT EXISTS btree_gist;

CREATE TABLE room_packages (
    room_type VARCHAR(32) PRIMARY KEY,
    daily_rate NUMERIC(12,2) NOT NULL CHECK (daily_rate >= 0),
    breakfast_included BOOLEAN NOT NULL,
    extra_guest_rate NUMERIC(12,2) NOT NULL CHECK (extra_guest_rate >= 0),
    tax_rate NUMERIC(5,4) NOT NULL CHECK (tax_rate >= 0 AND tax_rate <= 1)
);

INSERT INTO room_packages(room_type, daily_rate, breakfast_included, extra_guest_rate, tax_rate) VALUES
    ('STANDARD', 6000, FALSE, 1000, 0.10),
    ('DELUXE', 9000, TRUE, 1500, 0.12),
    ('SUITE', 14000, TRUE, 2000, 0.15),
    ('FAMILY_SUITE', 18000, TRUE, 2500, 0.15);

CREATE TABLE rooms (
    id VARCHAR(32) PRIMARY KEY,
    room_type VARCHAR(32) NOT NULL REFERENCES room_packages(room_type),
    bed_type VARCHAR(20) NOT NULL CHECK (bed_type IN ('SINGLE','DOUBLE','QUEEN','KING')),
    capacity INTEGER NOT NULL CHECK (capacity > 0),
    operational_status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE'
      CHECK (operational_status IN ('ACTIVE','MAINTENANCE')),
    active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE guests (
    id UUID PRIMARY KEY,
    document_number VARCHAR(64) NOT NULL UNIQUE,
    full_name VARCHAR(200) NOT NULL,
    contact_number VARCHAR(32) NOT NULL,
    email VARCHAR(254) NOT NULL
);

CREATE TABLE reservations (
    id UUID PRIMARY KEY,
    guest_id UUID NOT NULL REFERENCES guests(id),
    room_id VARCHAR(32) NOT NULL REFERENCES rooms(id),
    check_in DATE NOT NULL,
    check_out DATE NOT NULL,
    guest_count INTEGER NOT NULL CHECK (guest_count > 0),
    status VARCHAR(16) NOT NULL CHECK (status IN ('CONFIRMED','CANCELLED')),
    advance_paid NUMERIC(12,2) NOT NULL DEFAULT 10000
      CHECK (advance_paid >= 0),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT ck_reservation_positive_stay CHECK (check_out > check_in),
    CONSTRAINT ex_room_active_stay EXCLUDE USING gist (
        room_id WITH =,
        daterange(check_in, check_out, '[)') WITH &&
    ) WHERE (status = 'CONFIRMED')
);

CREATE INDEX idx_reservations_guest ON reservations(guest_id);
CREATE INDEX idx_reservations_checkin ON reservations(check_in);

CREATE TABLE invoices (
    id UUID PRIMARY KEY,
    reservation_id UUID NOT NULL UNIQUE REFERENCES reservations(id),
    base_charge NUMERIC(12,2) NOT NULL,
    extra_charge NUMERIC(12,2) NOT NULL,
    discount_amount NUMERIC(12,2) NOT NULL,
    tax_amount NUMERIC(12,2) NOT NULL,
    gross_total NUMERIC(12,2) NOT NULL,
    advance_applied NUMERIC(12,2) NOT NULL,
    balance_due NUMERIC(12,2) NOT NULL,
    refund_due NUMERIC(12,2) NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
