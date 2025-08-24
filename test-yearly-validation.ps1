# Test validation API với yearly period nhưng khoảng thời gian ngắn (năng nó chỉ báo lỗi thay vì auto-downgrade)
# Trường hợp user đã test: fromDate=2024-01-01, toDate=2025-08-22, period=yearly
# Duration = khoảng 600 ngày, nhỏ hơn 1 năm (365 ngày) nên phải báo lỗi

Write-Host "🧪 Testing Yearly Period Validation - Should Return Error (Not Auto-downgrade)" -ForegroundColor Yellow

# Test với khoảng thời gian ngắn (< 365 ngày) cho yearly period
$response = Invoke-RestMethod -Uri "http://localhost:8080/api/books/statistics/summary?period=year&fromDate=2024-06-01&toDate=2025-01-01" -Method GET -Headers @{
    "Content-Type" = "application/json"
}

if ($response.success -eq $false) {
    Write-Host "✅ PASS: API correctly returned error for insufficient yearly period duration" -ForegroundColor Green
    Write-Host "Error Message: $($response.message)" -ForegroundColor Cyan
} else {
    Write-Host "❌ FAIL: API should have returned error but returned successful response" -ForegroundColor Red
    Write-Host "Response: $($response | ConvertTo-Json -Depth 3)" -ForegroundColor Yellow
}
