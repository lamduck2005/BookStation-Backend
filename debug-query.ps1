#!/usr/bin/env pwsh

Write-Host "=== DEBUG DATABASE QUERY ===" -ForegroundColor Yellow

# Get current timestamp ranges (last 30 days)
$now = Get-Date
$startDate = $now.AddDays(-30)
$endDate = $now

$startTimestamp = [DateTimeOffset]::new($startDate).ToUnixTimeMilliseconds()
$endTimestamp = [DateTimeOffset]::new($endDate).ToUnixTimeMilliseconds()

Write-Host "Time range for query:" -ForegroundColor Cyan
Write-Host "  Start: $startDate ($startTimestamp)" -ForegroundColor White  
Write-Host "  End: $endDate ($endTimestamp)" -ForegroundColor White

Write-Host "`nSQL Query that should be executed:" -ForegroundColor Cyan
$sqlQuery = @"
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
WHERE o.created_at >= $startTimestamp AND o.created_at <= $endTimestamp
  AND o.order_status IN ('DELIVERED', 'PARTIALLY_REFUNDED') 
GROUP BY CAST(DATEADD(SECOND, o.created_at / 1000, '1970-01-01') AS DATE) 
ORDER BY saleDate
"@

Write-Host $sqlQuery -ForegroundColor Gray

Write-Host "`n=== TESTING API CALL ===" -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "http://localhost:8080/api/books/statistics/summary?period=day" -Method GET
    
    if ($response.data -and $response.data.Count -gt 0) {
        $nonZeroData = $response.data | Where-Object { $_.totalBooksSold -gt 0 }
        
        Write-Host "API Results:" -ForegroundColor Cyan
        Write-Host "  Total points: $($response.data.Count)" -ForegroundColor White
        Write-Host "  Non-zero points: $($nonZeroData.Count)" -ForegroundColor White
        
        if ($nonZeroData.Count -gt 0) {
            Write-Host "  Recent non-zero data:" -ForegroundColor White
            $nonZeroData | Select-Object -Last 3 | ForEach-Object {
                Write-Host "    Date: $($_.date), Books: $($_.totalBooksSold)" -ForegroundColor Gray
                if ($_.netRevenue) {
                    Write-Host "    Revenue: $($_.netRevenue)" -ForegroundColor Gray
                }
            }
        }
        
        # Check today's data
        $today = Get-Date -Format "yyyy-MM-dd"
        $todayData = $response.data | Where-Object { $_.date -eq $today }
        if ($todayData) {
            Write-Host "`n  Today's data ($today):" -ForegroundColor Yellow
            Write-Host "    Books sold: $($todayData.totalBooksSold)" -ForegroundColor White
            if ($todayData.netRevenue) {
                Write-Host "    Net revenue: $($todayData.netRevenue)" -ForegroundColor White
            }
        } else {
            Write-Host "`n  No data for today ($today)" -ForegroundColor Red
        }
    }
    
} catch {
    Write-Host "ERROR: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`nNote: If API still returns old format, server may need complete restart or there might be caching issues." -ForegroundColor Yellow
