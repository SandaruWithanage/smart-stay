/** Only vetted internal destinations may survive an authentication round trip. */
export function safeAfterAuthPath(value:string|null):string {
  if (!value || !value.startsWith("/") || value.startsWith("//") ||
      value.includes("\\") || /[\u0000-\u001f]/.test(value)) return "/workspace";
  try {
    const url=new URL(value,"https://smartstay.invalid");
    if (url.origin!=="https://smartstay.invalid") return "/workspace";
    if (!["/workspace","/my/booking-requests","/booking-requests/new"].includes(url.pathname)) {
      return "/workspace";
    }
    if (url.pathname==="/booking-requests/new") {
      const room=url.searchParams.get("roomId");
      const arrival=url.searchParams.get("checkIn");
      const departure=url.searchParams.get("checkOut");
      if (!room || !/^[A-Za-z0-9-]{1,32}$/.test(room) ||
          !arrival || !departure ||
          !/^\d{4}-\d{2}-\d{2}$/.test(arrival) ||
          !/^\d{4}-\d{2}-\d{2}$/.test(departure) || departure<=arrival) {
        return "/availability";
      }
      return `/booking-requests/new?${new URLSearchParams({
        roomId:room, checkIn:arrival, checkOut:departure
      })}`;
    }
    return url.pathname;
  } catch {
    return "/workspace";
  }
}
