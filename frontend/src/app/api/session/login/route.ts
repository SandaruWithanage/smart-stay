import { NextRequest, NextResponse } from "next/server";
import { isSameOriginRequest } from "@/lib/request-origin";
import { backendOrigin, COOKIE_NAME } from "@/lib/server-session";

export async function POST(request: NextRequest) {
  // Login through this first-party BFF so access tokens are never exposed to client JS.
  if (!isSameOriginRequest(request)) {
    return NextResponse.json({ error: "Invalid request origin" }, { status: 403 });
  }
  const body = await request.json().catch(() => null);
  if (!body || typeof body.email !== "string" || typeof body.password !== "string") {
    return NextResponse.json({ error: "Email and password are required" }, { status: 400 });
  }
  try {
    const upstream = await fetch(backendOrigin + "/api/auth/login", {
      method: "POST", headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ email: body.email, password: body.password }),
      cache: "no-store", signal: AbortSignal.timeout(6000)
    });
    if (!upstream.ok) {
      return NextResponse.json({ error: "Invalid credentials" }, { status: 401 });
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
    return NextResponse.json({ error: "Login service unavailable" }, { status: 503 });
  }
}
