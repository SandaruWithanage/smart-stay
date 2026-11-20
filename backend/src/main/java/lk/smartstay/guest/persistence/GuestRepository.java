package lk.smartstay.guest.persistence;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GuestRepository extends JpaRepository<GuestEntity, UUID> {
    boolean existsByDocumentNumber(String documentNumber);
}
