# DICT-260303-001-03 Impact Check (Type D cascades: parent→child + read contract query children by parent)

**Request ID:** DICT-260303-001-03-IMP  
**Scope:** Fact-based impact + regression checklist before Step-03.

---

## 1) Likely impacted areas

### Pages/flows
- **Admin:** `pages/Admin/DictionaryItems/index.tsx` — parent selector (父级), rootsOnly via “全部” vs specific parent, list filtered by parentId/rootsOnly; create/edit with parentId; reorder per parent scope. No “roots only” vs “all” toggle for read-only consumer UX today.
- **Business:** Quote and DictionarySnapshotDemo call `getDictionaryItems(typeCode, { includeDisabled: false })` with no parentValue. No frontend caller currently uses parentValue in read-only API.

### Modules/files
- **Backend:** `DictItemEntity` (parent_id), `DictItemRepo`, `DictItemService` (list parentId/rootsOnly, create/update validateParent, reorder by parentId), `DictionaryReadService.getItemsByTypeCode` (parentValue → filter by parent id), `DictionaryReadController`, `DictionaryTypeAdminController` (items list/create/reorder).
- **Frontend:** `pages/Admin/DictionaryItems/index.tsx`, `features/dicttype/services/dictItem.ts` (listItems parentId/rootsOnly), `features/dicttype/services/dictRead.ts` (getDictionaryItems parentValue), `useDictionaryItems` (params include parentValue).

### APIs/contracts
- **Admin:** GET/POST ` /api/v1/admin/dictionary-types/{typeId}/items` — list: q, enabled, parentId, rootsOnly, page, pageSize; create: parentId in body; reorder: parentId + orderedItemIds. Response includes parentId (DictItemDto).
- **Read-only:** GET `/api/v1/dictionaries/{typeCode}/items` — params: includeDisabled, parentValue, q. Response: value, label, sortOrder, enabled, parentValue, note. No rootsOnly param.

---

## 2) Facts (evidence)

- **dict_item parent_id:** Supported. DB column `parent_id` (FK dict_item.id ON DELETE SET NULL). Admin list/create/update/reorder use `parentId` (Long). Read-only response exposes **parentValue** (String, the parent’s item_value); backend resolves parent by typeCode+parentValue and filters items by parentId, then maps parentId→item_value for each option (DictionaryItemOptionDto.parentValue).
- **Read-only filtering by parentValue:** When **parentValue present**: find parent by typeId+itemValue; if not found return []; else return items where parentId = that parent’s id (direct children only). When **parentValue absent (null/blank)**: no parent filter → returns **all** items for the type (roots + children). Read-only does **not** support “roots only”; that exists only on admin list (rootsOnly=true).
- **Type D in dict_type:** Column `dict_type.type` exists (A/B/C/D, default A, V0082/V0083). No migration or seed sets type='D'. No app code found that checks or relies on type D.
- **Cascade depth:** **One level only.** `DictItemService.validateParent`: if parentId != null, parent must have `parent.getParentId() == null` (parent must be root). So only root → child; no grandchild. Evidence: `DictItemService.java` lines 112–113.

---

## 3) Regression watchlist

- Super-admin: admin menu/routes and admin dictionary-types/dictionary-items APIs remain super-admin only.
- Type A/C: Quote and others using includeDisabled=false unchanged; no parentValue used by current business callers.
- History/snapshot: includeDisabled=true still returns disabled items with correct parentValue where applicable.
- Ordering: sortOrder asc, id asc unchanged.
- Parent/child: Step-03 changes (e.g. read-only rootsOnly or Type D semantics) must not hide items for callers that omit parentValue (currently “all”); any new “roots only” read contract must be opt-in (e.g. param) to avoid breaking existing “all items” behavior.

---

## 4) Build/test status

- Not run (facts-only, no code changes).
- Failing tests: not run.
