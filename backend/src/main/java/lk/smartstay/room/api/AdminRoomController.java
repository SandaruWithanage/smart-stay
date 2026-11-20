package lk.smartstay.room.api;

import jakarta.validation.Valid;
import java.util.List;
import lk.smartstay.room.service.RoomManagementService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Protected by deny-all security until the JWT/ABAC milestone. */
@RestController
@RequestMapping("/api/admin/rooms")
public class AdminRoomController {
    private final RoomManagementService service;

    public AdminRoomController(RoomManagementService service) { this.service = service; }

    @GetMapping
    public List<RoomView> all() { return service.all(); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RoomView create(@Valid @RequestBody RoomCreateRequest request) {
        return service.create(request);
    }

    @PutMapping("/{id}")
    public RoomView update(@PathVariable String id, @Valid @RequestBody RoomUpdateRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void retire(@PathVariable String id) { service.retire(id); }
}
