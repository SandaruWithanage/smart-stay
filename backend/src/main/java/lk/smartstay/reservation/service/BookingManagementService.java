package lk.smartstay.reservation.service;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lk.smartstay.billing.api.InvoiceView;
import lk.smartstay.billing.persistence.InvoiceEntity;
import lk.smartstay.billing.persistence.InvoiceRepository;
import lk.smartstay.billing.service.PricingCalculator;
import lk.smartstay.config.TimeConfiguration;
import lk.smartstay.guest.persistence.GuestEntity;
import lk.smartstay.guest.persistence.GuestRepository;
import lk.smartstay.reservation.api.BookingReceipt;
import lk.smartstay.reservation.api.ReservationCreateRequest;
import lk.smartstay.reservation.api.ReservationView;
import lk.smartstay.reservation.domain.ReservationStatus;
import lk.smartstay.reservation.domain.StayPeriod;
import lk.smartstay.reservation.persistence.ReservationEntity;
import lk.smartstay.reservation.persistence.ReservationRepository;
import lk.smartstay.room.domain.RoomOperationalStatus;
import lk.smartstay.room.persistence.RoomEntity;
import lk.smartstay.room.persistence.RoomPackageEntity;
import lk.smartstay.room.persistence.RoomPackageRepository;
import lk.smartstay.room.persistence.RoomRepository;
import lk.smartstay.shared.ConflictException;
import lk.smartstay.shared.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Booking operations are transactionally isolated against a pessimistic room lock.
 * PostgreSQL exclusion constraint is the final safeguard against overlapping stays.
 * The 10,000 LKR advance is a manually confirmed desk receipt, not a gateway charge.
 */
@Service
public class BookingManagementService {
    private static final BigDecimal FIXED_ADVANCE = new BigDecimal("10000.00");
    private final RoomRepository rooms;
    private final RoomPackageRepository packages;
    private final GuestRepository guests;
    private final ReservationRepository bookings;
    private final InvoiceRepository invoices;
    private final Clock clock;

    public BookingManagementService(RoomRepository rooms, RoomPackageRepository packages,
                                    GuestRepository guests, ReservationRepository bookings,
                                    InvoiceRepository invoices, Clock clock) {
        this.rooms = rooms;
        this.packages = packages;
        this.guests = guests;
        this.bookings = bookings;
        this.invoices = invoices;
        this.clock = clock;
    }

    @Transactional
    public BookingReceipt create(ReservationCreateRequest request) {
        if (!request.advanceReceived()) {
            throw new IllegalArgumentException("Staff must confirm receipt of the 10,000 LKR advance");
        }
        StayPeriod stay = new StayPeriod(request.checkIn(), request.checkOut());
        Instant now = clock.instant();
        LocalDateTime localNow = LocalDateTime.ofInstant(now, TimeConfiguration.HOTEL_ZONE);
        BookingRules.requireBookingNotice(localNow,
            stay.checkIn().atTime(BookingRules.DEFAULT_CHECK_IN_HOUR, 0));

        GuestEntity guest = guests.findById(request.guestId())
            .orElseThrow(() -> new NotFoundException("Guest not found"));

        // Lock room row before evaluating availability to serialize room-specific changes.
        RoomEntity room = rooms.lockById(request.roomId())
            .orElseThrow(() -> new NotFoundException("Room not found"));
        if (!room.isActive() || room.getOperationalStatus() != RoomOperationalStatus.ACTIVE) {
            throw new ConflictException("Room is not available for reservations");
        }
        if (room.getRoomType() != request.roomType()) {
            throw new IllegalArgumentException("Requested room type does not match room");
        }
        if (request.guestCount() <= 0) {
            throw new IllegalArgumentException("Guest count must be positive");
        }
        if (bookings.existsByRoom_IdAndStatusAndCheckInLessThanAndCheckOutGreaterThan(
            room.getId(), ReservationStatus.CONFIRMED, stay.checkOut(), stay.checkIn())) {
            throw new ConflictException("The room is reserved for an overlapping stay");
        }

        RoomPackageEntity pkg = packages.findById(room.getRoomType())
            .orElseThrow(() -> new NotFoundException("Room package not found"));
        var breakdown = PricingCalculator.calculate(pkg.toDomain(), stay,
            room.getCapacity(), request.guestCount(), FIXED_ADVANCE);

        ReservationEntity reservation = bookings.saveAndFlush(new ReservationEntity(
            UUID.randomUUID(), guest, room, stay, request.guestCount(), FIXED_ADVANCE, now));
        InvoiceEntity invoice = invoices.saveAndFlush(new InvoiceEntity(
            UUID.randomUUID(), reservation, pkg, breakdown, now));
        return new BookingReceipt(ReservationView.from(reservation), InvoiceView.from(invoice));
    }

    @Transactional
    public ReservationView cancel(UUID bookingId) {
        ReservationEntity booking = bookings.lockById(bookingId)
            .orElseThrow(() -> new NotFoundException("Reservation not found"));
        LocalDateTime localNow = LocalDateTime.ofInstant(clock.instant(), TimeConfiguration.HOTEL_ZONE);
        BookingRules.requireCancellationAllowed(booking.getStatus(), localNow,
            booking.getCheckIn().atTime(BookingRules.DEFAULT_CHECK_IN_HOUR, 0));
        booking.cancel(clock.instant());
        InvoiceEntity invoice = invoices.findByReservation_Id(bookingId)
            .orElseThrow(() -> new NotFoundException("Invoice not found"));
        invoice.voidInvoice();
        bookings.flush();
        invoices.flush();
        return ReservationView.from(booking);
    }

    @Transactional(readOnly = true)
    public ReservationView find(UUID bookingId) {
        return ReservationView.from(bookings.findById(bookingId)
            .orElseThrow(() -> new NotFoundException("Reservation not found")));
    }

    @Transactional(readOnly = true)
    public InvoiceView invoice(UUID bookingId) {
        return InvoiceView.from(invoices.findByReservation_Id(bookingId)
            .orElseThrow(() -> new NotFoundException("Invoice not found")));
    }

    @Transactional(readOnly = true)
    public List<ReservationView> search(UUID bookingId, String guestName, LocalDate checkIn) {
        int count = (bookingId != null ? 1 : 0)
            + (guestName != null && !guestName.isBlank() ? 1 : 0)
            + (checkIn != null ? 1 : 0);
        if (count != 1) throw new IllegalArgumentException("Provide exactly one reservation search filter");
        if (bookingId != null) return List.of(find(bookingId));
        if (guestName != null && !guestName.isBlank()) {
            return bookings.findByGuest_FullNameContainingIgnoreCase(guestName.trim())
                .stream().map(ReservationView::from).toList();
        }
        return bookings.findByCheckInOrderById(checkIn)
            .stream().map(ReservationView::from).toList();
    }
}
