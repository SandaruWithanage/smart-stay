package lk.smartstay.reservation.api;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

/** Same-room reservation modifications, not a second payment/advance receipt. */
public record ReservationModifyRequest(
    @NotNull LocalDate checkIn,
    @NotNull LocalDate checkOut,
    @Min(1) int guestCount
) { }
