# TBLWHEEL-260311-001-EVID — Table capability wheel evidence (read-only)

**Completed?** Yes

**Findings**

- **Item 1 (column settings / density / refresh source):** They are **not** from a shared project component or wrapper. Every list page imports `ProTable` from `@ant-design/pro-components` and configures it locally. No shared table wrapper exists in `shared/` or `features/`. The only shared table-related code is `toProTableResult` in `frontend/dfbs-ui/src/shared/utils/adapters.ts` (data shape only). Explicit toolbar `options` appears in one place only: **Customer** sets `options={{ reload: true }}` (Customer/index.tsx ~78). All other ProTable usages do not set `options`; any toolbar controls (reload, density, column settings) come from **ProTable’s library defaults**, not from a project-level shared pattern.
- **Item 2 (pages with vs without these controls):**  
  - **ProTable list pages (toolbar behavior = library default unless overridden):** Customer, Quote, Shipment, AfterSales, Finance, Platform/Org, Platform/Application, Warehouse/Inventory, Warehouse/Replenish, MasterData (Contract, Machine, MachineModel, ModelPartList, SparePart, SimCard), WorkOrder/Internal (four ProTables), Admin DictionaryTypes, DictionaryItems, System PlatformConfig. Embedded ProTables in MasterData/Machine/Detail and SimCard/Detail use `options={false}` and `toolBarRender={false}` (no toolbar).  
  - **Pages that do not use ProTable for the main list:** Admin AccountPermissions/AccountsTab (antd `Table`), BasicLayout role matrix modal (antd `Table`). These have no ProTable toolbar; no column settings/density/refresh from ProTable.
- **Item 3 (code path for pages that show controls):** **Page-local only.** Each of the listed ProTable pages has its own `<ProTable ...>` with its own `actionRef`, `columns`, `request`, `toolBarRender`. No shared component or config wraps them. Path: `pages/<Module>/<Page>/index.tsx` (or Detail.tsx) → direct use of `ProTable` from `@ant-design/pro-components`.
- **Item 4 (UI column-width adjustment):** **Not found.** Grep for `resizable`, `columnWidth`, `resize.*column` in `frontend/dfbs-ui/src` returned no matches. No UI column-width drag/resize implementation found in repo.
- **Item 5 (reuse judgment facts):**  
  - **Table base/list pattern:** Shared only as **data adapter** — `toProTableResult` in `shared/utils/adapters.ts`. ProTable usage is per-page; no shared list/table component.  
  - **Column settings:** No shared implementation; if present, from ProTable library default per page.  
  - **Density:** No shared implementation; if present, from ProTable library default per page.  
  - **Refresh:** Only **Customer** explicitly sets `options={{ reload: true }}`; other pages rely on library default and/or custom buttons that call `actionRef.current?.reload()`. No shared refresh pattern.  
  - **UI column resize:** Not present in repo.

**Not found**

- Shared table wrapper or shared ProTable config for toolbar (column settings, density, refresh).
- Any UI column-width adjustment (resizable columns) in the codebase.

**Build/test status:** Not run (evidence-only, read-only).

**Blocker question:** None.
