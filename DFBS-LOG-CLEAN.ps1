# DFBS-LOG-CLEAN.ps1 - keep last N dfbs-*.log in root logs\
param(
  [int]$Keep = 200,
  [switch]$NoPause
)

$Root   = $PSScriptRoot
$LogDir = Join-Path $Root "logs"

Write-Host "=================================================="
Write-Host "DFBS LOG CLEAN"
Write-Host "Dir : $LogDir"
Write-Host "Keep: $Keep"
Write-Host "Rule: only root logs\dfbs-*.log"
Write-Host "=================================================="
Write-Host ""

if (!(Test-Path $LogDir)) {
  Write-Host "[OK] logs folder not found, nothing to do."
  if (-not $NoPause) { Read-Host "Press Enter to close" }
  exit 0
}

# Only clean root logs\dfbs-*.log
$files = Get-ChildItem -Path $LogDir -File -Filter "dfbs-*.log" | Sort-Object LastWriteTime -Descending

$total = $files.Count
if ($total -le $Keep) {
  Write-Host "[OK] No cleanup needed. Files=$total, Keep=$Keep"
  if (-not $NoPause) { Read-Host "Press Enter to close" }
  exit 0
}

$toDelete = $files | Select-Object -Skip $Keep
Write-Host "[INFO] Total dfbs-*.log files: $total"
Write-Host "[INFO] Will delete: $($toDelete.Count)"
Write-Host ""

$deleted = 0
foreach ($f in $toDelete) {
  try {
    Remove-Item -LiteralPath $f.FullName -Force
    $deleted++
  } catch {
    Write-Host "[WARN] Failed to delete: $($f.FullName) -> $($_.Exception.Message)"
  }
}

Write-Host ""
Write-Host "[OK] Deleted $deleted file(s). Remaining should be <= $Keep."

if (-not $NoPause) { Read-Host "Press Enter to close" }
