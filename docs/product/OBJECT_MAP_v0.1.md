
```markdown
# 文件：docs/product/OBJECT_MAP_v0.1.md
# 对象地图 v0.1（落地版）
> 目的：把“系统里真正要管的东西（对象）”画清楚：谁关联谁、谁驱动谁。
> 讨论规则：以后每个模块的详细讨论，都必须指向“对象 + 关系 + 状态/动作”。

## 0) 对象清单（统一名词）
- 客户
- 合同
- 设备（设备号=机器号）
- 设备型号
- BOM（型号零部件清单）
- 平台信息（类型/服务条款）
- 平台账号/平台侧记录（可先记录不对接）
- SIM卡（卡号、套餐、状态）
- 入网准备记录（设备↔SIM↔平台的绑定关系 + 开通/绑定状态）
- 发货交付单（含签收/关闭）
- 工单（类型：维修/培训/调试/巡检/移机…）
- 报价单（统一对象：类型区分平台&SIM续费/运输/维修/配件）
- 收款记录
- 到账确认
- 库存台账（总仓+小仓）
- 库存单据（入库/出库/调拨/盘点/报损/借用归还/领用/发货）
- 平台成本台账（外部导入）
- 报表（视图/统计，不是业务对象但依赖所有对象数据）

---

## 1) 对象关系图（业务对象为主）

```mermaid
flowchart TB
  %% 合同与客户
  客户 -->|签订| 合同
  合同 -->|约定| 合同条款_交付信息
  合同 -->|约定| 保修规则_起算点与保修期
  合同 -->|约定(公司提供则填写)| 平台服务条款_类型_单价_周期
  合同 -->|约定(公司提供则填写)| SIM套餐条款_单价_周期

  %% 设备/型号/BOM
  设备型号 -->|定义| BOM_零部件清单
  合同 -->|包含| 设备型号
  合同 -->|生成交付需求| 发货交付单
  发货交付单 -->|包含| 设备(设备号=机器号)

  %% 入网准备（平台&SIM）
  设备(设备号=机器号) -->|绑定| 入网准备记录
  平台服务条款_类型_单价_周期 -->|提供信息| 入网准备记录
  SIM卡 -->|绑定| 入网准备记录
  入网准备记录 -->|状态| SIM开通状态
  入网准备记录 -->|状态| 平台绑定状态
  入网准备记录 -->|可记录(不对接)| 平台账号/平台侧记录

  %% 交付闭环
  发货交付单 -->|签收/关闭| 交付结果
  交付结果 -->|可能影响| 保修规则_起算点与保修期

  %% 工单（售后）
  设备(设备号=机器号) -->|产生| 工单
  工单 -->|类型| 工单类型_维修
  工单 -->|类型| 工单类型_新机培训
  工单 -->|类型| 工单类型_调试/巡检/移机(可扩展)
  工单 -->|引用| 合同
  工单 -->|判定| 保内保外结果

  %% 报价（统一对象）
  报价单_统一对象 -->|类型| 报价类型_平台SIM续费
  报价单_统一对象 -->|类型| 报价类型_运输
  报价单_统一对象 -->|类型| 报价类型_维修
  报价单_统一对象 -->|类型| 报价类型_配件

  平台服务条款_类型_单价_周期 -->|触发/定价依据| 报价类型_平台SIM续费
  SIM套餐条款_单价_周期 -->|触发/定价依据| 报价类型_平台SIM续费
  发货交付单 -->|可能触发| 报价类型_运输
  工单 -->|可能触发| 报价类型_维修
  工单 -->|可能触发| 报价类型_配件

  报价类型_平台SIM续费 --> 报价单_统一对象
  报价类型_运输 --> 报价单_统一对象
  报价类型_维修 --> 报价单_统一对象
  报价类型_配件 --> 报价单_统一对象

  报价单_统一对象 -->|收款| 收款记录
  收款记录 -->|会计确认| 到账确认

  %% 仓库/配件
  库存台账(总仓+小仓) -->|管理| 库存单据(入库/出库/调拨/盘点/报损/借用归还)
  工单 -->|领用/归还| 库存单据(按工单领用/归还)
  报价类型_配件 -->|执行发货| 库存单据(按配件订单发货)

  %% 平台成本与报表
  平台成本导入 --> 平台成本台账
  平台成本台账 -->|核算| 报表中心
  合同 --> 报表中心
  设备(设备号=机器号) --> 报表中心
  入网准备记录 --> 报表中心
  发货交付单 --> 报表中心
  工单 --> 报表中心
  报价单_统一对象 --> 报表中心
  收款记录 --> 报表中心
  到账确认 --> 报表中心
  库存台账(总仓+小仓) --> 报表中心
```

---

## Repo reality check (stage baseline 2025-02-24)

- **Objects present in repo:** 合同, 客户, 设备, 设备型号, BOM, 发货交付单, 工单, 报价单, 入网准备记录 (platform org/applications), SIM卡, 库存/补货, 收款/到账, 平台成本 (config) — entities or tables exist. **account / org person / primary business role:** `app_user` has `primary_business_role` (V0087); account bound to org person; no “mock people” layer.
- **Review/ownership objects:** 评审记录、发起人、分配营业企画、当前处理人 — not in repo; no contract review entity or ownership fields.

## Conflicts with current repo reality

- None. Object map does not assert that every object has a dedicated table; repo has contract, shipment, work_order, platform_application, etc. “合同” exists; “合同评审” flow and related objects do not.

## Anchor gaps / not yet present in repo

- **Contract review / 评审协同 objects:** No review record, initiator, assigned Business Planning person, or current handler in repo. Valid product discussion anchors; not implemented.
- **Leadership visibility / 报表中心 as object:** Dashboard exists; “领导自选维度” reporting object not implemented.