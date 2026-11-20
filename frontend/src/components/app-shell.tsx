"use client";

import { useEffect, useState, type ReactNode } from "react";
import Link from "next/link";
import { BedDouble, CalendarDays, ChevronLeft, Home, Menu, Moon, ShieldCheck, Sun, X } from "lucide-react";
import { type NavigationItem, type UiManifest, routeRegistry, visibleNavigation } from "@/lib/navigation";

type Props = { children: ReactNode; manifest: UiManifest };

function NavigationIcon({ item }: { item: NavigationItem }) {
  const icons = { home: Home, rooms: BedDouble, rules: CalendarDays };
  const Icon = icons[item.iconKey];
  return <Icon aria-hidden="true" size={18} strokeWidth={1.9} />;
}

export default function AppShell({ children, manifest }: Props) {
  const [collapsed, setCollapsed] = useState(false);
  const [open, setOpen] = useState(false);
  const [dark, setDark] = useState(false);
  const navigation = visibleNavigation(manifest);

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
                  className={`nav-item ${item.routeKey === "overview" ? "nav-item-active" : ""}`}
                  onClick={() => setOpen(false)}>
              <NavigationIcon item={item} />
              {!collapsed && <span>{item.label}</span>}
            </Link>
          ))}
        </nav>
        <div className="sidebar-foot">
          <div className="security-indicator"><ShieldCheck size={18}/>{!collapsed && <span>Public preview only</span>}</div>
          {!collapsed && <p className="muted-small">Authenticated RBAC/ABAC manifest arrives in a later milestone.</p>}
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
            <span className="preview-chip">Development preview</span>
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
