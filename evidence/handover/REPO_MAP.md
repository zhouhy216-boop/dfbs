# REPO_MAP — Key folders and entry points

- **As-of:** 2026-02-09 14:00
- **Repo:** main
- **Commit:** 1df603c5
- **Verification method:** list_dir repo root and key dirs; `App.tsx`, `BasicLayout.tsx`, `DfbsAppApplication.java`, `package.json`, `pom.xml`.

**Facts only.** Paths relative to repo root.

---

## High-level directory map

| Path | Purpose |
|------|--------|
| `backend/dfbs-app/` | Single Spring Boot application module. |
| `backend/dfbs-app/src/main/java/com/dfbs/app/` | Java source: `DfbsAppApplication.java`, `config/`, `interfaces/` (REST), `application/` (services), `modules/` (entities/repos). |
| `backend/dfbs-app/src/main/resources/` | `application.yml`, `db/migration/`, `templates/`. |
| `backend/dfbs-app/src/test/java/com/dfbs/app/` | Tests: `application/`, `interfaces/`, `infra/` (e.g. SwaggerTest), root (DfbsAppApplicationTests, ArchitectureRulesTest, MasterDataReadOnlyRulesTest). |
| `frontend/dfbs-ui/` | Vite + React + Ant Design Pro frontend. |
| `frontend/dfbs-ui/src/` | `main.tsx`, `App.tsx`, `pages/`, `layouts/`, `shared/` (components, utils, stores), `features/` (platform, orgstructure), `services/` (if any at top level). |
| `evidence/handover/` | Handover pack: STATE_SNAPSHOT, UI_ENTRYPOINTS, API_SURFACE, DATA_BASELINE, TEST_BASELINE, REPO_MAP, REUSABLE_BLOCKS, REUSABLE_BLOCKS_ZH, DEV-ENV. Intended for PM/non-repo viewers to understand current behavior and run/verify builds. |
| `legacy/` | Quarantined legacy docs; not authoritative. |
| `infra/` | `docker-compose.yml` (postgres, redis, rabbitmq, minio). |

---

## Entry points

- **Frontend routing:** `frontend/dfbs-ui/src/main.tsx` → `App.tsx`. Route definitions: `frontend/dfbs-ui/src/App.tsx`.
- **Frontend layout/menu:** `frontend/dfbs-ui/src/layouts/BasicLayout.tsx`. Menu from `MENU_ROUTES_BASE` + `ORG_STRUCTURE_MENU` when super admin.
- **Backend application:** `backend/dfbs-app/src/main/java/com/dfbs/app/DfbsAppApplication.java`.
- **Backend REST:** `backend/dfbs-app/src/main/java/com/dfbs/app/interfaces/` — controller classes per domain (auth, platformorg, platformaccount, platformconfig, quote, workorder, expense, orgstructure, masterdata, shipment, damage, carrier, bom, customer, attachment, aftersales, repair, statement, freightbill, importdata, inventory, warehouse, triprequest, notification, permission, payment, smartselect, settings).
- **Build scripts:** Backend: `backend/dfbs-app/mvnw.cmd` (Windows) / `mvnw` (Unix). Frontend: `frontend/dfbs-ui/package.json` scripts (dev, build, lint, preview).
- **Config roots:** Backend: `backend/dfbs-app/src/main/resources/application.yml`. Frontend: `frontend/dfbs-ui/vite.config.ts`, `package.json`.

---

## Where evidence/handover lives and intended usage

- **Path:** `evidence/handover/`.
- **Usage:** Single source of truth for non-repo viewers (PM, Gemini, Cursor). Describes current routes, APIs, migrations, tests, reusable blocks, and dev env. Do not change application code from handover instructions; update handover docs to match repo reality.

---

## Not verified

- Root scripts (`DFBS-*.ps1`) behavior and dependency on CEO-OPS.md not executed.
