import Link from "next/link";
import { ArrowUpRight, BadgeCheck, CalendarCheck2, CheckCircle2, Clock3, ShieldCheck, Sparkles } from "lucide-react";
import AppShell from "@/components/app-shell";
import { getPublicCatalog } from "@/lib/catalog";
import { publicPreviewManifest } from "@/lib/navigation";
import { sessionManifest } from "@/lib/server-session";

const friendlyName: Record<string, string> = {
  STANDARD: "Standard Room",
  DELUXE: "Deluxe Room",
  SUITE: "Suite",
  FAMILY_SUITE: "Family Suite"
};

export default async function HomePage() {
  const [{ packages, source }, current] = await Promise.all([getPublicCatalog(), sessionManifest()]);
  const money = (n: number) => new Intl.NumberFormat("en-LK", { maximumFractionDigits: 0 }).format(n);

  return (
    <AppShell manifest={current ?? publicPreviewManifest}>
      <div className="page-heading">
        <div><div className="eyebrow accent">HOTEL MANAGEMENT · ACADEMIC PROJECT</div>
          <h1>Thoughtful stays, <span>beautifully managed.</span></h1>
          <p>A secure workspace for discovering stays, managing reservations, and reviewing billing.</p></div>
        <span className="status-pill"><span className="dot"/> Hotel booking platform</span>
      </div>

      <section className="hero glass-panel">
        <div className="hero-copy">
          <span className="hero-kicker"><Sparkles size={15}/> THE AQUA FROST EXPERIENCE</span>
          <h2>Hotel operations, without the clutter.</h2>
          <p>A modern booking system built on Java business rules. Discover room packages, request a stay, and manage hotel operations through permission-based workspaces.</p>
          <Link className="primary-button" href="#packages">Explore room packages <ArrowUpRight size={17}/></Link>
        </div>
        <div className="hero-art" aria-hidden="true">
          <div className="art-orbit outer"/><div className="art-orbit inner"/>
          <div className="art-hotel">S<span>✦</span></div>
          <div className="art-floating art-one">Aqua Frost <CheckCircle2 size={16}/></div>
          <div className="art-floating art-two">SmartStay <BadgeCheck size={16}/></div>
        </div>
      </section>

      <div className="mini-grid" id="rules">
        <div className="info-card glass-panel"><div className="info-icon"><CalendarCheck2 size={20}/></div><strong>48-hour booking notice</strong><p>Provisional interpretation pending lecturer confirmation.</p></div>
        <div className="info-card glass-panel"><div className="info-icon"><Clock3 size={20}/></div><strong>24-hour cancellation cutoff</strong><p>Business rule enforced in the Java domain service.</p></div>
        <div className="info-card glass-panel"><div className="info-icon"><ShieldCheck size={20}/></div><strong>Secure-by-default APIs</strong><p>JWT authentication and database permissions protect every privileged API.</p></div>
      </div>

      <section className="packages-section" id="packages">
        <div className="section-heading"><div><div className="eyebrow accent">THE FOUR OFFICIAL PACKAGES</div><h2>Room collections</h2><p>Exact room rates, tax rates and extra-guest charges from the coursework brief.</p></div><span className={`source-label ${source === "sample" ? "source-sample" : ""}`}>{source === "api" ? "From Spring Boot API" : "Sample rates · connect backend"}</span></div>
        <div className="packages-grid">
          {packages.map((room, index) => (
            <article className="package-card glass-panel" key={room.roomType}>
              <div className={`package-visual tone-${index + 1}`}><span className="package-index">0{index + 1}</span><div className="package-symbol" aria-hidden="true">✦</div></div>
              <div className="package-body"><span className="package-category">ROOM COLLECTION</span><h3>{friendlyName[room.roomType]}</h3>
                <p className="package-rate">LKR {money(room.dailyRate)} <span>/ night</span></p>
                <div className="package-divider"/>
                <div className="package-detail"><CheckCircle2 size={16}/> {room.breakfastIncluded ? "Complimentary breakfast" : "Breakfast not included"}</div>
                <div className="package-detail"><CheckCircle2 size={16}/> Extra guest: LKR {money(room.extraGuestRate)}</div>
                <div className="package-detail"><CheckCircle2 size={16}/> Tax: {(room.taxRate * 100).toFixed(0)}%</div>
              </div>
            </article>
          ))}
        </div>
      </section>
      <div className="notice"><ShieldCheck size={19}/><span>Guest accounts, request review and invoices are available in the authenticated workspace. Package cards show catalog rates, not live availability; search specific dates to see bookable rooms.</span></div>
    </AppShell>
  );
}
