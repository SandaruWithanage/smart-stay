package lk.smartstay.billing.api;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import lk.smartstay.billing.persistence.InvoiceEntity;

public record InvoiceView(
    UUID invoiceId,
    UUID reservationId,
    String guestName,
    String roomId,
    String roomType,
    String bedType,
    BigDecimal dailyRate,
    LocalDate checkIn,
    LocalDate checkOut,
    int nights,
    int guests,
    BigDecimal baseCharge,
    BigDecimal extraCharge,
    BigDecimal discount,
    BigDecimal tax,
    BigDecimal grossTotal,
    BigDecimal advanceApplied,
    BigDecimal balanceDue,
    BigDecimal refundDue,
    boolean voided
) {
    public static InvoiceView from(InvoiceEntity invoice) {
        var booking = invoice.getReservation();
        return new InvoiceView(invoice.getId(), booking.getId(), invoice.getGuestNameSnapshot(),
            invoice.getRoomIdSnapshot(), invoice.getRoomTypeSnapshot(), invoice.getBedTypeSnapshot(),
            invoice.getDailyRateSnapshot(), booking.getCheckIn(), booking.getCheckOut(),
            invoice.getNights(), invoice.getGuestCount(), invoice.getBaseCharge(),
            invoice.getExtraCharge(), invoice.getDiscountAmount(), invoice.getTaxAmount(),
            invoice.getGrossTotal(), invoice.getAdvanceApplied(), invoice.getBalanceDue(),
            invoice.getRefundDue(), invoice.isVoided());
    }
}
