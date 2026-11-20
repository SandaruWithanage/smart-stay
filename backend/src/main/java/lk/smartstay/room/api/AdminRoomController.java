package lk.smartstay.room.api;

import jakarta.validation.Valid;
import java.util.List;
import lk.smartstay.identity.service.AuthorizationService;
import lk.smartstay.room.service.RoomManagementService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/rooms")
public class AdminRoomController {
    private final RoomManagementService service;
    private final AuthorizationService authz;

    public AdminRoomController(RoomManagementService service, AuthorizationService authz) {
        this.service = service;
        this.authz = authz;
    }

    @GetMapping
    public List<RoomView> all(Authentication user) {
        authz.require(user, "room.read");
        return service.all();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RoomView create(@Valid @RequestBody RoomCreateRequest request, Authentication user) {
        authz.require(user, "room.create");
        return service.create(request);
    }

    @PutMapping("/{id}")
    public RoomView update(@PathVariable String id,
                           @Valid @RequestBody RoomUpdateRequest request, Authentication user) {
        authz.require(user, "room.update");
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void retire(@PathVariable String id, Authentication user) {
        authz.require(user, "room.delete");
        service.retire(id);
    }
}
