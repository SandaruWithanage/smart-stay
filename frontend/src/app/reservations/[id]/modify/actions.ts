"use server";

import { redirect } from "next/navigation";
import { revalidatePath } from "next/cache";
import { backendForUser, sessionManifest } from "@/lib/server-session";

function validDate(v:string) {
  return /^\d{4}-\d{2}-\d{2}$/.test(v) && !Number.isNaN(Date.parse(v));
}

export async function saveReservationModification(form:FormData) {
  const manifest=await sessionManifest();
  if (!manifest) redirect("/login");
  if (!manifest.permissions.some(p=>p==="reservation.modify.own"||p==="reservation.modify.any")) {
    redirect("/workspace");
  }
  const bookingId=String(form.get("bookingId")??"");
  if (!/^[0-9a-f-]{36}$/i.test(bookingId)) redirect("/my/reservations?status=invalid");
  const dest=`/reservations/${bookingId}/modify`;
  const checkIn=String(form.get("checkIn")??"");
  const checkOut=String(form.get("checkOut")??"");
  const guestCount=Number(form.get("guestCount")??0);
  if (!validDate(checkIn)||!validDate(checkOut)||checkOut<=checkIn
      ||!Number.isInteger(guestCount)||guestCount<1||guestCount>50) {
    redirect(dest+"?status=invalid");
  }
  const response=await backendForUser(`/api/reservations/${bookingId}`,{
    method:"PUT",headers:{"Content-Type":"application/json"},
    body:JSON.stringify({checkIn,checkOut,guestCount})
  });
  if (!response) redirect(dest+"?status=error");
  if (response.status===403) redirect(dest+"?status=denied");
  if ([400,409].includes(response.status)) redirect(dest+"?status=conflict");
  if (!response.ok) redirect(dest+"?status=error");
  revalidatePath(dest);
  revalidatePath(`/reservations/${bookingId}/invoice`);
  revalidatePath("/my/reservations");
  revalidatePath("/admin/reservations");
  redirect(`/reservations/${bookingId}/invoice?updated=1`);
}
