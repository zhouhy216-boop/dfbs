# REUSABLE_BLOCKS_ZH — 可复用模块一览（中文）

- **As-of:** 2025-02-24 14:00
- **Repo:** main
- **Commit:** 23467d7d
- **Verification method:** Grep imports/usages in `frontend/dfbs-ui/src`; `shared/form/index.ts`, `shared/table/UnifiedProTable.tsx`, `shared/hooks/useDraftForm.ts`.

**仅事实。** 路径均在 `frontend/dfbs-ui/src/` 下。用途以页面/流程描述，不列代码路径。

---

## 1. SmartInput

- **用途：** 输入框，失焦时自动整理（去空格、仅字母、大写等），与表单联动。
- **位置：** `shared/components/SmartInput/index.tsx`。
- **使用场景：** 平台申请页（联系人、电话、邮箱、合同号、组织代码等）；平台组织页（组织代码、联系电话、邮箱）。

---

## 2. SmartReferenceSelect

- **用途：** 从后端智能选择接口按实体类型加载选项的下拉框，支持搜索与表单联动。
- **位置：** `shared/components/SmartReferenceSelect/index.tsx`。
- **使用场景：** 平台申请（客户、合同）；平台组织（组织/客户引用）；工单对外报修；工单对内；数据确认中心。

---

## 3. DuplicateCheckModal

- **用途：** 展示重复检查结果的弹窗，可自定义底部按钮（如返回编辑、确认新增、我要开卡）。
- **位置：** `features/platform/components/DuplicateCheckModal/index.tsx`。
- **使用场景：** 平台申请（创建/规划人提交前重复检查后）；平台组织（重复检查后）。

---

## 4. HitAnalysisPanel

- **用途：** 调用重复检查接口并展示命中列表（客户/电话/邮箱/组织），可配合 DuplicateCheckModal。
- **位置：** `features/platform/components/HitAnalysisPanel/index.tsx`。
- **使用场景：** 平台申请（管理员弹窗右侧命中分析）；平台组织（重复检查面板）。

---

## 5. validators（PhoneRule、EmailRule、ContractRule、OrgCodeRule 等）

- **用途：** 统一校验规则（电话、邮箱、合同号、组织代码等），供表单使用。
- **位置：** `features/platform/utils/validators.ts`。
- **使用场景：** 平台申请；平台组织。

---

## 6. request（axios 实例与 token 工具）

- **用途：** 统一请求实例（baseURL /api）、从本地存储取 Bearer token、401 时清 token 并跳转登录；对字典只读接口请求附加 Cache-Control: no-cache。
- **位置：** `shared/utils/request.ts`。
- **使用场景：** 所有调用后端的页面与服务（客户、报价、发货、财务、售后、工单、仓储、导入、主数据、平台、系统/字典、登录等）。

---

## 7. adapters（toProTableResult、SpringPage）

- **用途：** 将 Spring 分页结果转成 ProTable 所需格式（data、total、success）。
- **位置：** `shared/utils/adapters.ts`。
- **使用场景：** 运输异常、客户、报价、发货、财务、主数据（合同、机器型号、型号BOM、机器、SIM卡、零部件）、平台组织等列表页。

---

## 8. AttachmentList

- **用途：** 附件列表的展示与操作（上传、列表、删除），用于支持附件的业务实体。
- **位置：** `shared/components/AttachmentList.tsx`。
- **使用场景：** 报价单页；发货页。

---

## 9. useEffectivePermissions

- **用途：** 获取当前用户有效权限键（调用 GET /api/v1/perm/me/effective-keys），用于按钮与路由的权限控制。
- **位置：** `shared/hooks/useEffectivePermissions.ts`。
- **使用场景：** 发货页（无 VIEW 不可进页、可执行操作按钮按权限过滤）；其他按权限键控制入口的页面。

---

## 10. useAuthStore

- **用途：** 登录态（token、用户信息）的 Zustand 仓库，与 request 的 token 读写一致。
- **位置：** `shared/stores/useAuthStore.ts`。
- **使用场景：** 登录页；整体布局（头像、退出）；工单详情；权限控制组件 Access。

---

## 11. platformConfig 服务

- **用途：** 拉取平台配置（选项、校验规则），供平台下拉与校验使用。
- **位置：** `features/platform/services/platformConfig.ts`。
- **使用场景：** 平台申请；平台组织；系统「平台配置」页。

---

## 12. Access

- **用途：** 按权限控制子内容展示，无权限时显示 fallback；配合 useAccess(permission)。
- **位置：** `shared/components/Access.tsx`。
- **使用场景：** 用于按权限控制 UI。具体页面列表：未逐项核对。

---

## 13. TypeToConfirmModal

- **用途：** 需用户输入指定确认文案后才执行危险操作的弹窗。
- **位置：** `shared/components/TypeToConfirmModal/index.tsx`。
- **使用场景：** 系统「层级配置」重置确认；「组织架构」禁用/移动/删除确认；「变更记录」确认操作。

---

## 14. SuperAdminGuard

- **用途：** 仅超管可访问的子路由或内容，否则重定向或隐藏。
- **位置：** `shared/components/SuperAdminGuard.tsx`。
- **使用场景：** 路由：数据字典、层级配置、组织架构、变更记录、字典类型、字典项管理、状态流(迁移规则)、历史显示示例；侧栏中上述菜单项通过 useIsSuperAdmin() 控制显示。

---

## 15. PermSuperAdminGuard、AdminOrSuperAdminGuard、PlatformViewGuard、WorkOrderViewGuard

- **用途：** 路由守卫：角色与权限（Perm 白名单）；账号与权限（管理员或超管）；平台管理/申请管理（按权限）；工单管理（work_order:VIEW）。
- **位置：** `shared/components/` 下对应 Guard 组件。
- **使用场景：** 角色与权限页；账号与权限页；平台管理、申请管理；工单列表与工单详情。

---

## 16. TestDataCleanerModal

- **用途：** 测试数据清理器弹窗（预览/执行），仅超管可见。
- **位置：** `shared/components/TestDataCleaner/Modal.tsx`。
- **使用场景：** 布局右上角「测试数据清理器」链接（仅超管显示）。

---

## 17. OrgTreeSelect

- **用途：** 从组织树中选择组织节点。
- **位置：** `features/orgstructure/components/OrgTreeSelect.tsx`。
- **使用场景：** 系统「变更记录」按组织节点筛选。

---

## 18. OrgPersonSelect

- **用途：** 从组织架构中选择人员。
- **位置：** `features/orgstructure/components/OrgPersonSelect.tsx`。
- **使用场景：** 系统「组织架构」中为节点分配人员等。

---

## 19. useDictionaryItems

- **用途：** 按字典类型编码拉取字典项；每次 reload() 都会发请求；可传是否含禁用、父级、搜索等参数。
- **位置：** `features/dicttype/hooks/useDictionaryItems.ts`。
- **使用场景：** 系统「字典类型」页的读取示例折叠面板。

---

## 20. getDictionaryItems

- **用途：** 按字典类型编码拉取字典项（只读接口，无需登录）。
- **位置：** `features/dicttype/services/dictRead.ts`。
- **使用场景：** 字典类型页读取示例；历史显示示例页；报价单页费用类型下拉。

---

## 21. getTransitionsRead、listTransitionsAdmin、upsertTransitionsAdmin

- **用途：** getTransitionsRead：业务侧只读「允许的状态迁移」；listTransitionsAdmin/upsertTransitionsAdmin：超管维护某类型的迁移规则（列表与批量保存）。
- **位置：** `features/dicttype/services/dictTransition.ts`。
- **使用场景：** 状态流(迁移规则)页：底部「业务读取预览」用 getTransitionsRead；列表加载与保存用 listTransitionsAdmin、upsertTransitionsAdmin。

---

## 22. shared/form（表单轮子）

- **用途：** 最小可复用表单容器、分组区块、草稿条、只读视图、通用字段（电话/邮箱/文本）、校验规则、模板 hook；平台申请创建弹窗已接入（分组、说明、草稿、恢复默认、只读预览）。
- **位置：** `shared/form/`（FormSection、FormContainer、DraftAlert、ReadonlyFormView、FormFields、useFormReadonly、useFormTemplate、formValidators、formWheelStyles.css）；入口 `shared/form/index.ts`。
- **使用场景：** 平台申请创建弹窗（待处理/新建申请 与 销售·营企·服务申请 入口）；合同评审 V1 消费者尚未在仓库中。

---

## 23. UnifiedProTable、useTableColumnsState、CopyableCell、ResizableTitle

- **用途：** 基于 ProTable 的统一列表：列状态与列宽持久化、密度、刷新、恢复默认、列宽拖拽（表头右缘）、斑马纹、空态/加载态、可复制单元格；按 tableKey 存 localStorage。
- **位置：** `shared/table/UnifiedProTable.tsx`、`useTableColumnsState.ts`、`CopyableCell.tsx`、`ResizableTitle.tsx` 等；入口 `shared/table/index.ts`。
- **使用场景：** 客户、合同、账号列表、报价、发货、运输异常、财务、库存与补货、平台组织与申请、主数据（合同、SIM卡、零部件、机器、型号、型号BOM）、字典类型与项、数据确认中心、平台配置、工单、导入中心结果表；详情页内嵌表格（机器/SIM卡/型号详情）。

---

## 24. useDraftForm

- **用途：** 按 key 将表单草稿写入/读出/清除 localStorage；hasDraft、saveDraft(values)、loadDraft()、clearDraft()。
- **位置：** `shared/hooks/useDraftForm.ts`。
- **使用场景：** 平台申请创建弹窗（按渠道/入口区分 key）；申请管理营企确认弹窗；表单轮子 DraftAlert。

---

## 25. useSimulatedRoleStore、roleToUiGatingMatrix

- **用途：** 仅界面层面的角色模拟：顶栏下拉选中的模拟角色存入 store；矩阵与辅助函数负责左侧菜单与各页操作按钮按模拟角色显隐/禁用及 tooltip「该角色不可操作」。
- **位置：** `shared/stores/useSimulatedRoleStore.ts`；`shared/config/roleToUiGatingMatrix.ts`。
- **使用场景：** 顶栏（模拟角色下拉、角标、免责说明、角色-界面矩阵查看）；发货列表页（可执行操作按钮）；工单管理列表与详情（新建工单、受理、派单、驳回、接单）；平台管理页（销售申请、服务申请、营企申请、新建机构、编辑、删除）；申请管理页（通过、驳回、提交至管理员、关闭申请）。

---

## Reuse status（复用状态）

- 1–21：当前使用场景下可直接复用；request/adapters/字典读取/状态流读取为多页共用。
- 22（shared/form）：可直接复用；当前仅平台申请创建弹窗接入；合同评审 V1 消费者未在仓库。
- 23–24（UnifiedProTable、useDraftForm）：可直接复用；tableKey/草稿 key 需按页面/标签唯一。
- 25（角色模拟 store 与矩阵）：仅界面模拟，不改变后端身份或权限；可直接复用于当前验收场景。若需“真实”角色业务流，须依赖账号主业务角色与后端权限，不可仅靠模拟角色。

---

## Not verified

- Access 组件：具体哪些页面用其做权限控制未逐项枚举。
