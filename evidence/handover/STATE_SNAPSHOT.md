# STATE_SNAPSHOT — What works now / known limitations

**Facts only.** Derived from repo code and config. Unverified items marked `Not verified`.

---

## What the system does (by route/page)

- **Login** (`/login`): User authenticates; token stored in localStorage; redirect to `/dashboard`. Source: `App.tsx`, `pages/Login/index.tsx`.
- **Dashboard** (`/dashboard`): Entry after login. Source: `App.tsx`, `pages/Dashboard/index.tsx`.
- **Customers** (`/customers`): Customer master data list. Source: `App.tsx`, `pages/Customer/index.tsx`.
- **Quotes** (`/quotes`): Quote CRUD, workflow, items, attachments, payment, void. Source: `App.tsx`, `pages/Quote/index.tsx`.
- **Shipments** (`/shipments`): Shipment create/accept/ship/complete, machines, export, damages. Source: `App.tsx`, `pages/Shipment/index.tsx`.
- **After-sales** (`/after-sales`, `/after-sales/:id`): After-sales list and detail. Source: `App.tsx`, `pages/AfterSales/index.tsx`, `Detail.tsx`.
- **Work orders** (`/work-orders`, `/work-orders/:id`): Internal work order list and detail. Source: `App.tsx`, `pages/WorkOrder/Internal/index.tsx`, `Detail.tsx`.
- **Public repair** (`/public/repair`): Public work order create (no auth). Source: `App.tsx`, `pages/WorkOrder/Public/index.tsx`.
- **Finance** (`/finance`): Finance list/views. Source: `App.tsx`, `pages/Finance/index.tsx`.
- **Warehouse** (`/warehouse/inventory`, `/warehouse/replenish`): Inventory and replenish. Source: `App.tsx`, `pages/Warehouse/Inventory/index.tsx`, `Replenish/index.tsx`.
- **Import center** (`/import-center`): Bulk import (customers, contracts, models, spare-parts, machines, sim-cards, model-part-lists). Source: `App.tsx`, `pages/ImportCenter/index.tsx`.
- **Master data**: Contracts, machine models, spare parts, machines, sim cards, model-part-lists (list/detail where present). Source: `App.tsx`, `pages/MasterData/*`.
- **Platform** (`/platform/applications`, `/platform/orgs`, `/platform/sim-applications`): Account applications, orgs, sim applications. Source: `App.tsx`, `pages/Platform/Application/index.tsx`, `Org/index.tsx`, `SimApplication/index.tsx`.
- **Admin** (`/admin/confirmation-center`): Confirmation center. Source: `App.tsx`, `pages/Admin/ConfirmationCenter/index.tsx`.
- **Admin (Super Admin only)** (`/admin/org-levels`, `/admin/org-tree`, `/admin/org-change-logs`): Level config, org tree, change logs. Source: `App.tsx` (SuperAdminGuard), `pages/Admin/OrgLevelConfig/index.tsx`, `OrgTree/index.tsx`, `OrgChangeLog/index.tsx`.
- **System** (`/system/platform-config`): Platform config. Source: `App.tsx`, `pages/System/PlatformConfig/index.tsx`.

---

## Known limitations (facts)

- Legacy docs under `legacy/` are not authoritative for requirements.
- Full backend test run (`.\mvnw.cmd test`) and frontend production build (`npm run build`) success: Not verified in this handover. Run locally to confirm BUILD SUCCESS.

---

## Entry points (repo paths)

- Frontend entry: `frontend/dfbs-ui/src/main.tsx` → `App.tsx`. Routes: `App.tsx`.
- Frontend layout/menu: `frontend/dfbs-ui/src/layouts/BasicLayout.tsx`. Super Admin sees extra menu: 层级配置, 组织架构, 变更记录 (from `ORG_STRUCTURE_MENU` in BasicLayout.tsx).
- Backend entry: `backend/dfbs-app/src/main/java/com/dfbs/app/DfbsAppApplication.java`.
- Backend REST: `backend/dfbs-app/src/main/java/com/dfbs/app/interfaces/` (controllers by domain).
- DB migrations: `backend/dfbs-app/src/main/resources/db/migration/` (V0001 through V0066; no V0056 in repo).
- Tests: `backend/dfbs-app/src/test/java/com/dfbs/app/`.

---

## Build commands (facts)

- Backend compile: `cd backend/dfbs-app` then `.\mvnw.cmd -q clean compile -DskipTests`. Success = exit 0, log BUILD SUCCESS.
- Backend full test: `cd backend/dfbs-app` then `.\mvnw.cmd test`. Success = exit 0, Failures: 0, Errors: 0. Not verified.
- Frontend dev: `cd frontend/dfbs-ui` then `npm run dev` (runs `vite`). Source: `package.json` script "dev". Not verified.
- Frontend build: `cd frontend/dfbs-ui` then `npm run build` (runs `tsc -b && vite build`). Success = build completes, output in `dist/`. Source: `package.json` script "build". Not verified.

---

## Not verified (PM follow-up if needed)

- Full backend test run (`.\mvnw.cmd test`) and frontend production build (`npm run build`) were not executed in this handover; run locally to confirm BUILD SUCCESS.
- Access component: exact list of pages that use it for permission-based UI was not enumerated; only "referenced in codebase" is confirmed.
