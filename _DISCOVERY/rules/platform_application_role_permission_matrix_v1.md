# Platform Application — Role/Permission Matrix v1

**Document purpose:** Human-readable “角色/权限矩阵” for the Platform Application flow, derived **strictly from current code behaviour**. Intended as input for a future permission skeleton + configurable foundation.

**Last extracted from code:** 2026-01 (VNX-20260208-* scope).

---

## 0. Coverage boundary

This matrix covers all UI nodes that could affect role/permission behaviour in the Platform Application flow, as implemented in the frontend:

| Area | Nodes / entry points |
|------|----------------------|
| **Platform Management** | Page `/platform/orgs`; entry buttons: 销售申请, 服务申请, 营企申请, 新建机构 |
| **Application Management** | Page `/platform/applications`; tabs 待处理 / 申请历史; row actions: 详情, 营企处理, 管理员审核 |
| **Create apply form** | ModalForm (销售申请 / 服务申请 / 申请平台) — sales / service / enterprise |
| **Enterprise Confirm** | Modal 营企确认 (营企处理) |
| **Admin Review** | Modal 管理员审核 |
| **Duplicate-warning** | Modal 重复提醒 (返回编辑, 确认新增, 申请复用, 开卡申请, 申请核查) |
| **Placeholders** | 开卡申请, 申请核查, 申请复用 (as nodes; marked placeholder) |

**Out of scope:** Backend RBAC, other modules (WorkOrder, Admin/ConfirmationCenter, etc.). Only Platform Application flow as above.

---

## 1. Role list (implied by code)

The frontend **does not** resolve “who is the current user’s role” for the Platform Application flow. There is **no** use of `useAuthStore().userInfo.roles` or `useAuthStore().userInfo.permissions` or `useAccess(permission)` in any of: `Application/index.tsx`, `Org/index.tsx`, `applications/Apply.tsx`, or related Platform components.

**Evidence:** No imports of `Access`, `useAccess`, or `useAuthStore` in `frontend/dfbs-ui/src/pages/Platform/*`.

So “roles” below are **actor-by-context**: they describe “who can do what” only in the sense of **route + stage + row status**, not a stored user role.

| Actor (人话) | How it is determined in code | Evidence |
|--------------|------------------------------|----------|
| **申请人 / 发起人** | Any user who can open the create apply form (via Platform Management 销售申请/服务申请/营企申请, or apply route). No role check; entry is route/toolbar. | `Org/index.tsx` toolBarRender buttons navigate to `/platform/apply?source=...`; no `Access` or role condition. |
| **营企处理人 (Planner)** | Any user who can see the 营企处理 link on the application list. Link visibility is **row.status IN (PENDING_PLANNER, PENDING_CONFIRM, CLOSED)** — not user.role. | `Application/index.tsx` columns render: `(row.status === 'PENDING_PLANNER' \|\| row.status === 'PENDING_CONFIRM' \|\| row.status === 'CLOSED') && <a>营企处理</a>`. |
| **管理员 (Admin)** | Any user who can see the 管理员审核 link. Link visibility is **row.status === 'PENDING_ADMIN'** — not user.role. | `Application/index.tsx` columns render: `row.status === 'PENDING_ADMIN' && <a>管理员审核</a>`. |
| **平台管理操作人** | Any user who can open Platform Management (`/platform/orgs`). No role check; menu/route only. | `BasicLayout.tsx` menu item 平台管理 → `/platform/orgs`; no `Access` wrapper on route or toolbar. |

**Summary:** No runtime role resolution from auth store for Platform Application. Behaviour is **route- and stage-driven** (and row.status for list actions).

---

## 2. Node × Role matrix

Because there is **no role-based gating** in code, every node behaves as **“All users (role-independent)”** from a permission perspective. What varies is **node state** and **document/source type** (and key fields like `violatesHouseRules`, `selectedHitCanReuse`), not “current user’s role”.

**Thresholds (E):** Not implemented for role or permission gating. No condition on amount/quantity for showing/hiding buttons or fields. Evidence: no such logic in Application/index.tsx or Org/index.tsx.

Below: for each node, we give **who can see/do what** in practice — which is “any user who reaches this node” (by route/stage), with conditions that **do** exist in code (A=Node, C=source type, D=state/flags).

---

### 2.1 Platform Management (/platform/orgs)

| Dimension | Visible blocks/sections | Editable / Required / Read-only | Buttons visible/clickable | Condition |
|-----------|--------------------------|----------------------------------|-----------------------------|-----------|
| All users | Table (org list), search, toolbar | Table read-only; search editable | 销售申请, 服务申请, 营企申请, 新建机构 | (A) Node = Platform Management. No role check. Evidence: Org/index.tsx toolBarRender. |

---

### 2.2 Application Management — list (待处理 / 申请历史)

| Dimension | Visible blocks/sections | Editable / Required / Read-only | Buttons visible/clickable | Condition |
|-----------|--------------------------|----------------------------------|-----------------------------|-----------|
| All users | Table columns, 操作 | Table read-only | 详情 (always) | (A) Node = Application list. Evidence: Application/index.tsx columns. |
| All users | — | — | 营企处理 | (A) Node = Application list **AND** (D) `row.status` IN (`PENDING_PLANNER`, `PENDING_CONFIRM`, `CLOSED`). Evidence: columns render. |
| All users | — | — | 管理员审核 | (A) Node = Application list **AND** (D) `row.status === 'PENDING_ADMIN'`. Evidence: columns render. |

---

### 2.3 Create apply form (销售申请 / 服务申请 / 申请平台)

| Dimension | Visible blocks/sections | Editable / Required / Read-only | Buttons visible/clickable | Condition |
|-----------|--------------------------|----------------------------------|-----------------------------|-----------|
| All users | 平台信息, 客户信息, 机构全称, 联系人信息 | All editable; required per platform_application_rules.md §4.1 | 提交, 保存草稿 | (A) Node = Create apply form. (C) sourceType determines contractNo vs price/quantity/reason. Evidence: Application/index.tsx ModalForm. |
| All users | 恢复草稿 / 清除草稿 (Alert) | — | 恢复草稿, 清除草稿 | (D) `hasDraft` for current draft key. No role. |

---

### 2.4 Enterprise Confirm modal (营企确认)

| Dimension | Visible blocks/sections | Editable / Required / Read-only | Buttons visible/clickable | Condition |
|-----------|--------------------------|----------------------------------|-----------------------------|-----------|
| All users | 申请单号, 平台, 客户, 机构全称, 联系人, 销售负责人, 合同/单价台数原因 (by currentRow.sourceType) | All form fields editable and required as per §4.2 of rules doc | 关闭申请, 取消, 保存草稿, 提交至管理员 | (A) Node = 营企确认. Visible to anyone who opened 营企处理 (i.e. who saw the link). No role check. Evidence: Application/index.tsx Modal 营企确认. |

---

### 2.5 Admin Review modal (管理员审核)

| Dimension | Visible blocks/sections | Editable / Required / Read-only | Buttons visible/clickable | Condition |
|-----------|--------------------------|----------------------------------|-----------------------------|-----------|
| All users | Left: application summary (read-only). Right: HitAnalysisPanel (read-only). Form: orgCodeShort, region | orgCodeShort, region editable and required | 通过, 驳回 | (A) Node = 管理员审核. Visible to anyone who opened 管理员审核 from list. No role check. Evidence: Application/index.tsx admin Modal, handleApprove. |

---

### 2.6 Duplicate-warning modal (重复提醒)

| Dimension | Visible blocks/sections | Editable / Required / Read-only | Buttons visible/clickable | Condition |
|-----------|--------------------------|----------------------------------|-----------------------------|-----------|
| All users | Hit list (read-only), hit selection | Read-only display; selection is click | 返回编辑 | Always. Evidence: renderFooter in Application/index.tsx and Org/index.tsx. |
| All users | — | — | 确认新增 | (D) `!violatesHouseRules`. |
| All users | — | — | 申请复用 | (D) `selectedHitCanReuse`. |
| All users | — | — | 开卡申请 | (C) Enterprise Confirm stage **AND** (D) `violatesHouseRules`. |
| All users | — | — | 申请核查 | (C) Service stage **AND** (D) `violatesHouseRules`. |

No role dimension; only stage and house-rule/hit state.

---

### 2.7 Org create modal (新建机构)

| Dimension | Visible blocks/sections | Editable / Required / Read-only | Buttons visible/clickable | Condition |
|-----------|--------------------------|----------------------------------|-----------------------------|-----------|
| All users | 平台, 客户, 机构代码/全称, 联系人, 地区, 申请人, 备注, 启用状态 | All editable; required as per rules doc | 取消, 保存草稿, 提交 | (A) Node = 新建机构. No role check. Evidence: Org/index.tsx Modal create. |

---

### 2.8 Placeholder pages (开卡申请 / 申请核查 / 申请复用)

| Node | Visible | Editable | Buttons | Condition |
|------|---------|----------|---------|-----------|
| 开卡申请 (sim-activation) | Placeholder content only | N/A | N/A | (A) Placeholder. Reachable from duplicate 开卡申请. Evidence: SimActivation.tsx. |
| 申请核查 (verification) | Placeholder content only | N/A | N/A | (A) Placeholder. Evidence: Verification.tsx. |
| 申请复用 (reuse) | Placeholder content only | N/A | N/A | (A) Placeholder. Evidence: Reuse.tsx. |

---

## 3. Entry × Role matrix (Platform Management)

All entry buttons on Platform Management (`/platform/orgs`) are **visible and enabled for any authenticated user** who can reach the page. There is no `Access` wrapper and no check of `userInfo.roles` or `userInfo.permissions` for these buttons.

| Entry button | Visible (Y/N) | Enabled/clickable (Y/N) | Notes / conditions | Evidence |
|--------------|---------------|--------------------------|--------------------|----------|
| 销售申请 | Y | Y | Navigate to `/platform/apply?source=sales`. No role condition. | Org/index.tsx toolBarRender `<Button onClick={() => navigate('/platform/apply?source=sales')}>`. |
| 服务申请 | Y | Y | Navigate to `/platform/apply?source=service`. No role condition. | Same file, source=service. |
| 营企申请 | Y | Y | Navigate to `/platform/apply?source=enterprise`. No role condition. | Same file, source=enterprise. |
| 新建机构 | Y | Y | Open create org modal. No role condition. | Same file, openCreateModal. |

**Role column:** Not used; no roles in code. Row applies to “any user on Platform Management”.

---

## 4. Multi-role decision rule

**No runtime role resolution; behaviour is route/stage-driven.**

- The frontend does **not** read `userInfo.roles` or `userInfo.permissions` for Platform Application UI.
- It does **not** use `useAccess(permission)` or `<Access accessible={...}>` in Platform pages.
- “Who can do what” is determined by:
  - **Route** (e.g. who opened `/platform/orgs` or `/platform/apply`),
  - **Stage / source type** (sales / service / enterprise),
  - **Row state** (e.g. `row.status` for 营企处理 vs 管理员审核),
  - **Duplicate modal state** (violatesHouseRules, selectedHitCanReuse).

**Evidence:**

- `frontend/dfbs-ui/src/pages/Platform/Application/index.tsx` — no import of useAuthStore, useAccess, Access.
- `frontend/dfbs-ui/src/pages/Platform/Org/index.tsx` — no import of useAuthStore, useAccess, Access.
- `frontend/dfbs-ui/src/shared/stores/useAuthStore.ts` — UserInfo has optional `roles?: string[]`, `permissions?: string[]` but they are not referenced in Platform code.
- `frontend/dfbs-ui/src/shared/components/Access.tsx` — exists but is not used in Platform Application flow.

---

## 5. Evidence appendix

| Rule group | File path(s) | Symbol / key variables |
|------------|--------------|-------------------------|
| Platform Management toolbar | `frontend/dfbs-ui/src/pages/Platform/Org/index.tsx` | toolBarRender: 销售申请, 服务申请, 营企申请, 新建机构; navigate, openCreateModal |
| Application list row actions | `frontend/dfbs-ui/src/pages/Platform/Application/index.tsx` | columns render: 详情, 营企处理 (row.status PENDING_PLANNER \|\| PENDING_CONFIRM \|\| CLOSED), 管理员审核 (row.status === PENDING_ADMIN) |
| Create form / 营企确认 / Admin modal | `frontend/dfbs-ui/src/pages/Platform/Application/index.tsx` | ModalForm, Modal 营企确认, Modal 管理员审核; no Access/useAccess |
| Duplicate modal footer | `frontend/dfbs-ui/src/pages/Platform/Application/index.tsx`, `Org/index.tsx` | renderFooter; violatesHouseRules, selectedHitCanReuse, pendingCreateValues (stage) |
| Auth store (unused in Platform) | `frontend/dfbs-ui/src/shared/stores/useAuthStore.ts` | UserInfo.roles?, UserInfo.permissions? |
| Access component (unused in Platform) | `frontend/dfbs-ui/src/shared/components/Access.tsx` | Access, useAccess(permission) |
| No role/permission in Platform | `frontend/dfbs-ui/src/pages/Platform/**/*.tsx` | grep: no useAuthStore, useAccess, Access in Platform |

---

## 6. Coverage checklist

| Item | Status | Notes |
|------|--------|-------|
| **Roles listed** | ✓ | 申请人/发起人, 营企处理人, 管理员, 平台管理操作人 — as actor-by-context; no stored role. |
| **Nodes covered** | ✓ | Platform Management, Application list, Create apply form, 营企确认, 管理员审核, 重复提醒, 新建机构, 开卡申请/申请核查/申请复用 placeholders. |
| **Entry buttons covered** | ✓ | 销售申请, 服务申请, 营企申请, 新建机构 — all Y/Y, role-independent. |
| **Multi-role rule stated** | ✓ | §4: No runtime role resolution; behaviour is route/stage-driven; evidence cited. |
| **Evidence included** | ✓ | §1, §2, §3, §4, §5 reference file paths and symbol names. |
| **Thresholds** | ✓ | Explicitly “not implemented for role gating”; no amount/quantity in permission logic. |

---

*End of Platform Application — Role/Permission Matrix v1.*
