# DICT-260211-001-06.a Seed list and form selection evidence

**Request ID:** DICT-260211-001-06.a-EVID1  
**Purpose:** Freeze v1 seed list + pick 1 high-frequency form for end-to-end dict migration (immediate effect + historical label stability).

---

## Item 1: DB dialect + seed feasibility

**DB vendor/dialect:** PostgreSQL.  
- **Config:** `backend/dfbs-app/src/main/resources/application.yml` lines 19–21: `spring.datasource.url: jdbc:postgresql://localhost:5432/dfbs`.  
- **Migrations:** Flyway enabled (lines 26–30); migrations use PostgreSQL syntax (BIGSERIAL, VARCHAR, TIMESTAMP, etc.).

**Idempotent seed strategy:** Use **INSERT ... ON CONFLICT DO NOTHING** (PostgreSQL).  
- **Evidence:** Existing seeds use this pattern: `V0010__payment_record.sql` line 94 (`ON CONFLICT (name) DO NOTHING`), `V0009__fee_dictionary_and_bom.sql` lines 100, 117, `V0012__quote_cc_leadership.sql` line 55, `V0013__quote_cc_warehouse.sql` line 29.  
- **Dict tables:** `dict_type` has `CONSTRAINT uk_dict_type_code UNIQUE (type_code)` (V0067 line 12). `dict_item` has `CONSTRAINT uk_dict_item_type_value UNIQUE (type_id, item_value)` (V0068 line 17).  
- **Recommended seed:** For dict_type: `INSERT INTO dict_type (...) VALUES (...) ON CONFLICT (type_code) DO NOTHING`. For dict_item: insert after resolving type_id, then `ON CONFLICT (type_id, item_value) DO NOTHING` (or use a single statement with a subquery on type_code). File reference: same migration folder `backend/dfbs-app/src/main/resources/db/migration/`.

---

## Item 2: Candidate “high-frequency” enum/select fields (3 candidates max)

### Candidate 1: Quote 明细类型 (expenseType)

- **Frontend:** Page **Quote** (`/quote`), route in `frontend/dfbs-ui/src/App.tsx`. Select field: **类型** (expenseType) in the quote-item add/edit modal (table of items in quote detail).  
- **Options today:** Hardcoded `EXPENSE_TYPE_OPTIONS` in `frontend/dfbs-ui/src/pages/Quote/index.tsx` lines 78–88: `[{ label: '维修费', value: 'REPAIR' }, ...]` (10 options, labels in Chinese).  
- **Backend/DB:** Table `quote_item`, column `expense_type` VARCHAR(32). Migration: `V0007__quote_item_mvp.sql` lines 23–24 (ADD COLUMN expense_type). Entity: `backend/dfbs-app/src/main/java/com/dfbs/app/modules/quote/QuoteItemEntity.java` line 26–27 (`@Column(name = "expense_type")`, type `QuoteExpenseType` enum). API: create/update item via QuoteItemController, DTO `QuoteItemDto` (expenseType).  
- **Display today:** In Quote detail, items table column “类型” renders `EXPENSE_TYPE_OPTIONS.find((o) => o.value === v)?.label ?? v` (index.tsx lines 323, 432). Backend returns `expenseType` (enum name) only; no snapshot.  
- **Where to show snapshot:** Add `expense_type_label_snapshot` on `quote_item`; in `QuoteItemDto` and service DTO add a display field (e.g. `expenseTypeLabelSnapshot`); frontend “类型” column should read from that snapshot when present (stable after rename/disable in dict).

### Candidate 2: Expense 费用类型 (expenseType)

- **Frontend:** Expense/claim flow (not in Quote page). Backend has `ExpenseEntity.expenseType` (enum `ExpenseType`: FREIGHT, ACCOMMODATION, TRANSPORT, DINING, OTHER).  
- **Options today:** Enum in `backend/dfbs-app/src/main/java/com/dfbs/app/modules/expense/ExpenseType.java`; frontend options source not found in same pattern as Quote (may be hardcoded or different page).  
- **Backend/DB:** Table `expense`, column `expense_type` VARCHAR(32). Migration: `V0032__expense_claim_mvp.sql` line 12. Entity: `ExpenseEntity.java` line 35–36.  
- **Display:** Backend DTO/API returns expenseType; display would need snapshot column on `expense` and DTO/UI to show it.  
- **Where to show snapshot:** Table `expense`, add `expense_type_label_snapshot`; DTO and list/detail UI read from snapshot.

### Candidate 3: Contract price item 类型 (itemType)

- **Frontend:** No contract-price UI found in `frontend/dfbs-ui/src` (grep contract_price/ContractPrice). Backend-only or other entry point.  
- **Options today:** Backend uses `QuoteExpenseType` for `ContractPriceItemEntity.itemType` (same enum as quote_item).  
- **Backend/DB:** Table `contract_price_item`, column `item_type` VARCHAR(32). Migration: `V0035__contract_pricing_mvp.sql` line 27. Entity: `ContractPriceItemEntity.java` (itemType).  
- **Display:** Backend-only; if a UI is added later, display would be from value or snapshot.  
- **Where to show snapshot:** Table `contract_price_item`, add `item_type_label_snapshot`; DTO/UI read from snapshot.

---

## Item 3: Choose 1 v1 target for end-to-end migration

**Chosen target:** **Quote 明细类型 (quote_item.expense_type)**.

**Reasons (facts only):**  
- Single table (`quote_item`), single column (`expense_type`); storage and display path are the simplest.  
- One main user-facing form: Quote page, add/edit item modal, with a single select (类型) and clear submit path (value only, already evidenced in 05.a).  
- Display is already centralized: items table in Quote detail uses one render for “类型”; switching to snapshot is a single column + one DTO field + one frontend change.  
- Broader blast radius: contract_price and expense touch other flows and (for expense) a different enum; quote_item is the most contained and already has the demo pattern (05.b) for snapshot capture.  
- Backend already uses `QuoteExpenseType` enum (string-stored); migrating to dict-driven options with value + label snapshot is a minimal step from current behavior.

---

## Item 4: v1 seed dictionary list (small, aligned to chosen target)

**Dict type:** One type for the chosen form.

- **type_code:** `quote_expense_type`  
- **type_name:** 报价明细类型 (or 费用类型)  
- **dict_item rows:** One row per current `QuoteExpenseType` enum value, Chinese labels; all enabled, no parent. Sort order can follow enum order (e.g. 1–10).

| item value   | item_label | sort_order | enabled | parent |
|-------------|------------|------------|---------|--------|
| REPAIR      | 维修费     | 1          | true    | null   |
| ON_SITE     | 上门费     | 2          | true    | null   |
| PARTS       | 配件费     | 3          | true    | null   |
| PLATFORM    | 平台费     | 4          | true    | null   |
| DATA_PLAN   | 流量费     | 5          | true    | null   |
| STORAGE     | 仓储费     | 6          | true    | null   |
| SHIPPING    | 运输费     | 7          | true    | null   |
| PACKING     | 包装费     | 8          | true    | null   |
| CONSTRUCTION| 施工费     | 9          | true    | null   |
| OTHER       | 其他       | 10         | true    | null   |

**Extras (if already in DB, no duplicate seed):** type_code `payment_method` was used in 04.b/05.b demo; if it is already seeded elsewhere, it can remain as a second type. For v1 seed in this ticket, the **only required seed** is `quote_expense_type` + the 10 items above (Chinese labels, values matching current enum names for drop-in replacement).

---

## Item 5: Snapshot design for the chosen target (minimal, stable)

**Forms submit value only:** Already evidenced in DICT_260211_001_05a (frontend submits expenseType string; no labelInValue). No change to submit shape.

**Backend-captured snapshot:**  
- On create/update of a quote item, backend resolves label from dictionary: call read API (or DictionaryReadService) with `typeCode = quote_expense_type`, `includeDisabled = true`, and current `itemValue` (expense_type value).  
- If an item is found: use its label as `expense_type_label_snapshot`. If not found (type missing or value removed): use `itemValue` as fallback so display never breaks.  
- Persist on the business record: **value** in existing `quote_item.expense_type`; **label snapshot** in new column `quote_item.expense_type_label_snapshot` (e.g. VARCHAR(128)).

**Where to add snapshot column(s):**  
- **Table:** `quote_item`.  
- **Column(s):** `expense_type_label_snapshot` VARCHAR(128) NULL (nullable for existing rows; new/updated rows get it at save time).  
- **Migration:** New Flyway migration in `backend/dfbs-app/src/main/resources/db/migration/` (e.g. V0070).

**Where display should read from:**  
- **Backend:** `QuoteItemDto` (or the service DTO used for GET quote items) should expose a display field for the type label (e.g. `expenseTypeLabelSnapshot`), populated from `QuoteItemEntity.getExpenseTypeLabelSnapshot()` when non-null, else fallback to resolving from dict or to raw value for backward compatibility.  
- **Frontend:** Quote page, items table column “类型”: read from the new snapshot field returned by the API (e.g. `row.expenseTypeLabelSnapshot ?? EXPENSE_TYPE_OPTIONS.find(...)?.label ?? row.expenseType`), so that after migration all display uses the snapshot when present and remains stable after dict renames/disables.

---

*Evidence-only; no code, migrations, or config changed.*
