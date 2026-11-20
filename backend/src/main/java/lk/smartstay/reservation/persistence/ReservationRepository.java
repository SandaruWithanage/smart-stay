package lk.smartstay.reservation.persistence;

import jakarta.persistence.LockModeType;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lk.smartstay.reservation.domain.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReservationRepository extends JpaRepository<ReservationEntity, UUID> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select b from ReservationEntity b where b.id = :id")
    Optional<ReservationEntity> lockById(@Param("id") UUID id);

    @Query("""
      select case when count(b) > 0 then true else false end
      from ReservationEntity b where b.room.id = :roomId and b.status = :status
        and b.checkIn < :checkout and b.checkOut > :checkin
      """)
    boolean hasConflict(@Param("roomId") String roomId, @Param("status") ReservationStatus status,
                        @Param("checkout") LocalDate checkout, @Param("checkin") LocalDate checkin);

    @Query("""
      select case when count(b) > 0 then true else false end
      from ReservationEntity b where b.room.id = :roomId and b.status = :status
        and b.checkOut > :date
      """)
    boolean hasUnfinishedBooking(@Param("roomId") String roomId,
                                 @Param("status") ReservationStatus status,
                                 @Param("date") LocalDate date);

    @Query("select b from ReservationEntity b where b.checkIn = :checkIn order by b.id")
    List<ReservationEntity> findByCheckInDate(@Param("checkIn") LocalDate checkIn);
    List<ReservationEntity> findByGuest_FullNameContainingIgnoreCase(String name);
}
