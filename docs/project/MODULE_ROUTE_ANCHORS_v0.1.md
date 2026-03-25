# 模块-路由锚点表 v0.1（以“主入口路由”为锚点）

使用规则（统一口径）：
- “业务模块”≠“单一路由”。但每个模块必须指定 1 个“主入口路由（锚点）”。
- 同模块的详情/新建/历史等，都归为“相关路由（子页面）”。
- 发现“同一件事多个入口”时：必须标注“别名/重复入口”，后续再统一菜单口径。

---

## A) 公共入口（无需登录）
| 模块 | 主入口路由（锚点） | 相关路由 | 当前页面名 | 访问前置 | 备注 |
|---|---|---|---|---|---|
| 登录 | `/login` | - | Login | 无 | 登录后会跳转到 `/` |
| 客户自助报修（公共工单） | `/public/repair` | - | Public repair | 无 | 面向客户/外部使用 |

---

## B) 业务入口（登录后：业务侧）
| 业务模块 | 主入口路由（锚点） | 相关路由（子页面） | 当前页面名（系统里怎么叫） | 访问前置 | 备注 |
|---|---|---|---|---|---|
| 主页/看板 | `/dashboard` | - | Dashboard | 登录 | 当前主页内容还很空 |
| 报价与收费（统一报价对象） | `/quotes` | - | 报价单 (Quotes) | 登录 | 列表/创建/编辑/提交/作废/导出 |
| 交付发货（物流） | `/shipments` | - | 发货列表 (Shipments) | 登录 | 含接收/发货/完成/异常/取消/导出 |
| 运输异常（物流异常） | `/after-sales` | `/after-sales/:id` | 运输异常 / 运输异常详情 | 登录 | 详情含提交/接收/处理/退回/完成等动作 |
| 售后工单中心（内部工单） | `/work-orders` | `/work-orders/:id` | 工单管理 / 工单详情 | 登录 + `work_order:VIEW` | 这是“售后主线”的锚点 |
| 财务 | `/finance` | - | 财务 (Finance) | 登录 | 支付/费用/对账等 |
| 仓库与配件（库存台账） | `/warehouse/inventory` | - | 库存管理 | 登录 | 库存台账入口 |
| 补货审批 | `/warehouse/replenish` | - | 补货审批 | 登录 | 我的待办/我的申请等 |
| 数据导入中心 | `/import-center` | - | 数据导入 | 登录 | 客户/合同/型号/零部件/机器/SIM 等导入 |
| 主数据-客户 | `/customers` | - | 客户 (Customers) | 登录 | 客户列表/合并 |
| 主数据-合同 | `/master-data/contracts` | - | 合同 | 登录 | 合同列表/创建/编辑/禁用 |
| 主数据-机器型号 | `/master-data/machine-models` | `/master-data/machine-models/:id` | 机器型号 | 登录 | 列表 + 详情 |
| 主数据-零部件 | `/master-data/spare-parts` | - | 零部件 | 登录 | 列表/创建/编辑/禁用 |
| 主数据-机器（设备号=机器号） | `/master-data/machines` | `/master-data/machines/:id` | 机器 | 登录 | 列表 + 详情 + 历史 |
| 主数据-SIM卡 | `/master-data/sim-cards` | `/master-data/sim-cards/:id` | SIM卡 | 登录 | 列表 + 详情 + 历史 |
| 主数据-型号BOM | `/master-data/model-part-lists` | - | 型号BOM | 登录 | 草稿/发布/冲突等 |
| 平台&网卡管理-平台组织 | `/platform/orgs` | - | 平台管理 | 登录 + `platform_application.orgs:VIEW` | 平台组织/客户等 |
| 平台&网卡管理-平台申请 | `/platform/applications` | `/platform/applications/history` `/platform/apply` `/platform/applications/reuse` `/platform/applications/verification` `/platform/applications/sim-activation` | 申请管理 + 一组申请相关页面 | 登录 + `platform_application.applications:VIEW`（主入口） | 这里是“平台申请/开卡”相关主入口 |
| 平台&网卡管理-SIM管理 | `/platform/sim-applications` | - | SIM管理 | 登录 | SIM申请相关 |

> 业务模块“评审协同（营业企画）/生产准备（生产企画）/入网准备协作（营业企画→生产企画→平台主管）”：
- 目前在现有路由清单里没有独立页面锚点（属于“待实现模块”）；先挂在模块地图里，后续讨论到它时再补路由锚点。

---

## C) 系统/后台入口（登录后：系统侧）
| 系统模块 | 主入口路由（锚点） | 相关路由（子页面） | 当前页面名（系统里怎么叫） | 访问前置 | 备注 |
|---|---|---|---|---|---|
| 数据确认中心 | `/admin/confirmation-center` | - | 数据确认中心 | 登录 | 后台确认池 |
| 平台配置（系统配置） | `/system/platform-config` | - | 平台配置 | 登录 | 系统配置入口 |
| 数据字典（别名入口） | `/admin/data-dictionary` | - | 数据字典 | 登录 + 超管 | ⚠️该入口“等同字典类型”（重复入口/别名） |
| 字典中心（字典类型） | `/admin/dictionary-types` | - | 字典类型 | 登录 + 超管 | 建议后续只保留一个菜单入口 |
| 字典项管理 | `/admin/dictionary-types/:typeId/items` | - | 字典项管理 | 登录 + 超管 | |
| 状态流（迁移规则） | `/admin/dictionary-types/:typeId/transitions` | - | 状态流(迁移规则) | 登录 + 超管 | |
| 历史显示示例 | `/admin/dictionary-snapshot-demo` | - | 历史显示示例 | 登录 + 超管 | |
| 组织架构 | `/admin/org-tree` | - | 组织架构 | 登录 + 超管 | |
| 层级配置 | `/admin/org-levels` | - | 层级配置 | 登录 + 超管 | |
| 变更记录 | `/admin/org-change-logs` | - | 变更记录 | 登录 + 超管 | |
| 账号与权限 | `/admin/account-permissions` | - | 账号与权限 | 登录 + 管理员或超管 | |
| 角色与权限 | `/admin/roles-permissions` | - | 角色与权限 | 登录 + 权限白名单（Perm 超管） | |

---

## D) 路由别名 / 重定向（用于“统一口径”）
| 别名路由 | 实际跳转到 | 备注 |
|---|---|---|
| `/` | `/dashboard` | 默认首页 |
| `/logistics` | `/shipments` | 物流模块别名入口 |
| `/after-sales-service` | `/work-orders` | 售后服务模块别名入口 |
| `/master-data` | `/master-data/contracts` | 主数据模块别名入口 |
| `/platform` | `/platform/applications` | 平台模块别名入口 |
| `/admin` | `/admin/confirmation-center` | 系统后台别名入口 |
| `*` | `/` | 未匹配路由统一回首页 |

---

## E) 待实现模块（先占位，后续逐模块细聊时补锚点）
| 业务模块 | 预期主入口路由（暂空） | 备注 |
|---|---|---|
| 评审协同（营业企画） | （暂无） | 目前未在路由清单中看到独立入口 |
| 生产准备（生产企画） | （暂无） | 目前未在路由清单中看到独立入口 |
| 入网准备协作（营业企画→生产企画→平台主管） | （暂无） | 未来可能落在“机器详情/合同详情/平台&SIM模块”之一，等细聊再定 |
| 报表中心（领导自选） | （暂无） | 目前只有 Dashboard；报表中心未见独立入口 |

---

## Repo reality check (stage baseline rebuild 2025-02-24; commit 23467d7d)

- **Already in repo:** All routes in sections A–D exist in `frontend/dfbs-ui/src/App.tsx`. Aliases and redirects match. Left menu does not show 角色与权限 or 字典类型 (removed per menu cleanup); 账号与权限 and 数据字典 remain. Account-permissions page: create/edit account with 主业务角色 (Primary Business Role); PUT `/api/v1/admin/account-permissions/accounts/{userId}` for profile update.
- **Menu vs route:** 角色与权限 is reachable by direct URL `/admin/roles-permissions` but not in left menu.

## Conflicts with current repo reality

- None. Product text describes “字典类型” and “数据字典” as duplicate/alias; repo reflects that (one menu entry removed). 账号与权限 is Admin or Super Admin; 角色与权限 is Perm Super Admin (different guard).

## Anchor gaps / not yet present in repo

- **评审协同（营业企画）:** No route or page; no contract review flow, no review record entity. MODULE_ROUTE_ANCHORS correctly states “目前未在路由清单中看到独立入口”.
- **生产准备（生产企画）、入网准备协作、报表中心:** No dedicated routes; product intent unchanged; repo has no new anchors for these.