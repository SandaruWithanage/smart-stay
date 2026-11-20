export type RoomType = "STANDARD" | "DELUXE" | "SUITE" | "FAMILY_SUITE";
export type BedType = "SINGLE" | "DOUBLE" | "QUEEN" | "KING";
export type RoomStatus = "ACTIVE" | "MAINTENANCE";
export type ReservationStatus = "CONFIRMED" | "CANCELLED";
export type RefundStatus = "NOT_APPLICABLE" | "PENDING" | "SETTLED";

export type RoomView = {
  id: string;
  roomType: RoomType;
  bedType: BedType;
  capacity: number;
  operationalStatus: RoomStatus;
  active: boolean;
  dailyRate: number;
};

export type ReservationView = {
  bookingId: string;
  guestId: string;
  guestName: string;
  roomId: string;
  roomType: RoomType;
  checkIn: string;
  checkOut: string;
  guestCount: number;
  status: ReservationStatus;
  recordedAdvance: number;
  refundStatus: RefundStatus;
  cancellationRefundDue: number;
};

export type InvoiceView = {
  invoiceId: string;
  reservationId: string;
  guestName: string;
  roomId: string;
  roomType: RoomType;
  bedType: BedType;
  dailyRate: number;
  checkIn: string;
  checkOut: string;
  nights: number;
  guests: number;
  baseCharge: number;
  extraCharge: number;
  discount: number;
  tax: number;
  grossTotal: number;
  advanceApplied: number;
  balanceDue: number;
  refundDue: number;
  voided: boolean;
};

export type BookingReceipt = { reservation: ReservationView; invoice: InvoiceView };
export const roomTypes: RoomType[] = ["STANDARD","DELUXE","SUITE","FAMILY_SUITE"];
export const bedTypes: BedType[] = ["SINGLE","DOUBLE","QUEEN","KING"];
export const roomStatuses: RoomStatus[] = ["ACTIVE","MAINTENANCE"];

export const roomName = (value: string) =>
  ({ STANDARD:"Standard", DELUXE:"Deluxe", SUITE:"Suite", FAMILY_SUITE:"Family Suite" } as Record<string,string>)[value]
    ?? value.replaceAll("_"," ");

export function lkr(value: number | string): string {
  const n = Number(value);
  return Number.isFinite(n) ? "LKR " + new Intl.NumberFormat("en-LK", {
    minimumFractionDigits: 2, maximumFractionDigits: 2
  }).format(n) : "LKR —";
}

export function displayDate(value: string) {
  const d = new Date(value + "T00:00:00Z");
  return Number.isNaN(d.getTime()) ? value : new Intl.DateTimeFormat("en-GB", {
    day:"2-digit", month:"short", year:"numeric", timeZone:"UTC"
  }).format(d);
}
