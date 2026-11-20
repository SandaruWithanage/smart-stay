package lk.smartstay.billing.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import lk.smartstay.billing.domain.InvoiceBreakdown;
import lk.smartstay.reservation.domain.StayPeriod;
import lk.smartstay.room.domain.RoomType;
import lk.smartstay.room.service.RoomPackages;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PricingCalculatorTest {
    private StayPeriod nights(int count) {
        LocalDate start = LocalDate.of(2026, 12, 10);
        return new StayPeriod(start, start.plusDays(count));
    }

    @Test void fiveNightStandardStayMatchesCourseworkExample() {
        InvoiceBreakdown result = PricingCalculator.calculate(
            RoomPackages.find(RoomType.STANDARD), nights(5), 2, 2, new BigDecimal("10000"));
        assertEquals(0, new BigDecimal("30000.00").compareTo(result.baseCharge()));
        assertEquals(0, new BigDecimal("3600.00").compareTo(result.longStayDiscount()));
        assertEquals(0, new BigDecimal("2640.00").compareTo(result.taxAmount()));
        assertEquals(0, new BigDecimal("19040.00").compareTo(result.balanceDue()));
    }

    @Test void extraGuestsAreChargedPerNightProvisionally() {
        InvoiceBreakdown result = PricingCalculator.calculate(
            RoomPackages.find(RoomType.DELUXE), nights(2), 2, 3, new BigDecimal("10000"));
        assertEquals(0, new BigDecimal("3000.00").compareTo(result.extraGuestCharge()));
        assertEquals(0, new BigDecimal("13520.00").compareTo(result.balanceDue()));
    }

    @Test void fourNightStayDoesNotDiscount() {
        InvoiceBreakdown result = PricingCalculator.calculate(
            RoomPackages.find(RoomType.STANDARD), nights(4), 2, 2, BigDecimal.TEN);
        assertEquals(0, result.longStayDiscount().compareTo(BigDecimal.ZERO));
    }

    @Test void paidAdvanceAboveSmallBillProducesRefund() {
        InvoiceBreakdown result = PricingCalculator.calculate(
            RoomPackages.find(RoomType.STANDARD), nights(1), 2, 2, new BigDecimal("10000"));
        assertEquals(0, new BigDecimal("0.00").compareTo(result.balanceDue()));
        assertEquals(0, new BigDecimal("3400.00").compareTo(result.refundDue()));
    }
}
