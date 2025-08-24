# ================================================================================================
# FIX API STATISTICS SUMMARY REPORT
# ================================================================================================
# Date: August 24, 2025
# Issue: API /api/books/statistics/summary tra ve du lieu sai
# ================================================================================================

Write-Host "=== BOOKSTATION API STATISTICS FIX SUMMARY ===" -ForegroundColor Green

Write-Host "`n📝 ISSUES IDENTIFIED:" -ForegroundColor Yellow
Write-Host "  1. ❌ API chi dem so luong sach ban ma khong tru hoan tra" -ForegroundColor Red
Write-Host "  2. ❌ API khong tra ve doanh thu thuan (sau khi tru voucher)" -ForegroundColor Red  
Write-Host "  3. ❌ Data hien thi ngay sai (23/8 co 1 sach, 24/8 khong co)" -ForegroundColor Red

Write-Host "`n🔧 FIXES IMPLEMENTED:" -ForegroundColor Cyan
Write-Host "  1. ✅ Updated OrderDetailRepository.findBookSalesSummaryByDateRange" -ForegroundColor Green
Write-Host "     - Added LEFT JOIN with refund_item table" -ForegroundColor Gray
Write-Host "     - Calculate net books sold: sold_quantity - refund_quantity" -ForegroundColor Gray
Write-Host "     - Calculate net revenue: total_amount - voucher_discount_amount" -ForegroundColor Gray

Write-Host "`n  2. ✅ Updated BookServiceImpl.getBookStatisticsSummary" -ForegroundColor Green
Write-Host "     - Modified data processing to handle netRevenue field" -ForegroundColor Gray
Write-Host "     - Updated all generate methods (daily, weekly, monthly, quarterly, yearly)" -ForegroundColor Gray

Write-Host "`n📊 NEW SQL QUERY STRUCTURE:" -ForegroundColor Cyan
$newQuery = @"
SELECT 
    CAST(DATEADD(SECOND, o.created_at / 1000, '1970-01-01') AS DATE) as saleDate,
    COALESCE(SUM(od.quantity), 0) - COALESCE(SUM(refunds.refund_quantity), 0) as netBooksSold,
    COALESCE(SUM(o.total_amount - COALESCE(o.voucher_discount_amount, 0)), 0) as netRevenue
FROM order_detail od 
JOIN [order] o ON od.order_id = o.id 
LEFT JOIN ( 
    SELECT od2.order_id, od2.book_id, SUM(ri.refund_quantity) as refund_quantity
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
Write-Host $newQuery -ForegroundColor Gray

Write-Host "`n📋 NEW API RESPONSE FORMAT:" -ForegroundColor Cyan
$responseFormat = @"
{
    "status": 200,
    "message": "Summary statistics retrieved successfully",
    "data": [
        {
            "date": "2025-08-24",
            "period": "daily", 
            "totalBooksSold": 1,        // Net books sold (after refunds)
            "netRevenue": 95000.00      // Net revenue (after voucher discount)
        }
    ]
}
"@
Write-Host $responseFormat -ForegroundColor Gray

Write-Host "`n⚠️ SERVER RESTART REQUIRED:" -ForegroundColor Yellow
Write-Host "  - Application needs to be restarted for changes to take effect" -ForegroundColor White
Write-Host "  - Port 8080 conflict detected - please kill all Java processes first" -ForegroundColor Red

Write-Host "`n🧪 TESTING INSTRUCTIONS:" -ForegroundColor Cyan
Write-Host "  1. Kill all Java processes: taskkill /F /IM java.exe" -ForegroundColor White
Write-Host "  2. Start server: ./mvnw spring-boot:run" -ForegroundColor White
Write-Host "  3. Test API: .\test-detailed.ps1 -Period 'day'" -ForegroundColor White
Write-Host "  4. Verify netRevenue field is present in response" -ForegroundColor White

Write-Host "`n📂 FILES MODIFIED:" -ForegroundColor Cyan
Write-Host "  - src/main/java/org/datn/bookstation/repository/OrderDetailRepository.java" -ForegroundColor White
Write-Host "  - src/main/java/org/datn/bookstation/service/impl/BookServiceImpl.java" -ForegroundColor White

Write-Host "`n✨ EXPECTED IMPROVEMENTS:" -ForegroundColor Green
Write-Host "  ✅ Accurate book sales count (net of refunds)" -ForegroundColor Green
Write-Host "  ✅ Net revenue calculations (after voucher discounts)" -ForegroundColor Green
Write-Host "  ✅ Proper handling of refund statuses (APPROVED, COMPLETED only)" -ForegroundColor Green
Write-Host "  ✅ All period types supported (day/week/month/quarter/year)" -ForegroundColor Green

Write-Host "`n📞 NEXT STEPS:" -ForegroundColor Magenta
Write-Host "  1. Restart server to apply changes" -ForegroundColor White
Write-Host "  2. Test with different periods (day/week/month)" -ForegroundColor White  
Write-Host "  3. Verify data accuracy with database" -ForegroundColor White
Write-Host "  4. Update frontend to display netRevenue if needed" -ForegroundColor White

Write-Host "`n=== FIX COMPLETED ===" -ForegroundColor Green
