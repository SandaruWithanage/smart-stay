import type { Metadata } from "next";
import "./globals.css";

export const metadata: Metadata = {
  title: "SmartStay | Hotel Booking System",
  description: "SmartStay hotel booking and management coursework application"
};

export default function RootLayout({ children }: Readonly<{ children: React.ReactNode }>) {
  return <html lang="en"><body>{children}</body></html>;
}
