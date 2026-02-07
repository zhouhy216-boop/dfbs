# CEO 操作速查（复制粘贴）

命令 + 中文行内注释；每步先 `cd` 再执行，路径均相对仓库根目录。

---

## 一、一键脚本（仓库根执行）

```powershell
cd .   # 【拉取】确保在仓库根
.\DFBS-GIT-PULL.ps1 -NoPause
```

```powershell
cd .   # 【基建】启动 Docker 编排（Postgres/Redis/RabbitMQ/MinIO）
.\DFBS-INFRA-UP.ps1 -NoPause
```

```powershell
cd .   # 【收尾】暂存 + 提交 + 推送（无暂停、不退出当前 shell）
.\DFBS-END.ps1 -NoPause
```

---

## 二、脚本不可用时的等价命令（仓库根）

### 拉取（GIT-PULL 等价）

```powershell
cd .   # 【拉取】查看状态
git status
```

```powershell
cd .   # 【拉取】查看远程
git remote -v
```

```powershell
cd .   # 【拉取】拉取远程
git pull
```

### 基建（INFRA-UP 等价）

```powershell
cd .   # 【基建】检查 Docker（任意目录可执行）
docker version
```

```powershell
cd .   # 【基建】检查 Compose（任意目录可执行）
docker compose version
```

```powershell
cd .   # 【基建】启动所有服务
docker compose -f ".\infra\docker-compose.yml" up -d
```

```powershell
cd .   # 【基建】查看运行中的服务
docker compose -f ".\infra\docker-compose.yml" ps
```

```powershell
cd .   # 【基建】查看某服务日志（把 <service> 换成 postgres / redis / rabbitmq / minio）
docker compose -f ".\infra\docker-compose.yml" logs <service>
```

```powershell
cd .   # 【基建】停止并删除容器（不删卷）
docker compose -f ".\infra\docker-compose.yml" down
```

### 收尾（END 等价）

```powershell
cd .   # 【收尾】查看状态
git status
```

```powershell
cd .   # 【收尾】只暂存已跟踪文件的修改
git add -u
```

```powershell
cd .   # 【收尾】检查是否有暂存（0=无，1=有）
git diff --cached --quiet
```

```powershell
cd .   # 【收尾】提交
git commit -m "sync"
```

```powershell
cd .   # 【收尾】推送（带进度，凭证提示会显示在当前窗口）
git push --progress
```

---

## 三、换电脑前（仓库根）

```powershell
cd .   # 【换电脑前】只暂存已跟踪修改
git add -u
```

```powershell
cd .   # 【换电脑前】可选：包含新文件（可能很慢、容易把 node_modules 等杂物加进去）
git add .
```

```powershell
cd .   # 【换电脑前】提交备份
git commit -m "Backup: before switching computers"
```

```powershell
cd .   # 【换电脑前】推送到远程
git push --progress
```

---

## 四、换电脑后（仓库根）

```powershell
cd .   # 【换电脑后】先看状态
git status
```

```powershell
cd .   # 【换电脑后】拉取
git pull
```

```powershell
cd .   # 【换电脑后】再确认
git status
```

---

## 五、启动后端（仓库根 → 后端目录）

```powershell
cd .\backend\dfbs-app   # 【启动后端】进入后端目录
.\mvnw.cmd spring-boot:run
```
停止：在该窗口按 `Ctrl+C`。

---

## 六、启动前端（仓库根 → 前端目录）

```powershell
cd .   # 【前端】在仓库根（首次或依赖变更时执行）
cd .\frontend\dfbs-ui
npm install
```

```powershell
cd .\frontend\dfbs-ui   # 【前端】启动开发服务器
npm run dev
```

---

## 七、数据库清理（说明：各命令“清掉什么”）

### A) Docker 卷重置（清掉所有持久化数据，含整库）

```powershell
cd .   # 【清库】停止并删容器+卷，清掉 Postgres/MinIO 等所有卷内数据
docker compose -f ".\infra\docker-compose.yml" down -v
```

```powershell
cd .   # 【清库后】重新启动服务
docker compose -f ".\infra\docker-compose.yml" up -d
```

### B) 查当前容器名（便于下面用 docker exec）

```powershell
cd .   # 【查容器】看服务名与容器名
docker compose -f ".\infra\docker-compose.yml" ps
```
Postgres 容器名为：`dfbs-postgres`。

### C) 执行 SQL：只清业务表（truncate-business-data.sql）

清掉表：`work_order`（及 CASCADE 子表）、`platform_account_applications`、`platform_org`、`md_customer`、`contracts`。

```powershell
cd .   # 【清业务表】复制 SQL 进容器
docker cp .\backend\dfbs-app\truncate-business-data.sql dfbs-postgres:/tmp\truncate-business-data.sql
```

```powershell
cd .   # 【清业务表】在容器内用 psql 执行（用户 dfbs，库 dfbs）
docker exec -i dfbs-postgres psql -U dfbs -d dfbs -f /tmp/truncate-business-data.sql
```

### D) 执行 SQL：清空 public 库（wipe-schema.sql）

清掉：删除并重建 `public` schema，库内 public 下全部对象和数据清空。

```powershell
cd .   # 【清 public】复制 SQL 进容器
docker cp .\backend\dfbs-app\scripts\wipe-schema.sql dfbs-postgres:/tmp\wipe-schema.sql
```

```powershell
cd .   # 【清 public】在容器内执行
docker exec -i dfbs-postgres psql -U dfbs -d dfbs -f /tmp/wipe-schema.sql
```

---

## 八、危险操作（仅本地、会清库，慎用）

### WipeSchemaMain（Maven 执行类，会清库）

```powershell
cd .\backend\dfbs-app   # 【危险】执行 WipeSchemaMain，会清库，只对本地
.\mvnw.cmd -q exec:java -Dexec.mainClass="com.dfbs.app.scripts.WipeSchemaMain" -Dexec.classpathScope=test
```

### Spring 启动时清业务表（cleanup 配置）

```powershell
cd .\backend\dfbs-app   # 【危险】带 cleanup 配置启动，启动时清：work_order、platform_account_applications、platform_org、md_customer、contracts
.\mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=cleanup
```

---

## 九、环境检查（任意目录可执行）

以下命令不依赖当前目录，可在任意目录执行；注释中标明“任意目录可执行”。

```powershell
pwsh -v   # 任意目录可执行
```

```powershell
git --version   # 任意目录可执行
```

```powershell
docker version   # 任意目录可执行
```

```powershell
docker compose version   # 任意目录可执行
```

```powershell
node -v   # 任意目录可执行
```

```powershell
npm -v   # 任意目录可执行
```

```powershell
java -version   # 任意目录可执行
```

```powershell
cd .\backend\dfbs-app   # Maven 版本需在后端目录
.\mvnw.cmd -v
```

```powershell
wsl --status   # 任意目录可执行（仅当已装 WSL）
```

```powershell
wsl -l -v   # 任意目录可执行（仅当已装 WSL）
```
