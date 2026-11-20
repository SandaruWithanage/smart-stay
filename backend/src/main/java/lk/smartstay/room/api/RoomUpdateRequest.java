package lk.smartstay.room.api;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lk.smartstay.room.domain.BedType;
import lk.smartstay.room.domain.RoomOperationalStatus;
import lk.smartstay.room.domain.RoomType;

public record RoomUpdateRequest(
    @NotNull RoomType roomType,
    @NotNull BedType bedType,
    @Min(1) int capacity,
    @NotNull RoomOperationalStatus operationalStatus
) { }
