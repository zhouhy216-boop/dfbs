# REPO_MAP — Key folders and entry points

- **As-of:** 2025-02-24 20:00
- **Repo:** main
- **Commit:** 983df8e7
- **Verification method:** List repo root and key dirs; inspected `App.tsx`, `BasicLayout.tsx`, `DfbsAppApplication.java`, `package.json`, `pom.xml`.

**Facts only.** Paths relative to repo root.

---

## High-level directory map

| Path | Purpose |
|------|---------|
| `backend/` | Maven root; contains `dfbs-app/` (single Spring Boot module). |
| `backend/dfbs-app/` | Spring Boot application. |
| `backend/dfbs-app/src/main/java/com/dfbs/app/` | Java source: `DfbsAppApplication.java`, `config/`, `interfaces/` (REST controllers), `application/` (services), `modules/` (entities/repos), `infra/` (e.g. GlobalExceptionHandler). |
| `backend/dfbs-app/src/main/resources/` | `application.yml`, `db/migration/` (Flyway V0001–V0086), `templates/`. |
| `backend/dfbs-app/src/test/java/com/dfbs/app/` | Tests: `application/`, `interfaces/`, `infra/` (e.g. SwaggerTest), root (DfbsAppApplicationTests, ArchitectureRulesTest, MasterDataReadOnlyRulesTest). |
| `frontend/dfbs-ui/` | Vite + React + TypeScript + Ant Design Pro frontend. |
| `frontend/dfbs-ui/src/` | `main.tsx`, `App.tsx`, `pages/`, `layouts/`, `shared/` (components, utils, stores, hooks), `features/` (dicttype, orgstructure, platform). |
| `evidence/handover/` | Handover pack: STATE_SNAPSHOT.md, UI_ENTRYPOINTS.md, API_SURFACE.md, DATA_BASELINE.md, TEST_BASELINE.md, REPO_MAP.md, REUSABLE_BLOCKS.md, REUSABLE_BLOCKS_ZH.md, DEV-ENV.md. Intended for PM/non-repo viewers to understand current behavior and run/verify builds. |
| `evidence/perm/verify/` | Permission/impact check and delivery clarify docs (e.g. DICT_260303_001_*). |
| `docs/` | Project docs (e.g. test-data-cleaner, perm, dev-frontend-proxy). |
| `_DFBS_TEMPLATES/` | Planning/delivery templates. |

---

## Entry points

- **Frontend routing:** `frontend/dfbs-ui/src/main.tsx` → `App.tsx`. Route definitions: `frontend/dfbs-ui/src/App.tsx` (Routes, Route, Navigate).
- **Frontend layout/menu:** `frontend/dfbs-ui/src/layouts/BasicLayout.tsx`. Menu from `buildMenuRoutes(isSuperAdmin, _permAllowed, isAdminOrSuperAdmin, hasPermission)` using `MENU_ROUTES_BASE`, `adminExtras` = `ACCOUNT_PERMISSIONS_MENU` (Admin or Super Admin) + `ORG_STRUCTURE_MENU` (Super Admin only). Left menu does not include 角色与权限 or 字典类型 (removed per menu cleanup). Simulated-role filter: `filterMenuBySimulatedRole(menuRoutes, simulatedRole)`; store: `useSimulatedRoleStore`.
- **Backend application:** `backend/dfbs-app/src/main/java/com/dfbs/app/DfbsAppApplication.java`.
- **Backend REST:** `backend/dfbs-app/src/main/java/com/dfbs/app/interfaces/` — controller classes per domain (auth, dicttype, platformorg, platformaccount, platformconfig, quote, workorder, expense, orgstructure, masterdata, shipment, damage, carrier, bom, customer, attachment, aftersales, repair, statement, freightbill, importdata, inventory, warehouse, triprequest, notification, permission, payment, smartselect, settings, testdatacleaner, bizperm, perm, correction, iccid).
- **Build scripts:** Backend: `backend/dfbs-app/mvnw.cmd` (Windows) / `mvnw` (Unix). Frontend: `frontend/dfbs-ui/package.json` scripts (dev, build, lint, preview).
- **Config roots:** Backend: `backend/dfbs-app/src/main/resources/application.yml`. Frontend: `frontend/dfbs-ui/vite.config.ts`, `package.json`.

---

## Where evidence/handover lives and intended usage

- **Path:** `evidence/handover/`.
- **Usage:** Single source of truth for non-repo viewers (PM, operations). Describes current routes, APIs, migrations, test commands, reusable blocks, and dev env. Do not change application code from handover instructions; update handover docs to match repo reality.

---

## Not verified

- Root scripts (`DFBS-*.ps1`) behavior and dependency on CEO-OPS.md not executed.
