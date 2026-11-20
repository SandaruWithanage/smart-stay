package lk.smartstay.billing.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;
import lk.smartstay.billing.domain.InvoiceBreakdown;
import lk.smartstay.reservation.domain.StayPeriod;
import lk.smartstay.room.domain.RoomPackage;

/**
 * Provisional SRS billing interpretation.
 * Extras are charged per extra guest per night; tax applies after long-stay discount.
 * Advance is a recorded liability, NOT evidence of gateway payment.
 */
public final class PricingCalculator {
    private static final BigDecimal LONG_STAY_DISCOUNT = new BigDecimal("0.12");

    private PricingCalculator() { }

    public static InvoiceBreakdown calculate(RoomPackage roomPackage, StayPeriod stay,
                                             int capacity, int guests, BigDecimal advancePaid) {
        Objects.requireNonNull(roomPackage, "roomPackage");
        Objects.requireNonNull(stay, "stay");
        Objects.requireNonNull(advancePaid, "advancePaid");
        if (capacity <= 0 || guests <= 0 || advancePaid.signum() < 0) {
            throw new IllegalArgumentException("Capacity, guests or advance are invalid");
        }

        BigDecimal nights = BigDecimal.valueOf(stay.nights());
        BigDecimal base = money(roomPackage.dailyRate().multiply(nights));
        long extraCount = Math.max(0L, (long) guests - capacity);
        BigDecimal extras = money(roomPackage.extraGuestRate()
            .multiply(BigDecimal.valueOf(extraCount)).multiply(nights));
        BigDecimal discount = stay.nights() >= 5
            ? money(base.multiply(LONG_STAY_DISCOUNT)) : money(BigDecimal.ZERO);
        BigDecimal taxable = money(base.add(extras).subtract(discount));
        BigDecimal tax = money(taxable.multiply(roomPackage.taxRate()));
        BigDecimal gross = money(taxable.add(tax));
        BigDecimal applied = money(advancePaid.min(gross));
        BigDecimal due = money(gross.subtract(applied));
        BigDecimal refund = money(advancePaid.subtract(applied));

        return new InvoiceBreakdown(base, extras, discount, taxable, tax,
            gross, applied, due, refund);
    }

    private static BigDecimal money(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }
}
