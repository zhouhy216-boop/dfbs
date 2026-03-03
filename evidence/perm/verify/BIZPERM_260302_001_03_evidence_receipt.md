# BIZPERM-260302-001-03-EVID — 证据收据（只读）

**Request ID:** BIZPERM-260302-001-03-EVID  
**Purpose:** 为 Step-03（按业务模块视图的按账号直接分配）收集真实项目事实，不做任何代码修改。

---

## Completed?

Yes.

---

## Findings

**Item 1 — 账号编辑注入点与现有区块**
- 账号编辑 UI 为 **Drawer**，位于 `frontend/dfbs-ui/src/pages/Admin/AccountPermissions/AccountsTab.tsx`：`open={accountDetailDrawerOpen && selectedUser != null}` 约 593 行；抽屉内顺序为账号信息 → 启用/重置密码 → **业务模块视图**区块（约 624–656 行，含 `BizPermCatalogMaintenance` 或占位）→ 保存/还原、分配角色模板、**差异视图**（约 665–737+ 行：保存/还原、角色模板选择、差异视图 1/2/3、高级编辑）。按账号 override 的 diff-only UI 与业务模块视图为同一抽屉内上下相邻独立区块，互不覆盖。

**Item 2 — 目录可读性 vs 维护性（门控拆分）**
- 当前目录接口：**GET /api/v1/admin/bizperm/catalog** 返回 tree + unclassified（`BizPermCatalogController`，`@RequestMapping("/api/v1/admin/bizperm/catalog")`，约 19、37–40 行）；门控为 **PermSuperAdminGuard.requirePermSuperAdmin()**，非 allowlist 返回 403。
- **不存在** 面向 admin（非 allowlist）的只读目录接口；当前仅此一个 GET，且为 allowlist-only。
- **建议**：若 Step-03 需让普通 admin 在业务模块视图中“只读目录 + 按账号勾选分配”，需新增 **admin 只读** 目录接口（例如 GET 同 path 或只读子路径），由 AdminOrSuperAdminGuard 保护，返回与现有一致的 tree + unclassified，不暴露写操作。

**Item 3 — “不依赖角色模板”与当前 override 存储**
- Override 合约：**GET/PUT /api/v1/admin/account-permissions/accounts/{userId}/override**（`AccountPermissionsController` 211–240 行）；请求体 `SaveAccountOverrideRequest(roleTemplateId, addKeys, removeKeys)`（`PermAccountOverrideDto` 38–46 行），`roleTemplateId` 为 `Long` 可空。
- 逻辑路径：`PermAccountOverrideService.getOverride`（66–91 行）当无模板或 `roleId==null` 时 `roleTemplateId=null`、`templateKeys` 为空，effective = addKeys − removeKeys；`saveOverride`（94–136 行）当 `roleTemplateId != null` 时校验角色存在且启用，`templateRow.setRoleId(roleTemplateId)` 可传 null（116 行），add/remove 按列表全量替换。
- **结论**：可以“直接分配、不依赖模板”：存 **roleTemplateId = null**，**addKeys = 勾选 key 列表**，**removeKeys = []**；GET 返回的 effective 即为 addKeys；后续仅凭此数据即可还原，无需角色模板。约束：addKeys/removeKeys 中的 key 须通过 `roleService.validatePermissionKey(key)`（106 行），即须在权限树中存在。

**Item 4 — 权限键与中文标签可用于 admin 分配 UI**
- 目录操作点（含未归类）字段：**permissionKey**、**cnName**（可空）、**handledOnly**、**sortOrder**、**id**（持久化有 id，计算未归类为 null）；见 `BizPermCatalogDto.OpPoint`、前端 `bizpermCatalogService.ts` 的 `OpPoint`。
- 未归类计算项（id=null）：出现在 GET catalog 的 unclassified 列表中，前端以 `u.id ?? u.permissionKey` 作 key；超管可认领到节点（后端会落库）；若 Step-03 允许 admin 对账号“直接勾选分配”，可同样允许勾选这些 key（id=null 仅影响是否在维护 UI 中编辑该条，不影响用 permissionKey 写入 override）。
- 现有 BizPerm 维护 UI（`BizPermCatalogMaintenance.tsx`）：标签与空态均为中文（如「目录树」「中文名称」「权限键」「仅已处理」「排序」「未归类/待认领」「从未归类添加」「一键认领到当前节点」「暂无未归类权限」「搜索权限键或中文名」），可直接复用于按账号分配时的展示与提示。

**Item 5 — 搜索/筛选可行性**
- 现有搜索：`BizPermCatalogMaintenance.tsx` 中 **unclassifiedSearch** 状态（约 67 行），**Input.Search** 两处（约 369、419 行），placeholder「搜索权限键或中文名」/「搜索」，过滤逻辑为 `unclassifiedFiltered`（约 104 行）：按 `permissionKey` 与 `cnName` 的 toLowerCase 包含匹配。该搜索仅作用于**未归类列表**。若 Step-03 需“按模块/操作点”搜索整棵目录或操作点列表，需在现有 catalog 数据结构（tree + unclassified）上在前端做树/扁平过滤，或复用同一 `unclassifiedSearch` 风格增加“模块/节点/操作点”的过滤状态与 UI，当前无后端按模块/op 的专用搜索接口。

---

## Not found items

- 无“admin 只读”的 bizperm catalog 接口（见 Item 2）。
- 无专门“按模块/操作点”的后端搜索接口（Item 5 可前端实现）。

---

## Full-suite build

Not executed（证据只读，未改代码）。

---

## Blocker question

None.
