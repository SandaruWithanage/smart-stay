"use client";

import { useState, type FormEvent } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { ArrowRight, BedDouble, LockKeyhole } from "lucide-react";

export default function LoginPage() {
  const router = useRouter();
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState("");

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (busy) return;
    setBusy(true);
    setError("");
    const fields = new FormData(event.currentTarget);
    try {
      const response = await fetch("/api/session/login", {
        method: "POST", headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          email: String(fields.get("email") ?? ""),
          password: String(fields.get("password") ?? "")
        })
      });
      if (!response.ok) {
        const body = await response.json().catch(() => ({ error: "Login failed" }));
        setError(body.error ?? "Login failed");
        return;
      }
      router.replace("/workspace");
      router.refresh();
    } catch {
      setError("Unable to connect to the application.");
    } finally {
      setBusy(false);
    }
  }

  return (
    <main className="auth-layout">
      <section className="auth-brand">
        <Link href="/" className="auth-logo"><span className="brand-mark"><BedDouble size={23}/></span> SmartStay</Link>
        <div className="auth-brand-body"><span className="eyebrow accent">WELCOME BACK</span>
          <h1>Making every stay <span>feel effortless.</span></h1>
          <p>Modern hotel operations with elegant, permission-aware workspaces.</p>
          <span className="auth-decoration" aria-hidden="true">✧</span>
        </div>
        <p>SmartStay · Academic hotel booking project</p>
      </section>
      <section className="auth-card glass-panel">
        <div className="auth-card-icon"><LockKeyhole size={21}/></div>
        <h2>Sign in</h2>
        <p>Access your private SmartStay workspace.</p>
        <form onSubmit={submit} className="auth-form">
          <label htmlFor="email">Email address</label>
          <input id="email" name="email" autoComplete="email" type="email"
                 maxLength={254} placeholder="name@example.com" required/>
          <label htmlFor="password">Password</label>
          <input id="password" name="password" autoComplete="current-password"
                 type="password" required/>
          {error && <div className="auth-error" role="alert">{error}</div>}
          <button type="submit" disabled={busy} className="availability-submit">
            {busy ? "Signing in..." : "Sign in"} <ArrowRight size={17}/>
          </button>
        </form>
        <p className="auth-switch">New guest? <Link href="/register">Create an account</Link></p>
        <Link className="auth-back" href="/">Back to room catalog</Link>
      </section>
    </main>
  );
}
