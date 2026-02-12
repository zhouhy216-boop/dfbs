import { useMemo, useState, useEffect } from 'react';
import { useLocation } from 'react-router-dom';
import { Alert, Button, Checkbox, Input, Modal, Radio, Space, Table, Tag, Typography } from 'antd';
import type { CheckboxChangeEvent } from 'antd/es/checkbox';
import type { RadioChangeEvent } from 'antd/es/radio';
import {
  MODULE_GROUPS,
  UNMAPPED_KEY,
  getAllSelectableModuleIds,
  getModuleLabelById,
  resolveModulesByPath,
  resolveSelectionWithDeps,
} from './moduleRegistry';
import { fetchPreview, type TestDataCleanerPreviewResponse } from './previewApi';
import { fetchExecute, type TestDataCleanerExecuteResponse } from './executeApi';

export interface TestDataCleanerModalProps {
  open: boolean;
  onClose: () => void;
}

type PresetMode = 'full' | 'current' | 'custom';

/**
 * Test Data Cleaner modal (admin-only). Selection + preview + execute (RESET 门槛 + 一次性报告).
 */
export function TestDataCleanerModal({ open, onClose }: TestDataCleanerModalProps) {
  const location = useLocation();
  const pathname = location.pathname ?? '';
  const resolved = useMemo(() => resolveModulesByPath(pathname), [pathname]);
  const moduleList = useMemo(() => MODULE_GROUPS, []);
  const allSelectableIds = useMemo(() => getAllSelectableModuleIds(), []);

  const [presetMode, setPresetMode] = useState<PresetMode>('current');
  const [customSelectedIds, setCustomSelectedIds] = useState<string[]>([]);
  const [previewLoading, setPreviewLoading] = useState(false);
  const [previewError, setPreviewError] = useState<string | null>(null);
  const [previewResult, setPreviewResult] = useState<TestDataCleanerPreviewResponse | null>(null);
  const [executeLoading, setExecuteLoading] = useState(false);
  const [executeError, setExecuteError] = useState<string | null>(null);
  const [executeResult, setExecuteResult] = useState<TestDataCleanerExecuteResponse | null>(null);
  const [resetConfirmInput, setResetConfirmInput] = useState('');

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

  useEffect(() => {
    setPreviewResult(null);
    setPreviewError(null);
    setExecuteResult(null);
    setExecuteError(null);
    setResetConfirmInput('');
  }, [effectiveSelectedIds.join(',')]);

  const handlePreview = async () => {
    if (effectiveSelectedIds.length === 0) return;
    setPreviewLoading(true);
    setPreviewError(null);
    try {
      const res = await fetchPreview({ moduleIds: effectiveSelectedIds });
      setPreviewResult(res);
    } catch (e) {
      setPreviewResult(null);
      setPreviewError(e instanceof Error ? e.message : '预览请求失败');
    } finally {
      setPreviewLoading(false);
    }
  };

  const handleExecute = async () => {
    if (effectiveSelectedIds.length === 0 || !previewResult) return;
    if (previewResult.requiresResetConfirm && resetConfirmInput !== 'RESET') return;
    setExecuteLoading(true);
    setExecuteError(null);
    try {
      const res = await fetchExecute({
        moduleIds: effectiveSelectedIds,
        confirmText: previewResult.requiresResetConfirm ? resetConfirmInput : null,
        includeAttachments: false,
      });
      setExecuteResult(res);
    } catch (e: unknown) {
      const err = e as { response?: { data?: { machineCode?: string; message?: string } }; message?: string };
      const code = err.response?.data?.machineCode;
      if (code === 'RESET_CONFIRM_REQUIRED') setExecuteError('需要输入 RESET 才能执行');
      else if (code === 'ATTACHMENTS_NOT_SUPPORTED_YET') setExecuteError('暂不支持附件清理');
      else setExecuteError(err.response?.data?.message ?? (err instanceof Error ? err.message : '执行失败'));
    } finally {
      setExecuteLoading(false);
    }
  };

  const executeDisabled =
    effectiveSelectedIds.length === 0 ||
    executeLoading ||
    !previewResult ||
    (previewResult.requiresResetConfirm && resetConfirmInput !== 'RESET');

  const handlePresetChange = (e: RadioChangeEvent) => {
    setPresetMode((e.target.value ?? 'current') as PresetMode);
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
          <Button
            key="preview"
            disabled={effectiveSelectedIds.length === 0 || previewLoading}
            loading={previewLoading}
            onClick={handlePreview}
          >
            预览
          </Button>
          <Button
            key="run"
            type="primary"
            danger
            disabled={executeDisabled}
            loading={executeLoading}
            onClick={handleExecute}
          >
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
        仅限管理员使用。请先选择范围并点击「预览」，再根据需要执行清理。执行后将显示本次报告。
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

      {previewError && (
        <Alert type="error" message={previewError} style={{ marginTop: 16 }} showIcon />
      )}

      {previewResult && (
        <section style={{ marginTop: 16 }}>
          <Typography.Title level={5} style={{ marginBottom: 8 }}>
            模块汇总数量
          </Typography.Title>
          <Table
            size="small"
            pagination={false}
            dataSource={previewResult.items.map((item) => ({
              key: item.moduleId,
              模块: getModuleLabelById(item.moduleId) ?? item.moduleId,
              预估删除数量: item.count,
            }))}
            columns={[
              { title: '模块', dataIndex: '模块', key: 'module' },
              { title: '预估删除数量', dataIndex: '预估删除数量', key: 'count', align: 'right' as const },
            ]}
            summary={() => (
              <Table.Summary fixed>
                <Table.Summary.Row>
                  <Table.Summary.Cell index={0}>合计</Table.Summary.Cell>
                  <Table.Summary.Cell index={1} align="right">
                    {previewResult.totalCount}
                  </Table.Summary.Cell>
                </Table.Summary.Row>
              </Table.Summary>
            )}
          />
          {previewResult.invalidModuleIds && previewResult.invalidModuleIds.length > 0 && (
            <Typography.Text type="warning" style={{ fontSize: 12, display: 'block', marginTop: 8 }}>
              以下模块 ID 未被识别，已忽略：{previewResult.invalidModuleIds.join('、')}
            </Typography.Text>
          )}

          <Typography.Title level={5} style={{ marginTop: 16, marginBottom: 8 }}>
            RESET 输入门槛
          </Typography.Title>
          {previewResult.requiresResetConfirm ? (
            <Alert
              type="warning"
              showIcon
              message={
                previewResult.requiresResetReasons?.includes('FULL_RESET')
                  ? '将执行全量重置'
                  : '已选择基础模块，需确认执行 RESET'
              }
            />
          ) : (
            <Typography.Text type="secondary">当前选择无需 RESET 确认。</Typography.Text>
          )}

          {previewResult.requiresResetConfirm && (
            <div style={{ marginTop: 12 }}>
              <Typography.Text strong>请输入 RESET 以确认：</Typography.Text>
              <Input
                value={resetConfirmInput}
                onChange={(e) => setResetConfirmInput(e.target.value)}
                placeholder="RESET"
                style={{ marginTop: 8, maxWidth: 200 }}
                status={resetConfirmInput && resetConfirmInput !== 'RESET' ? 'error' : undefined}
              />
            </div>
          )}
        </section>
      )}

      {!previewResult && effectiveSelectedIds.length > 0 && (
        <Typography.Text type="secondary" style={{ display: 'block', marginTop: 8 }}>
          请先点击预览
        </Typography.Text>
      )}

      {executeError && (
        <Alert type="error" message={executeError} style={{ marginTop: 16 }} showIcon />
      )}

      {executeResult && (
        <section style={{ marginTop: 16 }}>
          <Typography.Title level={5} style={{ marginBottom: 8 }}>
            执行报告（本次）
          </Typography.Title>
          <div style={{ marginBottom: 12, fontSize: 14 }}>
            <div><strong>总删除条数：</strong>{executeResult.totalDeleted}</div>
            <div style={{ marginTop: 4 }}>
              <strong>整体状态：</strong>
              {executeResult.status === 'SUCCESS' ? '成功' : executeResult.status === 'PARTIAL' ? '部分成功' : '失败'}
            </div>
            <div style={{ marginTop: 4 }}><strong>Redis：</strong>{executeResult.redisMessage}</div>
          </div>
          {executeResult.items.map((item) => (
            <div key={item.moduleId} style={{ marginBottom: 12, padding: 8, background: 'rgba(0,0,0,0.02)', borderRadius: 4 }}>
              <div style={{ fontWeight: 600 }}>
                {getModuleLabelById(item.moduleId) ?? item.moduleId} — 模块状态：
                {item.moduleStatus === 'SUCCESS' ? '成功' : item.moduleStatus === 'PARTIAL' ? '部分成功' : '失败'}，删除 {item.moduleDeletedTotal} 条
              </div>
              {item.tables && item.tables.length > 0 && (
                <ul style={{ marginTop: 6, paddingLeft: 20, fontSize: 12 }}>
                  {item.tables.map((t) => (
                    <li key={t.table}>
                      {t.table}：删除 {t.deleted} 条，状态 {t.status === 'SUCCESS' ? '成功' : t.status === 'FAILED' ? '失败' : '跳过'}
                      {t.error ? `（${t.error}）` : ''}
                    </li>
                  ))}
                </ul>
              )}
            </div>
          ))}
        </section>
      )}

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
