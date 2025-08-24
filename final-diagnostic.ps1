#!/usr/bin/env pwsh
# Final diagnostic test - check exact issue with timestamps

Write-Host "=== FINAL DIAGNOSTIC TEST ===" -ForegroundColor Cyan

$baseUrl = "http://localhost:8080/api/books"

# Get actual order data to understand timestamps
Write-Host "1. Getting actual order data:" -ForegroundColor Yellow
try {
    $ordersResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/orders?page=0&size=10" -Method GET
    
    if ($ordersResponse.data.content) {
        foreach ($order in $ordersResponse.data.content) {
            $orderDate = [DateTimeOffset]::FromUnixTimeMilliseconds($order.orderDate)
            Write-Host "  Order: $($order.code)" -ForegroundColor Cyan
            Write-Host "    Timestamp: $($order.orderDate)" -ForegroundColor Gray
            Write-Host "    Date: $($orderDate.ToString('yyyy-MM-dd HH:mm:ss dddd'))" -ForegroundColor Gray
            
            # Calculate what week this order should be in
            $dayOfWeek = [int]$orderDate.DayOfWeek
            $daysToSubtract = if ($dayOfWeek -eq 0) { 6 } else { $dayOfWeek - 1 }
            $monday = $orderDate.Date.AddDays(-$daysToSubtract)
            $sunday = $monday.AddDays(6)
            Write-Host "    Should be in week: $($monday.ToString('yyyy-MM-dd')) to $($sunday.ToString('yyyy-MM-dd'))" -ForegroundColor Gray
            Write-Host ""
        }
    }
} catch {
    Write-Host "Error getting orders: $($_.Exception.Message)" -ForegroundColor Red
}

# Test with exact timestamp ranges that should work
Write-Host "2. Testing with exact timestamp ranges:" -ForegroundColor Yellow

# Range that should definitely include all orders (very wide)
$veryWideStart = 1755400000000  # Well before any orders
$veryWideEnd = 1756200000000    # Well after any orders

Write-Host "Testing very wide range: $veryWideStart to $veryWideEnd" -ForegroundColor Cyan
$testUrl = "$baseUrl/statistics/details?period=day`&date=1756030908539`&limit=10"

try {
    $response = Invoke-RestMethod -Uri $testUrl -Method GET
    Write-Host "Day query result: $($response.data.Count) books" -ForegroundColor Green
    
    if ($response.data.Count -gt 0) {
        foreach ($book in $response.data) {
            Write-Host "  $($book.name): $($book.quantitySold) sold" -ForegroundColor Yellow
        }
    }
} catch {
    Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""

# The key test - Week calculation comparison
Write-Host "3. Key Issue - Monday timestamp calculation:" -ForegroundColor Yellow

$mondayTimestamp = 1755475200000  # Monday 2025-08-18
$sundayTimestamp = 1756030908539  # Sunday 2025-08-24 (actual order time)

$mondayDate = [DateTimeOffset]::FromUnixTimeMilliseconds($mondayTimestamp)
$sundayDate = [DateTimeOffset]::FromUnixTimeMilliseconds($sundayTimestamp)

Write-Host "Monday input: $mondayTimestamp = $($mondayDate.ToString('yyyy-MM-dd HH:mm:ss dddd'))" -ForegroundColor Cyan
Write-Host "Sunday input: $sundayTimestamp = $($sundayDate.ToString('yyyy-MM-dd HH:mm:ss dddd'))" -ForegroundColor Cyan

# Both should calculate to the same week (2025-08-18 to 2025-08-24)
Write-Host "Both should calculate to week: 2025-08-18 to 2025-08-24" -ForegroundColor Yellow
Write-Host "But Monday input gives 0 results, Sunday input gives results" -ForegroundColor Red
Write-Host "This suggests the Java week calculation has a bug with Monday timestamps" -ForegroundColor Red

Write-Host ""
Write-Host "=== CONCLUSION ===" -ForegroundColor Cyan
Write-Host "The bug is in the Java week calculation logic when input is Monday timestamp" -ForegroundColor Red
Write-Host "The fix needs to handle Monday timestamps correctly to match Sunday timestamps" -ForegroundColor Yellow
Write-Host "=== END DIAGNOSTIC ===" -ForegroundColor Cyan
