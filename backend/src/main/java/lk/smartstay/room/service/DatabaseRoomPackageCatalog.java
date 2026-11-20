package lk.smartstay.room.service;

import java.util.List;
import lk.smartstay.room.domain.RoomPackage;
import lk.smartstay.room.persistence.RoomPackageRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Cache immutable seeded room-package reference data; never cache booking availability. */
@Service
public class DatabaseRoomPackageCatalog {
    private final RoomPackageRepository repository;

    public DatabaseRoomPackageCatalog(RoomPackageRepository repository) {
        this.repository = repository;
    }

    @Cacheable("roomPackages")
    @Transactional(readOnly = true)
    public List<RoomPackage> all() {
        return repository.findAll().stream()
            .map(entity -> entity.toDomain())
            .sorted(java.util.Comparator.comparing(pkg -> pkg.roomType().ordinal()))
            .toList();
    }
}
