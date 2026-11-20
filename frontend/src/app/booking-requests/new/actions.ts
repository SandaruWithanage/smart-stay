"use server";

import { redirect } from "next/navigation";
import { revalidatePath } from "next/cache";
import { backendForUser, sessionManifest } from "@/lib/server-session";

function validDate(value:string) {
  return /^\d{4}-\d{2}-\d{2}$/.test(value) && !Number.isNaN(Date.parse(value));
}

export async function submitStayRequest(form:FormData) {
  const manifest=await sessionManifest();
  if (!manifest) redirect("/login");
  if (!manifest.permissions.includes("booking_request.create.own")) redirect("/workspace");

  const roomId=String(form.get("roomId")??"").trim();
  const checkIn=String(form.get("checkIn")??"");
  const checkOut=String(form.get("checkOut")??"");
  const guestCount=Number(form.get("guestCount")??0);
  if (!/^[A-Za-z0-9-]{1,32}$/.test(roomId) ||
      !validDate(checkIn) || !validDate(checkOut) || checkOut<=checkIn ||
      !Number.isInteger(guestCount) || guestCount<1 || guestCount>50) {
    redirect("/my/booking-requests?status=invalid");
  }

  const response=await backendForUser("/api/booking-requests",{
    method:"POST",headers:{"Content-Type":"application/json"},
    body:JSON.stringify({roomId,checkIn,checkOut,guestCount})
  });
  if (!response) redirect("/my/booking-requests?status=error");
  if (response.status===403) redirect("/my/booking-requests?status=denied");
  if (response.status===409) redirect("/my/booking-requests?status=conflict");
  if (!response.ok) redirect("/my/booking-requests?status=invalid");
  revalidatePath("/my/booking-requests");
  revalidatePath("/admin/booking-requests");
  redirect("/my/booking-requests?status=requested");
}
