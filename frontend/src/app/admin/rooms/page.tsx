import { notFound } from "next/navigation";
import { BedDouble, CirclePlus, ShieldCheck, Wrench } from "lucide-react";
import AppShell from "@/components/app-shell";
import { ConfirmSubmit, SubmitButton } from "@/components/ui/confirm-submit";
import { DataTable, EmptyState, Feedback, FormField, MetricCard, PageHeader, PrimaryLink, SectionHeader, StatusBadge, Surface, type Column } from "@/components/ui/hotel-ui";
import { type RoomView, roomTypes, bedTypes, roomStatuses, roomName, lkr } from "@/lib/hotel";
import { authenticatedJson, requireAccess } from "@/lib/server-hotel";
import { createRoom, editRoom, retireRoom } from "./actions";

type Params = { status?:string; query?:string; filter?:string };

export default async function RoomsPage({ searchParams }: { searchParams:Promise<Params> }) {
  const manifest = await requireAccess("room.read");
  const rooms = await authenticatedJson<RoomView[]>("/api/admin/rooms");
  const params = await searchParams;
  if (rooms == null) return <AppShell manifest={manifest}>
    <EmptyState title="Room inventory unavailable" description="The backend did not return inventory records. Check the API connection and current permissions."/>
  </AppShell>;
  const search = (params.query ?? "").trim().toLowerCase().slice(0,60);
  const filter = params.filter ?? "all";
  const filtered = rooms.filter(room =>
    (room.id.toLowerCase().includes(search) || roomName(room.roomType).toLowerCase().includes(search))
    && (filter === "all" || (filter === "active" && room.active && room.operationalStatus === "ACTIVE")
      || (filter === "maintenance" && room.active && room.operationalStatus === "MAINTENANCE")
      || (filter === "retired" && !room.active)));
  const active = rooms.filter(r => r.active && r.operationalStatus === "ACTIVE").length;
  const maintenance = rooms.filter(r => r.active && r.operationalStatus === "MAINTENANCE").length;
  const mayEdit = manifest.permissions.includes("room.update");
  const mayDelete = manifest.permissions.includes("room.delete");
  const columns:Column<RoomView>[] = [
    {key:"id",label:"Room / Unit", render:r=><strong className="ss-table-strong">{r.id}</strong>},
    {key:"category",label:"Category",render:r=>roomName(r.roomType)},
    {key:"bed",label:"Bed",render:r=><span className="ss-muted">{r.bedType}</span>},
    {key:"capacity",label:"Capacity",render:r=>`${r.capacity} guests`},
    {key:"rate",label:"Nightly rate",render:r=><span className="ss-tabular">{lkr(r.dailyRate)}</span>,align:"right"},
    {key:"status",label:"Operational state",render:r=><StatusBadge value={!r.active?"RETIRED":r.operationalStatus}/>},
    {key:"actions",label:"Manage",render:r=><div className="ss-row-actions">
      {mayEdit && r.active && <details className="ss-inline-details">
        <summary>Edit</summary>
        <form action={editRoom} className="ss-inline-editor">
          <input name="id" type="hidden" value={r.id}/>
          <FormField name={`type-${r.id}`} label="Room type"><select name="roomType" id={`type-${r.id}`} defaultValue={r.roomType}>
            {roomTypes.map(t=><option value={t} key={t}>{roomName(t)}</option>)}</select></FormField>
          <FormField name={`bed-${r.id}`} label="Bed type"><select id={`bed-${r.id}`} name="bedType" defaultValue={r.bedType}>
            {bedTypes.map(t=><option key={t}>{t}</option>)}</select></FormField>
          <FormField name={`capacity-${r.id}`} label="Capacity"><input id={`capacity-${r.id}`} type="number" name="capacity" min={1} max={50} required defaultValue={r.capacity}/></FormField>
          <FormField name={`status-${r.id}`} label="Operational state"><select id={`status-${r.id}`} name="operationalStatus" defaultValue={r.operationalStatus}>
            {roomStatuses.map(t=><option key={t}>{t}</option>)}</select></FormField>
          <SubmitButton>Save room</SubmitButton>
        </form>
      </details>}
      {mayDelete && r.active && <form action={retireRoom}>
        <input type="hidden" name="id" value={r.id}/>
        <ConfirmSubmit message={`Retire room ${r.id}? This is not allowed when it has active/future reservations.`}>Retire</ConfirmSubmit>
      </form>}
      {!mayEdit && !mayDelete && <span className="ss-muted">Read only</span>}
    </div>}
  ];
  return <AppShell manifest={manifest}>
    <PageHeader kicker="HOTEL OPERATIONS / INVENTORY" title="Room inventory"
      description="Maintain physical room details, capacity, status and the four coursework pricing categories."
      action={<PrimaryLink href="/admin/reservations/new">New desk reservation</PrimaryLink>}/>
    <Feedback status={params.status}/>
    <div className="ss-metrics">
      <MetricCard label="All room units" value={rooms.length} icon={<BedDouble size={20}/>}/>
      <MetricCard label="Operational" value={active} icon={<ShieldCheck size={20}/>} hint="Bookings may occupy these rooms on selected dates."/>
      <MetricCard label="Under maintenance" value={maintenance} icon={<Wrench size={20}/>}/>
    </div>
    {manifest.permissions.includes("room.create") && <Surface className="ss-block">
      <SectionHeader title="Add a room" subtitle="Capacity is explicitly entered; no unsupported default is assumed."/>
      <form action={createRoom} className="ss-grid-form">
        <FormField label="Unique room ID" name="new-room-id" required><input id="new-room-id" name="id" placeholder="E.g. D-204" pattern="[A-Za-z0-9-]{1,32}" maxLength={32} required/></FormField>
        <FormField label="Room package" name="new-room-type" required><select id="new-room-type" name="roomType" defaultValue="STANDARD">
          {roomTypes.map(t=><option key={t} value={t}>{roomName(t)}</option>)}</select></FormField>
        <FormField label="Bed type" name="new-bed" required><select id="new-bed" name="bedType" defaultValue="QUEEN">
          {bedTypes.map(t=><option key={t}>{t}</option>)}</select></FormField>
        <FormField label="Maximum guests" name="new-capacity" required><input id="new-capacity" name="capacity" type="number" min={1} max={50} required placeholder="Enter approved capacity"/></FormField>
        <FormField label="Operational status" name="new-status" required><select id="new-status" name="operationalStatus" defaultValue="ACTIVE">
          {roomStatuses.map(t=><option key={t}>{t}</option>)}</select></FormField>
        <div className="ss-form-action"><SubmitButton><CirclePlus size={17}/> Add room</SubmitButton></div>
      </form>
    </Surface>}
    <Surface solid className="ss-block">
      <SectionHeader title="Live inventory roster" subtitle="Room availability for a stay is checked separately against confirmed reservation dates."/>
      <form className="ss-filterbar" action="/admin/rooms" method="get">
        <input name="query" aria-label="Search room ID or type" placeholder="Search room ID or package..." defaultValue={params.query ?? ""}/>
        <select name="filter" aria-label="Filter room state" defaultValue={filter}>
          <option value="all">All room states</option><option value="active">Active</option>
          <option value="maintenance">Maintenance</option><option value="retired">Retired</option>
        </select>
        <button className="ss-btn ss-btn-secondary" type="submit">Filter rooms</button>
      </form>
      {filtered.length>0?<DataTable rows={filtered} columns={columns} rowKey={r=>r.id}/>:
        <EmptyState title="No matching rooms" description="Add your first room above or adjust the current inventory filters."/>}
    </Surface>
    <div className="notice"><ShieldCheck size={19}/><span>Room type rates come from the database package catalog. Retiring a room preserves historical records and is blocked if a current or future confirmed booking exists.</span></div>
  </AppShell>;
}
