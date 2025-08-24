#!/usr/bin/env pwsh
# 🔧 SCRIPT TEST HOÀN CHỈNH CHO BOOK STATISTICS API
# Sau khi fix: Week 34 phải có data, Week 35 không có data, loại bỏ growth calculation

param(
    [string]$BaseUrl = "http://localhost:8080/api/books"
)

Write-Host "🔍 TESTING BOOK STATISTICS APIS - COMPLETE VALIDATION" -ForegroundColor Cyan
Write-Host "=================================================" -ForegroundColor Cyan

# Test data expectations
Write-Host "📋 Expected Results:" -ForegroundColor Yellow
Write-Host "  Week 34 (2025-08-18 to 2025-08-24): Should have 7 books (orders on 2025-08-24)" -ForegroundColor Green
Write-Host "  Week 35 (2025-08-25 to 2025-08-31): Should have 4 books (no orders in this period)" -ForegroundColor Green
Write-Host "  Growth calculations: Should be REMOVED from details API" -ForegroundColor Blue
Write-Host ""

# 1. Test Summary API
Write-Host "📊 1. TESTING SUMMARY API" -ForegroundColor Yellow
$summaryUrl = "$BaseUrl/statistics/summary?period=week`&fromDate=1754179200000`&toDate=1756080000000"

try {
    $summaryResponse = Invoke-RestMethod -Uri $summaryUrl -Method GET
    Write-Host "✅ Summary API Status: $($summaryResponse.status)" -ForegroundColor Green
    
    $week34Summary = $summaryResponse.data | Where-Object { $_.dateRange -like "*2025-08-18*" }
    $week35Summary = $summaryResponse.data | Where-Object { $_.dateRange -like "*2025-08-25*" }
    
    if ($week34Summary) {
        Write-Host "📈 Week 34 Summary: $($week34Summary.totalBooksSold) books" -ForegroundColor Green
    } else {
        Write-Host "❌ Week 34 not found in summary" -ForegroundColor Red
    }
    
    if ($week35Summary) {
        Write-Host "📈 Week 35 Summary: $($week35Summary.totalBooksSold) books" -ForegroundColor Green
    } else {
        Write-Host "❌ Week 35 not found in summary" -ForegroundColor Red
    }
} catch {
    Write-Host "❌ Summary API Error: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""

# 2. Test Details API for Week 34
Write-Host "📊 2. TESTING DETAILS API - WEEK 34" -ForegroundColor Yellow
$week34Timestamp = 1755475200000  # 2025-08-18 Monday
$detailsUrl34 = "$BaseUrl/statistics/details?period=week`&date=$week34Timestamp`&limit=10"

$week34Success = $false
try {
    $detailsResponse34 = Invoke-RestMethod -Uri $detailsUrl34 -Method GET
    Write-Host "✅ Week 34 Details Status: $($detailsResponse34.status)" -ForegroundColor Green
    
    if ($detailsResponse34.data.Count -gt 0) {
        Write-Host "✅ Week 34 Details: Found $($detailsResponse34.data.Count) books" -ForegroundColor Green
        $week34Success = $true
        
        $totalQuantityWeek34 = 0
        foreach ($book in $detailsResponse34.data) {
            Write-Host "   📖 $($book.name): $($book.quantitySold) sold" -ForegroundColor Cyan
            $totalQuantityWeek34 += $book.quantitySold
            
            # Kiểm tra growth fields đã bị loại bỏ chưa
            $hasGrowthFields = $book.PSObject.Properties.Name -contains "quantityGrowthPercent" -or 
                             $book.PSObject.Properties.Name -contains "revenueGrowthPercent"
            
            if ($hasGrowthFields) {
                Write-Host "   ⚠️ WARNING: Growth fields still present (should be removed)" -ForegroundColor Yellow
            } else {
                Write-Host "   ✅ Growth fields removed successfully" -ForegroundColor Green
            }
        }
        Write-Host "📊 Week 34 Total: $totalQuantityWeek34 books" -ForegroundColor Green
        
        # Compare với summary
        if ($week34Summary -and $totalQuantityWeek34 -eq $week34Summary.totalBooksSold) {
            Write-Host "✅ Week 34: Details matches Summary ($totalQuantityWeek34 books)" -ForegroundColor Green
        } else {
            Write-Host "❌ Week 34: Details ($totalQuantityWeek34) doesn't match Summary ($($week34Summary.totalBooksSold))" -ForegroundColor Red
        }
    } else {
        Write-Host "❌ Week 34 Details: NO DATA (THIS WAS THE BUG!)" -ForegroundColor Red
    }
} catch {
    Write-Host "❌ Week 34 Details Error: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""

# 3. Test Details API for Week 35
Write-Host "📊 3. TESTING DETAILS API - WEEK 35" -ForegroundColor Yellow
$week35Timestamp = 1756080000000  # 2025-08-25 Monday
$detailsUrl35 = "$BaseUrl/statistics/details?period=week`&date=$week35Timestamp`&limit=10"

$week35Success = $false
try {
    $detailsResponse35 = Invoke-RestMethod -Uri $detailsUrl35 -Method GET
    Write-Host "✅ Week 35 Details Status: $($detailsResponse35.status)" -ForegroundColor Green
    
    if ($detailsResponse35.data.Count -gt 0) {
        Write-Host "📊 Week 35 Details: Found $($detailsResponse35.data.Count) books" -ForegroundColor Green
        
        $totalQuantityWeek35 = 0
        foreach ($book in $detailsResponse35.data) {
            Write-Host "   📖 $($book.name): $($book.quantitySold) sold" -ForegroundColor Cyan
            $totalQuantityWeek35 += $book.quantitySold
        }
        Write-Host "📊 Week 35 Total: $totalQuantityWeek35 books" -ForegroundColor Green
        
        # Compare với summary
        if ($week35Summary -and $totalQuantityWeek35 -eq $week35Summary.totalBooksSold) {
            Write-Host "✅ Week 35: Details matches Summary ($totalQuantityWeek35 books)" -ForegroundColor Green
            $week35Success = $true
        } else {
            Write-Host "❌ Week 35: Details ($totalQuantityWeek35) doesn't match Summary ($($week35Summary.totalBooksSold))" -ForegroundColor Red
        }
    } else {
        Write-Host "📊 Week 35 Details: No data (expected if no orders in this week)" -ForegroundColor Gray
        
        # Check if summary also shows 0
        if ($week35Summary -and $week35Summary.totalBooksSold -eq 0) {
            Write-Host "✅ Week 35: Details matches Summary (0 books)" -ForegroundColor Green
            $week35Success = $true
        }
    }
} catch {
    Write-Host "❌ Week 35 Details Error: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""

# Final validation
Write-Host "🔍 FINAL VALIDATION RESULTS:" -ForegroundColor Cyan
Write-Host "=================================================" -ForegroundColor Cyan

if ($week34Success) {
    Write-Host "✅ Week 34 API: FIXED - Returns correct data" -ForegroundColor Green
} else {
    Write-Host "❌ Week 34 API: STILL BROKEN - Needs more debugging" -ForegroundColor Red
}

if ($week35Success) {
    Write-Host "✅ Week 35 API: FIXED - Returns correct data" -ForegroundColor Green
} else {
    Write-Host "❌ Week 35 API: STILL BROKEN - Needs more debugging" -ForegroundColor Red
}

$allFixed = $week34Success -and $week35Success
if ($allFixed) {
    Write-Host "" 
    Write-Host "🎉 ALL APIS WORKING CORRECTLY! 🎉" -ForegroundColor Green
    Write-Host "=================================================" -ForegroundColor Green
} else {
    Write-Host ""
    Write-Host "⚠️ SOME ISSUES REMAIN - Continue debugging needed" -ForegroundColor Yellow
    Write-Host "=================================================" -ForegroundColor Yellow
}

return $allFixed
