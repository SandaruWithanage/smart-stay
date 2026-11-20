package lk.smartstay.reservation.api;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import lk.smartstay.reservation.persistence.ReservationAmendmentEntity;

public record ReservationAmendmentView(
    UUID id, LocalDate previousCheckIn, LocalDate previousCheckOut,
    LocalDate newCheckIn, LocalDate newCheckOut,
    int previousGuestCount, int newGuestCount,
    BigDecimal previousGrossTotal, BigDecimal newGrossTotal, Instant changedAt
) {
    public static ReservationAmendmentView from(ReservationAmendmentEntity row) {
        return new ReservationAmendmentView(
            row.getId(),row.getPreviousCheckIn(),row.getPreviousCheckOut(),
            row.getNewCheckIn(),row.getNewCheckOut(),row.getPreviousGuestCount(),
            row.getNewGuestCount(),row.getPreviousGrossTotal(),row.getNewGrossTotal(),row.getChangedAt());
    }
}
