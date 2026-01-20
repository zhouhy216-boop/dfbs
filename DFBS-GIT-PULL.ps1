# DFBS-GIT-PULL.ps1 - SAFE git pull only
param([switch]$NoPause)

$Root = $PSScriptRoot
$LogDir = Join-Path $Root "logs"
New-Item -ItemType Directory -Force -Path $LogDir | Out-Null
$LogFile = Join-Path $LogDir ("dfbs-git-pull_{0}.log" -f (Get-Date -Format "yyyyMMdd_HHmmss"))

Write-Host "=== DFBS GIT PULL ==="
Write-Host "Root: $Root"
Write-Host "Log : $LogFile"
Write-Host ""

cmd /c "git pull" 2>&1 | Tee-Object -FilePath $LogFile -Append

if (-not $NoPause) {
  Read-Host "Press Enter to close"
}
