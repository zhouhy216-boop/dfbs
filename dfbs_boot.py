# -*- coding: utf-8 -*-
"""
DFBS 一键启动脚本（Win11 强稳定版，避免 GBK/Unicode 解码问题）

覆盖重启后的步骤 2/3/4/5：
- docker compose up -d
- 容器检查（不解析中文输出，只检查命令是否成功 + 用 docker inspect 判断运行状态）
- 新窗口启动 Spring Boot (mvnw spring-boot:run)
- 轮询 /api/healthz 直到 ok

用法：
  python dfbs_boot.py all
  python dfbs_boot.py up
  python dfbs_boot.py status
  python dfbs_boot.py app
"""

from __future__ import annotations
import os
import sys
import time
import subprocess
from urllib.request import urlopen
from urllib.error import URLError, HTTPError

PROJECT_ROOT = r"C:\Users\zhouhy\dfbs"
COMPOSE_FILE = os.path.join(PROJECT_ROOT, "infra", "docker-compose.yml")
APP_DIR = os.path.join(PROJECT_ROOT, "backend", "dfbs-app")

REQUIRED_CONTAINERS = [
    "dfbs-postgres",
    "dfbs-redis",
    "dfbs-rabbitmq",
    "dfbs-minio",
]

HEALTH_URL = "http://localhost:8080/api/healthz"


def run_no_decode(cmd: list[str], cwd: str | None = None, check: bool = True) -> subprocess.CompletedProcess:
    """
    不捕获 stdout/stderr，避免 Windows GBK/UTF-8 解码问题。
    """
    print(f"\n$ {' '.join(cmd)}")
    return subprocess.run(cmd, cwd=cwd, check=check)


def docker_ready() -> None:
    """
    验证 docker 可用且 daemon 已启动。
    """
    try:
        subprocess.run(["docker", "--version"], check=True, stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)
        subprocess.run(["docker", "info"], check=True, stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)
    except Exception:
        print("❌ Docker 不可用：请先打开 Docker Desktop，并等待状态为 Running。")
        sys.exit(2)


def compose_up() -> None:
    docker_ready()
    run_no_decode(["docker", "compose", "-f", COMPOSE_FILE, "up", "-d"], cwd=PROJECT_ROOT, check=True)


def container_running(name: str) -> bool:
    """
    用 docker inspect 判断容器是否 Running（不依赖 stdout 中文编码）。
    """
    cp = subprocess.run(
        ["docker", "inspect", "-f", "{{.State.Running}}", name],
        stdout=subprocess.PIPE,
        stderr=subprocess.DEVNULL,
        text=True,
        encoding="utf-8",
        errors="ignore",
    )
    if cp.returncode != 0:
        return False
    return cp.stdout.strip().lower() == "true"


def status() -> None:
    docker_ready()

    missing = []
    for c in REQUIRED_CONTAINERS:
        if not container_running(c):
            missing.append(c)

    if missing:
        print("❌ 缺少或未运行的容器：")
        for m in missing:
            print(f" - {m}")
        print("\n👉 先执行：python dfbs_boot.py up")
        sys.exit(3)

    print("✅ 4 个基础容器都在运行。")


def wait_health(timeout_sec: int = 90) -> None:
    deadline = time.time() + timeout_sec
    while time.time() < deadline:
        try:
            with urlopen(HEALTH_URL, timeout=2) as resp:
                body = resp.read().decode("utf-8", errors="ignore").strip()
                if body.lower() == "ok":
                    print(f"✅ healthz OK: {HEALTH_URL}")
                    return
        except (URLError, HTTPError):
            pass
        time.sleep(1)

    print(f"❌ healthz 超时未就绪（>{timeout_sec}s）：{HEALTH_URL}")
    print("👉 可能原因：应用未启动/端口占用/启动报错。请看新开的 app 窗口日志。")
    sys.exit(4)


def start_app_new_window() -> None:
    mvnw = os.path.join(APP_DIR, "mvnw.cmd")
    if not os.path.exists(mvnw):
        print(f"❌ 找不到 mvnw：{mvnw}")
        sys.exit(5)

    ps_cmd = f'cd "{APP_DIR}"; .\\mvnw.cmd spring-boot:run'
    print("\n🚀 将在新窗口启动 Spring Boot：")
    print(ps_cmd)

    # 新窗口启动，不阻塞当前脚本
    subprocess.Popen(
        ["powershell", "-NoExit", "-Command", ps_cmd],
        cwd=APP_DIR,
        stdout=subprocess.DEVNULL,
        stderr=subprocess.DEVNULL,
    )

    print("\n⏳ 等待 /api/healthz 就绪 ...")
    wait_health(timeout_sec=120)


def main() -> None:
    if len(sys.argv) < 2:
        print("用法：python dfbs_boot.py up|status|app|all")
        sys.exit(1)

    cmd = sys.argv[1].lower().strip()

    if cmd == "up":
        compose_up()
        status()
        return

    if cmd == "status":
        status()
        return

    if cmd == "app":
        status()
        start_app_new_window()
        return

    if cmd == "all":
        compose_up()
        status()
        start_app_new_window()
        return

    print("未知命令：", cmd)
    print("用法：python dfbs_boot.py up|status|app|all")
    sys.exit(1)


if __name__ == "__main__":
    main()
