"use client";

import { useEffect, useState, type ReactNode } from "react";
import Link from "next/link";
import { usePathname, useRouter } from "next/navigation";
import { BedDouble, CalendarDays, ChevronLeft, Home, Menu, Moon, ShieldCheck, Sun, Search, LogIn, LogOut, X } from "lucide-react";
import { type NavigationItem, type UiManifest, routeRegistry, visibleNavigation } from "@/lib/navigation";

type Props = { children: ReactNode; manifest: UiManifest };

function NavigationIcon({ item }: { item: NavigationItem }) {
  const icons = { home: Home, rooms: BedDouble, search: Search, rules: CalendarDays };
  const Icon = icons[item.iconKey];
  return <Icon aria-hidden="true" size={18} strokeWidth={1.9} />;
}

export default function AppShell({ children, manifest }: Props) {
  const [collapsed, setCollapsed] = useState(false);
  const [open, setOpen] = useState(false);
  const [dark, setDark] = useState(false);
  const navigation = visibleNavigation(manifest);
  const pathname = usePathname();
  const router = useRouter();
  const signedIn = manifest.mode === "authenticated";

  async function logout() {
    await fetch("/api/session/logout", { method: "POST" });
    router.replace("/login");
    router.refresh();
  }

  useEffect(() => {
    document.documentElement.dataset.theme = dark ? "dark" : "light";
  }, [dark]);

  return (
    <div className={`app-frame ${collapsed ? "sidebar-collapsed" : ""}`}>
      {open && <button type="button" aria-label="Close menu overlay" className="mobile-overlay" onClick={() => setOpen(false)} />}
      <aside className={`sidebar glass-panel ${open ? "sidebar-open" : ""}`} aria-label="Main navigation">
        <div className="brand">
          <div className="brand-mark" aria-hidden="true"><BedDouble size={23} /></div>
          {!collapsed && <div><strong>SmartStay</strong><span>Hotel management</span></div>}
          <button className="icon-btn mobile-close" aria-label="Close menu" type="button" onClick={() => setOpen(false)}><X size={18}/></button>
        </div>
        <div className="sidebar-section-title">{collapsed ? "•••" : "WORKSPACE"}</div>
        <nav className="nav-list">
          {navigation.map((item) => (
            <Link key={item.id} href={routeRegistry[item.routeKey]}
                  className={`nav-item ${routeRegistry[item.routeKey].split("#")[0] === pathname ? "nav-item-active" : ""}`}
                  onClick={() => setOpen(false)}>
              <NavigationIcon item={item} />
              {!collapsed && <span>{item.label}</span>}
            </Link>
          ))}
        </nav>
        <div className="sidebar-foot">
          <div className="security-indicator"><ShieldCheck size={18}/>{!collapsed && <span>{signedIn ? "Authenticated workspace" : "Public preview"}</span>}</div>
          {!collapsed && <p className="muted-small">{signedIn ? "Navigation is filtered by current database permissions." : "Log in to access your personal workspace."}</p>}
          <button type="button" className="sidebar-toggle" onClick={() => setCollapsed(c => !c)} aria-label={collapsed ? "Expand sidebar" : "Collapse sidebar"}>
            <ChevronLeft size={17} className={collapsed ? "rotate-180" : ""}/>
            {!collapsed && "Collapse sidebar"}
          </button>
        </div>
      </aside>
      <div className="main-area">
        <header className="topbar glass-panel">
          <div className="topbar-leading">
            <button type="button" className="icon-btn mobile-menu" aria-label="Open menu" onClick={() => setOpen(true)}><Menu size={20}/></button>
            <div><span className="eyebrow">SMARTSTAY / WORKSPACE</span><div className="topbar-title">Welcome to SmartStay</div></div>
          </div>
          <div className="topbar-actions">
            <span className="preview-chip">{signedIn ? (manifest.roleCodes?.join(", ") ?? "Signed in") : "Public preview"}</span>
            {signedIn ? (
              <button type="button" className="theme-toggle" onClick={logout} aria-label="Sign out" title="Sign out"><LogOut size={18}/></button>
            ) : (
              <Link className="theme-toggle" href="/login" aria-label="Sign in" title="Sign in"><LogIn size={18}/></Link>
            )}
            <button type="button" className="theme-toggle" onClick={() => setDark(value => !value)}
                    aria-label={dark ? "Switch to light theme" : "Switch to dark theme"}>
              {dark ? <Sun size={18}/> : <Moon size={18}/>}
            </button>
            <div className="avatar" title={manifest.displayName} aria-hidden="true">SS</div>
          </div>
        </header>
        <main id="main-content" className="main-content">{children}</main>
        <footer className="footer">SmartStay · Coursework implementation preview · Aqua Frost</footer>
      </div>
    </div>
  );
}
