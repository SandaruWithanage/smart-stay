package lk.smartstay.reservation.service;

import java.util.Collection;
import java.util.Objects;
import lk.smartstay.reservation.domain.Reservation;
import lk.smartstay.reservation.domain.StayPeriod;
import lk.smartstay.room.domain.RoomOperationalStatus;

/** Deterministic predicate for room availability; DB prevents concurrent overlaps. */
public final class AvailabilityChecker {
    private AvailabilityChecker() { }

    public static boolean isAvailable(String roomId, RoomOperationalStatus operationalStatus,
                                      StayPeriod proposed, Collection<Reservation> existing) {
        Objects.requireNonNull(roomId, "roomId");
        Objects.requireNonNull(operationalStatus, "operationalStatus");
        Objects.requireNonNull(proposed, "proposed");
        Objects.requireNonNull(existing, "existing");
        if (roomId.isBlank() || operationalStatus != RoomOperationalStatus.ACTIVE) {
            return false;
        }
        return existing.stream().noneMatch(reservation ->
            reservation.roomId().equals(roomId)
                && reservation.blocksAvailability()
                && reservation.stay().overlaps(proposed));
    }
}
