package lk.smartstay.room.domain;

/**
 * Operational room state. RESERVED is derived from bookings for a requested interval,
 * not permanently persisted as an operational status.
 */
public enum RoomOperationalStatus {
    ACTIVE, MAINTENANCE
}
