# Simple Week 34 Check - compare with known working Sunday timestamp

Write-Host "=== COMPARING MONDAY vs SUNDAY TIMESTAMPS ===" -ForegroundColor Yellow

$mondayTimestamp = 1755475200000  # Week 34 Monday (not working)
$sundayTimestamp = 1756030908539  # Week 34 Sunday (working from our tests)

Write-Host "Testing Monday timestamp (should work): $mondayTimestamp" -ForegroundColor Cyan
try {
    $mondayResult = Invoke-RestMethod -Uri "http://localhost:8080/api/books/statistics/details?period=week&date=$mondayTimestamp&limit=5" -Method GET
    Write-Host "Monday result - Count: $($mondayResult.data.Count)" -ForegroundColor $(if($mondayResult.data.Count -gt 0) { "Green" } else { "Red" })
} catch {
    Write-Host "Monday failed: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`nTesting Sunday timestamp (known working): $sundayTimestamp" -ForegroundColor Cyan  
try {
    $sundayResult = Invoke-RestMethod -Uri "http://localhost:8080/api/books/statistics/details?period=week&date=$sundayTimestamp&limit=5" -Method GET
    Write-Host "Sunday result - Count: $($sundayResult.data.Count)" -ForegroundColor $(if($sundayResult.data.Count -gt 0) { "Green" } else { "Red" })
    if ($sundayResult.data.Count -gt 0) {
        Write-Host "Books found on Sunday:" -ForegroundColor Green
        $sundayResult.data | ForEach-Object { Write-Host "  - $($_.title): $($_.totalQuantity)" }
    }
} catch {
    Write-Host "Sunday failed: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`nBoth timestamps should map to the same week (2025-08-18 to 2025-08-24)" -ForegroundColor Yellow
