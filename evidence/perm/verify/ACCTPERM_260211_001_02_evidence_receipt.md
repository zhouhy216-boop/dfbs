# ACCTPERM-260211-001-02 Evidence Receipt (Read-Only)

**Request ID:** ACCTPERM-260211-001-02-EVID

---

## Completed?

Yes

---

## Findings

**Item 1 — Org Structure “Person” model**

- **Backend Entity/table:** `OrgPersonEntity` → table `org_person` (`backend/dfbs-app/src/main/java/com/dfbs/app/modules/orgstructure/OrgPersonEntity.java`). Key fields: `id`, `name`, `phone`, `email`, `remark`, `job_level_id` (position/job level), `is_active`; audit: `created_at`, `created_by`, `updated_at`, `updated_by`. Org unit: via `person_affiliation` (person_id ↔ org_node_id, is_primary, secondary).
- **Migrations:** `V0061__org_structure_v1.sql`: creates `org_person` (id, name, phone, email, remark, job_level_id, is_active, created_at, created_by, updated_at, updated_by); `person_affiliation` (person_id, org_node_id, is_primary). No title/position on `org_person`; position bindings in `org_position_binding` (V0063).
- **APIs used by Org Structure UI:** Base path `/api/v1/org-structure`. Person: `OrgPersonController` (`/api/v1/org-structure/people`): GET ` /people` (search, keyword, primaryOrgId, pageable), GET `/people/by-org` (orgNodeId, includeDescendants, includeSecondaries, activeOnly, keyword, pageable), GET `/people/options?keyword=`, GET `/people/{id}`, GET `/people/{id}/positions`, POST `/people`, PUT `/people/{id}`, POST `/people/{id}/disable`, POST `/people/{id}/enable`. Tree: GET `/nodes/tree` (OrgNodeController). Frontend: `frontend/dfbs-ui/src/features/orgstructure/services/orgStructure.ts` — `searchPeople`, `searchPeopleByOrg`, `getPersonOptions`, `getPerson`, `createPerson`, `updatePerson`, `disablePerson`, `enablePerson`; `getOrgTree` → GET `${BASE}/nodes/tree`.
- **Server-side gate for Org Structure APIs:** All person/node/level/position/change-log controllers use **SuperAdminGuard** (`superAdminGuard.requireSuperAdmin()`). Example: `OrgPersonController` injects `SuperAdminGuard`, every method calls `superAdminGuard.requireSuperAdmin()`. So **SuperAdmin only** (ROLE_SUPER_ADMIN), not Admin or Public.

**Item 2 — Current “Account” model**

- **app_user table/entity:** `UserEntity` @ `app_user` (`backend/.../modules/user/UserEntity.java`). Fields: `id`, `username`, `nickname`, `can_request_permission`, `authorities`, `allow_normal_notification`, `can_manage_statements`. Migrations: V0024 (create app_user: id, can_request_permission, authorities); V0025 (allow_normal_notification); V0026 (can_manage_statements); V0046 (username, nickname; unique index on username); V0047 (seed admin).
- **Enabled/disabled flag:** **None** on `app_user` / `UserEntity`. No `enabled` or `is_active` column.
- **Username uniqueness:** **DB:** V0046 creates `CREATE UNIQUE INDEX IF NOT EXISTS uk_app_user_username ON app_user(username)`. **Service:** No explicit duplicate-username validation in AuthController or UserRepo; uniqueness enforced by DB. Perm test kit creates users by fixed usernames (PermTestAccountKitService).
- **Linkage app_user ↔ org person:** **None.** No `person_id` on `app_user`; no `user_id` on `org_person`. No join table linking the two.

**Item 3 — Auth/password mechanism**

- **Login:** `AuthController` (`backend/.../interfaces/auth/AuthController.java`). `POST /api/auth/login`: looks up user by `request.getUsername().trim()` only; **password is not checked** (LoginRequest has `password` field but it is never used). Token: `"mock-jwt-token-" + System.currentTimeMillis()`. Response: `LoginResponse(token, UserInfo(id, username, nickname, roles))`; roles from `parseAuthorities(user.getAuthorities())` (ROLE_ prefix stripped).
- **Password column:** **None** on `app_user`; no password field in `UserEntity`.
- **PasswordEncoder / BCrypt:** **Not found** in backend.
- **Reset-password:** **Not found** in backend or frontend.

**Item 4 — Reuse candidates for Step-02**

- **Admin-only “assign role template” / account override:** `AccountPermissionsController` (`/api/v1/admin/account-permissions`): GET `/accounts/{userId}/override`, PUT `/accounts/{userId}/override` (body: roleTemplateId, addKeys, removeKeys). Both require **AdminOrSuperAdminGuard** (ROLE_ADMIN or ROLE_SUPER_ADMIN). Reusable for “assign role template” to an account.
- **User search already admin-gated:** Same controller: GET `/users?query=...`, GET `/users/{id}` return minimal user summary (id, username, nickname). Admin-only. Frontend already uses these in AccountPermissions Accounts tab (`acctPermService.searchUsers`, `getUser`, `getAccountOverride`, `saveAccountOverride`).

---

## Not found

- Password column or any password storage.
- PasswordEncoder / BCrypt usage.
- Reset-password flow or precedent.
- Any linkage between `app_user` and `org_person` (person_id, user_id, or join table).
- Enabled/disabled flag on `app_user`.

---

## Full-suite build PASS/FAIL

Not executed.

---

## Blocker question

None.
