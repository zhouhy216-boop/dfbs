# ROLESIM-260306-001-03-A-FIX2 — Evidence-only audit

**Ticket:** Evidence-only audit of real left-nav labels, route mapping, and simulated-role menu effect.  
**No code changes.** Source: repo code as of audit.

---

## 1) Rendered menu tree (label → path)

From `BasicLayout.tsx` `MENU_ROUTES_BASE` + `buildMenuRoutes()` output. ProLayout receives this tree (after simulator filter) via `route={{ routes: menuRoutes }}` and `menuDataRender={() => menuRoutes}`. The **name** field is what appears as the left-nav label in the UI.

| Level | Chinese label (name) | Route path | Notes |
|-------|----------------------|------------|--------|
| Top   | Dashboard            | /dashboard | |
| Top   | 报价单               | /quotes | |
| Top   | 物流管理             | /logistics | Group |
| Child | 发货列表             | /shipments | Under 物流管理 |
| Child | 运输异常             | /after-sales | Under 物流管理 |
| Top   | 售后服务             | /after-sales-service | Group |
| Child | 工单管理             | /work-orders | Under 售后服务 |
| Top   | 财务                 | /finance | |
| Top   | 库存管理             | /warehouse/inventory | |
| Top   | 补货审批             | /warehouse/replenish | |
| Top   | 数据导入             | /import-center | |
| Top   | 主数据               | /master-data | Group |
| Child | 客户                 | /customers | |
| Child | 合同                 | /master-data/contracts | |
| Child | 机器                 | /master-data/machines | |
| Child | 机器型号             | /master-data/machine-models | |
| Child | 型号BOM              | /master-data/model-part-lists | |
| Child | 零部件               | /master-data/spare-parts | |
| Child | SIM卡                | /master-data/sim-cards | |
| Top   | 平台&网卡管理        | /platform | Group |
| Child | 平台管理             | /platform/orgs | |
| Child | SIM管理              | /platform/sim-applications | |
| Child | 申请管理             | /platform/applications | |
| Top   | 系统                 | /admin | Group |
| Child | 数据确认中心         | /admin/confirmation-center | |
| Child | 平台配置             | /system/platform-config | |
| Child | 账号与权限           | /admin/account-permissions | When isAdminOrSuperAdmin |
| Child | 角色与权限           | /admin/roles-permissions | When permAllowed |
| Child | 数据字典             | /admin/data-dictionary | When isSuperAdmin |
| Child | 层级配置             | /admin/org-levels | When isSuperAdmin |
| Child | 组织架构             | /admin/org-tree | When isSuperAdmin |
| Child | 变更记录             | /admin/org-change-logs | When isSuperAdmin |
| Child | 字典类型             | /admin/dictionary-types | When isSuperAdmin |
| Child | 历史显示示例         | /admin/dictionary-snapshot-demo | When isSuperAdmin |

Baseline (before simulator filter) can further reduce items when real permissions apply: e.g. 平台管理 requires `platform_application.orgs:VIEW`, 申请管理 requires `platform_application.applications:VIEW`, 工单管理 requires `work_order:VIEW` (see `buildMenuRoutes` in BasicLayout.tsx).

---

## 2) Seeded matrix entry → visible Chinese menu label

From `roleToUiGatingMatrix.ts` routeAnchors and BasicLayout menu paths.

| Matrix entry id              | routeAnchors (relevant)     | Visible Chinese label(s) in left nav |
|-----------------------------|-----------------------------|--------------------------------------|
| shipments-page              | /shipments                 | **发货列表** (under 物流管理)        |
| work-orders-page            | /work-orders, /work-orders/:id | **工单管理** (under 售后服务)     |
| platform-orgs-page         | /platform/orgs             | **平台管理** (under 平台&网卡管理)   |
| platform-applications-page | /platform/applications, /platform/apply | **申请管理** (under 平台&网卡管理) |
| admin-data-dictionary      | /admin/data-dictionary     | **数据字典** (under 系统)            |
| admin-dictionary-types     | /admin/dictionary-types, … | **字典类型** (under 系统)           |
| admin-org-tree             | /admin/org-tree, org-levels, org-change-logs | **组织架构**, **层级配置**, **变更记录** (under 系统) |
| admin-account-permissions  | /admin/account-permissions | **账号与权限** (under 系统)          |
| admin-roles-permissions    | /admin/roles-permissions   | **角色与权限** (under 系统)          |

Routes present in the menu but **not** in the matrix (fallback = always show when simulating): 运输异常 (/after-sales), SIM管理 (/platform/sim-applications), 数据确认中心, 平台配置, Dashboard, 报价单, 财务, etc.

---

## 3) Per-role visible diff (from filter logic and current matrix)

Source: `isRouteVisibleForSimulatedRole`, `filterMenuBySimulatedRole`, and `ROLE_TO_UI_GATING_MATRIX` (post-FIX1). Filter only runs when `simulatedRole` is set (non-null and not `'__none__'`). It checks **leaf** `path`; groups are kept if at least one child remains.

- **None (simulatedRole null or __none__):** Filter is no-op. Menu = full baseline (per real permissions).
- **Super Admin:** All seeded entries include Super Admin in allowedSimulatedRoleSet → no seeded item removed.
- **Admin:** All seeded entries include Admin → no seeded item removed.
- **Operator:**  
  - platform-applications-page allows only ['Super Admin', 'Admin'] → **申请管理** hidden.  
  - 平台管理, 发货列表, 工单管理 allowed → stay.  
  - 平台&网卡管理: 平台管理 + SIM管理 remain (申请管理 removed).
- **Viewer:**  
  - shipments-page, work-orders-page, platform-orgs-page, platform-applications-page all exclude Viewer → **发货列表**, **工单管理**, **平台管理**, **申请管理** hidden.  
  - 物流管理: only 运输异常 remains (not in matrix → shown).  
  - 售后服务: 工单管理 removed → no children left → **售后服务** group removed.  
  - 平台&网卡管理: only SIM管理 remains (平台管理 + 申请管理 removed).

So with **current matrix (after FIX1)**:
- **Viewer:** 发货列表, 工单管理, 平台管理, 申请管理 disappear; 售后服务 group disappears.
- **Operator:** 申请管理 disappears.

---

## 4) Why CEO screenshots showed no visible change for non-system items

- **Acceptance wording:** Descriptions used route paths (/shipments, /work-orders, /platform/applications) instead of the actual UI labels (发货列表, 工单管理, 申请管理). That made it harder to map “claimed seeded areas” to what appears in the left nav.
- **Matrix data before FIX1:** For shipments-page, work-orders-page, platform-orgs-page, platform-applications-page, `allowedSimulatedRoleSet` was `['Super Admin', 'Admin', 'Operator', 'Viewer']`. So for **any** simulated role, `isRouteVisibleForSimulatedRole(path, role)` was true for those paths. The filter never removed 发货列表, 工单管理, 平台管理, 申请管理. So non-system items did not change with role in the UI.
- **Wiring:** The same `menuRoutes` (buildMenuRoutes → filterMenuBySimulatedRole) is passed to ProLayout; the filter runs on the same tree that contains /shipments, /work-orders, /platform/orgs, /platform/applications. So the implementation **was** applied to the correct menu paths; the lack of visible change was due to **matrix data**, not to wrong paths or wrong tree.

**Conclusion on cause:** Non-admin seeded routes did not visibly change because the matrix **allowed all simulator roles** for those entries, so the filter had no effect on them. System/admin entries had restricted allowed sets, so only 系统 group changed. After FIX1, restricted allowedSimulatedRoleSet for the four non-admin seeded entries makes 发货列表, 工单管理, 平台管理, 申请管理 respond to Viewer/Operator in the left nav.

---

## 5) Fact-only conclusion

- **Prior implementation claim vs runtime-visible behavior:** **Partially matched.** The filter and path→menu mapping were correct and applied to the rendered menu. The claim that “seeded non-admin menu areas respond to simulated-role switch” was **not** true at the time of the CEO screenshots, because the matrix data did not differentiate by role for those entries. So the claim was **overstated** for the pre-FIX1 state.
- **Which seeded entries visibly change today (from code, after FIX1):**  
  - **By simulated role:** 发货列表, 工单管理, 平台管理, 申请管理 (hidden for Viewer; 申请管理 also hidden for Operator).  
  - **系统 group:** 数据字典, 字典类型, 组织架构/层级配置/变更记录, 账号与权限, 角色与权限 continue to show/hide by role as before (Super Admin only vs Admin vs others).
- **Which do not visibly change:**  
  - Any route not in the matrix (e.g. 运输异常, SIM管理, 数据确认中心, 平台配置, Dashboard, 报价单, 财务, 主数据 children) is always shown when simulating (fallback).  
  - If baseline real-permission filtering already removes an item (e.g. 工单管理 when user lacks work_order:VIEW), the simulator cannot make it appear; it can only remove items the baseline already shows.

---

## Files inspected

- `frontend/dfbs-ui/src/layouts/BasicLayout.tsx` (MENU_ROUTES_BASE, buildMenuRoutes, menuRoutes useMemo, route/menuDataRender)
- `frontend/dfbs-ui/src/shared/config/roleToUiGatingMatrix.ts` (ROLE_TO_UI_GATING_MATRIX, isRouteVisibleForSimulatedRole, filterMenuBySimulatedRole, routeAnchorMatchesPath)

## Technical self-verify

Evidence is taken only from the current code: menu tree and labels from BasicLayout, matrix and filter logic from roleToUiGatingMatrix. No local runtime was run; per-role diff is derived from allowedSimulatedRoleSet and filter logic. CEO screenshot timing is assumed to be pre-FIX1 from the ticket description.
