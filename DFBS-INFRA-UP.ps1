# DFBS-INFRA-UP.ps1 - Docker Compose up with clear progress and diagnostics (Win11 + PS 7.x)
param([switch]$NoPause)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

. "$PSScriptRoot\DFBS-UTILS.ps1"

$sw = [System.Diagnostics.Stopwatch]::StartNew()
$root = Resolve-RepoRoot
$logFile = Start-RunLog "dfbs-infra-up"
$composePath = Join-Path $root "infra\docker-compose.yml"
$step = 0
$total = 5

try {
    Write-RunHeader "INFRA UP (docker compose)" $logFile

    # [1/5] Docker available and daemon reachable
    $step++; Write-Step "[$step/$total] Checking Docker..."
    Assert-Docker
    Write-Ok "  [$step/$total] Docker OK"

    # [2/5] Docker Compose available
    $step++; Write-Step "[$step/$total] Checking Docker Compose..."
    Assert-DockerCompose
    Write-Ok "  [$step/$total] Docker Compose OK"

    # [3/5] Compose file exists
    $step++; Write-Step "[$step/$total] Checking compose file..."
    if (-not (Test-Path $composePath)) {
        Write-Fail "Compose file not found: $composePath"
        throw "Compose file missing."
    }
    Write-Ok "  [$step/$total] Compose file OK"

    # [4/5] docker compose up -d
    $step++; Write-Step "[$step/$total] Starting services (docker compose up -d)..."
    $upCmd = "docker compose -f `"$composePath`" up -d"
    Invoke-External -StepName "docker compose up -d" -CommandLine $upCmd -LogFile $logFile -WorkingDirectory $root
    Write-Ok "  [$step/$total] Services started"

    # [5/5] What is running now
    $step++; Write-Step "[$step/$total] What is running now..."
    Invoke-External -StepName "docker compose ps" -CommandLine "docker compose -f `"$composePath`" ps" -LogFile $logFile -WorkingDirectory $root
    Write-Ok "  [$step/$total] Done"

    $sw.Stop()
    Write-Host ""
    Write-Ok ("SUCCESS. Elapsed: {0:N1}s. Log: {1}" -f $sw.Elapsed.TotalSeconds, $logFile)
}
catch {
    $sw.Stop()
    Write-Host ""
    Write-Fail ("FAILED at step $step. Log: {0}" -f $logFile)
    Write-Host $_.Exception.Message
    if ($step -eq 4) {
        Write-Warn "To inspect: run 'docker compose -f `"$composePath`" ps' and 'docker compose -f `"$composePath`" logs <service>'"
    }
    if (-not $NoPause) { Read-Host "Press Enter to close" }
    exit 1
}

if (-not $NoPause) { Read-Host "Press Enter to close" }
exit 0
