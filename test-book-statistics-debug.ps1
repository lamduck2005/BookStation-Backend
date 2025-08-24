#!/usr/bin/env pwsh
# 🔍 Script debug Book Statistics APIs issue
# Problem: API summary vs details showing different data for same week

Write-Host "🔍 DEBUGGING BOOK STATISTICS MISMATCH" -ForegroundColor Cyan
Write-Host "================================================" -ForegroundColor Cyan

# Base URL
$baseUrl = "http://localhost:8080/api/books"

# Test timestamps
$week34_timestamp = 1755475200000  # Tuần 34 (click point from frontend)
$week35_timestamp = 1756080000000  # Tuần 35 (end date in summary)

# Summary API call (works correctly)
Write-Host "📊 1. Testing Summary API (period=week, range covers week 34 & 35)" -ForegroundColor Yellow
$summaryUrl = "$baseUrl/statistics/summary?period=week`&fromDate=1754179200000`&toDate=1756080000000"
try {
    $summaryResponse = Invoke-RestMethod -Uri $summaryUrl -Method GET -ContentType "application/json"
    Write-Host "✅ Summary API Status: $($summaryResponse.status)" -ForegroundColor Green
    
    $week34Data = $summaryResponse.data | Where-Object { $_.dateRange -like "*Tuần 34*" }
    $week35Data = $summaryResponse.data | Where-Object { $_.dateRange -like "*Tuần 35*" }
    
    if ($week34Data) {
        Write-Host "📈 WEEK 34 Summary: $($week34Data.totalBooksSold) books, Revenue: $($week34Data.netRevenue)" -ForegroundColor Green
        Write-Host "   📅 Date: $($week34Data.date), Range: $($week34Data.dateRange)" -ForegroundColor Gray
    } else {
        Write-Host "❌ No Week 34 data found in summary" -ForegroundColor Red
    }
    
    if ($week35Data) {
        Write-Host "📈 WEEK 35 Summary: $($week35Data.totalBooksSold) books, Revenue: $($week35Data.netRevenue)" -ForegroundColor Green
        Write-Host "   📅 Date: $($week35Data.date), Range: $($week35Data.dateRange)" -ForegroundColor Gray
    } else {
        Write-Host "❌ No Week 35 data found in summary" -ForegroundColor Red
    }
} catch {
    Write-Host "❌ Summary API Error: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""

# Details API call for week 34 (should show data but returns nothing)
Write-Host "🔍 2. Testing Details API for Week 34 (timestamp: $week34_timestamp)" -ForegroundColor Yellow
$detailsUrl34 = "$baseUrl/statistics/details?period=week`&date=$week34_timestamp`&limit=10"
try {
    $detailsResponse34 = Invoke-RestMethod -Uri $detailsUrl34 -Method GET -ContentType "application/json"
    Write-Host "✅ Details API Week 34 Status: $($detailsResponse34.status)" -ForegroundColor Green
    Write-Host "📊 Details API Week 34 Message: $($detailsResponse34.message)" -ForegroundColor Gray
    
    if ($detailsResponse34.data.Count -gt 0) {
        Write-Host "📚 Found $($detailsResponse34.data.Count) books for Week 34:" -ForegroundColor Green
        foreach ($book in $detailsResponse34.data) {
            Write-Host "   📖 $($book.name): $($book.quantitySold) sold, Revenue: $($book.revenue)" -ForegroundColor Cyan
        }
    } else {
        Write-Host "❌ NO DATA returned for Week 34 details (THIS IS THE BUG!)" -ForegroundColor Red
    }
} catch {
    Write-Host "❌ Details API Week 34 Error: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""

# Details API call for week 35 (should show correct data but shows wrong amounts)
Write-Host "🔍 3. Testing Details API for Week 35 (timestamp: $week35_timestamp)" -ForegroundColor Yellow
$detailsUrl35 = "$baseUrl/statistics/details?period=week`&date=$week35_timestamp`&limit=10"
try {
    $detailsResponse35 = Invoke-RestMethod -Uri $detailsUrl35 -Method GET -ContentType "application/json"
    Write-Host "✅ Details API Week 35 Status: $($detailsResponse35.status)" -ForegroundColor Green
    Write-Host "📊 Details API Week 35 Message: $($detailsResponse35.message)" -ForegroundColor Gray
    
    if ($detailsResponse35.data.Count -gt 0) {
        Write-Host "📚 Found $($detailsResponse35.data.Count) books for Week 35:" -ForegroundColor Green
        $totalQuantityFromDetails = 0
        foreach ($book in $detailsResponse35.data) {
            Write-Host "   📖 $($book.name): $($book.quantitySold) sold, Revenue: $($book.revenue)" -ForegroundColor Cyan
            $totalQuantityFromDetails += $book.quantitySold
        }
        Write-Host "📊 TOTAL from Details: $totalQuantityFromDetails books" -ForegroundColor Magenta
        
        # Compare với summary
        if ($week35Data) {
            $summaryTotal = $week35Data.totalBooksSold
            Write-Host "📊 TOTAL from Summary: $summaryTotal books" -ForegroundColor Magenta
            
            if ($totalQuantityFromDetails -ne $summaryTotal) {
                Write-Host "⚠️ MISMATCH! Details: $totalQuantityFromDetails vs Summary: $summaryTotal" -ForegroundColor Red
            } else {
                Write-Host "✅ Match! Details and Summary agree on totals" -ForegroundColor Green
            }
        }
    } else {
        Write-Host "❌ NO DATA returned for Week 35 details" -ForegroundColor Red
    }
} catch {
    Write-Host "❌ Details API Week 35 Error: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""

# Debug: Test với các timestamp khác nhau để xem logic tính tuần
Write-Host "🔧 4. Debug Week Calculation Logic" -ForegroundColor Yellow

# Test với một số timestamp trong tuần 34 & 35
$testTimestamps = @(
    @{ Name = "Start of Week 34"; Timestamp = 1754870400000 },  # Approximate start
    @{ Name = "Mid Week 34"; Timestamp = 1755216000000 },       # Mid week  
    @{ Name = "End of Week 34"; Timestamp = 1755475199999 },    # End
    @{ Name = "Start of Week 35"; Timestamp = 1755475200000 },  # Start of next week
    @{ Name = "Mid Week 35"; Timestamp = 1755820800000 },       # Mid week
    @{ Name = "End Week 35"; Timestamp = 1756080000000 }        # End
)

foreach ($test in $testTimestamps) {
    Write-Host "🕐 Testing $($test.Name) (timestamp: $($test.Timestamp))" -ForegroundColor Cyan
    $testUrl = "$baseUrl/statistics/details?period=week`&date=$($test.Timestamp)`&limit=5"
    
    try {
        $testResponse = Invoke-RestMethod -Uri $testUrl -Method GET -ContentType "application/json"
        $bookCount = if ($testResponse.data) { $testResponse.data.Count } else { 0 }
        $totalQuantity = if ($testResponse.data) { 
            ($testResponse.data | Measure-Object -Property quantitySold -Sum).Sum 
        } else { 0 }
        
        Write-Host "   📊 Result: $bookCount books, $totalQuantity total sold" -ForegroundColor Gray
    } catch {
        Write-Host "   ❌ Error: $($_.Exception.Message)" -ForegroundColor Red
    }
}

Write-Host ""
Write-Host "🔍 ANALYSIS SUMMARY:" -ForegroundColor Cyan
Write-Host "1. Week 34 Details API returns no data (BUG)" -ForegroundColor Red  
Write-Host "2. Week 35 Details API may return wrong totals vs Summary" -ForegroundColor Yellow
Write-Host "3. Need to check week calculation logic consistency" -ForegroundColor Yellow
Write-Host "4. Need to remove growth calculation logic as requested" -ForegroundColor Blue
Write-Host "================================================" -ForegroundColor Cyan
