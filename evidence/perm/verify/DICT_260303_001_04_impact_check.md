# DICT-260303-001-04 Impact Check (Type B status flow: ordering + allowed transitions from→to)

**Request ID:** DICT-260303-001-04-IMP  
**Scope:** Fact-based impact + regression checklist before Step-04.

---

## 1) Likely impacted areas

### Pages/flows
- **Admin:** No existing UI for status-flow / transition matrix or graph. Dictionary items page (`DictionaryItems/index.tsx`) has list, create/edit, reorder, enable/disable; no “transitions” or “allowed from→to” UI. Dictionary types list has type A/B/C/D; no link/placeholder to transition rules.
- **Business:** Status transitions are implemented per domain with enum + service logic (e.g. `AfterSalesService.transition(from, to)` with `AfterSalesStatus`; `QuoteWorkflowService` with quote status; `WorkOrderService` with work order status). No current use of dictionary-driven status flow or “allowed transitions” from a dict type.

### Modules/files
- **Backend:** `dict_type` / `dict_item` (entities, repos, services, controllers). No transition/rule entity or table found. Quote workflow and other workflows are separate (quote/workorder/aftersales status enums and services).
- **Frontend:** Dictionary admin pages (`DictionaryTypes`, `DictionaryItems`), `dictType.ts`, `dictItem.ts`, `dictRead.ts`, `useDictionaryItems`. No hooks or services for “status rules” or “transitions” from dictionary.

### APIs/contracts
- **Read-only:** `GET /api/v1/dictionaries/{typeCode}/items` (includeDisabled, parentValue, q); response value/label/sortOrder/enabled/parentValue/note. No `/transitions`, `/workflow`, or `/status-rules` for dictionaries.
- **Admin:** Dictionary-types and dictionary-items CRUD as existing; no transition/rule endpoints.

---

## 2) Facts (evidence)

- **Transition data model:** **None.** No DB table, entity, or migration for dict item transitions or “from→to” rules. Searches for `transition`, `status_flow`, `dict_transition`, `status-rules` in app code (excluding node_modules) show no dictionary transition model. Quote/workorder/aftersales use entity-level status enums and in-code transition checks (e.g. `AfterSalesService.transition(AfterSalesStatus from, AfterSalesStatus to)`).
- **How statuses are represented:** Business modules use **enums** (e.g. `QuoteStatus`, `WorkOrderStatus`, `AfterSalesStatus`, `ReplenishStatus`) or **string status** columns, not dictionary items. Exception: `quote_expense_type` is a dict type used for expense type **options** (dropdown), not for status flow.
- **Dict types with type='B':** Column `dict_type.type` exists (A/B/C/D, default 'A', V0082/V0083). **No seed sets type='B'.** V0070 seeds `quote_expense_type` without setting type (column added later; default 'A'). No app code checks `type === 'B'` or consumes Type B.
- **Ordering contract:** **sortOrder asc, id asc** in `DictItemService.list`, `DictionaryReadService.getItemsByTypeCode`; admin list and read-only responses use this order. Ordering is **displayed and edited** in admin: `DictionaryItems` page has “排序” column and 上移/下移 reorder; create/edit form has sortOrder field; reorder API `PATCH .../items/reorder` updates sort_order.

---

## 3) Regression watchlist

- Super-admin gate unchanged for dictionary admin (menu, routes, admin dictionary-types/dictionary-items APIs).
- Type A/C/D read contracts unchanged: includeDisabled, parentValue, response shape, ordering (sortOrder asc, id asc).
- Disable-over-delete and history resolution (includeDisabled=true) unchanged.
- No breaking changes to `GET .../dictionaries/{typeCode}/items` response (value, label, sortOrder, enabled, parentValue, note).

---

## 4) Build/test status

- Not run (facts-only, no code changes).
- Failing tests: not run.
