package lk.smartstay.guest.api;

import java.util.UUID;
import lk.smartstay.guest.persistence.GuestEntity;

public record GuestView(UUID id, String fullName, String contactNumber, String email) {
    public static GuestView from(GuestEntity entity) {
        return new GuestView(entity.getId(), entity.getFullName(),
            entity.getContactNumber(), entity.getEmail());
    }
}
