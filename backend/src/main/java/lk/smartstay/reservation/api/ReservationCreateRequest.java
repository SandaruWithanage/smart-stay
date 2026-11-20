package lk.smartstay.reservation.api;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;
import lk.smartstay.room.domain.RoomType;

/**
 * Desk-operated confirmation, not evidence that an online gateway charged funds.
 * Only authorized staff must be permitted to invoke this API in the auth milestone.
 */
public record ReservationCreateRequest(
    @NotNull UUID guestId,
    @NotBlank String roomId,
    @NotNull RoomType roomType,
    @NotNull LocalDate checkIn,
    @NotNull LocalDate checkOut,
    @Min(1) int guestCount,
    @AssertTrue boolean advanceReceived
) { }
