"use server";

import { redirect } from "next/navigation";
import { revalidatePath } from "next/cache";
import { backendForUser, sessionManifest } from "@/lib/server-session";

export async function saveRolePermissions(data: FormData) {
  const manifest = await sessionManifest();
  if (!manifest?.permissions.includes("access.manage")) redirect("/workspace");
  const role = String(data.get("role") ?? "");
  if (role !== "ADMIN" && role !== "GUEST") redirect("/admin/access?status=invalid");
  const permissions = data.getAll("permission")
    .map(String).filter(value => value.length > 0 && value.length < 100);
  const res = await backendForUser(`/api/admin/access/roles/${role}/permissions`, {
    method: "PUT", headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ permissions: [...new Set(permissions)] })
  });
  if (!res?.ok) redirect("/admin/access?status=failed");
  revalidatePath("/workspace");
  revalidatePath("/admin/access");
  redirect("/admin/access?status=saved");
}

export async function saveNavigation(data: FormData) {
  const manifest = await sessionManifest();
  if (!manifest?.permissions.includes("navigation.manage")) redirect("/workspace");
  const id = String(data.get("id") ?? "");
  if (!/^[a-z0-9-]{1,64}$/.test(id)) redirect("/admin/access?status=invalid");
  const label = String(data.get("label") ?? "").trim().slice(0, 100);
  const order = Number(data.get("order") ?? 100);
  const enabled = data.get("enabled") === "on";
  if (!label || !Number.isInteger(order) || order < 0 || order > 9999) {
    redirect("/admin/access?status=invalid");
  }
  const res = await backendForUser(`/api/admin/access/navigation/${id}`, {
    method: "PUT", headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ label, order, enabled })
  });
  if (!res?.ok) redirect("/admin/access?status=failed");
  revalidatePath("/workspace");
  revalidatePath("/admin/access");
  redirect("/admin/access?status=saved");
}
