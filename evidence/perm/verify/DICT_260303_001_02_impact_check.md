# DICT-260303-001-02 Impact Check (Dictionary Items: itemKey/label/order/enabled; Type A/C baseline)

**Request ID:** DICT-260303-001-02-IMP  
**Scope:** Fact-based impact + regression checklist before Step-02 implementation.

---

## 1) Likely impacted areas

### Pages/flows
- **Admin dictionary items:** `pages/Admin/DictionaryItems/index.tsx` — route `/admin/dictionary-types/:typeId/items`, linked from DictionaryTypes list via “字典项” button. UX: ProTable with columns 值(itemValue)/标签(itemLabel)/排序(sortOrder)/状态(enabled)/父级/备注; create/edit modal (itemValue, itemLabel, sortOrder, enabled, parentId, note); enable/disable/delete; reorder. Entry is super-admin only (SuperAdminGuard + menu in ORG_STRUCTURE_MENU).
- **Business consumers:**  
  - **Quote** (`pages/Quote/index.tsx`): `getDictionaryItems('quote_expense_type', { includeDisabled: false })` for dropdown.  
  - **DictionarySnapshotDemo** (`pages/Admin/DictionarySnapshotDemo/index.tsx`): `getDictionaryItems(typeCode, { includeDisabled: false })` for demo.  
  Backend **DictLabelSnapshotDemoService** uses `getItemsByTypeCode(typeCode, true, null, null)` (includeDisabled=true) to resolve labels for historical snapshot — no separate “resolve disabled” endpoint; same read API with `includeDisabled=true`.

### Modules/files
- **Backend:**  
  - **Entity/table:** `DictItemEntity` (`dict_item`), migration `V0068__dict_item_v1.sql` — columns: id, type_id, item_value, item_label, sort_order, enabled, note, parent_id; unique (type_id, item_value).  
  - **Repos:** `DictItemRepo` (findByTypeIdAndItemValue).  
  - **Services:** `DictItemService` (list, create, update, setEnabled, deleteById, reorder), `DictionaryReadService` (getItemsByTypeCode with includeDisabled/parentValue/q, sort by sortOrder then id).  
  - **Controllers:** `DictionaryTypeAdminController` (GET/POST `/{typeId}/items`, PATCH `/{typeId}/items/reorder`), `DictionaryItemAdminController` (PUT/PATCH/DELETE `/api/v1/admin/dictionary-items/{id}`), `DictionaryReadController` (GET `/api/v1/dictionaries/{typeCode}/items`).
- **Frontend:**  
  - **Admin:** `features/dicttype/services/dictItem.ts` — listItems(typeId), createItem, updateItem, enableItem, disableItem, deleteDictItem, reorderItems; `pages/Admin/DictionaryItems/index.tsx`.  
  - **Read-only:** `features/dicttype/services/dictRead.ts` (getDictionaryItems), `features/dicttype/hooks/useDictionaryItems.ts`; used by Quote, DictionarySnapshotDemo, and DictionaryTypes “读取示例” block.

### APIs/contracts
- **Admin (super-admin only):**  
  - `GET /api/v1/admin/dictionary-types/{typeId}/items` — q, enabled, parentId, rootsOnly, page, pageSize → DictItemListResponse (items: DictItemDto[]: id, typeId, itemValue, itemLabel, sortOrder, enabled, note, parentId, createdAt, updatedAt).  
  - `POST /api/v1/admin/dictionary-types/{typeId}/items` — CreateDictItemRequest (itemValue, itemLabel, sortOrder, enabled, note, parentId).  
  - `PUT /api/v1/admin/dictionary-items/{id}` — UpdateDictItemRequest (itemLabel, sortOrder, enabled, note, parentId); itemValue immutable.  
  - `PATCH /api/v1/admin/dictionary-items/{id}/enable|disable`, `DELETE /api/v1/admin/dictionary-items/{id}`, `PATCH /api/v1/admin/dictionary-types/{typeId}/items/reorder`.
- **Read-only (no auth):**  
  - `GET /api/v1/dictionaries/{typeCode}/items` — includeDisabled (default false), parentValue, q → DictionaryItemsResponse (typeCode, items: value, label, sortOrder, enabled, parentValue, note). Used by quoting and snapshot demo; ordering sortOrder asc, id asc.
- **“Resolve disabled for history”:** Same read endpoint with `includeDisabled=true` (e.g. DictLabelSnapshotDemoService); no dedicated endpoint.

---

## 2) Regression watchlist (must-test)

- **Non-super-admin:** Admin menu “字典类型”/“数据字典” and route `/admin/dictionary-types/:typeId/items` remain super-admin only (SuperAdminGuard). Admin APIs under `/api/v1/admin/dictionary-types/*` and `/api/v1/admin/dictionary-items/*` use SuperAdminGuard; non-super-admin must get 403.
- **Business dropdowns/enums:** Quote and any other callers using `getDictionaryItems(typeCode, { includeDisabled: false })` must still receive correctly ordered options (sortOrder, id); response shape (value, label, sortOrder, enabled, parentValue, note) must remain compatible.
- **Disabled-item behavior:** Default read `includeDisabled=false` → disabled items hidden from dropdowns. `includeDisabled=true` used for snapshot/history label resolution; any Step-02 change to “Type A/C” or item semantics must not break this.
- **Ordering:** DictionaryReadService returns items sorted by sortOrder asc, id asc; DictItemService.list uses same ordering. Consumers assume stable order; reorder API only changes sortOrder/persistence.

---

## 3) Build/test status

- **Full-suite build:** Not run for this impact check (facts-only, no code changes).
- **Failing tests:** Not run.
