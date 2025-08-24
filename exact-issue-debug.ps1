#!/usr/bin/env pwsh
# Test different timestamps to find the exact issue

Write-Host "=== FINDING THE EXACT ISSUE ===" -ForegroundColor Cyan

$baseUrl = "http://localhost:8080/api/books"

# All orders are on 2025-08-24 Sunday (1756030908539 to 1756064534247 range)
# Week 34: 2025-08-18 to 2025-08-24 (should include all orders)

# Test timestamps around the actual order dates
$testTimestamps = @(
    @{ Name = "Order Date 1756030908539"; Timestamp = 1756030908539 },
    @{ Name = "Order Date 1756064534247"; Timestamp = 1756064534247 },
    @{ Name = "Sunday 2025-08-24 Start"; Timestamp = 1756022400000 },
    @{ Name = "Sunday 2025-08-24 End"; Timestamp = 1756108799999 }
)

foreach ($test in $testTimestamps) {
    Write-Host "Testing $($test.Name) (timestamp: $($test.Timestamp))" -ForegroundColor Yellow
    $testUrl = "$baseUrl/statistics/details?period=day`&date=$($test.Timestamp)`&limit=10"
    
    try {
        $response = Invoke-RestMethod -Uri $testUrl -Method GET
        $bookCount = if ($response.data) { $response.data.Count } else { 0 }
        $totalQuantity = if ($response.data) { 
            ($response.data | Measure-Object -Property quantitySold -Sum).Sum 
        } else { 0 }
        
        Write-Host "  Day query result: $bookCount books, $totalQuantity total sold" -ForegroundColor Green
        
        if ($bookCount -gt 0) {
            foreach ($book in $response.data) {
                Write-Host "    $($book.name): $($book.quantitySold) sold" -ForegroundColor Cyan
            }
        }
    } catch {
        Write-Host "  Error: $($_.Exception.Message)" -ForegroundColor Red
    }
    
    Write-Host ""
}

Write-Host "Now testing the week query that should work:" -ForegroundColor Yellow

# Test với week period và timestamp trong khoảng có dữ liệu
$testUrl = "$baseUrl/statistics/details?period=week`&date=1756030908539`&limit=10"

try {
    $response = Invoke-RestMethod -Uri $testUrl -Method GET
    Write-Host "Week query with order timestamp: $($response.data.Count) books" -ForegroundColor Green
    
    if ($response.data.Count -gt 0) {
        foreach ($book in $response.data) {
            Write-Host "  $($book.name): $($book.quantitySold) sold" -ForegroundColor Cyan
        }
    } else {
        Write-Host "  Still no data - there's definitely a bug!" -ForegroundColor Red
    }
} catch {
    Write-Host "Week query error: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "=== END TEST ===" -ForegroundColor Cyan
