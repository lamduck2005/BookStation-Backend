#!/usr/bin/env pwsh

# ================================================================================================
# SCRIPT TEST VA FIX API THONG KE SACH
# ================================================================================================
# Muc dich: Fix API /api/books/statistics/summary de tra ve du lieu chinh xac
# - Tong so luong sach da ban (tru hoan tra thanh cong)  
# - Doanh thu thuan (tru voucher neu co)
# - Tinh theo period: day/week/month/quarter/year
# ================================================================================================

param(
    [string]$Period = "day",
    [string]$BaseUrl = "http://localhost:8080"
)

Write-Host "STARTING API STATISTICS TEST AND FIX" -ForegroundColor Green
Write-Host "Period: $Period" -ForegroundColor Cyan
Write-Host "Base URL: $BaseUrl" -ForegroundColor Cyan

# ================================================================================================
# STEP 1: TEST CURRENT API
# ================================================================================================
Write-Host "`nSTEP 1: Testing current API..." -ForegroundColor Yellow

$apiUrl = "$BaseUrl/api/books/statistics/summary?period=$Period"
Write-Host "API URL: $apiUrl" -ForegroundColor White

try {
    $response = Invoke-RestMethod -Uri $apiUrl -Method GET -ErrorAction Stop
    Write-Host "API Response received" -ForegroundColor Green
    
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
        
        # Show recent dates (last 5 days)
        Write-Host "`n  - Recent dates data:" -ForegroundColor White
        $response.data | Select-Object -Last 5 | ForEach-Object {
            Write-Host "    * Date: $($_.date), Sold: $($_.totalBooksSold)" -ForegroundColor Gray
        }
    }
    
} catch {
    Write-Host "Error testing current API: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "Full error: $($_.Exception)" -ForegroundColor Red
    exit 1
}

# ================================================================================================
# 📋 STEP 2: ANALYZE DATABASE TO UNDERSTAND THE ISSUE
# ================================================================================================
Write-Host "`n🔍 STEP 2: Analyzing database structure..." -ForegroundColor Yellow

Write-Host "📋 Analysis needed:" -ForegroundColor Cyan
Write-Host "  1. Current query only counts sold quantities from order_detail" -ForegroundColor White
Write-Host "  2. Need to subtract successful refund quantities from refund_item" -ForegroundColor White  
Write-Host "  3. Need to add net revenue (total_amount - voucher_discount_amount)" -ForegroundColor White
Write-Host "  4. Refund status to consider: APPROVED, COMPLETED" -ForegroundColor White

# ================================================================================================
# 📋 STEP 3: SHOW PROPOSED SOLUTION
# ================================================================================================
Write-Host "`n💡 STEP 3: Proposed solution..." -ForegroundColor Yellow

$proposedQuery = @"
SELECT 
    CAST(DATEADD(SECOND, o.created_at / 1000, '1970-01-01') AS DATE) as saleDate,
    COALESCE(SUM(od.quantity), 0) - COALESCE(SUM(refunds.refund_quantity), 0) as netBooksSold,
    COALESCE(SUM(o.total_amount - COALESCE(o.voucher_discount_amount, 0)), 0) as netRevenue
FROM order_detail od 
JOIN [order] o ON od.order_id = o.id 
LEFT JOIN (
    SELECT 
        od2.order_id,
        od2.book_id, 
        SUM(ri.refund_quantity) as refund_quantity
    FROM refund_item ri
    JOIN refund_request rr ON ri.refund_request_id = rr.id
    JOIN order_detail od2 ON rr.order_id = od2.order_id AND ri.book_id = od2.book_id
    WHERE rr.status IN ('APPROVED', 'COMPLETED')
    GROUP BY od2.order_id, od2.book_id
) refunds ON od.order_id = refunds.order_id AND od.book_id = refunds.book_id
WHERE o.created_at >= :startDate AND o.created_at <= :endDate 
  AND o.order_status IN ('DELIVERED', 'PARTIALLY_REFUNDED') 
GROUP BY CAST(DATEADD(SECOND, o.created_at / 1000, '1970-01-01') AS DATE) 
ORDER BY saleDate
"@

Write-Host "🔧 Proposed SQL Query:" -ForegroundColor Cyan
Write-Host $proposedQuery -ForegroundColor Gray

# ================================================================================================
# 📋 STEP 4: CREATE BACKUP & IMPLEMENTATION PLAN
# ================================================================================================
Write-Host "`n📝 STEP 4: Implementation plan..." -ForegroundColor Yellow

Write-Host "Implementation steps:" -ForegroundColor Cyan
Write-Host "  1. ✅ Create backup of current OrderDetailRepository.findBookSalesSummaryByDateRange" -ForegroundColor Green
Write-Host "  2. ✅ Update method to include refund calculation and net revenue" -ForegroundColor Green  
Write-Host "  3. ✅ Update response format to include netRevenue field" -ForegroundColor Green
Write-Host "  4. ✅ Test with different periods (day/week/month/quarter/year)" -ForegroundColor Green
Write-Host "  5. ✅ Verify calculations are accurate" -ForegroundColor Green

# ================================================================================================
# 📋 STEP 5: FILES THAT NEED TO BE MODIFIED
# ================================================================================================
Write-Host "`n📁 STEP 5: Files to be modified..." -ForegroundColor Yellow

$filesToModify = @(
    "src/main/java/org/datn/bookstation/repository/OrderDetailRepository.java",
    "src/main/java/org/datn/bookstation/service/impl/BookServiceImpl.java"
)

Write-Host "Files that need modification:" -ForegroundColor Cyan
foreach ($file in $filesToModify) {
    $fullPath = Join-Path (Get-Location) $file
    if (Test-Path $fullPath) {
        Write-Host "  ✅ $file (exists)" -ForegroundColor Green
    } else {
        Write-Host "  ❌ $file (not found)" -ForegroundColor Red
    }
}

# ================================================================================================
# 📋 STEP 6: WAIT FOR USER CONFIRMATION
# ================================================================================================
Write-Host "`n🤔 STEP 6: Ready to implement fixes..." -ForegroundColor Yellow

$confirmation = Read-Host "Do you want to proceed with implementing the fixes? (y/N)"

if ($confirmation -ne 'y' -and $confirmation -ne 'Y') {
    Write-Host "⏹️ Operation cancelled by user." -ForegroundColor Yellow
    exit 0
}

# ================================================================================================
# 📋 STEP 7: IMPLEMENT THE FIXES
# ================================================================================================
Write-Host "`n🔧 STEP 7: Implementing fixes..." -ForegroundColor Yellow

Write-Host "✅ Fix implementation will be done through VS Code tools..." -ForegroundColor Green
Write-Host "📋 Next steps:" -ForegroundColor Cyan
Write-Host "  1. Update OrderDetailRepository to add new method with refund calculation" -ForegroundColor White
Write-Host "  2. Update BookServiceImpl to use new method and return net revenue" -ForegroundColor White
Write-Host "  3. Test the updated API" -ForegroundColor White

# ================================================================================================
# 📋 FINAL SUMMARY
# ================================================================================================
Write-Host "`n📊 SUMMARY:" -ForegroundColor Green
Write-Host "Current API issues identified:" -ForegroundColor Cyan
Write-Host "  - ❌ Not accounting for refunded quantities" -ForegroundColor Red
Write-Host "  - ❌ Not returning net revenue (after voucher discounts)" -ForegroundColor Red
Write-Host "  - ❌ Dates might be incorrect due to timezone/calculation issues" -ForegroundColor Red

Write-Host "`nProposed improvements:" -ForegroundColor Cyan  
Write-Host "  - ✅ Calculate net books sold (sold - refunded)" -ForegroundColor Green
Write-Host "  - ✅ Return net revenue (total_amount - voucher_discount_amount)" -ForegroundColor Green
Write-Host "  - ✅ Properly handle refund statuses (APPROVED, COMPLETED)" -ForegroundColor Green

Write-Host "`n🚀 Ready to proceed with code changes!" -ForegroundColor Green
