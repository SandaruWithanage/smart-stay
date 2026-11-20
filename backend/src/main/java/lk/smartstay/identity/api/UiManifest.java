package lk.smartstay.identity.api;

import java.util.List;
import java.util.UUID;

public record UiManifest(
    UUID userId,
    String displayName,
    List<String> roleCodes,
    long configurationVersion,
    List<String> permissions,
    List<NavigationItem> navigation
) {
    public record NavigationItem(String id, String label, String routeKey, String iconKey,
                                 int order, String requiredPermission, boolean enabled) { }
}
