"use server";

import { redirect } from "next/navigation";
import { revalidatePath } from "next/cache";
import { backendForUser, sessionManifest } from "@/lib/server-session";

export async function withdrawStayRequest(form:FormData) {
  const manifest=await sessionManifest();
  if (!manifest) redirect("/login");
  if (!manifest.permissions.includes("booking_request.withdraw.own")) redirect("/workspace");
  const id=String(form.get("requestId")??"");
  if (!/^[0-9a-f-]{36}$/i.test(id)) redirect("/my/booking-requests?status=invalid");
  const response=await backendForUser(`/api/booking-requests/${id}/withdraw`,{method:"POST"});
  if (!response) redirect("/my/booking-requests?status=error");
  if (response.status===403) redirect("/my/booking-requests?status=denied");
  if (response.status===409) redirect("/my/booking-requests?status=conflict");
  if (!response.ok) redirect("/my/booking-requests?status=error");
  revalidatePath("/my/booking-requests");
  revalidatePath("/admin/booking-requests");
  redirect("/my/booking-requests?status=withdrawn");
}
