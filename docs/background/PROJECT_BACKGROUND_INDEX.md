# PROJECT_BACKGROUND_INDEX.md

## 1. 文档目的

本文用于作为 DFBS 项目背景体系的总入口与阅读索引。  
本文不重新定义业务规则，不展开系统能力细节，不描述项目当前实现状态，不承担任务单作用。  
本文只负责说明：

- 当前项目背景由哪些主文构成
- 各文档分别负责什么
- 阅读顺序建议
- 不同场景下应优先查看哪些文档
- 后续任务整理默认应以哪些文档为依据

---

## 2. 项目背景文档总体结构

当前项目背景分为以下几层：

### 2.1 项目地图层
用于建立全局连接，帮助阅读者快速理解项目整体结构、业务主线、关键对象、业务模块与逻辑锚点。

包括：

- `PROCESS_MAP_v0.2.md`
- `OBJECT_MAP_v0.2.md`
- `BUSINESS_MAP_v0.2.md`
- `MODULE_ROUTE_ANCHORS_v0.2.md`

### 2.2 业务规则层
用于定义各业务链的正式规则、边界、流转、异常、上下游关系和冻结结论。

包括：

- `CHAIN_01_CONTRACT_REVIEW_v1.md`
- `CHAIN_02_PRE_SHIPMENT_PREPARATION_v1.md`
- `CHAIN_03_SHIPMENT_AND_SIGNOFF_v1.md`
- `CHAIN_04_PLATFORM_BILLING_ENTRY_v1.md`
- `CHAIN_05_SIM_BILLING_ENTRY_v1.md`
- `CHAIN_06_WORK_ORDER_SYSTEM_v1.md`
- `CHAIN_07_WAREHOUSE_AND_SPARE_PARTS_AROUND_WORK_ORDER_v1.md`
- `CHAIN_08_ENTRUSTED_SHIPMENT_BRANCH_v1.md`
- `CHAIN_09_QUOTE_SYSTEM_BEFORE_MERGE_v1.md`
- `CHAIN_10_SETTLEMENT_SYSTEM_v1.md`
- `CHAIN_11_SETTLEMENT_CORRECTION_AND_ERROR_v1.md`
- `CHAIN_12_TRANSPORT_EXCEPTION_BRANCH_v1.md`
- `CHAIN_13_MASTER_DATA_v1.md`
- `CHAIN_14_COST_BRANCH_v1.md`
- `CHAIN_15_TRAVEL_LOAN_ALLOWANCE_AND_REIMBURSEMENT_BRANCH_v1.md`
- `CHAIN_16_SEAL_APPLICATION_BRANCH_v1.md`
- `CHAIN_17_FAULT_KNOWLEDGE_BASE_BRANCH_v1.md`
- `CHAIN_18_BATCH_DEFECT_ALERT_BRANCH_v1.md`
- `CHAIN_19_VENDOR_PAYABLE_LOGISTICS_BRANCH_v1.md`
- `CHAIN_20_VENDOR_PAYABLE_PLATFORM_BRANCH_v1.md`
- `CHAIN_21_VENDOR_PAYABLE_SIM_BRANCH_v1.md`
- `CHAIN_22_VENDOR_PAYABLE_COURIER_BRANCH_v1.md`
- `CHAIN_23_REPORTING_OUTPUT_LAYER_v1.md`
- `CHAIN_24_SERVICE_MANAGER_PERFORMANCE_MANAGEMENT_BRANCH_v1.md`
- `CHAIN_25_SERVICE_SUPPLEMENT_AGREEMENT_LIFECYCLE_BRANCH_v1.md`
- `CHAIN_26_SERVICE_CONTRACT_SLA_AND_WORK_ORDER_TIMELINESS_BRANCH_v1.md`

### 2.3 系统通用能力层
用于定义不依附于单一业务链、可被多个业务段复用的系统底座能力。

包括：

- `SYSTEM_CAPABILITIES_BACKGROUND.md`

### 2.4 扩展能力层
用于定义不属于系统通用底座、但可能作为后续正式扩展方向纳入项目范围的产品形态类能力和业务扩展类能力。

包括：

- `EXTENDED_PRODUCT_AND_BUSINESS_CAPABILITIES.md`

---

## 3. 各主文职责说明

### 3.1 PROCESS_MAP_v0.2.md
职责：

- 说明项目核心业务流程主线
- 建立主链与支链之间的全局连接
- 说明哪些是主流程、哪些是条件触发、哪些是结果输出方向

不负责：

- 展开对象定义
- 展开系统通用能力
- 展开页面、字段、代码实现
- 承担项目当前状态说明

### 3.2 OBJECT_MAP_v0.2.md
职责：

- 说明项目中的核心业务对象
- 说明业务对象之间的关系
- 说明系统支撑对象与业务对象的轻关系

不负责：

- 展开业务规则全文
- 展开流程细节
- 展开模块设计或实现设计

### 3.3 BUSINESS_MAP_v0.2.md
职责：

- 说明项目主要业务模块如何分工
- 说明各模块主要承接什么、不承接什么
- 说明系统通用能力作为跨模块底座的引用关系

不负责：

- 承担系统能力详细设计
- 承担项目现状说明
- 展开独立扩展能力全文

### 3.4 MODULE_ROUTE_ANCHORS_v0.2.md
职责：

- 统一逻辑锚点命名
- 为业务模块与系统能力提供背景层入口语言
- 为后续任务整理、模块讨论和命名统一提供参考

不负责：

- 描述当前页面真实实现状态
- 承担功能设计文角色
- 替代路由实现文

### 3.5 CHAIN_01–26
职责：

- 作为正式业务规则来源
- 说明每条业务链的目标、边界、流转、异常和输出关系
- 承担跨链引用时的正式口径依据

不负责：

- 展开页面/字段/按钮级设计
- 展开代码实现
- 承担系统通用能力文角色

### 3.6 SYSTEM_CAPABILITIES_BACKGROUND.md
职责：

- 定义系统通用能力范围
- 说明系统底座能力如何分层
- 说明系统能力的边界、关系和不负责范围

不负责：

- 重写业务链规则
- 记录项目当前状态
- 展开产品形态或扩展业务能力全文

### 3.7 EXTENDED_PRODUCT_AND_BUSINESS_CAPABILITIES.md
职责：

- 承接不属于系统通用底座、但属于正式扩展方向的内容
- 说明产品形态类扩展能力和业务扩展类能力的定位与边界
- 作为后续扩展方向需求锁定前的背景依据

不负责：

- 替代系统通用能力文
- 替代业务链文档
- 展开开发任务和实现方案

---

## 4. 推荐阅读顺序

### 4.1 第一次进入项目时的推荐阅读顺序

1. `PROJECT_BACKGROUND_INDEX.md`  
2. `PROCESS_MAP_v0.2.md`  
3. `OBJECT_MAP_v0.2.md`  
4. `BUSINESS_MAP_v0.2.md`  
5. `SYSTEM_CAPABILITIES_BACKGROUND.md`  
6. `EXTENDED_PRODUCT_AND_BUSINESS_CAPABILITIES.md`  
7. 根据当前讨论主题进入对应 `CHAIN_xx` 文档  
8. 如涉及模块入口或命名，再查看 `MODULE_ROUTE_ANCHORS_v0.2.md`

### 4.2 当需要快速建立全局认知时

优先阅读：

- `PROCESS_MAP_v0.2.md`
- `OBJECT_MAP_v0.2.md`
- `BUSINESS_MAP_v0.2.md`

### 4.3 当需要确认正式业务规则时

优先阅读：

- 对应 `CHAIN_xx` 文档

必要时结合：

- `PROCESS_MAP_v0.2.md`
- `OBJECT_MAP_v0.2.md`

### 4.4 当需要判断某项需求是否应做成系统底座时

优先阅读：

- `SYSTEM_CAPABILITIES_BACKGROUND.md`

必要时结合：

- 对应业务链文档
- `BUSINESS_MAP_v0.2.md`

### 4.5 当需要判断某项方向是否属于产品扩展或业务扩展时

优先阅读：

- `EXTENDED_PRODUCT_AND_BUSINESS_CAPABILITIES.md`

必要时结合：

- `SYSTEM_CAPABILITIES_BACKGROUND.md`
- 对应业务链文档

### 4.6 当需要讨论模块命名、逻辑入口或后续统一锚点时

优先阅读：

- `MODULE_ROUTE_ANCHORS_v0.2.md`

---

## 5. 不同工作场景下的默认引用文档

### 5.1 需求发现
默认引用：

- `PROCESS_MAP_v0.2.md`
- `BUSINESS_MAP_v0.2.md`
- 相关 `CHAIN_xx` 文档
- `SYSTEM_CAPABILITIES_BACKGROUND.md`（当需求涉及系统底座时）
- `EXTENDED_PRODUCT_AND_BUSINESS_CAPABILITIES.md`（当需求涉及扩展方向时）

### 5.2 需求锁定
默认引用：

- 对应 `CHAIN_xx` 文档
- `OBJECT_MAP_v0.2.md`
- `BUSINESS_MAP_v0.2.md`
- `SYSTEM_CAPABILITIES_BACKGROUND.md` 或 `EXTENDED_PRODUCT_AND_BUSINESS_CAPABILITIES.md`（视需求类型而定）

### 5.3 给 Cursor 整理任务
默认引用：

- 对应 `CHAIN_xx` 文档作为业务规则依据
- `BUSINESS_MAP_v0.2.md` 作为模块边界依据
- `MODULE_ROUTE_ANCHORS_v0.2.md` 作为逻辑入口和命名参考
- `SYSTEM_CAPABILITIES_BACKGROUND.md` 或 `EXTENDED_PRODUCT_AND_BUSINESS_CAPABILITIES.md` 作为能力边界依据

### 5.4 结果复审与验收
默认引用：

- 对应 `CHAIN_xx` 文档
- `BUSINESS_MAP_v0.2.md`
- `OBJECT_MAP_v0.2.md`
- 相关系统能力文或扩展能力文

---

## 6. 文档使用原则

### 6.1 以业务链文档作为正式业务规则来源
当地图文、系统能力文、扩展能力文与业务规则发生理解冲突时，业务规则以对应 `CHAIN_xx` 文档为准。

### 6.2 地图文只负责连接，不替代正文
四份地图文用于建立整体连接和定位，不用于替代业务链规则全文，也不用于承载系统能力全文。

### 6.3 系统通用能力文只负责底座能力
`SYSTEM_CAPABILITIES_BACKGROUND.md` 只承接系统底座能力，不重写业务规则，不承接产品形态扩展方向。

### 6.4 扩展能力文只负责扩展方向
`EXTENDED_PRODUCT_AND_BUSINESS_CAPABILITIES.md` 只承接扩展方向，不替代业务链文档和系统通用能力文。

### 6.5 项目状态不写入背景文
项目当前是否已实现、未实现、待实现，不属于项目背景范围，不应混入上述背景文档中。

---

## 7. 当前项目背景基线结论

当前项目背景已形成以下稳定结构：

- 四份地图文负责全局连接、对象、模块和逻辑锚点
- CHAIN_01–26 负责正式业务规则和边界
- `SYSTEM_CAPABILITIES_BACKGROUND.md` 负责系统通用能力底座
- `EXTENDED_PRODUCT_AND_BUSINESS_CAPABILITIES.md` 负责扩展能力方向

当前这套文档结构已可作为项目背景基线长期使用。  
后续原则上不再随意补充背景正文。  
只有在以下情况下才更新背景文档：

- 业务边界发生变化
- 正式新增一类系统通用能力
- 正式新增一类扩展能力
- 原有文档分工发生冲突
- 为了提升导航效率而补充索引或术语层文档

---

## 8. 后续使用建议

后续新的讨论窗口，默认应先说明本次讨论属于以下哪一类：

- 需求发现
- 需求锁定
- 系统通用能力讨论
- 扩展能力讨论
- 给 Cursor 整理任务
- 开发结果二次审阅

并明确本次应以上述哪几份背景文作为依据。

若某次讨论仅涉及某一条业务链，则不必重读全部背景文，只需先查看本索引，再进入相关地图文和对应 `CHAIN_xx` 文档。  
若某次讨论涉及系统底座或扩展方向，应优先先看对应能力背景文，再回到业务链确认规则来源。

---

## 9. 本文边界

本文只作为项目背景总索引和阅读顺序文使用。  
本文不新增业务规则，不定义系统能力细节，不描述项目现状，不承担任务推进记录作用。