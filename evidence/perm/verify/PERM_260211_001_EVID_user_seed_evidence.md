# PERM-260211-001-EVID — User / non-allowlist login evidence (facts only)

**Request ID:** PERM-260211-001-EVID  
**Purpose:** Collect real project facts for non-allowlist user and second-user creation (read-only).

---

## Completed?

Yes.

---

## Findings

**Item 1 — Existing non-allowlist user / test account**
- **Not found.** The only seeded user is **admin** (username `admin`), defined in:
  - **File:** `backend/dfbs-app/src/main/resources/db/migration/V0047__app_user_seed_admin.sql`
  - **Behavior:** `INSERT INTO app_user (username, nickname, can_request_permission, authorities, allow_normal_notification, can_manage_statements) SELECT 'admin', 'Admin', true, '["ROLE_ADMIN"]', true, true WHERE NOT EXISTS (SELECT 1 FROM app_user WHERE username = 'admin');`
  - **Usage:** Log in via `POST /api/auth/login` with body `{"username":"admin","password":"<any>"}`. Password is **not checked** (comment in V0047: "password not checked in MVP"). Auth: `AuthController.java` looks up by `username` only and never uses `request.getPassword()`.
  - Admin’s `user.id` is typically **1**; if `dfbs.perm.superAdminAllowlist` includes `"1"`, admin is allowlisted. There is **no** seed or doc for a second, non-allowlist user.

**Item 2 — Minimal way to create a second user (without changing allowlist)**
- **User table:** `app_user` (entity: `backend/dfbs-app/src/main/java/com/dfbs/app/modules/user/UserEntity.java`, `@Table(name = "app_user")`).
- **Required columns (from V0024, V0046, V0025, V0026, V0047 + entity):**  
  `id` (BIGSERIAL), `username` (VARCHAR 64, NOT NULL, UNIQUE), `nickname` (VARCHAR 128), `can_request_permission` (BOOLEAN NOT NULL DEFAULT false), `authorities` (TEXT), `allow_normal_notification` (BOOLEAN NOT NULL DEFAULT true), `can_manage_statements` (BOOLEAN NOT NULL DEFAULT false).
- **Password:** **None.** No `password` column in `app_user` or in any migration. No `PasswordEncoder`, BCrypt, or hashing in `src/main` (grep: only `AuthController` has `LoginRequest.password` field; it is never used in login logic).
- **Helper scripts/commands:** None found (no script or CLI that inserts users).
- **How-to outline (do not execute):**
  1. Connect to the same DB as the app (e.g. PostgreSQL, schema from `application.yml`).
  2. Run a single `INSERT` into `app_user` with: `username` (unique, e.g. `'seconduser'`), `nickname`, `can_request_permission`, `authorities` (e.g. `'["ROLE_USER"]'`), `allow_normal_notification`, `can_manage_statements`. Omit `id` (auto-generated).
  3. Do **not** add the new user’s `id` to `dfbs.perm.superAdminAllowlist` so they remain non-allowlist.
  4. Log in via `POST /api/auth/login` with `{"username":"<new_username>","password":"<ignored>"}`. Backend will return token + user info; no password verification.

**Item 3 — Admin/test endpoint that creates users or imports fixtures**
- **Not found.** `TestDataCleanerAdminController` (`/api/v1/admin/test-data-cleaner`): only `POST .../preview` and `POST .../execute` (cleanup by module IDs). Guard: `SuperAdminGuard.requireSuperAdmin()`. No user creation or fixture import. No other admin endpoint found that creates users (grep: createUser, create.*user, fixture, import).

---

## Not found

- Any existing non-allowlist user or test account seed.
- Password column or hashing (BCrypt/PasswordEncoder) for `app_user`.
- Any helper script or command to create users.
- Any admin or test endpoint that creates users or imports user fixtures.

---

## Full-suite build

Not executed (read-only evidence; no build run).

---

## Blocker question

None.
