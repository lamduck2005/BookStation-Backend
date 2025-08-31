# Test full refund flow để kiểm tra khi nào net revenue bị trừ
Write-Host "Testing full refund flow..." -ForegroundColor Cyan
Write-Host "=============================" -ForegroundColor Yellow

# Get current order info
$getOrderUrl = "http://localhost:8080/api/orders/1"
$orderResp = Invoke-RestMethod -Uri $getOrderUrl -Method Get
$order = $orderResp.data

Write-Host "Current order status: $($order.orderStatus)" -ForegroundColor Green
Write-Host "Current net revenue should be: 326,000 VND" -ForegroundColor Green

# Test API before any status change
$overviewUrl = "http://localhost:8080/api/order-statistics/overview"
$resp1 = Invoke-RestMethod -Uri $overviewUrl -Method Get
Write-Host "`nBEFORE any status change:" -ForegroundColor Blue
Write-Host "Net Revenue Today: $($resp1.data.netRevenueToday) VND" -ForegroundColor Green

Write-Host "`nDự đoán theo luồng:" -ForegroundColor Yellow
Write-Host "- AWAITING_GOODS_RETURN -> GOODS_RECEIVED_FROM_CUSTOMER: Revenue vẫn 326,000" -ForegroundColor Yellow
Write-Host "- GOODS_RECEIVED_FROM_CUSTOMER -> GOODS_RETURNED_TO_WAREHOUSE: Revenue vẫn 326,000" -ForegroundColor Yellow  
Write-Host "- Chỉ khi REFUNDED thực sự: Revenue mới = 0" -ForegroundColor Yellow

Write-Host "`nPress Enter to continue với test..."
Read-Host
