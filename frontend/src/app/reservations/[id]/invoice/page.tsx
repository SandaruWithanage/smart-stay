import Link from "next/link";
import { notFound, redirect } from "next/navigation";
import { BedDouble, CalendarDays, CircleAlert, FileText, ShieldCheck } from "lucide-react";
import AppShell from "@/components/app-shell";
import { InvoiceBreakdown } from "@/components/ui/invoice-breakdown";
import { PrintButton } from "@/components/ui/print-button";
import { Feedback, PageHeader, StatusBadge, Surface } from "@/components/ui/hotel-ui";
import { displayDate, lkr, roomName, type InvoiceView } from "@/lib/hotel";
import { authenticatedJson } from "@/lib/server-hotel";
import { sessionManifest } from "@/lib/server-session";

export default async function InvoicePage({params,searchParams}:{params:Promise<{id:string}>;searchParams:Promise<{updated?:string}>}) {
  const manifest = await sessionManifest();
  if (!manifest) redirect("/login");
  if (!manifest.permissions.some(p=>p==="invoice.read.any"||p==="invoice.read.own")) notFound();
  const {id}=await params;
  if (!/^[0-9a-f-]{36}$/i.test(id)) notFound();
  const [invoice,query]=await Promise.all([authenticatedJson<InvoiceView>(`/api/reservations/${id}/invoice`),searchParams]);
  if (!invoice) notFound();
  return <AppShell manifest={manifest}>
    <div className="ss-print-hidden">
      <PageHeader kicker="BILLING / OFFICIAL GUEST FOLIO" title="Guest invoice"
        description="Itemized room charges, discount, tax, and the recorded LKR 10,000 advance."
        action={<PrintButton/>}/>
    </div>
    {query.updated==="1" && <div className="ss-print-hidden"><Feedback status="saved"/></div>}
    <div className="ss-invoice-paper">
      <header className="ss-invoice-heading">
        <div className="ss-invoice-brand"><div className="ss-invoice-logo"><BedDouble size={24}/></div>
          <div><strong>SmartStay</strong><small>SRI LANKA · HOTEL BOOKING SYSTEM</small></div>
        </div>
        <div className="ss-invoice-reference"><span>INVOICE / TAX FOLIO</span>
          <strong>{invoice.invoiceId.slice(0,8).toUpperCase()}</strong>
          <small>Booking {invoice.reservationId.slice(0,8).toUpperCase()}</small>
          <StatusBadge value={invoice.voided?"VOIDED":"CONFIRMED"}/>
        </div>
      </header>
      {invoice.voided && <div className="ss-invoice-void" role="status"><CircleAlert size={18}/>
        This invoice was voided following cancellation. It is retained for historical reference only.</div>}
      <div className="ss-invoice-meta-grid">
        <section><h2>Guest details</h2><strong>{invoice.guestName}</strong>
          <span>Reservation reference: {invoice.reservationId}</span></section>
        <section><h2>Stay details</h2><strong>{roomName(invoice.roomType)} · {invoice.roomId}</strong>
          <span>{invoice.bedType} bed · {invoice.guests} guest(s)</span></section>
        <section><h2>Arrival</h2><strong>{displayDate(invoice.checkIn)}</strong>
          <span>Check-in date</span></section>
        <section><h2>Departure</h2><strong>{displayDate(invoice.checkOut)}</strong>
          <span>{invoice.nights} night(s) booked</span></section>
      </div>
      <section className="ss-invoice-charges">
        <h2>Itemized billing ledger</h2>
        <div className="ss-invoice-rate"><span><FileText size={16}/> Room rate per night</span>
          <strong>{lkr(invoice.dailyRate)}</strong></div>
        <InvoiceBreakdown invoice={invoice}/>
      </section>
      <footer className="ss-invoice-footer">
        <div><ShieldCheck size={19}/><span>Calculated in the Java backend from the booked package and preserved as an invoice snapshot.</span></div>
        <p>This coursework invoice is not proof of electronic payment or an issued tax receipt. The recorded advance is entered by authorized front desk staff; refunds require separate settlement.</p>
      </footer>
    </div>
    <div className="ss-invoice-nav ss-print-hidden">
      <Link href={manifest.permissions.includes("reservation.read.any")?"/admin/reservations":"/my/reservations"} className="ss-btn ss-btn-secondary">Back to reservations</Link>
    </div>
  </AppShell>;
}
