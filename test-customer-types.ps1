# Test Counter Sales Customer Type Differentiation
param(
    [string]$BaseUrl = "http://localhost:8080"
)

Write-Host "🧪 Testing Counter Sales Customer Type Differentiation..." -ForegroundColor Yellow
Write-Host "=================================================" -ForegroundColor Yellow

# Common test data - assuming book ID 1 has flash sale
$bookId = 1
$quantity = 1

Write-Host ""
Write-Host "📋 Test 1: Walk-in Customer (userId = null)" -ForegroundColor Cyan
Write-Host "Expected: Regular price, no flash sale validation" -ForegroundColor Gray

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
Write-Host "Expected: Flash sale price if available, cumulative validation" -ForegroundColor Gray

# First get flash sale info for book
try {
    $flashSaleInfo = Invoke-RestMethod -Uri "$BaseUrl/api/flash-sales/book/$bookId" -Method GET
    $flashSaleItemId = $flashSaleInfo.id
    $flashSalePrice = $flashSaleInfo.currentPrice
    Write-Host "📊 Flash Sale Info: ID=$flashSaleItemId, Price=$flashSalePrice" -ForegroundColor Yellow
} catch {
    Write-Host "⚠️ No flash sale found for book $bookId" -ForegroundColor Yellow
    $flashSaleItemId = $null
    $flashSalePrice = 120000
}

$registeredRequest = @{
    userId = 2
    orderDetails = @(
        @{
            bookId = $bookId
            quantity = $quantity
            unitPrice = $flashSalePrice
            flashSaleItemId = $flashSaleItemId
        }
    )
    shippingAddress = "Counter Sale"
    paymentMethod = "CASH"
    totalAmount = $flashSalePrice
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
Write-Host "📋 Test 3: Stock Validation Difference" -ForegroundColor Cyan
Write-Host "Walk-in uses book stock, Registered uses flash sale stock" -ForegroundColor Gray

# Test large quantity to trigger stock validation
$largeQuantity = 100

$walkInLargeRequest = @{
    userId = $null
    orderDetails = @(
        @{
            bookId = $bookId
            quantity = $largeQuantity
            unitPrice = 150000
            flashSaleItemId = $null
        }
    )
    shippingAddress = "Counter Sale"
    paymentMethod = "CASH"
    totalAmount = ($largeQuantity * 150000)
} | ConvertTo-Json -Depth 3

try {
    $walkInLargeResponse = Invoke-RestMethod -Uri "$BaseUrl/api/counter-sales/calculate" -Method POST -ContentType "application/json" -Body $walkInLargeRequest
    Write-Host "✅ Walk-in large quantity ($largeQuantity) - PASSED stock validation" -ForegroundColor Green
} catch {
    Write-Host "❌ Walk-in large quantity ($largeQuantity) - FAILED: $($_.Exception.Message)" -ForegroundColor Red
}

if ($flashSaleItemId) {
    $registeredLargeRequest = @{
        userId = 2
        orderDetails = @(
            @{
                bookId = $bookId
                quantity = $largeQuantity
                unitPrice = $flashSalePrice
                flashSaleItemId = $flashSaleItemId
            }
        )
        shippingAddress = "Counter Sale"
        paymentMethod = "CASH"
        totalAmount = ($largeQuantity * $flashSalePrice)
    } | ConvertTo-Json -Depth 3

    try {
        $registeredLargeResponse = Invoke-RestMethod -Uri "$BaseUrl/api/counter-sales/calculate" -Method POST -ContentType "application/json" -Body $registeredLargeRequest
        Write-Host "✅ Registered large quantity ($largeQuantity) - PASSED flash sale stock validation" -ForegroundColor Green
    } catch {
        Write-Host "❌ Registered large quantity ($largeQuantity) - FAILED: $($_.Exception.Message)" -ForegroundColor Red
    }
}

Write-Host ""
Write-Host "🎯 Summary:" -ForegroundColor Yellow
Write-Host "- Walk-in customers: Regular pricing, book stock validation, no flash sale limits" -ForegroundColor White
Write-Host "- Registered customers: Flash sale pricing, flash sale stock validation, cumulative limits" -ForegroundColor White
Write-Host "=================================================" -ForegroundColor Yellow
