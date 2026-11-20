"use server";

import { redirect } from "next/navigation";
import { revalidatePath } from "next/cache";
import { backendForUser, sessionManifest } from "@/lib/server-session";

function status(name:string):never { redirect(`/admin/booking-requests?feedback=${name}`); }

async function guard() {
  const manifest=await sessionManifest();
  if (!manifest) redirect("/login");
  if (!manifest.permissions.includes("booking_request.review")) redirect("/workspace");
}
function requestId(form:FormData):string {
  const id=String(form.get("requestId")??"");
  if (!/^[0-9a-f-]{36}$/i.test(id)) status("invalid");
  return id;
}
function invalidate() {
  revalidatePath("/admin/booking-requests");
  revalidatePath("/admin/reservations");
  revalidatePath("/my/booking-requests");
  revalidatePath("/my/reservations");
}

export async function approveBookingRequest(form:FormData) {
  await guard();
  const id=requestId(form);
  if (form.get("advanceReceived")!=="on") status("invalid");
  const response=await backendForUser(`/api/admin/booking-requests/${id}/approve`,{
    method:"POST", headers:{"Content-Type":"application/json"},
    body:JSON.stringify({advanceReceived:true})
  });
  if (!response) status("error");
  if (response.status===403) status("denied");
  if (response.status===409 || response.status===400) status("conflict");
  if (!response.ok) status("error");
  invalidate();
  status("approved");
}

export async function rejectBookingRequest(form:FormData) {
  await guard();
  const id=requestId(form);
  const reason=String(form.get("reason")??"").trim();
  if (!reason || reason.length>200) status("invalid");
  const response=await backendForUser(`/api/admin/booking-requests/${id}/reject`,{
    method:"POST",headers:{"Content-Type":"application/json"},
    body:JSON.stringify({reason})
  });
  if (!response) status("error");
  if (response.status===403) status("denied");
  if (response.status===409) status("conflict");
  if (!response.ok) status("invalid");
  invalidate();
  status("rejected");
}
