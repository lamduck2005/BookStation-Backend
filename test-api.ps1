param(
    [string]$Period = "day",
    [string]$BaseUrl = "http://localhost:8080"
)

Write-Host "=== TESTING API STATISTICS ===" -ForegroundColor Green
Write-Host "Period: $Period" -ForegroundColor Cyan
Write-Host "Base URL: $BaseUrl" -ForegroundColor Cyan

$apiUrl = "$BaseUrl/api/books/statistics/summary?period=$Period"
Write-Host "API URL: $apiUrl" -ForegroundColor White

try {
    $response = Invoke-RestMethod -Uri $apiUrl -Method GET -ErrorAction Stop
    Write-Host "SUCCESS: API Response received" -ForegroundColor Green
    
    if ($response -and $response.data) {
        $totalDataPoints = $response.data.Count
        $nonZeroPoints = ($response.data | Where-Object { $_.totalBooksSold -gt 0 }).Count
        
        Write-Host "Current API Results:" -ForegroundColor Cyan
        Write-Host "  - Total data points: $totalDataPoints" -ForegroundColor White
        Write-Host "  - Non-zero points: $nonZeroPoints" -ForegroundColor White
        
        if ($nonZeroPoints -gt 0) {
            Write-Host "  - Sample non-zero data:" -ForegroundColor White
            $response.data | Where-Object { $_.totalBooksSold -gt 0 } | Select-Object -First 3 | ForEach-Object {
                Write-Host "    * Date: $($_.date), Sold: $($_.totalBooksSold)" -ForegroundColor Gray
            }
        }
        
        Write-Host "  - Recent dates data (last 5):" -ForegroundColor White
        $response.data | Select-Object -Last 5 | ForEach-Object {
            Write-Host "    * Date: $($_.date), Sold: $($_.totalBooksSold)" -ForegroundColor Gray
        }
    }
    
} catch {
    Write-Host "ERROR: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

Write-Host "`n=== ANALYSIS ===" -ForegroundColor Yellow
Write-Host "Issues found:" -ForegroundColor Red
Write-Host "  - API not accounting for refunded quantities" -ForegroundColor Red
Write-Host "  - API not returning net revenue (after voucher discounts)" -ForegroundColor Red
Write-Host "  - Dates might be showing wrong data" -ForegroundColor Red

Write-Host "`nReady to implement fixes..." -ForegroundColor Green
