package lk.smartstay.reservation.api;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import lk.smartstay.billing.api.InvoiceView;
import lk.smartstay.reservation.service.BookingManagementService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** All reservation and invoice APIs remain blocked until JWT ownership/ABAC is implemented. */
@RestController
@RequestMapping("/api/reservations")
public class ReservationController {
    private final BookingManagementService service;

    public ReservationController(BookingManagementService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookingReceipt create(@Valid @RequestBody ReservationCreateRequest request) {
        return service.create(request);
    }

    @GetMapping
    public List<ReservationView> search(
        @RequestParam(required = false) UUID bookingId,
        @RequestParam(required = false) String guestName,
        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn) {
        return service.search(bookingId, guestName, checkIn);
    }

    @GetMapping("/{id}")
    public ReservationView find(@PathVariable UUID id) { return service.find(id); }

    @GetMapping("/{id}/invoice")
    public InvoiceView invoice(@PathVariable UUID id) { return service.invoice(id); }

    @PostMapping("/{id}/cancel")
    public ReservationView cancel(@PathVariable UUID id) { return service.cancel(id); }
}
