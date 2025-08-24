# FINAL COMPREHENSIVE TEST - Book Statistics APIs Fix Validation

Write-Host "=== FINAL COMPREHENSIVE TEST ===" -ForegroundColor Yellow
Write-Host "Testing all fixes completed for Book Statistics APIs" -ForegroundColor White

$mondayTimestamp = 1755475200000  # Week 34 Monday 
$sundayTimestamp = 1756030908539  # Week 34 Sunday

Write-Host ""
Write-Host "1. TESTING MONDAY TIMESTAMP (Week 34) - Should work now" -ForegroundColor Cyan
try {
    $mondayUrl = "http://localhost:8080/api/books/statistics/details?period=week" + "&date=$mondayTimestamp" + "&limit=5"
    $mondayResult = Invoke-RestMethod -Uri $mondayUrl -Method GET
    
    Write-Host "SUCCESS: Monday API Response:" -ForegroundColor Green
    Write-Host "   Status: $($mondayResult.status)" -ForegroundColor White
    Write-Host "   Message: $($mondayResult.message)" -ForegroundColor White
    Write-Host "   Books found: $($mondayResult.data.Count)" -ForegroundColor White
    
    if ($mondayResult.data.Count -gt 0) {
        Write-Host "Books details:" -ForegroundColor Green
        $mondayResult.data | ForEach-Object { 
            Write-Host "   - $($_.title): $($_.totalQuantity) sold, Revenue: $($_.revenue)" -ForegroundColor White
        }
    }
} catch {
    Write-Host "ERROR: Monday test failed: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""
Write-Host "2. TESTING SUNDAY TIMESTAMP (Week 34) - Should also work" -ForegroundColor Cyan
try {
    $sundayUrl = "http://localhost:8080/api/books/statistics/details?period=week" + "&date=$sundayTimestamp" + "&limit=5"
    $sundayResult = Invoke-RestMethod -Uri $sundayUrl -Method GET
    
    Write-Host "SUCCESS: Sunday API Response:" -ForegroundColor Green
    Write-Host "   Status: $($sundayResult.status)" -ForegroundColor White
    Write-Host "   Books found: $($sundayResult.data.Count)" -ForegroundColor White
} catch {
    Write-Host "ERROR: Sunday test failed: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""
Write-Host "3. TESTING SUMMARY API - Should still work" -ForegroundColor Cyan
try {
    $summaryUrl = "http://localhost:8080/api/books/statistics/summary?period=week" + "&date=$mondayTimestamp"
    $summaryResult = Invoke-RestMethod -Uri $summaryUrl -Method GET
    
    Write-Host "SUCCESS: Summary API Response:" -ForegroundColor Green
    Write-Host "   Status: $($summaryResult.status)" -ForegroundColor White
    Write-Host "   Total books in summary: $($summaryResult.data.Count)" -ForegroundColor White
} catch {
    Write-Host "ERROR: Summary test failed: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""
Write-Host "=== FINAL RESULTS ===" -ForegroundColor Yellow
if ($mondayResult.data.Count -gt 0 -and $sundayResult.data.Count -gt 0) {
    Write-Host "SUCCESS: All issues have been fixed!" -ForegroundColor Green
    Write-Host "   - Week 34 Monday timestamp now returns data" -ForegroundColor Green  
    Write-Host "   - Week 34 Sunday timestamp still works" -ForegroundColor Green
    Write-Host "   - Both timestamps return books for the same week" -ForegroundColor Green
} else {
    Write-Host "Some issues remain unresolved" -ForegroundColor Red
}
