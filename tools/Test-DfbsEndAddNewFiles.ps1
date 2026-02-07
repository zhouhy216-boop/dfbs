# Test-DfbsEndAddNewFiles.ps1 - Regression test for "git add new files" with Chinese/non-ASCII and spaces/parens.
# (VNX-20260207-003)
#
# How to run (from repo root D:\dfbs):
#   pwsh -NoProfile -ExecutionPolicy Bypass -File .\tools\Test-DfbsEndAddNewFiles.ps1
# Or:
#   . .\tools\Test-DfbsEndAddNewFiles.ps1
#
# Creates untracked files under _DISCOVERY/_test_add/ (Chinese name, spaces, parens), runs the staging
# helpers, asserts they are staged, then unstages and removes the test dir.
# Ensures DFBS END does not fail with exit 128 / pathspec "346/231/272" artifacts.

$ErrorActionPreference = 'Stop'
$repoRoot = if ($PSScriptRoot) { (Get-Item $PSScriptRoot).Parent.FullName } else { Get-Location }
. (Join-Path $repoRoot "DFBS-UTILS.ps1")

$testDir = Join-Path $repoRoot "_DISCOVERY\_test_add"
$testFiles = @(
    "测试-新增.md",
    "file with spaces.txt",
    "file (with) parens.txt"
)

function Test-DfbsEndAddNewFilesSafe {
    if (-not (Test-Path (Join-Path $repoRoot ".git"))) {
        Write-Host "SKIP: Not a git repo root: $repoRoot" -ForegroundColor Yellow
        return $true
    }
    Push-Location $repoRoot
    try {
        if (-not (Test-Path $testDir)) { New-Item -ItemType Directory -Force -Path $testDir | Out-Null }
        foreach ($f in $testFiles) {
            $path = Join-Path $testDir $f
            Set-Content -LiteralPath $path -Value "test" -Encoding UTF8 -Force
        }
        $pathListFile = Get-SafeUntrackedPathListFile -RepoRoot $repoRoot
        if (-not $pathListFile) {
            Write-Host "FAIL: Get-SafeUntrackedPathListFile returned null (expected test files to be untracked)." -ForegroundColor Red
            return $false
        }
        $logDir = Get-LogDir
        $logFile = Join-Path $logDir "test-add-new-files.log"
        Invoke-GitAddPathspecNul -RepoRoot $repoRoot -PathspecFile $pathListFile -LogFile $logFile -TimeoutSec 60
        $staged = & git -c core.quotepath=false -C $repoRoot diff --cached --name-only 2>&1 | Out-String
        $stagedLines = @($staged -split "[\r\n]+" | ForEach-Object { $_.Trim() } | Where-Object { $_ -ne "" })
        $testStaged = $stagedLines | Where-Object { $_ -like "*_test_add*" }
        $ok = $testStaged.Count -ge $testFiles.Count
        if ($ok) {
            foreach ($f in $testFiles) { Write-Host "OK: Staged (in _test_add): $f" -ForegroundColor Green }
        } else {
            Write-Host "FAIL: Expected at least $($testFiles.Count) staged paths under _test_add, got $($testStaged.Count). Staged: $($testStaged -join ', ')" -ForegroundColor Red
        }
        & git -C $repoRoot reset HEAD -- _DISCOVERY/_test_add/ 2>&1 | Out-Null
        Remove-Item -LiteralPath $testDir -Recurse -Force -ErrorAction SilentlyContinue
        return $ok
    } finally {
        Pop-Location
    }
}

# When run as script (e.g. pwsh -File tools/Test-DfbsEndAddNewFiles.ps1), run test and exit.
$result = Test-DfbsEndAddNewFilesSafe
if ($result) { Write-Host "PASS: Add new files (non-ASCII/safe) regression test." -ForegroundColor Green; exit 0 }
else { Write-Host "FAIL: Regression test failed." -ForegroundColor Red; exit 1 }
