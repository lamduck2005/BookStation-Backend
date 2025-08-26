# Test book image fix in order APIs
$baseUrl = "http://localhost:8080"
$userId = 4

Write-Host "Testing Book Image Fix..." -ForegroundColor Cyan

# Test the main API endpoint
$url = "$baseUrl/api/orders/user/$userId"
$response = Invoke-RestMethod -Uri $url -Method GET

if ($response.status -eq 200) {
    $order = $response.data[0]
    $detail = $order.orderDetails[0]
    
    Write-Host ""
    Write-Host "Order Code: $($order.code)" -ForegroundColor White
    Write-Host "Book Name: $($detail.bookName)" -ForegroundColor White
    Write-Host "Book Image: $($detail.bookImageUrl)" -ForegroundColor Yellow
    
    if ($detail.bookImageUrl) {
        Write-Host ""
        Write-Host "SUCCESS: Book image is now included in API response!" -ForegroundColor Green
        Write-Host "Image URL: $($detail.bookImageUrl)" -ForegroundColor Green
    } else {
        Write-Host ""
        Write-Host "FAILED: Book image is still null in API response" -ForegroundColor Red
    }
} else {
    Write-Host "API Error: Status $($response.status)" -ForegroundColor Red
}

Write-Host ""
Write-Host "Test completed." -ForegroundColor Magenta
