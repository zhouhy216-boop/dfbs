# DFBS-UTILS.ps1 - Reusable logging/exec helpers for DFBS root scripts.
# Dot-source from repo root: . "$PSScriptRoot\DFBS-UTILS.ps1"
# Expects to live at repo root; script that dot-sources must be at repo root (so $PSScriptRoot = repo root).

# --- Configurable: dirs/files to exclude from "git add" new files (avoids huge/build/deps) ---
$script:DFBS_AddExcludeDirs = @(
    'node_modules', 'dist', 'build', '.next', '.nuxt', '.venv', 'venv',
    '.idea', '.vs', '.cache', 'coverage', 'tmp', 'temp', 'out', 'target', 'bin', 'obj'
)
$script:DFBS_AddExcludeFiles = @('.DS_Store', 'Thumbs.db')
$script:DFBS_HighRiskUntrackedThreshold = 200

function Resolve-RepoRoot {
    return $PSScriptRoot
}

function Write-Section { param([string]$Title)
    Write-Host ""
    Write-Host $Title -ForegroundColor Cyan
    Write-Host ("-" * [Math]::Min(60, $Title.Length))
}

function Write-Step { param([string]$Message)
    Write-Host $Message
}

function Write-Ok { param([string]$Message)
    Write-Host $Message -ForegroundColor Green
}

function Write-Warn { param([string]$Message)
    Write-Host $Message -ForegroundColor Yellow
}

function Write-Fail { param([string]$Message)
    Write-Host $Message -ForegroundColor Red
}

function Get-LogDir {
    $root = Resolve-RepoRoot
    $dir = Join-Path $root ".logs"
    if (-not (Test-Path $dir)) { New-Item -ItemType Directory -Force -Path $dir | Out-Null }
    return $dir
}

function Start-RunLog {
    param([string]$ScriptName)
    $logDir = Get-LogDir
    $ts = Get-Date -Format "yyyyMMdd_HHmmss"
    $logFile = Join-Path $logDir ("{0}_{1}.log" -f $ScriptName, $ts)
    return $logFile
}

function Assert-Git {
    $err = $null
    try { $null = & git --version 2>&1 } catch { $err = $_ }
    if ($LASTEXITCODE -ne 0 -or $err) {
        Write-Fail "Git not found or not runnable. Install Git for Windows and add it to PATH."
        throw "Git is required."
    }
}

function Assert-Docker {
    $err = $null
    try { $null = & docker info 2>&1 } catch { $err = $_ }
    if ($LASTEXITCODE -ne 0 -or $err) {
        Write-Fail "Docker not found or Docker daemon not running. Start Docker Desktop and try again."
        throw "Docker is required."
    }
}

function Assert-DockerCompose {
    $err = $null
    try { $null = & docker compose version 2>&1 } catch { $err = $_ }
    if ($LASTEXITCODE -eq 0) { return }
    try { $null = & docker-compose --version 2>&1 } catch { $err = $_ }
    if ($LASTEXITCODE -ne 0 -or $err) {
        Write-Fail "Docker Compose not available. Use Docker Desktop (includes Compose) or install docker-compose."
        throw "Docker Compose is required."
    }
}

function Get-ComposeCommand {
    $root = Resolve-RepoRoot
    $composeFile = Join-Path $root "infra\docker-compose.yml"
    if (-not (Test-Path $composeFile)) { return $null }
    # Prefer "docker compose" (plugin); fallback to "docker-compose"
    $out = docker compose version 2>&1
    if ($LASTEXITCODE -eq 0) { return @("docker", "compose", "-f", "`"$composeFile`"") }
    $out = docker-compose --version 2>&1
    if ($LASTEXITCODE -eq 0) { return @("docker-compose", "-f", "`"$composeFile`"") }
    return $null
}

function Get-RepoStatusSummary {
    param([string]$RepoRoot = (Resolve-RepoRoot))
    $porcelain = & git -C $RepoRoot status --porcelain 2>&1 | Out-String
    $lines = @($porcelain -split "`n" | Where-Object { $_.Trim() -ne "" })
    $trackedCount = 0
    $untrackedPaths = [System.Collections.ArrayList]@()
    foreach ($line in $lines) {
        if ($line.Length -lt 4) { continue }
        $xy = $line.Substring(0, 2)
        $path = $line.Substring(3).Trim()
        if ($xy -eq '??') {
            [void]$untrackedPaths.Add($path)
        } else {
            $trackedCount++
        }
    }
    # Top-level untracked folder breakdown (first path segment)
    $folderCounts = @{}
    foreach ($p in $untrackedPaths) {
        $seg = ($p -replace '\\', '/').Split('/')[0]
        if (-not $seg) { $seg = '.' }
        $folderCounts[$seg] = 1 + (0 + $folderCounts[$seg])
    }
    $topFolders = $folderCounts.GetEnumerator() | Sort-Object -Property Value -Descending | Select-Object -First 10
    $topList = @($topFolders | ForEach-Object { [pscustomobject]@{ Name = $_.Key; Count = $_.Value } })
    # High-risk: many untracked or known huge dir in top folders
    $excludeDirs = $script:DFBS_AddExcludeDirs
    $threshold = $script:DFBS_HighRiskUntrackedThreshold
    $highRisk = ($untrackedPaths.Count -gt $threshold)
    if (-not $highRisk) {
        foreach ($t in $topList) {
            if ($excludeDirs -contains $t.Name) { $highRisk = $true; break }
        }
    }
    return [pscustomobject]@{
        TrackedCount       = $trackedCount
        UntrackedCount    = $untrackedPaths.Count
        TopUntrackedFolders = $topList
        HighRisk          = $highRisk
    }
}

function Get-SafeUntrackedPathListFile {
    param(
        [string]$RepoRoot = (Resolve-RepoRoot),
        [string]$OutputFile = ""
    )
    $others = & git -C $RepoRoot ls-files --others --exclude-standard 2>&1
    if (-not $others) { return $null }
    $excludeDirs = $script:DFBS_AddExcludeDirs
    $excludeFiles = $script:DFBS_AddExcludeFiles
    $safe = [System.Collections.ArrayList]@()
    foreach ($line in $others) {
        $path = ($line -replace '\\', '/').Trim()
        if (-not $path) { continue }
        $top = $path.Split('/')[0]
        if ($excludeDirs -contains $top) { continue }
        if ($excludeFiles -contains $path -or $excludeFiles -contains $top) { continue }
        [void]$safe.Add($path)
    }
    if ($safe.Count -eq 0) { return $null }
    if (-not $OutputFile) {
        $logDir = Get-LogDir
        $OutputFile = Join-Path $logDir ("safe-add-paths_{0}.txt" -f (Get-Date -Format "yyyyMMdd_HHmmss"))
    }
    $safe | Out-File -FilePath $OutputFile -Encoding utf8
    return $OutputFile
}

function Get-GitAddFailureReasons {
    param([switch]$IsTimeout)
    $reasons = [System.Collections.ArrayList]@()
    if ($IsTimeout) {
        [void]$reasons.Add("Command ran too long (timeout). Git add can hang on huge or slow paths.")
    }
    [void]$reasons.Add("Windows Defender or antivirus scanning many files (especially node_modules/build).")
    [void]$reasons.Add("Repo or working tree on a sync drive (OneDrive, Dropbox, etc.) causing slow I/O.")
    [void]$reasons.Add("Very large untracked folders (e.g. node_modules, dist) being added.")
    [void]$reasons.Add("Disk or network drive latency; try running from a local non-synced folder.")
    return $reasons
}

function Write-FailureCard {
    param(
        [Parameter(Mandatory=$true)][string]$StepName,
        [Parameter(Mandatory=$true)][string]$CommandLine,
        [int]$ElapsedSec = 0,
        [bool]$IsTimeout = $false,
        [Parameter(Mandatory=$true)][string]$LogFile,
        [hashtable]$StatusCounts = @{},
        [string[]]$SuggestedCommands = @()
    )
    Write-Host ""
    Write-Fail "--- Failure summary (no need to open log) ---"
    Write-Fail "Step:    $StepName"
    Write-Fail "Command: $CommandLine"
    if ($ElapsedSec -gt 0) { Write-Fail "Elapsed: $ElapsedSec s" }
    if ($IsTimeout) { Write-Fail "Result:  Timed out (command was stopped)." }
    if ($StatusCounts -and $StatusCounts.Count -gt 0) {
        Write-Host ""
        if ($StatusCounts.ContainsKey('Tracked')) { Write-Host "  Tracked changes: $($StatusCounts.Tracked)" }
        if ($StatusCounts.ContainsKey('Untracked')) { Write-Host "  Untracked files:  $($StatusCounts.Untracked)" }
    }
    Write-Host ""
    Write-Warn "Likely causes:"
    $reasons = Get-GitAddFailureReasons -IsTimeout $IsTimeout
    foreach ($r in $reasons) { Write-Host "  * $r" }
    Write-Host ""
    Write-Warn "What to do now (copy-paste in repo root):"
    if ($SuggestedCommands.Count -gt 0) {
        foreach ($c in $SuggestedCommands) { Write-Host "  $c" }
    } else {
        Write-Host "  git status"
        Write-Host "  git add -u"
        Write-Host "  git commit -m ""sync"""
        Write-Host "  git push"
    }
    Write-Host ""
    Write-Host "Full output was saved to: $LogFile" -ForegroundColor DarkGray
    Write-Fail "---"
    Write-Host ""
}

function Invoke-External {
    param(
        [Parameter(Mandatory=$true)][string]$StepName,
        [Parameter(Mandatory=$true)][string]$CommandLine,
        [Parameter(Mandatory=$true)][string]$LogFile,
        [string]$WorkingDirectory = (Resolve-RepoRoot),
        [int]$TimeoutSec = 0,
        [int]$HeartbeatSec = 0,
        [hashtable]$StatusCounts = $null,
        [switch]$InteractiveConsole
    )
    $displayCmd = if ($CommandLine.Length -gt 70) { $CommandLine.Substring(0, 67) + "..." } else { $CommandLine }
    Write-Host "  Running: $displayCmd"

    $useTimeout = $TimeoutSec -gt 0
    $useHeartbeat = $HeartbeatSec -gt 0

    if ($InteractiveConsole) {
        # Run in same console (no redirection) so credential/passphrase prompts are visible.
        # Parse "exe arg1 arg2" -> FilePath exe, ArgumentList array
        $idx = $CommandLine.IndexOf(' ')
        if ($idx -lt 0) { $exe = $CommandLine; $argArray = @() } else { $exe = $CommandLine.Substring(0, $idx).Trim(); $argArray = @($CommandLine.Substring($idx + 1).Trim() -split ' ') }
        try {
            if ($argArray.Count -gt 0) {
                $proc = Start-Process -FilePath $exe -ArgumentList $argArray -WorkingDirectory $WorkingDirectory -PassThru -NoNewWindow
            } else {
                $proc = Start-Process -FilePath $exe -WorkingDirectory $WorkingDirectory -PassThru -NoNewWindow
            }
        } catch {
            Write-Fail "Failed to start command: $($_.Exception.Message)"
            throw "Invoke-External: start failed."
        }
        try { Start-Transcript -Path $LogFile -Append -ErrorAction SilentlyContinue } catch { }
        $elapsed = 0
        $lastHeartbeat = 0
        $credentialHintShown = $false
        while (-not $proc.HasExited) {
            Start-Sleep -Seconds 1
            $elapsed++
            if ($elapsed -ge 30 -and -not $credentialHintShown) {
                $credentialHintShown = $true
                Write-Host "  If this is waiting for credentials/passphrase, you should see a prompt in this window. If not, press Ctrl+C and run: git push --progress manually once to authenticate." -ForegroundColor DarkYellow
            }
            if ($useHeartbeat -and $HeartbeatSec -gt 0 -and ($elapsed - $lastHeartbeat) -ge $HeartbeatSec) {
                Write-Host "  Still working… $StepName … elapsed $elapsed s (Ctrl+C to abort)" -ForegroundColor Gray
                $lastHeartbeat = $elapsed
            }
            if ($useTimeout -and $TimeoutSec -gt 0 -and $elapsed -ge $TimeoutSec) {
                try { Stop-Transcript -ErrorAction SilentlyContinue } catch { }
                try { Stop-Process -Id $proc.Id -Force -ErrorAction SilentlyContinue } catch { }
                $proc.WaitForExit(5000) | Out-Null
                $counts = if ($StatusCounts) { $StatusCounts } else { @{} }
                Write-FailureCard -StepName $StepName -CommandLine $CommandLine -ElapsedSec $TimeoutSec -IsTimeout $true -LogFile $LogFile -StatusCounts $counts -SuggestedCommands @(
                    "git push --progress",
                    "git push -u origin <branch>"
                )
                throw ("Command timed out after " + $TimeoutSec + "s: " + $StepName)
            }
        }
        try { Stop-Transcript -ErrorAction SilentlyContinue } catch { }
        $exitCode = $proc.ExitCode
        if ($exitCode -ne 0) {
            Write-Host ""
            Write-Fail "Step failed: $StepName (exit code $exitCode)"
            $hint = Get-FailureHint -CommandLine $CommandLine -Output "" -ExitCode $exitCode
            if ($hint) { Write-Warn "What to try:"; Write-Host $hint }
            $counts = if ($StatusCounts) { $StatusCounts } else { @{} }
            Write-FailureCard -StepName $StepName -CommandLine $CommandLine -ElapsedSec $elapsed -IsTimeout $false -LogFile $LogFile -StatusCounts $counts -SuggestedCommands @(
                "git push --progress",
                "git push -u origin <branch>"
            )
            throw ("Command failed with exit code " + $exitCode + " — " + $StepName)
        }
        return
    }

    # Non-interactive: redirect stdout/stderr to temp files
    $logDir = Get-LogDir
    $procId = [System.Diagnostics.Process]::GetCurrentProcess().Id
    $ts = [DateTimeOffset]::UtcNow.ToUnixTimeMilliseconds()
    $outFile = Join-Path $logDir ("_out_$procId`_$ts.txt")
    $errFile = Join-Path $logDir ("_err_$procId`_$ts.txt")

    try {
        $proc = Start-Process -FilePath "cmd.exe" -ArgumentList "/c", $CommandLine -WorkingDirectory $WorkingDirectory -PassThru -NoNewWindow -RedirectStandardOutput $outFile -RedirectStandardError $errFile
    } catch {
        Write-Fail "Failed to start command: $($_.Exception.Message)"
        throw "Invoke-External: start failed."
    }

    $elapsed = 0
    $lastHeartbeat = 0
    if ($useTimeout -or $useHeartbeat) {
        while (-not $proc.HasExited) {
            Start-Sleep -Seconds 1
            $elapsed++
            if ($useHeartbeat -and $HeartbeatSec -gt 0 -and ($elapsed - $lastHeartbeat) -ge $HeartbeatSec) {
                Write-Host "  Still working… $StepName … elapsed $elapsed s (Ctrl+C to abort)" -ForegroundColor Gray
                $lastHeartbeat = $elapsed
            }
            if ($useTimeout -and $TimeoutSec -gt 0 -and $elapsed -ge $TimeoutSec) {
                try { Stop-Process -Id $proc.Id -Force -ErrorAction SilentlyContinue } catch { }
                $proc.WaitForExit(5000) | Out-Null
                $elapsed = $TimeoutSec
                $all = ""
                try {
                    if (Test-Path -LiteralPath $outFile) { $all = Get-Content -LiteralPath $outFile -Raw -ErrorAction SilentlyContinue }
                    if (Test-Path -LiteralPath $errFile) { $err = Get-Content -LiteralPath $errFile -Raw -ErrorAction SilentlyContinue; if ($err) { $all += "`n" + $err } }
                } catch { }
                try { $all | Out-File -FilePath $LogFile -Encoding UTF8 -Append } catch { }
                $counts = if ($StatusCounts) { $StatusCounts } else { @{} }
                Write-FailureCard -StepName $StepName -CommandLine $CommandLine -ElapsedSec $elapsed -IsTimeout $true -LogFile $LogFile -StatusCounts $counts -SuggestedCommands @(
                    "git add -u",
                    "git commit -m ""sync""",
                    "git push"
                )
                try { Remove-Item -LiteralPath $outFile, $errFile -Force -ErrorAction SilentlyContinue } catch { }
                throw "Command timed out after ${elapsed}s: $StepName"
            }
        }
    } else {
        $proc.WaitForExit()
    }

    $all = ""
    try {
        if (Test-Path -LiteralPath $outFile) { $all = Get-Content -LiteralPath $outFile -Raw -ErrorAction SilentlyContinue }
        if (Test-Path -LiteralPath $errFile) { $err = Get-Content -LiteralPath $errFile -Raw -ErrorAction SilentlyContinue; if ($err) { $all += "`n" + $err } }
    } catch { }
    try { $all | Out-File -FilePath $LogFile -Encoding UTF8 -Append } catch { }
    try { Remove-Item -LiteralPath $outFile, $errFile -Force -ErrorAction SilentlyContinue } catch { }

    $exitCode = $proc.ExitCode
    if ($exitCode -ne 0) {
        $lines = @(($all -split "`n" | Where-Object { $_.Trim() -ne "" }))
        $tail = if ($lines.Count -gt 40) { $lines[-40..-1] } else { $lines }
        Write-Host ""
        Write-Fail "Step failed: $StepName (exit code $exitCode)"
        Write-Host "Last lines:" -ForegroundColor Yellow
        Write-Host ($tail -join "`n")
        $hint = Get-FailureHint -CommandLine $CommandLine -Output $all -ExitCode $exitCode
        if ($hint) { Write-Warn "What to try:"; Write-Host $hint }
        $counts = if ($StatusCounts) { $StatusCounts } else { @{} }
        Write-FailureCard -StepName $StepName -CommandLine $CommandLine -ElapsedSec $elapsed -IsTimeout $false -LogFile $LogFile -StatusCounts $counts -SuggestedCommands @(
            "git add -u",
            "git commit -m ""sync""",
            "git push"
        )
        throw ("Command failed with exit code " + $exitCode + " — " + $StepName)
    }
}

function Get-FailureHint {
    param([string]$CommandLine, [string]$Output, [int]$ExitCode)
    $o = $Output + " " + $CommandLine
    if ($o -match "git.*not found|'git' is not recognized|command not found.*git") {
        return "Git is not installed or not on PATH. Install Git for Windows and ensure it is in your PATH."
    }
    if ($o -match "merge conflict|CONFLICT|Automatic merge failed") {
        return "Merge conflict: Open the listed files, resolve conflict markers, then run: git add . ; git commit -m 'resolve merge' ; then rerun this script."
    }
    if ($o -match "docker.*not found|'docker' is not recognized|Cannot connect to the Docker daemon") {
        return "Docker is not installed, or the Docker daemon is not running. Start Docker Desktop (or the Docker service) and try again."
    }
    if ($o -match "port is already allocated|address already in use|Bind for .* failed") {
        return "A port is already in use. Check what is using it (e.g. another container or app), or stop the other service."
    }
    if ($o -match "denied|Permission denied|Authentication failed|Could not read from remote") {
        return "Authentication failed. Check your git credentials (e.g. SSH key or credential manager) and remote access rights."
    }
    if ($o -match "no upstream branch|push.*no pushable|set-upstream") {
        return "Branch has no upstream. Run: git push -u origin <branch-name> (use your branch name)."
    }
    if ($o -match "rejected.*non-fast-forward|Updates were rejected") {
        return "Remote has new commits. Pull first (e.g. run DFBS-GIT-PULL.ps1), then push again."
    }
    if ($o -match "compose.*not found|docker-compose.*not found") {
        return "Docker Compose is not available. Install Docker Desktop (includes Compose) or install docker-compose separately."
    }
    return $null
}

function Write-RunHeader {
    param(
        [string]$ScriptTitle,
        [string]$LogFile
    )
    $root = Resolve-RepoRoot
    $psVer = $PSVersionTable.PSVersion.ToString()
    $ts = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
    $branch = ""
    $dirty = ""
    if (Test-Path (Join-Path $root ".git")) {
        Push-Location $root
        try {
            $branch = (git rev-parse --abbrev-ref HEAD 2>$null)
            $status = (git status --porcelain 2>$null)
            $dirty = if ($status) { " (dirty)" } else { " (clean)" }
        } finally { Pop-Location }
    }
    Write-Section "DFBS — $ScriptTitle"
    Write-Host "Repo root : $root"
    Write-Host "PowerShell: $psVer"
    Write-Host "Time      : $ts"
    if ($branch) { Write-Host "Git branch: $branch$dirty" }
    Write-Host "Log file  : $LogFile"
}
