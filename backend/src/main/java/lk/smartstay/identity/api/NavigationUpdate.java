package lk.smartstay.identity.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record NavigationUpdate(@NotBlank @Size(max = 100) String label,
                               int order, boolean enabled) { }
