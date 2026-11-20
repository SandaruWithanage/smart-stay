import { notFound, redirect } from "next/navigation";
import { backendForUser, sessionManifest } from "@/lib/server-session";
import type { UiManifest } from "@/lib/navigation";

export async function requireAccess(permission: string): Promise<UiManifest> {
  const manifest = await sessionManifest();
  if (!manifest) redirect("/login");
  if (!manifest.permissions.includes(permission)) notFound();
  return manifest;
}

export async function authenticatedJson<T>(path: string): Promise<T | null> {
  const result = await backendForUser(path);
  if (!result?.ok) return null;
  try { return await result.json() as T; } catch { return null; }
}
