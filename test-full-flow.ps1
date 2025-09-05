# Test Full Counter Sales Flow - Create Orders
$baseUrl = "http://localhost:8080"

Write-Host "🧪 Testing Full Counter Sales Flow (Calculate + Create)" -ForegroundColor Yellow

# Test 1: Walk-in customer - Calculate
Write-Host "`n1️⃣ Walk-in Customer - Calculate" -ForegroundColor Cyan
$walkInCalc = @{
    userId = $null
    customerName = 'Walk-in Customer'
    customerPhone = '0123456789'
    orderDetails = @(@{bookId=1; quantity=1; unitPrice=89000})
    voucherIds = @()
    subtotal = 89000
    totalAmount = 89000
    staffId = 1
} | ConvertTo-Json -Depth 5

$calcResponse = Invoke-RestMethod -Uri "$baseUrl/api/counter-sales/calculate" -Method POST -ContentType "application/json" -Body $walkInCalc
Write-Host "   Price: $($calcResponse.data.items[0].unitPrice), Flash Sale: $($calcResponse.data.items[0].flashSale)" -ForegroundColor White

# Test 2: Walk-in customer - Create Order
Write-Host "`n2️⃣ Walk-in Customer - Create Order" -ForegroundColor Cyan
$walkInOrder = @{
    userId = $null
    customerName = 'Walk-in Customer'
    customerPhone = '0123456789'
    orderDetails = @(@{bookId=1; quantity=1; unitPrice=89000})
    voucherIds = @()
    subtotal = 89000
    discountAmount = 0
    totalAmount = 89000
    paymentMethod = 'CASH'
    staffId = 1
    notes = 'Walk-in customer test order'
} | ConvertTo-Json -Depth 5

try {
    $orderResponse = Invoke-RestMethod -Uri "$baseUrl/api/counter-sales" -Method POST -ContentType "application/json" -Body $walkInOrder
    Write-Host "   ✅ Order ID: $($orderResponse.data.orderId), Status: $($orderResponse.data.orderStatus)" -ForegroundColor Green
    Write-Host "   Final Price: $($orderResponse.data.items[0].unitPrice), Flash Sale: $($orderResponse.data.items[0].flashSale)" -ForegroundColor White
} catch {
    Write-Host "   ❌ Error: $($_.Exception.Message)" -ForegroundColor Red
}

# Test 3: Registered customer - Calculate  
Write-Host "`n3️⃣ Registered Customer - Calculate" -ForegroundColor Cyan
$registeredCalc = @{
    userId = 2
    customerName = 'Registered Customer'
    customerPhone = '0987654321'
    orderDetails = @(@{bookId=1; quantity=1; unitPrice=71200})
    voucherIds = @()
    subtotal = 71200
    totalAmount = 71200
    staffId = 1
} | ConvertTo-Json -Depth 5

$calcResponse2 = Invoke-RestMethod -Uri "$baseUrl/api/counter-sales/calculate" -Method POST -ContentType "application/json" -Body $registeredCalc
Write-Host "   Price: $($calcResponse2.data.items[0].unitPrice), Flash Sale: $($calcResponse2.data.items[0].flashSale)" -ForegroundColor White

# Test 4: Registered customer - Create Order
Write-Host "`n4️⃣ Registered Customer - Create Order" -ForegroundColor Cyan
$registeredOrder = @{
    userId = 2
    customerName = 'Registered Customer'
    customerPhone = '0987654321'
    orderDetails = @(@{bookId=1; quantity=1; unitPrice=71200})
    voucherIds = @()
    subtotal = 71200
    discountAmount = 0
    totalAmount = 71200
    paymentMethod = 'CASH'
    staffId = 1
    notes = 'Registered customer test order'
} | ConvertTo-Json -Depth 5

try {
    $orderResponse2 = Invoke-RestMethod -Uri "$baseUrl/api/counter-sales" -Method POST -ContentType "application/json" -Body $registeredOrder
    Write-Host "   ✅ Order ID: $($orderResponse2.data.orderId), Status: $($orderResponse2.data.orderStatus)" -ForegroundColor Green
    Write-Host "   Final Price: $($orderResponse2.data.items[0].unitPrice), Flash Sale: $($orderResponse2.data.items[0].flashSale)" -ForegroundColor White
} catch {
    Write-Host "   ❌ Error: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n🎯 Full Flow Test Complete!" -ForegroundColor Yellow
