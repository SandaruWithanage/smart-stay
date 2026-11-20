package lk.smartstay.reservation.service;

import java.util.List;
import java.util.UUID;
import lk.smartstay.reservation.api.ReservationView;
import lk.smartstay.reservation.persistence.ReservationRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Bounded lists avoid accidentally exposing or loading the full hotel ledger. */
@Service
public class ReservationListingService {
    private final ReservationRepository repository;
    public ReservationListingService(ReservationRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<ReservationView> forGuest(UUID guestId) {
        return repository.recentForGuest(guestId, PageRequest.of(0, 50))
            .stream().map(ReservationView::from).toList();
    }

    @Transactional(readOnly = true)
    public List<ReservationView> forDesk() {
        return repository.recentForDesk(PageRequest.of(0, 50))
            .stream().map(ReservationView::from).toList();
    }
}
