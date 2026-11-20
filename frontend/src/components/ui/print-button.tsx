"use client";

import { Printer } from "lucide-react";

export function PrintButton() {
  return <button type="button" onClick={() => window.print()} className="ss-btn ss-btn-secondary ss-print-hidden">
    <Printer size={17}/> Print / Save PDF
  </button>;
}
