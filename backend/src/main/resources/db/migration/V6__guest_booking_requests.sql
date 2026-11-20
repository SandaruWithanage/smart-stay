-- Guest-submitted requests never reserve a room or record a payment.
-- Only desk approval (after manual receipt confirmation) creates a confirmed reservation.
CREATE TABLE booking_requests (
    id UUID PRIMARY KEY,
    requested_by UUID NOT NULL REFERENCES app_users(id),
    guest_id UUID NOT NULL REFERENCES guests(id),
    room_id VARCHAR(32) NOT NULL REFERENCES rooms(id),
    check_in DATE NOT NULL,
    check_out DATE NOT NULL,
    guest_count INTEGER NOT NULL CHECK (guest_count > 0),
    status VARCHAR(16) NOT NULL DEFAULT 'PENDING'
      CHECK (status IN ('PENDING','APPROVED','REJECTED','WITHDRAWN')),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    reviewed_at TIMESTAMPTZ,
    reviewed_by UUID REFERENCES app_users(id),
    reviewer_note VARCHAR(200),
    reservation_id UUID UNIQUE REFERENCES reservations(id),
    CONSTRAINT ck_booking_request_stay CHECK (check_out > check_in),
    CONSTRAINT ck_booking_request_approval CHECK (
       (status = 'APPROVED' AND reservation_id IS NOT NULL AND reviewed_by IS NOT NULL AND reviewed_at IS NOT NULL)
       OR (status <> 'APPROVED' AND reservation_id IS NULL)
    )
);
CREATE INDEX idx_booking_request_user_created ON booking_requests(requested_by,created_at DESC);
CREATE INDEX idx_booking_request_status_created ON booking_requests(status,created_at DESC);
-- Prevent a guest from accidentally submitting the same pending room/date request twice.
CREATE UNIQUE INDEX uq_booking_request_pending_duplicate
  ON booking_requests(requested_by, room_id, check_in, check_out) WHERE status = 'PENDING';

INSERT INTO permissions(code,description) VALUES
 ('booking_request.create.own','Submit a request for a linked guest stay'),
 ('booking_request.read.own','View own booking requests'),
 ('booking_request.withdraw.own','Withdraw own pending booking requests'),
 ('booking_request.review','Review, approve or reject booking requests')
ON CONFLICT (code) DO NOTHING;

INSERT INTO role_permissions(role_code,permission_code) VALUES
 ('GUEST','booking_request.create.own'),
 ('GUEST','booking_request.read.own'),
 ('GUEST','booking_request.withdraw.own'),
 ('ADMIN','booking_request.review')
ON CONFLICT DO NOTHING;

INSERT INTO navigation_items(id,label,route_key,icon_key,sort_order,required_permission,enabled) VALUES
 ('guest-request-new','Request a stay','requestStay','plus',41,'booking_request.create.own',TRUE),
 ('guest-request-history','My requests','myBookingRequests','calendar',43,'booking_request.read.own',TRUE),
 ('admin-request-queue','Booking requests','bookingRequestQueue','calendar',38,'booking_request.review',TRUE)
ON CONFLICT (id) DO NOTHING;
UPDATE ui_configuration_revision SET version=version+1 WHERE id=1;
