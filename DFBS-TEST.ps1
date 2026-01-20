# DFBS-TEST.ps1 - run backend tests only (SAFE)
# - NO git pull
# - NO docker compose
# - NO app start
# - Always write FULL output log to logs\
# - Keep window open (Press Enter to close)
param([switch]$NoPause)

$Root   = $PSScriptRoot
$App    = Join-Path $Root "backend\dfbs-app"
$LogDir = Join-Path $Root "logs"
New-Item -ItemType Directory -Force -Path $LogDir | Out-Null

$Ts = Get-Date -Format "yyyyMMdd_HHmmss"
$LogFile = Join-Path $LogDir ("dfbs-test_{0}.log" -f $Ts)

function Write-Header {
  "==================================================" | Out-File -FilePath $LogFile -Encoding UTF8
  "DFBS TEST - mvnw clean test" | Out-File -FilePath $LogFile -Encoding UTF8 -Append
  ("Time: {0}" -f (Get-Date)) | Out-File -FilePath $LogFile -Encoding UTF8 -Append
  ("Root: {0}" -f $Root) | Out-File -FilePath $LogFile -Encoding UTF8 -Append
  ("App : {0}" -f $App)  | Out-File -FilePath $LogFile -Encoding UTF8 -Append
  "==================================================" | Out-File -FilePath $LogFile -Encoding UTF8 -Append
  "" | Out-File -FilePath $LogFile -Encoding UTF8 -Append
  "[RUN] mvnw clean test" | Out-File -FilePath $LogFile -Encoding UTF8 -Append
}

try {
  Write-Host "=================================================="
  Write-Host "DFBS TEST - mvnw clean test"
  Write-Host "Time: $(Get-Date)"
  Write-Host "Root: $Root"
  Write-Host "App : $App"
  Write-Host "Log : $LogFile"
  Write-Host "=================================================="
  Write-Host ""

  if (!(Test-Path $App)) { throw "App folder not found: $App" }

  $mvnw = Join-Path $App "mvnw.cmd"
  if (!(Test-Path $mvnw)) { throw "mvnw.cmd not found: $mvnw" }

  Write-Header

  # 用 Start-Process 运行，确保：
  # - 输出一定写到日志（stdout+stderr）
  # - 退出码一定可靠
  $psi = New-Object System.Diagnostics.ProcessStartInfo
  $psi.FileName = "cmd.exe"
  $psi.Arguments = "/c `"$mvnw`" clean test"
  $psi.WorkingDirectory = $App
  $psi.UseShellExecute = $false
  $psi.RedirectStandardOutput = $true
  $psi.RedirectStandardError  = $true
  $psi.CreateNoWindow = $true

  $p = New-Object System.Diagnostics.Process
  $p.StartInfo = $psi

  [void]$p.Start()

  # 实时把输出写入日志，同时打印到控制台
  while (-not $p.HasExited) {
    while (-not $p.StandardOutput.EndOfStream) {
      $line = $p.StandardOutput.ReadLine()
      $line | Out-File -FilePath $LogFile -Encoding UTF8 -Append
      Write-Host $line
    }
    while (-not $p.StandardError.EndOfStream) {
      $line = $p.StandardError.ReadLine()
      $line | Out-File -FilePath $LogFile -Encoding UTF8 -Append
      Write-Host $line
    }
    Start-Sleep -Milliseconds 50
  }

  # 把剩余的尾巴读完
  while (-not $p.StandardOutput.EndOfStream) {
    $line = $p.StandardOutput.ReadLine()
    $line | Out-File -FilePath $LogFile -Encoding UTF8 -Append
    Write-Host $line
  }
  while (-not $p.StandardError.EndOfStream) {
    $line = $p.StandardError.ReadLine()
    $line | Out-File -FilePath $LogFile -Encoding UTF8 -Append
    Write-Host $line
  }

  $exitCode = $p.ExitCode

  Write-Host ""
  if ($exitCode -eq 0) {
    "[OK] TESTS PASS" | Out-File -FilePath $LogFile -Encoding UTF8 -Append
    Write-Host "[OK] TESTS PASS"
  } else {
    ("[FAILED] mvnw clean test (exit code={0})" -f $exitCode) | Out-File -FilePath $LogFile -Encoding UTF8 -Append
    ("Please open log: {0}" -f $LogFile) | Out-File -FilePath $LogFile -Encoding UTF8 -Append
    Write-Host ("[FAILED] mvnw clean test (exit code={0})" -f $exitCode)
    Write-Host "Please open log: $LogFile"
  }

  if (-not $NoPause) { Read-Host "Press Enter to close" }

  exit $exitCode
}
catch {
  "" | Out-File -FilePath $LogFile -Encoding UTF8 -Append
  ("[FAILED] {0}" -f $_.Exception.Message) | Out-File -FilePath $LogFile -Encoding UTF8 -Append

  Write-Host ""
  Write-Host "[FAILED] $($_.Exception.Message)"
  Write-Host "Please open log: $LogFile"

  if (-not $NoPause) { Read-Host "Press Enter to close" }
  exit 1
}
