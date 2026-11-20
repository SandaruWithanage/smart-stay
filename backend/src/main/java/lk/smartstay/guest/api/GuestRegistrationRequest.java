package lk.smartstay.guest.api;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record GuestRegistrationRequest(
    @NotBlank @Size(max = 64) String documentNumber,
    @NotBlank @Size(max = 200) String fullName,
    @NotBlank @Size(max = 32) String contactNumber,
    @NotBlank @Email @Size(max = 254) String email
) { }
