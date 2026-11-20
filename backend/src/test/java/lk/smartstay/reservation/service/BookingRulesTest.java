package lk.smartstay.reservation.service;

import java.time.LocalDateTime;
import lk.smartstay.reservation.domain.ReservationStatus;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class BookingRulesTest {
    private final LocalDateTime checkIn = LocalDateTime.of(2026, 12, 20, 14, 0);

    @Test void exactly48HoursAdvanceIsAllowed() {
        assertDoesNotThrow(() -> BookingRules.requireBookingNotice(checkIn.minusHours(48), checkIn));
    }

    @Test void oneMinuteInsideAdvanceWindowIsRejected() {
        assertThrows(IllegalArgumentException.class, () ->
            BookingRules.requireBookingNotice(checkIn.minusHours(48).plusMinutes(1), checkIn));
    }

    @Test void exactly24HoursCancellationIsAllowed() {
        assertDoesNotThrow(() -> BookingRules.requireCancellationAllowed(
            ReservationStatus.CONFIRMED, checkIn.minusHours(24), checkIn));
    }

    @Test void cancellationInside24HoursIsRejected() {
        assertThrows(IllegalArgumentException.class, () ->
            BookingRules.requireCancellationAllowed(
                ReservationStatus.CONFIRMED, checkIn.minusHours(23), checkIn));
    }
    @Test void modificationCutoffUsesOriginalCheckIn() {
        assertDoesNotThrow(()->BookingRules.requireModificationAllowed(
            ReservationStatus.CONFIRMED,checkIn.minusHours(25),checkIn));
        assertThrows(IllegalArgumentException.class,()->BookingRules.requireModificationAllowed(
            ReservationStatus.CONFIRMED,checkIn.minusHours(23),checkIn));
        assertThrows(IllegalArgumentException.class,()->BookingRules.requireModificationAllowed(
            ReservationStatus.CANCELLED,checkIn.minusHours(30),checkIn));
    }
}

