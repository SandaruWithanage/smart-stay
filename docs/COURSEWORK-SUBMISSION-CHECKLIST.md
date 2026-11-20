# CI6115 SmartStay submission and hand-in checklist

**Basis:** User-supplied 10-page **draft** CI6115 Programming III CW1 assessment brief, and SmartStay SRS. Confirm the final moderated hand-in instructions with module staff. No submission date was provided in the draft. **Do not interpret this checklist as lecturer approval of provisional business assumptions.**

## Required artifacts

- [ ] **One consolidated report/document** containing class diagram, design discussion, complete source code, and test cases. The draft brief explicitly requires these three parts in a single document; it does not say the SRS can replace them.
- [ ] **Readable UML class diagram image(s).** Replace `KINGSTON_ID_` placeholder on **every diagram class name** with your actual Kingston University student ID. The brief warns marks can be deducted otherwise.
- [ ] **Class and object design rationale** covering fields, methods, associations, encapsulation and OOP decisions (see `COURSEWORK-CLASS-DIAGRAM.md`).
- [ ] **Full source code listings** of the proposed application in the report or as permitted by the final assessment instructions. The uploaded draft asks for complete source code, with classes/objects, OOP, data structures and algorithms. If the source is long, consult lecturer on appendices/external listings rather than silently omitting files.
- [ ] **Standard test cases** with preconditions, input/steps, expected result, observed result, date, pass/fail and evidence (see `COURSEWORK-TEST-CASE-MATRIX.md`). CI logs alone are not a substitute for report-readable cases.
- [ ] **Clear screenshots** of the code and the actual application UI, according to the assessment brief's instructions. Capture *actual* running application screens, not Stitch mockups or illustrative previews.
- [ ] **Source/project ZIP** of all required project files as a separate compressed submission item. Follow the brief's instruction to upload it separately from the consolidated document.
- [ ] **Acknowledgement of AI Contribution** in the written report describing how generative AI contributed (planning, requirements, design, code/test assistance, editing, etc.), in accordance with the University's academic integrity guidance. Verify accuracy and write it in your own words.
- [ ] Coversheet, author/student ID, module details and any declaration required by your final assessment instructions.
- [ ] Confirm final deadline, formatting, assessment portal and submission rules with your module team. These were not specified in the uploaded draft.

## Recommended report outline (proposed organization)

1. Coursework coversheet and document metadata
2. Introduction, case study and scope
3. Requirements and provisional assumptions (summary referencing SRS)
4. OOP/class diagram image and explanation
5. Architecture and key algorithms (room availability and invoice calculation)
6. UI/UX decisions: Aqua Frost, Spatial Glass, semantic red/green actions and RBAC-driven navigation
7. Source code listings / appendix with all required files
8. Test-case matrix, observed runs and screenshots
9. Results, restrictions and future work
10. Acknowledgement of AI Contribution and references

This outline is an editorial proposal, not a new coursework requirement. The formal deliverables above are drawn from the supplied draft.

## Screenshot capture plan (Windows, actual application)

| Figure | Page/source | What it should prove |
| --- | --- | --- |
| UI-01 | `/register` | Guest registration fields |
| UI-02 | `/login` and `/workspace` | Login and permission-aware navigation |
| UI-03 | `/admin/rooms` | Inventory CRUD, room type, status |
| UI-04 | `/availability` | Search by type/date; live availability |
| UI-05 | `/my/booking-requests` | Guest pending and review history |
| UI-06 | `/admin/booking-requests` | Green approve / red reject actions |
| UI-07 | `/admin/reservations` | Booking search and cancellation |
| UI-08 | `/my/reservations` | Guest-own reservations |
| UI-09 | `/reservations/{id}/invoice` | Charges, discount, tax, advance and balance |
| UI-10 | `/reservations/{id}/modify` | Date and guest-count amendment history |
| UI-11 | `/admin/access` | Database-managed permissions/navigation |
| UI-12 | Invoice print preview (A4) | Print layout and itemized invoice |
| CODE-01 | `PricingCalculator.java` | Monetary formula and two-decimal rounding |
| CODE-02 | `BookingRules.java` | 48-hour and 24-hour checks |
| CODE-03 | `ReservationRepository.java` / V1 migration | Availability and SQL exclusion constraint |
| CODE-04 | `ReservationModificationService.java` | Locking, reprice and amendment history |
| CODE-05 | `AuthorizationService.java` | Ownership enforcement |

Use demonstration records only and obscure authentication, personal identifiers and secrets before inserting screenshots in an assessed report. Do not misrepresent mockups as executed tests.

## Environment and source ZIP creation

Source code is available at `https://github.com/SandaruWithanage/smart-stay`. For a clean source archive on Windows, run the included script from repository root:

```powershell
powershell -ExecutionPolicy Bypass -File scripts/Create-Submission-Zip.ps1
```

It archives **Git-tracked** source/docs only; local `.env`, `node_modules`, `target`, `.next`, and personal data must remain excluded. The output archive is not a report and does not fulfill the single-document requirement by itself.

Run the exact test commands listed in the README and capture final GitHub Actions run URL and commit before submission.

## Provisional assumptions still requiring approval

- Room capacity and the definition of extra-guest charge per night
- Hotel-local 14:00 check-in and booking/cancellation/modification cutoffs
- Tax application order and refund settlement method
- Whether modifying a reservation permits room type/unit changes
- Coursework interface selection (web GUI) and any final moderation changes
- Deadline, identity format and source-code listing length

**A passing CI build is not a substitute for lecturer decisions, manual testing, or final report author review.**
