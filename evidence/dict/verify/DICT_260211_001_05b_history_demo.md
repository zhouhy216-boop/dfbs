# DICT-260211-001-05.b History demo verification

**Ticket:** DICT-260211-001-05.b — Historical label stability (v1): store label snapshot at save time + minimal Admin demo.

**Purpose:** UI-oriented evidence that old records keep saved label; rename/disable does not change them; new records get new snapshot.

---

## Build

- **Backend:** `backend/dfbs-app/mvnw.cmd -q compile -DskipTests` — PASS
- **Frontend:** Not run full build (existing TS errors elsewhere); new files compile in isolation.

---

## Steps performed (verification story)

1. **Create a demo record**
   - Open 历史显示示例 (`/admin/dictionary-snapshot-demo`).
   - 字典类型(typeCode) left as default `payment_method` (or enter it).
   - 字典项: select an item from the dropdown (e.g. one with label "现金" or similar).
   - 备注: optional.
   - Click "保存示例记录".
   - **Observation:** Record appears in the table; column "保存时标签" shows the label at save time (e.g. "现金"); "保存时值" shows the value (e.g. "cash").

2. **Rename or disable the dict item**
   - Go to 字典类型 → open the type → 字典项管理 (or navigate to the item’s type and edit the item).
   - Rename the same dictionary item (e.g. change "现金" to "现金（旧）") or disable it.
   - Save.

3. **Verify old record unchanged**
   - Return to 历史显示示例.
   - **Observation:** The record created in step 1 still shows the **original** "保存时标签" (e.g. "现金"). It does **not** show the new label or disappear. Display uses `item_label_snapshot` only (historical stability).

4. **Create another demo record after rename**
   - On the same page, select the same 字典类型, and in 字典项 select the (now renamed) item.
   - Click "保存示例记录".
   - **Observation:** The **new** record shows the **new** "保存时标签" (e.g. "现金（旧）"). New records get the current label as snapshot at save time.

---

## Summary

- **Create record:** 保存时标签 and 保存时值 stored and shown.
- **After rename/disable:** Existing record’s 保存时标签 unchanged.
- **New record after rename:** Shows new label as 保存时标签.

Screenshots can be added under this section if captured (optional).

---

## Evidence file path

`evidence/dict/verify/DICT_260211_001_05b_history_demo.md`
