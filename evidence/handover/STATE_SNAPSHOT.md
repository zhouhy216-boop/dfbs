# STATE_SNAPSHOT — What works now / known limitations

- **As-of:** 2025-02-24 14:00
- **Repo:** main
- **Commit:** 23467d7d
- **Verification method:** Inspected `frontend/dfbs-ui/src/App.tsx`, `layouts/BasicLayout.tsx`, backend `interfaces/perm/AccountPermissionsController.java`, `application.yml`; grep `@RequestMapping` under `backend/dfbs-app/src/main/java/com/dfbs/app/interfaces/`; list `db/migration/V*.sql`.

**Facts only.** Unverifiable items in "Not verified" section.

---

## What works now (user-visible capabilities)

| Capability | Route / entry | Evidence |
|------------|---------------|----------|
| Login | `/login` | `frontend/dfbs-ui/src/App.tsx` L59, `pages/Login/index.tsx` |
| Dashboard | `/dashboard` | `App.tsx` L64, `pages/Dashboard/index.tsx` |
| Customers | `/customers` | `App.tsx` L65, `pages/Customer/index.tsx` |
| Quotes | `/quotes` | `App.tsx` L66, `pages/Quote/index.tsx` |
| Shipments | `/shipments` | `App.tsx` L70, `pages/Shipment/index.tsx`; workflow; requires `shipment.shipments:VIEW` or admin bypass. |
| After-sales list/detail | `/after-sales`, `/after-sales/:id` | `App.tsx` L69–70, `pages/AfterSales/index.tsx`, `Detail.tsx` |
| Work orders list/detail | `/work-orders`, `/work-orders/:id` | `App.tsx` L72–73, `WorkOrderViewGuard`; `pages/WorkOrder/Internal/index.tsx`, `Detail.tsx` |
| Public repair (no auth) | `/public/repair` | `App.tsx` L60, `pages/WorkOrder/Public/index.tsx` |
| Finance | `/finance` | `App.tsx` L74, `pages/Finance/index.tsx` |
| Warehouse inventory / replenish | `/warehouse/inventory`, `/warehouse/replenish` | `App.tsx` L75–76, `pages/Warehouse/*` |
| Import center | `/import-center` | `App.tsx` L77, `pages/ImportCenter/index.tsx` |
| Master data: contracts | `/master-data/contracts` | `App.tsx` L79, `pages/MasterData/Contract/index.tsx` |
| Master data: machine-models, spare-parts, machines, sim-cards, model-part-lists | `/master-data/*` | `App.tsx` L80–86, `pages/MasterData/*` |
| Platform orgs | `/platform/orgs` | `App.tsx` L89, `PlatformViewGuard`; `pages/Platform/Org/index.tsx` |
| Platform applications / apply / history / reuse / verification / sim-activation | `/platform/applications`, `/platform/apply`, etc. | `App.tsx` L90–95; `pages/Platform/Application/index.tsx` |
| Platform sim-applications | `/platform/sim-applications` | `App.tsx` L97 |
| Admin confirmation center | `/admin/confirmation-center` | `App.tsx` L99 |
| Admin data dictionary, org-levels, org-tree, org-change-logs | `/admin/data-dictionary`, `/admin/org-levels`, `/admin/org-tree`, `/admin/org-change-logs` | `App.tsx` L100–103, `SuperAdminGuard` |
| Admin dictionary-types, items, transitions, snapshot-demo | `/admin/dictionary-types/*` | `App.tsx` L104–107, `SuperAdminGuard` |
| Admin account-permissions | `/admin/account-permissions` | `App.tsx` L108, `AdminOrSuperAdminGuard`; create account (bind person + **Primary Business Role**), edit account (update Primary Business Role), list/detail; `AccountsTab.tsx`, `acctPermService.ts` (createAccount, updateAccount). |
| Admin roles-permissions | `/admin/roles-permissions` | `App.tsx` L109, `PermSuperAdminGuard` |
| System platform-config | `/system/platform-config` | `App.tsx` L110 |
| Test data cleaner (Super Admin, header) | Header link "测试数据清理器" | `BasicLayout.tsx`, `TestDataCleanerModal`; only when `useIsSuperAdmin()` true |
| UI role simulator (admin/super-admin only) | Top bar: badge "模拟中：{角色}" or "当前：未模拟"; link "验收边界说明"; dropdown "角色模拟"; link "角色-界面矩阵（查看）" | `BasicLayout.tsx`; `useSimulatedRoleStore`; Chinese business roles only (`SIMULATOR_BUSINESS_ROLES_ZH`); visible when `userInfoRefreshedFromServer && userInfo` has ADMIN or SUPER_ADMIN. Left-nav filtered by `filterMenuBySimulatedRole`. Action gating (disable + tooltip "该角色不可操作") on Shipment, Work Order, Platform Org, Platform Application per `roleToUiGatingMatrix.ts`. Matrix modal: first-batch anchored table (M04/M05/M08) + system/global table. |
| Admin/super-admin backend bypass | Page entry + first-batch actions | `PermEnforcementService.java`: whitelist bypass for VIEW keys, work_order:CREATE, and `DEV_STAGE_FIRST_BATCH_ACTION_BYPASS` (shipment/work_order/platform application/org action keys). Non–Super Admin users are not bypassed. |

---

## Reality semantics

- **Login/auth:** Token in localStorage; `GET /api/auth/me` used to refresh server-authoritative roles on protected routes (`AuthGuard` in `App.tsx`). No role in token payload; roles come from backend user/authorities.
- **Organization/person:** `org_person`, `person_affiliation`, `org_node`; persons listed for account binding via `GET /api/v1/admin/account-permissions/people`. Org tree and positions under `/admin/org-tree`. Person is the real identity basis for account creation.
- **Account management:** Accounts created by binding to an existing org person; one **Primary Business Role** per account (field `app_user.primary_business_role`, migration V0087). Create: POST `/api/v1/admin/account-permissions/accounts` (orgPersonId, username, nickname, roleTemplateId, primaryBusinessRole). Update profile: PUT `/api/v1/admin/account-permissions/accounts/{userId}` (nickname, primaryBusinessRole). List/detail show primaryBusinessRole. Real accounts only; no mock-person layer.
- **Permissions/bypass:** Effective permissions = (template ∪ add) \ remove. Admin/Super Admin bypass is **whitelist-based** in `PermEnforcementService.java`: only VIEW keys for shipments/platform orgs/platform applications/work_order, work_order:CREATE, and the fixed set in `DEV_STAGE_FIRST_BATCH_ACTION_BYPASS`. Any other action key requires the user to have it in effective keys or request fails with 403.
- **Role simulator / mock account:** Simulator is **UI-only**: dropdown and badge show simulated business role (Chinese labels); left menu and page actions are gated by `roleToUiGatingMatrix.ts` (menus hide; buttons disable with tooltip). No backend identity change. Real account identity (Primary Business Role) is stored on `app_user` and used for future downstream flows (e.g. Contract Review not yet in repo).

---

## Reuse status

| Area | Status | Note |
|------|--------|------|
| Auth / login / me | Reusable as-is | Token + /auth/me; roles from server |
| Org structure / person | Reusable as-is | Person list for binding; org tree, positions |
| Account create/edit (bind person + Primary Business Role) | Reusable as-is | Create + PUT accounts; list/detail show primaryBusinessRole |
| Permission override (template + add/remove) | Reusable as-is | GET/PUT override; effective keys resolution |
| Role simulator (UI) | Reusable as-is | Chinese roles; menu/action gating; matrix modal |
| First-batch pages (shipments, work-orders, platform) | Reusable with small patch | Frontend may need isAdminOrSuperAdmin for action visibility; backend bypass already in place for Super Admin |
| Contract module | Exists but incomplete | List/detail/create at `/master-data/contracts`; no contract review flow, no initiator/assigned/current handler in repo |
| shared/form (form wheel) | Reusable as-is | Platform Application create modal only; Contract Review V1 consumer not in repo |
| UnifiedProTable / table wheel | Reusable as-is | List/detail tables use tableKey; column state and widths persisted |
| Contract review (M02) | Not present | No route or review record in repo; anchor pack marks 评审协同 as “暂无” |

---

## Decision-risk notes

- **Route exists ≠ feature complete:** Many routes exist but page may redirect or show “无权限” if user lacks effective permission; admin/super-admin see menu only when VIEW bypass applies; action buttons still require corresponding action keys or Super Admin first-batch bypass.
- **Simulator is not real identity:** Simulated role only changes UI visibility/disable state. Backend never sees simulated role; all API calls use real user and effective permissions.
- **Primary Business Role is real:** Stored on `app_user`; create requires it; edit can update it. No contract review flow in repo yet to consume it.
- **Build success:** Frontend `npm run build` currently fails due to existing TypeScript errors in unrelated files; backend test run not executed in this handover.

---

## Known limitations (facts)

- Frontend production build (`npm run build`) fails with existing TypeScript errors in multiple files (e.g. `OrgTreeSelect.tsx`, `BizPermCatalogMaintenance.tsx`, `OrgTree/index.tsx`, `RolesPermissions/index.tsx`, `AfterSales/index.tsx`, `ImportCenter/index.tsx`, `Platform/Application/index.tsx`, `Quote/index.tsx`, others). Source: `npm run build` runs `tsc -b && vite build`.
- Backend full test run was not executed in this handover; BUILD SUCCESS for tests is Not verified.
- Contract review (initiator, assigned Business Planning person, current handler) does not exist in repo; no review list/popup or review record entity.

---

## Feature flags / config gates

None found. No feature-flag or config-gate symbols under `backend/dfbs-app/src` or `frontend/dfbs-ui/src` (excluding node_modules).

---

## Not verified

- Full backend test run and frontend production build not executed in this handover.
- Exact set of pages using permission guards (e.g. hasPermission) for UI hide/disable was not fully enumerated.
