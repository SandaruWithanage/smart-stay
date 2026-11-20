package lk.smartstay.guest.api;

import jakarta.validation.Valid;
import java.util.UUID;
import lk.smartstay.guest.service.GuestService;
import lk.smartstay.identity.service.AuthorizationService;
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
@RequestMapping("/api/guests")
public class GuestController {
    private final GuestService service;
    private final AuthorizationService authz;

    public GuestController(GuestService service, AuthorizationService authz) {
        this.service = service;
        this.authz = authz;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public GuestView register(@Valid @RequestBody GuestRegistrationRequest request, Authentication auth) {
        authz.require(auth, "guest.create");
        return service.register(request);
    }

    @GetMapping
    public GuestView findByDocument(@RequestParam String documentNumber, Authentication auth) {
        authz.require(auth, "guest.read.any");
        return service.findDocument(documentNumber);
    }

    @GetMapping("/{id}")
    public GuestView find(@PathVariable UUID id, Authentication auth) {
        authz.requireGuest(auth, id, "guest.read.any", "guest.read.own");
        return service.find(id);
    }
}
