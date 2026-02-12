# DICT-260211-001-01.b API smoketest evidence

**Request ID:** DICT-260211-001-01.b-fix1  
**Purpose:** Evidence that Dictionary Types backend APIs work (create/duplicate/enable/disable); no code changes.

**Base URL:** http://localhost:8080  
**Endpoints:** `/api/v1/admin/dictionary-types` (GET list, POST create), `/api/v1/admin/dictionary-types/{id}` (PUT update), `/api/v1/admin/dictionary-types/{id}/enable`, `/api/v1/admin/dictionary-types/{id}/disable`

---

## 1) Backend and tokens

- Backend started locally (spring-boot:run), health 200 at http://localhost:8080/actuator/health.
- **Super-admin token:** Obtained via `POST /api/auth/login` with body `{"username":"admin","password":""}` (seed user from V0047/V0061). Token stored in env for requests; **not included** in this document.
- **Non–super-admin token:** No normal (non–super-admin) user seed found in repo (only `admin` with ROLE_SUPER_ADMIN). 403 proof with a non-admin token **skipped**.

---

## 2) Actual results

**A) List (super admin)**  
- Request: `GET /api/v1/admin/dictionary-types?page=0&pageSize=20` with Bearer token.  
- **HTTP status:** 200  
- **Response:** `{"items":[],"total":0}` (before create); after create `{"items":[{...}],"total":1}`. First item: `id`, `typeCode`, `typeName`, `description`, `enabled`, `createdAt`, `updatedAt`.

**B) Create (super admin)**  
- Request: `POST /api/v1/admin/dictionary-types` with body `{"typeCode":"payment_method","typeName":"PayMethod","description":"optional"}`.  
- **HTTP status:** 200  
- **Response (key fields):** `id`: 1, `typeCode`: "payment_method", `enabled`: true.

**C) Duplicate create (super admin)**  
- Request: Same POST body again (same typeCode).  
- **HTTP status:** 400  
- **Response:** `"machineCode":"DICT_TYPE_CODE_EXISTS"`, message 字典类型编码已存在.

**D) Disable / Enable (super admin)**  
- Request: `PATCH /api/v1/admin/dictionary-types/1/disable` then `PATCH /api/v1/admin/dictionary-types/1/enable`.  
- **HTTP status:** 200 for both.  
- **Response:** After disable: `enabled`: false; after enable: `enabled`: true.

**E) Server-side guard (non-admin)**  
- No non–super-admin user seed available in repo; **403 proof with non-admin token skipped.**

---

## Evidence file

- **Path:** evidence/dict/verify/DICT_260211_001_01b_api_smoketest.md
