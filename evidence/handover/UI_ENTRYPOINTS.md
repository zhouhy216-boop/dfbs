# UI_ENTRYPOINTS — Pages and where to enter them

- **As-of:** 2026-02-09 14:00
- **Repo:** main
- **Commit:** 1df603c5
- **Verification method:** `frontend/dfbs-ui/src/App.tsx`, `frontend/dfbs-ui/src/layouts/BasicLayout.tsx` (routes and menu config).

**Facts only.** Authoritative source: `App.tsx` Routes and `BasicLayout.tsx` menu.

---

## All UI entry points

| Name | Route / path | Prerequisites | Primary actions / outcomes | Related API / module pointer |
|------|--------------|---------------|---------------------------|------------------------------|
| Login | `/login` | None | Authenticate; token stored; redirect to dashboard | `AuthController` POST `/api/auth/login` |
| Public repair | `/public/repair` | None | Create work order (no auth) | `WorkOrderPublicController` POST `/api/v1/public/work-orders/create` |
| Dashboard | `/dashboard` | AuthGuard (token) | Landing after login | — |
| Customers | `/customers` | AuthGuard | Customer list | `CustomerMasterDataController` GET `/api/v1/customers` |
| Quotes | `/quotes` | AuthGuard | Quote list, CRUD, workflow, items, payment, void | `QuoteController`, `QuoteItemController`, `QuoteWorkflowController`, `QuoteVoidController`, `PaymentController` (quote) |
| Shipments | `/shipments` | AuthGuard | Shipment list, create/accept/ship/complete, machines, export, damages | `ShipmentController`, `DamageController` |
| After-sales list | `/after-sales` | AuthGuard | After-sales list | `AfterSalesController` GET `/api/v1/after-sales` |
| After-sales detail | `/after-sales/:id` | AuthGuard | After-sales detail | `AfterSalesController` GET `/api/v1/after-sales/{id}` |
| Work orders list | `/work-orders` | AuthGuard | Internal work order list | `WorkOrderController` GET `/api/v1/work-orders/pool`, `my-orders` |
| Work order detail | `/work-orders/:id` | AuthGuard | Work order detail | `WorkOrderController` GET `/api/v1/work-orders/{id}` |
| Finance | `/finance` | AuthGuard | Finance list/views | Statements, payments (see API_SURFACE) |
| Warehouse inventory | `/warehouse/inventory` | AuthGuard | Inventory | `WhStockController`, `InventoryController` |
| Warehouse replenish | `/warehouse/replenish` | AuthGuard | Replenish requests | `WhReplenishController` |
| Import center | `/import-center` | AuthGuard | Bulk import (customers, contracts, models, spare-parts, machines, sim-cards, model-part-lists) | `ImportController` POST `/api/v1/imports/*` |
| Master data – contracts | `/master-data/contracts` | AuthGuard | Contract CRUD | `ContractController` |
| Master data – machine models | `/master-data/machine-models`, `/master-data/machine-models/:id` | AuthGuard | Model list/detail | `MachineModelController` |
| Master data – spare parts | `/master-data/spare-parts` | AuthGuard | Spare parts CRUD | `SparePartController` |
| Master data – machines | `/master-data/machines`, `/master-data/machines/:id` | AuthGuard | Machines list/detail | `MachineController` |
| Master data – sim cards | `/master-data/sim-cards`, `/master-data/sim-cards/:id` | AuthGuard | Sim cards list/detail | `SimCardController` |
| Master data – model part lists | `/master-data/model-part-lists` | AuthGuard | Model part lists CRUD, draft/publish, conflicts | `ModelPartListController` |
| Platform – orgs | `/platform/orgs` | AuthGuard | Platform orgs CRUD, duplicate check | `PlatformOrgController` |
| Platform – applications | `/platform/applications` | AuthGuard | Account applications list, create, planner submit, approve/reject/close | `PlatformAccountApplicationController` |
| Platform – applications history | `/platform/applications/history` | AuthGuard | Applications history | Same controller `/page` |
| Platform – apply | `/platform/apply` | AuthGuard | Apply flow | Same controller `/create` |
| Platform – reuse / verification / sim-activation | `/platform/applications/reuse`, `verification`, `sim-activation` | AuthGuard | Placeholder pages | — |
| Platform – sim applications | `/platform/sim-applications` | AuthGuard | Sim applications | — |
| Admin – confirmation center | `/admin/confirmation-center` | AuthGuard | Temp pool confirm/reject | `ConfirmationController` |
| Admin – org-levels (Super Admin) | `/admin/org-levels` | AuthGuard + SuperAdminGuard | Level config, reorder, reset | `OrgLevelController`, `OrgStructureDevController` |
| Admin – org-tree (Super Admin) | `/admin/org-tree` | AuthGuard + SuperAdminGuard | Org tree, nodes, people, positions | `OrgNodeController`, `OrgPersonController`, `OrgPositionController` |
| Admin – org-change-logs (Super Admin) | `/admin/org-change-logs` | AuthGuard + SuperAdminGuard | Change logs list | `OrgChangeLogController` |
| System – platform config | `/system/platform-config` | AuthGuard | Platform config CRUD, toggle | `PlatformConfigController` |

---

## Redirects (from App.tsx)

- `/` → `/dashboard`
- `/logistics` → `/shipments`
- `/after-sales-service` → `/work-orders`
- `/master-data` → `/master-data/contracts`
- `/platform` → `/platform/applications`
- `/admin` → `/admin/confirmation-center`
- `/platform/applications/enterprise-direct` → `/platform/apply?source=enterprise`
- `*` → `/`

---

## Navigation (menu)

Menu built from `BasicLayout.tsx`: `MENU_ROUTES_BASE` + when `useIsSuperAdmin()` is true, `ORG_STRUCTURE_MENU` appended under 系统 (admin-group). Menu labels: Dashboard, 报价单, 物流管理 (发货列表, 运输异常), 售后服务 (工单管理), 财务, 库存管理, 补货审批, 数据导入, 主数据 (客户, 合同, 机器, 机器型号, 型号BOM, 零部件, SIM卡), 平台&网卡管理 (平台管理, SIM管理, 申请管理), 系统 (数据确认中心, 平台配置 [, 层级配置, 组织架构, 变更记录 ]).

---

## Not verified

- Exact role/permission checks per route (beyond SuperAdminGuard) not enumerated from backend; auth is token-based via AuthGuard.
