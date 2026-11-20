package lk.smartstay.room.api;

import java.util.List;
import lk.smartstay.room.domain.RoomPackage;
import lk.smartstay.room.service.RoomPackages;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Public catalog only; privileged room CRUD must wait for real server-side RBAC. */
@RestController
@RequestMapping("/api/public/room-packages")
public class PublicRoomPackageController {
    @GetMapping
    public List<RoomPackage> list() {
        return RoomPackages.ALL;
    }
}
