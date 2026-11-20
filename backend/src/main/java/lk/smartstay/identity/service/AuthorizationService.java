package lk.smartstay.identity.service;

import java.util.UUID;
import lk.smartstay.identity.persistence.UserRepository;
import lk.smartstay.reservation.persistence.ReservationRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

/**
 * Constrained ABAC: explicit DB grants plus Java-validated ownership.
 * A client-supplied guest ID or booking ID cannot grant itself access.
 */
@Service
public class AuthorizationService {
    private final UserRepository users;
    private final ReservationRepository reservations;

    public AuthorizationService(UserRepository users, ReservationRepository reservations) {
        this.users = users;
        this.reservations = reservations;
    }

    public UUID userId(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            throw new AccessDeniedException("Authentication required");
        }
        try {
            return UUID.fromString(auth.getName());
        } catch (IllegalArgumentException e) {
            throw new AccessDeniedException("Invalid authenticated principal");
        }
    }

    public boolean has(Authentication auth, String permission) {
        return auth != null && auth.isAuthenticated()
            && auth.getAuthorities().stream()
                .anyMatch(granted -> granted.getAuthority().equals("PERM_" + permission));
    }

    public void require(Authentication auth, String permission) {
        if (!has(auth, permission)) {
            throw new AccessDeniedException("Access denied");
        }
    }

    public void requireGuest(Authentication auth, UUID guestId, String any, String own) {
        if (has(auth, any)) return;
        if (!has(auth, own)) throw new AccessDeniedException("Access denied");
        var user = users.findById(userId(auth))
            .orElseThrow(() -> new AccessDeniedException("Access denied"));
        if (user.getGuestId() == null || !user.getGuestId().equals(guestId)) {
            throw new AccessDeniedException("Access denied");
        }
    }

    public void requireReservation(Authentication auth, UUID bookingId, String any, String own) {
        if (has(auth, any)) return;
        if (!has(auth, own)) throw new AccessDeniedException("Access denied");
        var user = users.findById(userId(auth))
            .orElseThrow(() -> new AccessDeniedException("Access denied"));
        var booking = reservations.findById(bookingId)
            .orElseThrow(() -> new AccessDeniedException("Access denied"));
        if (user.getGuestId() == null
            || !user.getGuestId().equals(booking.getGuest().getId())) {
            throw new AccessDeniedException("Access denied");
        }
    }
}
