package lk.smartstay.reservation.persistence;

import jakarta.persistence.LockModeType;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lk.smartstay.reservation.domain.ReservationStatus;
import org.springframework.data.domain.Pageable;
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
      from ReservationEntity b where b.room.id = :roomId and b.id <> :excludeId
        and b.status = :status and b.checkIn < :checkout and b.checkOut > :checkin
      """)
    boolean hasConflictExcluding(@Param("roomId") String roomId,
        @Param("excludeId") UUID excludeId, @Param("status") ReservationStatus status,
        @Param("checkout") LocalDate checkout, @Param("checkin") LocalDate checkin);

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

    @Query("""
      select b from ReservationEntity b
      join fetch b.guest join fetch b.room
      where b.guest.id = :guestId order by b.createdAt desc
      """)
    List<ReservationEntity> recentForGuest(@Param("guestId") UUID guestId, Pageable page);

    @Query("""
      select b from ReservationEntity b
      join fetch b.guest join fetch b.room
      order by b.createdAt desc
      """)
    List<ReservationEntity> recentForDesk(Pageable page);
}
