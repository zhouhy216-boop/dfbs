/**
 * 权限键选择器：树形展示，按动作勾选，用于「添加权限」/「移除权限」。
 * 仅维护勾选集合，确定后由父组件写入 draftAdd/draftRemove。
 */
import { Checkbox } from 'antd';
import type { ModuleNodeLike } from './permissionQuickOps';

export interface PermissionKeyPickerProps {
  modules: ModuleNodeLike[];
  selectedKeys: Set<string>;
  onSelectionChange: (keys: Set<string>) => void;
  title: string;
}

function PickerNode({
  node,
  depth,
  selectedKeys,
  onSelectionChange,
}: {
  node: ModuleNodeLike;
  depth: number;
  selectedKeys: Set<string>;
  onSelectionChange: (keys: Set<string>) => void;
}) {
  const toggle = (key: string) => {
    const next = new Set(selectedKeys);
    if (next.has(key)) next.delete(key);
    else next.add(key);
    onSelectionChange(next);
  };

  return (
    <div style={{ marginBottom: 8 }}>
      <div style={{ marginLeft: depth * 16, fontWeight: 500, marginBottom: 4 }}>{node.label}</div>
      <div style={{ marginLeft: depth * 16 + 16, display: 'flex', flexWrap: 'wrap', gap: 8 }}>
        {(node.actions || []).map((actionKey) => {
          const key = `${node.key}:${actionKey}`;
          return (
            <Checkbox
              key={key}
              checked={selectedKeys.has(key)}
              onChange={() => toggle(key)}
            >
              {actionKey}
            </Checkbox>
          );
        })}
      </div>
      {(node.children ?? []).map((child) => (
        <PickerNode
          key={child.key}
          node={child}
          depth={depth + 1}
          selectedKeys={selectedKeys}
          onSelectionChange={onSelectionChange}
        />
      ))}
    </div>
  );
}

export function PermissionKeyPicker({
  modules,
  selectedKeys,
  onSelectionChange,
  title,
}: PermissionKeyPickerProps) {
  return (
    <div>
      <p style={{ marginBottom: 12, color: '#666' }}>{title}</p>
      <div style={{ maxHeight: 360, overflow: 'auto', padding: 8, background: '#fafafa', borderRadius: 4 }}>
        {modules.length === 0 ? (
          <span style={{ color: '#999' }}>暂无模块</span>
        ) : (
          modules.map((mod) => (
            <PickerNode
              key={mod.key}
              node={mod}
              depth={0}
              selectedKeys={selectedKeys}
              onSelectionChange={onSelectionChange}
            />
          ))
        )}
      </div>
    </div>
  );
}
