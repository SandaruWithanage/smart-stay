"use server";

import { redirect } from "next/navigation";
import { revalidatePath } from "next/cache";
import { backendForUser, sessionManifest } from "@/lib/server-session";
import { bedTypes, roomStatuses, roomTypes, type RoomType } from "@/lib/hotel";

function readValues(form: FormData) {
  const id = String(form.get("id") ?? "").trim();
  const roomType = String(form.get("roomType") ?? "");
  const bedType = String(form.get("bedType") ?? "");
  const operationalStatus = String(form.get("operationalStatus") ?? "");
  const capacity = Number(form.get("capacity") ?? 0);
  if (!/^[A-Za-z0-9-]{1,32}$/.test(id) || !roomTypes.includes(roomType as RoomType) ||
      !bedTypes.includes(bedType as typeof bedTypes[number]) ||
      !roomStatuses.includes(operationalStatus as typeof roomStatuses[number]) ||
      !Number.isInteger(capacity) || capacity <= 0 || capacity > 50) return null;
  return { id, roomType, bedType, operationalStatus, capacity };
}

async function ensure(permission: string) {
  const manifest = await sessionManifest();
  if (!manifest) redirect("/login");
  if (!manifest.permissions.includes(permission)) redirect("/workspace");
}

async function updateRequest(path:string, method:"POST"|"PUT"|"DELETE", body?:object) {
  const res = await backendForUser(path, {
    method, headers: { "Content-Type":"application/json" },
    ...(body ? { body: JSON.stringify(body) } : {})
  });
  if (!res) redirect("/admin/rooms?status=error");
  if (res.status === 403) redirect("/admin/rooms?status=denied");
  if (res.status === 409) redirect("/admin/rooms?status=conflict");
  if (!res.ok) redirect("/admin/rooms?status=error");
}

export async function createRoom(form:FormData) {
  await ensure("room.create");
  const payload = readValues(form);
  if (!payload) redirect("/admin/rooms?status=invalid");
  await updateRequest("/api/admin/rooms","POST",payload);
  revalidatePath("/admin/rooms");
  redirect("/admin/rooms?status=created");
}

export async function editRoom(form:FormData) {
  await ensure("room.update");
  const payload = readValues(form);
  if (!payload) redirect("/admin/rooms?status=invalid");
  await updateRequest(`/api/admin/rooms/${encodeURIComponent(payload.id)}`,"PUT",{
    roomType:payload.roomType, bedType:payload.bedType,
    capacity:payload.capacity, operationalStatus:payload.operationalStatus
  });
  revalidatePath("/admin/rooms");
  redirect("/admin/rooms?status=saved");
}

export async function retireRoom(form:FormData) {
  await ensure("room.delete");
  const id = String(form.get("id") ?? "");
  if (!/^[A-Za-z0-9-]{1,32}$/.test(id)) redirect("/admin/rooms?status=invalid");
  await updateRequest(`/api/admin/rooms/${encodeURIComponent(id)}`,"DELETE");
  revalidatePath("/admin/rooms");
  redirect("/admin/rooms?status=retired");
}
