# TEST_BASELINE — How to run tests; what counts as BUILD SUCCESS

- **As-of:** 2026-02-09 14:00
- **Repo:** main
- **Commit:** 1df603c5
- **Verification method:** `backend/dfbs-app/pom.xml`, `frontend/dfbs-ui/package.json`, `backend/dfbs-app/src/main/resources/application.yml`, list_dir `backend/dfbs-app/src/test/java/com/dfbs/app/`.

**Facts only.** No test run was executed in this handover; BUILD SUCCESS criteria are defined from project config.

---

## Exact commands to run

| Scope | Command | Working directory |
|-------|---------|-------------------|
| Backend tests | `.\mvnw.cmd test` (Windows) or `./mvnw test` (Unix) | `backend/dfbs-app` |
| Backend compile (no tests) | `.\mvnw.cmd -q clean compile -DskipTests` | `backend/dfbs-app` |
| Frontend build | `npm run build` | `frontend/dfbs-ui` |
| Frontend lint | `npm run lint` | `frontend/dfbs-ui` |
| Frontend dev | `npm run dev` | `frontend/dfbs-ui` |

---

## What counts as BUILD SUCCESS

- **Backend:** Exit code 0; Maven log line `[INFO] BUILD SUCCESS`; test summary with Failures: 0, Errors: 0. Runner: Maven Surefire (JUnit 5). Tests under `backend/dfbs-app/src/test/java/com/dfbs/app/`.
- **Frontend:** `npm run build` completes without error; TypeScript compile (`tsc -b`) and Vite build succeed; output under `frontend/dfbs-ui/dist/`. Source: `package.json` script `"build": "tsc -b && vite build"`.
- **Full-suite (definition):** Backend `.\mvnw.cmd test` PASS and frontend `npm run build` PASS. Not verified in this handover.

---

## Required env / config (names + where referenced)

| Name | Where | Purpose |
|------|--------|---------|
| `spring.datasource.url` | `backend/dfbs-app/src/main/resources/application.yml` | JDBC URL (default `jdbc:postgresql://localhost:5432/dfbs`) |
| `spring.datasource.username` | same | DB user (default `dfbs`) |
| `spring.datasource.password` | same | DB password (default `dfbs`) |
| `spring.flyway.enabled` | same | Flyway on (default true); `validate-on-migrate: false`, `ignore-missing-migrations: true` in repo |

Frontend: no required env vars found in `frontend/dfbs-ui/src` for build; dev/proxy may use `.env` or Vite config. Not verified.

---

## Backend test layout (facts from repo)

- **Application (service) tests:** `src/test/java/com/dfbs/app/application/` — e.g. QuoteItemTest, QuoteStateTest, QuoteVoidTest, ShipmentPanoramaTest, ShipmentProcessTest, RepairRecordTest, PaymentRecordTest, QuotePaymentWorkflowTest, AccountStatementTest, ExpenseClaimTest, TripRequestTest, CorrectionTest, FreightBillTest, InventoryTest, InvoiceApplicationTest, CustomerMergeTest, DamageRecordTest, BomServiceTest, CarrierTest, PriceBookTest, NotificationTest, PermissionRequestTest, PaymentTest, DictionaryLogicTest, DictionaryQuoteTest, QuoteCcTest, QuoteDownstreamTest, QuoteExportTest, QuoteNumberingTest, QuotePartLinkTest, QuoteStandardizationTest, QuoteWarehouseTest, WorkOrderQuoteTest, ExpenseStatsTest, AttachmentRuleTest, MasterDataListTest; orgstructure: OrgLevelServiceTest, OrgStructureDevResetServiceTest.
- **Interface (controller) tests:** `src/test/java/com/dfbs/app/interfaces/` — e.g. ContractMasterDataCreateTest, CustomerMasterDataSearchTest, AccountStatementControllerTest, ContractPriceControllerTest, MachineMasterDataCreateTest, ProductMasterDataCreateTest, ProductMasterDataSearchTest, IccidMasterDataCreateTest, QuoteVersionActivateTest; orgstructure: OrgLevelControllerTest, OrgLevelReorderControllerTest, OrgChangeLogControllerTest, OrgNodeControllerTest, OrgStructureResetAllControllerTest.
- **Other:** `DfbsAppApplicationTests.java`, `ArchitectureRulesTest.java`, `MasterDataReadOnlyRulesTest.java`; `infra/SwaggerTest.java`.

---

## Frontend scripts (from package.json)

Source: `frontend/dfbs-ui/package.json`.

| Script | Command |
|--------|---------|
| dev | vite |
| build | tsc -b && vite build |
| lint | eslint . |
| preview | vite preview |

There is no `test` script. Frontend gate for PR: Not verified; candidates are `npm run build`, `npm run lint`.

---

## Common failure modes (facts from repo config)

- Backend: Tests that hit the DB require Postgres at `spring.datasource.url`; if DB is down or migrations not applied, tests can fail. Flyway is configured with `validate-on-migrate: false` and `ignore-missing-migrations: true` (see `application.yml`), so missing V0056 does not block startup.
- Frontend: `npm run build` runs `tsc -b` first; TypeScript errors cause build failure. No test script; no automated frontend test run documented.

---

## Not verified

- Full backend test run and full frontend build were not executed in this handover.
- Required env vars for frontend dev (e.g. proxy, API base URL) not enumerated from code.
