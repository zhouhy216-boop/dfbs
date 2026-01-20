# DFBS-DEV.ps1 - SAFE dev flow (no git pull)
# 1) infra up (this window)
# 2) app run  (this window, long-running)
# 3) healthz  (new window, delayed)
param([switch]$NoPause)

$Root = $PSScriptRoot

Write-Host "=================================================="
Write-Host "DFBS DEV (SAFE)"
Write-Host "1) INFRA UP"
Write-Host "2) APP RUN (this window)"
Write-Host "3) HEALTHZ (new window, delayed 5s)"
Write-Host "NO git pull / NO tests"
Write-Host "=================================================="
Write-Host ""

# 1) infra up (block until finished)
& (Join-Path $Root "DFBS-INFRA-UP.ps1") -NoPause

Write-Host ""
Write-Host "=================================================="
Write-Host "Starting HEALTHZ in a new window (after 5 seconds)..."
Write-Host "=================================================="
Write-Host ""

# 3) healthz in a new window (delay to let app start)
# Use -ExecutionPolicy Bypass to avoid policy prompts; -NoExit so window stays.
$healthzScript = Join-Path $Root "DFBS-HEALTHZ.ps1"
$healthzCmd = "Start-Sleep -Seconds 5; & `"$healthzScript`""
Start-Process powershell.exe -ArgumentList @(
  "-NoProfile",
  "-ExecutionPolicy", "Bypass",
  "-NoExit",
  "-Command", $healthzCmd
) | Out-Null

# 2) app run (long-running in this window)
Write-Host "=================================================="
Write-Host "Starting APP RUN in this window..."
Write-Host "=================================================="
Write-Host ""
& (Join-Path $Root "DFBS-APP-RUN.ps1") -NoPause

if (-not $NoPause) { Read-Host "Press Enter to close" }
