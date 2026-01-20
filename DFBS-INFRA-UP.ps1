# DFBS-INFRA-UP.ps1 - SAFE infra startup only (no git, no app)
param([switch]$NoPause)

$Root = $PSScriptRoot
$Compose = Join-Path $Root "infra\docker-compose.yml"
$LogDir = Join-Path $Root "logs"
New-Item -ItemType Directory -Force -Path $LogDir | Out-Null
$LogFile = Join-Path $LogDir ("dfbs-infra-up_{0}.log" -f (Get-Date -Format "yyyyMMdd_HHmmss"))

function Log([string]$s) {
  $s | Out-File -FilePath $LogFile -Encoding UTF8 -Append
  Write-Host $s
}

try {
  Write-Host "=================================================="
  Write-Host "DFBS INFRA UP - docker compose up -d"
  Write-Host "Compose: $Compose"
  Write-Host "Log    : $LogFile"
  Write-Host "=================================================="
  Write-Host ""

  if (!(Test-Path $Compose)) { throw "docker-compose.yml not found: $Compose" }

  "==================================================" | Out-File -FilePath $LogFile -Encoding UTF8
  "DFBS INFRA UP" | Out-File -FilePath $LogFile -Encoding UTF8 -Append
  ("Time: {0}" -f (Get-Date)) | Out-File -FilePath $LogFile -Encoding UTF8 -Append
  ("Compose: {0}" -f $Compose) | Out-File -FilePath $LogFile -Encoding UTF8 -Append
  "==================================================" | Out-File -FilePath $LogFile -Encoding UTF8 -Append
  "" | Out-File -FilePath $LogFile -Encoding UTF8 -Append

  Log "[RUN] docker compose up -d"
  cmd /c "docker compose -f `"$Compose`" up -d" 2>&1 | ForEach-Object { Log $_ }

  Log ""
  Log "[RUN] docker ps"
  cmd /c "docker ps --format `"table {{.Names}}\t{{.Status}}\t{{.Ports}}`"" 2>&1 | ForEach-Object { Log $_ }

  Write-Host ""
  Write-Host "[OK] INFRA UP DONE"
  if (-not $NoPause) { Read-Host "Press Enter to close" }
  exit 0
}
catch {
  Log ""
  Log ("[FAILED] {0}" -f $_.Exception.Message)
  Write-Host ""
  Write-Host "[FAILED] $($_.Exception.Message)"
  Write-Host "Please open log: $LogFile"
  if (-not $NoPause) { Read-Host "Press Enter to close" }
  exit 1
}
