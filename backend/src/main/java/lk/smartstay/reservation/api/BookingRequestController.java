package lk.smartstay.reservation.api;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lk.smartstay.reservation.domain.BookingRequestStatus;
import lk.smartstay.reservation.service.BookingRequestService;
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

/** Controllers never accept a client-supplied guest/owner identity for booking requests. */
@RestController
@RequestMapping("/api")
public class BookingRequestController {
    private final BookingRequestService service;
    public BookingRequestController(BookingRequestService service) { this.service = service; }

    @PostMapping("/booking-requests")
    @ResponseStatus(HttpStatus.CREATED)
    public BookingRequestView submit(@Valid @RequestBody BookingRequestCreate input,
                                     Authentication auth) {
        return service.submit(auth, input);
    }

    @GetMapping("/booking-requests")
    public List<BookingRequestView> mine(Authentication auth) {
        return service.myRequests(auth);
    }

    @PostMapping("/booking-requests/{id}/withdraw")
    public BookingRequestView withdraw(@PathVariable UUID id, Authentication auth) {
        return service.withdraw(auth, id);
    }

    @GetMapping("/admin/booking-requests")
    public List<BookingRequestView> desk(Authentication auth,
        @RequestParam(required = false) BookingRequestStatus status) {
        return service.deskQueue(auth, status);
    }

    @PostMapping("/admin/booking-requests/{id}/approve")
    public BookingRequestView approve(@PathVariable UUID id,
        @Valid @RequestBody BookingRequestReview.Approval input, Authentication auth) {
        return service.approve(auth, id, input.advanceReceived());
    }

    @PostMapping("/admin/booking-requests/{id}/reject")
    public BookingRequestView reject(@PathVariable UUID id,
        @Valid @RequestBody BookingRequestReview.Rejection input, Authentication auth) {
        return service.reject(auth, id, input.reason());
    }
}
