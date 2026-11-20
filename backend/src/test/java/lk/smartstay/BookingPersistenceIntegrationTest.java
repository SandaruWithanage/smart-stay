package lk.smartstay;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import lk.smartstay.guest.api.GuestRegistrationRequest;
import lk.smartstay.guest.service.GuestService;
import lk.smartstay.reservation.api.ReservationCreateRequest;
import lk.smartstay.reservation.domain.RefundStatus;
import lk.smartstay.reservation.domain.ReservationStatus;
import lk.smartstay.reservation.service.BookingManagementService;
import lk.smartstay.room.api.RoomCreateRequest;
import lk.smartstay.room.domain.BedType;
import lk.smartstay.room.domain.RoomOperationalStatus;
import lk.smartstay.room.domain.RoomType;
import lk.smartstay.room.service.RoomAvailabilityService;
import lk.smartstay.room.service.RoomManagementService;
import lk.smartstay.shared.ConflictException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Uses the PostgreSQL GitHub Actions service: migrations, JPA and business operations.
 * @Transactional rolls back test data, leaving seeded room packages intact.
 */
@SpringBootTest
@Transactional
class BookingPersistenceIntegrationTest {
    @Autowired GuestService guests;
    @Autowired RoomManagementService rooms;
    @Autowired RoomAvailabilityService availability;
    @Autowired BookingManagementService bookings;
    @Autowired lk.smartstay.reservation.service.ReservationModificationService modifications;
    @Autowired lk.smartstay.reservation.persistence.ReservationAmendmentRepository amendments;
    @Autowired lk.smartstay.identity.service.IdentityService identities;
    @Autowired lk.smartstay.identity.persistence.UserRepository accounts;

    private final LocalDate start = LocalDate.of(2030, 5, 10);

    private UUID createGuest() {
        return guests.register(new GuestRegistrationRequest(
            "P-" + UUID.randomUUID(), "Test Guest", "+94770001122", "test@example.org")).id();
    }

    private UUID createActor() {
        String email="amend-actor-"+UUID.randomUUID()+"@example.org";
        identities.register(new lk.smartstay.identity.api.RegistrationRequest(
            "Amendment Test Actor","P-"+UUID.randomUUID(),"+94770001122",email,
            "StrongTestPassword123!"));
        return accounts.findByEmail(email).orElseThrow().getId();
    }

    private String createRoom(RoomOperationalStatus status) {
        String id = "R-" + UUID.randomUUID().toString().substring(0, 8);
        rooms.create(new RoomCreateRequest(id, RoomType.DELUXE, BedType.QUEEN, 2, status));
        return id;
    }

    private ReservationCreateRequest request(UUID guest, String room, LocalDate in, LocalDate out,
                                             int count) {
        return new ReservationCreateRequest(guest, room, RoomType.DELUXE, in, out, count, true);
    }

    @Test
    void persistsBookingWithPriceSnapshotAndFiveNightDiscount() {
        UUID guest = createGuest();
        String room = createRoom(RoomOperationalStatus.ACTIVE);
        var result = bookings.create(request(guest, room, start, start.plusDays(5), 3));

        assertEquals(ReservationStatus.CONFIRMED, result.reservation().status());
        assertEquals(room, result.invoice().roomId());
        assertEquals("DELUXE", result.invoice().roomType());
        assertEquals("QUEEN", result.invoice().bedType());
        assertEquals("Test Guest", result.invoice().guestName());
        assertEquals(5, result.invoice().nights());
        assertEquals(0, new BigDecimal("45000.00").compareTo(result.invoice().baseCharge()));
        assertEquals(0, new BigDecimal("7500.00").compareTo(result.invoice().extraCharge()));
        assertEquals(0, new BigDecimal("5400.00").compareTo(result.invoice().discount()));
        assertEquals(0, new BigDecimal("5652.00").compareTo(result.invoice().tax()));
        assertEquals(0, new BigDecimal("42752.00").compareTo(result.invoice().balanceDue()));
        assertEquals(result.invoice(), bookings.invoice(result.reservation().bookingId()));
        assertEquals(result.reservation(), bookings.find(result.reservation().bookingId()));
    }

    @Test
    void bookingBlocksOverlappingDatesButAllowsSameDayTurnover() {
        UUID guest = createGuest();
        String room = createRoom(RoomOperationalStatus.ACTIVE);
        bookings.create(request(guest, room, start, start.plusDays(3), 2));

        assertTrue(availability.search(start.plusDays(1), start.plusDays(3), RoomType.DELUXE)
            .stream().noneMatch(r -> r.id().equals(room)));
        assertThrows(ConflictException.class, () -> bookings.create(request(
            guest, room, start.plusDays(2), start.plusDays(4), 2)));
        assertTrue(availability.search(start.plusDays(3), start.plusDays(4), RoomType.DELUXE)
            .stream().anyMatch(r -> r.id().equals(room)));
        assertDoesNotThrow(() -> bookings.create(request(
            guest, room, start.plusDays(3), start.plusDays(4), 2)));
    }

    @Test
    void cancellationVoidsInvoiceAndRecordsUnsettledRefund() {
        UUID guest = createGuest();
        String room = createRoom(RoomOperationalStatus.ACTIVE);
        var result = bookings.create(request(guest, room, start, start.plusDays(2), 2));

        var cancelled = bookings.cancel(result.reservation().bookingId());
        assertEquals(ReservationStatus.CANCELLED, cancelled.status());
        assertEquals(RefundStatus.PENDING, cancelled.refundStatus());
        assertEquals(0, new BigDecimal("10000.00").compareTo(cancelled.cancellationRefundDue()));
        assertTrue(bookings.invoice(cancelled.bookingId()).voided());
        assertTrue(availability.search(start, start.plusDays(2), RoomType.DELUXE)
            .stream().anyMatch(r -> r.id().equals(room)));
    }

    @Test
    void unavailableRoomOrIncorrectRoomTypeCannotBeBooked() {
        UUID guest = createGuest();
        String room = createRoom(RoomOperationalStatus.MAINTENANCE);
        assertThrows(ConflictException.class, () ->
            bookings.create(request(guest, room, start, start.plusDays(2), 2)));
        assertTrue(availability.search(start, start.plusDays(2), RoomType.DELUXE)
            .stream().noneMatch(r -> r.id().equals(room)));
    }

    @Test
    void roomCannotBeRetiredWithFutureConfirmedReservations() {
        UUID guest = createGuest();
        String room = createRoom(RoomOperationalStatus.ACTIVE);
        bookings.create(request(guest, room, start, start.plusDays(2), 2));
        assertThrows(ConflictException.class, () -> rooms.retire(room));
    }

    @Test
    void amendmentRecalculatesInvoiceWithoutSecondAdvanceAndAppendsHistory() {
        UUID guest=createGuest();
        UUID actor=createActor();
        String room=createRoom(RoomOperationalStatus.ACTIVE);
        var original=bookings.create(request(guest,room,start,start.plusDays(2),2));
        UUID id=original.reservation().bookingId();
        assertEquals(0,new BigDecimal("20160.00").compareTo(original.invoice().grossTotal()));

        var revised=modifications.change(id,new lk.smartstay.reservation.api.ReservationModifyRequest(
            start,start.plusDays(5),3),actor);

        assertEquals(5,revised.invoice().nights());
        assertEquals(3,revised.reservation().guestCount());
        assertEquals(0,new BigDecimal("52752.00").compareTo(revised.invoice().grossTotal()));
        assertEquals(0,new BigDecimal("10000.00").compareTo(revised.reservation().recordedAdvance()));
        assertEquals(0,new BigDecimal("42752.00").compareTo(revised.invoice().balanceDue()));
        assertEquals(original.invoice().invoiceId(),revised.invoice().invoiceId());
        var history=modifications.history(id);
        assertEquals(1,history.size());
        assertEquals(0,new BigDecimal("20160.00").compareTo(history.getFirst().previousGrossTotal()));
        assertEquals(0,new BigDecimal("52752.00").compareTo(history.getFirst().newGrossTotal()));
        modifications.change(id,new lk.smartstay.reservation.api.ReservationModifyRequest(
            start,start.plusDays(5),3),actor);
        assertEquals(1,amendments.count());
    }

    @Test
    void amendmentCannotOverlapAnotherConfirmedReservation() {
        UUID guest=createGuest();
        String room=createRoom(RoomOperationalStatus.ACTIVE);
        var first=bookings.create(request(guest,room,start,start.plusDays(2),2));
        bookings.create(request(guest,room,start.plusDays(2),start.plusDays(4),2));
        assertThrows(ConflictException.class,()->modifications.change(
            first.reservation().bookingId(),
            new lk.smartstay.reservation.api.ReservationModifyRequest(start,start.plusDays(3),2),
            UUID.randomUUID()));
    }

    @Test
    void cancelledBookingCannotBeModified() {
        UUID guest=createGuest();
        String room=createRoom(RoomOperationalStatus.ACTIVE);
        var original=bookings.create(request(guest,room,start,start.plusDays(2),2));
        bookings.cancel(original.reservation().bookingId());
        assertThrows(IllegalArgumentException.class,()->modifications.change(
            original.reservation().bookingId(),
            new lk.smartstay.reservation.api.ReservationModifyRequest(start,start.plusDays(3),2),
            UUID.randomUUID()));
    }

    @Test
    void searchableByBookingIdGuestNameAndCheckinDate() {
        UUID guest = createGuest();
        String room = createRoom(RoomOperationalStatus.ACTIVE);
        var created = bookings.create(request(guest, room, start, start.plusDays(2), 2));
        UUID id = created.reservation().bookingId();

        assertEquals(id, bookings.search(id, null, null).getFirst().bookingId());
        assertTrue(bookings.search(null, "tEsT gUeSt", null).stream()
            .anyMatch(r -> r.bookingId().equals(id)));
        assertTrue(bookings.search(null, null, start).stream()
            .anyMatch(r -> r.bookingId().equals(id)));
        assertThrows(IllegalArgumentException.class, () -> bookings.search(null, null, null));
    }
}
