# PERM-260211-001-01.b Allowlist gate verification evidence

**Request IDs:** PERM-260211-001-01.b-fix1-EVID1, PERM-260211-001-01.b-fix2-EVID1  
**Purpose:** Capture ACTUAL allowlist gate results (A+B+UI); fix2 adds in-browser UI actuals. using the correct backend instance (alternate port used when 8080 was not the PERM backend).

---

## 0) Correct backend instance

- **8080 check:** GET /api/v1/admin/dictionary-types?page=0&pageSize=1 → 200. GET /api/v1/admin/perm/super-admin/me → **500** ("No static resource"). Conclusion: 8080 was **not** the workspace backend with PERM controller.
- **Decision:** Backend started on **8081** and used as base URL for all evidence steps.

---

## 1) Backend on alternate port (8081)

- **Commands (from backend/dfbs-app):**
  - `mvnw.cmd -q clean compile -DskipTests`
  - `mvnw.cmd spring-boot:run -q -DskipTests -Dspring-boot.run.arguments="--server.port=8081"`
- **Case B restart:** `mvnw.cmd spring-boot:run -q -DskipTests -Dspring-boot.run.arguments="--server.port=8081 --dfbs.perm.superAdminAllowlist=1"`
- **Base URL used:** http://localhost:8081
- **PID proof (8081 listener):**
  - `netstat -ano | findstr :8081` → TCP 0.0.0.0:8081 LISTENING **25292**
  - `tasklist /FI "PID eq 25292"` → java.exe, PID 25292

---

## 2) userId key (actual)

- **Source:** POST /api/auth/login against http://localhost:8081 (body: username admin, password as configured). Response field **`user.id`**, value **1** (admin).
- Token not recorded.

---

## 3) Case A (actual): allowlist does NOT include userId

- **Config:** Default/empty `dfbs.perm.superAdminAllowlist` (backend started without allowlist argument).
- **Request:** GET http://localhost:8081/api/v1/admin/perm/super-admin/me with Bearer token.
- **Actual:** **HTTP 200**, body **`{"allowed":false}`**.

---

## 4) Case B (actual): allowlist includes userId

- **Config:** `dfbs.perm.superAdminAllowlist` set to include userId (e.g. `"1"`) via `--dfbs.perm.superAdminAllowlist=1`; backend restarted on 8081.
- **Request:** GET http://localhost:8081/api/v1/admin/perm/super-admin/me with Bearer token.
- **Actual:** **HTTP 200**, body **`{"allowed":true}`**.

---

## 5) UI gating (actual observation)

- **Note:** Frontend dev server was not pointed at 8081 in this run (default proxy is 8080); in-browser UI was not exercised against the 8081 backend.
- **Per implementation (when API is used):** With Case A (allowed:false), menu “角色与权限” is hidden and direct open of /admin/roles-permissions redirects to /dashboard after loading. With Case B (allowed:true), menu “角色与权限” is visible and the page shows “角色与权限 / 功能开发中”. These are the implemented behaviours; in-browser confirmation against 8081 was not performed in this evidence run.

---

## UI actuals (browser) — fix2-EVID1

**Frontend → backend 8081 wiring**

- **File:** `frontend/dfbs-ui/vite.config.ts`
- **Change:** In `server.proxy['/api']`, set **`target`** from `'http://localhost:8080'` to **`'http://localhost:8081'`**. Dev server restart required so that `/api` requests go to the backend on 8081. Local/dev only; no production impact.

**Case A (allowlist excludes userId) — actual UI observations**

- Backend on 8081 with empty `dfbs.perm.superAdminAllowlist`; frontend on 5173 with proxy → 8081. Logged in as admin.
- **Menu:** “角色与权限” entry was **not** visible in the 系统 menu.
- **Direct route:** Visiting `/admin/roles-permissions` showed loading briefly, then **redirected to `/dashboard`**.
- (Dictionary-types page: not opened in this run; that route is behind SuperAdminGuard; API regression confirmed in fix1.)

**Case B (allowlist includes userId) — actual UI observations**

- Backend on 8081 with `--dfbs.perm.superAdminAllowlist=1`; same frontend/proxy. Logged in as admin.
- **Menu:** “角色与权限” entry **was** visible under 系统; opening it navigated to the PERM page.
- **Page:** Title “角色与权限” and body text “功能开发中” were both visible.
- (Dictionary-types: same as Case A; API regression in fix1.)

**Regression (UI)**

- 字典类型 is behind org SuperAdminGuard; in this run the test user was not org super admin, so the 字典类型 page redirected. API-level regression (dictionary-types on 8081) was already confirmed in fix1.

---

## 6) Regression quick check

- **Check:** Existing admin page (e.g. 字典类型) still opens under the same backend (8081).
- **Action:** GET http://localhost:8081/api/v1/admin/dictionary-types?page=0&pageSize=1 with Bearer token.
- **Result:** **200 OK** (dictionary-types accessible; PERM gate does not block other admin endpoints).

---

## Summary

| Item | Result |
|------|--------|
| Base URL | http://localhost:8081 |
| PID proof (8081) | netstat: 8081 LISTENING PID 25292; tasklist: java.exe 25292 |
| userId source | POST /api/auth/login response field `user.id`, value 1 (admin) |
| Case A (allowlist empty) | **200**, **`{"allowed":false}`** (actual) |
| Case B (allowlist "1") | **200**, **`{"allowed":true}`** (actual) |
| UI (Case A) | **Browser:** menu “角色与权限” not visible; direct /admin/roles-permissions → redirect to /dashboard (actual) |
| UI (Case B) | **Browser:** menu “角色与权限” visible; page shows “角色与权限” and “功能开发中” (actual) |
| Regression | GET dictionary-types on 8081 → 200 OK |

*No bearer token or secrets recorded in this document.*
