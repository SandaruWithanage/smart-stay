# SmartStay coursework: Test cases and execution evidence

**Project:** SmartStay Hotel Booking System · CI6115 Programming III CW1
**Baseline:** latest merged feature PR #8 (20 November 2026)
**Method:** Unit tests (JUnit 5), transactional PostgreSQL integration/HTTP tests (Spring Boot), browser E2E (Playwright Chromium) and manual UI acceptance tests.
**Evidence:** [CI for amendment PR #8](https://github.com/SandaruWithanage/smart-stay/actions/runs/44721081009): 40 Java tests (0 failures), Next.js build/typecheck and browser E2E jobs succeeded. **Do not replace screenshots of actually executed tests with sample examples.**

## Test case template

Each case records **ID**, objective, preconditions, test procedure/input, expected result, execution method/evidence, and result. `Passed (CI)` means the named automated Java/Playwright tests ran on the cited CI run. `Not yet executed` means no verified manual test evidence is available. Student must complete actual screenshot/reviewer fields in their final report.

## Business rules, calculation and persistence

| ID | Objective / input and steps | Expected result | Implemented automated evidence | Result |
| --- | --- | --- | --- | --- |
| TC-001 | Create Standard room with valid type, bed, capacity | Room persisted and discoverable | `BookingPersistenceIntegrationTest` setup / `RoomManagementService` | Passed (CI) |
| TC-002 | Search occupied room for overlapping half-open interval | Confirmed booking blocks overlapping stay | `BookingPersistenceIntegrationTest.bookingBlocksOverlappingDatesButAllowsSameDayTurnover` | Passed (CI) |
| TC-003 | Search room starting on previous guest's checkout day | Same-day turnover permitted | Same test as TC-002 | Passed (CI) |
| TC-004 | Attempt booking room under maintenance | Booking rejected | `BookingPersistenceIntegrationTest.unavailableRoomOrIncorrectRoomTypeCannotBeBooked` | Passed (CI) |
| TC-005 | Retire room with future confirmed reservation | Retirement blocked | `BookingPersistenceIntegrationTest.roomCannotBeRetiredWithFutureConfirmedReservations` | Passed (CI) |
| TC-006 | Book less than 48h before 14:00 check-in | Validation rejects booking | `BookingRulesTest.oneMinuteInsideAdvanceWindowIsRejected` | Passed (CI) |
| TC-007 | Book exactly 48h before provisional check-in | Boundary accepted | `BookingRulesTest.exactly48HoursAdvanceIsAllowed` | Passed (CI) |
| TC-008 | Cancel exactly 24h before check-in | Cancellation accepted | `BookingRulesTest.exactly24HoursCancellationIsAllowed` | Passed (CI) |
| TC-009 | Cancel 23h before check-in | Cancellation rejected | `BookingRulesTest.cancellationInside24HoursIsRejected` | Passed (CI) |
| TC-010 | Five-night Deluxe stay, three guests, LKR 10,000 advance | Base 45,000; extras 7,500; discount 5,400; tax 5,652; balance 42,752 | `BookingPersistenceIntegrationTest.persistsBookingWithPriceSnapshotAndFiveNightDiscount` | Passed (CI) |
| TC-011 | Cancel reservation with stored invoice | Invoice void; LKR 10,000 refund liability PENDING, no transfer | `BookingPersistenceIntegrationTest.cancellationVoidsInvoiceAndRecordsUnsettledRefund` | Passed (CI) |
| TC-012 | Search reservation by UUID, name, check-in date | Records returned by exactly one filter | `BookingPersistenceIntegrationTest.searchableByBookingIdGuestNameAndCheckinDate` | Passed (CI) |
| TC-013 | Amend two-night reservation to five nights and three guests | Invoice repriced, recorded advance retained, history row stores previous and new totals | `BookingPersistenceIntegrationTest.amendmentRecalculatesInvoiceWithoutSecondAdvanceAndAppendsHistory` | Passed (CI) |
| TC-014 | Submit same amendment twice | No second history entry for unchanged state | Same amendment test as TC-013 | Passed (CI) |
| TC-015 | Amend into another confirmed stay | Conflict rejected | `BookingPersistenceIntegrationTest.amendmentCannotOverlapAnotherConfirmedReservation` | Passed (CI) |
| TC-016 | Amend cancelled reservation | Rejected | `BookingPersistenceIntegrationTest.cancelledBookingCannotBeModified` | Passed (CI) |
| TC-017 | Amend at 25h vs 23h before original check-in | 25h accepted, 23h rejected | `BookingRulesTest.modificationCutoffUsesOriginalCheckIn` | Passed (CI) |

## Authentication, permission, review state and privacy

| ID | Objective / input and steps | Expected result | Implemented automated evidence | Result |
| --- | --- | --- | --- | --- |
| TC-018 | Access privileged API with no JWT or invalid JWT | 401 Unauthorized | `AuthorizationHttpIntegrationTest.unauthenticatedRequestsAndBadTokensAreRejected` | Passed (CI) |
| TC-019 | Guest opens another guest's record, reservation or invoice | 403 Forbidden | `AuthorizationHttpIntegrationTest.guestRoleCannotCreateRoomsOrReadOtherGuests`; `crossGuestReservationAndInvoiceAccessIsDenied` | Passed (CI) |
| TC-020 | Assign ADMIN in DB, change role grants, reuse original token | Effective permissions checked again on each request | `AuthorizationHttpIntegrationTest.rolesAndNavigationAreResolvedFromDatabaseNotJwtClaims` | Passed (CI) |
| TC-021 | Logout; retry old access token | Request rejected | `AuthorizationHttpIntegrationTest.logoutRevokesCurrentTokenVersionImmediately` | Passed (CI) |
| TC-022 | Try to remove ADMIN access-management permission | Role policy update rejected | `AuthorizationHttpIntegrationTest.roleEditorPreventsDeletingAdminManagementPermission` | Passed (CI) |
| TC-023 | Guest sends stay request without payment | PENDING; no confirmed reservation created | `BookingRequestHttpIntegrationTest.guestRequestIsUnpaidAndDoesNotReserveRoomUntilStaffApproval` | Passed (CI) |
| TC-024 | Guest tries to approve request, or admin approves with false advance receipt | Guest denied; false receipt rejected | Same as TC-023 | Passed (CI) |
| TC-025 | Staff approves pending request after confirming advance | APPROVED; single confirmed reservation, invoice persisted | Same as TC-023 | Passed (CI) |
| TC-026 | Staff tries to approve request twice | Second attempt conflicts | Same as TC-023 | Passed (CI) |
| TC-027 | Reject request with reason; owner withdraws pending request | REJECTED/WITHDRAWN without booking creation | `BookingRequestHttpIntegrationTest.rejectedAndWithdrawnRequestsNeverBecomeReservations` | Passed (CI) |
| TC-028 | Guest tries to withdraw another guest's request | Forbidden; only own list returned | `BookingRequestHttpIntegrationTest.guestCannotWithdrawOrListAnotherUsersRequest` | Passed (CI) |
| TC-029 | Guest submits duplicate pending room/date request | Database blocks duplicate | `BookingRequestHttpIntegrationTest.duplicatePendingRoomDateRequestIsRejected` | Passed (CI) |
| TC-030 | Guest modifies own reservation; tries another user's | Own modification allowed, cross-guest 403 | `ReservationAmendmentHttpIntegrationTest.ownGuestCanAmendButCannotAmendAnotherGuestsReservation` | Passed (CI) |
| TC-031 | Admin modifies another guest's reservation with current role | Allowed and repriced | `ReservationAmendmentHttpIntegrationTest.administratorCanAmendAfterDatabaseRoleAssignment` | Passed (CI) |

## Frontend and acceptance evidence

| ID | Procedure | Expected | Evidence | Result |
| --- | --- | --- | --- | --- |
| TC-032 | Open public catalog; switch Aqua Frost ↔ Spatial Glass on desktop/mobile | Headings load, theme changes, menu remains navigable | `frontend/e2e/public-smoke.spec.ts` | Passed (CI) |
| TC-033 | Guest login and admin route access without session | Sign-in visible; unauthenticated admin redirects | `frontend/e2e/public-smoke.spec.ts` | Passed (CI) |
| TC-034 | Real full-stack flow: admin creates room → guest registers → requests stay → admin approves → guest prints invoice | Request and confirmed reservation stored, invoice shown | `frontend/e2e/full-request-journey.spec.ts` | Passed (CI) |
| TC-035 | Guest modifies confirmed reservation; invoice recalculated | New five-night invoice shown | Same E2E test as TC-034 | Passed (CI) |
| TC-036 | Manually tab through every interactive screen and document focus/focus order | All controls keyboard accessible | Needs human QA screenshots/notes | Not yet executed |
| TC-037 | Inspect common tables/sidebar at 320px, 375px, 768px, 1440px | No clipped critical actions/text | Needs browser screenshots | Not yet executed |
| TC-038 | Open invoice print preview in Chrome, save A4 PDF | No cut off charges; buttons/sidebar hidden | Needs print-preview screenshots | Not yet executed |
| TC-039 | Screen-reader walkthrough (e.g., NVDA on Windows) | Labels/headings/status announcements intelligible | Needs actual assistive-tech test | Not yet executed |
| TC-040 | Test failed payment claim/refund process against a real provider | Should only be possible with verified provider implementation | No provider integration exists | Not applicable to current implementation |

## Execution record template for report

```text
Test Case ID: TC-___
Tester: [NAME / STUDENT ID]
Date, Time and Environment: [FILL IN]
Preconditions:
Input/Test Steps:
Expected Result:
Actual Result:
Status: PASS / FAIL / BLOCKED / NOT RUN
Evidence: [Screenshot/CI run link, commit SHA, test log]
Defect ID and Retest, if any:
```

The table is an implementation/test mapping, not an assertion that all manual tests were performed. The CI runs use generated test guest data and an isolated PostgreSQL service. Do not copy actual passport/NIC details or real passwords into evidence screenshots.
