# TDCLEAN-260210-001-06.a Scripts smoketest evidence

**Request ID:** TDCLEAN-260210-001-06.a-EVID1  
**Purpose:** Verify Step-06 scripts (raw JSON stdout + exit codes). Evidence only; no code changes.

---

## Item 1: Script paths

All four scripts exist:

- scripts/tdclean_preview.sh
- scripts/tdclean_execute.sh
- scripts/tdclean_preview.ps1
- scripts/tdclean_execute.ps1

---

## Item 2: Token acquisition (no user involvement)

- **Login endpoint:** `POST /api/auth/login` (AuthController.java, `@RequestMapping("/api/auth")`, `@PostMapping("/login")`). Body: `{"username":"admin","password":""}`. Response includes `token`.
- **Credential source:** Seed user `admin` from migrations V0047 (app_user seed) and V0061 (grants ROLE_SUPER_ADMIN to admin). Password not validated in MVP.
- **Method used:** Invoke-RestMethod to `http://localhost:8080/api/auth/login` with above body; `$loginResp.token` captured and assigned to `$env:ADMIN_BEARER_TOKEN`. Token not printed (only token length confirmed).
- **Missing credential:** None; admin seed exists.

---

## Item 3: Actual results

**Backend:** Running at http://localhost:8080. Token set in session env for PowerShell.

**A) Preview (PowerShell)** — `.\scripts\tdclean_preview.ps1 -moduleIds "dashboard"`  
- **Exit code:** 1  
- **Stdout (raw JSON):** `{"message":"JSON parse error: Cannot construct instance of `java.util.ArrayList` ... deserialize from String value ('dashboard')","machineCode":"INTERNAL_SERVER_ERROR"}`  
- **Cause (fact):** PowerShell `ConvertTo-Json` serializes a single-element array as the scalar value (`"moduleIds": "dashboard"` instead of `"moduleIds": ["dashboard"]`), so backend receives a string and returns 500. Script correctly printed raw JSON and exited 1. Token not shown.

**B) Execute (PowerShell)** — `.\scripts\tdclean_execute.ps1 -moduleIds "dashboard"`  
- **Exit code:** 1  
- **Stdout:** Same 500 JSON parse error as A (same moduleIds serialization). Token not shown.

**C) Missing token (PowerShell)** — Unset `ADMIN_BEARER_TOKEN`, run preview.ps1 with `-moduleIds "dashboard"`  
- **Exit code:** 1  
- **Output:** Script wrote `{"error":"ADMIN_BEARER_TOKEN is required"}` (to stderr via Write-Error). Token not present in output.

**D) Bash** — Not run. On this runner, `bash` invoked from PowerShell did not receive the session env (WSL/path); script reported "ADMIN_BEARER_TOKEN is required" and exit 1. No token echoed.

---

## Short receipt

- **Completed?** Yes.
- **Token acquisition:** POST http://localhost:8080/api/auth/login with body `{"username":"admin","password":""}`. Seed: V0047 (admin user), V0061 (ROLE_SUPER_ADMIN). Token stored in env only; not revealed in output.
- **A (preview ps1):** Exit 1; stdout = raw JSON (backend 500 due to moduleIds format). Token not shown.
- **B (execute ps1):** Exit 1; same 500/JSON. Token not shown.
- **C (no token):** Exit 1; error message "ADMIN_BEARER_TOKEN is required"; token not shown.
- **D (bash):** Skipped; bash did not receive PowerShell env on this runner.
- **Evidence file:** evidence/tdclean/verify/TDCLEAN_260210_001_06a_scripts_smoketest.md.
- **Full-suite build:** Not run (evidence-only).
- **Blocker:** None. (Optional follow-up: fix ps1 so single-element moduleIds is serialized as JSON array to match backend.)

---

## Actual results (after fix) — TDCLEAN-260210-001-06.a-fix1

Token acquired same way (POST /api/auth/login, seed admin). Token not included in evidence.

**A) ps1 preview with single moduleId "dashboard"**  
- Command: `.\scripts\tdclean_preview.ps1 -moduleIds "dashboard"`  
- Exit code: **0**  
- Stdout (raw JSON, first line): `{"items":[{"moduleId":"dashboard","count":0}],"totalCount":0,"requiresResetConfirm":false,"requiresResetReasons":[],"invalidModuleIds":null}`  
- Backend returned 200; token not shown.

**B) ps1 execute with single moduleId "dashboard"**  
- Command: `.\scripts\tdclean_execute.ps1 -moduleIds "dashboard"`  
- Exit code: **0**  
- Stdout: response includes `"totalDeleted":0` and `"status":"SUCCESS"`. Token not shown.

**C) Missing token**  
- Command: Unset `ADMIN_BEARER_TOKEN`; then `.\scripts\tdclean_preview.ps1 -moduleIds "dashboard"`  
- Exit code: **1**  
- Output: `{"error":"ADMIN_BEARER_TOKEN is required"}` (no token in message).
