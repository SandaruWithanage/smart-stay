package lk.smartstay.room.service;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import lk.smartstay.config.TimeConfiguration;
import lk.smartstay.reservation.domain.ReservationStatus;
import lk.smartstay.reservation.persistence.ReservationRepository;
import lk.smartstay.room.api.RoomCreateRequest;
import lk.smartstay.room.api.RoomUpdateRequest;
import lk.smartstay.room.api.RoomView;
import lk.smartstay.room.persistence.RoomEntity;
import lk.smartstay.room.persistence.RoomPackageRepository;
import lk.smartstay.room.persistence.RoomRepository;
import lk.smartstay.shared.ConflictException;
import lk.smartstay.shared.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RoomManagementService {
    private final RoomRepository rooms;
    private final RoomPackageRepository packages;
    private final ReservationRepository reservations;
    private final Clock clock;

    public RoomManagementService(RoomRepository rooms, RoomPackageRepository packages,
                                 ReservationRepository reservations, Clock clock) {
        this.rooms = rooms;
        this.packages = packages;
        this.reservations = reservations;
        this.clock = clock;
    }

    @Transactional
    public RoomView create(RoomCreateRequest request) {
        String id = request.id().trim();
        if (rooms.existsById(id)) throw new ConflictException("Room ID already exists");
        RoomEntity room = rooms.saveAndFlush(new RoomEntity(id, request.roomType(),
            request.bedType(), request.capacity(), request.operationalStatus()));
        return view(room);
    }

    @Transactional
    public RoomView update(String roomId, RoomUpdateRequest request) {
        RoomEntity room = rooms.lockById(roomId)
            .orElseThrow(() -> new NotFoundException("Room not found"));
        if (!room.isActive()) throw new ConflictException("Retired rooms cannot be updated");
        room.update(request.roomType(), request.bedType(), request.capacity(), request.operationalStatus());
        rooms.flush();
        return view(room);
    }

    @Transactional
    public void retire(String roomId) {
        RoomEntity room = rooms.lockById(roomId)
            .orElseThrow(() -> new NotFoundException("Room not found"));
        LocalDate today = LocalDate.now(clock.withZone(TimeConfiguration.HOTEL_ZONE));
        if (reservations.hasUnfinishedBooking(
            roomId, ReservationStatus.CONFIRMED, today)) {
            throw new ConflictException("Room has an active or future reservation");
        }
        room.retire();
    }

    @Transactional(readOnly = true)
    public List<RoomView> all() {
        return rooms.findAll().stream().map(this::view).toList();
    }

    private RoomView view(RoomEntity room) {
        var pkg = packages.findById(room.getRoomType())
            .orElseThrow(() -> new NotFoundException("Room package missing"));
        return RoomView.from(room, pkg);
    }
}
