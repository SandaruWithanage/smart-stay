package lk.smartstay.reservation.service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lk.smartstay.billing.api.InvoiceView;
import lk.smartstay.billing.persistence.InvoiceRepository;
import lk.smartstay.billing.service.PricingCalculator;
import lk.smartstay.config.TimeConfiguration;
import lk.smartstay.reservation.api.BookingReceipt;
import lk.smartstay.reservation.api.ReservationAmendmentView;
import lk.smartstay.reservation.api.ReservationModifyRequest;
import lk.smartstay.reservation.api.ReservationView;
import lk.smartstay.reservation.domain.ReservationStatus;
import lk.smartstay.reservation.domain.StayPeriod;
import lk.smartstay.reservation.persistence.ReservationAmendmentEntity;
import lk.smartstay.reservation.persistence.ReservationAmendmentRepository;
import lk.smartstay.reservation.persistence.ReservationRepository;
import lk.smartstay.room.domain.RoomOperationalStatus;
import lk.smartstay.room.persistence.RoomPackageRepository;
import lk.smartstay.room.persistence.RoomRepository;
import lk.smartstay.shared.ConflictException;
import lk.smartstay.shared.NotFoundException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Same-room amendment. The original advance is carried forward, never collected again.
 * Lock room then booking, revalidate overlap, update invoice, append immutable audit entry.
 */
@Service
public class ReservationModificationService {
    private final ReservationRepository reservations;
    private final ReservationAmendmentRepository history;
    private final InvoiceRepository invoices;
    private final RoomRepository rooms;
    private final RoomPackageRepository packages;
    private final Clock clock;

    public ReservationModificationService(ReservationRepository reservations,
        ReservationAmendmentRepository history, InvoiceRepository invoices,
        RoomRepository rooms, RoomPackageRepository packages, Clock clock) {
        this.reservations=reservations;
        this.history=history;
        this.invoices=invoices;
        this.rooms=rooms;
        this.packages=packages;
        this.clock=clock;
    }

    @Transactional
    public BookingReceipt change(UUID bookingId, ReservationModifyRequest input, UUID actorId) {
        // Determine the original room, then lock in the same room-first order as booking.create.
        var initial=reservations.findById(bookingId)
            .orElseThrow(() -> new NotFoundException("Reservation not found"));
        var room=rooms.lockById(initial.getRoom().getId())
            .orElseThrow(() -> new NotFoundException("Room not found"));
        var booking=reservations.lockById(bookingId)
            .orElseThrow(() -> new NotFoundException("Reservation not found"));
        if (!booking.getRoom().getId().equals(room.getId())) {
            throw new ConflictException("Booking room changed concurrently");
        }

        LocalDateTime now=LocalDateTime.ofInstant(clock.instant(), TimeConfiguration.HOTEL_ZONE);
        BookingRules.requireModificationAllowed(booking.getStatus(),now,
            booking.getCheckIn().atTime(BookingRules.DEFAULT_CHECK_IN_HOUR,0));
        StayPeriod proposed=new StayPeriod(input.checkIn(),input.checkOut());
        if (!proposed.checkIn().equals(booking.getCheckIn())) {
            BookingRules.requireBookingNotice(now,
                proposed.checkIn().atTime(BookingRules.DEFAULT_CHECK_IN_HOUR,0));
        }
        if (input.guestCount()<1 || input.guestCount()>50) {
            throw new IllegalArgumentException("Guest count must be between 1 and 50");
        }
        if (!room.isActive() || room.getOperationalStatus()!=RoomOperationalStatus.ACTIVE) {
            throw new ConflictException("Room cannot be modified while inactive or under maintenance");
        }

        var invoice=invoices.findByReservation_Id(bookingId)
            .orElseThrow(() -> new NotFoundException("Invoice not found"));
        if (invoice.isVoided() || booking.getStatus()!=ReservationStatus.CONFIRMED) {
            throw new ConflictException("Cannot modify a cancelled reservation or void invoice");
        }

        if (booking.getCheckIn().equals(input.checkIn())
            && booking.getCheckOut().equals(input.checkOut())
            && booking.getGuestCount()==input.guestCount()) {
            return new BookingReceipt(ReservationView.from(booking),InvoiceView.from(invoice));
        }
        if (reservations.hasConflictExcluding(room.getId(),bookingId,
                ReservationStatus.CONFIRMED,proposed.checkOut(),proposed.checkIn())) {
            throw new ConflictException("Room already reserved for the new dates");
        }

        var pkg=packages.findById(room.getRoomType())
            .orElseThrow(() -> new NotFoundException("Room package not found"));
        var breakdown=PricingCalculator.calculate(pkg.toDomain(),proposed,room.getCapacity(),
            input.guestCount(),booking.getAdvancePaid());

        var oldCheckIn=booking.getCheckIn();
        var oldCheckOut=booking.getCheckOut();
        int oldCount=booking.getGuestCount();
        var oldGross=invoice.getGrossTotal();
        Instant changedAt=clock.instant();

        booking.reschedule(proposed,input.guestCount());
        invoice.reprice(pkg,breakdown);

        // Flush the changed occupancy and current invoice before recording the amendment.
        // PostgreSQL's exclusion constraint is the final concurrent-overlap safeguard.
        reservations.flush();
        invoices.flush();
        history.saveAndFlush(new ReservationAmendmentEntity(UUID.randomUUID(),booking,actorId,
            oldCheckIn,oldCheckOut,proposed.checkIn(),proposed.checkOut(),oldCount,
            input.guestCount(),oldGross,breakdown.grossTotal(),changedAt));
        return new BookingReceipt(ReservationView.from(booking),InvoiceView.from(invoice));
    }

    @Transactional(readOnly=true)
    public List<ReservationAmendmentView> history(UUID bookingId) {
        return history.findByReservation_IdOrderByChangedAtDesc(bookingId,PageRequest.of(0,50))
            .stream().map(ReservationAmendmentView::from).toList();
    }
}
