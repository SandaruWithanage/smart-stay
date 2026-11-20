"use server";

import { redirect } from "next/navigation";
import { revalidatePath } from "next/cache";
import { backendForUser, sessionManifest } from "@/lib/server-session";

export async function cancelReservation(form: FormData) {
  const manifest = await sessionManifest();
  if (!manifest) redirect("/login");
  if (!manifest.permissions.some(p => ["reservation.cancel.any","reservation.cancel.own"].includes(p))) {
    redirect("/workspace");
  }
  const bookingId = String(form.get("bookingId") ?? "");
  const source = form.get("source") === "admin" ? "admin" : "guest";
  const back = source === "admin" ? "/admin/reservations" : "/my/reservations";
  if (!/^[0-9a-fA-F-]{36}$/.test(bookingId)) redirect(back+"?status=invalid");
  const response = await backendForUser(
    `/api/reservations/${encodeURIComponent(bookingId)}/cancel`, {method:"POST"});
  if (!response) redirect(back+"?status=error");
  if (response.status === 403) redirect(back+"?status=denied");
  if (response.status === 409 || response.status === 400) redirect(back+"?status=conflict");
  if (!response.ok) redirect(back+"?status=error");
  revalidatePath(back);
  revalidatePath(`/reservations/${bookingId}/invoice`);
  redirect(back+"?status=cancelled");
}
