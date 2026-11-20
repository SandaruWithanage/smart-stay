package lk.smartstay.identity.api;

import lk.smartstay.identity.service.AuthorizationService;
import lk.smartstay.identity.service.UiManifestService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/me")
public class MeController {
    private final UiManifestService manifests;
    private final AuthorizationService authz;

    public MeController(UiManifestService manifests, AuthorizationService authz) {
        this.manifests = manifests;
        this.authz = authz;
    }

    @GetMapping
    public UiManifest profile(Authentication authentication) {
        authz.require(authentication, "workspace.read");
        return manifests.manifest(authentication);
    }

    @GetMapping("/ui-config")
    public UiManifest uiConfig(Authentication authentication) {
        authz.require(authentication, "workspace.read");
        return manifests.manifest(authentication);
    }
}
