# DFBS-HEALTHZ.ps1 - SAFE health check only
param([switch]$NoPause)

$Root = $PSScriptRoot
$LogDir = Join-Path $Root "logs"
New-Item -ItemType Directory -Force -Path $LogDir | Out-Null
$LogFile = Join-Path $LogDir ("dfbs-healthz_{0}.log" -f (Get-Date -Format "yyyyMMdd_HHmmss"))

$Url = "http://localhost:8080/api/healthz"

function Log([string]$s) {
  $s | Out-File -FilePath $LogFile -Encoding UTF8 -Append
  Write-Host $s
}

Log "=================================================="
Log "DFBS HEALTHZ"
Log ("Time: {0}" -f (Get-Date))
Log ("URL : {0}" -f $Url)
Log ("Log : {0}" -f $LogFile)
Log "=================================================="
Log ""

try {
  $resp = Invoke-WebRequest -Uri $Url -TimeoutSec 5 -UseBasicParsing
  Log ("Status: {0}" -f $resp.StatusCode)
  Log "Body:"
  Log $resp.Content
} catch {
  Log ("FAILED: {0}" -f $_.Exception.Message)
}

if (-not $NoPause) { Read-Host "Press Enter to close" }
