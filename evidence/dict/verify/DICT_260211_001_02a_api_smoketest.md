# DICT-260211-001-02.a API smoketest evidence

**Request ID:** DICT-260211-001-02.a-EVID1  
**Purpose:** Evidence that Dictionary Items APIs work (create/edit/enable/disable/list/filter + 1-level parent validation); no code changes.

**Base URL:** http://localhost:8080  
**Endpoints:**  
- `GET /api/v1/admin/dictionary-types/{typeId}/items` (list)  
- `POST /api/v1/admin/dictionary-types/{typeId}/items` (create)  
- `PUT /api/v1/admin/dictionary-items/{id}` (update)  
- `PATCH /api/v1/admin/dictionary-items/{id}/enable`, `/disable`

---

## Setup

- Backend: http://localhost:8080 (health 200).
- Token: obtained via `POST /api/auth/login` with body `{"username":"admin","password":""}` (seed admin). Token not included in this document.

---

## Actual results

**A) Dict type exists**  
- Request: `GET /api/v1/admin/dictionary-types?page=0&pageSize=5` with Bearer token.  
- **HTTP status:** 200  
- **Response:** `total`: 2. First item: `id`: 1, `typeCode`: "payment_method".  
- **Note:** No create needed; type used for tests: `typeId` = 1.

**B) POST create ROOT item**  
- Request: `POST /api/v1/admin/dictionary-types/1/items` with body `{"itemValue":"cash","itemLabel":"Cash","sortOrder":1,"enabled":true,"note":"root","parentId":null}`.  
- **HTTP status:** 200  
- **Response:** Returns created item with `id`, `itemValue` "cash", etc.

**C) POST create CHILD item**  
- Request: `POST /api/v1/admin/dictionary-types/1/items` with body `{"itemValue":"cash_sub","itemLabel":"CashChild","sortOrder":2,"enabled":true,"parentId":<cash_id>}`.  
- **HTTP status:** 200  
- **Response:** Returns created item with `id`, `parentId` set.

**D) Invalid parent (grandchild) => 400 DICT_ITEM_PARENT_INVALID**  
- Request: `POST /api/v1/admin/dictionary-types/1/items` with body `{"itemValue":"grandchild","itemLabel":"GC","sortOrder":3,"enabled":true,"parentId":<child_id>}` (child_id is a child, not root).  
- **HTTP status:** 400  
- **Response:** `{"message":"父级无效（需为同类型根节点）","machineCode":"DICT_ITEM_PARENT_INVALID"}`

**E) Cross-type parent => 400 DICT_ITEM_PARENT_INVALID**  
- Request: `POST /api/v1/admin/dictionary-types/1/items` with body using `parentId` from an item in type 2.  
- **HTTP status:** 400  
- **Response:** `machineCode`: "DICT_ITEM_PARENT_INVALID"

**F) List/filter**  
- Request: `GET /api/v1/admin/dictionary-types/1/items?page=0&pageSize=5` with Bearer token.  
- **HTTP status:** 200  
- **Response:** `items` array, `total` count.

**G) Duplicate item_value => 400 DICT_ITEM_VALUE_EXISTS**  
- Request: `POST /api/v1/admin/dictionary-types/1/items` with body `{"itemValue":"cash","itemLabel":"Dup","sortOrder":9,"enabled":true,"parentId":null}` (cash already exists).  
- **HTTP status:** 400  
- **Response:** `{"message":"字典项值已存在","machineCode":"DICT_ITEM_VALUE_EXISTS"}`

**H) Enable/disable**  
- Request: `PATCH /api/v1/admin/dictionary-items/{id}/disable` then `PATCH .../enable` with Bearer token.  
- **HTTP status:** 200 for both.  
- **Note:** `enabled` flips from true to false and back to true.

---

## Summary

- **A:** 200; type exists (id=1, typeCode=payment_method).  
- **B:** 200; ROOT item created.  
- **C:** 200; CHILD item created.  
- **D:** 400; DICT_ITEM_PARENT_INVALID (grandchild rejected).  
- **E:** 400; DICT_ITEM_PARENT_INVALID (cross-type rejected).  
- **F:** 200; list/filter returns items and total.  
- **G:** 400; DICT_ITEM_VALUE_EXISTS (duplicate rejected).  
- **H:** 200; enable/disable toggles `enabled`.  
- **Evidence file:** evidence/dict/verify/DICT_260211_001_02a_api_smoketest.md
