"use client";

import { useState, type FormEvent } from "react";
import { useRouter } from "next/navigation";
import Link from "next/link";
import { ArrowRight, BedDouble, UserRoundPlus } from "lucide-react";

export default function RegisterPage() {
  const router = useRouter();
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState("");

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (busy) return;
    setBusy(true);
    setError("");
    const form = new FormData(event.currentTarget);
    const fields = Object.fromEntries(form.entries());
    try {
      const response = await fetch("/api/session/register", {
        method: "POST", headers: { "Content-Type": "application/json" },
        body: JSON.stringify(fields)
      });
      if (!response.ok) {
        const body = await response.json().catch(() => ({ error: "Registration unsuccessful" }));
        setError(body.error ?? "Registration unsuccessful");
        return;
      }
      router.replace("/workspace");
      router.refresh();
    } catch {
      setError("Unable to connect to the registration service.");
    } finally {
      setBusy(false);
    }
  }

  return (
    <main className="auth-layout">
      <section className="auth-brand">
        <Link href="/" className="auth-logo"><span className="brand-mark"><BedDouble size={23}/></span> SmartStay</Link>
        <div className="auth-brand-body"><span className="eyebrow accent">SMARTSTAY GUEST ACCESS</span>
          <h1>Your stay starts <span>right here.</span></h1>
          <p>Register as a guest to access your own records and future reservations.</p>
          <span className="auth-decoration" aria-hidden="true">✧</span>
        </div>
        <p>No administrator roles are available through public registration.</p>
      </section>
      <section className="auth-card glass-panel">
        <div className="auth-card-icon"><UserRoundPlus size={21}/></div>
        <h2>Create guest account</h2>
        <p>Use a strong password. Identity details must match your NIC or passport.</p>
        <form onSubmit={submit} className="auth-form">
          <label htmlFor="fullName">Full name</label>
          <input id="fullName" name="fullName" maxLength={200} required autoComplete="name"/>
          <label htmlFor="documentNumber">NIC or passport number</label>
          <input id="documentNumber" name="documentNumber" maxLength={64} required autoComplete="off"/>
          <label htmlFor="phone">Contact number</label>
          <input id="phone" name="phone" maxLength={32} required autoComplete="tel"/>
          <label htmlFor="email">Email address</label>
          <input id="email" name="email" type="email" maxLength={254} required autoComplete="email"/>
          <label htmlFor="password">Password (at least 12 characters)</label>
          <input id="password" name="password" type="password" minLength={12} maxLength={128}
                 autoComplete="new-password" required/>
          {error && <div className="auth-error" role="alert">{error}</div>}
          <button type="submit" disabled={busy} className="availability-submit">
            {busy ? "Creating account..." : "Create account"} <ArrowRight size={17}/>
          </button>
        </form>
        <p className="auth-switch">Have an account? <Link href="/login">Sign in</Link></p>
        <Link className="auth-back" href="/">Back to room catalog</Link>
      </section>
    </main>
  );
}
