import { NextRequest, NextResponse } from "next/server";
import { isSameOriginRequest } from "@/lib/request-origin";
import { accessToken, backendOrigin, COOKIE_NAME } from "@/lib/server-session";

export async function POST(request: NextRequest) {
  if (!isSameOriginRequest(request)) {
    return NextResponse.json({ error: "Invalid request origin" }, { status: 403 });
  }
  const token = await accessToken();
  if (token) {
    try {
      await fetch(backendOrigin + "/api/auth/logout", {
        method: "POST", headers: { Authorization: "Bearer " + token },
        cache: "no-store", signal: AbortSignal.timeout(5000)
      });
    } catch { /* Clear browser session even if backend cannot be contacted. */ }
  }
  const res = NextResponse.json({ ok: true }, { headers: { "Cache-Control": "no-store" } });
  res.cookies.set(COOKIE_NAME, "", { path: "/", maxAge: 0 });
  return res;
}
