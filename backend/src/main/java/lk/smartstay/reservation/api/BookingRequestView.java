package lk.smartstay.reservation.api;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import lk.smartstay.reservation.domain.BookingRequestStatus;
import lk.smartstay.reservation.persistence.BookingRequestEntity;

public record BookingRequestView(
    UUID id, String roomId, String roomType, String guestName,
    LocalDate checkIn, LocalDate checkOut, int guestCount,
    BookingRequestStatus status, Instant submittedAt, Instant reviewedAt,
    String reviewerNote, UUID reservationId
) {
    public static BookingRequestView from(BookingRequestEntity row) {
        return new BookingRequestView(
            row.getId(), row.getRoom().getId(), row.getRoom().getRoomType().name(),
            row.getGuest().getFullName(), row.getCheckIn(), row.getCheckOut(),
            row.getGuestCount(), row.getStatus(), row.getCreatedAt(),
            row.getReviewedAt(), row.getReviewerNote(), row.getReservationId());
    }
}
