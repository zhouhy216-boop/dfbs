# REUSABLE_BLOCKS — Inventory of reusable building blocks

- **As-of:** 2026-02-09 14:00
- **Repo:** main
- **Commit:** 1df603c5
- **Verification method:** grep imports/usages in `frontend/dfbs-ui/src` for component and service names.

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

- **Purpose:** Axios instance with baseURL `/api`, Bearer token from localStorage; 401 clears token and redirects to login.
- **Location:** `shared/utils/request.ts`.
- **Usage sites:** Most pages and services that call the backend (Customer, Quote, Shipment, Finance, AfterSales, WorkOrder, Warehouse, ImportCenter, MasterData/*, Platform/*, Admin, Login, SmartReferenceSelect, HitAnalysisPanel, platformConfig).
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

## 9. useAuthStore

- **Purpose:** Zustand store for auth state; token, userInfo; getStoredToken/setStoredToken/clearStoredToken from request.
- **Location:** `shared/stores/useAuthStore.ts`.
- **Usage sites:** Login; BasicLayout; WorkOrder Internal Detail; Access component.
- **Key interface:** useAuthStore((s) => s.token), .userInfo, .logout, .hydrateFromStorage.

---

## 10. platformConfig service

- **Purpose:** Fetches platform config (options, rules) for platform dropdown and validation.
- **Location:** `features/platform/services/platformConfig.ts`.
- **Usage sites:** Platform Application; Platform Org; System PlatformConfig page.
- **Key interface:** getPlatformConfigs (or equivalent); type PlatformConfigItem — see service file.

---

## 11. Access

- **Purpose:** Renders children when accessible (permission); fallback otherwise; useAccess(permission) hook.
- **Location:** `shared/components/Access.tsx`.
- **Usage sites:** Referenced in codebase for permission-based UI. Exact page list: Not verified.
- **Key interface:** Props (permission, fallback, children); useAccess(permission).

---

## 12. TypeToConfirmModal

- **Purpose:** Modal that requires user to type a confirmation string before executing a destructive action.
- **Location:** `shared/components/TypeToConfirmModal/index.tsx`.
- **Usage sites:** Admin OrgLevelConfig (reset/confirm); Admin OrgTree (disable/move/delete confirm); Admin OrgChangeLog (confirm action).
- **Key interface:** Props (visible, expectedText, onConfirm, onCancel, title, etc.) — see component file.

---

## 13. SuperAdminGuard

- **Purpose:** Renders children only when current user is super admin; otherwise redirect or hide.
- **Location:** `shared/components/SuperAdminGuard.tsx`.
- **Usage sites:** App.tsx (wraps routes /admin/org-levels, /admin/org-tree, /admin/org-change-logs); BasicLayout (useIsSuperAdmin() to show org-structure menu items).
- **Key interface:** SuperAdminGuard({ children }); useIsSuperAdmin() hook.

---

## 14. OrgTreeSelect

- **Purpose:** Selector for org node from tree.
- **Location:** `features/orgstructure/components/OrgTreeSelect.tsx`; exported from `features/orgstructure/index.ts`.
- **Usage sites:** Admin OrgChangeLog (filter by org node).
- **Key interface:** OrgTreeSelectProps (value, onChange, placeholder, etc.) — see component file.

---

## 15. OrgPersonSelect

- **Purpose:** Selector for person in org structure.
- **Location:** `features/orgstructure/components/OrgPersonSelect.tsx`; exported from `features/orgstructure/index.ts`.
- **Usage sites:** Admin OrgTree (assign person to node or similar).
- **Key interface:** OrgPersonSelectProps — see component file.

---

## Not verified

- Access component: exact list of pages that use it for permission-based UI was not enumerated.
