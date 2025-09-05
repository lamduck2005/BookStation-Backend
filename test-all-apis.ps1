# Test all 3 APIs: cart/items, orders, counter-sales
Write-Host "Testing User 2 flash sale validation across all APIs..." -ForegroundColor Cyan

# User 2 token
$headers = @{
    "Authorization" = "Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIyIiwiaWF0IjoxNzM2OTU5OTEyLCJleHAiOjE3MzgwODY0MDB9.sWWF0oOpKo5tJo-82hJH9y7rWa8fZFxQJvksQ0EKTxs"
    "Content-Type" = "application/json"
}

# Clear cart first
try {
    Write-Host "`n1. Clearing User 2 cart..." -ForegroundColor Yellow
    $clearResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/carts/user/2/clear" -Method DELETE -Headers $headers
    Write-Host "   Cart cleared: $($clearResponse.message)" -ForegroundColor Green
} catch {
    Write-Host "   Failed to clear cart: $($_.Exception.Message)" -ForegroundColor Red
}

# Test 1: Add to cart - should succeed (1 pending + 1 new = 2 <= 6)
Write-Host "`n2. Testing /api/carts/items (1 item)..." -ForegroundColor Yellow
$cartBody = @{
    userId = 2
    bookId = 1
    quantity = 1
} | ConvertTo-Json

try {
    $cartResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/carts/items" -Method POST -Headers $headers -Body $cartBody
    Write-Host "   Cart API (1 item): SUCCESS" -ForegroundColor Green
} catch {
    Write-Host "   Cart API (1 item): FAILED" -ForegroundColor Red
    if ($_.Exception.Response) {
        $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
        $errorBody = $reader.ReadToEnd()
        Write-Host "   Error: $errorBody" -ForegroundColor Red
    }
}

# Test 2: Add 6 more to cart - should fail (1 pending + 1 existing + 6 new = 8 > 6)
Write-Host "`n3. Testing /api/carts/items (6 more items - should fail)..." -ForegroundColor Yellow
$cartBody2 = @{
    userId = 2
    bookId = 1
    quantity = 6
} | ConvertTo-Json

try {
    $cartResponse2 = Invoke-RestMethod -Uri "http://localhost:8080/api/carts/items" -Method POST -Headers $headers -Body $cartBody2
    Write-Host "   Cart API (6 more): UNEXPECTED SUCCESS" -ForegroundColor Red
} catch {
    Write-Host "   Cart API (6 more): CORRECTLY FAILED" -ForegroundColor Green
    if ($_.Exception.Response) {
        $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
        $errorBody = $reader.ReadToEnd()
        Write-Host "   Error: $errorBody" -ForegroundColor Yellow
    }
}

# Test 3: Direct order API - should fail if quantity is too high
Write-Host "`n4. Testing /api/orders (8 items - should fail)..." -ForegroundColor Yellow
$orderBody = @{
    userId = 2
    addressId = 1
    orderDetails = @(
        @{
            bookId = 1
            quantity = 8
            flashSaleItemId = 1
            unitPrice = 68000
        }
    )
    paymentMethod = "CASH"
    orderType = "ONLINE"
    subtotal = 544000
    totalAmount = 544000
} | ConvertTo-Json -Depth 3

try {
    $orderResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/orders" -Method POST -Headers $headers -Body $orderBody
    Write-Host "   Order API (8 items): UNEXPECTED SUCCESS" -ForegroundColor Red
} catch {
    Write-Host "   Order API (8 items): CORRECTLY FAILED" -ForegroundColor Green
    if ($_.Exception.Response) {
        $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
        $errorBody = $reader.ReadToEnd()
        Write-Host "   Error: $errorBody" -ForegroundColor Yellow
    }
}

# Test 4: Counter sale API - should fail if quantity is too high
Write-Host "`n5. Testing /api/counter-sales (8 items - should fail)..." -ForegroundColor Yellow
$counterBody = @{
    userId = 2
    customerName = "Test Customer"
    customerPhone = "0123456789"
    orderDetails = @(
        @{
            bookId = 1
            quantity = 8
            flashSaleItemId = 1
            unitPrice = 68000
        }
    )
    subtotal = 544000
    totalAmount = 544000
    staffId = 1
} | ConvertTo-Json -Depth 3

try {
    $counterResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/counter-sales" -Method POST -Headers $headers -Body $counterBody
    Write-Host "   Counter Sale API (8 items): UNEXPECTED SUCCESS" -ForegroundColor Red
} catch {
    Write-Host "   Counter Sale API (8 items): CORRECTLY FAILED" -ForegroundColor Green
    if ($_.Exception.Response) {
        $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
        $errorBody = $reader.ReadToEnd()
        Write-Host "   Error: $errorBody" -ForegroundColor Yellow
    }
}

Write-Host "`nTesting completed!" -ForegroundColor Cyan
