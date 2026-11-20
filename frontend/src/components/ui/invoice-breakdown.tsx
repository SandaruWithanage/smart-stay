import { lkr, type InvoiceView } from "@/lib/hotel";

const lineItems = (invoice: InvoiceView) => [
  ["Base room charge", invoice.baseCharge],
  ["Additional guests", invoice.extraCharge],
  ["Long-stay discount", -invoice.discount],
  ["Applicable tax", invoice.tax]
] as const;

export function InvoiceBreakdown({ invoice }: { invoice:InvoiceView }) {
  return <div className="ss-invoice-breakdown">
    <div className="ss-bill-items">
      {lineItems(invoice).map(([label,value])=><div key={label} className="ss-bill-row">
        <span>{label}</span><strong className="ss-tabular">{value<0?"− "+lkr(-value):lkr(value)}</strong>
      </div>)}
    </div>
    <div className="ss-bill-row ss-bill-subtotal"><span>Gross total</span><strong>{lkr(invoice.grossTotal)}</strong></div>
    <div className="ss-bill-row"><span>Advance applied</span><strong>− {lkr(invoice.advanceApplied)}</strong></div>
    <div className="ss-bill-total"><span>Remaining payable</span><strong>{lkr(invoice.balanceDue)}</strong></div>
    {invoice.refundDue>0 && <div className="ss-bill-row"><span>Excess advance to reconcile</span><strong>{lkr(invoice.refundDue)}</strong></div>}
  </div>;
}
