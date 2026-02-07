# CEO 操作速查（一行一行复制）

每步两行：第一行 `cd` 说明在哪儿执行，第二行要执行的命令。注释用大白话说明「属于哪一步、干了啥、清空啥」。

---

## 【开工】确认本机 Docker 已就绪（可验证的命令）

```
cd .   # 【开工】在仓库根；任意目录也能执行
```

```
docker version   # 【开工】检查本机是否装了 Docker、能否连上引擎
```

```
docker compose version   # 【开工】检查本机是否有 Compose（任意目录也能执行）
```

---

## 【脚本-首选】拉取远程代码

```
cd .   # 【脚本-首选】在仓库根，准备拉取
```

```
.\DFBS-GIT-PULL.ps1 -NoPause   # 【脚本-首选】拉取远程代码（无暂停）
```

---

## 【脚本-首选】启动本机数据库和中间件（Postgres/Redis/RabbitMQ/MinIO）

```
cd .   # 【脚本-首选】在仓库根，准备启动
```

```
.\DFBS-INFRA-UP.ps1 -NoPause   # 【脚本-首选】启动本机数据库和中间件（无暂停）
```

---

## 【脚本-首选】下班前：暂存 + 提交 + 推送到远程

```
cd .   # 【脚本-首选】在仓库根，准备收尾
```

```
.\DFBS-END.ps1 -NoPause   # 【脚本-首选】暂存、提交、推送（无暂停、不关窗口）
```

---

## 【换电脑前】用脚本收尾（首选）

```
cd .   # 【换电脑前】在仓库根
```

```
.\DFBS-END.ps1 -NoPause   # 【换电脑前】把当前改动推上去，换电脑后能拉下来
```

---

## 【换电脑后】用脚本拉取（首选）

```
cd .   # 【换电脑后】在仓库根
```

```
.\DFBS-GIT-PULL.ps1 -NoPause   # 【换电脑后】把远程最新代码拉下来
```

---

## 【脚本坏了-手动等价】拉取：看状态、看远程、拉取

```
cd .   # 【拉取-手动】在仓库根
```

```
git status   # 【拉取-手动】看当前有哪些改动
```

```
cd .   # 【拉取-手动】在仓库根
```

```
git remote -v   # 【拉取-手动】看远程地址
```

```
cd .   # 【拉取-手动】在仓库根
```

```
git pull   # 【拉取-手动】从远程拉最新代码
```

---

## 【脚本坏了-手动等价】启动本机服务：看版本、启动、看状态、看日志、停止

```
cd .   # 【启动-手动】在仓库根；任意目录也能执行
```

```
docker version   # 【启动-手动】检查 Docker 是否可用
```

```
cd .   # 【启动-手动】在仓库根；任意目录也能执行
```

```
docker compose version   # 【启动-手动】检查 Compose 是否可用
```

```
cd .   # 【启动-手动】在仓库根
```

```
docker compose -f ".\infra\docker-compose.yml" up -d   # 【启动-手动】启动本机数据库和中间件（后台跑）
```

```
cd .   # 【启动-手动】在仓库根
```

```
docker compose -f ".\infra\docker-compose.yml" ps   # 【启动-手动】看哪些服务在跑
```

```
cd .   # 【启动-手动】在仓库根
```

```
docker compose -f ".\infra\docker-compose.yml" logs postgres   # 【启动-手动】看数据库服务日志
```

```
cd .   # 【启动-手动】在仓库根
```

```
docker compose -f ".\infra\docker-compose.yml" logs redis   # 【启动-手动】看 Redis 服务日志
```

```
cd .   # 【启动-手动】在仓库根
```

```
docker compose -f ".\infra\docker-compose.yml" logs rabbitmq   # 【启动-手动】看 RabbitMQ 服务日志
```

```
cd .   # 【启动-手动】在仓库根
```

```
docker compose -f ".\infra\docker-compose.yml" logs minio   # 【启动-手动】看 MinIO 服务日志
```

```
cd .   # 【启动-手动】在仓库根
```

```
docker compose -f ".\infra\docker-compose.yml" down   # 【启动-手动】停止所有服务（不删本机数据）
```

---

## 【脚本坏了-手动等价】收尾：暂存、看有没有要提交的、提交、推送

```
cd .   # 【收尾-手动】在仓库根
```

```
git status   # 【收尾-手动】看当前有哪些改动
```

```
cd .   # 【收尾-手动】在仓库根
```

```
git add -u   # 【收尾-手动】只把已跟踪文件的修改放进「待提交」
```

```
cd .   # 【收尾-手动】在仓库根
```

```
git diff --cached --quiet   # 【收尾-手动】看有没有待提交的（0=没有，1=有）
```

```
cd .   # 【收尾-手动】在仓库根
```

```
git commit -m "sync"   # 【收尾-手动】把待提交的打成一条提交
```

```
cd .   # 【收尾-手动】在仓库根
```

```
git push --progress   # 【收尾-手动】推送到远程（会显示进度；要输密码时在本窗口输）
```

---

## 【启动后端】在后端目录启动 Spring Boot

```
cd .\backend\dfbs-app   # 【启动后端】进入后端目录
```

```
.\mvnw.cmd spring-boot:run   # 【启动后端】启动本机后端服务（停用请在本窗口按 Ctrl+C）
```

---

## 【启动前端】在前端目录装依赖、启动开发服务器

```
cd .\frontend\dfbs-ui   # 【启动前端】进入前端目录（仓库根没有 package.json，必须进这里）
```

```
npm install   # 【启动前端】安装依赖（首次或依赖改过时执行）
```

```
cd .\frontend\dfbs-ui   # 【启动前端】进入前端目录
```

```
npm run dev   # 【启动前端】启动本机前端开发服务器
```

---

## 【清库】方法一：停服务并删掉本机数据（数据库+文件存储等全部清空）

说明：只影响这台电脑上的本机数据库、本机测试数据、本机文件存储等，不影响 GitHub 上的代码。

```
cd .   # 【清库-方法一】在仓库根
```

```
docker compose -f ".\infra\docker-compose.yml" down -v   # 【清库-方法一】停服务并删掉本机数据（卷删掉=数据库和 MinIO 存的数据全没）
```

```
cd .   # 【清库-方法一】在仓库根
```

```
docker compose -f ".\infra\docker-compose.yml" up -d   # 【清库-方法一】重新启动本机服务（库是空的）
```

---

## 【清库】方法二：只清业务表（不动库结构）

说明：执行仓库里的 `truncate-business-data.sql`，清空这些表里的数据：work_order（及级联子表）、platform_account_applications、platform_org、md_customer、contracts。库结构还在。

```
cd .   # 【清库-方法二】在仓库根
```

```
docker cp .\backend\dfbs-app\truncate-business-data.sql dfbs-postgres:/tmp/truncate-business-data.sql   # 【清库-方法二】把 SQL 文件拷进数据库容器
```

```
cd .   # 【清库-方法二】在仓库根
```

```
docker compose -f ".\infra\docker-compose.yml" exec -T postgres psql -U dfbs -d dfbs -f /tmp/truncate-business-data.sql   # 【清库-方法二】在容器里执行 SQL，清空业务表数据
```

---

## 【清库】方法三：清空 public 里所有对象（本机数据库从零开始）

说明：执行仓库里的 `wipe-schema.sql`，删掉并重建 public，等于本机数据库里 public 下所有表、数据都没了，从零开始。

```
cd .   # 【清库-方法三】在仓库根
```

```
docker cp .\backend\dfbs-app\scripts\wipe-schema.sql dfbs-postgres:/tmp/wipe-schema.sql   # 【清库-方法三】把 SQL 拷进数据库容器
```

```
cd .   # 【清库-方法三】在仓库根
```

```
docker compose -f ".\infra\docker-compose.yml" exec -T postgres psql -U dfbs -d dfbs -f /tmp/wipe-schema.sql   # 【清库-方法三】在容器里执行，清空 public 里所有对象
```

---

## 【环境检查】每行检查一项（任意目录也能执行的已标出）

```
cd .   # 【环境检查】在仓库根；任意目录也能执行
```

```
pwsh -v   # 【环境检查】看 PowerShell 版本；任意目录也能执行
```

```
cd .   # 【环境检查】在仓库根；任意目录也能执行
```

```
git --version   # 【环境检查】看 Git 是否装好；任意目录也能执行
```

```
cd .   # 【环境检查】在仓库根；任意目录也能执行
```

```
docker version   # 【环境检查】看 Docker 是否装好、能否连上引擎；任意目录也能执行
```

```
cd .   # 【环境检查】在仓库根；任意目录也能执行
```

```
docker compose version   # 【环境检查】看 Compose 是否可用；任意目录也能执行
```

```
cd .   # 【环境检查】在仓库根；任意目录也能执行
```

```
node -v   # 【环境检查】看 Node 版本；任意目录也能执行
```

```
cd .   # 【环境检查】在仓库根；任意目录也能执行
```

```
npm -v   # 【环境检查】看 npm 版本；任意目录也能执行
```

```
cd .   # 【环境检查】在仓库根；任意目录也能执行
```

```
java -version   # 【环境检查】看 Java 是否装好；任意目录也能执行
```

```
cd .\backend\dfbs-app   # 【环境检查】必须在后端目录看 Maven 版本
```

```
.\mvnw.cmd -v   # 【环境检查】看 Maven（mvnw）版本
```

```
cd .   # 【环境检查】在仓库根；任意目录也能执行（仅当已装 WSL）
```

```
wsl --status   # 【环境检查】看 WSL 状态；任意目录也能执行（仅当已装 WSL）
```

```
cd .   # 【环境检查】在仓库根；任意目录也能执行（仅当已装 WSL）
```

```
wsl -l -v   # 【环境检查】看 WSL 里的发行版；任意目录也能执行（仅当已装 WSL）
```
