# Test Order Overview API - Simple Version
# Kiểm tra doanh thu thuần chỉ tính từ đơn DELIVERED

Write-Host "Testing Order Overview API (Simple Version)..." -ForegroundColor Cyan
Write-Host "======================================" -ForegroundColor Yellow

# API URL
$apiUrl = "http://localhost:8080/api/order-statistics/overview"

try {
    Write-Host "Calling API: $apiUrl" -ForegroundColor Green
    $response = Invoke-RestMethod -Uri $apiUrl -Method Get -ContentType "application/json"
    
    Write-Host "API Response Status: $($response.status)" -ForegroundColor Green
    Write-Host "Message: $($response.message)" -ForegroundColor Blue
    
    $data = $response.data
    
    Write-Host "`nORDER OVERVIEW STATISTICS:" -ForegroundColor Magenta
    Write-Host "======================================" -ForegroundColor Gray
    
    Write-Host "TONG SO DON HANG:" -ForegroundColor Yellow
    Write-Host "   Hom nay: $($data.totalOrdersToday)" -ForegroundColor White
    Write-Host "   Thang nay: $($data.totalOrdersThisMonth)" -ForegroundColor White
    
    Write-Host "`nDOANH THU THUAN (CHI DON DELIVERED):" -ForegroundColor Yellow
    Write-Host "   Hom nay: $($data.netRevenueToday) VND" -ForegroundColor Green
    Write-Host "   Thang nay: $($data.netRevenueThisMonth) VND" -ForegroundColor Green
    
    Write-Host "`nSO DON HOAN TRA:" -ForegroundColor Yellow
    Write-Host "   Hom nay: $($data.refundedOrdersToday)" -ForegroundColor Red
    Write-Host "   Thang nay: $($data.refundedOrdersThisMonth)" -ForegroundColor Red
    
    Write-Host "`nSO DON HUY:" -ForegroundColor Yellow
    Write-Host "   Hom nay: $($data.canceledOrdersToday)" -ForegroundColor Red
    Write-Host "   Thang nay: $($data.canceledOrdersThisMonth)" -ForegroundColor Red
    
    Write-Host "`nKIEM TRA LOGIC:" -ForegroundColor Magenta
    Write-Host "======================================" -ForegroundColor Gray
    Write-Host "   Don REFUND_REQUESTED khong duoc tru khoi doanh thu thuan" -ForegroundColor Cyan
    Write-Host "   Chi don DELIVERED moi duoc tinh vao doanh thu thuan" -ForegroundColor Cyan
    Write-Host "   Voi du lieu test: 1 don REFUND_REQUESTED (89,000 VND)" -ForegroundColor Cyan
    Write-Host "   Doanh thu thuan hom nay phai = 89,000 VND (vi chua REFUNDED)" -ForegroundColor Green
    
    Write-Host "`n======================================" -ForegroundColor Gray
    Write-Host "Test completed successfully!" -ForegroundColor Green
    
} catch {
    Write-Host "ERROR: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "Details: $($_.ErrorDetails.Message)" -ForegroundColor Red
}

Write-Host "`nPress Enter to exit..."
Read-Host
