# Final test for Week 34 fix - using enhanced nanosecond precision

Write-Host "Final Week 34 Fix Test" -ForegroundColor Yellow
Write-Host "Testing with enhanced end-time precision..." -ForegroundColor White

# Test the fixed Week 34 calculation
$mondayTimestamp = 1755475200000
$url = "http://localhost:8080/api/books/statistics/details?period=week&date=$mondayTimestamp&limit=10"

try {
    Write-Host "Testing URL: $url" -ForegroundColor Cyan
    $response = Invoke-RestMethod -Uri $url -Method GET
    
    Write-Host "Response received:" -ForegroundColor Green
    Write-Host "Total books: $($response.Count)" -ForegroundColor White
    
    if ($response.Count -gt 0) {
        Write-Host "Books found:" -ForegroundColor Green
        $response | ForEach-Object { 
            Write-Host "- $($_.title): $($_.totalQuantity) sold" -ForegroundColor White
        }
        Write-Host "SUCCESS: Week 34 now returns data!" -ForegroundColor Green
    } else {
        Write-Host "STILL NO DATA for Week 34" -ForegroundColor Red
    }
} 
catch {
    Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""
Write-Host "For reference:" -ForegroundColor Yellow
$dateObj = Get-Date -UnixTimeSeconds ($mondayTimestamp/1000)
Write-Host "Monday timestamp: $mondayTimestamp = $dateObj" -ForegroundColor White
Write-Host "Expected week: 2025-08-18 to 2025-08-24" -ForegroundColor White
