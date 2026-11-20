package lk.smartstay.reservation.service;

import java.time.LocalDateTime;
import java.time.Duration;
import java.util.Objects;
import lk.smartstay.reservation.domain.ReservationStatus;

/**
 * Provisional interpretation of coursework timing rules:
 * booking >= 48h before check-in; cancellation >= 24h before check-in.
 * Actual check-in local time still requires lecturer clarification.
 */
public final class BookingRules {
    private static final Duration BOOKING_LEAD = Duration.ofHours(48);
    private static final Duration CANCELLATION_LEAD = Duration.ofHours(24);
    public static final int DEFAULT_CHECK_IN_HOUR = 14;

    private BookingRules() { }

    public static void requireBookingNotice(LocalDateTime bookingTime, LocalDateTime checkInTime) {
        Objects.requireNonNull(bookingTime, "bookingTime");
        Objects.requireNonNull(checkInTime, "checkInTime");
        if (bookingTime.plus(BOOKING_LEAD).isAfter(checkInTime)) {
            throw new IllegalArgumentException("Reservation requires at least 48 hours notice");
        }
    }

    public static void requireModificationAllowed(ReservationStatus status,
                                                  LocalDateTime requestTime,
                                                  LocalDateTime originalCheckIn) {
        Objects.requireNonNull(status);
        Objects.requireNonNull(requestTime);
        Objects.requireNonNull(originalCheckIn);
        if (status != ReservationStatus.CONFIRMED) {
            throw new IllegalArgumentException("Only confirmed reservations may be modified");
        }
        if (requestTime.plus(CANCELLATION_LEAD).isAfter(originalCheckIn)) {
            throw new IllegalArgumentException("Modification prohibited within 24 hours of check-in");
        }
    }

    public static void requireCancellationAllowed(ReservationStatus status,
                                                  LocalDateTime requestTime,
                                                  LocalDateTime checkInTime) {
        Objects.requireNonNull(status, "status");
        Objects.requireNonNull(requestTime, "requestTime");
        Objects.requireNonNull(checkInTime, "checkInTime");
        if (status != ReservationStatus.CONFIRMED) {
            throw new IllegalArgumentException("Only confirmed reservations may be cancelled");
        }
        if (requestTime.plus(CANCELLATION_LEAD).isAfter(checkInTime)) {
            throw new IllegalArgumentException("Cancellation prohibited within 24 hours of check-in");
        }
    }
}
