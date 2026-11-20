package lk.smartstay.reservation.domain;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/**
 * Half-open stay interval [checkIn, checkOut). Checkout-day arrivals are permitted.
 */
public record StayPeriod(LocalDate checkIn, LocalDate checkOut) {
    public StayPeriod {
        Objects.requireNonNull(checkIn, "checkIn");
        Objects.requireNonNull(checkOut, "checkOut");
        if (!checkOut.isAfter(checkIn)) {
            throw new IllegalArgumentException("checkOut must be after checkIn");
        }
    }

    public long nights() {
        return ChronoUnit.DAYS.between(checkIn, checkOut);
    }

    public boolean overlaps(StayPeriod other) {
        Objects.requireNonNull(other, "other");
        return checkIn.isBefore(other.checkOut()) && checkOut.isAfter(other.checkIn());
    }
}
