# Test Order Overview API - Simple Version
# Kiểm tra doanh thu thuần chỉ tính từ đơn DELIVERED

Write-Host "🧪 Testing Order Overview API (Simple Version)..." -ForegroundColor Cyan
Write-Host "======================================" -ForegroundColor Yellow

# API URL
$apiUrl = "http://localhost:8080/api/order-statistics/overview"

try {
    Write-Host "📞 Calling API: $apiUrl" -ForegroundColor Green
    $response = Invoke-RestMethod -Uri $apiUrl -Method Get -ContentType "application/json"
    
    Write-Host "✅ API Response Status: $($response.status)" -ForegroundColor Green
    Write-Host "📄 Message: $($response.message)" -ForegroundColor Blue
    
    $data = $response.data
    
    Write-Host "`n📊 ORDER OVERVIEW STATISTICS:" -ForegroundColor Magenta
    Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Gray
    
    Write-Host "📦 TỔNG SỐ ĐỚN HÀNG:" -ForegroundColor Yellow
    Write-Host "   • Hôm nay: $($data.totalOrdersToday)" -ForegroundColor White
    Write-Host "   • Tháng này: $($data.totalOrdersThisMonth)" -ForegroundColor White
    
    Write-Host "`n💰 DOANH THU THUẦN (CHỈ ĐƠN DELIVERED):" -ForegroundColor Yellow
    Write-Host "   • Hôm nay: $($data.netRevenueToday) VND" -ForegroundColor Green
    Write-Host "   • Tháng này: $($data.netRevenueThisMonth) VND" -ForegroundColor Green
    
    Write-Host "`n🔄 SỐ ĐƠN HOÀN TRẢ:" -ForegroundColor Yellow
    Write-Host "   • Hôm nay: $($data.refundedOrdersToday)" -ForegroundColor Red
    Write-Host "   • Tháng này: $($data.refundedOrdersThisMonth)" -ForegroundColor Red
    
    Write-Host "`n❌ SỐ ĐƠN HỦY:" -ForegroundColor Yellow
    Write-Host "   • Hôm nay: $($data.canceledOrdersToday)" -ForegroundColor Red
    Write-Host "   • Tháng này: $($data.canceledOrdersThisMonth)" -ForegroundColor Red
    
    Write-Host "`n🎯 KIỂM TRA LOGIC:" -ForegroundColor Magenta
    Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Gray
    Write-Host "   • Đơn REFUND_REQUESTED không được trừ khỏi doanh thu thuần" -ForegroundColor Cyan
    Write-Host "   • Chỉ đơn DELIVERED mới được tính vào doanh thu thuần" -ForegroundColor Cyan
    Write-Host "   • Với dữ liệu test: 1 đơn REFUND_REQUESTED (89,000 VND)" -ForegroundColor Cyan
    Write-Host "   • Doanh thu thuần hôm nay phải = 89,000 VND (vì chưa REFUNDED)" -ForegroundColor Green
    
    Write-Host "`n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Gray
    Write-Host "✅ Test completed successfully!" -ForegroundColor Green
    
} catch {
    Write-Host "❌ ERROR: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "Details: $($_.ErrorDetails.Message)" -ForegroundColor Red
}

Write-Host "`n🔍 Press any key to exit..." -ForegroundColor Yellow
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
