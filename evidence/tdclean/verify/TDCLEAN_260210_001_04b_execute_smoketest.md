# TDCLEAN-260210-001-04.b Execute API smoketest evidence

**Request ID:** TDCLEAN-260210-001-04.b-EVID1  
**Purpose:** Verify backend execute API end-to-end (evidence only; no code changes).

---

## Item 1: URL and method

- **Path:** `/api/v1/admin/test-data-cleaner/execute`  
  (Controller: `@RequestMapping("/api/v1/admin/test-data-cleaner")`, `@PostMapping("/execute")` — `TestDataCleanerAdminController.java`.)
- **Method:** POST required. No `@GetMapping("/execute")`; therefore **GET** on the same path returns **405 Method Not Allowed** (Spring MVC default).
- **Backend base URL / port:** From `application.yml`: `server.port: 8080` → base URL **http://localhost:8080**.  
  Full execute URL: **http://localhost:8080/api/v1/admin/test-data-cleaner/execute**.

---

## Item 2: SUPER_ADMIN token (from existing UI session)

- **Token storage (frontend):** `localStorage` key **`dfbs_token`** (see `frontend/dfbs-ui/src/shared/utils/request.ts`: `AUTH_TOKEN_KEY = 'dfbs_token'`).
- **How to obtain:** With preview already working in the UI, (1) DevTools → Application → Local Storage → copy value for `dfbs_token`, or (2) DevTools → Network → select any API request (e.g. preview) → Request Headers → copy `Authorization: Bearer <token>`.
- **Requirement:** Logged-in user must have **ROLE_SUPER_ADMIN** (e.g. seed user `admin` after migration V0061: `authorities = '["ROLE_ADMIN","ROLE_SUPER_ADMIN"]'`). Do not change code to expose the token.

---

## Item 3: Three POST requests — expected and (when run) actual

Run with:  
`curl -s -w "\n%{http_code}" -X POST "http://localhost:8080/api/v1/admin/test-data-cleaner/execute" -H "Content-Type: application/json" -H "Authorization: Bearer <TOKEN>" -d "<BODY>"`

Replace `<TOKEN>` with the value from Item 2 (or omit header if backend uses permitAll + first user as super admin). Replace `<BODY>` with the JSON below.

**A) RESET gating (must BLOCK, no deletion)**  
- **Body:** `{"moduleIds":["org-tree"],"confirmText":null,"includeAttachments":false}`  
- **Expected:** **400**; response body includes **`machineCode": "RESET_CONFIRM_REQUIRED"`** (and `message` about RESET confirmation). No deletion.

**B) Attachments gating (must BLOCK, no deletion)**  
- **Body:** `{"moduleIds":["quotes"],"confirmText":null,"includeAttachments":true}`  
- **Expected:** **400**; response body includes **`machineCode": "ATTACHMENTS_NOT_SUPPORTED_YET"`**. No deletion.

**C) Safe 200 path (deletes nothing)**  
- **Body:** `{"moduleIds":["dashboard"],"confirmText":null,"includeAttachments":false}`  
- **Expected:** **200**; response includes at least: **`items`** (array, dashboard module with 0 tables), **`totalDeleted": 0`**, **`status`** (e.g. `"SUCCESS"` or `"FAILED"` when no tables run), **`redisMessage`** (string).  
- **Note:** `dashboard` has an empty table list in `TestDataCleanerExecuteService.MODULE_TABLES_DELETE_ORDER` (and in preview mapping), so no DELETE runs.

---

## Actual results (live run)

**Backend:** Started locally; base URL **http://localhost:8080**.  
**Token:** Obtained via `POST /api/auth/login` with body `{"username":"admin","password":""}`; response `token` used in `Authorization: Bearer <token>` for execute calls. (Admin has ROLE_SUPER_ADMIN per V0061.)

| Request | Body | HTTP status | machineCode / key fields |
|--------|------|-------------|---------------------------|
| **A** | `{"moduleIds":["org-tree"],"confirmText":null,"includeAttachments":false}` | **400** | `machineCode`: **RESET_CONFIRM_REQUIRED**, `message`: "RESET confirmation required" |
| **B** | `{"moduleIds":["quotes"],"confirmText":null,"includeAttachments":true}` | **400** | `machineCode`: **ATTACHMENTS_NOT_SUPPORTED_YET**, `message`: "Attachments cleanup not supported yet" |
| **C** | `{"moduleIds":["dashboard"],"confirmText":null,"includeAttachments":false}` | **200** | `totalDeleted`: **0**, `status`: **SUCCESS**, `redisMessage`: "Redis not configured.", `items`: one entry `moduleId` "dashboard", `tables`: [], `moduleDeletedTotal`: 0, `moduleStatus`: "SUCCESS" |

Empty-module for C: **dashboard** (verified empty in `MODULE_TABLES_DELETE_ORDER`). No deletion performed.

---

## Short receipt

- **Completed?** Yes.
- **Backend started?** Yes. URL used: **http://localhost:8080**.
- **A:** HTTP 400, `machineCode`: RESET_CONFIRM_REQUIRED, `message`: RESET confirmation required.
- **B:** HTTP 400, `machineCode`: ATTACHMENTS_NOT_SUPPORTED_YET, `message`: Attachments cleanup not supported yet.
- **C:** HTTP 200, `totalDeleted`: 0, `status`: SUCCESS, `redisMessage`: "Redis not configured.", `items` (dashboard, 0 tables).
- **Evidence file:** `evidence/tdclean/verify/TDCLEAN_260210_001_04b_execute_smoketest.md`.
- **Blocker question:** None.

---

## Appendix (full JSON bodies)

**Request A:**  
`{"moduleIds":["org-tree"],"confirmText":null,"includeAttachments":false}`

**Request B:**  
`{"moduleIds":["quotes"],"confirmText":null,"includeAttachments":true}`

**Request C:**  
`{"moduleIds":["dashboard"],"confirmText":null,"includeAttachments":false}`

**Example 200 response shape (C):**  
`{"startedAt":"...","finishedAt":"...","requiresResetConfirm":false,"requiresResetReasons":[],"invalidModuleIds":[],"items":[{"moduleId":"dashboard","tables":[],"moduleDeletedTotal":0,"moduleStatus":"SUCCESS"}],"totalDeleted":0,"status":"SUCCESS","redisMessage":"Redis not configured."}`
