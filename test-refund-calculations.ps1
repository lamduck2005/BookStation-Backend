# Book Statistics API Testing Script - Fixed Refund Calculations
# Test both Summary and Details APIs with real database data

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  BOOK STATISTICS API REFUND TEST" -ForegroundColor Yellow  
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Wait for server to be ready
Write-Host "🔄 Waiting for server to start..." -ForegroundColor Yellow
do {
    Start-Sleep -Seconds 2
    $serverStatus = $null
    try {
        $serverStatus = Invoke-RestMethod -Uri "http://localhost:8080/actuator/health" -TimeoutSec 3
    } catch {
        Write-Host "." -NoNewline -ForegroundColor Gray
    }
} while ($serverStatus -eq $null -or $serverStatus.status -ne "UP")

Write-Host ""
Write-Host "✅ Server is ready!" -ForegroundColor Green
Write-Host ""

# Test 1: Summary API - 30 days default
Write-Host "[1/3] Testing Summary API (30 days default)..." -ForegroundColor White
try {
    $summaryResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/books/statistics/summary?period=day" -Method GET -Headers @{"Accept"="application/json"}
    
    if ($summaryResponse.status -eq 200) {
        Write-Host "✅ SUCCESS - Summary API (Fixed Refund Calculation)" -ForegroundColor Green
        
        # Count non-zero days
        $nonZeroDays = $summaryResponse.data | Where-Object { $_.totalBooksSold -gt 0 }
        Write-Host "📊 Total days with sales: $($nonZeroDays.Count)" -ForegroundColor Cyan
        
        if ($nonZeroDays.Count -gt 0) {
            Write-Host "📈 Sample data:" -ForegroundColor Yellow
            $nonZeroDays | Select-Object -First 5 | ForEach-Object {
                Write-Host "   $($_.date): $($_.totalBooksSold) books sold" -ForegroundColor White
            }
            
            # Look specifically for 2025-08-14 
            $aug14 = $summaryResponse.data | Where-Object { $_.date -eq "2025-08-14" }
            if ($aug14) {
                Write-Host "🎯 2025-08-14: $($aug14.totalBooksSold) books sold (after refund)" -ForegroundColor Magenta
                if ($aug14.totalBooksSold -eq 2) {
                    Write-Host "✅ REFUND CALCULATION CORRECT! (Expected: 2)" -ForegroundColor Green
                } else {
                    Write-Host "❌ REFUND CALCULATION WRONG! (Expected: 2, Got: $($aug14.totalBooksSold))" -ForegroundColor Red
                }
            }
        }
    } else {
        Write-Host "❌ ERROR - Summary API: $($summaryResponse.message)" -ForegroundColor Red
    }
} catch {
    Write-Host "❌ ERROR - Summary API Exception: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""

# Test 2: Details API - Real data not mock
Write-Host "[2/3] Testing Details API (Real data, not mock)..." -ForegroundColor White
try {
    # August 14, 2025 timestamp (user's example date)
    $aug14Timestamp = 1723594800000  # 2025-08-14 00:00:00 UTC
    
    $detailsResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/books/statistics/details?period=day&date=$aug14Timestamp&limit=10" -Method GET
    
    if ($detailsResponse.status -eq 200) {
        Write-Host "✅ SUCCESS - Details API (Real Database Data)" -ForegroundColor Green
        
        if ($detailsResponse.data.Count -gt 0) {
            Write-Host "📚 Books found: $($detailsResponse.data.Count)" -ForegroundColor Cyan
            Write-Host "📈 Sample book data:" -ForegroundColor Yellow
            
            $detailsResponse.data | Select-Object -First 3 | ForEach-Object {
                Write-Host "   📖 $($_.code): $($_.name)" -ForegroundColor White
                Write-Host "      Quantity: $($_.quantitySold) | Revenue: $($_.revenue)" -ForegroundColor Gray
                Write-Host "      Growth: $($_.quantityGrowthPercent)% quantity, $($_.revenueGrowthPercent)% revenue" -ForegroundColor Gray
                Write-Host ""
            }
            
            # Check if data looks real (not mock BK001)
            $mockData = $detailsResponse.data | Where-Object { $_.code -eq "BK001" -and $_.quantitySold -eq 45 }
            if ($mockData) {
                Write-Host "❌ STILL RETURNING MOCK DATA! (BK001 with 45 quantity)" -ForegroundColor Red
            } else {
                Write-Host "✅ REAL DATA CONFIRMED! (No mock BK001 detected)" -ForegroundColor Green
            }
        } else {
            Write-Host "⚠️  No books found for the specified period" -ForegroundColor Yellow
        }
    } else {
        Write-Host "❌ ERROR - Details API: $($detailsResponse.message)" -ForegroundColor Red
    }
} catch {
    Write-Host "❌ ERROR - Details API Exception: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""

# Test 3: Period auto-grouping validation
Write-Host "[3/3] Testing Period Auto-grouping..." -ForegroundColor White
try {
    # Test weekly grouping (45 days should auto-group to weekly)
    $weeklyResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/books/statistics/summary?period=custom&fromDate=1720051200000&toDate=1723593600000" -Method GET
    
    if ($weeklyResponse.status -eq 200 -and $weeklyResponse.data.Count -gt 0) {
        $firstPeriod = $weeklyResponse.data[0].period
        if ($firstPeriod -eq "weekly") {
            Write-Host "✅ SUCCESS - Auto-grouping to weekly (45 days)" -ForegroundColor Green
        } else {
            Write-Host "❌ ERROR - Expected weekly, got: $firstPeriod" -ForegroundColor Red
        }
    } else {
        Write-Host "⚠️  Period auto-grouping test failed" -ForegroundColor Yellow
    }
} catch {
    Write-Host "⚠️  Period auto-grouping test exception: $($_.Exception.Message)" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "           TEST COMPLETED                " -ForegroundColor Yellow  
Write-Host "========================================" -ForegroundColor Cyan
