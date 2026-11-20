import type { NextRequest } from "next/server";

/** Only permit first-party browser POSTs; do not trust a framework-inferred origin.
 * NextRequest.nextUrl.origin can be a canonical localhost origin even when a
 * browser navigated to 127.0.0.1 or through a trusted reverse proxy.
 * Compare the browser Origin against the actual Host header.
 */
export function isSameOriginRequest(request: NextRequest): boolean {
  const raw = request.headers.get("origin");
  const host = request.headers.get("host");
  if (!raw || !host) return false;
  try {
    const origin = new URL(raw);
    return (origin.protocol === "https:" || origin.protocol === "http:")
      && origin.username === "" && origin.password === ""
      && origin.host.toLowerCase() === host.toLowerCase()
      && origin.pathname === "/" && origin.search === "" && origin.hash === "";
  } catch {
    return false;
  }
}
