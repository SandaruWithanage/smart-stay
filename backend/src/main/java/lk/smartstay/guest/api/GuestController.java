package lk.smartstay.guest.api;

import jakarta.validation.Valid;
import java.util.UUID;
import lk.smartstay.guest.service.GuestService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** No anonymous guest-specific access until authenticated ownership exists. */
@RestController
@RequestMapping("/api/guests")
public class GuestController {
    private final GuestService service;

    public GuestController(GuestService service) { this.service = service; }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public GuestView register(@Valid @RequestBody GuestRegistrationRequest request) {
        return service.register(request);
    }

    @GetMapping("/{id}")
    public GuestView find(@PathVariable UUID id) { return service.find(id); }
}
