package lk.smartstay.reservation.domain;

/** Cancellation refund obligation. PENDING does not mean payment was transferred. */
public enum RefundStatus {
    NOT_APPLICABLE, PENDING, SETTLED
}
