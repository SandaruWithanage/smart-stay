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
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;
import lk.smartstay.guest.persistence.GuestEntity;
import lk.smartstay.reservation.domain.RefundStatus;
import lk.smartstay.reservation.domain.ReservationStatus;
import lk.smartstay.reservation.domain.StayPeriod;
import lk.smartstay.room.persistence.RoomEntity;

@Entity
@Table(name = "reservations")
public class ReservationEntity {
    @Id
    private UUID id;

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
    private ReservationStatus status;

    @Column(name = "advance_paid", nullable = false, precision = 12, scale = 2)
    private BigDecimal advancePaid;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "cancelled_at")
    private Instant cancelledAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "refund_status", nullable = false, length = 20)
    private RefundStatus refundStatus;

    @Column(name = "cancellation_refund_due", nullable = false, precision = 12, scale = 2)
    private BigDecimal cancellationRefundDue;

    protected ReservationEntity() { }

    public ReservationEntity(UUID id, GuestEntity guest, RoomEntity room, StayPeriod stay,
                             int guestCount, BigDecimal advancePaid, Instant createdAt) {
        this.id = Objects.requireNonNull(id);
        this.guest = Objects.requireNonNull(guest);
        this.room = Objects.requireNonNull(room);
        Objects.requireNonNull(stay);
        if (guestCount <= 0 || advancePaid == null || advancePaid.signum() < 0) {
            throw new IllegalArgumentException("Invalid reservation guests or advance");
        }
        this.checkIn = stay.checkIn();
        this.checkOut = stay.checkOut();
        this.guestCount = guestCount;
        this.advancePaid = advancePaid;
        this.createdAt = Objects.requireNonNull(createdAt);
        this.status = ReservationStatus.CONFIRMED;
        this.refundStatus = RefundStatus.NOT_APPLICABLE;
        this.cancellationRefundDue = BigDecimal.ZERO.setScale(2);
    }

    public void cancel(Instant when) {
        if (status != ReservationStatus.CONFIRMED) throw new IllegalStateException("Already cancelled");
        status = ReservationStatus.CANCELLED;
        cancelledAt = Objects.requireNonNull(when);
        cancellationRefundDue = advancePaid;
        refundStatus = RefundStatus.PENDING;
    }

    public UUID getId() { return id; }
    public GuestEntity getGuest() { return guest; }
    public RoomEntity getRoom() { return room; }
    public StayPeriod getStay() { return new StayPeriod(checkIn, checkOut); }
    public LocalDate getCheckIn() { return checkIn; }
    public LocalDate getCheckOut() { return checkOut; }
    public int getGuestCount() { return guestCount; }
    public ReservationStatus getStatus() { return status; }
    public BigDecimal getAdvancePaid() { return advancePaid; }
    public RefundStatus getRefundStatus() { return refundStatus; }
    public BigDecimal getCancellationRefundDue() { return cancellationRefundDue; }
}
