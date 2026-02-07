# STATE_SNAPSHOT — What the system does (vNext handover)

**Facts only.** Unknowns marked "Unknown (not verified)".

---

## What the system currently does (by module/page)

- **Login** (`/login`): User authenticates; token stored in localStorage; redirect to `/dashboard`.
- **Dashboard** (`/dashboard`): Entry after login; content Unknown (not verified).
- **Customers** (`/customers`): Customer master data list/search; uses ProTable + `request` + `toProTableResult`.
- **Quotes** (`/quotes`): Quote CRUD, workflow (submit/audit), items, attachments; export; payment workflow; void flow.
- **Shipments** (`/shipments`): Shipment create/accept/ship/complete; machines; export ticket/receipt; damages.
- **Finance** (`/finance`): Finance-related list/views; uses ProTable + adapters.
- **After-sales** (`/after-sales`, `/after-sales/:id`): After-sales records list and detail.
- **Work orders** (`/work-orders`, `/work-orders/:id`): Internal work order list and detail; public repair at `/public/repair`.
- **Warehouse** (`/warehouse/inventory`, `/warehouse/replenish`): Inventory and replenish requests.
- **Import center** (`/import-center`): Bulk import for customers, contracts, models, spare-parts, machines, sim-cards, model-part-lists.
- **Master data**: Contracts, machine models, spare parts, machines, sim cards, model-part-lists (each with list/detail where applicable).
- **Platform** (`/platform/applications`, `/platform/orgs`, `/platform/sim-applications`): Platform account applications (create/planner confirm/admin approve/reject), platform orgs CRUD, sim applications.
- **Admin** (`/admin/confirmation-center`): Confirmation center.
- **System** (`/system/platform-config`): Platform config options/rules.

---

## What it does NOT do (known limitations)

- Legacy docs under `legacy/` are not authoritative; do not use for requirements.
- Full test suite and frontend build: Unknown (not verified) in this handover—run in your environment to confirm BUILD SUCCESS.

---

## Key entry points (where a new engineer starts)

- **Frontend app**: `frontend/dfbs-ui/src/main.tsx` → `App.tsx`; routes in `App.tsx`.
- **Frontend layout/menu**: `frontend/dfbs-ui/src/layouts/BasicLayout.tsx`.
- **Backend app**: `backend/dfbs-app/src/main/java/com/dfbs/app/DfbsAppApplication.java`.
- **Backend REST**: `backend/dfbs-app/src/main/java/com/dfbs/app/interfaces/` (controllers by domain).
- **DB migrations**: `backend/dfbs-app/src/main/resources/db/migration/` (V0001__*.sql through V0060__*.sql).
- **Tests**: `backend/dfbs-app/src/test/java/com/dfbs/app/`.

---

## Build/test evidence (key lines only)

Facts only. CEO has reported backend BUILD SUCCESS in their environment; that is not asserted here as this handover’s own build proof. If Delivery PM provides CEO-gate snippets, include them explicitly labeled: **"CEO gate evidence (provided by CEO): &lt;snippet&gt;"**

- **Backend compile**: `cd backend/dfbs-app && ./mvnw.cmd -q clean compile -DskipTests` → Self-produced in handover environment: exit 0, BUILD SUCCESS (compile completes without error). No key log line pasted; run locally to reproduce.
- **Backend full test**: `./mvnw.cmd test` → Not verified in this environment. When verified (e.g. by CEO), key evidence line is: `[INFO] BUILD SUCCESS` (and tests summary). If Delivery PM provides CEO-gate backend snippet, it will be added above as "CEO gate evidence (provided by CEO): ...".
- **Frontend — dev server vs production build**:
  - **Dev server**: `npm run dev` (runs `vite`) → "dev server ready" when Vite reports local URL. Not verified in this handover environment.
  - **Production build**: `npm run build` (runs `tsc -b && vite build`) → Success when build completes and outputs to `dist/`. Not verified in this handover environment. If you can self-produce success, paste only the key success line(s); do not list speculative TS errors unless you observed them. If Delivery PM provides CEO-gate frontend build snippet, it will be added as "CEO gate evidence (provided by CEO): ...".
