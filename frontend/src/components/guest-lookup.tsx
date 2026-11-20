"use client";

import { useState } from "react";
import { Search, X } from "lucide-react";

type Guest = { id:string; fullName:string; email?:string };

export function GuestLookup() {
  const [document,setDocument]=useState("");
  const [guest,setGuest]=useState<Guest | null>(null);
  const [busy,setBusy]=useState(false);
  const [error,setError]=useState("");

  async function findGuest() {
    if (!document.trim() || busy) return;
    setBusy(true); setError(""); setGuest(null);
    try {
      const response=await fetch("/api/desk/guest-lookup",{
        method:"POST",headers:{"Content-Type":"application/json"},
        body:JSON.stringify({documentNumber:document.trim()})
      });
      const body=await response.json();
      if (!response.ok) {setError(body.error??"Guest not found");return;}
      if (typeof body.id!=="string" || typeof body.fullName!=="string") {
        setError("Invalid guest response");return;
      }
      setGuest({id:body.id,fullName:body.fullName,email:body.email});
    } catch {setError("Guest lookup service unavailable");}
    finally {setBusy(false);}
  }
  return <div className="ss-guest-lookup">
    <div className="ss-field">
      <label htmlFor="lookupDocument">Find an existing guest by NIC/passport</label>
      <div className="ss-lookup-row">
        <input id="lookupDocument" type="text" value={document}
          onChange={event=>{setDocument(event.target.value);setGuest(null);}}
          placeholder="Enter NIC or passport to search" maxLength={64} autoComplete="off"/>
        <button type="button" className="ss-btn ss-btn-secondary" onClick={findGuest} disabled={busy}>
          <Search size={16}/>{busy?"Searching…":"Find guest"}
        </button>
      </div>
      {error && <span className="ss-lookup-error" role="alert">{error}</span>}
    </div>
    {guest && <div className="ss-lookup-result">
      <div><strong>{guest.fullName}</strong><small>{guest.email ?? "Existing guest record"} · {guest.id}</small></div>
      <button type="button" className="ss-action-link" onClick={()=>{setGuest(null);setDocument("");}} aria-label="Clear selected guest"><X size={16}/></button>
    </div>}
    <input type="hidden" name="guestId" value={guest?.id ?? ""} readOnly/>
    <p className="ss-lookup-hint">If you selected an existing guest, the new guest fields below will be ignored. Otherwise, complete those details to create a new desk guest record.</p>
  </div>;
}
