"use client";

import { useFormStatus } from "react-dom";

type ButtonTone = "primary" | "success" | "danger";

export function SubmitButton({ children, className, tone = "primary" }: {
  children: React.ReactNode; className?:string; tone?:ButtonTone
}) {
  const { pending } = useFormStatus();
  const style = className ?? `ss-btn ss-btn-${tone}`;
  return <button type="submit" className={style} disabled={pending} aria-busy={pending}>
    {pending ? "Saving…" : children}
  </button>;
}

/** Destructive operations always require a second intentional confirmation. */
export function ConfirmSubmit({ children, message, className, tone = "danger" }: {
  children:React.ReactNode; message:string; className?:string; tone?:ButtonTone
}) {
  const { pending } = useFormStatus();
  const style = className ?? `ss-btn ss-btn-${tone}`;
  return <button className={style} type="submit" disabled={pending} aria-busy={pending}
    onClick={event => { if (!window.confirm(message)) event.preventDefault(); }}>
    {pending?"Working…":children}
  </button>;
}
