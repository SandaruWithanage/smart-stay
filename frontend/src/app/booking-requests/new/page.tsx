import Link from "next/link";
import { notFound, redirect } from "next/navigation";
import { CalendarCheck2, ShieldCheck, CreditCard } from "lucide-react";
import AppShell from "@/components/app-shell";
import { SubmitButton } from "@/components/ui/confirm-submit";
import { EmptyState, FormField, PageHeader, SectionHeader, StatusBadge, Surface } from "@/components/ui/hotel-ui";
import { displayDate, lkr, roomName, type RoomView } from "@/lib/hotel";
import { backendOrigin, sessionManifest } from "@/lib/server-session";
import { submitStayRequest } from "./actions";

type Params={roomId?:string;checkIn?:string;checkOut?:string};
function validDate(v?:string) { return !!v && /^\d{4}-\d{2}-\d{2}$/.test(v) && !Number.isNaN(Date.parse(v)); }

export default async function NewStayRequest({searchParams}:{searchParams:Promise<Params>}) {
  const params=await searchParams;
  const manifest=await sessionManifest();
  if (!manifest) {
    const target="/booking-requests/new?"+new URLSearchParams({
      roomId:(params.roomId??"").slice(0,32),
      checkIn:(params.checkIn??"").slice(0,10),
      checkOut:(params.checkOut??"").slice(0,10)
    });
    redirect("/login?next="+encodeURIComponent(target));
  }
  if (!manifest.permissions.includes("booking_request.create.own")) notFound();
  const roomId=params.roomId??"";
  const checkIn=params.checkIn??"";
  const checkOut=params.checkOut??"";
  let selected:RoomView | undefined;
  if (/^[A-Za-z0-9-]{1,32}$/.test(roomId) &&
    validDate(checkIn)&&validDate(checkOut)&&checkOut>checkIn) {
    try {
      const query=new URLSearchParams({checkIn,checkOut});
      const res=await fetch(`${backendOrigin}/api/public/rooms/available?${query}`,{
        cache:"no-store",signal:AbortSignal.timeout(6000)
      });
      if (res.ok) {
        const list=await res.json() as RoomView[];
        selected=Array.isArray(list)?list.find(r=>r.id===roomId):undefined;
      }
    } catch { /* No unverified availability claims when backend is offline. */ }
  }

  return <AppShell manifest={manifest}>
    <PageHeader kicker="GUEST SERVICES / BOOKING REQUEST" title="Request your stay"
      description="Submit your preferred room and dates for hotel review. This is not a confirmed booking and no payment is collected here."/>
    {!selected ? <EmptyState title="Select an available room first"
      description="Start with the live room availability search to choose a room and dates. A pending request does not hold the room."
      action={<Link href="/availability" className="ss-btn ss-btn-primary">Find available rooms</Link>}/> :
      <div className="ss-booking-layout">
        <Surface solid className="ss-block">
          <SectionHeader title="Request details" subtitle="Your identity is taken from your authenticated guest account."/>
          <div className="ss-request-stay-header">
            <StatusBadge value="PENDING"/>
            <strong>{roomName(selected.roomType)} · {selected.id}</strong>
          </div>
          <form action={submitStayRequest} className="ss-request-form">
            <input type="hidden" name="roomId" value={selected.id}/>
            <input type="hidden" name="checkIn" value={checkIn}/>
            <input type="hidden" name="checkOut" value={checkOut}/>
            <div className="ss-two-column">
              <FormField label="Check-in" name="arrival">
                <input id="arrival" type="text" value={displayDate(checkIn)} readOnly/>
              </FormField>
              <FormField label="Check-out" name="departure">
                <input id="departure" type="text" value={displayDate(checkOut)} readOnly/>
              </FormField>
              <FormField label="Guests" name="guestCount" required>
                <input id="guestCount" name="guestCount" type="number" min={1} max={50}
                  defaultValue={Math.min(2,selected.capacity)} required/>
              </FormField>
              <FormField label="Base room rate" name="rate">
                <input id="rate" type="text" value={lkr(selected.dailyRate)+" / night"} readOnly/>
              </FormField>
            </div>
            <div className="ss-request-note"><strong>Important:</strong> Submitting a request is free. No money is captured or recorded.
              The hotel confirms room availability again at approval and must independently confirm receipt of the LKR 10,000 advance.
              Extra-guest charges, discount and tax are calculated by the Java booking engine.</div>
            <SubmitButton><CalendarCheck2 size={17}/> Submit booking request</SubmitButton>
          </form>
        </Surface>
        <aside className="ss-booking-help">
          <Surface className="ss-block">
            <SectionHeader title="How approval works"/>
            <div className="ss-rule"><CalendarCheck2 size={18}/><span>1. Your request appears as pending.</span></div>
            <div className="ss-rule"><CreditCard size={18}/><span>2. Arrange the advance with authorized hotel staff; no in-app payment integration exists.</span></div>
            <div className="ss-rule"><ShieldCheck size={18}/><span>3. Staff recheck availability and create the real reservation only after receipt is confirmed.</span></div>
          </Surface>
          <Link href="/my/booking-requests" className="ss-action-link">View my requests</Link>
        </aside>
      </div>}
  </AppShell>;
}
