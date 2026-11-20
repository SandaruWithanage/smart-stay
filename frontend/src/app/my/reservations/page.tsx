import Link from "next/link";
import { CalendarCheck2, ClipboardList, ReceiptText } from "lucide-react";
import AppShell from "@/components/app-shell";
import { ConfirmSubmit } from "@/components/ui/confirm-submit";
import { DataTable, EmptyState, Feedback, MetricCard, PageHeader, PrimaryLink, StatusBadge, Surface, type Column } from "@/components/ui/hotel-ui";
import { displayDate, lkr, roomName, type ReservationView } from "@/lib/hotel";
import { authenticatedJson, requireAccess } from "@/lib/server-hotel";
import { cancelReservation } from "@/app/reservations/[id]/actions";

export default async function MyReservations({searchParams}:{searchParams:Promise<{status?:string}>}) {
  const manifest = await requireAccess("reservation.read.own");
  const bookings = await authenticatedJson<ReservationView[]>("/api/me/reservations");
  const status = (await searchParams).status;
  const canCancel = manifest.permissions.includes("reservation.cancel.own");
  const canModify = manifest.permissions.includes("reservation.modify.own")
    || manifest.permissions.includes("reservation.modify.any");
  const columns:Column<ReservationView>[] = [
    {key:"booking",label:"Reference",render:b=><Link href={`/reservations/${b.bookingId}/invoice`} className="ss-record-link">{b.bookingId.slice(0,8).toUpperCase()}</Link>},
    {key:"room",label:"Room collection",render:b=><div><strong className="ss-table-strong">{roomName(b.roomType)}</strong><small className="ss-cell-sub">{b.roomId}</small></div>},
    {key:"dates",label:"Stay dates",render:b=><div>{displayDate(b.checkIn)}<small className="ss-cell-sub">to {displayDate(b.checkOut)}</small></div>},
    {key:"guests",label:"Guests",render:b=>b.guestCount},
    {key:"status",label:"Status",render:b=><StatusBadge value={b.status}/>},
    {key:"amount",label:"Advance recorded",align:"right",render:b=>lkr(b.recordedAdvance)},
    {key:"actions",label:"Actions",render:b=><div className="ss-row-actions">
      <Link href={`/reservations/${b.bookingId}/invoice`} className="ss-action-link">View invoice</Link>
      {canModify && b.status==="CONFIRMED" &&
        <Link className="ss-action-link" href={`/reservations/${b.bookingId}/modify`}>Modify stay</Link>}
      {canCancel && b.status==="CONFIRMED" && <form action={cancelReservation}>
        <input type="hidden" name="bookingId" value={b.bookingId}/>
        <input type="hidden" name="source" value="guest"/>
        <ConfirmSubmit message="Cancel your reservation? Cancellation is only allowed at least 24 hours before check-in." className="ss-small-danger">Cancel</ConfirmSubmit>
      </form>}
    </div>}
  ];
  return <AppShell manifest={manifest}>
    <PageHeader kicker="YOUR STAYS / BOOKING HISTORY" title="My reservations"
      description="Your private stay records. Only reservations linked to your account are returned by the backend."
      action={<PrimaryLink href="/availability">Find a room</PrimaryLink>}/>
    <Feedback status={status}/>
    <div className="ss-metrics">
      <MetricCard label="Your reservations" value={bookings?.length ?? 0} icon={<ClipboardList size={20}/>}/>
      <MetricCard label="Confirmed stays" value={(bookings??[]).filter(b=>b.status==="CONFIRMED").length} icon={<CalendarCheck2 size={20}/>}/>
      <MetricCard label="Cancelled stays" value={(bookings??[]).filter(b=>b.status==="CANCELLED").length} icon={<ReceiptText size={20}/>}/>
    </div>
    <Surface solid className="ss-block">
      {bookings===null ? <EmptyState title="History unavailable" description="Your reservation history could not be loaded at this time."/>:
        bookings.length ? <DataTable rows={bookings} columns={columns} rowKey={r=>r.bookingId}/> :
        <EmptyState title="No reservations yet" description="Your booking history will appear here when a reservation is confirmed and linked to this account."
          action={<PrimaryLink href="/availability">Browse rooms</PrimaryLink>}/>}
    </Surface>
  </AppShell>;
}
