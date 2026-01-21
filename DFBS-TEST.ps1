param([switch]$NoPause)

# ==================================================
# DFBS-TEST.ps1 (PowerShell 7+ only)
# 目标：
# - 实时滚动输出（避免“像卡死”）
# - 同时生成 FULL LOG + SUMMARY LOG
# - PASS/FAIL 只由 mvnw exit code 决定（忽略 warning）
# ==================================================

$Root   = $PSScriptRoot
$App    = Join-Path $Root "backend\dfbs-app"
$LogDir = Join-Path $Root "logs"
New-Item -ItemType Directory -Force -Path $LogDir | Out-Null

$Ts = Get-Date -Format "yyyyMMdd_HHmmss"
$LogFile     = Join-Path $LogDir ("dfbs-test_{0}.log" -f $Ts)
$SummaryFile = Join-Path $LogDir ("dfbs-test_{0}.summary.log" -f $Ts)

function Write-Header([string]$path) {
  "==================================================" | Out-File -FilePath $path -Encoding UTF8
  "DFBS TEST - mvnw clean test"                        | Out-File -FilePath $path -Encoding UTF8 -Append
  ("Time: {0}" -f (Get-Date -Format "yyyy/M/d HH:mm:ss")) | Out-File -FilePath $path -Encoding UTF8 -Append
  ("Root: {0}" -f $Root)                              | Out-File -FilePath $path -Encoding UTF8 -Append
  ("App : {0}" -f $App)                               | Out-File -FilePath $path -Encoding UTF8 -Append
  "==================================================" | Out-File -FilePath $path -Encoding UTF8 -Append
  ""                                                  | Out-File -FilePath $path -Encoding UTF8 -Append
}

function Build-Summary([string]$fullLog, [string]$summaryLog, [int]$exitCode) {
  $lines = Get-Content -Path $fullLog -Encoding UTF8

  $out = New-Object System.Collections.Generic.List[string]
  $out.Add("==================================================")
  $out.Add("DFBS TEST - SUMMARY")
  $out.Add(("Time: {0}" -f (Get-Date -Format "yyyy/M/d HH:mm:ss")))
  $out.Add(("RESULT: {0} (exit code={1})" -f ($(if ($exitCode -eq 0) {"SUCCESS"} else {"FAILURE"}), $exitCode)))
  $out.Add(("FULL LOG   : {0}" -f $fullLog))
  $out.Add(("SUMMARY LOG: {0}" -f $summaryLog))
  $out.Add("==================================================")
  $out.Add("")

  # 关键行：少而关键
  foreach ($line in $lines) {
    if ($line -match 'Tests run:' -or
        $line -match 'Failures:' -or
        $line -match 'Errors:'   -or
        $line -match 'Skipped:'  -or
        $line -match 'BUILD SUCCESS' -or
        $line -match 'BUILD FAILURE' -or
        $line -match '^\[ERROR\]' -or
        $line -match '^\[FAILED\]' -or
        $line -match 'surefire-reports' -or
        $line -match 'MojoFailureException') {
      $out.Add($line)
    }
  }

  Set-Content -Path $summaryLog -Value $out -Encoding UTF8
}

Write-Header -path $LogFile

if (-not (Test-Path $App)) {
  "[FAILED] App folder not found: $App" | Out-File -FilePath $LogFile -Encoding UTF8 -Append
  Write-Host "❌ TEST FAIL (app folder missing)"
  Write-Host "FULL LOG   : $LogFile"
  Write-Host "SUMMARY LOG: $SummaryFile"
  if (-not $NoPause) { Read-Host "Press Enter to close" }
  exit 1
}

Push-Location $App

$mvnw = Join-Path $App "mvnw.cmd"
if (-not (Test-Path $mvnw)) { $mvnw = Join-Path $App "mvnw" }
if (-not (Test-Path $mvnw)) {
  Pop-Location
  "[FAILED] mvnw not found" | Out-File -FilePath $LogFile -Encoding UTF8 -Append
  Write-Host "❌ TEST FAIL (mvnw not found)"
  Write-Host "FULL LOG   : $LogFile"
  Write-Host "SUMMARY LOG: $SummaryFile"
  if (-not $NoPause) { Read-Host "Press Enter to close" }
  exit 1
}

"[RUN] mvnw clean test" | Out-File -FilePath $LogFile -Encoding UTF8 -Append

# ✅ 实时滚动输出：
# - 控制台边跑边输出
# - 同时把输出追加写入 LogFile
# - 不做任何“把 warning 当失败”的判断
& $mvnw clean test 2>&1 | Tee-Object -FilePath $LogFile -Append

$exitCode = $LASTEXITCODE
Pop-Location

Build-Summary -fullLog $LogFile -summaryLog $SummaryFile -exitCode $exitCode

Write-Host ""
if ($exitCode -eq 0) {
  Write-Host "✅ TEST PASS (exit code=0)"
} else {
  Write-Host "❌ TEST FAIL (exit code=$exitCode)"
}
Write-Host "FULL LOG   : $LogFile"
Write-Host "SUMMARY LOG: $SummaryFile"

if (-not $NoPause) { Read-Host "Press Enter to close" }
exit $exitCode
