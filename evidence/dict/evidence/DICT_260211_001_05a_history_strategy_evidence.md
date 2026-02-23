# DICT-260211-001-05.a History strategy evidence

**Request ID:** DICT-260211-001-05.a-EVID1  
**Purpose:** Collect real project facts to choose a minimal snapshot mechanism for historical label stability.

---

## Item 1: Current persistence pattern for “code+name” across business tables

Searched: `backend/dfbs-app/src/main/resources/db/migration/*.sql` and entities for column pairs and value-only codes.

### Representative examples (table + columns + migration path/line)

| Table | Columns | Migration / reference |
|-------|---------|------------------------|
| dict_type | type_code, type_name | V0067__dict_type_v1.sql (lines 6–7) |
| dict_item | item_value, item_label | V0068__dict_item_v1.sql (lines 7–8) |
| account_statement | customer_id, customer_name | V0026__account_statement.sql (lines 11–12) |
| quote | customer_id, customer_name, original_customer_name | V0030__quote_standardization.sql (lines 5–7) |
| quote_item | original_part_name | V0030__quote_standardization.sql (line 10) |
| contract_price_header | customer_id, contract_name | V0035__contract_pricing_mvp.sql (lines 7–8) |
| contract_price_item | item_type (value only) | V0035__contract_pricing_mvp.sql (line 27) |
| expense | expense_type (value only) | V0032__expense_claim_mvp.sql (line 12) |
| invoice_record | invoice_type (value only) | V0015__invoice_workflow.sql (line 52) |
| quote_item | item_type, expense_type (value only) | V0005__quote_item.sql (line 13); V0007__quote_item_mvp.sql (lines 23–24) |
| repair_record | customer_name (no id pair) | V0023__repair_ledger.sql (line 6) |
| org_level | display_name | V0061__org_structure_v1.sql (line 9) |
| job_level | display_name | V0061__org_structure_v1.sql (line 41) |
| org_position_catalog | base_name, display_name, short_name | V0064__org_position_catalog_and_templates_seed.sql (line 6); V0063__org_position_config_v1.sql (line 17) |

**Summary:** Two patterns: (1) FK + denormalized name (customer_id + customer_name, contract_name) for display/history; (2) value-only code (expense_type, item_type, invoice_type) with no stored label. No business table currently stores dict item_value + a label snapshot.

---

## Item 2: Any existing “label snapshot” strategy

Searched for: label_snapshot, name_snapshot, display_name, saved_label, history_label.

- **display_name:** Used as the entity’s own name in org_level, job_level, org_position_catalog (backend: OrgLevelEntity.java, JobLevelEntity.java, OrgPositionCatalogEntity.java; migrations V0061, V0063, V0064). Not a “snapshot of a dictionary label.”
- **label_snapshot / name_snapshot / saved_label / history_label:** No occurrences in repo.

**Conclusion:** No existing “label snapshot” strategy for dictionary options. Denormalized names (customer_name, contract_name) are written in backend services when saving the related entity and read for display via DTOs/UI.

---

## Item 3: Frontend form select patterns (options representation and submit)

Searched: `frontend/dfbs-ui/src/pages/**` for Select, ProFormSelect, labelInValue, options, value/label submit.

- **labelInValue:** Not used in the codebase (no matches).
- **Submit shape:** Forms submit **value only**. Labels are used for display by resolving from options at render time.
- **Concrete references:**
  1. **Quote (expense type):** `src/pages/Quote/index.tsx` — ProFormSelect `name="expenseType"` with `options={EXPENSE_TYPE_OPTIONS}`; form submits `expenseType` (value); table render uses `EXPENSE_TYPE_OPTIONS.find((o) => o.value === v)?.label ?? v` (lines 323, 432, 463, 484).
  2. **ImportCenter:** `src/pages/ImportCenter/index.tsx` — options as `{ value, label }[]` (lines 74–81).
  3. **Contract:** `src/pages/MasterData/Contract/index.tsx` — ProFormSelect with `fieldProps: { options: STATUS_OPTIONS }`.
  4. **Platform Application:** `src/pages/Platform/Application/index.tsx` — `<Select options={...} />` and SmartReferenceSelect; options as value/label arrays.

**Conclusion:** Antd Select/ProFormSelect are used with `options={[{ value, label }]}`. Forms submit **value only**; label is not sent unless the app explicitly adds it. No project-wide use of labelInValue for dictionary-style options.

---

## Item 4: Dict read contract consumers today

Searched for: GET /api/v1/dictionaries/, getDictionaryItems, useDictionaryItems, dictRead.

- **Service:** `frontend/dfbs-ui/src/features/dicttype/services/dictRead.ts` — defines getDictionaryItems (GET `/v1/dictionaries/{typeCode}/items`).
- **Hook:** `frontend/dfbs-ui/src/features/dicttype/hooks/useDictionaryItems.ts` — consumes dictRead.
- **Request interceptor:** `frontend/dfbs-ui/src/shared/utils/request.ts` — special-cases `/v1/dictionaries/` (read-only) for error handling.
- **Page usage:** Only `frontend/dfbs-ui/src/pages/Admin/DictionaryTypes/index.tsx` — “读取示例（只读）” collapsible demo (useDictionaryItems, readDemoTypeCode, 刷新).

**Conclusion:** The only consumer of the dict read contract (GET /api/v1/dictionaries/{typeCode}/items) in the frontend today is the 04.c demo on the 字典类型 page. No business forms yet use it.

---

## Item 5: Minimal snapshot mechanism proposal (fact-based)

**Chosen approach:** **A) Store both dict value + labelSnapshot on the business record at save time; display uses labelSnapshot.**

**Justification from Items 1–4:**

- Item 1: Business tables already use “id/code + name” for stability (customer_id + customer_name, contract_name). Value-only codes (expense_type, etc.) have no historical label. For dict-driven fields, adding a label snapshot aligns with existing denormalized-name pattern and avoids guessing labels at read time.
- Item 2: No existing label-snapshot pattern for dict; introducing one is additive. Writing a name at save time is already done for customer_name / contract_name.
- Item 3: Forms submit value only. To get a stable label we must capture it at save time—either frontend sends { value, label } and backend persists both, or backend resolves label from dict by value and persists it.
- Item 4: No business forms use the dict read API yet, so we can define the first contract: backend can accept optional label from frontend or resolve from DictionaryReadService and store value + labelSnapshot.

**Where to capture the snapshot:**

- **Recommendation: Backend save.** Backend already owns persistence and denormalized names (customer_name, contract_name). On create/update of a record that has a dict-backed field, backend should: (1) accept or derive the current label (e.g. from request body or by calling dict read by typeCode + value), (2) persist `xxx_value` (or existing code column) and `xxx_label_snapshot` (or equivalent). Display (DTO/UI) uses the snapshot; no need to resolve label at read time for history.

**Alternative:** Frontend could send `{ value, label }` and backend persist both; backend then does not need to call dict read, but must trust/validate label. Backend save with resolution from dict is safer and keeps a single source of truth for “current” label at save time.

---

*Evidence-only; no code or behavior changed.*
