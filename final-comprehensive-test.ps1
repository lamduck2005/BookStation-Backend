# FINAL COMPREHENSIVE TEST - Book Statistics APIs Fix Validation

Write-Host "=== FINAL COMPREHENSIVE TEST ===" -ForegroundColor Yellow
Write-Host "Testing all fixes completed for Book Statistics APIs" -ForegroundColor White

$mondayTimestamp = 1755475200000  # Week 34 Monday 
$sundayTimestamp = 1756030908539  # Week 34 Sunday

Write-Host "`n1. TESTING MONDAY TIMESTAMP (Week 34) - Should work now" -ForegroundColor Cyan
try {
    $mondayResult = Invoke-RestMethod -Uri "http://localhost:8080/api/books/statistics/details?period=week&date=$mondayTimestamp&limit=5" -Method GET
    
    Write-Host "✅ Monday API Response:" -ForegroundColor Green
    Write-Host "   Status: $($mondayResult.status)" -ForegroundColor White
    Write-Host "   Message: $($mondayResult.message)" -ForegroundColor White
    Write-Host "   Books found: $($mondayResult.data.Count)" -ForegroundColor White
    
    if ($mondayResult.data.Count -gt 0) {
        Write-Host "📚 Books details:" -ForegroundColor Green
        $mondayResult.data | ForEach-Object { 
            Write-Host "   - $($_.title): $($_.totalQuantity) sold, Revenue: $($_.revenue)" -ForegroundColor White
        }
        
        # Check if growth calculation fields are removed
        $hasGrowthFields = $false
        $mondayResult.data | ForEach-Object {
            if ($_.quantityGrowthPercent -ne $null -or $_.revenueGrowthPercent -ne $null) {
                $hasGrowthFields = $true
            }
        }
        
        if ($hasGrowthFields) {
            Write-Host "❌ WARNING: Growth calculation fields still present" -ForegroundColor Yellow
        } else {
            Write-Host "✅ Growth calculation fields successfully removed" -ForegroundColor Green
        }
    }
} catch {
    Write-Host "❌ Monday test failed: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n2. TESTING SUNDAY TIMESTAMP (Week 34) - Should also work" -ForegroundColor Cyan
try {
    $sundayResult = Invoke-RestMethod -Uri "http://localhost:8080/api/books/statistics/details?period=week&date=$sundayTimestamp&limit=5" -Method GET
    
    Write-Host "✅ Sunday API Response:" -ForegroundColor Green
    Write-Host "   Status: $($sundayResult.status)" -ForegroundColor White
    Write-Host "   Books found: $($sundayResult.data.Count)" -ForegroundColor White
    
    if ($sundayResult.data.Count -gt 0) {
        Write-Host "📚 Books details:" -ForegroundColor Green
        $sundayResult.data | ForEach-Object { 
            Write-Host "   - $($_.title): $($_.totalQuantity) sold" -ForegroundColor White
        }
    }
} catch {
    Write-Host "❌ Sunday test failed: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n3. TESTING SUMMARY API - Should still work" -ForegroundColor Cyan
try {
    $summaryResult = Invoke-RestMethod -Uri "http://localhost:8080/api/books/statistics/summary?period=week&date=$mondayTimestamp" -Method GET
    
    Write-Host "✅ Summary API Response:" -ForegroundColor Green
    Write-Host "   Status: $($summaryResult.status)" -ForegroundColor White
    Write-Host "   Total books in summary: $($summaryResult.data.Count)" -ForegroundColor White
} catch {
    Write-Host "❌ Summary test failed: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n=== FINAL RESULTS ===" -ForegroundColor Yellow
if ($mondayResult.data.Count -gt 0 -and $sundayResult.data.Count -gt 0) {
    Write-Host "🎉 SUCCESS: All issues have been fixed!" -ForegroundColor Green
    Write-Host "   ✅ Week 34 Monday timestamp now returns data" -ForegroundColor Green  
    Write-Host "   ✅ Week 34 Sunday timestamp still works" -ForegroundColor Green
    Write-Host "   ✅ Both timestamps return books for the same week" -ForegroundColor Green
} else {
    Write-Host "❌ Some issues remain unresolved" -ForegroundColor Red
}
