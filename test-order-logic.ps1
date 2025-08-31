# Test Order Overview Logic với nhiều trạng thái
# Kiểm tra doanh thu thuần theo từng trạng thái đơn hàng

Write-Host "Testing Order Overview Logic..." -ForegroundColor Cyan
Write-Host "======================================" -ForegroundColor Yellow

# Get current orders first
$ordersUrl = "http://localhost:8080/api/orders?page=0&size=10"
Write-Host "Getting current orders..." -ForegroundColor Blue

try {
    $ordersResponse = Invoke-RestMethod -Uri $ordersUrl -Method Get -ContentType "application/json"
    $orders = $ordersResponse.data.content
    
    Write-Host "Found $($orders.Count) orders:" -ForegroundColor Green
    foreach ($order in $orders) {
        Write-Host "  Order ID: $($order.id), Code: $($order.code), Status: $($order.orderStatus), Subtotal: $($order.subtotal)" -ForegroundColor White
    }
    
    # Test API Overview
    Write-Host "`nTesting Overview API..." -ForegroundColor Blue
    $overviewUrl = "http://localhost:8080/api/order-statistics/overview"
    $overviewResponse = Invoke-RestMethod -Uri $overviewUrl -Method Get -ContentType "application/json"
    
    $data = $overviewResponse.data
    
    Write-Host "`nCURRENT OVERVIEW RESULT:" -ForegroundColor Magenta
    Write-Host "======================================" -ForegroundColor Gray
    Write-Host "Total Orders Today: $($data.totalOrdersToday)" -ForegroundColor White
    Write-Host "Net Revenue Today: $($data.netRevenueToday) VND" -ForegroundColor Green
    Write-Host "Refunded Orders Today: $($data.refundedOrdersToday)" -ForegroundColor Red
    Write-Host "Canceled Orders Today: $($data.canceledOrdersToday)" -ForegroundColor Red
    
    Write-Host "`nLOGIC EXPLANATION:" -ForegroundColor Yellow
    Write-Host "======================================" -ForegroundColor Gray
    Write-Host "1. Don REFUND_REQUESTED chua thuc su hoan tra" -ForegroundColor Cyan
    Write-Host "2. Chi don DELIVERED moi duoc tinh doanh thu thuan" -ForegroundColor Cyan
    Write-Host "3. Ket qua hien tai la DUNG: Net Revenue = 0 vi don chua DELIVERED" -ForegroundColor Green
    
    Write-Host "`nNEU MUON TEST DON DELIVERED:" -ForegroundColor Yellow
    Write-Host "Can update trang thai don hang thanh DELIVERED trong database" -ForegroundColor Cyan
    
} catch {
    Write-Host "ERROR: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.ErrorDetails) {
        Write-Host "Details: $($_.ErrorDetails.Message)" -ForegroundColor Red
    }
}

Write-Host "`nPress Enter to exit..."
Read-Host
