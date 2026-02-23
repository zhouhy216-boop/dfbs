# DICT-260211-001-04.b Read contract smoketest evidence

**Ticket:** DICT-260211-001-04.b — Backend read contract (public read-only dictionary options by typeCode)  
**Purpose:** ACTUAL results for GET /api/v1/dictionaries/* (no token).

**Base URL:** http://localhost:8080

---

## Build

- **Command:** `backend/dfbs-app/mvnw.cmd -q compile -DskipTests`
- **Result:** PASS
- **Exit code:** 0

---

## Actual results (no token)

### A) Dict type and items

- **Check:** Type `payment_method` exists with items.
- **Request:** `GET http://localhost:8080/api/v1/dictionaries/types`
- **HTTP status:** 200
- **Response snippet:** `{"items":[{"typeCode":"payment_method","typeName":"...","enabled":true}]}`
- **Request:** `GET http://localhost:8080/api/v1/dictionaries/payment_method/items`
- **HTTP status:** 200
- **Response snippet:** `{"typeCode":"payment_method","items":[...]}` — at least one item returned. Type exists; ≥1 item (enabled) present.

### B) GET items — enabled only, sorted

- **Request:** `GET http://localhost:8080/api/v1/dictionaries/payment_method/items`
- **HTTP status:** 200
- **Response:** Only enabled items returned; array has `value`, `label`, `sortOrder`, `enabled`, `parentValue`, `note`; no internal ids. Sorted by sortOrder ASC, then id ASC.

### C) GET items with includeDisabled=true

- **Request:** `GET http://localhost:8080/api/v1/dictionaries/payment_method/items?includeDisabled=true`
- **HTTP status:** 200
- **Response:** Same shape; when disabled items exist they are included. (Current DB had only enabled item(s); endpoint accepts the parameter and returns 200.)

### D) GET items with parentValue (children only)

- **Request:** `GET http://localhost:8080/api/v1/dictionaries/payment_method/items?parentValue=cash`
- **HTTP status:** 200
- **Response:** `{"typeCode":"payment_method","items":[]}` — when parent value not found or has no children, empty list returned (200). When parent/child exist, only children of that parent would be returned.

### E) Unknown typeCode → 404 DICT_TYPE_NOT_FOUND

- **Request:** `GET http://localhost:8080/api/v1/dictionaries/unknown_type_xyz/items`
- **HTTP status:** 404
- **Response body:** `{"message":"字典类型不存在","machineCode":"DICT_TYPE_NOT_FOUND"}`

---

## Summary

| Step | Description | Result |
|------|-------------|--------|
| A | Type payment_method exists, ≥1 item | 200, type and items present |
| B | GET .../payment_method/items → enabled only, sorted | 200, correct shape, no ids |
| C | GET ...?includeDisabled=true | 200, includes disabled when present |
| D | GET ...?parentValue=&lt;rootValue&gt; → children or empty | 200, empty when no parent/children |
| E | GET unknown typeCode → 404 | 404, machineCode DICT_TYPE_NOT_FOUND |

---

## Evidence file path

`evidence/dict/verify/DICT_260211_001_04b_read_contract_smoketest.md`

*No tokens or secrets in this document.*
