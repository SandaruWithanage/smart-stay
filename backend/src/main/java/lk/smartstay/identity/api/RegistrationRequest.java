package lk.smartstay.identity.api;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegistrationRequest(
    @NotBlank @Size(max = 200) String fullName,
    @NotBlank @Size(max = 64) String documentNumber,
    @NotBlank @Size(max = 32) String phone,
    @NotBlank @Email @Size(max = 254) String email,
    @NotBlank @Size(min = 12, max = 128) String password
) { }
