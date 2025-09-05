# Test Counter Sales với logic phân biệt khách hàng FIXED
param(
    [string]$BaseUrl = "http://localhost:8080"
)

Write-Host "🧪 Testing Counter Sales Customer Type Logic - COMPREHENSIVE TEST" -ForegroundColor Yellow
Write-Host "=================================================================" -ForegroundColor Yellow

# Thông tin sách test: bookId=1
$bookId = 1
$regularPrice = 89000
$flashSalePrice = 71200

Write-Host ""
Write-Host "📖 Book Info: ID=$bookId, Regular Price=$regularPrice, Flash Sale Price=$flashSalePrice" -ForegroundColor Cyan

Write-Host ""
Write-Host "🧪 TEST 1: Walk-in Customer (userId = null)" -ForegroundColor Green
Write-Host "Expected: Regular price ($regularPrice), NO flash sale" -ForegroundColor Gray

$walkInRequest = @{
    userId = $null
    customerName = 'Walk-in Customer'
    customerPhone = '0123456789'
    orderDetails = @(
        @{
            bookId = $bookId
            quantity = 1
            unitPrice = $regularPrice  # Frontend gửi giá thường
            # Không gửi flashSaleItemId hoặc isFlashSale
        }
    )
    voucherIds = @()
    subtotal = $regularPrice
    discountAmount = 0
    totalAmount = $regularPrice
    notes = 'Walk-in customer test'
    staffId = 1
} | ConvertTo-Json -Depth 5

try {
    $response = Invoke-RestMethod -Uri "$BaseUrl/api/counter-sales/calculate" -Method POST -ContentType "application/json" -Body $walkInRequest
    $actualPrice = $response.data.items[0].unitPrice
    $isFlashSale = $response.data.items[0].flashSale
    
    if ($actualPrice -eq $regularPrice -and $isFlashSale -eq $false) {
        Write-Host "✅ PASSED: Walk-in customer - Price=$actualPrice, Flash Sale=$isFlashSale" -ForegroundColor Green
    } else {
        Write-Host "❌ FAILED: Walk-in customer - Price=$actualPrice (expected $regularPrice), Flash Sale=$isFlashSale (expected false)" -ForegroundColor Red
    }
} catch {
    Write-Host "❌ FAILED: Walk-in customer calculation error: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""
Write-Host "🧪 TEST 2: Registered Customer (userId = 2)" -ForegroundColor Green  
Write-Host "Expected: Flash sale price ($flashSalePrice), WITH flash sale" -ForegroundColor Gray

$registeredRequest = @{
    userId = 2
    customerName = 'Registered Customer'
    customerPhone = '0987654321'
    orderDetails = @(
        @{
            bookId = $bookId
            quantity = 1
            unitPrice = $flashSalePrice  # Frontend gửi giá flash sale
            # Backend sẽ tự động detect flash sale
        }
    )
    voucherIds = @()
    subtotal = $flashSalePrice
    discountAmount = 0
    totalAmount = $flashSalePrice
    notes = 'Registered customer test'
    staffId = 1
} | ConvertTo-Json -Depth 5

try {
    $response = Invoke-RestMethod -Uri "$BaseUrl/api/counter-sales/calculate" -Method POST -ContentType "application/json" -Body $registeredRequest
    $actualPrice = $response.data.items[0].unitPrice
    $isFlashSale = $response.data.items[0].flashSale
    $savedAmount = $response.data.items[0].savedAmount
    
    if ($actualPrice -eq $flashSalePrice -and $isFlashSale -eq $true) {
        Write-Host "✅ PASSED: Registered customer - Price=$actualPrice, Flash Sale=$isFlashSale, Saved=$savedAmount" -ForegroundColor Green
    } else {
        Write-Host "❌ FAILED: Registered customer - Price=$actualPrice (expected $flashSalePrice), Flash Sale=$isFlashSale (expected true)" -ForegroundColor Red
    }
} catch {
    Write-Host "❌ FAILED: Registered customer calculation error: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""
Write-Host "🧪 TEST 3: Large Quantity Limits" -ForegroundColor Green
Write-Host "Testing quantity=5 for both customer types" -ForegroundColor Gray

# Test walk-in customer với quantity lớn (should PASS)
$walkInLargeRequest = @{
    userId = $null
    customerName = 'Walk-in Customer'
    customerPhone = '0123456789'
    orderDetails = @(
        @{
            bookId = $bookId
            quantity = 5
            unitPrice = $regularPrice
        }
    )
    voucherIds = @()
    subtotal = ($regularPrice * 5)
    discountAmount = 0
    totalAmount = ($regularPrice * 5)
    notes = 'Walk-in large quantity test'
    staffId = 1
} | ConvertTo-Json -Depth 5

try {
    $response = Invoke-RestMethod -Uri "$BaseUrl/api/counter-sales/calculate" -Method POST -ContentType "application/json" -Body $walkInLargeRequest
    Write-Host "✅ PASSED: Walk-in large quantity (5) - No flash sale limits applied" -ForegroundColor Green
} catch {
    Write-Host "❌ FAILED: Walk-in large quantity should pass: $($_.Exception.Message)" -ForegroundColor Red
}

# Test registered customer với quantity lớn (should FAIL due to flash sale limits)
$registeredLargeRequest = @{
    userId = 2
    customerName = 'Registered Customer'
    customerPhone = '0987654321'
    orderDetails = @(
        @{
            bookId = $bookId
            quantity = 5
            unitPrice = $flashSalePrice
        }
    )
    voucherIds = @()
    subtotal = ($flashSalePrice * 5)
    discountAmount = 0
    totalAmount = ($flashSalePrice * 5)
    notes = 'Registered large quantity test'
    staffId = 1
} | ConvertTo-Json -Depth 5

try {
    $response = Invoke-RestMethod -Uri "$BaseUrl/api/counter-sales/calculate" -Method POST -ContentType "application/json" -Body $registeredLargeRequest
    Write-Host "❌ FAILED: Registered large quantity should be rejected due to flash sale limits" -ForegroundColor Red
} catch {
    Write-Host "✅ PASSED: Registered large quantity (5) correctly rejected - Flash sale limits working" -ForegroundColor Green
}

Write-Host ""
Write-Host "🎯 SUMMARY:" -ForegroundColor Yellow
Write-Host "- Walk-in customers: Regular pricing, no flash sale limits, use book stock" -ForegroundColor White
Write-Host "- Registered customers: Auto-detect flash sale, apply limits, use flash sale stock" -ForegroundColor White
Write-Host "=================================================================" -ForegroundColor Yellow
