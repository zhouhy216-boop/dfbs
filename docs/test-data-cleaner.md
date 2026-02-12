# 测试数据清理器（CI/脚本触发）

通过脚本或 CI 调用后端测试数据清理接口：预览（preview）与执行（execute）。仅支持现有管理员 Bearer Token，通过环境变量传入；不支持附件（v1 仅 `includeAttachments: false`）。

## 前置条件

- 后端默认基地址：`http://localhost:8080`（可由环境变量覆盖）。
- 必须使用已有的管理员 Bearer Token，仅通过环境变量传入，脚本内不得打印 Token。

## 环境变量

| 变量 | 必填 | 说明 |
|------|------|------|
| `ADMIN_BEARER_TOKEN` | 是 | 管理员 Bearer Token；**切勿在日志或输出中打印**。 |
| `TDCLEAN_BASE_URL` | 否 | 后端基地址，默认 `http://localhost:8080`。 |

## 示例命令（可直接复制）

### PowerShell

```powershell
# 设置 Token（示例：从登录接口获取后写入环境变量，不要打印）
$env:ADMIN_BEARER_TOKEN = "<your-token>"
$env:TDCLEAN_BASE_URL = "http://localhost:8080"   # 可选，默认即此

# 预览（单模块 dashboard，通常 0 删除，适合冒烟）
.\scripts\tdclean_preview.ps1 -moduleIds "dashboard"
.\scripts\tdclean_execute.ps1 -moduleIds "dashboard"
```

### Bash

```bash
export ADMIN_BEARER_TOKEN="<your-token>"
export TDCLEAN_BASE_URL="http://localhost:8080"   # 可选

./scripts/tdclean_preview.sh --moduleIds "dashboard"
./scripts/tdclean_execute.sh --moduleIds "dashboard"
```

### 需要重置确认时（如 org-tree）

当预览返回需确认（例如 `RESET_CONFIRM_REQUIRED`）时，执行需传入 `confirmText=RESET`：

```bash
./scripts/tdclean_execute.sh --moduleIds "org-tree" --confirmText "RESET"
```

```powershell
.\scripts\tdclean_execute.ps1 -moduleIds "org-tree" -confirmText "RESET"
```

## 退出码

| 退出码 | 含义 |
|--------|------|
| 0 | 成功（preview 成功；execute 的 status=SUCCESS） |
| 2 | 部分成功（仅 execute，status=PARTIAL） |
| 1 | 失败或其他错误（含 execute 的 status=FAILED、未分类 4xx/5xx 等） |
| 3 | 无效输入/安全拦截（如 RESET_CONFIRM_REQUIRED、ATTACHMENTS_NOT_SUPPORTED_YET、或脚本层 moduleIds 为空等） |
| 4 | 未授权/禁止（HTTP 401/403） |

脚本始终将接口返回的**原始 JSON** 输出到 stdout，便于 CI 解析。

## v1 限制与安全说明

- **附件**：当前不支持附件，请求中 `includeAttachments` 须为 false 或省略。
- **安全**：execute 会真实删除数据，建议先用 `dashboard`（通常 0 删除）做冒烟；**切勿对生产环境执行**。
