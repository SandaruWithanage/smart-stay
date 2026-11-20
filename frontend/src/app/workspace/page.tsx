import Link from "next/link";
import { redirect } from "next/navigation";
import { BadgeCheck, CalendarDays, KeyRound, ShieldCheck, UserRound } from "lucide-react";
import AppShell from "@/components/app-shell";
import { sessionManifest } from "@/lib/server-session";

export default async function WorkspacePage() {
  const manifest = await sessionManifest();
  if (!manifest) redirect("/login");
  const canManage = manifest.permissions.includes("access.manage");

  return (
    <AppShell manifest={manifest}>
      <div className="page-heading">
        <div>
          <div className="eyebrow accent">AUTHENTICATED WORKSPACE</div>
          <h1>Welcome back, <span>{manifest.displayName}.</span></h1>
          <p>Your navigation and actions come from the current PostgreSQL role configuration.</p>
        </div>
        <span className="status-pill"><span className="dot"/> Signed in</span>
      </div>
      <section className="workspace-grid">
        <article className="workspace-tile glass-panel">
          <div className="info-icon"><UserRound size={21}/></div>
          <div className="eyebrow accent">CURRENT ACCOUNT</div>
          <h2>{manifest.displayName}</h2>
          <p>Role: {manifest.roleCodes?.join(", ") || "No assigned role"}</p>
        </article>
        <article className="workspace-tile glass-panel">
          <div className="info-icon"><KeyRound size={21}/></div>
          <div className="eyebrow accent">LIVE PERMISSION GRANTS</div>
          <h2>{manifest.permissions.length}</h2>
          <p>Loaded from the database for this session request.</p>
        </article>
        <article className="workspace-tile glass-panel">
          <div className="info-icon"><CalendarDays size={21}/></div>
          <div className="eyebrow accent">BOOKING ACCESS</div>
          <h2>{manifest.permissions.includes("reservation.create.any") ? "Desk operator" : "Guest account"}</h2>
          <p>Advance-payment booking confirmations are restricted to authorized staff.</p>
        </article>
      </section>
      <div className="workspace-info glass-panel">
        <div><ShieldCheck size={23}/></div>
        <div>
          <h3>Permission-based access is enabled</h3>
          <p>This workspace receives a filtered navigation manifest. The backend independently checks access to each protected guest, reservation, invoice and administrator operation.</p>
          {canManage && <Link className="primary-button" href="/admin/access">Manage roles and navigation <BadgeCheck size={16}/></Link>}
          {!canManage && <p>Guest reservations and invoice views are the next interface development milestone.</p>}
        </div>
      </div>
    </AppShell>
  );
}
