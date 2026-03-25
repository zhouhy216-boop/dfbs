# Chain Spec

## Meta
- chain_id: CHAIN_01
- chain_name: 合同评审
- version: v1
- status: frozen-draft
- purpose: 中间标准稿，用于后续汇总成项目背景文件
- scope: 仅描述购销合同评审链及其向后续业务传递的关键信息
- not_for: 服务补充合同/协议评审、代码现状、实现方案、页面设计、任务单
- note:
  - 本链当前仅覆盖购销合同评审
  - 服务补充合同 / 协议属于合同主数据下的另一类合同口径，不在本链展开

## Canonical Terms
- 合同评审单
  - aliases: 合同评审, 评审单
  - note: 系统内部真正流转的对象，不等于客户签订的业务合同
- 客户合同
  - aliases: 合同文本, 合同原件
  - note: 业务来源文件
- 支付识别方案
  - aliases: 摄像头
  - note: 旧口头叫法“摄像头”废弃，标准口径统一为“支付识别方案”
- 平台方案
  - aliases: 智能化平台
  - note: 用于区分公司提供 / 客供 / 无
- 设备行项目
  - aliases: 型号台数, 合同行项目
  - note: 每行包含型号 + 台数，可增行

## Main Object
- object_name: 合同评审单
- source: 客户合同
- result_on_approved: 进入合同主数据
- result_on_closed: 保留在评审列表，可基于原单重新发起新一轮评审

## Roles
- 销售
  - actions: 发起, 编辑, 关闭, 重新发起
- 部门长
  - actions: 通过, 退回
- 统括部长
  - actions: 通过, 退回
- 营企
  - actions: 编辑, 关闭, 填写损益, 通过, 退回
- 生产企画
  - actions: 查看, 填写改造费用
  - note: 非审批节点
- 本部长
  - actions: 通过, 退回
- 副总
  - actions: 通过, 退回
- 总经理
  - actions: 通过, 退回

## Main Flow
1. 销售发起合同评审单
2. 部门长审批
3. 统括部长审批
4. 营企填写损益并提交
5. 本部长审批
6. 副总审批
7. 总经理审批
8. 审批通过后进入合同主数据

## Collaboration Rule
- 生产企画从发起后可见
- 生产企画补改造费用
- 营企基于改造费用填写损益
- 生产企画不是主审批链节点

## Permission Rule
- 销售和营企只在自己节点可编辑并可关闭
- 其他审批节点只能通过/退回
- 生产企画只能查看和补改造费用，不能通过/退回

## Return Rule
- 营企提交前统一退销售
- 营企提交后统一退营企
- 不是退回上一节点

## List Scope
- 待处理
- 全部

## Attachment Rule
- 销售发起评审时可上传合同文本附件
- attachment_name: 合同文本附件
- aliases: 合同附件, 合同扫描件, 合同文件
- note: 随评审流保留

## Fields
- name: 合同编号
  - aliases: 合同号
  - type: text
  - downstream: 合同主数据, 培训申请, 其他关联合同流程

- name: 客户名称
  - aliases: 客户名
  - type: text
  - downstream: 合同主数据

- name: 客户邮箱
  - aliases: 平台账号邮箱
  - type: text
  - required: false
  - downstream: 可在后续平台开通时默认带入
  - note: 标准口径仍为“客户邮箱”，不是专用平台邮箱

- name: 设备行项目
  - aliases: 型号台数
  - type: list
  - item_fields: 型号, 台数
  - note: 可按 Excel 式增行

- name: 交货期限
  - aliases: 无
  - type: optional
  - downstream: 发货申请

- name: 平台方案
  - aliases: 智能化平台
  - type: enum
  - values: 系统配置平台, 客供, 无
  - downstream: 平台开通, 平台费用判断
  - rules:
    - 选系统配置平台时，才承接公司平台相关收费信息
    - 选客供时出现“客供平台说明”
    - 选客供时，不承接公司平台收费判断
    - 选无时表示无平台需求

- name: 客供平台说明
  - aliases: 客供平台文本
  - type: text
  - show_when: 平台方案 = 客供

- name: SIM卡配置
  - aliases: SIM配置
  - type: enum
  - values: 有, 客供, 无
  - downstream: SIM安装, SIM收费判断
  - rules:
    - 有 = 带公司SIM，后续收费
    - 只有当 SIM卡配置 = 有 时，才承接公司SIM收费相关信息
    - 客供 = 客户寄卡到公司，由我们安装
    - 客供不产生公司SIM收费
    - 无 = 不带卡

- name: 支付识别方案
  - aliases: 摄像头
  - type: enum
  - values: 微信刷脸, 支付宝刷脸, 无
  - downstream: 套餐判断
  - note: 旧口头名“摄像头”保留为别名，不再作为标准名

- name: 平台费限免期
  - aliases: 平台费限免期
  - type: value
  - show_when: 平台方案 = 系统配置平台
  - downstream: 平台收费
  - note:
    - 合同可原样填写
    - 系统统一折算为月
    - 起算节点与保修起算节点相同

- name: 平台费单价
  - aliases: 平台费单价
  - type: value
  - show_when: 平台方案 = 系统配置平台
  - downstream: 平台收费

- name: SIM费限免期
  - aliases: 卡限免期
  - type: value
  - show_when: SIM卡配置 = 有
  - downstream: SIM收费
  - note:
    - 合同可原样填写
    - 系统统一折算为月
    - 起算节点与保修起算节点相同

- name: 保修起算节点
  - aliases: 保修起算点
  - type: enum
  - values: 发货, 发船, 签收, 调试完成
  - downstream: 保内保外判断, 平台限免判断, SIM限免判断
  - note:
    - 当前保留四种合同候选起算点
    - 平台费限免期与SIM费限免期沿用同一起算节点

- name: 保修期限
  - aliases: 保修期
  - type: value
  - downstream: 保内保外判断
  - note:
    - 合同可原样填写
    - 系统统一折算为月

- name: 是否需要卸货到点
  - aliases: 卸货到点
  - type: boolean
  - downstream: 发货申请

- name: 是否包含上楼
  - aliases: 上楼
  - type: boolean
  - downstream: 发货申请

- name: 培训次数
  - aliases: 培训额度
  - type: number
  - downstream: 培训工单发起限制

## Derived Rules
- 本链当前仅覆盖购销合同评审，不覆盖服务补充合同 / 协议评审
- 购销合同中的平台 / SIM 条款，仅承接购销合同约定事实
- 若后续存在服务补充合同 / 协议，则平台 / SIM 的系统建议值与执行口径，按合同主数据相关规则另行处理
- 平台费限免期、SIM费限免期、保修期限在合同中允许原样填写，但系统统一折算为月
- 保修起算节点当前保留四种合同候选值：发货 / 发船 / 签收 / 调试完成
- 平台费限免期起算节点与保修起算节点相同
- SIM费限免期起算节点与保修起算节点相同
- 只有当 平台方案 = 系统配置平台 时，才承接平台费限免期与平台费单价
- 平台方案 = 客供 / 无 时，不产生公司平台收费判断
- 合同中不单独填写SIM单价
- 只有当 SIM卡配置 = 有 时，才按后台套餐规则并结合支付识别方案判断收费套餐
- SIM卡配置 = 客供 / 无 时，不产生公司SIM收费
- 培训申请必须挂合同号
- 培训申请审批通过后预扣次数
- 培训完成后实扣次数
- 培训申请取消 / 作废 / 未完成关闭时，释放预扣次数
- 可发起培训次数 = 总次数 - 预扣中次数 - 已完成次数

## Open Points
- 审批意见必填规则是否单独抽到评审通用规则文档，待后续决定