# Chain Spec

## Meta
- chain_id: CHAIN_03
- chain_name: 发货申请与签收链
- version: v1
- status: frozen-draft
- purpose: 中间标准稿，用于后续汇总成项目背景文件
- scope: 仅描述合同评审通过后，从发货申请到客户签收及营企审核结束的业务链
- not_for: 代码现状、实现方案、页面设计、任务单

## Canonical Terms
- 发货申请
  - aliases: 发货单申请
  - note: 基于合同发起的本次发货申请

- 发货明细
  - aliases: 发货依赖明细
  - note: 本次发货的逐行产品明细，可增行

- 具备发货条件日期
  - aliases: 具体提货条件日期, 我准备好了日期
  - note: 生企填写的同一概念，表示已具备发货条件

- 提货票
  - aliases: 无
  - note: 由物流管理基于机器号和发货信息从系统导出，用于线下签字流程；签完后由物流管理上传

- 回执单
  - aliases: 签收单, 客户签收单
  - note: 由物流管理基于机器号和发货信息从系统导出；物流最终上传签收后的回执单

## Main Object
- object_name: 发货申请单
- source: 合同评审通过结果
- result_on_completed: 完成本次发货、客户签收、营企审核，并完成相关单据归档，正常流程结束

## Trigger
- 合同评审通过后可发起
- 一次合同可对应多次发货申请
- 每次发货按本次发货明细单独处理

## Roles
- 销售
  - actions: 发起发货申请, 填写发货基础信息, 填写发货明细

- 营企
  - actions: 审核发货申请, 签收后审核结束流程
  - note: 客户签收后由营企审核，审核完成后本次发货链结束

- 生产企画
  - actions: 全程可见, 提前填写具备发货条件日期, 到节点时补齐相关信息
  - note: 在到自己节点前也可提前填写“具备发货条件日期”

- 仓库
  - actions: 选择出库机器号

- 物流管理
  - actions: 填写提货日期, 基于机器号和发货信息导出提货票与回执单, 上传已签提货票

- 物流
  - actions: 更新运输状态, 更新当前位置, 更新预计送达时间, 上传签收后的回执单, 登记签收日期

- 客户
  - actions: 签收

## Main Flow
1. 销售发起发货申请
2. 营企审核
3. 生企处理
4. 仓库处理
5. 物流管理处理
6. 物流处理
7. 客户签收
8. 营企审核
9. 正常流程结束

## Sales Input Rule
- 销售选择合同号
- 系统带出客户名称
- 系统带出是否需要卸货到点
- 系统带出是否包含上楼
- 销售填写“希望发货日”或“希望到货日”，二选一
- 销售填写接货人
- 销售填写联系电话
- 销售填写到货详细地址

## Shipment Detail Rule
- 发货明细按行填写
- 产品型号从合同中选择
- 选择型号后带出产品名称
- 销售填写本次发货台数
- 销售填写包装方式
- 明细支持增行
- 本次发货台数不能大于该合同该型号的剩余未发货台数

## Production Planning Rule
- 生企全过程可见
- 生企在到自己节点前即可填写“具备发货条件日期”
- “具备发货条件日期”与“具体提货条件日期”为同一概念
- 到生企节点时补齐相关信息

## Warehouse Rule
- 仓库按实际出库选择机器号
- 仓库处理在物流管理之前
- 未形成机器号时，无法完成机器号选择

## Logistics Management Rule
- 物流管理填写提货日期
- 物流管理基于机器号和发货信息，从系统导出两个Excel：
  - 提货票
  - 回执单
- 提货票走线下签字流程
- 提货票签完后由物流管理上传回系统

## Logistics Rule
- 物流环节需持续更新运输状态、当前位置和预计送达时间
- 物流最终上传签收后的回执单
- 物流登记签收日期

## Document Relationship Rule
- 提货票与回执单之间存在对应关系
- 提货票与回执单均与合同存在对应关系
- 最终相关单据在系统中归档保存

## Completion Rule
- 客户完成签收后，不直接结束
- 需由营企进行审核
- 营企审核完成后，本次发货链正常结束

## Fields
- name: 合同号
  - aliases: 合同编号
  - type: reference
  - source: 合同
  - downstream: 发货申请主关联, 单据归档关联

- name: 客户名称
  - aliases: 客户名
  - type: text
  - source: 合同

- name: 是否需要卸货到点
  - aliases: 卸货到点
  - type: boolean
  - source: 合同

- name: 是否包含上楼
  - aliases: 上楼
  - type: boolean
  - source: 合同

- name: 希望发货日
  - aliases: 无
  - type: date
  - required_rule: 与“希望到货日”二选一

- name: 希望到货日
  - aliases: 无
  - type: date
  - required_rule: 与“希望发货日”二选一

- name: 接货人
  - aliases: 收货人
  - type: text

- name: 联系电话
  - aliases: 电话
  - type: text

- name: 到货详细地址
  - aliases: 收货地址
  - type: text

- name: 发货明细
  - aliases: 发货依赖明细
  - type: list
  - item_fields: 产品型号, 产品名称, 发货台数, 包装方式

- name: 具备发货条件日期
  - aliases: 具体提货条件日期, 我准备好了日期
  - type: date
  - filled_by: 生产企画

- name: 提货日期
  - aliases: 无
  - type: date
  - filled_by: 物流管理

- name: 出库机器号
  - aliases: 机器号
  - type: list
  - filled_by: 仓库
  - downstream: 提货票导出, 回执单导出

- name: 提货票
  - aliases: 无
  - type: attachment
  - filled_by: 物流管理
  - source: 系统导出Excel后回传

- name: 回执单
  - aliases: 签收单, 客户签收单
  - type: attachment
  - filled_by: 物流
  - source: 系统导出Excel后签收回传

- name: 运输状态
  - aliases: 无
  - type: status
  - filled_by: 物流

- name: 当前位置
  - aliases: 无
  - type: text
  - filled_by: 物流

- name: 预计送达时间
  - aliases: 预计送货时间
  - type: datetime
  - filled_by: 物流

- name: 签收日期
  - aliases: 无
  - type: date
  - filled_by: 物流

## Derived Rules
- 一份合同可拆分多次发货
- 发货明细按行处理，不是单一型号单一数量
- 合同中的基础交付要求会传递到发货申请
- 机器号与发货链直接关联
- 提货票与回执单均由物流管理基于机器号和发货信息从系统导出
- 提货票走线下签字并由物流管理上传
- 回执单作为最终签收凭证由物流上传
- 提货票、回执单、合同三者之间需建立对应关系并归档
- 客户签收后，还需营企审核一次才结束流程