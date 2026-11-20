import { CalendarDays, BedDouble, ShieldCheck, UserRound, KeyRound } from "lucide-react";
import AppShell from "@/components/app-shell";
import { MetricCard, PageHeader, PrimaryLink, SectionHeader, Surface } from "@/components/ui/hotel-ui";
import { requireAccess } from "@/lib/server-hotel";

export default async function WorkspacePage() {
  const manifest = await requireAccess("workspace.read");
  const permissions=new Set(manifest.permissions);
  const isAdmin=permissions.has("room.read") || permissions.has("reservation.read.any");
  return <AppShell manifest={manifest}>
    <PageHeader kicker="SMARTSTAY / PERSONAL WORKSPACE" title={`Welcome back, ${manifest.displayName}.`}
      description="Your current permissions and navigation are resolved from PostgreSQL. Only allowed operations are shown."/>
    <div className="ss-metrics">
      <MetricCard label="Account role" value={manifest.roleCodes?.join(", ") || "Guest"} icon={<UserRound size={21}/>}/>
      <MetricCard label="Effective permissions" value={manifest.permissions.length} icon={<KeyRound size={21}/>}
        hint="Checked against the database on each authenticated API request"/>
      <MetricCard label="Navigation items" value={manifest.navigation.length} icon={<ShieldCheck size={21}/>}
        hint="Filtered by the currently active role grants"/>
    </div>
    <Surface className="ss-block">
      <SectionHeader title={isAdmin?"Hotel operations":"Your stays"}
        subtitle="Frequently used activities are available below when your role grants the required permission."/>
      <div className="ss-shortcuts">
        {permissions.has("room.read") && <PrimaryLink href="/admin/rooms"><BedDouble size={16}/> Room inventory</PrimaryLink>}
        {permissions.has("reservation.read.any") && <PrimaryLink href="/admin/reservations"><CalendarDays size={16}/> Reservation ledger</PrimaryLink>}
        {permissions.has("reservation.create.any") && <PrimaryLink href="/admin/reservations/new">New desk reservation</PrimaryLink>}
        {permissions.has("reservation.read.own") && <PrimaryLink href="/my/reservations">My reservations</PrimaryLink>}
        {permissions.has("booking_request.review") && <PrimaryLink href="/admin/booking-requests">Review booking requests</PrimaryLink>}
        {permissions.has("booking_request.read.own") && <PrimaryLink href="/my/booking-requests">My booking requests</PrimaryLink>}
        {permissions.has("booking_request.create.own") && <PrimaryLink href="/availability">Request a stay</PrimaryLink>}
        {permissions.has("room.availability.read") && <PrimaryLink href="/availability">Browse available rooms</PrimaryLink>}
        {permissions.has("access.manage") && <PrimaryLink href="/admin/access">Manage permissions</PrimaryLink>}
      </div>
      <p className="ss-lookup-hint" style={{marginTop:16}}>Any action remains subject to the backend's independent permission, ownership, reservation-status and timing checks.</p>
    </Surface>
  </AppShell>;
}
