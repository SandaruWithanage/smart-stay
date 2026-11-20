import Link from "next/link";
import { BedDouble, CalendarDays, CircleAlert, Search, ShieldCheck } from "lucide-react";
import AppShell from "@/components/app-shell";
import { publicPreviewManifest } from "@/lib/navigation";

type RoomType = "STANDARD" | "DELUXE" | "SUITE" | "FAMILY_SUITE";
type Room = {
  id: string;
  roomType: RoomType;
  bedType: string;
  capacity: number;
  dailyRate: number;
};
type Params = { checkIn?: string; checkOut?: string; roomType?: string };
type Result = { kind: "initial" | "error" | "success"; rooms: Room[]; error?: string };
const types: RoomType[] = ["STANDARD", "DELUXE", "SUITE", "FAMILY_SUITE"];
const labels: Record<RoomType, string> = {
  STANDARD: "Standard", DELUXE: "Deluxe", SUITE: "Suite", FAMILY_SUITE: "Family Suite"
};
const validDate = (input: string) =>
  /^\d{4}-\d{2}-\d{2}$/.test(input) && !Number.isNaN(Date.parse(input));

async function findRooms(params: Params): Promise<Result> {
  const { checkIn, checkOut, roomType } = params;
  if (!checkIn && !checkOut) return { kind: "initial", rooms: [] };
  if (!checkIn || !checkOut || !validDate(checkIn) || !validDate(checkOut) || checkOut <= checkIn) {
    return { kind: "error", rooms: [], error: "Choose valid dates with check-out after check-in." };
  }
  if (roomType && roomType !== "ALL" && !types.includes(roomType as RoomType)) {
    return { kind: "error", rooms: [], error: "Choose a supported room category." };
  }
  const query = new URLSearchParams({ checkIn, checkOut });
  if (roomType && roomType !== "ALL") query.set("roomType", roomType);
  const api = process.env.API_BASE_URL ?? "http://localhost:8080";
  try {
    const response = await fetch(`${api}/api/public/rooms/available?${query}`, {
      cache: "no-store",
      signal: AbortSignal.timeout(5000)
    });
    if (!response.ok) throw new Error("Availability service returned an error");
    const data: unknown = await response.json();
    if (!Array.isArray(data)) throw new Error("Unexpected response");
    const rooms = data.filter((v): v is Room =>
      v !== null && typeof v === "object" && typeof v.id === "string"
      && typeof v.roomType === "string" && types.includes(v.roomType as RoomType)
      && typeof v.dailyRate === "number");
    return { kind: "success", rooms };
  } catch {
    return { kind: "error", rooms: [], error: "The availability service is currently unavailable. Start the Spring Boot backend and try again." };
  }
}

export default async function AvailabilityPage({
  searchParams
}: {
  searchParams: Promise<Params>;
}) {
  const params = await searchParams;
  const result = await findRooms(params);
  const amount = (value: number) => new Intl.NumberFormat("en-LK", {
    maximumFractionDigits: 0
  }).format(value);

  return (
    <AppShell manifest={publicPreviewManifest}>
      <section className="availability-header">
        <span className="eyebrow accent">ROOM COLLECTION / SEARCH</span>
        <h1>Find your next <span style={{ color: "var(--deep)" }}>comfortable stay.</span></h1>
        <p>Explore actual room records for your preferred dates. Availability is checked against confirmed reservations and room maintenance status in PostgreSQL.</p>
      </section>
      <form method="get" action="/availability" className="availability-form glass-panel" aria-label="Search available rooms">
        <div className="availability-field">
          <label htmlFor="checkIn">Check-in date</label>
          <input type="date" id="checkIn" name="checkIn" required defaultValue={params.checkIn ?? ""}/>
        </div>
        <div className="availability-field">
          <label htmlFor="checkOut">Check-out date</label>
          <input type="date" id="checkOut" name="checkOut" required defaultValue={params.checkOut ?? ""}/>
        </div>
        <div className="availability-field">
          <label htmlFor="roomType">Room category</label>
          <select id="roomType" name="roomType" defaultValue={params.roomType ?? "ALL"}>
            <option value="ALL">All room categories</option>
            {types.map(type => <option key={type} value={type}>{labels[type]}</option>)}
          </select>
        </div>
        <button type="submit" className="availability-submit"><Search size={17}/> Find rooms</button>
      </form>
      <section className="availability-results" aria-live="polite">
        <h2>Room availability</h2>
        {result.kind === "initial" && (
          <div className="availability-empty glass-panel">
            <h3><CalendarDays size={20} style={{ display: "inline", verticalAlign:"middle", marginRight:9 }}/> Select your dates</h3>
            <p>Choose a check-in and check-out date above to query live room inventory. The coursework's 48-hour booking-notice rule will be enforced when a reservation is created.</p>
          </div>
        )}
        {result.kind === "error" && (
          <div className="availability-empty glass-panel" role="alert">
            <h3><CircleAlert size={20} style={{ display: "inline", verticalAlign:"middle", marginRight:9 }}/> Search unavailable</h3>
            <p>{result.error}</p>
          </div>
        )}
        {result.kind === "success" && result.rooms.length === 0 && (
          <div className="availability-empty glass-panel">
            <h3>No available rooms for these dates</h3>
            <p>There may be no room records yet, or all matching rooms may be reserved or under maintenance. Try another date range or category.</p>
          </div>
        )}
        {result.kind === "success" && result.rooms.length > 0 && (
          <div className="availability-result-grid">
            {result.rooms.map(room => (
              <article key={room.id} className="availability-room glass-panel">
                <div className="availability-state"><BedDouble size={18}/> Available for selected dates</div>
                <h3>{labels[room.roomType]} · {room.id}</h3>
                <p>{room.bedType.toLowerCase().replace(/^./, c => c.toUpperCase())} bed · Standard capacity {room.capacity} guests</p>
                <div className="availability-price">LKR {amount(room.dailyRate)} <small>/ night, before adjustments</small></div>
                <p>Availability is rechecked when staff review your booking request. No payment is taken online.</p>
                <Link className="ss-btn ss-btn-primary" href={`/booking-requests/new?${new URLSearchParams({
                  roomId:room.id,checkIn:params.checkIn??"",checkOut:params.checkOut??""
                })}`}>Request this stay</Link>
              </article>
            ))}
          </div>
        )}
      </section>
      <div className="notice"><ShieldCheck size={19}/><span>Search results are public and read-only. Requesting a stay requires a guest login and does not reserve a room. Staff approvals recheck availability and require manual advance receipt. Prices may include taxes, discounts and extras.</span></div>
      <div className="availability-actions"><Link href="/" className="availability-back">Return to overview</Link></div>
    </AppShell>
  );
}
