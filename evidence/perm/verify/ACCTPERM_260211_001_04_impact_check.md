# ACCTPERM Step-04 Impact Check (Facts Only)

**Request ID:** ACCTPERM-260211-001-04-IMP | **Related:** ACCTPERM-260211-001-04

---

**1) Impacted areas**
- **Pages:** Permission editing in (1) Account & Permissions → Role Templates tab (`RoleTemplatesTab.tsx`: draft + Save/Reset → `saveRoleTemplate`); (2) Account tab (`AccountsTab.tsx`: tri-state override draftAdd/draftRemove + Save/Reset → `saveAccountOverride`); (3) old `/admin/roles-permissions` (`RolesPermissions/index.tsx`). Save/Reset: draft state local; apply only on Save; Reset restores saved.
- **Files:** RoleTemplatesTab (PermissionModuleBlock), AccountsTab (OverridePermissionModuleBlock), PermissionTreeTab; all use `fetchPermissionTree` + `ModuleNode` from `permService.ts`. `useEffectivePermissions` + `effectiveKeysCache` for menu/guards only — do not mix with editor draft.
- **APIs:** `GET /v1/admin/perm/permission-tree` (keyFormat, actions, modules); `GET/PUT roles/{id}/permissions`, `PUT roles/{id}/template`; `GET/PUT accounts/{userId}/override`.

**2) Permission-tree facts**
- **Shape:** PermissionTreeResponse: keyFormat `"<moduleKey>:<actionKey>"`, actions: ActionItem(key, label), modules: ModuleNode(key, label, actions[], children[]). Tree depth from perm_module parent_id (arbitrary).
- **Action keys (backend FALLBACK/perm_action):** VIEW/查看, CREATE/创建, EDIT/编辑, SUBMIT/提交, APPROVE/审批, REJECT/拒绝, ASSIGN/分配, CLOSE/关闭, DELETE/删除, EXPORT/导出. Beyond VIEW/CREATE/EDIT: SUBMIT, APPROVE, REJECT, ASSIGN, CLOSE, DELETE, EXPORT.
- **Example moduleKeys:** Menu uses platform_application.orgs:VIEW, platform_application.applications:VIEW, work_order:VIEW (BasicLayout); tree from perm_module.

**3) Menu visibility**
- buildMenuRoutes (BasicLayout) filters fixed route list by hasPermission(key); no separate menu-tree API. Step-04 “menu tree” = perm_module tree in editor (same as permission-tree modules).

**4) Regression**
- Quick ops / tree checkbox = draft until Save. Override remove-wins unchanged. Admin/allowlist gating and platform/work-order enforcement unchanged. effectiveKeysCache cleared on logout; no leakage.

**5) Build/test**
- Not executed.
