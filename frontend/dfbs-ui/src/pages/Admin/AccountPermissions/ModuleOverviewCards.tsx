/**
 * 模块概览卡片：根级模块卡片 + 快捷操作（全选/只读/读写/清空）。
 * 仅影响草稿，不请求后端；保存/还原由父组件负责。
 */
import { Button, Card, Space } from 'antd';
import type { QuickOp, ModuleNodeLike } from './permissionQuickOps';
import { collectSubtreeKeys, countSubtreeKeys } from './permissionQuickOps';

export interface ModuleOverviewCardsProps {
  modules: ModuleNodeLike[];
  onQuickOp: (node: ModuleNodeLike, op: QuickOp) => void;
  /** Optional: for role template, count of selected keys in draft (for 已授权 X / 总数 Y). */
  draftKeys?: Set<string>;
}

export function ModuleOverviewCards({ modules, onQuickOp, draftKeys }: ModuleOverviewCardsProps) {
  if (modules.length === 0) {
    return (
      <p style={{ color: '#999', marginBottom: 12 }}>暂无模块</p>
    );
  }

  return (
    <div style={{ marginBottom: 16 }}>
      <p style={{ fontSize: 12, color: '#666', marginBottom: 8 }}>模块概览 — 快捷操作仅修改当前草稿，需点击「保存」后生效</p>
      <Space wrap size="middle">
        {modules.map((node) => {
          const total = countSubtreeKeys(node);
          const subtreeKeys = collectSubtreeKeys(node);
          const selected = draftKeys ? subtreeKeys.filter((k) => draftKeys.has(k)).length : 0;
          return (
            <Card
              key={node.key}
              size="small"
              title={node.label}
              style={{ width: 260 }}
              extra={
                total > 0 ? (
                  <span style={{ fontSize: 12, color: '#666' }}>
                    已选 {selected} / {total}
                  </span>
                ) : null
              }
            >
              <Space wrap size="small">
                <Button size="small" onClick={() => onQuickOp(node, 'readonly')}>
                  只读
                </Button>
                <Button size="small" onClick={() => onQuickOp(node, 'readwrite')}>
                  读写
                </Button>
                <Button size="small" onClick={() => onQuickOp(node, 'all')}>
                  全选
                </Button>
                <Button size="small" onClick={() => onQuickOp(node, 'clear')}>
                  清空
                </Button>
              </Space>
            </Card>
          );
        })}
      </Space>
    </div>
  );
}
