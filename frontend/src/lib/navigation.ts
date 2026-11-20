export type RouteKey = "overview" | "packages" | "availability" | "courseworkRules" | "workspace" | "accessControl" | "roomInventory" | "reservationLedger" | "deskBooking" | "myReservations";

export type NavigationItem = {
  id: string;
  label: string;
  routeKey: RouteKey;
  iconKey: "home" | "rooms" | "search" | "rules" | "calendar" | "plus";
  order: number;
  requiredPermission?: string;
  enabled?: boolean;
};

export type UiManifest = {
  configurationVersion: number;
  displayName: string;
  mode: "public-preview" | "authenticated";
  roleCodes?: string[];
  permissions: string[];
  navigation: NavigationItem[];
};

// Only predefined route keys are accepted. The backend must never supply JSX or script.
export const routeRegistry: Record<RouteKey, string> = {
  overview: "/",
  packages: "/#packages",
  availability: "/availability",
  courseworkRules: "/#rules",
  workspace: "/workspace",
  accessControl: "/admin/access",
  roomInventory: "/admin/rooms",
  reservationLedger: "/admin/reservations",
  deskBooking: "/admin/reservations/new",
  myReservations: "/my/reservations"
};

export const publicPreviewManifest: UiManifest = {
  configurationVersion: 1,
  displayName: "Guest preview",
  mode: "public-preview",
  permissions: ["catalog.read", "room.availability.read"],
  navigation: [
    { id: "overview", label: "Overview", routeKey: "overview", iconKey: "home", order: 10 },
    { id: "packages", label: "Room packages", routeKey: "packages", iconKey: "rooms", order: 20, requiredPermission: "catalog.read" },
    { id: "availability", label: "Find available rooms", routeKey: "availability", iconKey: "search", order: 30, requiredPermission: "room.availability.read" },
    { id: "rules", label: "Booking rules", routeKey: "courseworkRules", iconKey: "rules", order: 40 }
  ]
};

export function visibleNavigation(manifest: UiManifest): NavigationItem[] {
  const allowed = new Set(manifest.permissions);
  return manifest.navigation
    .filter(item => Object.hasOwn(routeRegistry, item.routeKey) && item.enabled !== false)
    .filter(item => !item.requiredPermission || allowed.has(item.requiredPermission))
    .sort((a, b) => a.order - b.order);
}
