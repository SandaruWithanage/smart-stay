package lk.smartstay.room.api;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lk.smartstay.room.domain.BedType;
import lk.smartstay.room.domain.RoomOperationalStatus;
import lk.smartstay.room.domain.RoomType;

public record RoomCreateRequest(
    @NotBlank @Size(max = 32) String id,
    @NotNull RoomType roomType,
    @NotNull BedType bedType,
    @Min(1) int capacity,
    @NotNull RoomOperationalStatus operationalStatus
) { }
