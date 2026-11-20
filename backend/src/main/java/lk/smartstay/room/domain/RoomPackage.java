package lk.smartstay.room.domain;

import java.math.BigDecimal;
import java.util.Objects;

public record RoomPackage(
    RoomType roomType,
    BigDecimal dailyRate,
    boolean breakfastIncluded,
    BigDecimal extraGuestRate,
    BigDecimal taxRate
) {
    public RoomPackage {
        Objects.requireNonNull(roomType, "roomType");
        requireNonNegative(dailyRate, "dailyRate");
        requireNonNegative(extraGuestRate, "extraGuestRate");
        requireNonNegative(taxRate, "taxRate");
        if (taxRate.compareTo(BigDecimal.ONE) > 0) {
            throw new IllegalArgumentException("taxRate cannot exceed 1");
        }
    }

    private static void requireNonNegative(BigDecimal value, String field) {
        Objects.requireNonNull(value, field);
        if (value.signum() < 0) {
            throw new IllegalArgumentException(field + " cannot be negative");
        }
    }
}
