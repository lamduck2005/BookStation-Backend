# Test Counter Sales Customer Type Differentiation
param(
    [string]$BaseUrl = "http://localhost:8080"
)

Write-Host "🧪 Testing Counter Sales Customer Type Differentiation..." -ForegroundColor Yellow

$bookId = 1
$quantity = 1

Write-Host ""
Write-Host "📋 Test 1: Walk-in Customer (userId = null)" -ForegroundColor Cyan

$walkInRequest = @{
    userId = $null
    orderDetails = @(
        @{
            bookId = $bookId
            quantity = $quantity
            unitPrice = 150000
            flashSaleItemId = $null
        }
    )
    shippingAddress = "Counter Sale"
    paymentMethod = "CASH"
    totalAmount = 150000
} | ConvertTo-Json -Depth 3

try {
    $walkInResponse = Invoke-RestMethod -Uri "$BaseUrl/api/counter-sales/calculate" -Method POST -ContentType "application/json" -Body $walkInRequest
    Write-Host "✅ Walk-in calculation successful:" -ForegroundColor Green
    Write-Host "   Total: $($walkInResponse.totalAmount)" -ForegroundColor White
    Write-Host "   Flash Sale Applied: $($walkInResponse.orderDetails[0].flashSaleItemId -ne $null)" -ForegroundColor White
} catch {
    Write-Host "❌ Walk-in calculation failed: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""
Write-Host "📋 Test 2: Registered Customer (userId = 2)" -ForegroundColor Cyan

$registeredRequest = @{
    userId = 2
    orderDetails = @(
        @{
            bookId = $bookId
            quantity = $quantity
            unitPrice = 120000
            flashSaleItemId = 1
        }
    )
    shippingAddress = "Counter Sale"
    paymentMethod = "CASH"
    totalAmount = 120000
} | ConvertTo-Json -Depth 3

try {
    $registeredResponse = Invoke-RestMethod -Uri "$BaseUrl/api/counter-sales/calculate" -Method POST -ContentType "application/json" -Body $registeredRequest
    Write-Host "✅ Registered customer calculation successful:" -ForegroundColor Green
    Write-Host "   Total: $($registeredResponse.totalAmount)" -ForegroundColor White
    Write-Host "   Flash Sale Applied: $($registeredResponse.orderDetails[0].flashSaleItemId -ne $null)" -ForegroundColor White
} catch {
    Write-Host "❌ Registered customer calculation failed: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""
Write-Host "🎯 Summary:" -ForegroundColor Yellow
Write-Host "- Walk-in customers: Regular pricing, no flash sale" -ForegroundColor White
Write-Host "- Registered customers: Flash sale pricing and validation" -ForegroundColor White
