export type RoomPackage = {
  roomType: "STANDARD" | "DELUXE" | "SUITE" | "FAMILY_SUITE";
  dailyRate: number;
  breakfastIncluded: boolean;
  extraGuestRate: number;
  taxRate: number;
};

const sample: RoomPackage[] = [
  { roomType: "STANDARD", dailyRate: 6000, breakfastIncluded: false, extraGuestRate: 1000, taxRate: 0.10 },
  { roomType: "DELUXE", dailyRate: 9000, breakfastIncluded: true, extraGuestRate: 1500, taxRate: 0.12 },
  { roomType: "SUITE", dailyRate: 14000, breakfastIncluded: true, extraGuestRate: 2000, taxRate: 0.15 },
  { roomType: "FAMILY_SUITE", dailyRate: 18000, breakfastIncluded: true, extraGuestRate: 2500, taxRate: 0.15 }
];

// The sample catalog is explicitly labelled when the backend is unavailable.
// This does not report live availability or allow bookings.
export async function getPublicCatalog(): Promise<{ packages: RoomPackage[]; source: "api" | "sample" }> {
  const baseUrl = process.env.API_BASE_URL ?? "http://localhost:8080";
  try {
    const response = await fetch(`${baseUrl}/api/public/room-packages`, {
      cache: "no-store",
      signal: AbortSignal.timeout(3000)
    });
    if (!response.ok) throw new Error(`Catalog HTTP ${response.status}`);
    const data: unknown = await response.json();
    if (!Array.isArray(data) || data.length !== 4) throw new Error("Unexpected catalog");
    return { packages: data as RoomPackage[], source: "api" };
  } catch {
    return { packages: sample, source: "sample" };
  }
}
