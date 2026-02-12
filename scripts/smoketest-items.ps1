# DICT-260211-001-02.a smoketest - do NOT include token in output
$ErrorActionPreference = "Stop"
$login = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/login" -Method POST -ContentType "application/json" -Body '{"username":"admin","password":""}'
$token = $login.token
$h = @{ Authorization = "Bearer $token"; "Content-Type" = "application/json" }

Write-Host "=== A) Dict type exists ==="
$r = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/admin/dictionary-types?page=0&pageSize=5" -Headers $h -Method GET
Write-Host "HTTP:200 total=$($r.total) typeCode=$($r.items[0].typeCode)"

Write-Host "`n=== B) POST create ROOT item ==="
$bodyB = '{"itemValue":"cash","itemLabel":"Cash","sortOrder":1,"enabled":true,"note":"root","parentId":null}'
try {
    $rB = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/admin/dictionary-types/1/items" -Headers $h -Method POST -Body $bodyB
    Write-Host "HTTP:200 id=$($rB.id) itemValue=$($rB.itemValue)"
} catch {
    Write-Host "HTTP:$($_.Exception.Response.StatusCode.value__) $($_.ErrorDetails.Message)"
}

Write-Host "`n=== C) POST create CHILD item ==="
$rItems = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/admin/dictionary-types/1/items?page=0&pageSize=10" -Headers $h -Method GET
$cashId = ($rItems.items | Where-Object { $_.itemValue -eq 'cash' })[0].id
$bodyC = (@{itemValue="cash_sub";itemLabel="CashChild";sortOrder=2;enabled=$true;parentId=$cashId} | ConvertTo-Json)
try {
    $rC = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/admin/dictionary-types/1/items" -Headers $h -Method POST -Body $bodyC
    Write-Host "HTTP:200 id=$($rC.id) parentId=$($rC.parentId)"
} catch {
    Write-Host "HTTP:$($_.Exception.Response.StatusCode.value__) $($_.ErrorDetails.Message)"
}

Write-Host "`n=== D) Invalid parent (grandchild) => expect 400 ==="
$childItem = $rItems.items | Where-Object { $_.itemValue -eq 'cash_sub' } | Select-Object -First 1
$childId = if ($childItem) { $childItem.id } else { 0 }
$bodyD = (@{itemValue="grandchild";itemLabel="GC";sortOrder=3;enabled=$true;parentId=$childId} | ConvertTo-Json)
try {
    Invoke-RestMethod -Uri "http://localhost:8080/api/v1/admin/dictionary-types/1/items" -Headers $h -Method POST -Body $bodyD | Out-Null
    Write-Host "HTTP:200 (unexpected)"
} catch {
    $err = $_.ErrorDetails.Message | ConvertFrom-Json -ErrorAction SilentlyContinue
    $code = if ($err) { $err.machineCode } else { "N/A" }
    Write-Host "HTTP:$($_.Exception.Response.StatusCode.value__) machineCode=$code"
}

Write-Host "`n=== E) Cross-type parent => expect 400 ==="
$r2 = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/admin/dictionary-types/2/items?page=0&pageSize=10" -Headers $h -Method GET
$parentT2 = 0
if ($r2.items -and $r2.items.Count -gt 0) {
    $parentT2 = $r2.items[0].id
} else {
    $bodyE0 = (@{itemValue="other_type_root";itemLabel="OtherType";sortOrder=1;enabled=$true;parentId=$null} | ConvertTo-Json)
    $rE0 = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/admin/dictionary-types/2/items" -Headers $h -Method POST -Body $bodyE0
    $parentT2 = $rE0.id
}
$bodyE = (@{itemValue="cross_type_item";itemLabel="X";sortOrder=1;enabled=$true;parentId=$parentT2} | ConvertTo-Json)
try {
    Invoke-RestMethod -Uri "http://localhost:8080/api/v1/admin/dictionary-types/1/items" -Headers $h -Method POST -Body $bodyE | Out-Null
    Write-Host "HTTP:200 (unexpected)"
} catch {
    $err = $_.ErrorDetails.Message | ConvertFrom-Json -ErrorAction SilentlyContinue
    Write-Host "HTTP:$($_.Exception.Response.StatusCode.value__) machineCode=$($err.machineCode)"
}

Write-Host "`n=== F) List/filter ==="
$rF = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/admin/dictionary-types/1/items?page=0&pageSize=5" -Headers $h -Method GET
Write-Host "HTTP:200 total=$($rF.total) items=$($rF.items.Count)"

Write-Host "`n=== G) Duplicate item_value => expect 400 ==="
$bodyG = '{"itemValue":"cash","itemLabel":"Dup","sortOrder":9,"enabled":true,"parentId":null}'
try {
    Invoke-RestMethod -Uri "http://localhost:8080/api/v1/admin/dictionary-types/1/items" -Headers $h -Method POST -Body $bodyG | Out-Null
    Write-Host "HTTP:200 (unexpected)"
} catch {
    $err = $_.ErrorDetails.Message | ConvertFrom-Json -ErrorAction SilentlyContinue
    Write-Host "HTTP:$($_.Exception.Response.StatusCode.value__) machineCode=$($err.machineCode)"
}

Write-Host "`n=== H) Enable/disable ==="
$cashItem = ($rF.items | Where-Object { $_.itemValue -eq 'cash' })[0]
$id = $cashItem.id
$orig = $cashItem.enabled
Invoke-RestMethod -Uri "http://localhost:8080/api/v1/admin/dictionary-items/$id/disable" -Headers $h -Method PATCH | Out-Null
$rAfter = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/admin/dictionary-types/1/items?page=0&pageSize=10" -Headers $h -Method GET
$after = ($rAfter.items | Where-Object { $_.id -eq $id })[0]
Write-Host "HTTP:200 enabled: $orig -> $($after.enabled)"
Invoke-RestMethod -Uri "http://localhost:8080/api/v1/admin/dictionary-items/$id/enable" -Headers $h -Method PATCH | Out-Null
Write-Host "HTTP:200 re-enabled"
