package lk.smartstay.reservation.api;

import java.util.List;
import lk.smartstay.identity.persistence.UserRepository;
import lk.smartstay.identity.service.AuthorizationService;
import lk.smartstay.reservation.service.ReservationListingService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Server-authoritative list endpoints: the guest identifier is resolved from JWT identity,
 * never taken from a query parameter.
 */
@RestController
public class ReservationListController {
    private final ReservationListingService lists;
    private final AuthorizationService authz;
    private final UserRepository users;

    public ReservationListController(ReservationListingService lists,
                                     AuthorizationService authz, UserRepository users) {
        this.lists = lists;
        this.authz = authz;
        this.users = users;
    }

    @GetMapping("/api/me/reservations")
    public List<ReservationView> mine(Authentication user) {
        authz.require(user, "reservation.read.own");
        var account = users.findById(authz.userId(user))
            .orElseThrow(() -> new AccessDeniedException("Account not found"));
        if (account.getGuestId() == null) throw new AccessDeniedException("Guest account required");
        return lists.forGuest(account.getGuestId());
    }

    @GetMapping("/api/admin/reservations")
    public List<ReservationView> desk(Authentication user) {
        authz.require(user, "reservation.read.any");
        return lists.forDesk();
    }
}
