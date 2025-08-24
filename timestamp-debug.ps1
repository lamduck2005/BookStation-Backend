#!/usr/bin/env pwsh
# Debug timestamp calculation để tìm bug

Write-Host "=== TIMESTAMP DEBUG ===" -ForegroundColor Cyan

$baseUrl = "http://localhost:8080/api/books"

# Convert timestamps để hiểu
$timestamps = @{
    "Week 34 (Summary shows 7 books)" = 1755475200000;  # API details returns 0
    "Week 35 (Summary shows 4 books)" = 1756080000000;  # API details returns wrong 11
}

Write-Host "Analyzing timestamps:" -ForegroundColor Yellow
foreach ($item in $timestamps.GetEnumerator()) {
    $ts = $item.Value
    $date = [DateTimeOffset]::FromUnixTimeMilliseconds($ts)
    $dayOfWeek = $date.DayOfWeek
    
    # Calculate what Monday of that week should be
    $daysToSubtract = [int]$dayOfWeek - 1
    if ($daysToSubtract -eq -1) { $daysToSubtract = 6 }  # Sunday case
    $mondayOfWeek = $date.AddDays(-$daysToSubtract)
    
    Write-Host "  $($item.Key):" -ForegroundColor Cyan
    Write-Host "    Timestamp: $ts" -ForegroundColor Gray
    Write-Host "    Date: $($date.ToString('yyyy-MM-dd dddd'))" -ForegroundColor Gray
    Write-Host "    Monday of that week: $($mondayOfWeek.ToString('yyyy-MM-dd'))" -ForegroundColor Gray
    Write-Host "    Expected week range: $($mondayOfWeek.ToString('yyyy-MM-dd')) to $($mondayOfWeek.AddDays(6).ToString('yyyy-MM-dd'))" -ForegroundColor Gray
    Write-Host ""
}

# Test with orders to verify which week they belong to
Write-Host "Checking actual orders:" -ForegroundColor Yellow
try {
    $ordersResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/orders?page=0&size=20" -Method GET
    
    foreach ($order in $ordersResponse.data.content) {
        $orderDate = [DateTimeOffset]::FromUnixTimeMilliseconds($order.orderDate)
        $dayOfWeek = $orderDate.DayOfWeek
        
        # Calculate Monday of the week for this order
        $daysToSubtract = [int]$dayOfWeek - 1
        if ($daysToSubtract -eq -1) { $daysToSubtract = 6 }
        $mondayOfWeek = $orderDate.AddDays(-$daysToSubtract)
        
        Write-Host "  Order $($order.code): $($orderDate.ToString('yyyy-MM-dd dddd')) -> Week starting $($mondayOfWeek.ToString('yyyy-MM-dd'))" -ForegroundColor Cyan
    }
} catch {
    Write-Host "Error getting orders: $($_.Exception.Message)" -ForegroundColor Red
}

# Test with specific timestamps that SHOULD work
Write-Host ""
Write-Host "Testing week start timestamps for Week 34:" -ForegroundColor Yellow
$week34Start = [DateTimeOffset]::Parse("2025-08-18").ToUnixTimeMilliseconds()  # Monday of Week 34
$detailsUrl = "$baseUrl/statistics/details?period=week`&date=$week34Start`&limit=10"

try {
    $response = Invoke-RestMethod -Uri $detailsUrl -Method GET
    Write-Host "Week 34 with Monday timestamp ($week34Start): $($response.data.Count) books found" -ForegroundColor Green
} catch {
    Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "=== END DEBUG ===" -ForegroundColor Cyan
