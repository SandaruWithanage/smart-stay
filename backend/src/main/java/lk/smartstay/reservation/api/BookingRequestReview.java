package lk.smartstay.reservation.api;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class BookingRequestReview {
    private BookingRequestReview() {}

    /** Staff must confirm receipt of the advance; checkbox alone is not a payment gateway. */
    public record Approval(@AssertTrue boolean advanceReceived) { }

    public record Rejection(@NotBlank @Size(max=200) String reason) { }
}
