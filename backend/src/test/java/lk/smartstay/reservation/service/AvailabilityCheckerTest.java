package lk.smartstay.reservation.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import lk.smartstay.reservation.domain.*;
import lk.smartstay.room.domain.RoomOperationalStatus;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AvailabilityCheckerTest {
    private final LocalDate date = LocalDate.of(2026, 12, 10);

    private Reservation reservation(ReservationStatus status) {
        return new Reservation(UUID.randomUUID(), UUID.randomUUID(), "R-101",
            new StayPeriod(date, date.plusDays(3)), 2, status, new BigDecimal("10000"));
    }

    @Test void overlappingConfirmedStayIsUnavailable() {
        assertFalse(AvailabilityChecker.isAvailable("R-101", RoomOperationalStatus.ACTIVE,
            new StayPeriod(date.plusDays(2), date.plusDays(4)),
            List.of(reservation(ReservationStatus.CONFIRMED))));
    }

    @Test void sameDayCheckoutAndCheckinIsAvailable() {
        assertTrue(AvailabilityChecker.isAvailable("R-101", RoomOperationalStatus.ACTIVE,
            new StayPeriod(date.plusDays(3), date.plusDays(4)),
            List.of(reservation(ReservationStatus.CONFIRMED))));
    }

    @Test void cancelledReservationDoesNotBlockAvailability() {
        assertTrue(AvailabilityChecker.isAvailable("R-101", RoomOperationalStatus.ACTIVE,
            new StayPeriod(date.plusDays(1), date.plusDays(2)),
            List.of(reservation(ReservationStatus.CANCELLED))));
    }

    @Test void maintenanceRoomIsUnavailable() {
        assertFalse(AvailabilityChecker.isAvailable("R-101", RoomOperationalStatus.MAINTENANCE,
            new StayPeriod(date.plusDays(4), date.plusDays(5)), List.of()));
    }
}
