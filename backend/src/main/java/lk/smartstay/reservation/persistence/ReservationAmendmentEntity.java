package lk.smartstay.reservation.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

/** Append-only reservation amendment record; original invoice values are preserved here. */
@Entity
@Table(name="reservation_amendments")
public class ReservationAmendmentEntity {
    @Id private UUID id;
    @ManyToOne(fetch=FetchType.LAZY,optional=false)
    @JoinColumn(name="reservation_id",nullable=false)
    private ReservationEntity reservation;
    @Column(name="changed_by",nullable=false)
    private UUID changedBy;
    @Column(name="previous_check_in",nullable=false)
    private LocalDate previousCheckIn;
    @Column(name="previous_check_out",nullable=false)
    private LocalDate previousCheckOut;
    @Column(name="new_check_in",nullable=false)
    private LocalDate newCheckIn;
    @Column(name="new_check_out",nullable=false)
    private LocalDate newCheckOut;
    @Column(name="previous_guest_count",nullable=false)
    private int previousGuestCount;
    @Column(name="new_guest_count",nullable=false)
    private int newGuestCount;
    @Column(name="previous_gross_total",nullable=false,precision=12,scale=2)
    private BigDecimal previousGrossTotal;
    @Column(name="new_gross_total",nullable=false,precision=12,scale=2)
    private BigDecimal newGrossTotal;
    @Column(name="changed_at",nullable=false)
    private Instant changedAt;

    protected ReservationAmendmentEntity() {}

    public ReservationAmendmentEntity(UUID id, ReservationEntity reservation, UUID changedBy,
                                      LocalDate previousCheckIn, LocalDate previousCheckOut,
                                      LocalDate newCheckIn, LocalDate newCheckOut,
                                      int previousGuestCount, int newGuestCount,
                                      BigDecimal previousGrossTotal, BigDecimal newGrossTotal,
                                      Instant changedAt) {
        this.id=Objects.requireNonNull(id);
        this.reservation=Objects.requireNonNull(reservation);
        this.changedBy=Objects.requireNonNull(changedBy);
        this.previousCheckIn=Objects.requireNonNull(previousCheckIn);
        this.previousCheckOut=Objects.requireNonNull(previousCheckOut);
        this.newCheckIn=Objects.requireNonNull(newCheckIn);
        this.newCheckOut=Objects.requireNonNull(newCheckOut);
        this.previousGuestCount=previousGuestCount;
        this.newGuestCount=newGuestCount;
        this.previousGrossTotal=Objects.requireNonNull(previousGrossTotal);
        this.newGrossTotal=Objects.requireNonNull(newGrossTotal);
        this.changedAt=Objects.requireNonNull(changedAt);
    }

    public UUID getId(){return id;}
    public ReservationEntity getReservation(){return reservation;}
    public UUID getChangedBy(){return changedBy;}
    public LocalDate getPreviousCheckIn(){return previousCheckIn;}
    public LocalDate getPreviousCheckOut(){return previousCheckOut;}
    public LocalDate getNewCheckIn(){return newCheckIn;}
    public LocalDate getNewCheckOut(){return newCheckOut;}
    public int getPreviousGuestCount(){return previousGuestCount;}
    public int getNewGuestCount(){return newGuestCount;}
    public BigDecimal getPreviousGrossTotal(){return previousGrossTotal;}
    public BigDecimal getNewGrossTotal(){return newGrossTotal;}
    public Instant getChangedAt(){return changedAt;}
}
