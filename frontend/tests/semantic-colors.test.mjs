import assert from "node:assert/strict";
import { readFileSync } from "node:fs";
import { test } from "node:test";

const css=readFileSync(new URL("../src/app/globals.css",import.meta.url),"utf8");
const ui=readFileSync(new URL("../src/components/ui/hotel-ui.tsx",import.meta.url),"utf8");
const controls=readFileSync(new URL("../src/components/ui/confirm-submit.tsx",import.meta.url),"utf8");

function luminance(hex) {
  const rgb=[1,3,5].map(i=>parseInt(hex.slice(i,i+2),16)/255)
    .map(n=>n<=0.04045?n/12.92:((n+0.055)/1.055)**2.4);
  return 0.2126*rgb[0]+0.7152*rgb[1]+0.0722*rgb[2];
}
function contrast(fg,bg) {
  const [a,b]=[luminance(fg),luminance(bg)].sort((a,b)=>b-a);
  return (a+0.05)/(b+0.05);
}

test("Approve/reject buttons preserve WCAG AA contrast in Aqua Frost and Spatial Glass",()=>{
  assert.ok(contrast("#047857","#ffffff")>=4.5);
  assert.ok(contrast("#be123c","#ffffff")>=4.5);
  assert.ok(contrast("#34d399","#10131a")>=4.5);
  assert.ok(contrast("#fda4af","#10131a")>=4.5);
});

test("Semantic tokens exist and both themes have positive/negative variants",()=>{
  for(const token of ["--ss-positive","--ss-positive-hover","--ss-positive-contrast",
    "--ss-negative","--ss-negative-hover","--ss-negative-contrast",
    "--ss-positive-soft","--ss-negative-soft"]) {
    assert.ok(css.split(token).length>=3,token+" missing from a theme");
  }
  for(const cls of [".ss-btn-success",".ss-btn-danger",".ss-badge-success",
    ".ss-badge-danger",".ss-feedback-danger"]) assert.ok(css.includes(cls));
});

test("Actions and statuses use shared semantic components rather than custom colors",()=>{
  assert.match(controls,/ss-btn-\$\{tone\}/);
  assert.match(ui,/APPROVED/);
  assert.match(ui,/REJECTED/);
  assert.match(ui,/CircleX/);
  assert.match(ui,/ss-feedback-\$\{message.tone\}/);
});
