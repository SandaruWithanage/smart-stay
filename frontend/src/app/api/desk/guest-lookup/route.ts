import { NextRequest, NextResponse } from "next/server";
import { isSameOriginRequest } from "@/lib/request-origin";
import { backendForUser } from "@/lib/server-session";

/** Same-origin JSON POST; sensitive NIC/passport values stay out of browser URLs. */
export async function POST(request: NextRequest) {
  if (!isSameOriginRequest(request)) {
    return NextResponse.json({error:"Invalid request origin"}, {status:403});
  }
  const body = await request.json().catch(()=>null) as { documentNumber?:unknown } | null;
  if (!body || typeof body.documentNumber !== "string" || body.documentNumber.trim().length < 1
      || body.documentNumber.length > 64) {
    return NextResponse.json({error:"Enter a valid document number"}, {status:400});
  }
  const response = await backendForUser("/api/guests/lookup",{
    method:"POST",headers:{"Content-Type":"application/json"},
    body:JSON.stringify({documentNumber:body.documentNumber.trim()})
  });
  if (!response) return NextResponse.json({error:"Guest lookup unavailable"}, {status:503});
  if (response.status === 404) return NextResponse.json({error:"No matching guest"}, {status:404});
  if (!response.ok) return NextResponse.json({error:"Lookup is unavailable or not authorized"}, {status:response.status});
  const guest=await response.json() as {id?:string;fullName?:string;email?:string};
  if (!guest.id || !guest.fullName) return NextResponse.json({error:"Invalid guest response"}, {status:502});
  return NextResponse.json({id:guest.id,fullName:guest.fullName,email:guest.email},{
    headers:{"Cache-Control":"no-store"}
  });
}
