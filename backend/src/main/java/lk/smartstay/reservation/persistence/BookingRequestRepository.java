package lk.smartstay.reservation.persistence;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lk.smartstay.reservation.domain.BookingRequestStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookingRequestRepository extends JpaRepository<BookingRequestEntity, UUID> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from BookingRequestEntity r where r.id = :id")
    Optional<BookingRequestEntity> lockById(@Param("id") UUID id);

    @Query("""
      select r from BookingRequestEntity r join fetch r.guest join fetch r.room
      where r.requestedBy = :userId order by r.createdAt desc
      """)
    List<BookingRequestEntity> recentForUser(@Param("userId") UUID userId, Pageable pageable);

    @Query("""
      select r from BookingRequestEntity r join fetch r.guest join fetch r.room
      order by r.createdAt desc
      """)
    List<BookingRequestEntity> recentForDesk(Pageable pageable);

    @Query("""
      select r from BookingRequestEntity r join fetch r.guest join fetch r.room
      where r.status = :status order by r.createdAt desc
      """)
    List<BookingRequestEntity> recentForDeskByStatus(
        @Param("status") BookingRequestStatus status, Pageable pageable);
}
