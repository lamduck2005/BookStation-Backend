# Test Cart API with detailed logging
Write-Host "Testing Cart API with detailed analysis..." -ForegroundColor Cyan

# User 2 token
$headers = @{
    "Authorization" = "Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIyIiwiaWF0IjoxNzM2OTU5OTEyLCJleHAiOjE3MzgwODY0MDB9.sWWF0oOpKo5tJo-82hJH9y7rWa8fZFxQJvksQ0EKTxs"
    "Content-Type" = "application/json"
}

# Clear cart first
Write-Host "`n1. Clearing User 2 cart..." -ForegroundColor Yellow
try {
    $clearResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/carts/user/2/clear" -Method DELETE -Headers $headers
    Write-Host "   Cleared: $($clearResponse.message)" -ForegroundColor Green
} catch {
    Write-Host "   Clear failed: $($_.Exception.Message)" -ForegroundColor Red
}

# Test 1: Add 1 item - should succeed
Write-Host "`n2. Adding 1 item to cart..." -ForegroundColor Yellow
$cartBody1 = @{
    userId = 2
    bookId = 1
    quantity = 1
} | ConvertTo-Json

try {
    $response1 = Invoke-RestMethod -Uri "http://localhost:8080/api/carts/items" -Method POST -Headers $headers -Body $cartBody1
    Write-Host "   Add 1 item: SUCCESS" -ForegroundColor Green
    Write-Host "   Message: $($response1.message)" -ForegroundColor Cyan
} catch {
    Write-Host "   Add 1 item: FAILED" -ForegroundColor Red
    Write-Host "   Error: $($_.Exception.Message)" -ForegroundColor Red
}

# Test 2: Add 5 more items - should fail (1+5 = 6, plus 1 pending = 7 > 6)
Write-Host "`n3. Adding 5 MORE items to cart (should fail)..." -ForegroundColor Yellow
$cartBody2 = @{
    userId = 2
    bookId = 1
    quantity = 5
} | ConvertTo-Json

try {
    $response2 = Invoke-RestMethod -Uri "http://localhost:8080/api/carts/items" -Method POST -Headers $headers -Body $cartBody2
    Write-Host "   Add 5 more: UNEXPECTED SUCCESS" -ForegroundColor Red
    Write-Host "   Message: $($response2.message)" -ForegroundColor Cyan
} catch {
    Write-Host "   Add 5 more: CORRECTLY FAILED" -ForegroundColor Green
    if ($_.Exception.Response) {
        $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
        $errorBody = $reader.ReadToEnd()
        Write-Host "   Error: $errorBody" -ForegroundColor Yellow
    }
}

Write-Host "`nDone! Check logs for validation details." -ForegroundColor Cyan
