#!/usr/bin/env pwsh
# Debug week calculation specifically

Write-Host "=== WEEK CALCULATION DEBUG ===" -ForegroundColor Cyan

$baseUrl = "http://localhost:8080/api/books"

# Check what weeks summary shows
$summaryUrl = "$baseUrl/statistics/summary?period=week`&fromDate=1754179200000`&toDate=1756080000000"
$summaryResponse = Invoke-RestMethod -Uri $summaryUrl -Method GET

Write-Host "Summary weeks found:" -ForegroundColor Yellow
foreach ($item in $summaryResponse.data) {
    Write-Host "  Week: $($item.dateRange) | Date: $($item.date) | Books: $($item.totalBooksSold)" -ForegroundColor Cyan
}

Write-Host ""

# Convert timestamps to readable dates for debugging
$timestamps = @{
    "1754179200000" = "Start Range";
    "1756080000000" = "End Range";  
    "1755475200000" = "Week 34 Click Point";
}

Write-Host "Timestamp conversion:" -ForegroundColor Yellow
foreach ($ts in $timestamps.GetEnumerator()) {
    $date = [DateTimeOffset]::FromUnixTimeMilliseconds($ts.Name).ToString("yyyy-MM-dd HH:mm:ss")
    Write-Host "  $($ts.Value): $($ts.Name) = $date" -ForegroundColor Gray
}

Write-Host ""

# Test actual orders in the database for that time range 
Write-Host "Let's check orders API for verification..." -ForegroundColor Yellow
try {
    $ordersResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/orders?page=0&size=20" -Method GET
    
    Write-Host "Recent orders found:" -ForegroundColor Green
    foreach ($order in $ordersResponse.data.content) {
        $orderDate = [DateTimeOffset]::FromUnixTimeMilliseconds($order.orderDate).ToString("yyyy-MM-dd")
        Write-Host "  Order $($order.code): Date $orderDate | Status: $($order.orderStatus)" -ForegroundColor Cyan
    }
} catch {
    Write-Host "Error getting orders: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "=== END DEBUG ===" -ForegroundColor Cyan
