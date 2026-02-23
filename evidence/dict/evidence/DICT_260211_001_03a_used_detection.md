# DICT-260211-001-03.a — Used detection evidence (DICT-260211-001-03.a-EVID1)

**Purpose:** Collect real project facts for delete-guard design (minimal, correct, user-friendly). No guessing.

---

## Item 1: How dict types/items are referenced TODAY (DB + code)

### DB (migrations)

- **dict_type**  
  - `backend/dfbs-app/src/main/resources/db/migration/V0067__dict_type_v1.sql`  
  - Columns: id, type_code, type_name, description, enabled, created_at, updated_at.  
  - No other migration defines a FK to `dict_type.id`.

- **dict_item**  
  - `backend/dfbs-app/src/main/resources/db/migration/V0068__dict_item_v1.sql`  
  - Columns: id, type_id, item_value, item_label, sort_order, enabled, note, parent_id, created_at, updated_at.  
  - FK: `type_id` → `dict_type(id)` ON DELETE CASCADE.  
  - FK: `parent_id` → `dict_item(id)` ON DELETE SET NULL.  
  - No other migration defines a FK to `dict_type.id` or `dict_item.id`.

**Conclusion:**  
- **Tables with FK to dict_type.id:** only `dict_item.type_id`.  
- **Tables with FK to dict_item.id:** only `dict_item.parent_id` (self-reference).  
- **Tables/columns storing dict item "value" as string (e.g. payment_method = "cash"):** none. No other table has a column that stores `type_code` or `item_value` from dict.

### Code (entities / usage)

- **Entities:**  
  - `backend/dfbs-app/src/main/java/com/dfbs/app/modules/dicttype/DictTypeEntity.java` (table `dict_type`)  
  - `backend/dfbs-app/src/main/java/com/dfbs/app/modules/dicttype/DictItemEntity.java` (table `dict_item`, `type_id` Long, `parent_id` Long)

- **Repos:**  
  - `DictTypeRepo.java`, `DictItemRepo.java` — used only by `DictTypeService` / `DictItemService` and admin controllers.  
  - No other module references `DictTypeEntity`, `DictItemEntity`, `DictTypeRepo`, or `DictItemRepo`.

- **Note:** `payment_method` (V0010), `damage_type` (V0020), `fee_type` (V0009) are separate tables with their own FKs (e.g. quote.method_id → payment_method.id, damage_record.damage_type_id → damage_type.id, quote_item.fee_type_id → fee_type.id). They are not dict_type/dict_item.

---

## Item 2: Current migrated usage points (minimal list)

| Table     | Column     | Dictionary type (inferable) | Reference form   |
|----------|------------|----------------------------|------------------|
| dict_item | type_id   | N/A (any dict_type)         | FK to dict_type.id |
| dict_item | parent_id | N/A                         | FK to dict_item.id (self) |

**External usage (other tables referencing dict_type or dict_item):** none.

So the only “usage” is:

- **dict_type:** “used” only in the sense that `dict_item.type_id` points to it (no other tables).
- **dict_item:** “used” as a parent if and only if at least one other `dict_item` row has `parent_id = this item’s id`.

---

## Item 3: Minimal “never used” check strategy (non-analytics)

- **dict_type:**  
  - No external FK references exist.  
  - “Used” can be defined only as: has at least one `dict_item` with `type_id = this type’s id`.  
  - **Never-used check:** `SELECT COUNT(*) FROM dict_item WHERE type_id = ?` → 0 means no items; safe to treat as “never used” for a delete guard (or allow delete and rely on ON DELETE CASCADE to remove items).

- **dict_item:**  
  - Only reference to dict_item.id is `dict_item.parent_id`.  
  - **Never-used check:** `SELECT COUNT(*) FROM dict_item WHERE parent_id = ?` → 0 means no children; item is not used as a parent.  
  - No string-value usage anywhere, so no “count rows where column = item_value” is needed.

- **If there are zero real usage points (external to dict tables):**  
  - Today there are zero external usage points.  
  - Minimal guard fallback: (1) **dict_type:** allow delete only when no `dict_item` rows reference it (or document that delete is allowed and CASCADE will remove items). (2) **dict_item:** allow delete only when no other `dict_item` has `parent_id = this id`. This is weak for type (no external refs) and must be documented; for item it is exact.

---

## Item 4: Existing delete behaviors for dict types/items

- **Endpoints that delete dict types or dict items:** none.  
  - Searched: `backend/dfbs-app/src/main/java/com/dfbs/app/application/dicttype/*.java`, `backend/dfbs-app/src/main/java/com/dfbs/app/interfaces/dicttype/*.java`.  
  - No `DELETE`, `delete`, `Remove`, `remove` in those paths. Only create, list, update, enable/disable, reorder.

- **Soft-delete (deleted flag):**  
  - dict_type and dict_item have no `deleted_at` / `is_deleted` column (V0067, V0068).  
  - They use `enabled` for enable/disable only.  
  - Soft-delete is used elsewhere in the project (e.g. masterdata V0004: `deleted_at`; customer merge V0031: `deleted_at IS NULL`), but not in dict tables.

---

## File paths referenced (evidence)

- `backend/dfbs-app/src/main/resources/db/migration/V0067__dict_type_v1.sql`
- `backend/dfbs-app/src/main/resources/db/migration/V0068__dict_item_v1.sql`
- `backend/dfbs-app/src/main/java/com/dfbs/app/modules/dicttype/DictTypeEntity.java`
- `backend/dfbs-app/src/main/java/com/dfbs/app/modules/dicttype/DictItemEntity.java`
- `backend/dfbs-app/src/main/java/com/dfbs/app/modules/dicttype/DictTypeRepo.java`
- `backend/dfbs-app/src/main/java/com/dfbs/app/modules/dicttype/DictItemRepo.java`
- `backend/dfbs-app/src/main/java/com/dfbs/app/application/dicttype/DictTypeService.java`
- `backend/dfbs-app/src/main/java/com/dfbs/app/application/dicttype/DictItemService.java`
- `backend/dfbs-app/src/main/java/com/dfbs/app/interfaces/dicttype/DictionaryTypeAdminController.java`
- `backend/dfbs-app/src/main/java/com/dfbs/app/interfaces/dicttype/DictionaryItemAdminController.java`
