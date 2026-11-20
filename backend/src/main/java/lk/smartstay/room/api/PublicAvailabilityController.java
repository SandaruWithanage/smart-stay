package lk.smartstay.room.api;

import java.time.LocalDate;
import java.util.List;
import lk.smartstay.room.domain.RoomType;
import lk.smartstay.room.service.RoomAvailabilityService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public/rooms")
public class PublicAvailabilityController {
    private final RoomAvailabilityService availability;

    public PublicAvailabilityController(RoomAvailabilityService availability) {
        this.availability = availability;
    }

    @GetMapping("/available")
    public List<RoomView> available(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut,
        @RequestParam(required = false) RoomType roomType
    ) {
        return availability.search(checkIn, checkOut, roomType);
    }
}
