package lk.smartstay.room.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lk.smartstay.room.domain.RoomPackage;
import lk.smartstay.room.domain.RoomType;

@Entity
@Table(name = "room_packages")
public class RoomPackageEntity {
    @Id
    @Enumerated(EnumType.STRING)
    @Column(name = "room_type", length = 32)
    private RoomType roomType;

    @Column(name = "daily_rate", nullable = false, precision = 12, scale = 2)
    private BigDecimal dailyRate;

    @Column(name = "breakfast_included", nullable = false)
    private boolean breakfastIncluded;

    @Column(name = "extra_guest_rate", nullable = false, precision = 12, scale = 2)
    private BigDecimal extraGuestRate;

    @Column(name = "tax_rate", nullable = false, precision = 5, scale = 4)
    private BigDecimal taxRate;

    protected RoomPackageEntity() { }

    public RoomType getRoomType() { return roomType; }
    public BigDecimal getDailyRate() { return dailyRate; }
    public RoomPackage toDomain() {
        return new RoomPackage(roomType, dailyRate, breakfastIncluded, extraGuestRate, taxRate);
    }
}
