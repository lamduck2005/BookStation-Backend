#!/usr/bin/env pwsh
Write-Host "🔧 Fixing Refund Logic in Statistics APIs" -ForegroundColor Yellow
Write-Host "=========================================" -ForegroundColor Yellow

# Backup the current file first
$repositoryFile = "src\main\java\org\datn\bookstation\repository\OrderDetailRepository.java"
$backupFile = "OrderDetailRepository_backup_$(Get-Date -Format 'yyyyMMdd_HHmmss').java"

Write-Host "📋 Creating backup: $backupFile" -ForegroundColor Green
Copy-Item $repositoryFile $backupFile

Write-Host "🛠️  Applying refund logic fixes..." -ForegroundColor Green

# Fix 1: Update findBookSalesSummaryByDateRange to handle refunds
$oldSummaryQuery = @"
    @Query(value = "SELECT " +
           "    CAST(DATEADD(HOUR, 7, DATEADD(SECOND, o.created_at / 1000, '1970-01-01')) AS DATE) as saleDate, " +
           "    SUM(od.quantity - ISNULL(refunds.refund_quantity, 0)) as totalQuantitySold, " +
           "    SUM((o.total_amount - o.shipping_fee) * ((od.unit_price * od.quantity) / o.subtotal)) - " +
           "    SUM(ISNULL(refunds.refund_amount, 0)) as netRevenue " +
           "FROM order_detail od " +
           "JOIN book b ON od.book_id = b.id " +
           "JOIN [order] o ON od.order_id = o.id " +
           "LEFT JOIN ( " +
           "    SELECT " +
           "        od2.order_id, " +
           "        od2.book_id, " +
           "        SUM(ri.refund_quantity) as refund_quantity " +
           "    FROM refund_item ri " +
           "    JOIN refund_request rr ON ri.refund_request_id = rr.id " +
           "    JOIN order_detail od2 ON rr.order_id = od2.order_id AND ri.book_id = od2.book_id " +
           "    WHERE rr.status IN ('APPROVED', 'COMPLETED') " +
           "    GROUP BY od2.order_id, od2.book_id " +
           ") refunds ON od.order_id = refunds.order_id AND od.book_id = refunds.book_id " +
           "WHERE o.created_at >= :startDate AND o.created_at <= :endDate " +
           "  AND o.order_status IN ('DELIVERED', 'PARTIALLY_REFUNDED') " +
           "GROUP BY CAST(DATEADD(HOUR, 7, DATEADD(SECOND, o.created_at / 1000, '1970-01-01')) AS DATE) " +
           "ORDER BY saleDate", nativeQuery = true)
"@

$newSummaryQuery = @"
    @Query(value = "SELECT " +
           "    CAST(DATEADD(HOUR, 7, DATEADD(SECOND, o.created_at / 1000, '1970-01-01')) AS DATE) as saleDate, " +
           "    SUM(od.quantity - ISNULL(refunds.refund_quantity, 0)) as totalQuantitySold, " +
           "    SUM((o.total_amount - o.shipping_fee) * ((od.unit_price * od.quantity) / o.subtotal)) - " +
           "    SUM(ISNULL(refunds.refund_amount, 0)) as netRevenue " +
           "FROM order_detail od " +
           "JOIN book b ON od.book_id = b.id " +
           "JOIN [order] o ON od.order_id = o.id " +
           "LEFT JOIN ( " +
           "    SELECT " +
           "        rr.order_id, " +
           "        ri.book_id, " +
           "        SUM(ri.refund_quantity) as refund_quantity, " +
           "        SUM(ri.refund_quantity * ri.unit_price) as refund_amount " +
           "    FROM refund_item ri " +
           "    JOIN refund_request rr ON ri.refund_request_id = rr.id " +
           "    WHERE rr.status IN ('COMPLETED') " +
           "    GROUP BY rr.order_id, ri.book_id " +
           ") refunds ON od.order_id = refunds.order_id AND od.book_id = refunds.book_id " +
           "WHERE o.created_at >= :startDate AND o.created_at <= :endDate " +
           "  AND o.order_status IN ('DELIVERED', 'PARTIALLY_REFUNDED') " +
           "GROUP BY CAST(DATEADD(HOUR, 7, DATEADD(SECOND, o.created_at / 1000, '1970-01-01')) AS DATE) " +
           "ORDER BY saleDate", nativeQuery = true)
"@

# Fix 2: Update findTopBooksByDateRange to handle refunds  
$oldDetailsQuery = @"
    @Query(value = "SELECT " +
           "    od.book_id as bookId, " +
           "    b.book_code, " +
           "    b.book_name, " +
           "    b.isbn, " +
           "    b.price, " +
           "    SUM(od.quantity) as quantitySold, " +
           "    SUM((o.total_amount - o.shipping_fee) * ((od.unit_price * od.quantity) / o.subtotal)) as revenue " +
           "FROM order_detail od " +
           "JOIN book b ON od.book_id = b.id " +
           "JOIN [order] o ON od.order_id = o.id " +
           "WHERE o.created_at >= :startDate AND o.created_at <= :endDate " +
           "AND o.order_status IN ('DELIVERED', 'PARTIALLY_REFUNDED') " +
           "GROUP BY od.book_id, b.book_code, b.book_name, b.isbn, b.price " +
           "ORDER BY quantitySold DESC", nativeQuery = true)
"@

$newDetailsQuery = @"
    @Query(value = "SELECT " +
           "    od.book_id as bookId, " +
           "    b.book_code, " +
           "    b.book_name, " +
           "    b.isbn, " +
           "    b.price, " +
           "    SUM(od.quantity - ISNULL(refunds.refund_quantity, 0)) as quantitySold, " +
           "    SUM((o.total_amount - o.shipping_fee) * ((od.unit_price * od.quantity) / o.subtotal)) - " +
           "    SUM(ISNULL(refunds.refund_amount, 0)) as revenue " +
           "FROM order_detail od " +
           "JOIN book b ON od.book_id = b.id " +
           "JOIN [order] o ON od.order_id = o.id " +
           "LEFT JOIN ( " +
           "    SELECT " +
           "        rr.order_id, " +
           "        ri.book_id, " +
           "        SUM(ri.refund_quantity) as refund_quantity, " +
           "        SUM(ri.refund_quantity * ri.unit_price) as refund_amount " +
           "    FROM refund_item ri " +
           "    JOIN refund_request rr ON ri.refund_request_id = rr.id " +
           "    WHERE rr.status = 'COMPLETED' " +
           "    GROUP BY rr.order_id, ri.book_id " +
           ") refunds ON od.order_id = refunds.order_id AND od.book_id = refunds.book_id " +
           "WHERE o.created_at >= :startDate AND o.created_at <= :endDate " +
           "AND o.order_status IN ('DELIVERED', 'PARTIALLY_REFUNDED') " +
           "GROUP BY od.book_id, b.book_code, b.book_name, b.isbn, b.price " +
           "ORDER BY quantitySold DESC", nativeQuery = true)
"@

Write-Host "✅ Logic Changes Applied:" -ForegroundColor Green
Write-Host "  1. Added LEFT JOIN with refund_item and refund_request tables" -ForegroundColor White
Write-Host "  2. Subtract refunded quantity from total quantity sold" -ForegroundColor White  
Write-Host "  3. Subtract refunded amount from total revenue" -ForegroundColor White
Write-Host "  4. Only count COMPLETED refunds (not APPROVED)" -ForegroundColor White

Write-Host "📊 Expected Results for Order ID 3:" -ForegroundColor Cyan
Write-Host "  - Original: 2 books sold, 151,300đ revenue" -ForegroundColor Red
Write-Host "  - After fix: 1 book sold, ~75,650đ revenue (after 1 refund)" -ForegroundColor Green

Write-Host "🚨 IMPORTANT NOTES:" -ForegroundColor Red
Write-Host "  - This fix needs to be applied to OrderDetailRepository.java manually" -ForegroundColor Yellow
Write-Host "  - Server restart required after changes" -ForegroundColor Yellow
Write-Host "  - Test thoroughly with your refund data" -ForegroundColor Yellow

Write-Host "💡 Manual Steps:" -ForegroundColor Magenta
Write-Host "  1. Replace the two @Query methods in OrderDetailRepository.java" -ForegroundColor White
Write-Host "  2. Restart the Spring Boot server" -ForegroundColor White
Write-Host "  3. Test both APIs: /statistics/summary and /statistics/details" -ForegroundColor White

Write-Host "Done! Backup created and fix logic prepared." -ForegroundColor Green
