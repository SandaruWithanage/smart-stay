"use client";

import { useEffect, useRef, useState, type ReactNode } from "react";
import Link from "next/link";
import { usePathname, useRouter } from "next/navigation";
import { BedDouble, CalendarDays, ChevronLeft, Home, Menu, Moon, ShieldCheck, Sun, Search, LogIn, LogOut, X, Plus } from "lucide-react";
import { type NavigationItem, type UiManifest, routeRegistry, visibleNavigation } from "@/lib/navigation";

type Props = { children: ReactNode; manifest: UiManifest };

function NavigationIcon({ item }: { item: NavigationItem }) {
  const icons = { home: Home, rooms: BedDouble, search: Search, rules: CalendarDays, calendar: CalendarDays, plus: Plus };
  const Icon = icons[item.iconKey];
  return <Icon aria-hidden="true" size={18} strokeWidth={1.9} />;
}

export default function AppShell({ children, manifest }: Props) {
  const [collapsed, setCollapsed] = useState(false);
  const [open, setOpen] = useState(false);
  const [dark, setDark] = useState(false);
  const [themeReady, setThemeReady] = useState(false);
  const menuButtonRef = useRef<HTMLButtonElement>(null);
  const closeButtonRef = useRef<HTMLButtonElement>(null);
  const sidebarRef = useRef<HTMLElement>(null);
  const navigation = visibleNavigation(manifest);
  const pathname = usePathname();
  const router = useRouter();
  const signedIn = manifest.mode === "authenticated";

  async function logout() {
    await fetch("/api/session/logout", { method: "POST" });
    router.replace("/login");
    router.refresh();
  }

  // Remember only the visual preference, never authentication or profile data.
  // Apply it after hydration to avoid server/client markup mismatches.
  useEffect(() => {
    let preference = window.matchMedia("(prefers-color-scheme: dark)").matches;
    try {
      const saved = window.localStorage.getItem("smartstay-theme");
      if (saved === "dark" || saved === "light") preference = saved === "dark";
    } catch { /* Theme still works if browser storage is unavailable. */ }
    setDark(preference);
    setThemeReady(true);
  }, []);

  useEffect(() => {
    if (!themeReady) return;
    document.documentElement.dataset.theme = dark ? "dark" : "light";
    try {
      window.localStorage.setItem("smartstay-theme", dark ? "dark" : "light");
    } catch { /* A blocked localStorage should not prevent theme switching. */ }
  }, [dark, themeReady]);

  useEffect(() => {
    const sync = (event: StorageEvent) => {
      if (event.key === "smartstay-theme" &&
          (event.newValue === "dark" || event.newValue === "light")) {
        setDark(event.newValue === "dark");
      }
    };
    window.addEventListener("storage", sync);
    return () => window.removeEventListener("storage", sync);
  }, []);

  // Keyboard users should not be trapped behind a mobile navigation drawer.
  // When opened, focus enters the drawer; Escape closes it and Tab stays inside.
  useEffect(() => {
    if (!open) return;
    closeButtonRef.current?.focus();
    const keyDown = (event: KeyboardEvent) => {
      if (event.key === "Escape") {
        event.preventDefault();
        setOpen(false);
        return;
      }
      if (event.key !== "Tab") return;
      const focusable = Array.from(sidebarRef.current?.querySelectorAll<HTMLElement>(
        'a[href], button:not(:disabled), input:not(:disabled), [tabindex]:not([tabindex="-1"])'
      ) ?? []).filter(el => el.getClientRects().length > 0);
      if (!focusable.length) return;
      const first = focusable[0];
      const last = focusable[focusable.length - 1];
      if (event.shiftKey && document.activeElement === first) {
        event.preventDefault();
        last.focus();
      } else if (!event.shiftKey && document.activeElement === last) {
        event.preventDefault();
        first.focus();
      }
    };
    document.addEventListener("keydown", keyDown);
    return () => {
      document.removeEventListener("keydown", keyDown);
      menuButtonRef.current?.focus();
    };
  }, [open]);

  return (
    <div className={`app-frame ${collapsed ? "sidebar-collapsed" : ""}`}>
      <a className="ss-skip-link" href="#main-content">Skip to main content</a>
      {open && <button type="button" aria-label="Close menu overlay" className="mobile-overlay" onClick={() => setOpen(false)} />}
      <aside id="smartstay-sidebar" ref={sidebarRef} className={`sidebar glass-panel ${open ? "sidebar-open" : ""}`}
        aria-label="Main navigation" role={open ? "dialog" : undefined}
        aria-modal={open ? true : undefined}>
        <div className="brand">
          <div className="brand-mark" aria-hidden="true"><BedDouble size={23} /></div>
          {!collapsed && <div><strong>SmartStay</strong><span>Hotel management</span></div>}
          <button ref={closeButtonRef} className="icon-btn mobile-close" aria-label="Close menu" type="button" onClick={() => setOpen(false)}><X size={18}/></button>
        </div>
        <div className="sidebar-section-title">{collapsed ? "•••" : "WORKSPACE"}</div>
        <nav className="nav-list">
          {navigation.map((item) => (
            <Link key={item.id} href={routeRegistry[item.routeKey]}
                  className={`nav-item ${routeRegistry[item.routeKey].split("#")[0] === pathname ? "nav-item-active" : ""}`} aria-current={routeRegistry[item.routeKey].split("#")[0] === pathname ? "page" : undefined}
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
            <button ref={menuButtonRef} type="button" className="icon-btn mobile-menu" aria-label="Open menu" aria-controls="smartstay-sidebar" aria-expanded={open} onClick={() => setOpen(true)}><Menu size={20}/></button>
            <div><span className="eyebrow">SMARTSTAY / WORKSPACE</span><div className="topbar-title"><span className="topbar-title-full">Welcome to SmartStay</span><span className="topbar-title-compact">SmartStay</span></div></div>
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
        <main id="main-content" tabIndex={-1} className="main-content">{children}</main>
        <footer className="footer">SmartStay · Hotel booking coursework · Aqua Frost / Spatial Glass</footer>
      </div>
    </div>
  );
}
