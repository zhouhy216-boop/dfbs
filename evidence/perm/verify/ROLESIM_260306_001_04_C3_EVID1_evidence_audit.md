# ROLESIM-260306-001-04-C3-EVID1 — Evidence-only root-cause audit: super-admin permission denials in Platform Applications

**Ticket:** Evidence-only. No code changes. Source: current repo (backend PermEnforcementService + PlatformAccountApplicationController; frontend Platform/Application).

---

## 1) Failing action path(s) in Platform Applications

From CEO symptom: "even under simulated Super Admin, the Platform Applications flow still shows multiple 无权限 and cannot complete application submission / action verification."

The following **action** paths (not page load) are the ones that call backend endpoints that check **non-VIEW** permission keys. Those keys are **not** in the current admin/super-admin bypass sets, so baseline admin/super-admin without those keys in their effective permission set receive 403 and frontend surfaces "无权限":

| # | UI action (label) | Frontend trigger / component path | Backend endpoint | Permission key checked | Bypassed for admin/super-admin today? | Usable by baseline admin/super-admin? |
|---|--------------------|-----------------------------------|------------------|-------------------------|----------------------------------------|--------------------------------------|
| 1 | 提交申请 (create) | `Platform/Application/index.tsx` → doCreate → `request.post('/v1/platform-account-applications/create', ...)` | `POST /api/v1/platform-account-applications/create` | `platform_application.applications:CREATE` | **No** | **No** (403 if key missing) |
| 2 | 提交至管理员 | Planner modal footer → handlePlannerConfirm → `request.put(.../planner-submit)` | `PUT /api/v1/platform-account-applications/{id}/planner-submit` | `platform_application.applications:SUBMIT` | **No** | **No** |
| 3 | 关闭申请 | Planner modal footer → handlePlannerCloseApp → `request.post(.../close)` | `POST /api/v1/platform-account-applications/{id}/close` | `platform_application.applications:CLOSE` | **No** | **No** |
| 4 | 通过 | Admin modal → handleApprove → `request.post(.../approve)` | `POST /api/v1/platform-account-applications/{id}/approve` | `platform_application.applications:APPROVE` | **No** | **No** |
| 5 | 驳回 | Admin modal → setRejectModalOpen → handleRejectConfirm → `request.post(.../reject)` | `POST /api/v1/platform-account-applications/{id}/reject` | `platform_application.applications:REJECT` | **No** | **No** |

**Page entry / list load:** GET `/api/v1/platform-account-applications/page` (and GET `/{id}`, GET `/check-duplicates`, etc.) require `platform_application.applications:VIEW`. That key **is** in the bypass set, so entering the page and loading the list work for admin/super-admin. So:

- **Failing:** create, submit-to-admin, close, approve, reject (all action keys).
- **Working for admin/super-admin (bypassed):** opening 申请管理 page, loading list, loading detail (VIEW only).

---

## 2) Where admin/super-admin bypass lives and how it works

- **Location:** `backend/dfbs-app/src/main/java/com/dfbs/app/application/perm/PermEnforcementService.java`
- **Logic:** In `requirePermission(String permissionKey)`:
  - If `userIdResolver.isAdminOrSuperAdmin()` is true **and** the key is in one of two fixed sets, the method returns without throwing (bypass).
  - Otherwise, it resolves `effectiveKeys` via `accountOverrideService.getEffectiveKeys(userId)` and throws `PermForbiddenException(MESSAGE_NO_ACCESS)` (403) if the key is not in that set.

So the bypass is **whitelist-based**: only keys that are **explicitly listed** in one of the two sets are bypassed for admin/super-admin. There is no “super-admin gets all permissions” rule.

---

## 3) Current bypass list(s) (exact keys)

From `PermEnforcementService.java` as of this audit:

**BASELINE_VIEW_KEYS_ADMIN_BYPASS:**

- `shipment.shipments:VIEW`
- `platform_application.orgs:VIEW`
- `platform_application.applications:VIEW`
- `work_order:VIEW`

**BASELINE_CREATE_KEYS_ADMIN_BYPASS:**

- `work_order:CREATE`

No other keys (no CREATE/SUBMIT/APPROVE/REJECT/CLOSE for platform applications, no action keys for shipment or platform org) are in these sets.

---

## 4) Platform Applications: exact denied keys that explain "无权限"

For the flow CEO described (application submission and action verification), the **exact permission keys** that are **required** by the backend but **not** bypassed for admin/super-admin are:

- `platform_application.applications:CREATE` — submit new application
- `platform_application.applications:SUBMIT` — 提交至管理员 (planner-submit)
- `platform_application.applications:CLOSE` — 关闭申请
- `platform_application.applications:APPROVE` — 通过
- `platform_application.applications:REJECT` — 驳回

So: **page open was fixed** (VIEW is bypassed), but **all these action keys are still not bypassed**, so any of these actions can return 403 with "无权限" for a baseline admin/super-admin whose effective permissions do not include the corresponding key.

---

## 5) Is the system effectively using a whitelist-style admin/super-admin bypass?

**Yes.** The current mechanism is whitelist-based:

- Only keys in `BASELINE_VIEW_KEYS_ADMIN_BYPASS` or `BASELINE_CREATE_KEYS_ADMIN_BYPASS` are bypassed.
- Super-admin is **not** “all-access”; they only bypass this fixed list of keys.
- Any other permission (including all Platform Applications action keys above) still requires the key to be present in the user’s effective permission set, or the backend returns 403.

---

## 6) Same pattern in other modules (short fact-based note)

- **Shipment:** Only `shipment.shipments:VIEW` is bypassed. All action keys (ACCEPT, PREPARE, SHIP, TRACKING, COMPLETE, EXCEPTION, CANCEL, CLOSE) are **not** bypassed. So admin can open the page and see list/detail, but e.g. 审核并补充, 发运, 签收确认, 关闭 will 403 if the account has no corresponding permission.
- **Work Order:** `work_order:VIEW` and `work_order:CREATE` are bypassed. Other keys (REJECT, ASSIGN, SUBMIT, EDIT, APPROVE, CLOSE) are **not** bypassed. So 新建工单 and opening list/detail work; 受理, 派单, 驳回, 接单, 完修 etc. can still 403.
- **Platform Org:** Only `platform_application.orgs:VIEW` is bypassed. CREATE, EDIT, DELETE are **not** bypassed. So opening 平台管理 and list works; 新建机构, 编辑, 删除 can 403.

So the **same whitelist pattern** applies to all these modules: only the VIEW (and in Work Order’s case CREATE) keys that were explicitly added to the bypass sets are allowed without effective permission; all other action keys are still enforced and can cause "无权限" for admin/super-admin if not in their effective set. The Platform Applications flow is **particularly** exposed because **every** main UI action (create, submit-to-admin, close, approve, reject) uses a key that is **not** on the bypass list.

---

## 7) Concise conclusion

- **Whitelist-based bypass:** Yes. Only keys in the two sets in `PermEnforcementService` are bypassed for admin/super-admin.
- **Exact keys on the list today:** See section 3 above (VIEW set + `work_order:CREATE` only).
- **Exact missing keys that explain CEO’s current "无权限" in Platform Applications:**  
  `platform_application.applications:CREATE`, `platform_application.applications:SUBMIT`, `platform_application.applications:CLOSE`, `platform_application.applications:APPROVE`, `platform_application.applications:REJECT`.  
  Adding these (or a subset, depending on product choice) to an admin/super-admin bypass set would remove the corresponding 403s for baseline admin/super-admin, consistent with the current whitelist design.
