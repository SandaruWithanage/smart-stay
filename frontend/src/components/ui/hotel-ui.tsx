import type { ReactNode } from "react";
import Link from "next/link";
import { AlertCircle, ArrowUpRight, CircleCheck, Inbox, Wrench } from "lucide-react";

type SurfaceProps = { children: ReactNode; className?: string; solid?: boolean; id?: string };
export function Surface({ children, className = "", solid = false, id }: SurfaceProps) {
  return <section id={id} className={`ss-surface ${solid ? "ss-solid" : "glass-panel"} ${className}`}>{children}</section>;
}

export function PageHeader({ kicker, title, description, action }: {
  kicker: string; title: string; description?: string; action?: ReactNode
}) {
  return <header className="ss-page-header">
    <div><p className="ss-kicker">{kicker}</p><h1>{title}</h1>
      {description && <p className="ss-page-description">{description}</p>}</div>
    {action && <div className="ss-page-actions">{action}</div>}
  </header>;
}

export function SectionHeader({ title, subtitle, action }: {
  title: string; subtitle?: string; action?: ReactNode
}) {
  return <div className="ss-section-header"><div><h2>{title}</h2>{subtitle && <p>{subtitle}</p>}</div>{action}</div>;
}

export function PrimaryLink({ href, children }: { href:string; children:ReactNode }) {
  return <Link href={href} className="ss-btn ss-btn-primary">{children}<ArrowUpRight size={15}/></Link>;
}

export function MetricCard({ label, value, hint, icon }: {
  label:string; value:ReactNode; hint?:string; icon?:ReactNode
}) {
  return <Surface className="ss-metric-card">
    <div className="ss-metric-top"><span>{label}</span>{icon}</div>
    <div className="ss-metric-value">{value}</div>
    {hint && <p>{hint}</p>}
  </Surface>;
}

export type BadgeTone = "success" | "warning" | "danger" | "neutral";
export function StatusBadge({ value, tone }: { value:string; tone?:BadgeTone }) {
  const color = tone ?? (
    ["ACTIVE","AVAILABLE","CONFIRMED","SETTLED"].includes(value) ? "success"
      : ["MAINTENANCE","CANCELLED","VOIDED"].includes(value) ? "danger"
      : ["PENDING","RESERVED"].includes(value) ? "warning" : "neutral");
  const label = value.replaceAll("_"," ").toLowerCase().replace(/\b\w/g, c => c.toUpperCase());
  return <span className={`ss-badge ss-badge-${color}`}>
    {color === "success" ? <CircleCheck size={13}/> :
      color === "danger" ? <Wrench size={13}/> : <span className="ss-badge-dot"/>}
    {label}
  </span>;
}

export function EmptyState({ title, description, action }: {
  title:string; description:string; action?:ReactNode
}) {
  return <Surface solid className="ss-empty">
    <Inbox size={28} aria-hidden="true"/><h3>{title}</h3><p>{description}</p>{action}
  </Surface>;
}

export function Feedback({ status }: { status?:string }) {
  if (!status) return null;
  const good = ["saved","created","cancelled","retired"].includes(status);
  const descriptions:Record<string,string> = {
    saved:"Changes saved successfully.", created:"Record created successfully.",
    cancelled:"Reservation cancelled. Any refundable advance is pending settlement.",
    retired:"Room was safely retired.",
    error:"The operation was not completed. Please review your data and try again.",
    invalid:"Invalid or incomplete form values.", conflict:"A record conflict or booking restriction prevented the change.",
    denied:"Permission denied."
  };
  return <div role="status" className={`ss-feedback ${good?"ss-feedback-success":"ss-feedback-error"}`}>
    {good?<CircleCheck size={17}/>:<AlertCircle size={17}/>}
    {descriptions[status] ?? "Operation could not be completed."}
  </div>;
}

export function FormField({ label, name, children, hint, required=false }: {
  label:string; name:string; children:ReactNode; hint?:string; required?:boolean
}) {
  return <div className="ss-field"><label htmlFor={name}>{label}{required && <span aria-hidden="true"> *</span>}</label>
    {children}{hint && <small>{hint}</small>}</div>;
}

export type Column<T> = {
  key:string; label:string; render:(row:T)=>ReactNode;
  align?:"left"|"right"|"center";
};
export function DataTable<T>({ rows, columns, rowKey }: {
  rows:T[]; columns:Column<T>[]; rowKey:(row:T)=>string
}) {
  return <div className="ss-table-wrap"><table className="ss-table">
    <thead><tr>{columns.map(c=><th key={c.key} scope="col" className={c.align==="right"?"ss-align-right":""}>{c.label}</th>)}</tr></thead>
    <tbody>{rows.map(row=><tr key={rowKey(row)}>{columns.map(col=>
      <td key={col.key} className={col.align==="right"?"ss-align-right":""}>{col.render(row)}</td>)}</tr>)}</tbody>
  </table></div>;
}
