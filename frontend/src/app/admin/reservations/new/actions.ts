"use server";

import { redirect } from "next/navigation";
import { revalidatePath } from "next/cache";
import { backendForUser, sessionManifest } from "@/lib/server-session";
import type { BookingReceipt, RoomView } from "@/lib/hotel";

function error(status:string):never { redirect(`/admin/reservations/new?status=${status}`); }
function validDate(s:string) {return /^\d{4}-\d{2}-\d{2}$/.test(s) && !Number.isNaN(Date.parse(s));}

export async function createDeskBooking(data: FormData) {
  const manifest=await sessionManifest();
  if (!manifest) redirect("/login");
  if (!manifest.permissions.includes("reservation.create.any")) redirect("/workspace");
  const roomId=String(data.get("roomId")??"");
  const checkIn=String(data.get("checkIn")??"");
  const checkOut=String(data.get("checkOut")??"");
  const guestCount=Number(data.get("guestCount")??0);
  if (!roomId || !validDate(checkIn)||!validDate(checkOut)||checkOut<=checkIn||
    !Number.isInteger(guestCount)||guestCount<1||guestCount>50||
    data.get("advanceReceived")!=="on") error("invalid");
  const roomsRes=await backendForUser("/api/admin/rooms");
  if (!roomsRes?.ok) error("denied");
  const rooms=await roomsRes.json() as RoomView[];
  const room=rooms.find(r=>r.id===roomId && r.active && r.operationalStatus==="ACTIVE");
  if (!room) error("conflict");
  let guestId=String(data.get("guestId")??"").trim();
  if (!guestId) {
    if (!manifest.permissions.includes("guest.create")) error("denied");
    const fullName=String(data.get("fullName")??"").trim();
    const documentNumber=String(data.get("documentNumber")??"").trim();
    const contactNumber=String(data.get("contactNumber")??"").trim();
    const email=String(data.get("email")??"").trim();
    if (!fullName||!documentNumber||!contactNumber||!email.includes("@")) error("invalid");
    const registered=await backendForUser("/api/guests",{
      method:"POST", headers:{"Content-Type":"application/json"},
      body:JSON.stringify({fullName, documentNumber, contactNumber, email})
    });
    if (registered?.status===409) error("conflict");
    if (!registered?.ok) error("error");
    const guest=await registered.json() as {id?:string};
    if (!guest.id) error("error");
    guestId=guest.id;
  }
  if (!/^[0-9a-f-]{36}$/i.test(guestId)) error("invalid");
  const booked=await backendForUser("/api/reservations",{
    method:"POST",headers:{"Content-Type":"application/json"},
    body:JSON.stringify({guestId,roomId,roomType:room.roomType,checkIn,checkOut,guestCount,advanceReceived:true})
  });
  if (!booked) error("error");
  if (booked.status===409||booked.status===400) error("conflict");
  if (booked.status===403) error("denied");
  if (!booked.ok) error("error");
  const receipt=await booked.json() as BookingReceipt;
  const id=receipt.reservation?.bookingId;
  if (!id) error("error");
  revalidatePath("/admin/reservations");
  revalidatePath("/my/reservations");
  redirect(`/reservations/${id}/invoice`);
}
