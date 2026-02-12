# Thin wrapper: POST /api/v1/admin/test-data-cleaner/preview
# Env: ADMIN_BEARER_TOKEN (required), TDCLEAN_BASE_URL (default http://localhost:8080)
# Args: --moduleIds <comma-separated>
# Exit: 0=success, 1=failed, 3=safety/400, 4=401/403. Always prints raw JSON to stdout.

param(
    [Parameter(Mandatory = $true)]
    [object]$moduleIds
)

$Base = if ($env:TDCLEAN_BASE_URL) { $env:TDCLEAN_BASE_URL } else { "http://localhost:8080" }
if (-not $env:ADMIN_BEARER_TOKEN) {
    Write-Error '{"error":"ADMIN_BEARER_TOKEN is required"}'
    exit 1
}

# Normalize to string[]: string => split by comma; array => use as-is (trim/filter empty)
$raw = if ($moduleIds -is [string]) { $moduleIds -split ',' } else { @($moduleIds) }
$ids = $raw | ForEach-Object { $_.ToString().Trim() } | Where-Object { $_ }
if (-not $ids -or $ids.Count -eq 0) {
    '{"error":"moduleIds is required"}' | Write-Output
    exit 3
}

# Build JSON with moduleIds always as array (ConvertTo-Json serializes single-element array as scalar)
$idsJson = ($ids | ForEach-Object { '"' + ($_ -replace '\\', '\\\\' -replace '"', '\"') + '"' }) -join ','
$body = '{"moduleIds":[' + $idsJson + '],"includeAttachments":false}'

$statusCode = 0
try {
    $r = Invoke-WebRequest -Uri "$Base/api/v1/admin/test-data-cleaner/preview" -Method POST `
        -Headers @{ "Authorization" = "Bearer $($env:ADMIN_BEARER_TOKEN)"; "Content-Type" = "application/json" } `
        -Body $body -UseBasicParsing
    $raw = $r.Content
    $statusCode = [int]$r.StatusCode
} catch {
    if ($_.Exception.Response) {
        $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
        $raw = $reader.ReadToEnd()
        $reader.Close()
        $statusCode = [int]$_.Exception.Response.StatusCode
    } else {
        $raw = '{"error":"' + ($_.Exception.Message -replace '"', '\"') + '"}'
    }
}

$raw | Write-Output

if ($statusCode -eq 401 -or $statusCode -eq 403) { exit 4 }
if ($statusCode -eq 400) {
    try {
        $j = $raw | ConvertFrom-Json
        $code = $j.machineCode
        if ($code -eq "RESET_CONFIRM_REQUIRED" -or $code -eq "ATTACHMENTS_NOT_SUPPORTED_YET") { exit 3 }
    } catch { }
    exit 1
}
if ($statusCode -eq 200) { exit 0 }
exit 1
