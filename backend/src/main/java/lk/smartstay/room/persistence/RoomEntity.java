package lk.smartstay.room.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Objects;
import lk.smartstay.room.domain.BedType;
import lk.smartstay.room.domain.RoomOperationalStatus;
import lk.smartstay.room.domain.RoomType;

@Entity
@Table(name = "rooms")
public class RoomEntity {
    @Id
    @Column(length = 32)
    private String id;

    @Enumerated(EnumType.STRING)
    @Column(name = "room_type", nullable = false, length = 32)
    private RoomType roomType;

    @Enumerated(EnumType.STRING)
    @Column(name = "bed_type", nullable = false, length = 20)
    private BedType bedType;

    @Column(nullable = false)
    private int capacity;

    @Enumerated(EnumType.STRING)
    @Column(name = "operational_status", nullable = false, length = 20)
    private RoomOperationalStatus operationalStatus;

    @Column(nullable = false)
    private boolean active;

    protected RoomEntity() { }

    public RoomEntity(String id, RoomType roomType, BedType bedType, int capacity,
                      RoomOperationalStatus operationalStatus) {
        this.id = Objects.requireNonNull(id, "id");
        this.roomType = Objects.requireNonNull(roomType, "roomType");
        this.bedType = Objects.requireNonNull(bedType, "bedType");
        if (capacity <= 0) throw new IllegalArgumentException("Capacity must be positive");
        this.capacity = capacity;
        this.operationalStatus = Objects.requireNonNull(operationalStatus, "operationalStatus");
        this.active = true;
    }

    public void update(RoomType type, BedType bed, int roomCapacity, RoomOperationalStatus status) {
        if (roomCapacity <= 0) throw new IllegalArgumentException("Capacity must be positive");
        this.roomType = Objects.requireNonNull(type);
        this.bedType = Objects.requireNonNull(bed);
        this.capacity = roomCapacity;
        this.operationalStatus = Objects.requireNonNull(status);
    }

    public void retire() { this.active = false; }
    public String getId() { return id; }
    public RoomType getRoomType() { return roomType; }
    public BedType getBedType() { return bedType; }
    public int getCapacity() { return capacity; }
    public RoomOperationalStatus getOperationalStatus() { return operationalStatus; }
    public boolean isActive() { return active; }
}
