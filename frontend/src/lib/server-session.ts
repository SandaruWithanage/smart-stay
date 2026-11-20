import { cookies } from "next/headers";
import { type UiManifest, type NavigationItem, routeRegistry } from "@/lib/navigation";

export const COOKIE_NAME = process.env.NODE_ENV === "production"
  ? "__Host-smartstay_access"
  : "smartstay_access";

export const backendOrigin = process.env.API_BASE_URL ?? "http://localhost:8080";
const iconKeys = new Set(["home", "rooms", "search", "rules"]);

export async function accessToken(): Promise<string | undefined> {
  return (await cookies()).get(COOKIE_NAME)?.value;
}

export async function backendForUser(path: string, options?: RequestInit): Promise<Response | null> {
  const token = await accessToken();
  if (!token) return null;
  try {
    return await fetch(backendOrigin + path, {
      ...options,
      headers: { "Authorization": "Bearer " + token, ...options?.headers },
      cache: "no-store",
      signal: AbortSignal.timeout(6000)
    });
  } catch {
    return null;
  }
}

function safeItem(raw: unknown): NavigationItem | null {
  if (!raw || typeof raw !== "object") return null;
  const o = raw as Record<string, unknown>;
  if (typeof o.routeKey !== "string" || !Object.hasOwn(routeRegistry, o.routeKey)) return null;
  if (typeof o.iconKey !== "string" || !iconKeys.has(o.iconKey)) return null;
  if (typeof o.id !== "string" || typeof o.label !== "string") return null;
  if (o.id.length > 64 || o.label.length > 100) return null;
  return {
    id: o.id,
    label: o.label,
    routeKey: o.routeKey as NavigationItem["routeKey"],
    iconKey: o.iconKey as NavigationItem["iconKey"],
    order: typeof o.order === "number" ? o.order : 100,
    requiredPermission: typeof o.requiredPermission === "string" ? o.requiredPermission : undefined,
    enabled: o.enabled !== false
  };
}

export async function sessionManifest(): Promise<UiManifest | null> {
  const res = await backendForUser("/api/me/ui-config");
  if (!res?.ok) return null;
  try {
    const raw: unknown = await res.json();
    if (!raw || typeof raw !== "object") return null;
    const data = raw as Record<string, unknown>;
    if (!Array.isArray(data.permissions) || !Array.isArray(data.navigation)
        || typeof data.displayName !== "string") return null;
    return {
      configurationVersion: typeof data.configurationVersion === "number" ? data.configurationVersion : 0,
      displayName: data.displayName,
      mode: "authenticated",
      permissions: data.permissions.filter((v): v is string => typeof v === "string"),
      roleCodes: Array.isArray(data.roleCodes) ?
        data.roleCodes.filter((v): v is string => typeof v === "string") : [],
      navigation: data.navigation.map(safeItem).filter((v): v is NavigationItem => Boolean(v))
    };
  } catch {
    return null;
  }
}
