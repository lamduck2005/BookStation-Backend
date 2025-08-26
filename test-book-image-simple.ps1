#!/usr/bin/env pwsh

# Test book image fix in order APIs
Write-Host "BookStation Backend - Book Image Fix Test" -ForegroundColor Magenta

$baseUrl = "http://localhost:8080"
$userId = 4

# Test 1: User orders
Write-Host "`nTest 1: Get orders by user ID" -ForegroundColor Cyan
try {
    $response = Invoke-RestMethod -Uri "$baseUrl/api/orders/user/$userId" -Method GET
    $order = $response.data[0]
    $detail = $order.orderDetails[0]
    
    Write-Host "Order Code: $($order.code)" -ForegroundColor White
    Write-Host "Book Name: $($detail.bookName)" -ForegroundColor White
    Write-Host "Book Image: $($detail.bookImageUrl)" -ForegroundColor $(if ($detail.bookImageUrl) { 'Green' } else { 'Red' })
    
    if ($detail.bookImageUrl) {
        Write-Host "✓ PASS: Image URL found" -ForegroundColor Green
    } else {
        Write-Host "✗ FAIL: Image URL is null" -ForegroundColor Red
    }
} catch {
    Write-Host "✗ FAIL: $($_.Exception.Message)" -ForegroundColor Red
}

# Test 2: Order by ID
Write-Host "`nTest 2: Get order by ID" -ForegroundColor Cyan
try {
    $response = Invoke-RestMethod -Uri "$baseUrl/api/orders/1" -Method GET
    $order = $response.data
    $detail = $order.orderDetails[0]
    
    Write-Host "Order Code: $($order.code)" -ForegroundColor White
    Write-Host "Book Name: $($detail.bookName)" -ForegroundColor White
    Write-Host "Book Image: $($detail.bookImageUrl)" -ForegroundColor $(if ($detail.bookImageUrl) { 'Green' } else { 'Red' })
    
    if ($detail.bookImageUrl) {
        Write-Host "✓ PASS: Image URL found" -ForegroundColor Green
    } else {
        Write-Host "✗ FAIL: Image URL is null" -ForegroundColor Red
    }
} catch {
    Write-Host "✗ FAIL: $($_.Exception.Message)" -ForegroundColor Red
}

# Test 3: User orders with pagination
Write-Host "`nTest 3: Get user orders with pagination" -ForegroundColor Cyan
try {
    $response = Invoke-RestMethod -Uri "$baseUrl/api/orders/user/$userId/pagination?page=0&size=5" -Method GET
    $order = $response.data.content[0]
    $detail = $order.orderDetails[0]
    
    Write-Host "Order Code: $($order.code)" -ForegroundColor White
    Write-Host "Book Name: $($detail.bookName)" -ForegroundColor White  
    Write-Host "Book Image: $($detail.bookImageUrl)" -ForegroundColor $(if ($detail.bookImageUrl) { 'Green' } else { 'Red' })
    
    if ($detail.bookImageUrl) {
        Write-Host "✓ PASS: Image URL found" -ForegroundColor Green
    } else {
        Write-Host "✗ FAIL: Image URL is null" -ForegroundColor Red
    }
} catch {
    Write-Host "✗ FAIL: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n" + ("=" * 60) -ForegroundColor Cyan
Write-Host "Book Image Fix Test Completed!" -ForegroundColor Magenta
Write-Host "If all tests show PASS, the fix is working correctly." -ForegroundColor White
