package lk.smartstay.identity.service;

import java.util.List;
import java.util.Set;
import lk.smartstay.identity.api.NavigationUpdate;
import lk.smartstay.identity.persistence.AccessRepository;
import lk.smartstay.identity.persistence.NavigationRepository;
import lk.smartstay.shared.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccessManagementService {
    private final AccessRepository access;
    private final NavigationRepository navigation;

    public AccessManagementService(AccessRepository access, NavigationRepository navigation) {
        this.access = access;
        this.navigation = navigation;
    }

    @Transactional(readOnly = true)
    public List<String> rolePermissions(String role) {
        if (!access.roleExists(role)) throw new NotFoundException("Role not found");
        return access.rolePermissions(role);
    }

    @Transactional(readOnly = true)
    public List<String> permissions() { return access.permissionCatalog(); }

    @Transactional
    public List<String> replaceRolePermissions(String role, Set<String> grants) {
        if (!access.roleExists(role)) throw new NotFoundException("Role not found");
        if (grants == null || !access.permissionCatalog().containsAll(grants)) {
            throw new IllegalArgumentException("Unknown permission");
        }
        // Prevent accidental loss of the system's only policy-management capability.
        if ("ADMIN".equals(role) && !grants.contains("access.manage")) {
            throw new IllegalArgumentException("ADMIN must retain access.manage");
        }
        if ("GUEST".equals(role) && (grants.contains("access.manage")
            || grants.contains("navigation.manage"))) {
            throw new IllegalArgumentException("System permissions cannot be granted to GUEST");
        }
        access.replaceRolePermissions(role, grants.stream().sorted().toList());
        return access.rolePermissions(role);
    }

    @Transactional
    public void updateNavigation(String id, NavigationUpdate request) {
        if (navigation.update(id, request.label().trim(), request.order(), request.enabled()) != 1) {
            throw new NotFoundException("Navigation item not found");
        }
    }
}
