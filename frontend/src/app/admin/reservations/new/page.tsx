import Link from "next/link";
import { CalendarCheck2, CreditCard, Info, ShieldCheck, UserRoundPlus } from "lucide-react";
import AppShell from "@/components/app-shell";
import { SubmitButton } from "@/components/ui/confirm-submit";
import { EmptyState, Feedback, FormField, PageHeader, SectionHeader, Surface } from "@/components/ui/hotel-ui";
import { roomName, lkr, type RoomView } from "@/lib/hotel";
import { authenticatedJson, requireAccess } from "@/lib/server-hotel";
import { createDeskBooking } from "./actions";

export default async function NewDeskBooking({searchParams}:{searchParams:Promise<{status?:string}>}) {
  const manifest=await requireAccess("reservation.create.any");
  const rooms=await authenticatedJson<RoomView[]>("/api/admin/rooms");
  const active=(rooms??[]).filter(r=>r.active&&r.operationalStatus==="ACTIVE");
  const status=(await searchParams).status;
  return <AppShell manifest={manifest}>
    <PageHeader kicker="FRONT DESK / RESERVATION ENTRY" title="Create a desk reservation"
      description="Staff-only confirmation. Select an active room, identify the guest, and record the required advance actually received."/>
    <Feedback status={status}/>
    <div className="ss-booking-layout">
      <Surface solid className="ss-block">
        <SectionHeader title="Guest & stay information"
          subtitle="Use an existing guest UUID, or register a new desk guest before confirming this reservation."/>
        {!active.length ? <EmptyState title="No active rooms" description="Add a room before creating bookings."
          action={<Link className="ss-btn ss-btn-secondary" href="/admin/rooms">Manage inventory</Link>}/> :
          <form action={createDeskBooking} className="ss-booking-form">
            <div className="ss-form-section-label"><UserRoundPlus size={18}/> Guest identity</div>
            <FormField name="guestId" label="Existing guest UUID" hint="Optional. If provided, new guest fields are ignored.">
              <input id="guestId" name="guestId" placeholder="Existing registered guest UUID"/></FormField>
            <div className="ss-two-column">
              <FormField name="fullName" label="New guest full name"><input id="fullName" name="fullName" maxLength={200} placeholder="Name as shown on ID"/></FormField>
              <FormField name="documentNumber" label="NIC or passport"><input id="documentNumber" name="documentNumber" maxLength={64} placeholder="Guest document ID"/></FormField>
              <FormField name="contactNumber" label="Contact number"><input id="contactNumber" name="contactNumber" maxLength={32} placeholder="+94…"/></FormField>
              <FormField name="email" label="Guest email"><input id="email" name="email" type="email" maxLength={254} placeholder="guest@example.com"/></FormField>
            </div>
            <div className="ss-form-section-label"><CalendarCheck2 size={18}/> Reservation details</div>
            <FormField name="roomId" label="Room unit" required>
              <select id="roomId" name="roomId" required defaultValue="">
                <option value="" disabled>Select a room unit</option>
                {active.map(r=><option key={r.id} value={r.id}>{r.id} · {roomName(r.roomType)} · {lkr(r.dailyRate)} / night</option>)}
              </select>
            </FormField>
            <div className="ss-two-column">
              <FormField name="checkIn" label="Check-in date" required><input type="date" id="checkIn" name="checkIn" required/></FormField>
              <FormField name="checkOut" label="Check-out date" required><input type="date" id="checkOut" name="checkOut" required/></FormField>
              <FormField name="guestCount" label="Number of guests" required><input type="number" id="guestCount" name="guestCount" min={1} max={50} defaultValue={2} required/></FormField>
            </div>
            <div className="ss-receipt-check">
              <input id="advanceReceived" type="checkbox" name="advanceReceived" required/>
              <label htmlFor="advanceReceived"><strong>I confirm that the LKR 10,000 advance was actually received by the hotel.</strong>
                <small>This is a manual record, not an online payment charge.</small></label>
            </div>
            <div className="ss-booking-submit"><SubmitButton><CreditCard size={17}/> Confirm desk booking</SubmitButton></div>
          </form>}
      </Surface>
      <aside className="ss-booking-help">
        <Surface className="ss-block">
          <SectionHeader title="Booking rules" subtitle="Enforced by Java, not only by the browser."/>
          <div className="ss-rule"><CalendarCheck2 size={18}/><span>At least 48 hours before 2:00 PM check-in (provisional interpretation)</span></div>
          <div className="ss-rule"><ShieldCheck size={18}/><span>No overlapping confirmed reservations for a room</span></div>
          <div className="ss-rule"><Info size={18}/><span>12% base-room discount for 5 or more nights</span></div>
          <div className="ss-rule"><CreditCard size={18}/><span>Exact invoice calculated and snapshotted in PostgreSQL</span></div>
        </Surface>
        <div className="notice"><Info size={19}/><span>Creating a new desk guest does not automatically create a login. Existing guest accounts can use their linked guest UUID; connecting pre-existing unverified desk records requires a separate identity verification flow.</span></div>
        <Link href="/admin/reservations" className="ss-action-link">Return to reservation ledger</Link>
      </aside>
    </div>
  </AppShell>;
}
