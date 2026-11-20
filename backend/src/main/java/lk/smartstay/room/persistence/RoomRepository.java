package lk.smartstay.room.persistence;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import lk.smartstay.room.domain.RoomOperationalStatus;
import lk.smartstay.room.domain.RoomType;
import lk.smartstay.reservation.domain.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

public interface RoomRepository extends JpaRepository<RoomEntity, String> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from RoomEntity r where r.id = :id")
    Optional<RoomEntity> lockById(@Param("id") String id);

    @Query("""
      select r from RoomEntity r
      where r.active = true and r.operationalStatus = :operational
      and not exists (
        select b.id from ReservationEntity b
        where b.room = r and b.status = :confirmed
          and b.checkIn < :checkout and b.checkOut > :checkin
      )
      order by r.id
      """)
    List<RoomEntity> availableRooms(
        @Param("checkin") LocalDate checkIn,
        @Param("checkout") LocalDate checkOut,
        @Param("operational") RoomOperationalStatus operational,
        @Param("confirmed") ReservationStatus confirmed);

    @Query("""
      select r from RoomEntity r
      where r.active = true and r.operationalStatus = :operational and r.roomType = :type
      and not exists (
        select b.id from ReservationEntity b
        where b.room = r and b.status = :confirmed
          and b.checkIn < :checkout and b.checkOut > :checkin
      )
      order by r.id
      """)
    List<RoomEntity> availableRoomsOfType(
        @Param("checkin") LocalDate checkIn,
        @Param("checkout") LocalDate checkOut,
        @Param("operational") RoomOperationalStatus operational,
        @Param("confirmed") ReservationStatus confirmed,
        @Param("type") RoomType type);
}
