package lk.smartstay.room.api;

import java.math.BigDecimal;
import lk.smartstay.room.domain.BedType;
import lk.smartstay.room.domain.RoomOperationalStatus;
import lk.smartstay.room.domain.RoomType;
import lk.smartstay.room.persistence.RoomEntity;
import lk.smartstay.room.persistence.RoomPackageEntity;

public record RoomView(
    String id,
    RoomType roomType,
    BedType bedType,
    int capacity,
    RoomOperationalStatus operationalStatus,
    boolean active,
    BigDecimal dailyRate
) {
    public static RoomView from(RoomEntity room, RoomPackageEntity pkg) {
        return new RoomView(room.getId(), room.getRoomType(), room.getBedType(),
            room.getCapacity(), room.getOperationalStatus(), room.isActive(), pkg.getDailyRate());
    }
}
