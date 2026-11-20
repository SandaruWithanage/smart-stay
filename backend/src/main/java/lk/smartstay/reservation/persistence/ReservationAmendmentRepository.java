package lk.smartstay.reservation.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationAmendmentRepository extends JpaRepository<ReservationAmendmentEntity,UUID> {
    List<ReservationAmendmentEntity> findByReservation_IdOrderByChangedAtDesc(UUID id,Pageable page);
}
