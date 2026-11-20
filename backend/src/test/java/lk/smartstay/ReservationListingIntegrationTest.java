package lk.smartstay;

import java.time.LocalDate;
import java.util.UUID;
import lk.smartstay.guest.api.GuestRegistrationRequest;
import lk.smartstay.guest.service.GuestService;
import lk.smartstay.reservation.api.ReservationCreateRequest;
import lk.smartstay.reservation.service.BookingManagementService;
import lk.smartstay.reservation.service.ReservationListingService;
import lk.smartstay.room.api.RoomCreateRequest;
import lk.smartstay.room.domain.BedType;
import lk.smartstay.room.domain.RoomOperationalStatus;
import lk.smartstay.room.domain.RoomType;
import lk.smartstay.room.service.RoomManagementService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import static org.junit.jupiter.api.Assertions.*;

/** Integration tests exercise query bounds and owner filtering on PostgreSQL. */
@SpringBootTest
@Transactional
class ReservationListingIntegrationTest {
    @Autowired GuestService guests;
    @Autowired RoomManagementService rooms;
    @Autowired BookingManagementService booking;
    @Autowired ReservationListingService listing;

    private UUID guest(String name) {
        return guests.register(new GuestRegistrationRequest(
            "NIC-" + UUID.randomUUID(), name, "+94771234567", name.replace(" ","").toLowerCase()+"@example.org"
        )).id();
    }

    private String room() {
        String id="R-" + UUID.randomUUID().toString().substring(0,8);
        rooms.create(new RoomCreateRequest(id, RoomType.STANDARD,
            BedType.QUEEN,2,RoomOperationalStatus.ACTIVE));
        return id;
    }

    @Test
    void listsOnlyOwnGuestReservationsAndIncludesDeskRecords() {
        UUID alice=guest("Alice Listing");
        UUID bob=guest("Bob Listing");
        LocalDate start=LocalDate.of(2030, 8, 10);
        var a=booking.create(new ReservationCreateRequest(alice,room(),RoomType.STANDARD,
            start,start.plusDays(2),2,true)).reservation();
        var b=booking.create(new ReservationCreateRequest(bob,room(),RoomType.STANDARD,
            start,start.plusDays(3),2,true)).reservation();
        var aliceList=listing.forGuest(alice);
        assertTrue(aliceList.stream().anyMatch(x->x.bookingId().equals(a.bookingId())));
        assertTrue(aliceList.stream().noneMatch(x->x.bookingId().equals(b.bookingId())));
        assertTrue(listing.forDesk().stream().anyMatch(x->x.bookingId().equals(a.bookingId())));
        assertTrue(listing.forDesk().stream().anyMatch(x->x.bookingId().equals(b.bookingId())));
    }
}
