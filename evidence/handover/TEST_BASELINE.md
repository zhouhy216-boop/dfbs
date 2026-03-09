# TEST_BASELINE — How to run tests; what counts as BUILD SUCCESS

- **As-of:** 2025-02-24 (stage baseline rebuild)
- **Repo:** main
- **Commit:** 328150bd
- **Verification method:** Inspected `backend/dfbs-app/pom.xml`, `frontend/dfbs-ui/package.json`, `application.yml`; grep `*Test.java` under `backend/dfbs-app/src/test/`.

**Facts only.** No test run was executed in this handover; BUILD SUCCESS criteria are defined from project config.

---

## Exact commands to run

| Scope | Command | Working directory |
|-------|---------|-------------------|
| Backend tests | `.\mvnw.cmd test` (Windows) or `./mvnw test` (Unix) | `backend/dfbs-app` |
| Backend compile (no tests) | `.\mvnw.cmd -q clean compile -DskipTests` or `.\mvnw.cmd -q -DskipTests package` | `backend/dfbs-app` |
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

- **Application (service) tests:** `src/test/java/com/dfbs/app/application/` — e.g. QuoteItemTest, ShipmentPanoramaTest, ShipmentProcessTest, AttachmentRuleTest, DamageRecordTest, RepairRecordTest, QuotePaymentWorkflowTest, AccountStatementTest, and subpackages quote, quote/void, quote/payment, masterdata, orgstructure.
- **Interface (controller) tests:** `src/test/java/com/dfbs/app/interfaces/` — e.g. ShipmentControllerPermissionTest, ContractMasterDataCreateTest, CustomerMasterDataSearchTest, AccountStatementControllerTest, ContractPriceControllerTest, MachineMasterDataCreateTest, OrgLevelControllerTest, OrgLevelReorderControllerTest, OrgChangeLogControllerTest, OrgNodeControllerTest, OrgStructureResetAllControllerTest.
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

## Reality semantics

- **What “build success” proves:** Backend BUILD SUCCESS means compile + tests pass (no runtime deployment). Frontend build success means tsc and Vite build complete; it does not prove E2E or that all pages work with real backend.
- **What it does not prove:** Full-suite build was not run in this handover. Frontend currently fails build due to existing TS errors; backend test run not executed.

---

## Common failure modes (facts from repo config)

- **Backend:** Tests that hit the DB require Postgres at `spring.datasource.url`; if DB is down or migrations not applied, tests can fail. Flyway is configured with `validate-on-migrate: false` and `ignore-missing-migrations: true` (see `application.yml`), so missing V0056 does not block startup.
- **Frontend:** `npm run build` runs `tsc -b` first; TypeScript errors cause build failure. At handover time, multiple existing TS errors in repo (e.g. `OrgTreeSelect.tsx`, `AccountPermissions/BizPermCatalogMaintenance.tsx`, `OrgTree/index.tsx`, `RolesPermissions/index.tsx`, `AfterSales/index.tsx`, `ImportCenter/index.tsx`, `Quote/index.tsx`, `Platform/Application/index.tsx`, others) cause `npm run build` to fail. No automated frontend test script.

---

## Not verified

- Full backend test run and full frontend build were not executed in this handover.
- Required env vars for frontend dev (e.g. proxy, API base URL) not enumerated from code.
