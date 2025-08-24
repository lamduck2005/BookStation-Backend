#!/usr/bin/env pwsh
# 🔧 FINAL TEST SCRIPT - Simple and Clear

Write-Host "=== FINAL BOOK STATISTICS TEST ===" -ForegroundColor Cyan

$baseUrl = "http://localhost:8080/api/books"

# Test Week 34 - should have data
Write-Host "1. Testing Week 34 (should have 7+ books):" -ForegroundColor Yellow
$week34Url = "$baseUrl/statistics/details?period=week`&date=1755475200000`&limit=10"

try {
    $week34Response = Invoke-RestMethod -Uri $week34Url -Method GET
    if ($week34Response.data.Count -gt 0) {
        $total34 = ($week34Response.data | Measure-Object -Property quantitySold -Sum).Sum
        Write-Host "  ✅ Week 34: $($week34Response.data.Count) books, $total34 total sold" -ForegroundColor Green
    } else {
        Write-Host "  ❌ Week 34: NO DATA (BUG NOT FIXED)" -ForegroundColor Red
    }
} catch {
    Write-Host "  ❌ Week 34: ERROR - $($_.Exception.Message)" -ForegroundColor Red
}

# Test Week 35 - should have different data or no data
Write-Host "2. Testing Week 35:" -ForegroundColor Yellow
$week35Url = "$baseUrl/statistics/details?period=week`&date=1756080000000`&limit=10"

try {
    $week35Response = Invoke-RestMethod -Uri $week35Url -Method GET
    if ($week35Response.data.Count -gt 0) {
        $total35 = ($week35Response.data | Measure-Object -Property quantitySold -Sum).Sum
        Write-Host "  📊 Week 35: $($week35Response.data.Count) books, $total35 total sold" -ForegroundColor Cyan
    } else {
        Write-Host "  📊 Week 35: No data" -ForegroundColor Gray
    }
} catch {
    Write-Host "  ❌ Week 35: ERROR - $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""
Write-Host "Expected: Week 34 should have data (orders on 2025-08-24)" -ForegroundColor Yellow
Write-Host "=== END TEST ===" -ForegroundColor Cyan
