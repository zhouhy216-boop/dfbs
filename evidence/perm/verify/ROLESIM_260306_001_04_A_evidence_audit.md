# ROLESIM-260306-001-04-A — Evidence-only audit: page-level action buttons and v1 simulated-role action-gating entry points

**Ticket:** Evidence-only audit. No code changes. Source: current repo.

---

## 1) Seeded page action inventory (real Chinese labels)

### /shipments — 发货列表 (Shipment/index.tsx)

| Location | Chinese label | Source (path) | Current visibility / control |
|----------|----------------|---------------|------------------------------|
| Toolbar | New Shipment (新建发货单 trigger) | toolBarRender, ModalForm trigger | Always visible to anyone on page (page already gated by PERM_VIEW). No extra permission on create. |
| Row 操作 | 详情 | columns render | Always (link to drawer). |
| Row 操作 | 导出运单 | columns render | Always (no permission check in code). |
| Row 操作 | 导出回单 | columns render | Always (no permission check in code). |
| Drawer 可执行操作 | 审核并补充 / 备货确认 / 发运 / 更新物流信息 / 签收确认 / 关闭 / 标记异常 / 取消 | workflow.actions → visibleActions() | **Permission-hidden:** only shown when `has(permKeyForAction(actionCode))`. Backend /workflow also filters actions by permission. Labels from backend (labelCn). |
| Drawer | 发起售后 | Button | Always visible in drawer (no permission check). |

**Gating source:** `useEffectivePermissions()`; `visibleActions(workflow.actions)` filters by `permKeyForAction` (ACCEPT, PREPARE, SHIP, TRACKING, COMPLETE, EXCEPTION, CANCEL, CLOSE). Page entry: `has(PERM_VIEW)` else Navigate to /dashboard.

---

### /work-orders — 工单管理 (WorkOrder/Internal/index.tsx, Detail.tsx)

| Location | Chinese label | Source | Current visibility / control |
|----------|----------------|--------|-----------------------------|
| Toolbar extra | 新建工单 | extra, Button | **Permission-hidden:** `has('work_order:CREATE')`. |
| Row 操作 (待受理/待派单/全部) | 详情 | baseColumns / pool columns | Always. |
| Row 操作 | 受理 | pendingPoolColumns / allPoolColumns | **Permission + status:** `has('work_order:ASSIGN')` and status PENDING. |
| Row 操作 | 派单 | readyPoolColumns / allPoolColumns | **Permission + status:** `has('work_order:ASSIGN')` and status ACCEPTED_BY_DISPATCHER. |
| Row 操作 | 驳回 | pendingPoolColumns / allPoolColumns | **Permission + status:** `has('work_order:REJECT')` and status PENDING. |
| Detail header extra | 派单 | Detail.tsx | **Permission + status:** `canDispatch && has('work_order:ASSIGN')`. |
| Detail header extra | 接单 | Detail.tsx | **Permission + status:** `canAccept && has('work_order:SUBMIT')`. |
| Detail header extra | 开始处理 | Detail.tsx | **Permission + status:** `canStart && has('work_order:EDIT')`. |
| Detail header extra | 添加记录 / 添加配件 | Detail.tsx | **Permission + status:** `canAddRecordOrParts && has('work_order:EDIT')`. |
| Detail header extra | 提交签字 | Detail.tsx | **Permission + status:** `canSubmitSign && has('work_order:APPROVE')`. |
| Detail header extra | 完修 | Detail.tsx | **Permission + status:** `canComplete && has('work_order:CLOSE')`. |

**Gating source:** `useEffectivePermissions()` → `has('work_order:CREATE')`, `work_order:ASSIGN`, `work_order:REJECT`, `work_order:SUBMIT`, `work_order:EDIT`, `work_order:APPROVE`, `work_order:CLOSE`. Status flags (canDispatch, canAccept, etc.) from workflow state. Page entry: WorkOrderViewGuard (work_order:VIEW).

---

### /platform/orgs — 平台管理 (Platform/Org/index.tsx)

| Location | Chinese label | Source | Current visibility / control |
|----------|----------------|--------|-----------------------------|
| Toolbar | 销售发起 / 服务发起 / 企业发起 | extra Buttons | Always (navigate to apply). |
| Toolbar | 新建机构 | Button | **Permission-hidden:** `hasPermission(PERM_ORGS_CREATE)`. |
| Row 操作 | 编辑 | columns render | **Permission-hidden:** `hasPermission(PERM_ORGS_EDIT)`. |
| Row 操作 | 删除 | Popconfirm | **Permission + status:** `hasPermission(PERM_ORGS_DELETE)` and row.status !== 'DELETED'. |

**Gating source:** `useEffectivePermissions()` → `platform_application.orgs:CREATE`, `:EDIT`, `:DELETE`. Page entry: PlatformViewGuard(platform_application.orgs:VIEW).

---

### /platform/applications — 申请管理 (Platform/Application/index.tsx)

| Location | Chinese label | Source | Current visibility / control |
|----------|----------------|--------|-----------------------------|
| Toolbar / row | 通过 / 驳回 | Modal buttons, row actions | **Permission + status:** `hasPermission(PERM_APPS_APPROVE)` / `PERM_APPS_REJECT`, row.status === 'PENDING_ADMIN'. |
| Row 操作 | 提交至管理员 / 关闭申请 | etc. | **Permission:** `hasPermission(PERM_APPS_SUBMIT)`, `PERM_APPS_CLOSE`. |
| Modals | 保存草稿, 确认新增, 申请复用, 开卡申请, 申请核查 | Various | Mix of permission and status; some 功能暂留. |

**Gating source:** `useEffectivePermissions()` → `platform_application.applications:SUBMIT`, `:CLOSE`, `:APPROVE`, `:REJECT`. Page entry: PlatformViewGuard(platform_application.applications:VIEW).

---

### /admin/dictionary-types — 字典类型 (Admin/DictionaryTypes/index.tsx)

| Location | Chinese label | Source | Current visibility / control |
|----------|----------------|--------|-----------------------------|
| Toolbar | 新建字典类型 | Button | Always (page is SuperAdmin-only by route guard). |
| Row 操作 | 字典项 | Button | Always. |
| Row 操作 | 状态流(迁移规则) | Button (type B only) | Conditionally shown: row.type === 'B'. |
| Row 操作 | 编辑 / 禁用 / 启用 / 删除 | Buttons | Always. |

**Gating source:** Route-level SuperAdminGuard; no per-action permission in component. All actions visible to anyone who reaches the page.

---

### Other /admin/* in matrix

- **数据字典 / 组织架构 / 账号与权限 / 角色与权限:** Not re-inspected in this pass; typically route-guard only (SuperAdmin or Admin). Action-level permission checks not enumerated here.

---

## 2) Current gating source per action (summary)

| Page / area | Gating type | Keys / logic |
|-------------|-------------|--------------|
| Shipments list/drawer | Real permission | shipment.shipments:VIEW (page); ACCEPT, PREPARE, SHIP, TRACKING, COMPLETE, EXCEPTION, CANCEL, CLOSE (workflow buttons via visibleActions). |
| Work orders list/detail | Real permission + status | work_order:VIEW (page); CREATE, ASSIGN, REJECT, SUBMIT, EDIT, APPROVE, CLOSE (per action) + canDispatch/canAccept/... |
| Platform orgs | Real permission | platform_application.orgs:VIEW (page); CREATE, EDIT, DELETE (per action). |
| Platform applications | Real permission + status | platform_application.applications:VIEW (page); SUBMIT, CLOSE, APPROVE, REJECT + status. |
| Dictionary types | Route guard only | SuperAdminGuard; no per-action permission. |

---

## 3) Pages/actions suitable for v1 first batch

- **/shipments:** Already has backend-driven workflow actions and frontend `visibleActions` by permission. Simulator can layer on top: when simulating Viewer/Operator, same buttons can be disabled (or hidden) using matrix role vs. action mapping. Best candidate: **可执行操作** (审核并补充, 备货确认, 发运, 等) + optionally toolbar **新建发货单** and row **导出运单/导出回单**.
- **/work-orders:** Many row and detail actions (新建工单, 受理, 派单, 驳回, 接单, 开始处理, 添加记录, 添加配件, 提交签字, 完修) gated by real permission. Simulator v1 can mirror: disable (or hide) by simulated role for a small set (e.g. 新建工单, 派单, 接单) so CEO can see role switch effect without defining full matrix for every work_order:* key.
- **/platform/orgs:** 新建机构, 编辑, 删除 — few, clear; good for v1.
- **/platform/applications:** 通过, 驳回, 提交至管理员, 关闭申请 — permission + status; suitable for v1 batch if matrix defines role→action for these.
- **/admin/dictionary-types:** No per-action permission; page is super-admin only. Simulator action-gating here is optional (e.g. “Viewer cannot edit”); lower priority unless product asks to simulate non-admin on admin pages.

**Recommended v1 first batch (smallest, CEO-verifiable):**

1. **发货列表** (/shipments): **可执行操作** 区域内的 workflow 按钮（审核并补充、备货确认、发运、更新物流信息、签收确认、关闭、标记异常、取消）— already permission-driven; add simulator layer: disable when simulated role not in matrix-allowed set for that action, with tooltip “该角色不可操作”.
2. **工单管理** (/work-orders): Toolbar **新建工单**; row **受理、派单、驳回**; detail **派单、接单** — disable by simulated role with same tooltip.
3. **平台管理** (/platform/orgs): **新建机构**、row **编辑**、**删除** — disable by simulated role.
4. **申请管理** (/platform/applications): **通过**、**驳回**、**提交至管理员**、**关闭申请** — disable by simulated role where status allows.

---

## 4) Recommended v1 behavior per action: disable vs hide

- **Default (recommendation):** **Disable** + tooltip “该角色不可操作” so the user sees the action exists but cannot use it; avoids “empty UI” and clarifies role effect. Use **hide** only where product explicitly wants the action not shown (e.g. 关闭 for non-close role).
- **Shipments 可执行操作:** Prefer **disable**: buttons already from backend; when simulated role not allowed for that actionCode, render button disabled + tooltip. No new backend contract.
- **Work orders:** Prefer **disable** for 新建工单, 受理, 派单, 驳回, 接单, 等.
- **Platform orgs/applications:** Prefer **disable** for 新建机构, 编辑, 删除, 通过, 驳回, 提交, 关闭.
- **Consistency:** Use one strategy per page family in v1 (all disable or all hide for that page); prefer disable across the first batch for clarity.

---

## 5) Optional anchor display (recommendation)

- **v1:** **Tooltip only** when action is disabled by simulator: e.g. “该角色不可操作”. No secondary popover unless product asks.
- **Postpone:** Role name / matrix anchor in a small popover on hover; keep v1 minimal.

---

## 6) Blockers / anchor gaps

- **Actions already removed by real-permission:** If user has no work_order:CREATE, 新建工单 is already hidden; simulator cannot “show then disable” without changing real-permission behavior. So for v1, only apply simulator disable when the button is **already visible** (user has real permission); do not show disabled buttons for permissions the user does not have. No change to real-permission logic.
- **Actions with no permission check today:** 发货列表 row 导出运单/导出回单 have no frontend permission; if we add simulator gating only, we need a clear rule (e.g. same as VIEW or a dedicated export role). Document as “simulator only” if wired.
- **Route/page not ready:** Not observed for the seeded pages; /work-orders, /platform/orgs, /platform/applications, /shipments all have routes and pages. Admin pages exist and are route-guard only.
- **Anchor gap:** PROCESS_MAP/OBJECT_MAP do not enumerate every UI action (e.g. “派单”, “接单”); evidence uses current code labels. No guess for unmapped actions.

---

## 7) Smallest next execution batch (proposed)

1. **Shipments:** In `Shipment/index.tsx`, when simulator is active (simulatedRole set), for each workflow action button: if current simulated role is not in matrix `allowedSimulatedRoleSet` for that route/action, render button disabled with title “该角色不可操作”. Use existing matrix entries for shipments-page; action-level mapping can be “same as menu” (Viewer: no ACCEPT/PREPARE/SHIP/等) or a small action sub-matrix.
2. **Work orders:** In WorkOrder Internal list and Detail, when simulator is active: for 新建工单, 受理, 派单, 驳回, 接单 (and optionally 开始处理, 添加记录, 提交签字, 完修), if simulated role not allowed for that action, disable + tooltip. Requires defining work-order action → allowed roles (e.g. from matrix or minimal v1 rule: Viewer = none, Operator = 派单/接单, Admin = all).
3. **Platform orgs:** When simulator active: 新建机构, 编辑, 删除 disabled for disallowed role + tooltip.
4. **Platform applications:** When simulator active: 通过, 驳回, 提交至管理员, 关闭申请 disabled for disallowed role + tooltip.

All of the above react to role switch without refresh (same as left nav) because they depend on simulatedRole state. CEO can: switch role → see left nav change (existing) + see buttons disable on current page.

---

## Files inspected

- `frontend/dfbs-ui/src/pages/Shipment/index.tsx` (toolbar, columns, drawer, visibleActions, permKeyForAction)
- `frontend/dfbs-ui/src/pages/WorkOrder/Internal/index.tsx` (extra, pendingPoolColumns, readyPoolColumns, allPoolColumns)
- `frontend/dfbs-ui/src/pages/WorkOrder/Internal/Detail.tsx` (extra buttons: 派单, 接单, 开始处理, 添加记录, 添加配件, 提交签字, 完修)
- `frontend/dfbs-ui/src/pages/Platform/Org/index.tsx` (toolbar, row 编辑/删除, hasPermission)
- `frontend/dfbs-ui/src/pages/Platform/Application/index.tsx` (hasPermission, 通过, 驳回, 提交, 关闭)
- `frontend/dfbs-ui/src/pages/Admin/DictionaryTypes/index.tsx` (toolbar 新建字典类型, row 字典项, 状态流, 编辑, 禁用/启用, 删除)

## Technical self-verify

Inventory and gating source are from the listed files; no runtime was run. “Suitable for v1” and “recommended batch” are recommendations from code structure; exact matrix action↔role mapping for work orders and platform apps is not yet in the matrix and would need to be defined in the implementation ticket.
