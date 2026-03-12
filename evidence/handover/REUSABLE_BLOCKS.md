# REUSABLE_BLOCKS — Inventory of reusable building blocks

- **As-of:** 2025-02-24 14:00
- **Repo:** main
- **Commit:** 23467d7d
- **Verification method:** Grep imports/usages in `frontend/dfbs-ui/src` for component and hook names; `shared/form/index.ts`, `shared/table/UnifiedProTable.tsx`, `shared/hooks/useDraftForm.ts`.

**Facts only.** Usage sites from repo. Paths under `frontend/dfbs-ui/src/`.

---

## 1. SmartInput

- **Purpose:** Input with on-blur cleaning (trim, noSpaces, onlyLetters, uppercase); integrates with Form.
- **Location:** `shared/components/SmartInput/index.tsx`.
- **Usage sites:** Platform Application page (Create/Planner/Admin: contactPerson, phone, email, contractNo, orgCodeShort); Platform Org page (orgCodeShort, contactPhone, contactEmail).
- **Key interface:** Props (value, onChange, noSpaces, onlyLetters, uppercase, etc.) — see component file.

---

## 2. SmartReferenceSelect

- **Purpose:** Combobox that loads options from backend smart-select API by entity type; search/select; works with Form.
- **Location:** `shared/components/SmartReferenceSelect/index.tsx`.
- **Usage sites:** Platform Application (customer, contract); Platform Org (org/customer reference); WorkOrder Public; WorkOrder Internal; Admin ConfirmationCenter.
- **Key interface:** Props (entityType, value, onChange, placeholder, etc.) — see component file.

---

## 3. DuplicateCheckModal

- **Purpose:** Modal that shows duplicate-check matches; custom footer (e.g. 返回编辑, 确认新增, 我要开卡).
- **Location:** `features/platform/components/DuplicateCheckModal/index.tsx`.
- **Usage sites:** Platform Application (after duplicate check on create/planner flow); Platform Org.
- **Key interface:** Props (matches, visible, onClose, footer actions) — see component file.

---

## 4. HitAnalysisPanel

- **Purpose:** Calls check-duplicates API and displays hit list (customer/phone/email/org); can feed DuplicateCheckModal.
- **Location:** `features/platform/components/HitAnalysisPanel/index.tsx`.
- **Usage sites:** DuplicateCheckModal (type/matches); Platform Application (right panel in admin modal); Platform Org (duplicate check panel).
- **Key interface:** Props/inputs for check type and display — see component file.

---

## 5. validators (PhoneRule, EmailRule, ContractRule, OrgCodeRule, OrgCodeUppercaseRule)

- **Purpose:** Centralized regex and Form rule objects for phone, email, contract number, org code (platform-specific uppercase).
- **Location:** `features/platform/utils/validators.ts`.
- **Usage sites:** Platform Application; Platform Org.
- **Key interface:** Exported rule objects and functions — see file.

---

## 6. request (axios instance + token helpers)

- **Purpose:** Axios instance with baseURL `/api`, Bearer token from localStorage; 401 clears token and redirects to login. Adds Cache-Control: no-cache for GET to /v1/dictionaries/.../items and .../transitions.
- **Location:** `shared/utils/request.ts`.
- **Usage sites:** Most pages and services that call the backend (Customer, Quote, Shipment, Finance, AfterSales, WorkOrder, Warehouse, ImportCenter, MasterData/*, Platform/*, Admin, Login, SmartReferenceSelect, HitAnalysisPanel, platformConfig, dictRead, dictTransition).
- **Key interface:** Default axios instance; getStoredToken, setStoredToken, clearStoredToken.

---

## 7. adapters (toProTableResult, SpringPage)

- **Purpose:** Converts Spring Boot paginated response to ProTable format (data, total, success).
- **Location:** `shared/utils/adapters.ts`.
- **Usage sites:** AfterSales, Customer, Quote, Shipment, Finance, MasterData (Contract, MachineModel, ModelPartList, Machine, MachineModel Detail, SimCard, SparePart), Platform Org.
- **Key interface:** `toProTableResult<T>(res: SpringPage<T> | null | undefined)` — returns `{ data, total, success }`.

---

## 8. AttachmentList

- **Purpose:** Displays and manages attachment list (upload/list/delete) for entities that support attachments.
- **Location:** `shared/components/AttachmentList.tsx`.
- **Usage sites:** Quote page; Shipment page.
- **Key interface:** Props (entityType, entityId, etc.) — see component file.

---

## 9. useEffectivePermissions

- **Purpose:** Hook that loads effective permission keys for current user (GET /api/v1/perm/me/effective-keys); used for button/route gating.
- **Location:** `shared/hooks/useEffectivePermissions.ts`.
- **Usage sites:** Shipment page (VIEW gating + action button filter); other pages that gate by permission key.
- **Key interface:** useEffectivePermissions() → { has(key), loading, ... }.

---

## 10. useAuthStore

- **Purpose:** Zustand store for auth state; token, userInfo; getStoredToken/setStoredToken/clearStoredToken from request.
- **Location:** `shared/stores/useAuthStore.ts`.
- **Usage sites:** Login; BasicLayout; WorkOrder Internal Detail; Access component.
- **Key interface:** useAuthStore((s) => s.token), .userInfo, .logout, .hydrateFromStorage.

---

## 11. platformConfig service

- **Purpose:** Fetches platform config (options, rules) for platform dropdown and validation.
- **Location:** `features/platform/services/platformConfig.ts`.
- **Usage sites:** Platform Application; Platform Org; System PlatformConfig page.
- **Key interface:** getPlatformConfigs (or equivalent); type PlatformConfigItem — see service file.

---

## 12. Access

- **Purpose:** Renders children when accessible (permission); fallback otherwise; useAccess(permission) hook.
- **Location:** `shared/components/Access.tsx`.
- **Usage sites:** Referenced in codebase for permission-based UI. Exact page list: Not verified.
- **Key interface:** Props (permission, fallback, children); useAccess(permission).

---

## 13. TypeToConfirmModal

- **Purpose:** Modal that requires user to type a confirmation string before executing a destructive action.
- **Location:** `shared/components/TypeToConfirmModal/index.tsx`.
- **Usage sites:** Admin OrgLevelConfig (reset/confirm); Admin OrgTree (disable/move/delete confirm); Admin OrgChangeLog (confirm action).
- **Key interface:** Props (visible, expectedText, onConfirm, onCancel, title, etc.) — see component file.

---

## 14. SuperAdminGuard

- **Purpose:** Renders children only when current user is super admin; otherwise redirect or hide.
- **Location:** `shared/components/SuperAdminGuard.tsx`.
- **Usage sites:** App.tsx (wraps routes /admin/data-dictionary, /admin/org-levels, /admin/org-tree, /admin/org-change-logs, /admin/dictionary-types, /admin/dictionary-types/:typeId/items, /admin/dictionary-types/:typeId/transitions, /admin/dictionary-snapshot-demo); BasicLayout (useIsSuperAdmin() to show org-structure menu items).
- **Key interface:** SuperAdminGuard({ children }); useIsSuperAdmin() hook.

---

## 15. PermSuperAdminGuard, AdminOrSuperAdminGuard, PlatformViewGuard, WorkOrderViewGuard

- **Purpose:** Route guards: PermSuperAdminGuard (roles-permissions); AdminOrSuperAdminGuard (account-permissions); PlatformViewGuard (platform orgs/applications by permission); WorkOrderViewGuard (work_order:VIEW).
- **Location:** `shared/components/PermSuperAdminGuard.tsx`, `AdminOrSuperAdminGuard.tsx`, `PlatformViewGuard.tsx`, `WorkOrderViewGuard.tsx`.
- **Usage sites:** App.tsx (wraps /admin/roles-permissions, /admin/account-permissions, /platform/orgs, /platform/applications, /work-orders, /work-orders/:id); BasicLayout (useIsPermSuperAdmin, useIsAdminOrSuperAdmin, hasPermission for menu).
- **Key interface:** Guard({ children }); useIsPermSuperAdmin(), useIsAdminOrSuperAdmin(); PlatformViewGuard requiredPermission; WorkOrderViewGuard.

---

## 16. TestDataCleanerModal

- **Purpose:** Modal for test data cleaner (preview/execute); Super Admin only.
- **Location:** `shared/components/TestDataCleaner/Modal.tsx`.
- **Usage sites:** BasicLayout (header link "测试数据清理器" when useIsSuperAdmin() true).
- **Key interface:** Props (open, onClose) — see component file.

---

## 17. OrgTreeSelect

- **Purpose:** Selector for org node from tree.
- **Location:** `features/orgstructure/components/OrgTreeSelect.tsx`; exported from `features/orgstructure/index.ts`.
- **Usage sites:** Admin OrgChangeLog (filter by org node).
- **Key interface:** OrgTreeSelectProps (value, onChange, placeholder, etc.) — see component file.

---

## 18. OrgPersonSelect

- **Purpose:** Selector for person in org structure.
- **Location:** `features/orgstructure/components/OrgPersonSelect.tsx`; exported from `features/orgstructure/index.ts`.
- **Usage sites:** Admin OrgTree (assign person to node or similar).
- **Key interface:** OrgPersonSelectProps — see component file.

---

## 19. useDictionaryItems

- **Purpose:** Hook to fetch dictionary items by typeCode; reload() always triggers network request; params include includeDisabled, parentValue, q.
- **Location:** `features/dicttype/hooks/useDictionaryItems.ts`.
- **Usage sites:** Admin DictionaryTypes page (read demo panel).
- **Key interface:** useDictionaryItems(typeCode, params?) → { loading, error, items, typeCode, reload }. Params: GetDictionaryItemsParams (includeDisabled, parentValue, q).

---

## 20. getDictionaryItems

- **Purpose:** Fetches dictionary items by typeCode (GET /api/v1/dictionaries/{typeCode}/items). No auth required.
- **Location:** `features/dicttype/services/dictRead.ts`.
- **Usage sites:** useDictionaryItems (hook); Admin DictionarySnapshotDemo page; Quote page (quote_expense_type options).
- **Key interface:** getDictionaryItems(typeCode, params?) → Promise<DictionaryItemsResponse>. Params: includeDisabled, parentValue, q.

---

## 21. getTransitionsRead, listTransitionsAdmin, upsertTransitionsAdmin

- **Purpose:** getTransitionsRead: public read GET /api/v1/dictionaries/{typeCode}/transitions. listTransitionsAdmin / upsertTransitionsAdmin: admin list and batch upsert for Type B transitions.
- **Location:** `features/dicttype/services/dictTransition.ts`.
- **Usage sites:** getTransitionsRead — Admin DictionaryTransitions page (read preview). listTransitionsAdmin, upsertTransitionsAdmin — Admin DictionaryTransitions page (load/save transitions).
- **Key interface:** getTransitionsRead(typeCode, includeDisabled?) → Promise<TransitionsReadResponse>; listTransitionsAdmin(typeId) → Promise<TransitionListResponse>; upsertTransitionsAdmin(typeId, body) → Promise<TransitionListResponse>.

---

## 22. shared/form (form wheel)

- **Purpose:** Minimal reusable form container, sections, draft alert, readonly view, common fields (phone/email/text), validators, template hook. Used for Platform Application create modal (grouped display, draft, restore-default, readonly preview).
- **Location:** `shared/form/` — `FormSection.tsx`, `FormContainer.tsx`, `DraftAlert.tsx`, `ReadonlyFormView.tsx`, `FormFields.tsx`, `useFormReadonly.ts`, `useFormTemplate.ts`, `formValidators.ts`, `formWheelStyles.css`; entry `shared/form/index.ts`.
- **Usage sites:** Platform Application Create modal (Tabs and createModalOnly paths); `pages/Platform/Application/index.tsx`.
- **Key interface:** FormSection({ title, help, children }); FormContainer({ children }); DraftAlert({ hasDraft, onRestore, onClear }); ReadonlyFormView({ values, fields }); FormFieldPhone, FormFieldEmail, FormFieldText; useFormReadonly(initial); useFormTemplate(form, template); formValidators (PhoneRule, EmailRule).
- **Reuse status:** Reusable as-is for large forms with sections, draft, readonly switch, default template. Not validated on Contract Review (no consumer in repo).

---

## 23. UnifiedProTable, useTableColumnsState, CopyableCell, ResizableTitle

- **Purpose:** Wrapper around ProTable with unified column state persistence, density, refresh, restore-default, column resizing (Excel-like), zebra striping, empty/loading locale, copyable cell. Column widths and columnsState in localStorage by tableKey.
- **Location:** `shared/table/UnifiedProTable.tsx`, `useTableColumnsState.ts`, `CopyableCell.tsx`, `ResizableTitle.tsx`, `unifiedTableLocale.ts`, `unifiedTableConstants.ts`, `unifiedTableStyles.css`; entry `shared/table/index.ts`.
- **Usage sites:** Customer, Contract, AccountPermissions AccountsTab, Quote, Shipment, AfterSales, Finance, Warehouse Inventory/Replenish, Platform Org/Application, MasterData (Contract, SimCard, SparePart, Machine, MachineModel, ModelPartList), Admin DictionaryTypes/Items, ConfirmationCenter, System PlatformConfig, WorkOrder Internal, ImportCenter (unified-table class only); detail tables (Machine/SimCard/MachineModel Detail).
- **Key interface:** UnifiedProTable&lt;T&gt;(tableKey, columns, request, ...ProTableProps); useTableColumnsState(tableKey); CopyableCell({ value }); UNIFIED_TABLE_KEYS enum.
- **Reuse status:** Reusable as-is for list/detail tables; tableKey must be unique per page/tab.

---

## 24. useDraftForm

- **Purpose:** Hook to save/load/clear form draft in localStorage by key; hasDraft, saveDraft(values), loadDraft(), clearDraft().
- **Location:** `shared/hooks/useDraftForm.ts`.
- **Usage sites:** Platform Application Create modal (draftKey per sourceType); Platform Application Planner confirm modal; form wheel DraftAlert.
- **Key interface:** useDraftForm(storageKey) → { hasDraft, saveDraft, loadDraft, clearDraft }.

---

## 25. useSimulatedRoleStore, roleToUiGatingMatrix

- **Purpose:** UI-only role simulator: store holds current simulated role (from top bar dropdown); matrix and helpers (isShipmentWorkflowActionAllowedForSimulatedRole, isWorkOrderActionAllowedForSimulatedRole, isPlatformOrgActionAllowedForSimulatedRole, isPlatformApplicationActionAllowedForSimulatedRole, filterMenuBySimulatedRole) gate left nav and action buttons by simulated role.
- **Location:** `shared/stores/useSimulatedRoleStore.ts`; `shared/config/roleToUiGatingMatrix.ts`.
- **Usage sites:** BasicLayout (menu filter, dropdown, badge, matrix review modal); Shipment page (workflow buttons disable + tooltip); WorkOrder Internal list and Detail (新建工单, 受理, 派单, 驳回, 接单); Platform Org (销售申请, 服务申请, 营企申请, 新建机构, 编辑, 删除); Platform Application (通过, 驳回, 提交至管理员, 关闭申请).
- **Key interface:** useSimulatedRoleStore((s) => s.simulatedRole), .setSimulatedRole; getRoleToUiGatingEntry(id), isRouteVisibleForSimulatedRole(path, role), filterMenuBySimulatedRole(routes, role), is*ActionAllowedForSimulatedRole(role).

---

## Reuse status

- Blocks 1–21: Reusable as-is for current usage sites; request/adapters/dictRead/dictTransition are shared across pages.
- Block 22 (shared/form): Reusable as-is for sectioned forms, draft, readonly, default template; consumer so far: Platform Application create only; Contract Review V1 consumer not in repo.
- Blocks 23–24 (UnifiedProTable, useDraftForm): Reusable as-is; tableKey and draft key must be unique per context.
- Block 25 (useSimulatedRoleStore, roleToUiGatingMatrix): Reusable as-is for UI-only simulator; does not affect backend identity or permissions. Reusable in name only for “real” role-based flows—backend uses effective keys and Primary Business Role on account, not simulated role.

---

## Not verified

- Access component: exact list of pages that use it for permission-based UI was not enumerated.
