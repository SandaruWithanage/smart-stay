package lk.smartstay.billing.domain;

import java.math.BigDecimal;

/** Monetary breakdown after consistent two-decimal rounding, in LKR. */
public record InvoiceBreakdown(
    BigDecimal baseCharge,
    BigDecimal extraGuestCharge,
    BigDecimal longStayDiscount,
    BigDecimal taxableAmount,
    BigDecimal taxAmount,
    BigDecimal grossTotal,
    BigDecimal advanceApplied,
    BigDecimal balanceDue,
    BigDecimal refundDue
) { }
