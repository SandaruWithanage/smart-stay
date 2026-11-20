package lk.smartstay.reservation.api;

import lk.smartstay.billing.api.InvoiceView;

public record BookingReceipt(ReservationView reservation, InvoiceView invoice) { }
