import Link from "next/link";
import { CalendarDays, ClipboardList, Receipt, ShieldCheck } from "lucide-react";
import AppShell from "@/components/app-shell";
import { ConfirmSubmit } from "@/components/ui/confirm-submit";
import { DataTable, EmptyState, Feedback, MetricCard, PageHeader, PrimaryLink, SectionHeader, StatusBadge, Surface, type Column } from "@/components/ui/hotel-ui";
import { displayDate, lkr, roomName, type ReservationView } from "@/lib/hotel";
import { authenticatedJson, requireAccess } from "@/lib/server-hotel";
import { cancelReservation } from "@/app/reservations/[id]/actions";

type Params = { status?:string; filter?:string; query?:string; field?:string };
export default async function ReservationLedger({searchParams}:{searchParams:Promise<Params>}) {
  const manifest = await requireAccess("reservation.read.any");
  const params = await searchParams;
  const field = ["guestName","bookingId","checkIn"].includes(params.field ?? "") ? params.field! : "guestName";
  const query = (params.query ?? "").trim().slice(0,120);
  const valid = (field === "bookingId" && /^[0-9a-f-]{36}$/i.test(query))
    || (field === "checkIn" && /^\d{4}-\d{2}-\d{2}$/.test(query))
    || (field === "guestName" && query.length > 0);
  const url = query && valid ? `/api/reservations?${new URLSearchParams({[field]:query})}`
    : "/api/admin/reservations";
  const bookings = await authenticatedJson<ReservationView[]>(url);
  const status = params.filter ?? "ALL";
  const filtered = (bookings ?? []).filter(b => status === "ALL" || b.status === status);
  const confirmed = (bookings ?? []).filter(b => b.status === "CONFIRMED").length;
  const cancelled = (bookings ?? []).filter(b => b.status === "CANCELLED").length;
  const mayCancel = manifest.permissions.includes("reservation.cancel.any");
  const columns:Column<ReservationView>[] = [
    {key:"booking",label:"Booking reference",render:b=><Link className="ss-record-link" href={`/reservations/${b.bookingId}/invoice`}>{b.bookingId.slice(0,8).toUpperCase()}</Link>},
    {key:"guest",label:"Guest",render:b=><div><strong className="ss-table-strong">{b.guestName}</strong><small className="ss-cell-sub">{b.guestId.slice(0,8)}…</small></div>},
    {key:"room",label:"Room",render:b=><div>{b.roomId}<small className="ss-cell-sub">{roomName(b.roomType)}</small></div>},
    {key:"dates",label:"Stay dates",render:b=><div><span>{displayDate(b.checkIn)}</span><small className="ss-cell-sub">until {displayDate(b.checkOut)}</small></div>},
    {key:"status",label:"State",render:b=><StatusBadge value={b.status}/>},
    {key:"advance",label:"Recorded advance",align:"right",render:b=><span className="ss-tabular">{lkr(b.recordedAdvance)}</span>},
    {key:"actions",label:"Actions",render:b=><div className="ss-row-actions">
      <Link href={`/reservations/${b.bookingId}/invoice`} className="ss-action-link">Invoice</Link>
      {mayCancel && b.status==="CONFIRMED" && <form action={cancelReservation}>
        <input type="hidden" name="bookingId" value={b.bookingId}/>
        <input type="hidden" name="source" value="admin"/>
        <ConfirmSubmit message="Cancel this reservation? The advance refund will be recorded as pending and the invoice voided." className="ss-small-danger">Cancel</ConfirmSubmit>
      </form>}
    </div>}
  ];
  return <AppShell manifest={manifest}>
    <PageHeader kicker="HOTEL OPERATIONS / RESERVATIONS" title="Reservation ledger"
      description="Review recorded reservations, search by guest or booking reference, and manage eligible cancellations."
      action={manifest.permissions.includes("reservation.create.any") ? <PrimaryLink href="/admin/reservations/new">New desk booking</PrimaryLink> : undefined}/>
    <Feedback status={params.status}/>
    <div className="ss-metrics">
      <MetricCard label="Records shown" value={bookings?.length ?? 0} icon={<ClipboardList size={20}/>} hint={query?"Search results":"Up to 50 most recent records"}/>
      <MetricCard label="Confirmed" value={confirmed} icon={<CalendarDays size={20}/>}/>
      <MetricCard label="Cancelled" value={cancelled} icon={<Receipt size={20}/>}/>
    </div>
    <Surface solid className="ss-block">
      <SectionHeader title="Booking records" subtitle="The ledger is sourced from PostgreSQL, not generated demonstration data."/>
      <form action="/admin/reservations" method="get" className="ss-filterbar">
        <select name="field" defaultValue={field} aria-label="Search field">
          <option value="guestName">Guest name</option>
          <option value="bookingId">Booking UUID</option>
          <option value="checkIn">Check-in date (YYYY-MM-DD)</option>
        </select>
        <input name="query" defaultValue={query} placeholder="Search or leave blank for recent bookings…" aria-label="Search reservations"/>
        <select name="filter" defaultValue={status} aria-label="Reservation state">
          <option value="ALL">All states</option><option value="CONFIRMED">Confirmed</option>
          <option value="CANCELLED">Cancelled</option>
        </select>
        <button className="ss-btn ss-btn-secondary">Search ledger</button>
      </form>
      {bookings===null ? <EmptyState title="Ledger unavailable" description="Could not retrieve reservation data. Check the backend and your permissions."/> :
        filtered.length ? <DataTable rows={filtered} columns={columns} rowKey={b=>b.bookingId}/> :
        <EmptyState title="No reservations found" description="Try a different search, or create a desk booking after registering a guest and adding room inventory."
          action={manifest.permissions.includes("reservation.create.any") ? <PrimaryLink href="/admin/reservations/new">Create booking</PrimaryLink> : undefined}/>}
    </Surface>
    <div className="notice"><ShieldCheck size={19}/><span>Booking creation enforces the two-day notice, overlap prevention and manually confirmed advance. Cancellation is independently checked by the Java service against the 24-hour deadline.</span></div>
  </AppShell>;
}
