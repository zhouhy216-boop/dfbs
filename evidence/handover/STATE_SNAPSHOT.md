# STATE_SNAPSHOT — What works now / known limitations

- **As-of:** 2026-02-09 14:00
- **Repo:** main
- **Commit:** 1df603c5
- **Verification method:** `App.tsx`, `BasicLayout.tsx`, `backend/dfbs-app/src/main/java/com/dfbs/app/interfaces/`, `db/migration/` list_dir.

**Facts only.** Derived from repo code and config. Unverifiable items marked `Not verified`.

---

## What works now (user-visible capabilities)

| Capability | Route / entry | Evidence |
|------------|---------------|----------|
| Login | `/login` | `App.tsx` L49, `pages/Login/index.tsx` |
| Dashboard | `/dashboard` | `App.tsx` L54, `pages/Dashboard/index.tsx` |
| Customers | `/customers` | `App.tsx` L55, `pages/Customer/index.tsx` |
| Quotes | `/quotes` | `App.tsx` L56, `pages/Quote/index.tsx` |
| Shipments | `/shipments` | `App.tsx` L58, `pages/Shipment/index.tsx` |
| After-sales list/detail | `/after-sales`, `/after-sales/:id` | `App.tsx` L59–60, `pages/AfterSales/index.tsx`, `Detail.tsx` |
| Work orders list/detail | `/work-orders`, `/work-orders/:id` | `App.tsx` L62–63, `pages/WorkOrder/Internal/index.tsx`, `Detail.tsx` |
| Public repair (no auth) | `/public/repair` | `App.tsx` L50, `pages/WorkOrder/Public/index.tsx` |
| Finance | `/finance` | `App.tsx` L64, `pages/Finance/index.tsx` |
| Warehouse inventory/replenish | `/warehouse/inventory`, `/warehouse/replenish` | `App.tsx` L65–66, `pages/Warehouse/Inventory/index.tsx`, `Replenish/index.tsx` |
| Import center | `/import-center` | `App.tsx` L67, `pages/ImportCenter/index.tsx` |
| Master data (contracts, models, spare-parts, machines, sim-cards, model-part-lists) | `/master-data/*` | `App.tsx` L68–77, `pages/MasterData/*` |
| Platform orgs/applications/sim-applications | `/platform/orgs`, `/platform/applications`, `/platform/sim-applications` + history/apply/reuse/verification/sim-activation | `App.tsx` L78–86, `pages/Platform/*` |
| Admin confirmation center | `/admin/confirmation-center` | `App.tsx` L89, `pages/Admin/ConfirmationCenter/index.tsx` |
| Admin org-structure (Super Admin only) | `/admin/org-levels`, `/admin/org-tree`, `/admin/org-change-logs` | `App.tsx` L90–92 (SuperAdminGuard), `pages/Admin/OrgLevelConfig`, `OrgTree`, `OrgChangeLog/index.tsx` |
| System platform config | `/system/platform-config` | `App.tsx` L93, `pages/System/PlatformConfig/index.tsx` |

---

## Known limitations (facts)

- Docs under `legacy/` are not authoritative (repo convention).
- Backend full test run (`.\mvnw.cmd test`) and frontend production build (`npm run build`) were not executed in this handover; BUILD SUCCESS is Not verified.

---

## Feature flags / config gates

None found in application code (no `featureFlag`, `feature.`, or config gate symbols in `backend/dfbs-app/src`, `frontend/dfbs-ui/src` excluding node_modules).

---

## Entry points (repo paths)

- Frontend: `frontend/dfbs-ui/src/main.tsx` → `App.tsx`. Routes: `frontend/dfbs-ui/src/App.tsx`.
- Layout/menu: `frontend/dfbs-ui/src/layouts/BasicLayout.tsx`. Super Admin menu: `ORG_STRUCTURE_MENU` (层级配置, 组织架构, 变更记录).
- Backend: `backend/dfbs-app/src/main/java/com/dfbs/app/DfbsAppApplication.java`.
- REST: `backend/dfbs-app/src/main/java/com/dfbs/app/interfaces/` (per-domain controllers).
- Migrations: `backend/dfbs-app/src/main/resources/db/migration/` (V0001–V0066; V0056 missing).
- Tests: `backend/dfbs-app/src/test/java/com/dfbs/app/`.

---

## Build commands (facts)

| Action | Command | Success criterion |
|--------|---------|-------------------|
| Backend compile | `cd backend/dfbs-app` then `.\mvnw.cmd -q clean compile -DskipTests` | Exit 0, BUILD SUCCESS |
| Backend test | `cd backend/dfbs-app` then `.\mvnw.cmd test` | Exit 0, Failures: 0, Errors: 0. Not verified. |
| Frontend dev | `cd frontend/dfbs-ui` then `npm run dev` | Runs Vite. Source: `package.json` "dev". Not verified. |
| Frontend build | `cd frontend/dfbs-ui` then `npm run build` | `tsc -b && vite build`; output in `dist/`. Source: `package.json` "build". Not verified. |

---

## Not verified

- Full backend test run and frontend production build not executed in this handover.
- Access component: exact pages using it for permission-based UI not enumerated (only presence in codebase confirmed).
