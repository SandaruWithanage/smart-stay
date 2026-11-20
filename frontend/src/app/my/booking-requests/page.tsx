import Link from "next/link";
import { CalendarDays, Clock3, CircleCheck, CircleX } from "lucide-react";
import AppShell from "@/components/app-shell";
import { ConfirmSubmit } from "@/components/ui/confirm-submit";
import { DataTable, EmptyState, Feedback, MetricCard, PageHeader, PrimaryLink,
  StatusBadge, Surface, type Column } from "@/components/ui/hotel-ui";
import { displayDate, roomName } from "@/lib/hotel";
import { type BookingRequestView } from "@/lib/booking-requests";
import { authenticatedJson, requireAccess } from "@/lib/server-hotel";
import { withdrawStayRequest } from "./actions";

export default async function MyBookingRequests({searchParams}:{searchParams:Promise<{status?:string}>}) {
  const manifest=await requireAccess("booking_request.read.own");
  const requests=await authenticatedJson<BookingRequestView[]>("/api/booking-requests");
  const feedback=(await searchParams).status;
  const canWithdraw=manifest.permissions.includes("booking_request.withdraw.own");
  const columns:Column<BookingRequestView>[]=[
    {key:"ref",label:"Request",render:r=><strong>{r.id.slice(0,8).toUpperCase()}</strong>},
    {key:"stay",label:"Room & dates",render:r=><div><strong>{roomName(r.roomType)} · {r.roomId}</strong>
      <small className="ss-cell-sub">{displayDate(r.checkIn)} – {displayDate(r.checkOut)}</small></div>},
    {key:"guests",label:"Guests",render:r=>r.guestCount},
    {key:"status",label:"Review status",render:r=><StatusBadge value={r.status}/>},
    {key:"note",label:"Review note",render:r=><span className="ss-muted">{r.reviewerNote??"—"}</span>},
    {key:"actions",label:"Next action",render:r=><div className="ss-action-group">
      {r.status==="APPROVED"&&r.reservationId&&
        <Link href={`/reservations/${r.reservationId}/invoice`} className="ss-action-link">View invoice</Link>}
      {canWithdraw && r.status==="PENDING" && <form action={withdrawStayRequest}>
        <input name="requestId" type="hidden" value={r.id}/>
        <ConfirmSubmit className="ss-btn ss-btn-danger-soft"
          message="Withdraw this pending request? No confirmed booking exists yet.">Withdraw</ConfirmSubmit>
      </form>}
    </div>}
  ];
  return <AppShell manifest={manifest}>
    <PageHeader kicker="GUEST SERVICES / REQUEST HISTORY" title="My booking requests"
      description="Track pending requests and staff decisions. Pending requests do not hold rooms or require online payment."
      action={<PrimaryLink href="/availability">Find a room</PrimaryLink>}/>
    <Feedback status={feedback}/>
    <div className="ss-metrics">
      <MetricCard label="Recent requests" value={requests?.length??0} icon={<CalendarDays size={20}/>}/>
      <MetricCard label="Awaiting review" value={(requests??[]).filter(r=>r.status==="PENDING").length} icon={<Clock3 size={20}/>}/>
      <MetricCard label="Approved" value={(requests??[]).filter(r=>r.status==="APPROVED").length} icon={<CircleCheck size={20}/>}/>
    </div>
    <Surface solid className="ss-block">
      {requests===null ? <EmptyState title="Request history unavailable"
        description="Please check your login and backend connection."/> :
        requests.length>0 ? <DataTable rows={requests} columns={columns} rowKey={r=>r.id}/> :
        <EmptyState title="No booking requests yet"
          description="Search for an available room and submit your first stay request."
          action={<PrimaryLink href="/availability">Explore room availability</PrimaryLink>}/>}
    </Surface>
    <div className="ss-request-note"><strong>Pending is not a reservation.</strong> Approval requires staff confirmation
      of the advance and a fresh room-availability check. A rejected or withdrawn request records no payment.</div>
  </AppShell>;
}
