# Continue testing from CONFIRMED to DELIVERED
Write-Host "Continue testing from CONFIRMED..." -ForegroundColor Cyan

$orderId = 3
$transitionUrl = "http://localhost:8080/api/orders/$orderId/status-transition"

try {
    # CONFIRMED -> SHIPPED
    Write-Host "CONFIRMED -> SHIPPED..." -ForegroundColor Blue
    $shipData = @{
        orderId = $orderId
        currentStatus = "CONFIRMED"
        newStatus = "SHIPPED"
        performedBy = 1
        reason = "Test shipping transition"
    } | ConvertTo-Json
    
    $shipResp = Invoke-RestMethod -Uri $transitionUrl -Method Post -Body $shipData -ContentType "application/json"
    Write-Host "SHIPPED successfully!" -ForegroundColor Green
    
    # SHIPPED -> DELIVERED
    Write-Host "SHIPPED -> DELIVERED..." -ForegroundColor Blue
    $deliverData = @{
        orderId = $orderId
        currentStatus = "SHIPPED"
        newStatus = "DELIVERED"
        performedBy = 1
        reason = "Test delivery transition"
    } | ConvertTo-Json
    
    $deliverResp = Invoke-RestMethod -Uri $transitionUrl -Method Post -Body $deliverData -ContentType "application/json"
    Write-Host "DELIVERED successfully!" -ForegroundColor Green
    
    # Test API overview sau khi DELIVERED
    Write-Host "`nTesting Overview API after DELIVERED..." -ForegroundColor Blue
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
        Write-Host "`nSAI! Don DELIVERED phai co doanh thu thuan >= 89000" -ForegroundColor Red
    }
    
    # Tao yeu cau hoan tra
    Write-Host "`nTao yeu cau hoan tra..." -ForegroundColor Blue
    $refundRequestUrl = "http://localhost:8080/api/orders/$orderId/request-refund"
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
            Write-Host "`nPERFECT! Doanh thu van la $($data2.netRevenueToday) khi REFUND_REQUESTED" -ForegroundColor Green
            Write-Host "DUNG VI: Chi yeu cau hoan tra, chua hoan that su" -ForegroundColor Green
            Write-Host "Logic revenue calculation HOAN TOAN DUNG!" -ForegroundColor Green
        } else {
            Write-Host "`nSAI! Doanh thu thay doi khi REFUND_REQUESTED" -ForegroundColor Red
            Write-Host "Before: $($data.netRevenueToday)" -ForegroundColor Red
            Write-Host "After: $($data2.netRevenueToday)" -ForegroundColor Red
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
