package lk.smartstay.identity.api;

import jakarta.validation.constraints.NotNull;
import java.util.Set;

public record RolePermissionsUpdate(@NotNull Set<String> permissions) { }
