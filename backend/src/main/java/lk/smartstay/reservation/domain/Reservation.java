package lk.smartstay.reservation.domain;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

/** Immutable booking snapshot. Storage mapping is intentionally a separate responsibility. */
public record Reservation(UUID id, UUID guestId, String roomId, StayPeriod stay,
                          int guestCount, ReservationStatus status, BigDecimal advancePaid) {
    public Reservation {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(guestId, "guestId");
        Objects.requireNonNull(roomId, "roomId");
        Objects.requireNonNull(stay, "stay");
        Objects.requireNonNull(status, "status");
        Objects.requireNonNull(advancePaid, "advancePaid");
        if (roomId.isBlank() || guestCount <= 0 || advancePaid.signum() < 0) {
            throw new IllegalArgumentException("Invalid reservation values");
        }
    }

    public boolean blocksAvailability() {
        return status == ReservationStatus.CONFIRMED;
    }
}
