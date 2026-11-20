package lk.smartstay.identity.service;

import java.util.List;
import java.util.Set;
import lk.smartstay.identity.api.UiManifest;
import lk.smartstay.identity.persistence.AccessRepository;
import lk.smartstay.identity.persistence.NavigationRepository;
import lk.smartstay.identity.persistence.UserRepository;
import lk.smartstay.shared.NotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UiManifestService {
    private final AuthorizationService authz;
    private final AccessRepository access;
    private final NavigationRepository nav;
    private final UserRepository users;

    public UiManifestService(AuthorizationService authz, AccessRepository access,
                             NavigationRepository nav, UserRepository users) {
        this.authz = authz;
        this.access = access;
        this.nav = nav;
        this.users = users;
    }

    @Transactional(readOnly = true)
    public UiManifest manifest(Authentication authentication) {
        var id = authz.userId(authentication);
        var user = users.findById(id).orElseThrow(() -> new NotFoundException("Account not found"));
        List<String> permissions = access.permissions(id);
        Set<String> available = Set.copyOf(permissions);
        var visible = nav.active().stream()
            .filter(item -> item.requiredPermission() == null
                || available.contains(item.requiredPermission())).toList();
        // No client authority to edit route names, JavaScript or component code.
        return new UiManifest(id, user.getDisplayName(), access.roles(id),
            access.configurationVersion(), permissions, visible);
    }
}
