package lk.smartstay.room.service;

import java.time.LocalDate;
import java.util.List;
import lk.smartstay.reservation.domain.ReservationStatus;
import lk.smartstay.reservation.domain.StayPeriod;
import lk.smartstay.room.api.RoomView;
import lk.smartstay.room.domain.RoomOperationalStatus;
import lk.smartstay.room.domain.RoomType;
import lk.smartstay.room.persistence.RoomEntity;
import lk.smartstay.room.persistence.RoomPackageRepository;
import lk.smartstay.room.persistence.RoomRepository;
import lk.smartstay.shared.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RoomAvailabilityService {
    private final RoomRepository rooms;
    private final RoomPackageRepository packages;

    public RoomAvailabilityService(RoomRepository rooms, RoomPackageRepository packages) {
        this.rooms = rooms;
        this.packages = packages;
    }

    @Transactional(readOnly = true)
    public List<RoomView> search(LocalDate checkIn, LocalDate checkOut, RoomType type) {
        StayPeriod period = new StayPeriod(checkIn, checkOut);
        List<RoomEntity> available = type == null
            ? rooms.availableRooms(period.checkIn(), period.checkOut(),
                RoomOperationalStatus.ACTIVE, ReservationStatus.CONFIRMED)
            : rooms.availableRoomsOfType(period.checkIn(), period.checkOut(),
                RoomOperationalStatus.ACTIVE, ReservationStatus.CONFIRMED, type);

        return available.stream().map(room -> RoomView.from(room,
            packages.findById(room.getRoomType())
                .orElseThrow(() -> new NotFoundException("Room package missing")))).toList();
    }
}
