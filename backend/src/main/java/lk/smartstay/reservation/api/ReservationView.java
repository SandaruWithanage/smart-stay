package lk.smartstay.reservation.api;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import lk.smartstay.reservation.domain.RefundStatus;
import lk.smartstay.reservation.domain.ReservationStatus;
import lk.smartstay.reservation.persistence.ReservationEntity;

public record ReservationView(
    UUID bookingId,
    UUID guestId,
    String guestName,
    String roomId,
    String roomType,
    LocalDate checkIn,
    LocalDate checkOut,
    int guestCount,
    ReservationStatus status,
    BigDecimal recordedAdvance,
    RefundStatus refundStatus,
    BigDecimal cancellationRefundDue
) {
    public static ReservationView from(ReservationEntity b) {
        return new ReservationView(b.getId(), b.getGuest().getId(), b.getGuest().getFullName(),
            b.getRoom().getId(), b.getRoom().getRoomType().name(),
            b.getCheckIn(), b.getCheckOut(), b.getGuestCount(), b.getStatus(),
            b.getAdvancePaid(), b.getRefundStatus(), b.getCancellationRefundDue());
    }
}
