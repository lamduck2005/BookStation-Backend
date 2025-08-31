# Test doanh thu thuan dung voi don hang di tu PENDING den DELIVERED
# Roi yeu cau refund de test logic

Write-Host "Testing Net Revenue Logic theo dung luong..." -ForegroundColor Cyan
Write-Host "======================================" -ForegroundColor Yellow

# Tao don hang moi de test 
$createOrderUrl = "http://localhost:8080/api/orders"
$orderData = @{
    userId = 4
    orderType = "ONLINE"
    addressId = 1
    orderDetails = @(
        @{
            bookId = 1
            quantity = 1
            unitPrice = 89000
        }
    )
    paymentMethod = "VNPAY"
    notes = "Test order for net revenue validation"
} | ConvertTo-Json -Depth 3

try {
    Write-Host "Tao don hang moi..." -ForegroundColor Blue
    $newOrderResponse = Invoke-RestMethod -Uri $createOrderUrl -Method Post -Body $orderData -ContentType "application/json"
    $newOrderId = $newOrderResponse.data.id
    Write-Host "Don hang moi tao: ID = $newOrderId" -ForegroundColor Green
    
    # Dung status-transition thay vi PATCH status truc tiep
    Write-Host "Cap nhat trang thai qua status-transition..." -ForegroundColor Blue
    
    # PENDING -> CONFIRMED
    $transitionUrl = "http://localhost:8080/api/orders/$newOrderId/status-transition"
    $confirmData = @{
        orderId = $newOrderId
        currentStatus = "PENDING"
        newStatus = "CONFIRMED"
        performedBy = 1
        reason = "Test transition"
    } | ConvertTo-Json
    
    Write-Host "PENDING -> CONFIRMED..." -ForegroundColor Yellow
    $confirmResp = Invoke-RestMethod -Uri $transitionUrl -Method Post -Body $confirmData -ContentType "application/json"
    Write-Host "Confirmed successfully" -ForegroundColor Green
    
    # CONFIRMED -> SHIPPED
    $shipData = @{
        orderId = $newOrderId
        currentStatus = "CONFIRMED"
        newStatus = "SHIPPED"
        performedBy = 1
        reason = "Test shipping"
    } | ConvertTo-Json
    
    Write-Host "CONFIRMED -> SHIPPED..." -ForegroundColor Yellow
    $shipResp = Invoke-RestMethod -Uri $transitionUrl -Method Post -Body $shipData -ContentType "application/json"
    Write-Host "Shipped successfully" -ForegroundColor Green
    
    # SHIPPED -> DELIVERED
    $deliverData = @{
        orderId = $newOrderId
        currentStatus = "SHIPPED"
        newStatus = "DELIVERED"
        performedBy = 1
        reason = "Test delivery"
    } | ConvertTo-Json
    
    Write-Host "SHIPPED -> DELIVERED..." -ForegroundColor Yellow
    $deliverResp = Invoke-RestMethod -Uri $transitionUrl -Method Post -Body $deliverData -ContentType "application/json"
    Write-Host "Delivered successfully!" -ForegroundColor Green
    
    # Test API overview
    Write-Host "`nTest API Overview sau khi DELIVERED..." -ForegroundColor Blue
    $overviewUrl = "http://localhost:8080/api/order-statistics/overview"
    $overviewResponse = Invoke-RestMethod -Uri $overviewUrl -Method Get -ContentType "application/json"
    $data = $overviewResponse.data
    
    Write-Host "`nKET QUA SAU KHI DELIVERED:" -ForegroundColor Magenta
    Write-Host "======================================" -ForegroundColor Gray
    Write-Host "Total Orders Today: $($data.totalOrdersToday)" -ForegroundColor White
    Write-Host "Net Revenue Today: $($data.netRevenueToday) VND" -ForegroundColor Green
    Write-Host "Refunded Today: $($data.refundedToday)" -ForegroundColor Yellow
    
    if ($data.netRevenueToday -ge 89000) {
        Write-Host "`nDUNG! Don DELIVERED co doanh thu thuan = $($data.netRevenueToday)" -ForegroundColor Green
    } else {
        Write-Host "`nCO THE SAI! Don DELIVERED phai co doanh thu thuan >= 89000" -ForegroundColor Red
    }
    
    # Tao yeu cau hoan tra
    Write-Host "`nTao yeu cau hoan tra..." -ForegroundColor Blue
    $refundRequestUrl = "http://localhost:8080/api/orders/$newOrderId/request-refund"
    $refundData = @{
        userId = 4
        reason = "Test refund for revenue validation"
        refundType = "FULL"
    } | ConvertTo-Json
    
    try {
        $refundResp = Invoke-RestMethod -Uri $refundRequestUrl -Method Post -Body $refundData -ContentType "application/json"
        Write-Host "Yeu cau hoan tra thanh cong" -ForegroundColor Yellow
        
        # Test lai API overview
        $overviewResponse2 = Invoke-RestMethod -Uri $overviewUrl -Method Get -ContentType "application/json"
        $data2 = $overviewResponse2.data
        
        Write-Host "`nKET QUA SAU KHI REFUND_REQUESTED:" -ForegroundColor Magenta
        Write-Host "======================================" -ForegroundColor Gray
        Write-Host "Net Revenue Today: $($data2.netRevenueToday) VND" -ForegroundColor Green
        Write-Host "Refunded Today: $($data2.refundedToday)" -ForegroundColor Yellow
        
        if ($data2.netRevenueToday -eq $data.netRevenueToday) {
            Write-Host "`nDUNG! Doanh thu van la $($data2.netRevenueToday) khi REFUND_REQUESTED" -ForegroundColor Green
            Write-Host "DUNG VI: Chi yeu cau hoan tra chua hoan that su" -ForegroundColor Green
        } else {
            Write-Host "`nSAI! Doanh thu thay doi khi REFUND_REQUESTED" -ForegroundColor Red
        }
        
    } catch {
        Write-Host "Loi tao yeu cau hoan tra: $($_.Exception.Message)" -ForegroundColor Red
        if ($_.ErrorDetails) {
            Write-Host "Details: $($_.ErrorDetails.Message)" -ForegroundColor Red
        }
    }
    
} catch {
    Write-Host "ERROR: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.ErrorDetails) {
        Write-Host "Details: $($_.ErrorDetails.Message)" -ForegroundColor Red
    }
}

Write-Host "`nPress Enter to exit..."
Read-Host
