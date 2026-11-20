package lk.smartstay.reservation.api;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import lk.smartstay.billing.api.InvoiceView;
import lk.smartstay.identity.service.AuthorizationService;
import lk.smartstay.reservation.service.BookingManagementService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reservations")
public class ReservationController {
    private final BookingManagementService service;
    private final AuthorizationService authz;

    public ReservationController(BookingManagementService service, AuthorizationService authz) {
        this.service = service;
        this.authz = authz;
    }

    /**
     * Desk-only confirmation. Guest accounts cannot claim they received an advance.
     * A separate pending-reservation flow is needed for online guest bookings.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookingReceipt create(@Valid @RequestBody ReservationCreateRequest request,
                                 Authentication user) {
        authz.require(user, "reservation.create.any");
        return service.create(request);
    }

    @GetMapping
    public List<ReservationView> search(
        @RequestParam(required = false) UUID bookingId,
        @RequestParam(required = false) String guestName,
        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
        Authentication user) {
        if (bookingId != null && guestName == null && checkIn == null) {
            authz.requireReservation(user, bookingId, "reservation.read.any", "reservation.read.own");
        } else {
            authz.require(user, "reservation.read.any");
        }
        return service.search(bookingId, guestName, checkIn);
    }

    @GetMapping("/{id}")
    public ReservationView find(@PathVariable UUID id, Authentication user) {
        authz.requireReservation(user, id, "reservation.read.any", "reservation.read.own");
        return service.find(id);
    }

    @GetMapping("/{id}/invoice")
    public InvoiceView invoice(@PathVariable UUID id, Authentication user) {
        authz.requireReservation(user, id, "invoice.read.any", "invoice.read.own");
        return service.invoice(id);
    }

    @PostMapping("/{id}/cancel")
    public ReservationView cancel(@PathVariable UUID id, Authentication user) {
        authz.requireReservation(user, id, "reservation.cancel.any", "reservation.cancel.own");
        return service.cancel(id);
    }
}
