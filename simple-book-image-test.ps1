#!/usr/bin/env pwsh

# Simple test script to verify the book image fix
Write-Host "Testing Book Image Fix" -ForegroundColor Cyan

$baseUrl = "http://localhost:8080"
$userId = 4

# Test health endpoint first
Write-Host "Checking server status..." -ForegroundColor Yellow
try {
    $health = Invoke-RestMethod -Uri "$baseUrl/actuator/health" -Method GET -TimeoutSec 5
    Write-Host "Server is UP" -ForegroundColor Green
}
catch {
    Write-Host "Server is not running" -ForegroundColor Red
    exit 1
}

# Test the order API
Write-Host "Testing order API..." -ForegroundColor Blue
try {
    $response = Invoke-RestMethod -Uri "$baseUrl/api/orders/user/$userId" -Method GET
    
    if ($response.status -eq 200 -and $response.data) {
        $order = $response.data[0]
        $detail = $order.orderDetails[0]
        
        Write-Host "Order: $($order.code)" -ForegroundColor White
        Write-Host "Book: $($detail.bookName)" -ForegroundColor White
        
        if ($detail.bookImageUrl) {
            Write-Host "SUCCESS: Book Image URL: $($detail.bookImageUrl)" -ForegroundColor Green
        } else {
            Write-Host "FAILED: Book Image URL is null" -ForegroundColor Red
        }
        
        # Show the order detail JSON
        Write-Host "Order Detail JSON:" -ForegroundColor Cyan
        $detail | ConvertTo-Json | Write-Host
        
    } else {
        Write-Host "No orders found or API error" -ForegroundColor Red
    }
}
catch {
    Write-Host "API Test Failed: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "Test completed" -ForegroundColor Magenta
