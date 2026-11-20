package lk.smartstay.room.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lk.smartstay.room.domain.RoomPackage;
import lk.smartstay.room.domain.RoomType;

/**
 * Immutable coursework package catalog; V1 migration seeds matching database records.
 * Replace this with a repository-backed catalog when room inventory API is added.
 */
public final class RoomPackages {
    public static final List<RoomPackage> ALL = List.of(
        packageOf(RoomType.STANDARD, "6000", false, "1000", "0.10"),
        packageOf(RoomType.DELUXE, "9000", true, "1500", "0.12"),
        packageOf(RoomType.SUITE, "14000", true, "2000", "0.15"),
        packageOf(RoomType.FAMILY_SUITE, "18000", true, "2500", "0.15")
    );
    private static final Map<RoomType, RoomPackage> INDEX = ALL.stream()
        .collect(Collectors.toUnmodifiableMap(RoomPackage::roomType, Function.identity()));

    private RoomPackages() { }

    public static RoomPackage find(RoomType type) {
        RoomPackage result = INDEX.get(type);
        if (result == null) throw new IllegalArgumentException("Unknown room type: " + type);
        return result;
    }

    private static RoomPackage packageOf(RoomType type, String rate, boolean breakfast,
                                         String extra, String tax) {
        return new RoomPackage(type, new BigDecimal(rate), breakfast,
            new BigDecimal(extra), new BigDecimal(tax));
    }
}
