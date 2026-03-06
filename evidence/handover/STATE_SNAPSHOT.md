# STATE_SNAPSHOT — What works now / known limitations

- **As-of:** 2025-02-24 20:00
- **Repo:** main
- **Commit:** 983df8e7
- **Verification method:** Inspected `App.tsx`, `BasicLayout.tsx`, `PermEnforcementService.java`, controllers under `backend/dfbs-app/src/main/java/com/dfbs/app/interfaces/`, migrations under `backend/dfbs-app/src/main/resources/db/migration/`.

**Facts only.** Unverifiable items in "Not verified" section.

---

## What works now (user-visible capabilities)

| Capability | Route / entry | Evidence |
|------------|---------------|----------|
| Login | `/login` | `frontend/dfbs-ui/src/App.tsx` L59, `pages/Login/index.tsx` |
| Dashboard | `/dashboard` | `App.tsx` L64, `pages/Dashboard/index.tsx` |
| Customers | `/customers` | `App.tsx` L65, `pages/Customer/index.tsx` |
| Quotes | `/quotes` | `App.tsx` L66, `pages/Quote/index.tsx` |
| Shipments | `/shipments` | `App.tsx` L70, `pages/Shipment/index.tsx`; workflow (accept/prepare/ship/签收确认/关闭), 标记异常 form + exception records panel; requires `shipment.shipments:VIEW` (Step-03). |
| After-sales list/detail | `/after-sales`, `/after-sales/:id` | `App.tsx` L69–70, `pages/AfterSales/index.tsx`, `Detail.tsx` |
| Work orders list/detail | `/work-orders`, `/work-orders/:id` | `App.tsx` L72–73, `WorkOrderViewGuard`; `pages/WorkOrder/Internal/index.tsx`, `Detail.tsx` |
| Public repair (no auth) | `/public/repair` | `App.tsx` L60, `pages/WorkOrder/Public/index.tsx` |
| Finance | `/finance` | `App.tsx` L74, `pages/Finance/index.tsx` |
| Warehouse inventory | `/warehouse/inventory` | `App.tsx` L75, `pages/Warehouse/Inventory/index.tsx` |
| Warehouse replenish | `/warehouse/replenish` | `App.tsx` L76, `pages/Warehouse/Replenish/index.tsx` |
| Import center | `/import-center` | `App.tsx` L77, `pages/ImportCenter/index.tsx` |
| Master data: contracts | `/master-data/contracts` | `App.tsx` L79, `pages/MasterData/Contract/index.tsx` |
| Master data: machine-models, spare-parts, machines, sim-cards, model-part-lists | `/master-data/machine-models`, `/master-data/spare-parts`, `/master-data/machines`, `/master-data/sim-cards`, `/master-data/model-part-lists` | `App.tsx` L80–86, `pages/MasterData/*` |
| Platform orgs | `/platform/orgs` | `App.tsx` L89, `PlatformViewGuard` requiredPermission `platform_application.orgs:VIEW`; `pages/Platform/Org/index.tsx` |
| Platform applications | `/platform/applications` | `App.tsx` L90, `PlatformViewGuard` requiredPermission `platform_application.applications:VIEW`; `pages/Platform/Application/index.tsx` |
| Platform apply, history, reuse, verification, sim-activation | `/platform/apply`, `/platform/applications/history`, `/platform/applications/reuse`, `/platform/applications/verification`, `/platform/applications/sim-activation` | `App.tsx` L91–95 |
| Platform sim-applications | `/platform/sim-applications` | `App.tsx` L97, `pages/Platform/SimApplication/index.tsx` |
| Admin confirmation center | `/admin/confirmation-center` | `App.tsx` L99, `pages/Admin/ConfirmationCenter/index.tsx` |
| Admin data dictionary (Super Admin) | `/admin/data-dictionary` | `App.tsx` L100, `SuperAdminGuard`; renders `DictionaryTypes` |
| Admin org-levels | `/admin/org-levels` | `App.tsx` L101, `SuperAdminGuard`; `pages/Admin/OrgLevelConfig/index.tsx` |
| Admin org-tree | `/admin/org-tree` | `App.tsx` L102, `SuperAdminGuard`; `pages/Admin/OrgTree/index.tsx` |
| Admin org-change-logs | `/admin/org-change-logs` | `App.tsx` L103, `SuperAdminGuard`; `pages/Admin/OrgChangeLog/index.tsx` |
| Admin dictionary-types | `/admin/dictionary-types` | `App.tsx` L104, `SuperAdminGuard`; `pages/Admin/DictionaryTypes/index.tsx` |
| Admin dictionary-types items | `/admin/dictionary-types/:typeId/items` | `App.tsx` L105, `SuperAdminGuard`; `pages/Admin/DictionaryItems/index.tsx` |
| Admin dictionary-types transitions | `/admin/dictionary-types/:typeId/transitions` | `App.tsx` L106, `SuperAdminGuard`; `pages/Admin/DictionaryTransitions/index.tsx` |
| Admin dictionary-snapshot-demo | `/admin/dictionary-snapshot-demo` | `App.tsx` L107, `SuperAdminGuard`; `pages/Admin/DictionarySnapshotDemo/index.tsx` |
| Admin account-permissions | `/admin/account-permissions` | `App.tsx` L108, `AdminOrSuperAdminGuard`; `pages/Admin/AccountPermissions/index.tsx` |
| Admin roles-permissions | `/admin/roles-permissions` | `App.tsx` L109, `PermSuperAdminGuard`; `pages/Admin/RolesPermissions/index.tsx` |
| System platform-config | `/system/platform-config` | `App.tsx` L110, `pages/System/PlatformConfig/index.tsx` |
| Test data cleaner (Super Admin, header) | Header link "测试数据清理器" | `BasicLayout.tsx`, `TestDataCleanerModal`; only when `useIsSuperAdmin()` true |
| UI role simulator (admin/super-admin only) | Top bar: Simulated Role dropdown, badge "SIMULATING: &lt;role&gt;", disclaimer "仅界面模拟，不改变实际权限", link "角色-界面矩阵（查看）" | `BasicLayout.tsx`; `useSimulatedRoleStore`; visible when `userInfoRefreshedFromServer && userInfo` has ADMIN or SUPER_ADMIN. Left-nav filtered by `filterMenuBySimulatedRole`. Action gating (disable + tooltip "该角色不可操作") on Shipment, Work Order, Platform Org, Platform Application per `roleToUiGatingMatrix.ts`. |
| Admin/super-admin baseline bypass (backend) | Page entry + selected actions | `PermEnforcementService.java`: whitelist bypass for VIEW keys (shipment.shipments:VIEW, platform_application.orgs:VIEW, platform_application.applications:VIEW, work_order:VIEW) and work_order:CREATE. Platform Application action keys (CREATE, SUBMIT, APPROVE, REJECT, CLOSE) are not bypassed; admin without those effective keys get 403 on 提交申请 / 提交至管理员 / 关闭申请 / 通过 / 驳回. |

---

## Known limitations (facts)

- Frontend production build (`npm run build`) fails with existing TypeScript errors in multiple files (e.g. `OrgTreeSelect.tsx`, `AccountPermissions/BizPermCatalogMaintenance.tsx`, `OrgTree/index.tsx`, `RolesPermissions/index.tsx`, `AfterSales/index.tsx`, `ImportCenter/index.tsx`, others). Source: `npm run build` runs `tsc -b && vite build`; errors reported by tsc.
- Backend full test run (`.\mvnw.cmd test`) was not executed in this handover; BUILD SUCCESS for tests is Not verified.
- Docs under `legacy/` (if any) are not authoritative per repo convention.

---

## Feature flags / config gates

None found in application code. No symbols `featureFlag`, `feature.`, or config gate for feature toggles under `backend/dfbs-app/src` or `frontend/dfbs-ui/src` (excluding node_modules).

---

## Not verified

- Full backend test run and frontend production build not executed in this handover.
- Access component: exact pages that use it for permission-based UI were not enumerated (only presence in codebase confirmed).
