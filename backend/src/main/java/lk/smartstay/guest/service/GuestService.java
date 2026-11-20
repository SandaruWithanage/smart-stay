package lk.smartstay.guest.service;

import java.util.Locale;
import java.util.UUID;
import lk.smartstay.guest.api.GuestRegistrationRequest;
import lk.smartstay.guest.api.GuestView;
import lk.smartstay.guest.persistence.GuestEntity;
import lk.smartstay.guest.persistence.GuestRepository;
import lk.smartstay.shared.ConflictException;
import lk.smartstay.shared.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GuestService {
    private final GuestRepository guests;

    public GuestService(GuestRepository guests) { this.guests = guests; }

    @Transactional
    public GuestView register(GuestRegistrationRequest request) {
        String document = request.documentNumber().trim().toUpperCase(Locale.ROOT);
        if (guests.existsByDocumentNumber(document)) {
            throw new ConflictException("Guest document already registered");
        }
        GuestEntity saved = guests.saveAndFlush(new GuestEntity(
            UUID.randomUUID(), document, request.fullName().trim(), request.contactNumber().trim(),
            request.email().trim().toLowerCase(Locale.ROOT)));
        return GuestView.from(saved);
    }

    @Transactional(readOnly = true)
    public GuestView find(UUID id) {
        return GuestView.from(guests.findById(id)
            .orElseThrow(() -> new NotFoundException("Guest not found")));
    }
}
