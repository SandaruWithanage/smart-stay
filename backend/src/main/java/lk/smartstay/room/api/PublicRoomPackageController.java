package lk.smartstay.room.api;

import java.util.List;
import lk.smartstay.room.domain.RoomPackage;
import lk.smartstay.room.service.DatabaseRoomPackageCatalog;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Read-only DB-driven package catalog with limited Caffeine caching. */
@RestController
@RequestMapping("/api/public/room-packages")
public class PublicRoomPackageController {
    private final DatabaseRoomPackageCatalog packages;

    public PublicRoomPackageController(DatabaseRoomPackageCatalog packages) {
        this.packages = packages;
    }

    @GetMapping
    public List<RoomPackage> list() { return packages.all(); }
}
