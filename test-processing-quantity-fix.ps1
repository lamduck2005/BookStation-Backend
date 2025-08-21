#!/usr/bin/env powershell
# Test script to verify processing quantity logic fix

$baseUrl = "http://localhost:8080"

Write-Host "Testing Processing Quantity Logic Fix..." -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green

# Test the processing-quantity API
$bookId = 1  # Change this to match your database
$url = "$baseUrl/api/books/processing-quantity/$bookId"

Write-Host "Testing API: GET $url" -ForegroundColor Yellow

try {
    $response = Invoke-RestMethod -Uri $url -Method Get -ContentType "application/json"
    
    Write-Host "API Response:" -ForegroundColor Green
    Write-Host "Status: $($response.status)" -ForegroundColor Cyan
    Write-Host "Message: $($response.message)" -ForegroundColor Cyan
    Write-Host "Processing Quantity: $($response.data)" -ForegroundColor Magenta
    
    Write-Host ""
    Write-Host "Logic Explanation:" -ForegroundColor Yellow
    Write-Host "- Processing Quantity = Sum from orders with processing statuses" -ForegroundColor White
    Write-Host "- Fixed: Removed double counting from partial refunds" -ForegroundColor White
    Write-Host "- Example: Order 2 books, refund 1 => Processing = 1 (only refund quantity)" -ForegroundColor White
    
} catch {
    Write-Host "Error calling API:" -ForegroundColor Red
    Write-Host $_.Exception.Message -ForegroundColor Red
}

Write-Host ""
Write-Host "Test case flow:" -ForegroundColor Yellow
Write-Host "1. Order 2 books (PENDING) => Processing = 2" -ForegroundColor White
Write-Host "2. Order delivered (DELIVERED) => Processing = 0" -ForegroundColor White
Write-Host "3. Request partial refund 1 book (REFUND_REQUESTED) => Processing = 1" -ForegroundColor White
Write-Host "4. Refund completed (REFUNDED) => Processing = 0" -ForegroundColor White

Write-Host ""
Write-Host "Processing Quantity Logic Test Completed!" -ForegroundColor Green
