# DICT-260211-001-03.b Delete guard smoketest evidence

**Request ID:** DICT-260211-001-03.b-fix6-EVID1  
**Purpose:** ACTUAL working DELETE responses; backend run on alternate port (8081).

**Base URL used:** http://localhost:8081  
**Auth:** Bearer token via `POST /api/auth/login` (seed admin). Token not stored or printed.

---

## 1) Compile (actual)

- **Command:** `backend/dfbs-app/mvnw.cmd -q clean compile -DskipTests`
- **Result:** PASS  
- **Exit code:** 0  

---

## 2) Backend started on alternate port 8081

- **Exact start command (from backend/dfbs-app):**  
  `mvnw.cmd spring-boot:run -q -DskipTests "-Dspring-boot.run.arguments=--server.port=8081 --management.endpoints.web.exposure.include=health,info,mappings --management.endpoint.mappings.enabled=true"`
- **Started:** Server responded at `http://localhost:8081/actuator/health` with 200 after ~27s.

**PID proof for 8081:**

- `netstat -ano | findstr :8081`  
  ```
  TCP    0.0.0.0:8081           0.0.0.0:0              LISTENING       19148
  TCP    [::]:8081              [::]:0                 LISTENING       19148
  ```
- `tasklist /FI "PID eq 19148"`  
  ```
  java.exe                     19148 Console                    1    516,356 K
  ```

---

## 3) Server responds on 8081 (actual)

- **Request:** `GET http://localhost:8081/api/v1/admin/dictionary-types?page=0&pageSize=20` (with Bearer token).
- **Actual:** HTTP 200  

---

## 4) DELETE mappings proof (actual)

- **Request:** `GET http://localhost:8081/actuator/mappings`
- **Proof snippets:**

  - DELETE dictionary-types:
    ```
    "predicate":  "{DELETE [/api/v1/admin/dictionary-types/{id}]}"
    ```
  - DELETE dictionary-items:
    ```
    "predicate":  "{DELETE [/api/v1/admin/dictionary-items/{id}]}",
    "handler":  "com.dfbs.app.interfaces.dicttype.DictionaryItemAdminController#delete(Long)",
    ```

---

## Actual results (timestamp: local run on 8081)

### A) Type delete blocked (has items) => 400

- **Setup:** typeId=1, typeCode=payment_method, ≥1 item (5 items).
- **Request:** `DELETE http://localhost:8081/api/v1/admin/dictionary-types/1` with Bearer token.
- **Actual HTTP status line:** HTTP/1.1 400 Bad Request  
- **Actual JSON snippet:**
```json
{"message":"该类型下存在字典项，无法删除","machineCode":"DICT_TYPE_DELETE_NOT_ALLOWED_USED"}
```

### B) Item delete blocked (has children) => 400

- **Setup:** Root P=3, child C=5 under type 1.
- **Request:** `DELETE http://localhost:8081/api/v1/admin/dictionary-items/3` with Bearer token.
- **Actual HTTP status line:** HTTP/1.1 400 Bad Request  
- **Actual JSON snippet:**
```json
{"message":"该字典项存在子项，无法删除","machineCode":"DICT_ITEM_DELETE_NOT_ALLOWED_HAS_CHILDREN"}
```

### C) Allowed deletes => 204

- **Request:** `DELETE http://localhost:8081/api/v1/admin/dictionary-items/5` (child C) with Bearer token.  
- **Actual HTTP status line:** HTTP/1.1 204 No Content  

- **Setup:** Deleted remaining items so type 1 had 0 items.
- **Request:** `DELETE http://localhost:8081/api/v1/admin/dictionary-types/1` with Bearer token.  
- **Actual HTTP status line:** HTTP/1.1 204 No Content  

---

## Compile proof (actual)

- **Command:** `backend/dfbs-app/mvnw.cmd -q clean compile -DskipTests`
- **Result:** PASS  
- **Exit code:** 0  

---

*No secrets or tokens in this document.*
