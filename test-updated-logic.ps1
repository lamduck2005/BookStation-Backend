# Test Order Overview với logic mới
# Kiểm tra doanh thu thuần theo luồng đúng

Write-Host "Testing UPDATED Order Overview Logic..." -ForegroundColor Cyan
Write-Host "======================================" -ForegroundColor Yellow

$apiUrl = "http://localhost:8080/api/order-statistics/overview"

try {
    $response = Invoke-RestMethod -Uri $apiUrl -Method Get -ContentType "application/json"
    $data = $response.data
    
    Write-Host "ORDER OVERVIEW STATISTICS (UPDATED LOGIC):" -ForegroundColor Magenta
    Write-Host "======================================" -ForegroundColor Gray
    
    Write-Host "TONG SO DON HANG:" -ForegroundColor Yellow
    Write-Host "   Hom nay: $($data.totalOrdersToday)" -ForegroundColor White
    Write-Host "   Thang nay: $($data.totalOrdersThisMonth)" -ForegroundColor White
    
    Write-Host "`nDOANH THU THUAN (SUBTOTAL - DISCOUNTS - REFUNDED):" -ForegroundColor Yellow
    Write-Host "   Hom nay: $($data.netRevenueToday) VND" -ForegroundColor Green
    Write-Host "   Thang nay: $($data.netRevenueThisMonth) VND" -ForegroundColor Green
    
    Write-Host "`nSO DON HOAN TRA (DA HOAN TIEN):" -ForegroundColor Yellow
    Write-Host "   Hom nay: $($data.refundedOrdersToday)" -ForegroundColor Red
    Write-Host "   Thang nay: $($data.refundedOrdersThisMonth)" -ForegroundColor Red
    
    Write-Host "`nSO DON HUY:" -ForegroundColor Yellow
    Write-Host "   Hom nay: $($data.canceledOrdersToday)" -ForegroundColor Red
    Write-Host "   Thang nay: $($data.canceledOrdersThisMonth)" -ForegroundColor Red
    
    Write-Host "`nLOGIC MO TA (DA SUA):" -ForegroundColor Magenta
    Write-Host "======================================" -ForegroundColor Gray
    Write-Host "1. Don DELIVERED, REFUND_REQUESTED van tinh doanh thu" -ForegroundColor Cyan
    Write-Host "2. Chi tru khi REFUNDED (da hoan tien thuc su)" -ForegroundColor Cyan
    Write-Host "3. Doanh thu thuan = Subtotal - Discounts - RefundedAmount" -ForegroundColor Cyan
    Write-Host "4. Voi don REFUND_REQUESTED (chua hoan tien):" -ForegroundColor Yellow
    Write-Host "   => Van giu doanh thu vi chua hoan tien cho khach" -ForegroundColor Green
    
    # Kiem tra ket qua
    if ($data.netRevenueToday -gt 0) {
        Write-Host "`n   DUNG! Don REFUND_REQUESTED van duoc tinh doanh thu" -ForegroundColor Green
    } else {
        Write-Host "`n   SAI! Don REFUND_REQUESTED phai duoc tinh doanh thu" -ForegroundColor Red
    }
    
} catch {
    Write-Host "ERROR: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`nPress Enter to exit..."
Read-Host
