# TEST_BASELINE — Full test suite (facts)

**Facts only.** No plans or suggestions.

---

## What "full test suite" means

- **Backend**: Maven Surefire runs JUnit tests. Command: from `backend/dfbs-app/`, run `./mvnw.cmd test` (Windows) or `./mvnw test` (Unix). This compiles, runs all tests in `src/test/java`, and fails the build if any test fails.
- **Frontend**: See "Frontend scripts" below. Which command is the team's "frontend gate" is Unknown (not verified); no CI config was inspected. To verify: check repo for CI config (e.g. GitHub Actions, Jenkinsfile) or team runbook.

---

## Frontend scripts (from package.json)

Source: `frontend/dfbs-ui/package.json` (scripts section).

| Script name | Command |
|-------------|--------|
| `dev` | `vite` |
| `build` | `tsc -b && vite build` |
| `lint` | `eslint .` |
| `preview` | `vite preview` |

There is no `test` script in package.json. The command used as the team's "frontend gate" (e.g. for PR/merge) is Unknown (not verified). To verify: see CI configuration or ask the team; possible candidates are `npm run build` (production build) and/or `npm run lint`.

---

## Key test suites / test names (backend)

- **Application (service) tests**: `com.dfbs.app.application.*` — e.g. `QuoteItemTest`, `QuoteStateTest`, `QuoteVoidTest`, `ShipmentPanoramaTest`, `ShipmentProcessTest`, `RepairRecordTest`, `PaymentRecordTest`, `QuotePaymentWorkflowTest`, `AccountStatementTest`, `ExpenseClaimTest`, `TripRequestTest`, `CorrectionTest`, `FreightBillTest`, `InventoryTest`, `InvoiceApplicationTest`, `CustomerMergeTest`, `DamageRecordTest`, `BomServiceTest`, `CarrierTest`, `PriceBookTest`, `NotificationTest`, `PermissionRequestTest`, `PaymentTest`, `DictionaryLogicTest`, `DictionaryQuoteTest`, `QuoteCcTest`, `QuoteDownstreamTest`, `QuoteExportTest`, `QuoteNumberingTest`, `QuotePartLinkTest`, `QuoteStandardizationTest`, `QuoteWarehouseTest`, `WorkOrderQuoteTest`.
- **Interface (controller) tests**: `com.dfbs.app.interfaces.*` — e.g. `ContractMasterDataCreateTest`, `ContractMasterDataSearchTest`, `CustomerMasterDataCreateTest`, `CustomerMasterDataSearchTest`, `AccountStatementControllerTest`, `ContractPriceControllerTest`, `MachineMasterDataCreateTest`, `ProductMasterDataCreateTest`, `ProductMasterDataSearchTest`, `IccidMasterDataCreateTest`, `QuoteVersionActivateTest`, `AccountStatementControllerTest`.
- **Smoke / rules**: `DfbsAppApplicationTests`, `ArchitectureRulesTest`, `MasterDataReadOnlyRulesTest`, `SwaggerTest`.

Full list: enumerate test classes under `backend/dfbs-app/src/test/java/com/dfbs/app/`.

---

## Key build/test evidence lines (self-produced vs CEO gate)

Facts only. CEO has reported backend BUILD SUCCESS in their environment; do not assert that as this handover’s own build proof. If Delivery PM provides CEO-gate snippets, include them explicitly labeled: **"CEO gate evidence (provided by CEO): &lt;snippet&gt;"**

- **Backend compile** (no tests): `.\mvnw.cmd -q clean compile -DskipTests` → When successful: exit 0, BUILD SUCCESS. Self-produced in handover environment: success. Key line (when available): `[INFO] BUILD SUCCESS` (and "Finished at" or equivalent).
- **Backend full test**: `.\mvnw.cmd test` → When all pass: BUILD SUCCESS and "Tests run: N, Failures: 0, Errors: 0". Spring Boot tests require ApplicationContext (DB/Flyway etc.); if infra is missing, context load may fail. Not verified in this handover environment. CEO gate evidence (provided by CEO): [none provided in this handover; add snippet here when provided by Delivery PM].
- **Frontend — dev server vs production build**:
  - **Dev server**: `npm run dev` → "dev server ready" when Vite reports local URL. Command: `vite`. Not verified in this handover environment.
  - **Production build**: `npm run build` → Runs `tsc -b && vite build`. Success: build completes and emits output (e.g. to `dist/`). If you can self-produce success, paste only the key success line(s). If not verified, keep "Not verified in this environment"; do not list speculative TS errors unless you observed them. CEO gate evidence (provided by CEO): [none provided in this handover; add when provided].
