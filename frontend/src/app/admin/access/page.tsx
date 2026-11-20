import { notFound, redirect } from "next/navigation";
import { ShieldCheck, SlidersHorizontal, ListTree } from "lucide-react";
import AppShell from "@/components/app-shell";
import { backendForUser, sessionManifest } from "@/lib/server-session";
import { saveRolePermissions, saveNavigation } from "./actions";

type Nav = {
  id: string; label: string; routeKey: string; iconKey: string;
  order: number; requiredPermission: string | null; enabled: boolean;
};

async function jsonData<T>(path: string): Promise<T | null> {
  const response = await backendForUser(path);
  if (!response?.ok) return null;
  try { return await response.json() as T; } catch { return null; }
}

export default async function AccessPage({
  searchParams
}: {
  searchParams: Promise<{ status?: string }>
}) {
  const manifest = await sessionManifest();
  if (!manifest) redirect("/login");
  if (!manifest.permissions.includes("access.manage")) notFound();
  const [catalog, admin, guest, navigation] = await Promise.all([
    jsonData<string[]>("/api/admin/access/permissions"),
    jsonData<string[]>("/api/admin/access/roles/ADMIN/permissions"),
    jsonData<string[]>("/api/admin/access/roles/GUEST/permissions"),
    jsonData<Nav[]>("/api/admin/access/navigation")
  ]);
  const status = (await searchParams).status;
  if (!catalog || !admin || !guest || !navigation) {
    return <AppShell manifest={manifest}><div className="availability-empty glass-panel">
      <h2>Access configuration unavailable</h2><p>The backend did not return role or navigation settings.</p>
    </div></AppShell>;
  }

  const isGranted = (list: string[], permission: string) => list.includes(permission);

  return (
    <AppShell manifest={manifest}>
      <div className="page-heading">
        <div><div className="eyebrow accent">SECURITY ADMINISTRATION</div>
          <h1>Access <span>management.</span></h1>
          <p>Manage existing role grants and approved sidebar items without changing frontend code.</p></div>
        <span className="status-pill"><ShieldCheck size={15}/> Administrator</span>
      </div>
      {status === "saved" && <div className="feedback-ok" role="status">Configuration saved. Reload other active sessions to refresh their navigation; backend access decisions update immediately.</div>}
      {(status === "failed" || status === "invalid") && <div className="auth-error" role="alert">Configuration was not saved. Check the selected values and try again.</div>}
      <section className="access-section">
        <div className="section-heading"><div><div className="eyebrow accent">DATABASE-DRIVEN RBAC</div>
          <h2>Role permission grants</h2><p>Changes are enforced by Spring Boot even when a user has an existing JWT.</p></div></div>
        <div className="access-roles">
          {([{ code: "ADMIN", grants: admin }, { code: "GUEST", grants: guest }] as const).map(role => (
            <form key={role.code} action={saveRolePermissions} className="access-card glass-panel">
              <div className="access-card-top"><div className="info-icon"><ShieldCheck size={19}/></div>
                <div><h3>{role.code === "ADMIN" ? "Hotel Administrator" : "Hotel Guest"}</h3>
                  <p>{role.grants.length} granted permissions</p></div></div>
              <input type="hidden" name="role" value={role.code}/>
              {role.code === "ADMIN" && <input type="hidden" name="permission" value="access.manage"/>}
              <div className="permission-options">
                {catalog.filter(p => !(role.code === "GUEST" &&
                  (p === "access.manage" || p === "navigation.manage"))).map(permission => {
                  const protectedGrant = role.code === "ADMIN" && permission === "access.manage";
                  return (
                    <label className="permission-option" key={permission}>
                      <input name="permission" type="checkbox" value={permission}
                             defaultChecked={isGranted(role.grants, permission)} disabled={protectedGrant}/>
                      <span>{permission}</span>
                    </label>
                  );
                })}
              </div>
              <button type="submit" className="availability-submit">Save {role.code} grants</button>
            </form>
          ))}
        </div>
      </section>
      {manifest.permissions.includes("navigation.manage") && <section className="access-section">
        <div className="section-heading"><div><div className="eyebrow accent">CONFIGURABLE APP SHELL</div>
          <h2>Sidebar navigation</h2><p>Adjust labels, order and visibility for registered navigation items.</p></div></div>
        <div className="navigation-settings">
          {navigation.map(item => (
            <form action={saveNavigation} key={item.id} className="nav-edit-card glass-panel">
              <div><ListTree size={20}/><strong>{item.id}</strong>
                <small>Route: {item.routeKey} · Required: {item.requiredPermission ?? "none"}</small></div>
              <input type="hidden" name="id" value={item.id}/>
              <label>Label <input name="label" defaultValue={item.label} maxLength={100} required/></label>
              <label>Order <input name="order" type="number" min={0} max={9999} defaultValue={item.order} required/></label>
              <label className="nav-enabled"><input type="checkbox" name="enabled" defaultChecked={item.enabled}/> Visible</label>
              <button type="submit" className="nav-save"><SlidersHorizontal size={16}/> Save</button>
            </form>
          ))}
        </div>
      </section>}
      <div className="notice"><ShieldCheck size={19}/><span>Changing visible sidebar links does not grant backend access. All protected APIs independently enforce database permissions and guest ownership.</span></div>
    </AppShell>
  );
}
