# ACCTPERM-260211-001-01 Impact Check — System → Account & Permissions Entry (FACTS ONLY)

**Request ID:** ACCTPERM-260211-001-01-IMP | **DO NOT MODIFY**

---

**1) Frontend facts**

- **System menu:** Built in `frontend/dfbs-ui/src/layouts/BasicLayout.tsx`. Base routes in `MENU_ROUTES_BASE`: `admin-group` has path `/admin`, name `系统`, with base children 数据确认中心, 平台配置. Extras appended in `buildMenuRoutes`: **PERM allowlist** → `PERM_MENU` (角色与权限 → `/admin/roles-permissions`); **Super-admin** → `ORG_STRUCTURE_MENU` (层级配置, 组织架构, 变更记录, 字典类型, 历史显示示例).
- **Guards:** **Super-admin-only:** `SuperAdminGuard` + `useIsSuperAdmin()` — derives from `useAuthStore().userInfo?.roles` containing `SUPER_ADMIN`. **Perm allowlist-only:** `PermSuperAdminGuard` + `useIsPermSuperAdmin()` — calls `GET /api/v1/admin/perm/super-admin/me`, caches result in `permAllowedCache`; menu shows 角色与权限 only when `permAllowed === true`. No separate “admin-only” (e.g. ROLE_ADMIN) guard for a whole admin section.
- **Current PERM UI:** Single page `frontend/dfbs-ui/src/pages/Admin/RolesPermissions/index.tsx` (route `/admin/roles-permissions`, wrapped by `PermSuperAdminGuard`). Implements: **role templates** (left: role list CRUD; right: permission tree + Save/Reset), **account override** (tab “按账号覆盖”: user search, select account, role template + add/remove keys, save). Permission tree is **inside** the “角色模板” tab (no separate Permission Tree tab). API layer: `RolesPermissions/permService.ts` (allowlist-gated endpoints).
- **Hiding:** Menu: `buildMenuRoutes` uses `permAllowed` to show/hide PERM_MENU and `isSuperAdmin` for ORG_STRUCTURE_MENU. Tabs are internal to the page (no role-based tab hiding today).

**2) Backend facts**

- **Auth:** **ROLE_SUPER_ADMIN / ROLE_ADMIN:** Stored in `UserEntity.authorities` (JSON array). `CurrentUserIdResolver.isSuperAdmin()` true iff authorities contain `ROLE_SUPER_ADMIN`; used by `SuperAdminGuard.requireSuperAdmin()` (e.g. org-structure endpoints). **Perm allowlist:** `dfbs.perm.superAdminAllowlist` (comma-separated userIds), `PermAllowlistProperties.getSuperAdminAllowlistKeys()`. `PermSuperAdminGuard.requirePermSuperAdmin()` / `isPermSuperAdmin()` used on **all** `PermAdminController` endpoints; `GET /api/v1/admin/perm/super-admin/me` returns `{ "allowed": true|false }` (no allowlist leak). **PermMeController** (`/api/v1/perm/me/effective-keys`) uses normal auth only (no allowlist).
- **Reusable endpoints (all under allowlist):** Account: `GET/PUT /api/v1/admin/perm/accounts/{userId}/override`; user search/summary: `GET /api/v1/admin/perm/users?query=`, `GET /api/v1/admin/perm/users/{id}`. Roles: `GET/POST/PUT/DELETE /api/v1/admin/perm/roles`, `GET/PUT /api/v1/admin/perm/roles/{id}/permissions`, `PUT /api/v1/admin/perm/roles/{id}/template`. Tree: `GET /api/v1/admin/perm/permission-tree`. Modules (tree structure): POST/PUT/DELETE `/modules`, PUT `/modules/{id}/actions`.
- **Gating:** “Admin-only” for a new entry: no single backend gate today; either reuse **allowlist** (same as 角色与权限) or add a check (e.g. ROLE_ADMIN). “Permission tree super-admin only” is satisfied by existing **allowlist**: all permission-tree and role/account endpoints already use `permSuperAdminGuard.requirePermSuperAdmin()`.

**3) Regression watchlist**

- Perm allowlist: `/super-admin/me` and all `/api/v1/admin/perm/*` remain allowlist-gated; non-allowlist returns 403. Role template save (PUT role template) and account override save (PUT account override) behavior unchanged; remove-wins and template apply unchanged. Platform/work-order enforcement and `GET /api/v1/perm/me/effective-keys` unchanged. On logout, `useAuthStore.logout` clears `permAllowedCache` and `effectiveKeysCache` (no leakage).

**4) Build/test status**

- Backend compile: not executed. Frontend build: not executed. (Commands: backend `mvn -q compile`; frontend `npm run build`.)
