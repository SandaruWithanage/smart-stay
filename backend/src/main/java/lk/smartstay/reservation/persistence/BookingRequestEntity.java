package lk.smartstay.reservation.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;
import lk.smartstay.guest.persistence.GuestEntity;
import lk.smartstay.reservation.domain.BookingRequestStatus;
import lk.smartstay.reservation.domain.StayPeriod;
import lk.smartstay.room.persistence.RoomEntity;

/** A pending guest request is not a reservation, inventory hold, or payment. */
@Entity
@Table(name = "booking_requests")
public class BookingRequestEntity {
    @Id
    private UUID id;

    @Column(name = "requested_by", nullable = false)
    private UUID requestedBy;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "guest_id", nullable = false)
    private GuestEntity guest;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_id", nullable = false)
    private RoomEntity room;

    @Column(name = "check_in", nullable = false)
    private LocalDate checkIn;
    @Column(name = "check_out", nullable = false)
    private LocalDate checkOut;
    @Column(name = "guest_count", nullable = false)
    private int guestCount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private BookingRequestStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
    @Column(name = "reviewed_at")
    private Instant reviewedAt;
    @Column(name = "reviewed_by")
    private UUID reviewedBy;
    @Column(name = "reviewer_note", length = 200)
    private String reviewerNote;
    @Column(name = "reservation_id")
    private UUID reservationId;

    protected BookingRequestEntity() {}

    public BookingRequestEntity(UUID id, UUID requestedBy, GuestEntity guest,
                                RoomEntity room, StayPeriod stay, int guestCount, Instant createdAt) {
        this.id = Objects.requireNonNull(id);
        this.requestedBy = Objects.requireNonNull(requestedBy);
        this.guest = Objects.requireNonNull(guest);
        this.room = Objects.requireNonNull(room);
        Objects.requireNonNull(stay);
        if (guestCount <= 0) throw new IllegalArgumentException("Guest count must be positive");
        this.checkIn = stay.checkIn();
        this.checkOut = stay.checkOut();
        this.guestCount = guestCount;
        this.createdAt = Objects.requireNonNull(createdAt);
        this.status = BookingRequestStatus.PENDING;
    }

    private void requirePending() {
        if (status != BookingRequestStatus.PENDING) {
            throw new lk.smartstay.shared.ConflictException("Booking request is no longer pending");
        }
    }

    public void approve(UUID staffId, UUID bookingId, Instant now) {
        requirePending();
        reviewedBy = Objects.requireNonNull(staffId);
        reservationId = Objects.requireNonNull(bookingId);
        reviewedAt = Objects.requireNonNull(now);
        status = BookingRequestStatus.APPROVED;
    }

    public void reject(UUID staffId, String note, Instant now) {
        requirePending();
        reviewedBy = Objects.requireNonNull(staffId);
        reviewerNote = Objects.requireNonNull(note).strip();
        if (reviewerNote.isBlank() || reviewerNote.length() > 200) {
            throw new IllegalArgumentException("Rejection reason is required (max 200 characters)");
        }
        reviewedAt = Objects.requireNonNull(now);
        status = BookingRequestStatus.REJECTED;
    }

    public void withdraw() {
        requirePending();
        status = BookingRequestStatus.WITHDRAWN;
    }

    public UUID getId() { return id; }
    public UUID getRequestedBy() { return requestedBy; }
    public GuestEntity getGuest() { return guest; }
    public RoomEntity getRoom() { return room; }
    public LocalDate getCheckIn() { return checkIn; }
    public LocalDate getCheckOut() { return checkOut; }
    public int getGuestCount() { return guestCount; }
    public BookingRequestStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getReviewedAt() { return reviewedAt; }
    public UUID getReviewedBy() { return reviewedBy; }
    public String getReviewerNote() { return reviewerNote; }
    public UUID getReservationId() { return reservationId; }
}
