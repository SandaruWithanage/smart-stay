import { NextRequest, NextResponse } from "next/server";
import { backendOrigin, COOKIE_NAME } from "@/lib/server-session";

export async function POST(request: NextRequest) {
  if (request.headers.get("origin") !== request.nextUrl.origin) {
    return NextResponse.json({ error: "Invalid request origin" }, { status: 403 });
  }
  const body = await request.json().catch(() => null);
  if (!body || typeof body !== "object") {
    return NextResponse.json({ error: "Invalid registration" }, { status: 400 });
  }
  try {
    const upstream = await fetch(backendOrigin + "/api/auth/register", {
      method: "POST", headers: { "Content-Type": "application/json" },
      body: JSON.stringify(body),
      cache: "no-store", signal: AbortSignal.timeout(6000)
    });
    if (!upstream.ok) {
      return NextResponse.json({
        error: upstream.status === 409 ? "This account or document is already registered" :
          "Registration details are invalid"
      }, { status: upstream.status === 409 ? 409 : 400 });
    }
    const token = await upstream.json();
    if (typeof token.accessToken !== "string" || token.tokenType !== "Bearer") {
      throw new Error("Invalid backend response");
    }
    const response = NextResponse.json({ ok: true }, { headers: { "Cache-Control": "no-store" } });
    response.cookies.set(COOKIE_NAME, token.accessToken, {
      httpOnly: true, secure: process.env.NODE_ENV === "production",
      sameSite: "strict", path: "/", maxAge: Math.min(token.expiresInSeconds ?? 900, 900)
    });
    return response;
  } catch {
    return NextResponse.json({ error: "Registration service unavailable" }, { status: 503 });
  }
}
