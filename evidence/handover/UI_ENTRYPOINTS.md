# UI_ENTRYPOINTS — All current UI entry points

- **As-of:** 2025-02-24 (stage baseline rebuild)
- **Repo:** main
- **Commit:** 328150bd
- **Verification method:** Inspected `App.tsx` (Routes), `BasicLayout.tsx` (MENU_ROUTES_BASE, buildMenuRoutes, adminExtras), `AccountPermissions/AccountsTab.tsx` (create/edit account + Primary Business Role).

**Facts only.** Unverifiable items in "Not verified" section.

---

## Public (no auth)

| Name | Route / path | Prerequisites | Primary actions / outcomes | Related API / module |
|------|---------------|---------------|----------------------------|----------------------|
| Login | `/login` | None | Submit credentials; store token; redirect to `/` | POST `/api/auth/login`; `pages/Login/index.tsx` |
| Public repair | `/public/repair` | None | Create work order (public form) | POST `/api/v1/public/work-orders/create`; `WorkOrderPublicController`; `pages/WorkOrder/Public/index.tsx` |

---

## Protected (AuthGuard + BasicLayout)

All entries below require valid token (AuthGuard). Layout: `BasicLayout.tsx`; menu built from `buildMenuRoutes(isSuperAdmin, permAllowed, isAdminOrSuperAdmin, hasPermission)`.

| Name | Route / path | Prerequisites | Primary actions / outcomes | Related API / module |
|------|---------------|---------------|----------------------------|----------------------|
| Dashboard | `/dashboard` | Auth | View dashboard | `pages/Dashboard/index.tsx` |
| 报价单 (Quotes) | `/quotes` | Auth | List/create/edit quotes; confirm/cancel/submit; items; export | `QuoteController`, `QuoteItemController`, `QuoteWorkflowController`, `QuoteExportController`; `pages/Quote/index.tsx` |
| 发货列表 (Shipments) | `/shipments` | Auth + `shipment.shipments:VIEW` | List/create shipments; workflow (accept/prepare/ship/签收确认/关闭); 标记异常 form + exception records panel; export ticket/receipt; machines; no VIEW → redirect dashboard | `ShipmentController` (list, detail, workflow, exceptions, accept, prepare, ship, complete, tracking, exception, cancel, close); `pages/Shipment/index.tsx` |
| 运输异常 (After-sales) | `/after-sales` | Auth | List after-sales records | `AfterSalesController`; `pages/AfterSales/index.tsx` |
| 运输异常详情 | `/after-sales/:id` | Auth | View/detail one after-sales; submit/receive/process/send-back/complete | `AfterSalesController`; `pages/AfterSales/Detail.tsx` |
| 工单管理 (Work orders) | `/work-orders` | Auth + permission `work_order:VIEW` | List work orders (pool / my-orders) | `WorkOrderController`; `pages/WorkOrder/Internal/index.tsx`; `WorkOrderViewGuard` |
| 工单详情 | `/work-orders/:id` | Auth + `work_order:VIEW` | View/edit work order; create/reject/accept/dispatch/record/parts/sign/complete | `WorkOrderController`; `pages/WorkOrder/Internal/Detail.tsx` |
| 财务 (Finance) | `/finance` | Auth | Finance flows (payments, expenses, statements) | `PaymentController`, `ExpenseController`, `ClaimController`, `AccountStatementController`; `pages/Finance/index.tsx` |
| 库存管理 | `/warehouse/inventory` | Auth | Warehouse inventory | `WhStockController`, `InventoryController`; `pages/Warehouse/Inventory/index.tsx` |
| 补货审批 | `/warehouse/replenish` | Auth | Replenish create/approve; my-pending, my-requests | `WhReplenishController`; `pages/Warehouse/Replenish/index.tsx` |
| 数据导入 | `/import-center` | Auth | Import customers, contracts, models, spare-parts, machines, sim-cards, model-part-lists | `ImportController`; `pages/ImportCenter/index.tsx` |
| 客户 (Customers) | `/customers` | Auth | List/search customers; merge | `CustomerMasterDataController`; `pages/Customer/index.tsx` |
| 合同 | `/master-data/contracts` | Auth | List/create/edit/disable contracts | `ContractController`; `pages/MasterData/Contract/index.tsx` |
| 机器型号 | `/master-data/machine-models`, `/master-data/machine-models/:id` | Auth | List/create/edit/disable; detail | `MachineModelController`; `pages/MasterData/MachineModel/index.tsx`, `Detail.tsx` |
| 零部件 | `/master-data/spare-parts` | Auth | List/create/edit/disable spare parts | `SparePartController`; `pages/MasterData/SparePart/index.tsx` |
| 机器 | `/master-data/machines`, `/master-data/machines/:id` | Auth | List/create/edit/disable; detail, history | `MachineController`; `pages/MasterData/Machine/index.tsx`, `Detail.tsx` |
| SIM卡 | `/master-data/sim-cards`, `/master-data/sim-cards/:id` | Auth | List/create/edit/disable; detail, history | `SimCardController`; `pages/MasterData/SimCard/index.tsx`, `Detail.tsx` |
| 型号BOM | `/master-data/model-part-lists` | Auth | List/draft/publish/conflicts | `ModelPartListController`; `pages/MasterData/ModelPartList/index.tsx` |
| 平台管理 | `/platform/orgs` | Auth + `platform_application.orgs:VIEW` | Platform orgs CRUD | `PlatformOrgController`; `pages/Platform/Org/index.tsx`; `PlatformViewGuard` |
| 申请管理 | `/platform/applications` | Auth + `platform_application.applications:VIEW` | Platform applications page/create/approve/reject | `PlatformAccountApplicationController`; `pages/Platform/Application/index.tsx` |
| 申请历史 / 申请 / 复用 / 核验 / 开卡 | `/platform/applications/history`, `/platform/apply`, `/platform/applications/reuse`, `/platform/applications/verification`, `/platform/applications/sim-activation` | Auth | History list; apply; reuse; verification; sim-activation | `PlatformAccountApplicationController`; `pages/Platform/*` |
| SIM管理 | `/platform/sim-applications` | Auth | SIM applications | `pages/Platform/SimApplication/index.tsx` |
| 数据确认中心 | `/admin/confirmation-center` | Auth | Temp pool confirm/reject; smart-select | `ConfirmationController`; `pages/Admin/ConfirmationCenter/index.tsx` |
| 平台配置 | `/system/platform-config` | Auth | Platform config list/edit/toggle | `PlatformConfigController`; `pages/System/PlatformConfig/index.tsx` |
| 数据字典 | `/admin/data-dictionary` | Auth + Super Admin | Dictionary types list (same as 字典类型) | `DictionaryTypes`; `DictionaryReadController`, `DictionaryTypeAdminController` |
| 层级配置 | `/admin/org-levels` | Auth + Super Admin | Org levels CRUD; reset | `OrgLevelController`; `OrgStructureDevController`; `pages/Admin/OrgLevelConfig/index.tsx` |
| 组织架构 | `/admin/org-tree` | Auth + Super Admin | Org tree; nodes enable/disable/move | `OrgNodeController`, `OrgPersonController`, `OrgPositionController`; `pages/Admin/OrgTree/index.tsx` |
| 变更记录 | `/admin/org-change-logs` | Auth + Super Admin | Change logs list | `OrgChangeLogController`; `pages/Admin/OrgChangeLog/index.tsx` |
| 字典类型 | `/admin/dictionary-types` | Auth + Super Admin | Dict types list/create/edit/enable/disable/delete; 字典项 link; 状态流(迁移规则) for type B | `DictionaryTypeAdminController`, `DictionaryReadController`; `pages/Admin/DictionaryTypes/index.tsx` |
| 字典项管理 | `/admin/dictionary-types/:typeId/items` | Auth + Super Admin | Dict items list/create/edit/reorder/enable/disable | `DictionaryTypeAdminController` (items); `pages/Admin/DictionaryItems/index.tsx` |
| 状态流(迁移规则) | `/admin/dictionary-types/:typeId/transitions` | Auth + Super Admin | Transitions list/add/disable/enable; save; read preview | `DictionaryTypeAdminController` (transitions), `DictionaryReadController` (transitions); `pages/Admin/DictionaryTransitions/index.tsx` |
| 历史显示示例 | `/admin/dictionary-snapshot-demo` | Auth + Super Admin | Snapshot demo create/list records | `DictionarySnapshotDemoController`; `pages/Admin/DictionarySnapshotDemo/index.tsx` |
| 账号与权限 | `/admin/account-permissions` | Auth + Admin or Super Admin | Account list; create account (bind person + **主业务角色**); edit account (update 主业务角色); roles; override; reset password | `AccountPermissionsController` (POST/PUT accounts, GET people, account-list); `AccountsTab.tsx`, `acctPermService.ts`; `AdminOrSuperAdminGuard` |
| 角色与权限 | `/admin/roles-permissions` | Auth + Perm allowlist (Perm Super Admin) | Roles/permissions management. Not in left menu (removed per menu cleanup); reachable by direct URL. | `PermAdminController`; `pages/Admin/RolesPermissions/index.tsx`; `PermSuperAdminGuard` |

---

## Navigation (menu)

- Default index: `/` redirects to `/dashboard`. `App.tsx` L63.
- `/logistics` redirects to `/shipments`; `/after-sales-service` to `/work-orders`; `/master-data` to `/master-data/contracts`; `/platform` to `/platform/applications`; `/admin` to `/admin/confirmation-center`. `App.tsx` L67, L71, L78, L88, L98.
- Wildcard: `*` redirects to `/`. `App.tsx` L111.

---

## Reality semantics

- **Account-permissions page:** Create flow requires selecting an existing org person and one Primary Business Role (必选); edit flow in account detail drawer allows updating Primary Business Role and saving via PUT `/api/v1/admin/account-permissions/accounts/{userId}`. List and detail show primaryBusinessRole. No mock-person layer; identity basis is real account + bound person + primary business role.
- **Route vs usable:** Routes under platform/shipments/work-orders may show "无权限" or redirect if user lacks effective permission; admin/super-admin bypass is whitelist-based (see STATE_SNAPSHOT). Seeing a menu entry does not guarantee all actions succeed without the right keys or Super Admin.

---

## Reuse status

Per entry: see STATE_SNAPSHOT. Account-permissions: Reusable as-is for create/edit account with Primary Business Role. Contract page: Exists but incomplete (no review flow).

---

## Decision-risk notes

- **Menu visible ≠ page usable:** Menu visibility can be restored for admin/super-admin via bypass, but action-level 403 can still occur if the action key is not in the bypass whitelist.
- **Contract list/detail:** Exists at `/master-data/contracts`; no contract review, no ownership fields (initiator/assigned/current handler) in repo.

---

## Not verified

- Access component: which pages use it for permission-based UI was not enumerated.
