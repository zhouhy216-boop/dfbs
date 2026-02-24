# PERM-260211-001-01.a Allowlist enable admin — evidence

**Ticket:** PERM-260211-001-01.a-fix1  
**Purpose:** Add current admin userId to PERM super-admin allowlist so `allowed: true` and “角色与权限” entry visible.

---

## Base URL used

- **http://localhost:8080** (default backend port; frontend Vite proxy default per PERM-260211-001-02.a-fix1).

---

## Admin userId discovered

- **Source:** POST /api/auth/login with body `{"username":"admin","password":"..."}`; read response field **`user.id`**.
- **Value (this environment):** **1** (seeded admin user from V0047; consistent with PERM_260211_001_01b_allowlist_gate_evidence).
- Token not recorded.

---

## Config change

- **File:** `backend/dfbs-app/src/main/resources/application.yml`
- **Change:** `dfbs.perm.superAdminAllowlist` set from `""` to `"1"`.
- **Override (optional):** `DFBS_PERM_SUPERADMINALLOWLIST=1` or `--dfbs.perm.superAdminAllowlist=1` when starting backend.
- Backend must be restarted for the change to take effect.

---

## Before (allowlist empty)

- **Request:** GET /api/v1/admin/perm/super-admin/me (with valid auth).
- **Response:** **HTTP 200**, body **`{"allowed": false}`**.

---

## After (allowlist includes admin userId)

- **Request:** GET /api/v1/admin/perm/super-admin/me (with valid auth, same admin).
- **Response:** **HTTP 200**, body **`{"allowed": true}`**.

---

## UI check (actual)

- After config change and backend restart, in browser logged in as admin:
  - **Menu:** “角色与权限” is visible under 系统.
  - **Page:** Opening 角色与权限 opens the page (default action list and permission tree; Step-02 content).
