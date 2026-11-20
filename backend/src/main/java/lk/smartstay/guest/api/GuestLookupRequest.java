package lk.smartstay.guest.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record GuestLookupRequest(
    @NotBlank @Size(max=64) String documentNumber
) { }
