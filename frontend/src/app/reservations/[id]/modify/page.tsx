import Link from "next/link";
import { notFound, redirect } from "next/navigation";
import { CalendarClock, ShieldCheck, History, FileText } from "lucide-react";
import AppShell from "@/components/app-shell";
import { SubmitButton } from "@/components/ui/confirm-submit";
import { DataTable, EmptyState, Feedback, FormField, PageHeader,
  SectionHeader, Surface, type Column } from "@/components/ui/hotel-ui";
import { displayDate, lkr, roomName, type ReservationView } from "@/lib/hotel";
import { authenticatedJson } from "@/lib/server-hotel";
import { sessionManifest } from "@/lib/server-session";
import { saveReservationModification } from "./actions";

type Amendment = {
  id:string; previousCheckIn:string; previousCheckOut:string;
  newCheckIn:string; newCheckOut:string; previousGuestCount:number;
  newGuestCount:number; previousGrossTotal:number; newGrossTotal:number;
  changedAt:string
};

export default async function ModifyStay({
  params,searchParams
}:{params:Promise<{id:string}>;searchParams:Promise<{status?:string}>}) {
  const manifest=await sessionManifest();
  if (!manifest) redirect("/login");
  if (!manifest.permissions.some(p=>p==="reservation.modify.own"||p==="reservation.modify.any")) notFound();
  const {id}=await params;
  if (!/^[0-9a-f-]{36}$/i.test(id)) notFound();
  const [booking,amendments]=await Promise.all([
    authenticatedJson<ReservationView>(`/api/reservations/${id}`),
    authenticatedJson<Amendment[]>(`/api/reservations/${id}/amendments`)
  ]);
  if (!booking) notFound();
  const status=(await searchParams).status;
  const active=booking.status==="CONFIRMED";
  const columns:Column<Amendment>[]=[
    {key:"date",label:"Changed at",render:a=>new Date(a.changedAt).toLocaleString("en-GB",{timeZone:"Asia/Colombo"})},
    {key:"from",label:"Previous stay",render:a=><div>{displayDate(a.previousCheckIn)} – {displayDate(a.previousCheckOut)}
      <small className="ss-cell-sub">{a.previousGuestCount} guest(s)</small></div>},
    {key:"to",label:"New stay",render:a=><div>{displayDate(a.newCheckIn)} – {displayDate(a.newCheckOut)}
      <small className="ss-cell-sub">{a.newGuestCount} guest(s)</small></div>},
    {key:"prevTotal",label:"Previous total",align:"right",render:a=>lkr(a.previousGrossTotal)},
    {key:"newTotal",label:"Updated total",align:"right",render:a=>lkr(a.newGrossTotal)}
  ];
  return <AppShell manifest={manifest}>
    <PageHeader kicker="RESERVATION / AMEND STAY" title="Modify reservation"
      description="Adjust dates or guest count before the 24-hour cutoff. The hotel revalidates availability and recalculates your invoice."
      action={<Link href={`/reservations/${id}/invoice`} className="ss-btn ss-btn-secondary"><FileText size={16}/> View invoice</Link>}/>
    <Feedback status={status}/>
    <div className="ss-booking-layout">
      <Surface solid className="ss-block">
        <SectionHeader title="Reservation details" subtitle={`Booking ${booking.bookingId.slice(0,8).toUpperCase()} · ${roomName(booking.roomType)} / ${booking.roomId}`}/>
        {active ? <form action={saveReservationModification} className="ss-request-form">
          <input type="hidden" name="bookingId" value={booking.bookingId}/>
          <div className="ss-two-column">
            <FormField name="checkIn" label="Check-in date" required>
              <input type="date" id="checkIn" name="checkIn" defaultValue={booking.checkIn} required/></FormField>
            <FormField name="checkOut" label="Check-out date" required>
              <input type="date" id="checkOut" name="checkOut" defaultValue={booking.checkOut} required/></FormField>
            <FormField name="guestCount" label="Number of guests" required>
              <input type="number" id="guestCount" name="guestCount" min={1} max={50}
                defaultValue={booking.guestCount} required/></FormField>
          </div>
          <div className="ss-request-note"><strong>No additional booking advance is charged.</strong>
            The previously recorded advance is carried into the recalculated invoice. Changing the check-in date
            requires 48 hours' notice; changes are not allowed within 24 hours of the original check-in.
            Other confirmed reservations must not overlap the requested dates.</div>
          <SubmitButton><CalendarClock size={17}/> Save reservation changes</SubmitButton>
        </form> : <EmptyState title="Cancelled reservation"
          description="Cancelled reservations cannot be modified. The retained invoice is historical."/>}
      </Surface>
      <aside className="ss-booking-help">
        <Surface className="ss-block">
          <SectionHeader title="Modification rules"/>
          <div className="ss-rule"><ShieldCheck size={17}/><span>Room ID and room type remain unchanged.</span></div>
          <div className="ss-rule"><CalendarClock size={17}/><span>At least 24 hours before the original check-in.</span></div>
          <div className="ss-rule"><FileText size={17}/><span>Revised charges and previous totals remain auditable.</span></div>
        </Surface>
      </aside>
    </div>
    <Surface solid className="ss-block">
      <SectionHeader title="Change history" subtitle="Immutable records of previous and recalculated stay details."/>
      {amendments?.length ? <DataTable rows={amendments} columns={columns} rowKey={a=>a.id}/> :
        <EmptyState title="No previous amendments" description="Changes to dates or guest count will appear here once saved."/>}
    </Surface>
  </AppShell>;
}
