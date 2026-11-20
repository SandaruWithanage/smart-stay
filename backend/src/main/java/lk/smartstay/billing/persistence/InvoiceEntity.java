package lk.smartstay.billing.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import lk.smartstay.billing.domain.InvoiceBreakdown;
import lk.smartstay.reservation.persistence.ReservationEntity;
import lk.smartstay.room.persistence.RoomPackageEntity;

@Entity
@Table(name = "invoices")
public class InvoiceEntity {
    @Id
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reservation_id", nullable = false, unique = true)
    private ReservationEntity reservation;

    @Column(name = "base_charge", nullable = false, precision = 12, scale = 2)
    private BigDecimal baseCharge;
    @Column(name = "extra_charge", nullable = false, precision = 12, scale = 2)
    private BigDecimal extraCharge;
    @Column(name = "discount_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal discountAmount;
    @Column(name = "tax_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal taxAmount;
    @Column(name = "gross_total", nullable = false, precision = 12, scale = 2)
    private BigDecimal grossTotal;
    @Column(name = "advance_applied", nullable = false, precision = 12, scale = 2)
    private BigDecimal advanceApplied;
    @Column(name = "balance_due", nullable = false, precision = 12, scale = 2)
    private BigDecimal balanceDue;
    @Column(name = "refund_due", nullable = false, precision = 12, scale = 2)
    private BigDecimal refundDue;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "room_id_snapshot", nullable = false, length = 32)
    private String roomIdSnapshot;
    @Column(name = "room_type_snapshot", nullable = false, length = 32)
    private String roomTypeSnapshot;
    @Column(name = "bed_type_snapshot", nullable = false, length = 20)
    private String bedTypeSnapshot;
    @Column(name = "daily_rate_snapshot", nullable = false, precision = 12, scale = 2)
    private BigDecimal dailyRateSnapshot;
    @Column(name = "guest_name_snapshot", nullable = false, length = 200)
    private String guestNameSnapshot;
    @Column(nullable = false)
    private int nights;
    @Column(name = "guest_count", nullable = false)
    private int guestCount;
    @Column(nullable = false)
    private boolean voided;

    protected InvoiceEntity() { }

    public InvoiceEntity(UUID id, ReservationEntity reservation, RoomPackageEntity pkg,
                         InvoiceBreakdown charges, Instant createdAt) {
        this.id = Objects.requireNonNull(id);
        this.reservation = Objects.requireNonNull(reservation);
        Objects.requireNonNull(pkg);
        Objects.requireNonNull(charges);
        this.baseCharge = charges.baseCharge();
        this.extraCharge = charges.extraGuestCharge();
        this.discountAmount = charges.longStayDiscount();
        this.taxAmount = charges.taxAmount();
        this.grossTotal = charges.grossTotal();
        this.advanceApplied = charges.advanceApplied();
        this.balanceDue = charges.balanceDue();
        this.refundDue = charges.refundDue();
        this.createdAt = Objects.requireNonNull(createdAt);
        this.roomIdSnapshot = reservation.getRoom().getId();
        this.roomTypeSnapshot = reservation.getRoom().getRoomType().name();
        this.bedTypeSnapshot = reservation.getRoom().getBedType().name();
        this.dailyRateSnapshot = pkg.getDailyRate();
        this.guestNameSnapshot = reservation.getGuest().getFullName();
        this.nights = Math.toIntExact(reservation.getStay().nights());
        this.guestCount = reservation.getGuestCount();
        this.voided = false;
    }

    /** Replace active billing snapshot only after a validated reservation amendment. */
    public void reprice(RoomPackageEntity pkg, InvoiceBreakdown charges) {
        if (voided) throw new IllegalArgumentException("Cannot reprice a voided invoice");
        Objects.requireNonNull(pkg);
        Objects.requireNonNull(charges);
        this.baseCharge = charges.baseCharge();
        this.extraCharge = charges.extraGuestCharge();
        this.discountAmount = charges.longStayDiscount();
        this.taxAmount = charges.taxAmount();
        this.grossTotal = charges.grossTotal();
        this.advanceApplied = charges.advanceApplied();
        this.balanceDue = charges.balanceDue();
        this.refundDue = charges.refundDue();
        this.dailyRateSnapshot = pkg.getDailyRate();
        this.nights = Math.toIntExact(reservation.getStay().nights());
        this.guestCount = reservation.getGuestCount();
    }

    public void voidInvoice() { this.voided = true; }
    public UUID getId() { return id; }
    public BigDecimal getBaseCharge() { return baseCharge; }
    public BigDecimal getExtraCharge() { return extraCharge; }
    public BigDecimal getDiscountAmount() { return discountAmount; }
    public BigDecimal getTaxAmount() { return taxAmount; }
    public BigDecimal getGrossTotal() { return grossTotal; }
    public BigDecimal getAdvanceApplied() { return advanceApplied; }
    public BigDecimal getBalanceDue() { return balanceDue; }
    public BigDecimal getRefundDue() { return refundDue; }
    public BigDecimal getDailyRateSnapshot() { return dailyRateSnapshot; }
    public String getRoomIdSnapshot() { return roomIdSnapshot; }
    public String getRoomTypeSnapshot() { return roomTypeSnapshot; }
    public String getBedTypeSnapshot() { return bedTypeSnapshot; }
    public String getGuestNameSnapshot() { return guestNameSnapshot; }
    public int getNights() { return nights; }
    public int getGuestCount() { return guestCount; }
    public boolean isVoided() { return voided; }
    public ReservationEntity getReservation() { return reservation; }
}
