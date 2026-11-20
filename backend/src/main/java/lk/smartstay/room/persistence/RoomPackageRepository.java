package lk.smartstay.room.persistence;

import lk.smartstay.room.domain.RoomType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoomPackageRepository extends JpaRepository<RoomPackageEntity, RoomType> { }
