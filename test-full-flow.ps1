# Test doanh thu thuan dung voi don hang da DELIVERED
# Tao don hang moi de test logic

Write-Host "Testing Net Revenue Logic với don hang DELIVERED..." -ForegroundColor Cyan
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
    notes = "Test order for revenue calculation"
} | ConvertTo-Json -Depth 3

try {
    Write-Host "Tao don hang moi..." -ForegroundColor Blue
    $newOrderResponse = Invoke-RestMethod -Uri $createOrderUrl -Method Post -Body $orderData -ContentType "application/json"
    $newOrderId = $newOrderResponse.data.id
    Write-Host "Don hang moi tao: ID = $newOrderId" -ForegroundColor Green
    
    # Cap nhat trang thai len DELIVERED
    Write-Host "Cap nhat trang thai len DELIVERED..." -ForegroundColor Blue
    
    # PENDING -> CONFIRMED
    $confirmUrl = "http://localhost:8080/api/orders/$newOrderId/status"
    $confirmBody = @{newStatus="CONFIRMED"; staffId=1} | ConvertTo-Json
    Invoke-RestMethod -Uri $confirmUrl -Method Patch -Body $confirmBody -ContentType "application/json"
    Write-Host "Chuyen sang CONFIRMED" -ForegroundColor Yellow
    
    # CONFIRMED -> SHIPPED  
    $shipBody = @{newStatus="SHIPPED"; staffId=1} | ConvertTo-Json
    Invoke-RestMethod -Uri $confirmUrl -Method Patch -Body $shipBody -ContentType "application/json"
    Write-Host "Chuyen sang SHIPPED" -ForegroundColor Yellow
    
    # SHIPPED -> DELIVERED
    $deliverBody = @{newStatus="DELIVERED"; staffId=1} | ConvertTo-Json
    Invoke-RestMethod -Uri $confirmUrl -Method Patch -Body $deliverBody -ContentType "application/json"
    Write-Host "Chuyen sang DELIVERED" -ForegroundColor Green
    
    # Test API overview
    Write-Host "`nTest API Overview sau khi DELIVERED..." -ForegroundColor Blue
    $overviewUrl = "http://localhost:8080/api/order-statistics/overview"
    $overviewResponse = Invoke-RestMethod -Uri $overviewUrl -Method Get -ContentType "application/json"
    $data = $overviewResponse.data
    
    Write-Host "`nKET QUA SAU KHI DELIVERED:" -ForegroundColor Magenta
    Write-Host "======================================" -ForegroundColor Gray
    Write-Host "Total Orders Today: $($data.totalOrdersToday)" -ForegroundColor White
    Write-Host "Net Revenue Today: $($data.netRevenueToday) VND" -ForegroundColor Green
    
    if ($data.netRevenueToday -gt 0) {
        Write-Host "`nDUNG! Don DELIVERED co doanh thu thuan" -ForegroundColor Green
    } else {
        Write-Host "`nSAI! Don DELIVERED phai co doanh thu thuan" -ForegroundColor Red
    }
    
    # Chuyen sang REFUND_REQUESTED
    Write-Host "`nTao yeu cau hoan tra..." -ForegroundColor Blue
    $refundRequestUrl = "http://localhost:8080/api/orders/$newOrderId/request-refund"
    $refundData = @{
        userId = 4
        reason = "Test refund"
        refundType = "FULL"
    } | ConvertTo-Json
    
    try {
        Invoke-RestMethod -Uri $refundRequestUrl -Method Post -Body $refundData -ContentType "application/json"
        Write-Host "Yeu cau hoan tra thanh cong" -ForegroundColor Yellow
        
        # Test lai API overview
        $overviewResponse2 = Invoke-RestMethod -Uri $overviewUrl -Method Get -ContentType "application/json"
        $data2 = $overviewResponse2.data
        
        Write-Host "`nKET QUA SAU KHI REFUND_REQUESTED:" -ForegroundColor Magenta
        Write-Host "======================================" -ForegroundColor Gray
        Write-Host "Net Revenue Today: $($data2.netRevenueToday) VND" -ForegroundColor Green
        
        if ($data2.netRevenueToday -eq $data.netRevenueToday) {
            Write-Host "DUNG! Doanh thu van giu nguyen khi REFUND_REQUESTED" -ForegroundColor Green
        } else {
            Write-Host "SAI! Doanh thu thay doi khi REFUND_REQUESTED" -ForegroundColor Red
        }
        
    } catch {
        Write-Host "Loi tao yeu cau hoan tra: $($_.Exception.Message)" -ForegroundColor Red
    }
    
} catch {
    Write-Host "ERROR: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.ErrorDetails) {
        Write-Host "Details: $($_.ErrorDetails.Message)" -ForegroundColor Red
    }
}

Write-Host "`nPress Enter to exit..."
Read-Host
