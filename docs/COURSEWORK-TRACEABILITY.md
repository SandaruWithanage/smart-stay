# SmartStay requirement-to-source traceability

**Authority:** User-provided *draft* CI6115 Programming III CW1 SmartStay hotel case study, supported by SmartStay's proposed SRS. Only the case-study rules are identified as **brief-required** here; additional design choices are marked **project design**. Verify the final moderated assignment before submission.

| Requirement / rule | Source implementation | Verification |
| --- | --- | --- |
| [Brief] Room type: Standard, Deluxe, Suite, Family Suite | `room/domain/RoomType.java`; Flyway V1 `room_packages` | `RoomManagementService`, `BookingPersistenceIntegrationTest` |
| [Brief] Room ID, bed type, status, rate | `room/persistence/RoomEntity.java`, `RoomPackageEntity.java`, `RoomView.java` | Room inventory UI and integration tests |
| [Brief] Four daily rates, extras, breakfast, tax by type | `room/domain/RoomPackage.java`; `V1__create_smartstay_schema.sql` | `PricingCalculatorTest` and booking invoice persistence |
| [Brief] Registration | `identity/service/IdentityService.java`, `guest/GuestEntity.java` | `AuthorizationHttpIntegrationTest`; login/register browser flow |
| [Brief] Search by room type and dates | `room/service/RoomAvailabilityService.java`, `RoomRepository.java` | `BookingPersistenceIntegrationTest.bookingBlocksOverlappingDatesButAllowsSameDayTurnover`; public Playwright search |
| [Brief] Booking at least two days in advance | `reservation/service/BookingRules.java` | `BookingRulesTest` |
| [Brief] Refundable LKR 10,000 booking advance | `BookingManagementService.java`, `ReservationEntity.java` | Booking persistence and request-approval tests; no actual payment gateway |
| [Brief] Prevent booking occupied/maintenance room | `RoomRepository.java`; PostgreSQL exclusion constraint | Availability and overlap integration tests |
| [Brief] Booking confirmation, room and guest details | `ReservationController.java`, `BookingManagementService.java` | Booking persistence; browser desk/guest flow |
| [Brief] Search by booking ID, guest name, check-in | `BookingManagementService.search`, `ReservationRepository.java` | `BookingPersistenceIntegrationTest.searchableByBookingIdGuestNameAndCheckinDate` |
| [Brief] Cancellation not within 24h | `BookingRules.requireCancellationAllowed`, `BookingManagementService.cancel` | Boundary and cancellation integration tests |
| [Brief] Modification not within 24h | `ReservationModificationService.java`, Flyway V7 `reservation_amendments` | Amendment JUnit, HTTP and Playwright tests |
| [Brief] 12% base-room discount for 5+ nights | `PricingCalculator.calculate` | Pricing and five-night invoice integration tests |
| [Brief] Base + extras − discount + tax − advance | `billing/service/PricingCalculator.java` | `PricingCalculatorTest` + exact saved invoice test |
| [Brief] Detailed invoice | `billing/persistence/InvoiceEntity.java`, `billing/api/InvoiceView.java` | A4 invoice UI; persisted snapshot tests |
| [Brief] Administrator room CRUD/status | `room/service/RoomManagementService.java`; `/admin/rooms` | Room service and browser E2E |
| [Project design] PostgreSQL RBAC / constrained ABAC | `AuthorizationService.java`, `AccessRepository.java`, `CurrentIdentityConverter.java` | `AuthorizationHttpIntegrationTest` |
| [Project design] Database-owned sidebar | `UiManifestService.java`, `NavigationRepository.java`; `navigation.ts` | Permission UI test, session-driven browser navigation |
| [Project design] Guest request approval states | `BookingRequestService.java`; Flyway V6 | `BookingRequestHttpIntegrationTest`, browser request/approval |
| [Project design] Aqua Frost and Spatial Glass color system | `frontend/src/app/globals.css`; shared `components/ui` | `frontend/tests/semantic-colors.test.mjs`, Playwright smoke tests |

## Important qualification

The brief prohibits reservation modification within 24 hours but does not unambiguously define which fields may change. This implementation supports same-room dates and guest count pending lecturer confirmation. Extra-guest **per-night** charges and 14:00 hotel-local check-in are provisional assumptions. See `docs/ASSUMPTIONS.md`.

The report should describe these as assumptions, not claim they are explicitly prescribed by the coursework. Tax computation and discount order must also remain faithful to the given brief plus documented interpretation.

Any claims about manual accessibility, screenshots, third-party payments, refund settlement or final lecturer approval must be supported by separate actual evidence before submission.
