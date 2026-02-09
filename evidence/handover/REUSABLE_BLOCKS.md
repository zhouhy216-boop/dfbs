# REUSABLE_BLOCKS — Inventory of reusable building blocks

**Facts only.** Usage sites derived from repo (grep of imports/usages in `frontend/dfbs-ui/src`).

---

## 1. SmartInput

- **Path(s)**: `frontend/dfbs-ui/src/shared/components/SmartInput/index.tsx`
- **Purpose**: Input with on-blur cleaning (trim, noSpaces, onlyLetters, uppercase); integrates with Form.
- **Usage sites**: `pages/Platform/Application/index.tsx` (contactPerson, phone, email, contractNo, orgCodeShort in Create/Planner/Admin); `pages/Platform/Org/index.tsx` (orgCodeShort, contactPhone, contactEmail).
- **User-visible effect**: Text cleaned on blur; validation re-runs.

---

## 2. SmartReferenceSelect

- **Path(s)**: `frontend/dfbs-ui/src/shared/components/SmartReferenceSelect/index.tsx`
- **Purpose**: Combobox that loads options from backend smart-select API by entity type; search/select; works with Form.
- **Usage sites**: `pages/Platform/Application/index.tsx` (customer, contract); `pages/Platform/Org/index.tsx` (org/customer reference); `pages/WorkOrder/Public/index.tsx`; `pages/WorkOrder/Internal/index.tsx`; `pages/Admin/ConfirmationCenter/index.tsx`.
- **User-visible effect**: User selects entity from master data (customer, contract) instead of free text.

---

## 3. DuplicateCheckModal

- **Path(s)**: `frontend/dfbs-ui/src/components/Business/DuplicateCheckModal/index.tsx`.
- **Purpose**: Modal that shows duplicate-check matches; custom footer (e.g. 返回编辑, 确认新增, 我要开卡).
- **Usage sites**: `pages/Platform/Application/index.tsx` (after duplicate check on create/planner flow); `pages/Platform/Org/index.tsx`.
- **User-visible effect**: User sees matching records and can return to edit, confirm new, or go to sim flow.

---

## 4. HitAnalysisPanel

- **Path(s)**: `frontend/dfbs-ui/src/components/Business/HitAnalysisPanel/index.tsx`.
- **Purpose**: Calls check-duplicates API and displays hit list (customer/phone/email/org); can feed DuplicateCheckModal.
- **Usage sites**: `components/Business/DuplicateCheckModal/index.tsx` (uses type; modal receives matches from outside); `pages/Platform/Application/index.tsx` (right panel in admin modal); `pages/Platform/Org/index.tsx` (duplicate check panel).
- **User-visible effect**: Displays duplicate-hit analysis for user/admin to decide next step.

---

## 5. validators (PhoneRule, EmailRule, ContractRule, OrgCodeRule, OrgCodeUppercaseRule)

- **Path(s)**: `frontend/dfbs-ui/src/utils/validators.ts`
- **Purpose**: Centralized regex and Form rule objects for phone, email, contract number, org code (platform-specific uppercase).
- **Usage sites**: `pages/Platform/Application/index.tsx`; `pages/Platform/Org/index.tsx`.
- **User-visible effect**: Consistent validation messages and allowed formats.

---

## 6. request (axios instance + token helpers)

- **Path(s)**: `frontend/dfbs-ui/src/utils/request.ts`
- **Purpose**: Axios instance with baseURL `/api`, Bearer token from localStorage; 401 clears token and redirects to login.
- **Usage sites**: Used by most pages and services that call the backend (Customer, Quote, Shipment, Finance, AfterSales, WorkOrder, Warehouse, ImportCenter, MasterData/*, Platform/*, Admin, Login, SmartReferenceSelect, HitAnalysisPanel, platformConfig).
- **User-visible effect**: All API calls use same base URL and auth; unauthenticated users redirected to login.

---

## 7. adapters (toProTableResult, SpringPage)

- **Path(s)**: `frontend/dfbs-ui/src/utils/adapters.ts`
- **Purpose**: Converts Spring Boot paginated response to ProTable format (data, total, success).
- **Usage sites**: `pages/AfterSales/index.tsx`; `pages/Customer/index.tsx`; `pages/Quote/index.tsx`; `pages/Shipment/index.tsx`; `pages/Finance/index.tsx`; `pages/MasterData/Contract/index.tsx`; `pages/MasterData/MachineModel/index.tsx`; `pages/MasterData/ModelPartList/index.tsx`; `pages/MasterData/Machine/index.tsx`, `Detail.tsx`; `pages/MasterData/SimCard/index.tsx`, `Detail.tsx`; `pages/MasterData/SparePart/index.tsx`; `pages/Platform/Org/index.tsx`.
- **User-visible effect**: ProTable tables receive data in expected format.

---

## 8. AttachmentList

- **Path(s)**: `frontend/dfbs-ui/src/shared/components/AttachmentList.tsx`
- **Purpose**: Displays and manages attachment list (upload/list/delete) for entities that support attachments.
- **Usage sites**: `pages/Quote/index.tsx`; `pages/Shipment/index.tsx`.
- **User-visible effect**: User can view and upload attachments on quote and shipment.

---

## 9. useAuthStore

- **Path(s)**: `frontend/dfbs-ui/src/stores/useAuthStore.ts`
- **Purpose**: Zustand store for auth state; uses getStoredToken/setStoredToken/clearStoredToken from request.ts.
- **Usage sites**: `pages/Login/index.tsx`; `layouts/BasicLayout.tsx`; `pages/WorkOrder/Internal/Detail.tsx`; `shared/components/Access.tsx`.
- **User-visible effect**: Login state for layout and guards; login/logout and permission display.

---

## 10. platformConfig service

- **Path(s)**: `frontend/dfbs-ui/src/services/platformConfig.ts`
- **Purpose**: API client for platform config (options/rules).
- **Usage sites**: `pages/Platform/Application/index.tsx`; `pages/Platform/Org/index.tsx`; `pages/System/PlatformConfig/index.tsx`.
- **User-visible effect**: Platform-specific options or validation rules on Platform and System pages.

---

## 11. Access

- **Path(s)**: `frontend/dfbs-ui/src/shared/components/Access.tsx`
- **Purpose**: Renders children when accessible (permission); fallback otherwise; useAccess(permission) hook.
- **Usage sites**: Referenced in codebase for permission-based UI. Exact page list: Not verified.
- **User-visible effect**: Content shown or hidden by permission.

---

## 12. TypeToConfirmModal

- **Path(s)**: `frontend/dfbs-ui/src/shared/components/TypeToConfirmModal/index.tsx`
- **Purpose**: Modal that requires user to type a confirmation string before executing a destructive action.
- **Usage sites**: `pages/Admin/OrgLevelConfig/index.tsx` (reset/confirm); `pages/Admin/OrgTree/index.tsx` (disable/move/delete confirm); `pages/Admin/OrgChangeLog/index.tsx` (confirm action).
- **User-visible effect**: User must type confirmation text to proceed with dangerous operations on level config, org tree, and change logs.

---

## 13. SuperAdminGuard

- **Path(s)**: `frontend/dfbs-ui/src/shared/components/SuperAdminGuard.tsx`
- **Purpose**: Renders children only when current user is super admin; otherwise redirect or hide.
- **Usage sites**: `App.tsx` wraps routes `/admin/org-levels`, `/admin/org-tree`, `/admin/org-change-logs`; `BasicLayout.tsx` uses `useIsSuperAdmin()` to show org-structure menu items.
- **User-visible effect**: Only super admins see and can access 层级配置, 组织架构, 变更记录.

---

## 14. OrgTreeSelect

- **Path(s)**: `frontend/dfbs-ui/src/features/orgstructure/components/OrgTreeSelect.tsx`; exported from `features/orgstructure/index.ts`.
- **Purpose**: Selector for org node from tree (used in org-structure flows).
- **Usage sites**: `pages/Admin/OrgChangeLog/index.tsx` (filter/select org node for change logs).
- **User-visible effect**: User selects an org node when filtering change logs.

---

## 15. OrgPersonSelect

- **Path(s)**: `frontend/dfbs-ui/src/features/orgstructure/components/OrgPersonSelect.tsx`; exported from `features/orgstructure/index.ts`.
- **Purpose**: Selector for person in org structure.
- **Usage sites**: `pages/Admin/OrgTree/index.tsx` (assign person to node or similar).
- **User-visible effect**: User selects a person when managing org tree (e.g. node responsible).
