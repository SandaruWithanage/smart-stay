import Link from "next/link";
import { ClipboardCheck, Clock3, CircleCheck, CircleX } from "lucide-react";
import AppShell from "@/components/app-shell";
import { ConfirmSubmit } from "@/components/ui/confirm-submit";
import { DataTable, EmptyState, Feedback, MetricCard, PageHeader, SectionHeader, StatusBadge,
  Surface, type Column } from "@/components/ui/hotel-ui";
import { displayDate, roomName } from "@/lib/hotel";
import { type BookingRequestView, type BookingRequestStatus } from "@/lib/booking-requests";
import { authenticatedJson, requireAccess } from "@/lib/server-hotel";
import { approveBookingRequest, rejectBookingRequest } from "./actions";

type Search={feedback?:string;filter?:string};
const filters=["ALL","PENDING","APPROVED","REJECTED","WITHDRAWN"] as const;

export default async function BookingRequestQueue({searchParams}:{searchParams:Promise<Search>}) {
  const manifest=await requireAccess("booking_request.review");
  const data=await authenticatedJson<BookingRequestView[]>("/api/admin/booking-requests");
  const search=await searchParams;
  const filter=filters.includes(search.filter as typeof filters[number]) ? search.filter! : "PENDING";
  const filtered=(data??[]).filter(r=>filter==="ALL"||r.status===filter);
  const columns:Column<BookingRequestView>[]=[
    {key:"ref",label:"Request",render:r=><strong>{r.id.slice(0,8).toUpperCase()}</strong>},
    {key:"guest",label:"Guest",render:r=><strong className="ss-table-strong">{r.guestName}</strong>},
    {key:"room",label:"Room",render:r=><div>{r.roomId}<small className="ss-cell-sub">{roomName(r.roomType)}</small></div>},
    {key:"dates",label:"Requested stay",render:r=><div>{displayDate(r.checkIn)}
      <small className="ss-cell-sub">to {displayDate(r.checkOut)} · {r.guestCount} guest(s)</small></div>},
    {key:"state",label:"Status",render:r=><StatusBadge value={r.status}/>},
    {key:"actions",label:"Review action",render:r=>r.status==="PENDING"
      ? <div className="ss-review-controls">
          <form action={approveBookingRequest}>
            <input type="hidden" name="requestId" value={r.id}/>
            <label className="ss-review-confirm">
              <input type="checkbox" name="advanceReceived" required/>
              <span>I confirm LKR 10,000 was actually received</span>
            </label>
            <ConfirmSubmit tone="success"
              message="Approve and create a confirmed reservation? Confirm that the advance was actually received.">Approve</ConfirmSubmit>
          </form>
          <form action={rejectBookingRequest}>
            <input type="hidden" name="requestId" value={r.id}/>
            <textarea name="reason" required maxLength={200} aria-label="Reason for rejection" placeholder="Reason for rejection (required)"/>
            <ConfirmSubmit tone="danger" message="Reject this request? The guest will see the reason.">Reject</ConfirmSubmit>
          </form>
        </div>
      : <div className="ss-action-group">
          {r.status==="APPROVED" && r.reservationId &&
            <Link href={`/reservations/${r.reservationId}/invoice`} className="ss-action-link">View invoice</Link>}
          {r.reviewerNote && <span className="ss-muted">{r.reviewerNote}</span>}
        </div>}
  ];
  return <AppShell manifest={manifest}>
    <PageHeader kicker="FRONT DESK / APPROVAL QUEUE" title="Guest booking requests"
      description="Review requests, confirm received advances, or reject with a reason. A pending request does not hold inventory."/>
    <Feedback status={search.feedback}/>
    <div className="ss-metrics">
      <MetricCard label="Recently submitted" value={data?.length??0} icon={<ClipboardCheck size={20}/>} hint="Up to 50 most recent requests"/>
      <MetricCard label="Awaiting decision" value={(data??[]).filter(r=>r.status==="PENDING").length} icon={<Clock3 size={20}/>}/>
      <MetricCard label="Approved" value={(data??[]).filter(r=>r.status==="APPROVED").length} icon={<CircleCheck size={20}/>}/>
    </div>
    <div className="ss-request-note"><strong>Financial safeguard:</strong> A green approval is only valid after staff
      independently confirm receipt of the advance. Approval attempts recheck room availability and the 48-hour notice
      rule and store a real reservation and invoice. Rejection never records payment.</div>
    <Surface solid className="ss-block">
      <SectionHeader title="Review queue" subtitle="Changes are persisted in PostgreSQL and remain visible to the guest."/>
      <form className="ss-filterbar" action="/admin/booking-requests" method="get">
        <select name="filter" defaultValue={filter} aria-label="Filter booking request status">
          {filters.map(v=><option key={v} value={v}>{v==="ALL"?"All statuses":v.toLowerCase().replace(/^./,s=>s.toUpperCase())}</option>)}
        </select>
        <button type="submit" className="ss-btn ss-btn-secondary">Apply filter</button>
      </form>
      {data===null ? <EmptyState title="Request queue unavailable"
        description="The backend did not return request records. Check the connection and your permissions."/> :
        filtered.length ? <DataTable rows={filtered} columns={columns} rowKey={r=>r.id}/> :
        <EmptyState title="No matching requests"
          description="No requests match the current filter. New guest requests will appear here once submitted."/>}
    </Surface>
  </AppShell>;
}
