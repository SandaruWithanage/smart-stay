export type RouteKey = "overview" | "packages" | "courseworkRules";

export type NavigationItem = {
  id: string;
  label: string;
  routeKey: RouteKey;
  iconKey: "home" | "rooms" | "rules";
  order: number;
  requiredPermission?: string;
};

export type UiManifest = {
  configurationVersion: number;
  displayName: string;
  mode: "public-preview" | "authenticated";
  permissions: string[];
  navigation: NavigationItem[];
};

// Only predefined route keys are accepted. The backend must never supply JSX or script.
export const routeRegistry: Record<RouteKey, string> = {
  overview: "/",
  packages: "/#packages",
  courseworkRules: "/#rules"
};

export const publicPreviewManifest: UiManifest = {
  configurationVersion: 1,
  displayName: "Guest preview",
  mode: "public-preview",
  permissions: ["catalog.read"],
  navigation: [
    { id: "overview", label: "Overview", routeKey: "overview", iconKey: "home", order: 10 },
    { id: "packages", label: "Room packages", routeKey: "packages", iconKey: "rooms", order: 20, requiredPermission: "catalog.read" },
    { id: "rules", label: "Booking rules", routeKey: "courseworkRules", iconKey: "rules", order: 30 }
  ]
};

export function visibleNavigation(manifest: UiManifest): NavigationItem[] {
  const allowed = new Set(manifest.permissions);
  return manifest.navigation
    .filter(item => item.routeKey in routeRegistry)
    .filter(item => !item.requiredPermission || allowed.has(item.requiredPermission))
    .sort((a, b) => a.order - b.order);
}
