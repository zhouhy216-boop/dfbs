# DFBS-APP-RUN.ps1 - SAFE backend run only (no git, no tests)
# - Real-time output to console + logs\
# - Keep window open (Press Enter to close)
param([switch]$NoPause)

$Root   = $PSScriptRoot
$App    = Join-Path $Root "backend\dfbs-app"
$LogDir = Join-Path $Root "logs"
New-Item -ItemType Directory -Force -Path $LogDir | Out-Null
$LogFile = Join-Path $LogDir ("dfbs-app-run_{0}.log" -f (Get-Date -Format "yyyyMMdd_HHmmss"))

function Log([string]$s) {
  $s | Out-File -FilePath $LogFile -Encoding UTF8 -Append
  Write-Host $s
}

try {
  Write-Host "=================================================="
  Write-Host "DFBS APP RUN - Spring Boot (no git, no tests)"
  Write-Host "App : $App"
  Write-Host "Log : $LogFile"
  Write-Host "Tip : Stop with Ctrl+C (or close window)"
  Write-Host "=================================================="
  Write-Host ""

  if (!(Test-Path $App)) { throw "App folder not found: $App" }

  $mvnw = Join-Path $App "mvnw.cmd"
  if (!(Test-Path $mvnw)) { throw "mvnw.cmd not found: $mvnw" }

  # 写入日志头
  "==================================================" | Out-File -FilePath $LogFile -Encoding UTF8
  "DFBS APP RUN - mvnw spring-boot:run" | Out-File -FilePath $LogFile -Encoding UTF8 -Append
  ("Time: {0}" -f (Get-Date)) | Out-File -FilePath $LogFile -Encoding UTF8 -Append
  ("Root: {0}" -f $Root) | Out-File -FilePath $LogFile -Encoding UTF8 -Append
  ("App : {0}" -f $App)  | Out-File -FilePath $LogFile -Encoding UTF8 -Append
  "==================================================" | Out-File -FilePath $LogFile -Encoding UTF8 -Append
  "" | Out-File -FilePath $LogFile -Encoding UTF8 -Append
  "[RUN] mvnw spring-boot:run" | Out-File -FilePath $LogFile -Encoding UTF8 -Append

  # 用 ProcessStartInfo 启动并实时读 stdout/stderr
  $psi = New-Object System.Diagnostics.ProcessStartInfo
  $psi.FileName = "cmd.exe"
  $psi.Arguments = "/c `"$mvnw`" spring-boot:run"
  $psi.WorkingDirectory = $App
  $psi.UseShellExecute = $false
  $psi.RedirectStandardOutput = $true
  $psi.RedirectStandardError  = $true
  $psi.CreateNoWindow = $true

  $p = New-Object System.Diagnostics.Process
  $p.StartInfo = $psi
  [void]$p.Start()

  # 实时打印 + 写日志
  while (-not $p.HasExited) {
    while (-not $p.StandardOutput.EndOfStream) {
      Log $p.StandardOutput.ReadLine()
    }
    while (-not $p.StandardError.EndOfStream) {
      Log $p.StandardError.ReadLine()
    }
    Start-Sleep -Milliseconds 50
  }

  # 读完尾巴
  while (-not $p.StandardOutput.EndOfStream) { Log $p.StandardOutput.ReadLine() }
  while (-not $p.StandardError.EndOfStream)  { Log $p.StandardError.ReadLine() }

  $exitCode = $p.ExitCode
  Log ""
  Log ("[EXIT] spring-boot:run finished (exit code={0})" -f $exitCode)
  Log ("Please open log: {0}" -f $LogFile)

  Write-Host ""
  Write-Host "[STOPPED] Backend process exited (exit code=$exitCode)"
  Write-Host "Open log to see why: $LogFile"

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
