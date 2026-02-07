# REUSABLE_BLOCKS — Inventory of reusable building blocks

**Facts only.** For each block: name, file path(s), one-line purpose, usage sites (paths), user-visible effect.

---

## 1. SmartInput

- **Path(s)**: `frontend/dfbs-ui/src/components/SmartInput/index.tsx`
- **Purpose**: Input with on-blur cleaning: trim or noSpaces, optional onlyLetters, optional uppercase; integrates with Form via `name` and `form.setFieldValue`/validation.
- **Usage sites**:  
  - `frontend/dfbs-ui/src/pages/Platform/Application/index.tsx` (phone, email, contractNo, orgCodeShort in Create/Planner/Admin)  
  - `frontend/dfbs-ui/src/pages/Platform/Org/index.tsx` (orgCodeShort, contactPhone, contactEmail)
- **User-visible effect**: Text is cleaned on blur (whitespace removed and/or letters-only/uppercase for strict fields); validation re-runs.

---

## 2. SmartReferenceSelect

- **Path(s)**: `frontend/dfbs-ui/src/components/SmartReferenceSelect/index.tsx`
- **Purpose**: Combobox that loads options from backend smart-select API by entity type (e.g. CUSTOMER, CONTRACT) and supports search/select; works with Form.
- **Usage sites**:  
  - `frontend/dfbs-ui/src/pages/Platform/Application/index.tsx` (customer, contract)  
  - `frontend/dfbs-ui/src/pages/Platform/Org/index.tsx` (org/customer reference)  
  - `frontend/dfbs-ui/src/pages/WorkOrder/Public/index.tsx`  
  - `frontend/dfbs-ui/src/pages/WorkOrder/Internal/index.tsx`  
  - `frontend/dfbs-ui/src/pages/Admin/ConfirmationCenter/index.tsx`
- **User-visible effect**: User selects an entity from master data (customer, contract) instead of typing free text.

---

## 3. DuplicateCheckModal

- **Path(s)**: `frontend/dfbs-ui/src/components/Business/DuplicateCheckModal/index.tsx`
- **Purpose**: Modal that shows duplicate-check matches (e.g. from check-duplicates API); supports custom footer (e.g. "返回编辑", "确认新增", "我要开卡").
- **Usage sites**:  
  - `frontend/dfbs-ui/src/pages/Platform/Application/index.tsx` (after duplicate check on create/planner flow)  
  - `frontend/dfbs-ui/src/pages/Platform/Org/index.tsx`
- **User-visible effect**: User sees list of matching records and can return to edit, confirm new, or go to sim flow.

---

## 4. HitAnalysisPanel

- **Path(s)**: `frontend/dfbs-ui/src/components/Business/HitAnalysisPanel/index.tsx`
- **Purpose**: Calls check-duplicates API and displays hit list (customer/phone/email/org); exports `DuplicateMatchItem` type; can be used standalone or to feed DuplicateCheckModal.
- **Usage sites**:  
  - `frontend/dfbs-ui/src/components/Business/DuplicateCheckModal/index.tsx` (uses type; modal receives matches from outside)  
  - `frontend/dfbs-ui/src/pages/Platform/Application/index.tsx` (right panel in admin modal; loads hits and passes to display)  
  - `frontend/dfbs-ui/src/pages/Platform/Org/index.tsx` (duplicate check panel)
- **User-visible effect**: Displays duplicate-hit analysis (e.g. same customer/phone/email) so user or admin can decide next step.

---

## 5. validators (PhoneRule, EmailRule, ContractRule, OrgCodeRule, OrgCodeUppercaseRule)

- **Path(s)**: `frontend/dfbs-ui/src/utils/validators.ts`
- **Purpose**: Centralized regex patterns and Ant Design Form rule objects for phone, email, contract number, and org code (with platform-specific uppercase rule).
- **Usage sites**:  
  - `frontend/dfbs-ui/src/pages/Platform/Application/index.tsx` (PhoneRule, EmailRule, ContractRule, OrgCodeRule, OrgCodeUppercaseRule)  
  - `frontend/dfbs-ui/src/pages/Platform/Org/index.tsx` (PhoneRule, EmailRule, OrgCodeRule, OrgCodeUppercaseRule)
- **User-visible effect**: Consistent validation messages and allowed formats for phone, email, contract, and org code fields.

---

## 6. request (axios instance + token helpers)

- **Path(s)**: `frontend/dfbs-ui/src/utils/request.ts`
- **Purpose**: Axios instance with baseURL `/api`, timeout, Bearer token from localStorage; 401 clears token and redirects to login; getStoredToken/setStoredToken/clearStoredToken for auth.
- **Usage sites**: Used by most pages and services that call the backend (Customer, Quote, Shipment, Finance, AfterSales, WorkOrder, Warehouse, ImportCenter, MasterData/*, Platform/*, Admin, Login, SmartReferenceSelect, HitAnalysisPanel, platformConfig).
- **User-visible effect**: All API calls go through same base URL and auth; unauthenticated users are redirected to login.

---

## 7. adapters (toProTableResult, SpringPage)

- **Path(s)**: `frontend/dfbs-ui/src/utils/adapters.ts`
- **Purpose**: Converts Spring Boot paginated response (content, totalElements, number, size) to ProTable format (data, total, success).
- **Usage sites**:  
  - `frontend/dfbs-ui/src/pages/AfterSales/index.tsx`  
  - `frontend/dfbs-ui/src/pages/Customer/index.tsx`  
  - `frontend/dfbs-ui/src/pages/Quote/index.tsx`  
  - `frontend/dfbs-ui/src/pages/Shipment/index.tsx`  
  - `frontend/dfbs-ui/src/pages/Finance/index.tsx`  
  - `frontend/dfbs-ui/src/pages/MasterData/Contract/index.tsx`  
  - `frontend/dfbs-ui/src/pages/MasterData/MachineModel/index.tsx`  
  - `frontend/dfbs-ui/src/pages/MasterData/ModelPartList/index.tsx`  
  - `frontend/dfbs-ui/src/pages/MasterData/Machine/index.tsx`, `Detail.tsx`  
  - `frontend/dfbs-ui/src/pages/MasterData/SimCard/index.tsx`, `Detail.tsx`  
  - `frontend/dfbs-ui/src/pages/MasterData/SparePart/index.tsx`  
  - `frontend/dfbs-ui/src/pages/Platform/Org/index.tsx`
- **User-visible effect**: Tables (ProTable) receive data in the format they expect from Spring pagination.

---

## 8. AttachmentList

- **Path(s)**: `frontend/dfbs-ui/src/components/AttachmentList.tsx`
- **Purpose**: Displays and manages attachment list (upload/list/delete) for entities that support attachments.
- **Usage sites**:  
  - `frontend/dfbs-ui/src/pages/Quote/index.tsx`  
  - `frontend/dfbs-ui/src/pages/Shipment/index.tsx`
- **User-visible effect**: User can view and upload attachments on quote and shipment.

---

## 9. useAuthStore

- **Path(s)**: `frontend/dfbs-ui/src/stores/useAuthStore.ts`
- **Purpose**: Zustand store for auth state; uses getStoredToken/setStoredToken/clearStoredToken from `request.ts`.
- **Usage sites**:  
  - `frontend/dfbs-ui/src/pages/Login/index.tsx` (login)  
  - `frontend/dfbs-ui/src/layouts/BasicLayout.tsx` (token, hydrateFromStorage, logout, userInfo)  
  - `frontend/dfbs-ui/src/pages/WorkOrder/Internal/Detail.tsx` (userInfo)  
  - `frontend/dfbs-ui/src/components/Access.tsx` (userInfo)
- **User-visible effect**: Login state available to layout and guards; login/logout and permission display.

---

## 10. platformConfig service

- **Path(s)**: `frontend/dfbs-ui/src/services/platformConfig.ts`
- **Purpose**: API client for platform config (options/rules).
- **Usage sites**:  
  - `frontend/dfbs-ui/src/pages/Platform/Application/index.tsx` (getPlatformConfigs, PlatformConfigItem)  
  - `frontend/dfbs-ui/src/pages/Platform/Org/index.tsx` (getPlatformConfigs, PlatformConfigItem)  
  - `frontend/dfbs-ui/src/pages/System/PlatformConfig/index.tsx` (platform config CRUD/options)
- **User-visible effect**: UI shows platform-specific options or validation rules from backend on Platform and System pages.
