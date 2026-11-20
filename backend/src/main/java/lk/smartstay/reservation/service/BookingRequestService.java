package lk.smartstay.reservation.service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lk.smartstay.config.TimeConfiguration;
import lk.smartstay.identity.persistence.UserRepository;
import lk.smartstay.identity.service.AuthorizationService;
import lk.smartstay.reservation.api.BookingRequestCreate;
import lk.smartstay.reservation.api.BookingRequestView;
import lk.smartstay.reservation.api.ReservationCreateRequest;
import lk.smartstay.reservation.domain.BookingRequestStatus;
import lk.smartstay.reservation.domain.ReservationStatus;
import lk.smartstay.reservation.domain.StayPeriod;
import lk.smartstay.reservation.persistence.BookingRequestEntity;
import lk.smartstay.reservation.persistence.BookingRequestRepository;
import lk.smartstay.reservation.persistence.ReservationRepository;
import lk.smartstay.guest.persistence.GuestRepository;
import lk.smartstay.room.domain.RoomOperationalStatus;
import lk.smartstay.room.persistence.RoomRepository;
import lk.smartstay.shared.ConflictException;
import lk.smartstay.shared.NotFoundException;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * A request does not lock availability or record payment.
 * Approving it transactionally invokes the same guarded booking engine used by the desk.
 */
@Service
public class BookingRequestService {
    private final BookingRequestRepository requests;
    private final RoomRepository rooms;
    private final ReservationRepository bookings;
    private final UserRepository users;
    private final GuestRepository guests;
    private final BookingManagementService bookingManager;
    private final AuthorizationService authz;
    private final Clock clock;

    public BookingRequestService(BookingRequestRepository requests, RoomRepository rooms,
                                 ReservationRepository bookings, UserRepository users,
                                 GuestRepository guests, BookingManagementService bookingManager,
                                 AuthorizationService authz, Clock clock) {
        this.requests = requests;
        this.rooms = rooms;
        this.bookings = bookings;
        this.users = users;
        this.guests = guests;
        this.bookingManager = bookingManager;
        this.authz = authz;
        this.clock = clock;
    }

    @Transactional
    public BookingRequestView submit(Authentication auth, BookingRequestCreate input) {
        authz.require(auth, "booking_request.create.own");
        UUID userId = authz.userId(auth);
        var account = users.findById(userId)
            .orElseThrow(() -> new AccessDeniedException("Account unavailable"));
        if (account.getGuestId() == null) {
            throw new AccessDeniedException("A linked guest identity is required");
        }
        var guest = guests.findById(account.getGuestId())
            .orElseThrow(() -> new AccessDeniedException("Guest identity unavailable"));
        StayPeriod stay = new StayPeriod(input.checkIn(), input.checkOut());
        BookingRules.requireBookingNotice(
            LocalDateTime.ofInstant(clock.instant(), TimeConfiguration.HOTEL_ZONE),
            stay.checkIn().atTime(BookingRules.DEFAULT_CHECK_IN_HOUR, 0));

        var room = rooms.findById(input.roomId())
            .orElseThrow(() -> new NotFoundException("Room not found"));
        if (!room.isActive() || room.getOperationalStatus() != RoomOperationalStatus.ACTIVE) {
            throw new ConflictException("Room is unavailable");
        }
        if (input.guestCount() <= 0 || input.guestCount() > 50) {
            throw new IllegalArgumentException("Invalid guest count");
        }
        if (bookings.hasConflict(room.getId(), ReservationStatus.CONFIRMED,
                                 stay.checkOut(), stay.checkIn())) {
            throw new ConflictException("Room is no longer available for those dates");
        }
        var request = requests.saveAndFlush(new BookingRequestEntity(
            UUID.randomUUID(), userId, guest, room, stay, input.guestCount(), clock.instant()));
        return BookingRequestView.from(request);
    }

    @Transactional(readOnly = true)
    public List<BookingRequestView> myRequests(Authentication auth) {
        authz.require(auth, "booking_request.read.own");
        return requests.recentForUser(authz.userId(auth), PageRequest.of(0, 50))
            .stream().map(BookingRequestView::from).toList();
    }

    @Transactional(readOnly = true)
    public List<BookingRequestView> deskQueue(Authentication auth, BookingRequestStatus status) {
        authz.require(auth, "booking_request.review");
        var page=PageRequest.of(0, 50);
        var rows=status == null ? requests.recentForDesk(page)
            : requests.recentForDeskByStatus(status, page);
        return rows.stream().map(BookingRequestView::from).toList();
    }

    @Transactional
    public BookingRequestView approve(Authentication auth, UUID requestId, boolean advanceReceived) {
        authz.require(auth, "booking_request.review");
        if (!advanceReceived) {
            throw new IllegalArgumentException("Confirm that the LKR 10,000 advance was received");
        }
        var request = lockPending(requestId);
        var receipt = bookingManager.create(new ReservationCreateRequest(
            request.getGuest().getId(), request.getRoom().getId(),
            request.getRoom().getRoomType(), request.getCheckIn(),
            request.getCheckOut(), request.getGuestCount(), true));
        request.approve(authz.userId(auth), receipt.reservation().bookingId(), clock.instant());
        requests.flush();
        return BookingRequestView.from(request);
    }

    @Transactional
    public BookingRequestView reject(Authentication auth, UUID requestId, String reason) {
        authz.require(auth, "booking_request.review");
        var request = lockPending(requestId);
        request.reject(authz.userId(auth), reason, clock.instant());
        requests.flush();
        return BookingRequestView.from(request);
    }

    @Transactional
    public BookingRequestView withdraw(Authentication auth, UUID requestId) {
        authz.require(auth, "booking_request.withdraw.own");
        var request = requests.lockById(requestId)
            .orElseThrow(() -> new NotFoundException("Booking request not found"));
        if (!request.getRequestedBy().equals(authz.userId(auth))) {
            throw new AccessDeniedException("Cannot withdraw another guest's request");
        }
        request.withdraw();
        requests.flush();
        return BookingRequestView.from(request);
    }

    private BookingRequestEntity lockPending(UUID id) {
        var request = requests.lockById(id)
            .orElseThrow(() -> new NotFoundException("Booking request not found"));
        if (request.getStatus() != BookingRequestStatus.PENDING) {
            throw new ConflictException("Booking request has already been reviewed or withdrawn");
        }
        return request;
    }
}
