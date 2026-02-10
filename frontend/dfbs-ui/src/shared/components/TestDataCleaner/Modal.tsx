import { useMemo, useState, useEffect } from 'react';
import { useLocation } from 'react-router-dom';
import { Button, Checkbox, Modal, Radio, Space, Tag, Typography } from 'antd';
import type { CheckboxChangeEvent } from 'antd/es/checkbox';
import {
  MODULE_GROUPS,
  UNMAPPED_KEY,
  getAllSelectableModuleIds,
  getModuleLabelById,
  resolveModulesByPath,
  resolveSelectionWithDeps,
} from './moduleRegistry';

export interface TestDataCleanerModalProps {
  open: boolean;
  onClose: () => void;
}

type PresetMode = 'full' | 'current' | 'custom';

/**
 * Test Data Cleaner modal (admin-only). Selection UX: presets + custom multi-select.
 * Preview/执行 remain disabled; no API calls.
 */
export function TestDataCleanerModal({ open, onClose }: TestDataCleanerModalProps) {
  const location = useLocation();
  const pathname = location.pathname ?? '';
  const resolved = useMemo(() => resolveModulesByPath(pathname), [pathname]);
  const moduleList = useMemo(() => MODULE_GROUPS, []);
  const allSelectableIds = useMemo(() => getAllSelectableModuleIds(), []);

  const [presetMode, setPresetMode] = useState<PresetMode>('current');
  const [customSelectedIds, setCustomSelectedIds] = useState<string[]>([]);

  const isUnmapped = resolved.length > 0 && resolved[0].moduleKey === UNMAPPED_KEY;
  const currentModuleId = !isUnmapped && resolved.length > 0 ? resolved[0].moduleKey : null;

  const rawSelectedIds: string[] = useMemo(() => {
    if (presetMode === 'full') return allSelectableIds;
    if (presetMode === 'current') return currentModuleId ? [currentModuleId] : [];
    return customSelectedIds;
  }, [presetMode, allSelectableIds, currentModuleId, customSelectedIds]);

  const { effectiveIds: effectiveSelectedIds, addedDeps, reasons } = useMemo(
    () => resolveSelectionWithDeps(rawSelectedIds),
    [rawSelectedIds]
  );

  useEffect(() => {
    if (!open) return;
    if (presetMode === 'current' && isUnmapped) setCustomSelectedIds([]);
  }, [open, presetMode, isUnmapped]);

  const handlePresetChange = (e: { target: { value: PresetMode } }) => {
    setPresetMode(e.target.value);
  };

  const handleGroupCheckAll = (group: (typeof MODULE_GROUPS)[0], checked: boolean) => {
    const ids = group.modules.map((m) => m.id);
    if (checked) {
      setCustomSelectedIds((prev) => Array.from(new Set([...prev, ...ids])));
    } else {
      setCustomSelectedIds((prev) => prev.filter((id) => !ids.includes(id)));
    }
  };

  const handleModuleToggle = (moduleId: string, checked: boolean) => {
    if (checked) {
      setCustomSelectedIds((prev) => (prev.includes(moduleId) ? prev : [...prev, moduleId]));
    } else {
      setCustomSelectedIds((prev) => prev.filter((id) => id !== moduleId));
    }
  };

  const rawSelectedLabels = useMemo(
    () => rawSelectedIds.map((id) => getModuleLabelById(id) ?? id).filter(Boolean),
    [rawSelectedIds]
  );
  const effectiveSelectedLabels = useMemo(
    () => effectiveSelectedIds.map((id) => getModuleLabelById(id) ?? id).filter(Boolean),
    [effectiveSelectedIds]
  );
  const addedDepLabels = useMemo(
    () => addedDeps.map((id) => getModuleLabelById(id) ?? id).filter(Boolean),
    [addedDeps]
  );

  return (
    <Modal
      title="测试数据清理器"
      open={open}
      onCancel={onClose}
      footer={
        <>
          <Button key="preview" disabled>
            预览
          </Button>
          <Button key="run" type="primary" disabled>
            执行
          </Button>
          <Button key="close" type="primary" onClick={onClose}>
            关闭
          </Button>
        </>
      }
      width={600}
      destroyOnClose
    >
      <p>
        仅限管理员使用；当前为<strong>初始化界面</strong>，不会执行任何清理操作。预览与执行将在后续步骤中开放。
      </p>

      <section style={{ marginTop: 16 }}>
        <Typography.Title level={5} style={{ marginBottom: 8 }}>
          选择范围
        </Typography.Title>
        <Radio.Group value={presetMode} onChange={handlePresetChange}>
          <Space direction="vertical">
            <Radio value="full">全量重置</Radio>
            <Radio value="current">仅当前模块</Radio>
            {presetMode === 'current' && isUnmapped && (
              <Typography.Text type="secondary" style={{ display: 'block', marginLeft: 22 }}>
                当前页面未映射到模块，请使用自定义选择。
              </Typography.Text>
            )}
            <Radio value="custom">自定义选择</Radio>
          </Space>
        </Radio.Group>

        {presetMode === 'custom' && (
          <div style={{ marginTop: 12, marginLeft: 22 }}>
            {moduleList.map((group) => {
              const groupModuleIds = group.modules.map((m) => m.id);
              const selectedInGroup = customSelectedIds.filter((id) => groupModuleIds.includes(id));
              const allChecked = groupModuleIds.length > 0 && selectedInGroup.length === groupModuleIds.length;
              const someChecked = selectedInGroup.length > 0;
              const indeterminate = someChecked && !allChecked;
              return (
                <div key={group.id} style={{ marginBottom: 12 }}>
                  <Checkbox
                    indeterminate={indeterminate}
                    checked={allChecked}
                    onChange={(e: CheckboxChangeEvent) => handleGroupCheckAll(group, e.target.checked)}
                  >
                    本分组全选
                  </Checkbox>
                  <Typography.Text type="secondary" style={{ marginLeft: 8 }}>
                    （{group.label}）
                  </Typography.Text>
                  <div style={{ marginLeft: 22, marginTop: 4 }}>
                    <Space wrap>
                      {group.modules.map((m) => {
                        const isAddedDep = addedDeps.includes(m.id);
                        const isChecked = effectiveSelectedIds.includes(m.id);
                        return (
                          <Checkbox
                            key={m.id}
                            checked={isChecked}
                            disabled={isAddedDep}
                            onChange={(e: CheckboxChangeEvent) => !isAddedDep && handleModuleToggle(m.id, e.target.checked)}
                            title={isAddedDep ? '为所选模块的依赖，需先取消选择依赖方后方可取消' : undefined}
                          >
                            {m.label}
                            {isAddedDep && <Tag color="blue" style={{ marginLeft: 4 }}>依赖</Tag>}
                          </Checkbox>
                        );
                      })}
                    </Space>
                  </div>
                </div>
              );
            })}
          </div>
        )}

        <div style={{ marginTop: 12 }}>
          <strong>用户选择（原始）：</strong> {rawSelectedIds.length} 个模块
          {rawSelectedLabels.length > 0 && (
            <div style={{ marginTop: 4, fontSize: 12, color: 'rgba(0,0,0,0.65)' }}>
              {rawSelectedLabels.join('、')}
            </div>
          )}
        </div>
        <div style={{ marginTop: 8 }}>
          <strong>生效选择（含依赖补全）：</strong> {effectiveSelectedIds.length} 个模块
          {effectiveSelectedLabels.length > 0 && (
            <div style={{ marginTop: 4, fontSize: 12, color: 'rgba(0,0,0,0.65)' }}>
              {effectiveSelectedLabels.join('、')}
            </div>
          )}
        </div>

        <div style={{ marginTop: 12 }}>
          <strong>依赖自动补全</strong>
          {addedDeps.length === 0 ? (
            <div style={{ marginTop: 4, fontSize: 12, color: 'rgba(0,0,0,0.45)' }}>
              未发现需要自动补全的依赖
            </div>
          ) : (
            <div style={{ marginTop: 4, fontSize: 12 }}>
              <div>已自动补全：{addedDeps.length} 个模块（{addedDepLabels.join('、')}）</div>
              {reasons.length > 0 && (
                <ul style={{ marginTop: 4, paddingLeft: 20 }}>
                  {reasons.map((r, i) => (
                    <li key={i}>
                      因选择【{r.becauseLabel}】，自动补全【{r.addedIds.map((id) => getModuleLabelById(id) ?? id).join('、')}】
                    </li>
                  ))}
                </ul>
              )}
            </div>
          )}
        </div>
      </section>

      <section style={{ marginTop: 16 }}>
        <h4 style={{ marginBottom: 8 }}>模块诊断（v0）</h4>
        <div style={{ fontSize: 12, fontFamily: 'monospace' }}>
          <div><strong>当前路径：</strong> {pathname || '（空）'}</div>
          <div style={{ marginTop: 4 }}>
            <strong>归属模块：</strong>{' '}
            {resolved.length ? `${resolved[0].moduleLabel}（${resolved[0].groupLabel}）` : '—'}
          </div>
        </div>
        <div style={{ marginTop: 12 }}>
          <strong>按分组的模块列表：</strong>
          <ul style={{ marginTop: 4, paddingLeft: 20, fontSize: 12 }}>
            {moduleList.map((g) => (
              <li key={g.id}>
                {g.label}：{g.modules.map((m) => m.label).join('、')}
              </li>
            ))}
          </ul>
        </div>
      </section>
    </Modal>
  );
}
