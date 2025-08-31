# TEST ĐƠN GIẢN: Kiểm tra logic revenue khi hoàn trả thành công
# Đảm bảo net revenue = 0 khi đã hoàn tiền (không âm)

Write-Host "Testing REFUND COMPLETION Logic..." -ForegroundColor Green
Write-Host "===================================" -ForegroundColor Yellow

$apiUrl = "http://localhost:8080/api/order-statistics/overview"

try {
    Write-Host "Calling API: $apiUrl" -ForegroundColor Cyan
    
    $response = Invoke-RestMethod -Uri $apiUrl -Method Get -ContentType "application/json"
    $data = $response.data
    
    Write-Host "`nORDER OVERVIEW RESULTS:" -ForegroundColor Magenta
    Write-Host "======================" -ForegroundColor Gray
    
    Write-Host "Total Orders Today: $($data.totalOrdersToday)" -ForegroundColor White
    Write-Host "Total Orders This Month: $($data.totalOrdersThisMonth)" -ForegroundColor White
    
    Write-Host "`nNET REVENUE:" -ForegroundColor Yellow
    Write-Host "  Today: $($data.netRevenueToday) VND" -ForegroundColor $(if ($data.netRevenueToday -eq 0) {"Green"} elseif ($data.netRevenueToday -lt 0) {"Red"} else {"White"})
    Write-Host "  This Month: $($data.netRevenueThisMonth) VND" -ForegroundColor $(if ($data.netRevenueThisMonth -eq 0) {"Green"} elseif ($data.netRevenueThisMonth -lt 0) {"Red"} else {"White"})
    
    Write-Host "`nREFUND & CANCEL:" -ForegroundColor Yellow  
    Write-Host "  Refunded Today: $($data.refundedOrdersToday)" -ForegroundColor White
    Write-Host "  Refunded This Month: $($data.refundedOrdersThisMonth)" -ForegroundColor White
    Write-Host "  Canceled Today: $($data.canceledOrdersToday)" -ForegroundColor White
    Write-Host "  Canceled This Month: $($data.canceledOrdersThisMonth)" -ForegroundColor White
    
    Write-Host "`nLOGIC VALIDATION:" -ForegroundColor Yellow
    if ($data.netRevenueToday -eq 0 -and $data.refundedOrdersToday -gt 0) {
        Write-Host "  ✅ CORRECT: Net revenue = 0 when refunded successfully" -ForegroundColor Green
    } elseif ($data.netRevenueToday -lt 0) {
        Write-Host "  ❌ ERROR: Net revenue is NEGATIVE (double subtraction bug)" -ForegroundColor Red
        Write-Host "  💡 Fix needed: Remove refunded amount subtraction from net revenue calculation" -ForegroundColor Cyan
    } else {
        Write-Host "  ℹ️  INFO: Net revenue > 0 (orders not fully refunded yet)" -ForegroundColor Yellow
    }
    
} catch {
    Write-Host "❌ Error calling API: $_" -ForegroundColor Red
    Write-Host "Make sure server is running on localhost:8080" -ForegroundColor Yellow
}

Write-Host "`n🔄 Press any key to exit..." -ForegroundColor Gray
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
