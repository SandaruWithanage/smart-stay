"use client";

import { useFormStatus } from "react-dom";

export function SubmitButton({ children, className="ss-btn ss-btn-primary" }: {
  children: React.ReactNode; className?:string
}) {
  const { pending } = useFormStatus();
  return <button type="submit" className={className} disabled={pending} aria-busy={pending}>
    {pending ? "Saving…" : children}
  </button>;
}

export function ConfirmSubmit({ children, message, className="ss-btn ss-btn-danger" }: {
  children:React.ReactNode; message:string; className?:string
}) {
  const { pending } = useFormStatus();
  return <button className={className} type="submit" disabled={pending}
    onClick={event => { if (!window.confirm(message)) event.preventDefault(); }}>
    {pending?"Working…":children}
  </button>;
}
