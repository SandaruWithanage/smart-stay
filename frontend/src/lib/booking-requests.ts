export type BookingRequestStatus = "PENDING" | "APPROVED" | "REJECTED" | "WITHDRAWN";

export type BookingRequestView = {
  id: string;
  roomId: string;
  roomType: string;
  guestName: string;
  checkIn: string;
  checkOut: string;
  guestCount: number;
  status: BookingRequestStatus;
  submittedAt: string;
  reviewedAt: string | null;
  reviewerNote: string | null;
  reservationId: string | null;
};
