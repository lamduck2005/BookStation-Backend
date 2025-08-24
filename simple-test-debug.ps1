#!/usr/bin/env pwsh
# Simple test script to debug Book Statistics API issue

Write-Host "=== BOOK STATISTICS DEBUG ===" -ForegroundColor Cyan

$baseUrl = "http://localhost:8080/api/books"

# Test Summary API
Write-Host "1. Testing Summary API..." -ForegroundColor Yellow
$summaryUrl = "$baseUrl/statistics/summary?period=week`&fromDate=1754179200000`&toDate=1756080000000"

try {
    $summaryResponse = Invoke-RestMethod -Uri $summaryUrl -Method GET
    Write-Host "Summary Status: $($summaryResponse.status)" -ForegroundColor Green
    Write-Host "Summary Data Count: $($summaryResponse.data.Count)" -ForegroundColor Green
    
    foreach ($item in $summaryResponse.data) {
        if ($item.dateRange -like "*34*" -or $item.dateRange -like "*35*") {
            Write-Host "  $($item.dateRange): $($item.totalBooksSold) books" -ForegroundColor Cyan
        }
    }
} catch {
    Write-Host "Summary API Error: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""

# Test Details API for Week 34
Write-Host "2. Testing Details API for Week 34..." -ForegroundColor Yellow
$detailsUrl34 = "$baseUrl/statistics/details?period=week`&date=1755475200000`&limit=10"

try {
    $detailsResponse34 = Invoke-RestMethod -Uri $detailsUrl34 -Method GET
    Write-Host "Details Week 34 Status: $($detailsResponse34.status)" -ForegroundColor Green
    Write-Host "Details Week 34 Data Count: $($detailsResponse34.data.Count)" -ForegroundColor Green
    
    if ($detailsResponse34.data.Count -eq 0) {
        Write-Host "  NO DATA for Week 34 - THIS IS THE BUG!" -ForegroundColor Red
    } else {
        foreach ($book in $detailsResponse34.data) {
            Write-Host "  $($book.name): $($book.quantitySold) sold" -ForegroundColor Cyan
        }
    }
} catch {
    Write-Host "Details Week 34 Error: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""

# Test Details API for Week 35
Write-Host "3. Testing Details API for Week 35..." -ForegroundColor Yellow
$detailsUrl35 = "$baseUrl/statistics/details?period=week`&date=1756080000000`&limit=10"

try {
    $detailsResponse35 = Invoke-RestMethod -Uri $detailsUrl35 -Method GET
    Write-Host "Details Week 35 Status: $($detailsResponse35.status)" -ForegroundColor Green
    Write-Host "Details Week 35 Data Count: $($detailsResponse35.data.Count)" -ForegroundColor Green
    
    if ($detailsResponse35.data.Count -gt 0) {
        $totalFromDetails = 0
        foreach ($book in $detailsResponse35.data) {
            Write-Host "  $($book.name): $($book.quantitySold) sold" -ForegroundColor Cyan
            $totalFromDetails += $book.quantitySold
        }
        Write-Host "  TOTAL from Details: $totalFromDetails books" -ForegroundColor Magenta
    }
} catch {
    Write-Host "Details Week 35 Error: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""
Write-Host "=== DEBUG COMPLETE ===" -ForegroundColor Cyan
