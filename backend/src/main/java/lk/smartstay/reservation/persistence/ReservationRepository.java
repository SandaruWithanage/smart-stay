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

    boolean existsByRoom_IdAndStatusAndCheckInLessThanAndCheckOutGreaterThan(
        String roomId, ReservationStatus status, LocalDate checkout, LocalDate checkin);

    boolean existsByRoom_IdAndStatusAndCheckOutGreaterThan(
        String roomId, ReservationStatus status, LocalDate date);

    List<ReservationEntity> findByCheckInOrderById(LocalDate checkIn);
    List<ReservationEntity> findByGuest_FullNameContainingIgnoreCase(String name);
}
