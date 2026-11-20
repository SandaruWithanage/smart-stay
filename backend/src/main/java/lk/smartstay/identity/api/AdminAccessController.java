package lk.smartstay.identity.api;

import jakarta.validation.Valid;
import java.util.List;
import lk.smartstay.identity.persistence.NavigationRepository;
import lk.smartstay.identity.service.AccessManagementService;
import lk.smartstay.identity.service.AuthorizationService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/access")
public class AdminAccessController {
    private final AccessManagementService service;
    private final AuthorizationService authz;
    private final NavigationRepository nav;

    public AdminAccessController(AccessManagementService service,
                                 AuthorizationService authz, NavigationRepository nav) {
        this.service = service;
        this.authz = authz;
        this.nav = nav;
    }

    @GetMapping("/permissions")
    public List<String> allPermissions(Authentication user) {
        authz.require(user, "access.manage");
        return service.permissions();
    }

    @GetMapping("/roles/{role}/permissions")
    public List<String> rolePermissions(@PathVariable String role, Authentication user) {
        authz.require(user, "access.manage");
        return service.rolePermissions(role);
    }

    @PutMapping("/roles/{role}/permissions")
    public List<String> replace(@PathVariable String role,
                                @Valid @RequestBody RolePermissionsUpdate request,
                                Authentication user) {
        authz.require(user, "access.manage");
        return service.replaceRolePermissions(role, request.permissions());
    }

    @GetMapping("/navigation")
    public List<UiManifest.NavigationItem> navigation(Authentication user) {
        authz.require(user, "navigation.manage");
        return nav.all();
    }

    @PutMapping("/navigation/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateNavigation(@PathVariable String id,
                                 @Valid @RequestBody NavigationUpdate request,
                                 Authentication user) {
        authz.require(user, "navigation.manage");
        service.updateNavigation(id, request);
    }
}
