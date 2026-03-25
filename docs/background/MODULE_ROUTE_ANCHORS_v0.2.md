# MODULE_ROUTE_ANCHORS_v0.2

> 目的：为项目背景讨论提供统一的“逻辑路由锚点”命名约定。本文不描述当前实现状态，不判断是否已存在页面，只定义后续在 ChatGPT / Cursor / 任务单中可统一使用的模块锚点语言。

## 1. 使用规则
- 本文中的路由是“背景层逻辑锚点”，用于统一讨论入口，不等于当前项目已实现页面。
- 一个业务模块至少约定一个主入口锚点；详情页、历史页、子功能页归为相关锚点。
- 路由命名优先体现业务语义，而不是技术目录。
- 同一业务尽量只保留一个主入口锚点，避免多个别名长期并存。

## 2. 公共入口锚点
| 模块 | 主入口锚点 | 相关锚点 | 说明 |
|---|---|---|---|
| 登录 | `/login` | - | 统一登录入口 |
| 客户自助报修 | `/public/repair` | - | 面向客户的公共报修入口 |

## 3. 业务侧逻辑锚点
| 模块 | 主入口锚点 | 相关锚点 | 主对象 / 说明 |
|---|---|---|---|
| 购销合同评审 | `/contract-reviews` | `/contract-reviews/:id` | 合同评审单 |
| 发货前准备 / 装机信息回填 | `/pre-shipment-preparations` | `/pre-shipment-preparations/:id` | 发货前准备信息承接记录 |
| 发货申请与签收 | `/shipments` | `/shipments/:id` `/shipments/:id/signoff` | 发货申请单、提货票、回执单 |
| 委托发货 | `/entrusted-shipments` | `/entrusted-shipments/:id` | 委托发货单 |
| 运输异常 | `/transport-exceptions` | `/transport-exceptions/:id` | 运输异常记录 |
| 平台费入口 | `/platform-billing-entries` | `/platform-billing-entries/:id` | 平台来源报价依据 |
| SIM费入口 | `/sim-billing-entries` | `/sim-billing-entries/:id` | SIM来源报价依据 |
| 售后工单中心 | `/work-orders` | `/work-orders/:id` | 工单 |
| 仓库与配件 | `/warehouses` | `/warehouses/inventory` `/warehouses/stock-movements` | 仓库、库存单据 |
| 报价单体系 | `/quotes` | `/quotes/:id` `/quotes/demands` | 需求单、报价单、报价明细 |
| 结算层 | `/settlements` | `/settlements/:id` `/settlements/merge/:id` | 结算记录、合并结算单 |
| 纠偏 / 纠错 / 退票 | `/settlement-adjustments` | `/settlement-adjustments/:id` | 冲销、退款、退票等特殊流程 |
| 主数据-客户 | `/master-data/customers` | `/master-data/customers/:id` | 客户主体 / 客户业务单位 |
| 主数据-合同 | `/master-data/contracts` | `/master-data/contracts/:id` | 购销合同、服务补充合同 / 协议 |
| 主数据-机型 | `/master-data/models` | `/master-data/models/:id` | 机型 |
| 主数据-机器 | `/master-data/machines` | `/master-data/machines/:id` | 机器 |
| 主数据-关键件SN | `/master-data/critical-part-bindings` | `/master-data/critical-part-bindings/:id` | 机器关键零部件 SN |
| 主数据-SIM | `/master-data/sims` | `/master-data/sims/:id` | SIM |
| 主数据-平台 | `/master-data/platforms` | `/master-data/platforms/:id` | 平台配置、客户平台 / 平台机构 |
| 主数据-BOM / 零部件 | `/master-data/boms` | `/master-data/boms/:id` `/master-data/parts/:id` | BOM、零部件 |
| 主数据-现场信息 | `/master-data/site-info` | `/master-data/site-info/:id` | 联系人 / 地址 / 点位 |
| 成本中心 | `/costs` | `/costs/:id` | 成本记录 |
| 出差 / 借款 / 报销 | `/expense-workflows` | `/expense-workflows/trips` `/expense-workflows/reimbursements` `/expense-workflows/loans` | 出差申请、借款、补助、报销 |
| 盖章申请 | `/seal-applications` | `/seal-applications/:id` | 盖章申请记录 |
| 故障知识库 | `/fault-knowledge` | `/fault-knowledge/:id` | 故障知识条目 |
| 批量不良预警 | `/batch-defect-alerts` | `/batch-defect-alerts/:id` | 批量不良预警记录 |
| 物流商应付 | `/vendor-payables/logistics` | `/vendor-payables/logistics/:id` | 物流商对账单 |
| 平台商应付 | `/vendor-payables/platforms` | `/vendor-payables/platforms/:id` | 平台商月账单 |
| SIM卡商应付 | `/vendor-payables/sims` | `/vendor-payables/sims/:id` | SIM卡商月账单 |
| 快递商应付 | `/vendor-payables/couriers` | `/vendor-payables/couriers/:id` | 快递商月账单 |
| SLA统计 | `/sla-results` | `/sla-results/:id` | SLA结果 |
| 服务经理业绩管理 | `/performance/service-managers` | `/performance/service-managers/:id` | 月任务 / 月预计 / 已完成 / 差额 |
| 报表输出层 | `/reports` | `/reports/fixed` `/reports/topics` `/reports/ad-hoc` | 固定输出、专题输出、灵活分析 |

## 4. 系统侧逻辑锚点
| 模块 | 主入口锚点 | 相关锚点 | 主对象 / 说明 |
|---|---|---|---|
| 数据确认中心 | `/admin/data-confirmations` | `/admin/data-confirmations/:id` | 主数据待确认项 |
| 数据字典 | `/admin/dictionaries` | `/admin/dictionaries/:id/items` | 字典类型 / 字典项 |
| 组织架构 | `/admin/org-structures` | `/admin/org-structures/:id` | 内部组织、层级、组织树 |
| 内部人员 | `/admin/internal-people` | `/admin/internal-people/:id` | 内部人员主体 |
| 外部主体 | `/admin/external-subjects` | `/admin/external-subjects/:id` | 客户、供应商、其他外部协作主体 |
| 外部联系人 | `/admin/external-contacts` | `/admin/external-contacts/:id` | 外部联系人 |
| 账号与身份绑定 | `/admin/accounts` | `/admin/accounts/:id` | 账号、账号绑定主体 |
| 角色与权限 | `/admin/roles-permissions` | `/admin/roles-permissions/:id` | 角色、权限规则、授权关系 |
| 流程配置 | `/admin/workflows` | `/admin/workflows/:id` | 流程模板、节点、流转规则 |
| 表单配置 | `/admin/forms` | `/admin/forms/:id` | 表单模板、字段承接方式 |
| 动作与提醒规则 | `/admin/action-rules` | `/admin/action-rules/:id` | 按钮动作、确认提醒、规则提示 |
| 审计日志 | `/admin/audit-logs` | `/admin/audit-logs/:id` | 操作留痕 |
| 历史版本 | `/admin/version-history` | `/admin/version-history/:id` | 历史版本快照、回看、恢复依据 |
| 基础配置项 | `/admin/base-configs` | `/admin/base-configs/:id` | 通用系统配置项、预留配置项 |
| 附件中心 | `/admin/attachments` | `/admin/attachments/:id` | 附件、归属、预览控制 |
| 打印模板 | `/admin/print-templates` | `/admin/print-templates/:id` | 打印模板 |
| 通知模板 | `/admin/notification-templates` | `/admin/notification-templates/:id` | 短信 / 邮件 / 站内消息模板 |
| 消息中心 | `/messages` | `/messages/:id` | 站内消息、未读消息、消息查看 |
| 测试数据清理器 | `/admin/test-data-cleaner` | `/admin/test-data-cleaner/tasks/:id` | 全局测试数据清理任务 |

## 5. 锚点命名约定
- 主数据统一使用 `/master-data/*`。
- 供应商应付统一使用 `/vendor-payables/*`。
- 结算层特殊流程统一使用 `/settlement-adjustments/*`，不与普通结算混用。
- 报表输出统一以 `/reports` 为根，不把专题分析分散挂在各业务页面下作为背景层命名。
- 工单、报价、发货等主交易模块尽量采用复数集合页作为主入口锚点。

## 6. 本文边界
- 不写“当前有没有这个页面”。
- 不写“该锚点是否已存在于仓库”。
- 不写前后端实现、权限细节或菜单现状。
- 只保留背景层统一讨论所需的逻辑锚点命名。
