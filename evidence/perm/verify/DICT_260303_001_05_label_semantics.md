# DICT-260303-001-05 历史展示与标签语义说明

**范围：** 字典只读接口的标签解析语义（v1 基线）；供产品与研发澄清「改名后历史显示的是哪一版 label」。

---

## 1) v1 当前行为：按 itemKey 解析为「最新标签」

- 当字典项**标签（label）被重命名**后，所有**按 itemKey（item_value）解析**的展示都会显示**当前最新**的 label，即**实时解析**，不保留「保存当时的标签」。
- 只读接口在 `includeDisabled=true` 时仍会返回已禁用项，其 label 同样是**当前最新**值；启用/禁用只影响是否出现在列表中，不影响「用最新 label」的语义。

## 2) 涉及的接口

- **GET `/api/v1/dictionaries/{typeCode}/items`**  
  返回项的 `value`（itemKey）、`label` 等；业务侧用 value 匹配、用 label 展示时，得到的均为**当前最新** label。
- **GET `/api/v1/dictionaries/{typeCode}/transitions`**  
  返回边的 `fromValue`/`toValue`、`fromLabel`/`toLabel`、`enabled`；fromLabel/toLabel 同样为**当前最新** label。

## 3) 如需「保存当时的标签」或历史快照

- 若产品需要**历史界面显示「当时保存的 label」**（即改名后历史记录仍显示旧名称），需在**业务侧**实现，例如：
  - 在业务表中在保存时**冗余存储当时的 label**（或引用当时的快照），展示时用该字段而非实时查字典；或
  - 引入字典/标签的**版本或快照**能力（需额外数据模型与范围）。
- **当前字典模块不提供**「按时间/版本的 label 快照」；Step-05 不包含上述能力，若需则需单独排期。

---

**总结：** v1 下，按 itemKey 解析的展示一律使用**最新 label**；要展示「历史当时的 label」需在业务数据或后续需求中解决。
