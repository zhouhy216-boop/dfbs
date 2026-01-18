from __future__ import annotations

from dataclasses import dataclass
from datetime import datetime
from pathlib import Path
from typing import Iterable

ROOT = Path(__file__).resolve().parent
OUT_MD = ROOT / "PROJECT_FILES.md"

EXCLUDE_DIRS = {
    ".git", ".idea", ".vscode", "target", "node_modules", "dist", "build", "out",
    ".gradle", "__pycache__"
}

@dataclass(frozen=True)
class Group:
    title: str
    patterns: tuple[str, ...]


# ✅ 注意：GROUPS 必须在 class 之外
GROUPS: list[Group] = [
    Group("本地入口脚本（START/END）", (
        "DFBS-START.bat",
        "DFBS-END.bat",
        "gen_project_files.py",
    )),
    Group("基础设施（Docker）", (
        "infra/docker-compose.yml",
    )),
    Group("权威冻结基准（v2.1_final）", (
        "README.md",
        "PROJECT_STATUS.md",
        "docs/baseline/*",
        "docs/DECISIONS.md",
    )),
    Group("后端入口与健康检查", (
        "backend/dfbs-app/pom.xml",
        "backend/dfbs-app/src/main/java/**/DfbsAppApplication.java",
        "backend/dfbs-app/src/main/java/**/HealthController.java",
        "backend/dfbs-app/src/main/resources/application.yml",
    )),
    Group("报价模块（当前关注）", (
        "backend/dfbs-app/src/main/java/**/quote/**/*.java",
    )),
    Group("数据库迁移（Flyway）", (
        "backend/dfbs-app/src/main/resources/db/migration/*.sql",
    )),
    Group("后端工程骨架（分层目录）", (
        "backend/pom.xml",
        "backend/dfbs-platform/**",
        "backend/dfbs-modules/**",
        "backend/dfbs-application/**",
        "backend/dfbs-interfaces/**",
    )),
]

def is_excluded(path: Path) -> bool:
    return any(part in EXCLUDE_DIRS for part in path.parts)

def rel_posix(p: Path) -> str:
    return p.relative_to(ROOT).as_posix()

def collect_all_files() -> list[str]:
    files: list[str] = []
    for p in ROOT.rglob("*"):
        if p.is_file() and not is_excluded(p):
            files.append(rel_posix(p))
    files.sort()
    return files

def unique_keep_order(items: Iterable[str]) -> list[str]:
    seen: set[str] = set()
    out: list[str] = []
    for x in items:
        if x not in seen:
            seen.add(x)
            out.append(x)
    return out

def match_patterns(patterns: tuple[str, ...]) -> list[str]:
    matched: list[str] = []
    for pat in patterns:
        for p in ROOT.glob(pat):
            if p.exists() and p.is_file() and not is_excluded(p):
                matched.append(rel_posix(p))
    return unique_keep_order(matched)

def build_index() -> dict[str, list[str]]:
    index: dict[str, list[str]] = {}
    for g in GROUPS:
        index[g.title] = match_patterns(g.patterns)
    return index

def main() -> None:
    all_files = collect_all_files()
    index = build_index()

    lines: list[str] = []
    lines.append("# DFBS 项目文件清单（自动生成）")
    lines.append("")
    lines.append(f"生成时间：{datetime.now().strftime('%Y-%m-%d %H:%M:%S')}")
    lines.append("")
    lines.append("## 常用索引（自动生成，按分组）")
    lines.append("> 这一段用于让 ChatGPT / 自己快速定位关键文件，不需要手工维护。")
    lines.append("")

    for title, items in index.items():
        lines.append(f"### {title}")
        if items:
            for p in items:
                lines.append(f"- {p}")
        else:
            lines.append("- （未匹配到）")
        lines.append("")

    lines.append("## 全量文件列表（自动生成）")
    lines.append("")
    lines.append("```")
    lines.extend(all_files)
    lines.append("```")
    lines.append("")

    OUT_MD.write_text("\n".join(lines), encoding="utf-8")
    print("OK -> PROJECT_FILES.md")

if __name__ == "__main__":
    main()
