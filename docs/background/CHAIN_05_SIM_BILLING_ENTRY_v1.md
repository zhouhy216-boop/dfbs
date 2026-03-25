# Chain Spec

## Meta
- chain_id: CHAIN_05
- chain_name: SIM卡费计费提醒与来源报价单生成链
- version: v1
- status: frozen-draft
- purpose: 中间标准稿，用于后续汇总成项目背景文件
- scope: 仅描述SIM卡费从计费提醒到生成SIM来源报价单为止的规则
- not_for: 报价单合并规则、报价单主链、实现方案

## Canonical Terms
- SIM来源报价单
  - aliases: SIM报价单, 卡费报价单
  - note: SIM卡费入口先形成的来源报价单，后续是否合并到总报价单，另行讨论

- 预付费
  - aliases: 预收SIM卡费
  - note: SIM卡只有预付费

- 套餐
  - aliases: 卡套餐
  - note: SIM收费按套餐拆收费项，不按机构拆

- 点数
  - aliases: 卡点数
  - note: 一张卡当月只要有流量，就算1点

- 剩余点数
  - aliases: 余额点数
  - note: 上次购买后未使用完的点数

- 欠额点数
  - aliases: 欠点
  - note: 实际使用超出已购点数的差额

## Main Object
- object_name: SIM来源报价单
- source: SIM计费提醒结果
- result_on_completed: 形成SIM来源报价单，进入后续报价单体系

## Trigger
- 仅我司SIM收费
- SIM卡费只有预付费
- SIM卡费和平台费分开生成来源报价单
- 后续是否合并，由报价单体系决定

## Roles
- 平台管理员
  - actions: 上传流量记录, 核实卡使用情况, 处理预警, 发起SIM来源报价单, 在物联卡平台关闭/开启卡

## Start Rule
- SIM费限免期的起算节点，与合同约定的保修起算节点相同
- 保修起算节点当前可为：发货 / 发船 / 签收 / 调试完成
- SIM费限免期在合同中允许原样填写
- 系统统一将SIM费限免期折算为月
- 到达限免期结束后，系统进入SIM收费提醒与建议流程

## Billing Rule
- 仅我司SIM收费
- 客供SIM / 无SIM不收费
- 只有合同中 SIM卡配置 = 有 时，才承接公司SIM收费相关信息
- SIM费限免期按合同约定口径承接，合同可原样填写，系统统一折算为月
- 管理员每月不定时上传上月SIM卡流量
- 收费依据按历史用量判断
- 当前两档：
  - 200M以下一个单价
  - 200M以上一个单价

## Reminder Rule
- 至少两类常规提醒：
  - 有设备即将到达合同约定的SIM费限免期结束节点，提醒收费
  - 上月产生的点数或使用情况需要触发收费提醒

## Free Rule
- 免费SIM分为两类：
  - 公司内部使用免费
  - 销售申请免费
- 公司内部使用免费不走申请流程
- 由管理员直接设置免费，并填写免费原因
- 销售申请免费不在系统内发起
- 以线下签完的单据作为依据
- 系统只承接上传后的免费依据留存
- 由管理员根据线下依据设置免费
- 免费SIM不生成收费来源报价单
- 免费SIM不进入对客收费报价链
- 当前不展开线下单据模板、审批流、页面入口与状态流

## Usage Warning Rule
- 若一张按“200M以下档”收费的卡，连续两个月用量超过200M，需要提醒
- 若一张卡连续两个月用量为0，也要提醒
- 连续两个月为0时，平台主管通常会在物联卡管理平台关闭该卡
- 关闭卡的目的是避免继续产生费用
- 客户后续需要时，再在物联卡管理平台重新打开

## Package Rule
- SIM收费按套餐拆收费项
- 不是按机构拆
- 也不是简单按卡数拆
- 一张卡当月有流量算1点
- 套餐按月份累计点数形成收费项
- 示例：
  - 刷脸套餐有2张卡
  - 一张收12个月
  - 一张收6个月
  - 则该套餐收费项为18点

## Year-End Rule
- 一般年底都会发起一次收费
- 不管点数是否完全用完
- 系统应支持年底集中收费的业务倾向
- 但最终仍以人工与客户确认结果为准
- 允许客户欠点

## Quote Content Rule
- SIM来源报价项中应说明：
  - 去年购买多少点
  - 实际使用多少点
  - 剩余多少点 / 欠额多少点
  - 本次建议补多少点

## Fields
- name: SIM收费方式
  - aliases: 卡收费方式
  - type: enum
  - values: 预付费

- name: 套餐
  - aliases: 卡套餐
  - type: reference

- name: 上月流量
  - aliases: 上月用量
  - type: number
  - source: 平台管理员上传

- name: 去年已购点数
  - aliases: 去年购买点数
  - type: number

- name: 去年实际使用点数
  - aliases: 去年使用点数
  - type: number

- name: 剩余点数
  - aliases: 余额点数
  - type: number

- name: 欠额点数
  - aliases: 欠点
  - type: number

- name: 建议补购点数
  - aliases: 建议购买点数
  - type: number

## Derived Rules
- SIM卡费只有预付费
- 仅我司SIM进入SIM来源报价单体系
- 客供SIM / 无SIM不生成公司SIM来源报价单
- SIM费限免期起算节点与合同约定的保修起算节点相同
- SIM费限免期在合同中允许原样填写，但系统统一折算为月
- SIM收费按套餐拆收费项，不按机构拆
- 一张卡当月只要有流量，就算1点
- 提醒收费与限免期逻辑应与平台费链保持一致

## Open Points
- 各套餐字典与单价配置，未冻结
- 流量上传记录的固定字段结构，未冻结