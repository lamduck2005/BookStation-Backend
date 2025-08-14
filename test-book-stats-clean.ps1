# PowerShell script to test Book Statistics APIs v2.0

$baseUrl = "http://localhost:8080/api/books"

Write-Host "Testing Book Statistics APIs v2.0..." -ForegroundColor Green

# Test 1: Book Stats Overview
Write-Host "`nTesting Book Stats Overview..." -ForegroundColor Yellow
try {
    $response1 = Invoke-RestMethod -Uri "$baseUrl/stats/overview" -Method GET -ContentType "application/json"
    Write-Host "SUCCESS - Book Stats Overview:" -ForegroundColor Green
    $response1 | ConvertTo-Json -Depth 3
} catch {
    Write-Host "ERROR - Book Stats Overview: $($_.Exception.Message)" -ForegroundColor Red
}

# Test 2: Search Books for Dropdown
Write-Host "`nTesting Book Search for Dropdown..." -ForegroundColor Yellow
try {
    $response2 = Invoke-RestMethod -Uri "$baseUrl/search?query=marketing&limit=10" -Method GET -ContentType "application/json"
    Write-Host "SUCCESS - Book Search:" -ForegroundColor Green
    $response2 | ConvertTo-Json -Depth 3
} catch {
    Write-Host "ERROR - Book Search: $($_.Exception.Message)" -ForegroundColor Red
}

# Test 3: Book Comparison (Single Book)
Write-Host "`nTesting Book Comparison (Single Book)..." -ForegroundColor Yellow
try {
    $response3 = Invoke-RestMethod -Uri "$baseUrl/stats/compare?book1Id=1" -Method GET -ContentType "application/json"
    Write-Host "SUCCESS - Book Comparison (Single):" -ForegroundColor Green
    $response3 | ConvertTo-Json -Depth 3
} catch {
    Write-Host "ERROR - Book Comparison (Single): $($_.Exception.Message)" -ForegroundColor Red
}

# Test 4: Book Comparison (Two Books)
Write-Host "`nTesting Book Comparison (Two Books)..." -ForegroundColor Yellow
try {
    $response4 = Invoke-RestMethod -Uri "$baseUrl/stats/compare?book1Id=1&book2Id=2" -Method GET -ContentType "application/json"
    Write-Host "SUCCESS - Book Comparison (Two Books):" -ForegroundColor Green
    $response4 | ConvertTo-Json -Depth 3
} catch {
    Write-Host "ERROR - Book Comparison (Two Books): $($_.Exception.Message)" -ForegroundColor Red
}

# Test 5: Book Sales Chart
Write-Host "`nTesting Book Sales Chart..." -ForegroundColor Yellow
try {
    $response5 = Invoke-RestMethod -Uri "$baseUrl/stats/sales-chart?chartType=daily" -Method GET -ContentType "application/json"
    Write-Host "SUCCESS - Book Sales Chart:" -ForegroundColor Green
    $response5 | ConvertTo-Json -Depth 3
} catch {
    Write-Host "ERROR - Book Sales Chart: $($_.Exception.Message)" -ForegroundColor Red
}

# Test 6: Book Sales Velocity
Write-Host "`nTesting Book Sales Velocity..." -ForegroundColor Yellow
try {
    $response6 = Invoke-RestMethod -Uri "$baseUrl/stats/velocity-chart?chartType=trend" -Method GET -ContentType "application/json"
    Write-Host "SUCCESS - Book Sales Velocity:" -ForegroundColor Green
    $response6 | ConvertTo-Json -Depth 3
} catch {
    Write-Host "ERROR - Book Sales Velocity: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`nAll Book Statistics v2.0 tests completed!" -ForegroundColor Green
Write-Host "`nSummary of tested endpoints:" -ForegroundColor Cyan
Write-Host "   1. GET /api/books/stats/overview" -ForegroundColor White
Write-Host "   2. GET /api/books/search?query=marketing&limit=10" -ForegroundColor White
Write-Host "   3. GET /api/books/stats/compare?book1Id=1" -ForegroundColor White
Write-Host "   4. GET /api/books/stats/compare?book1Id=1&book2Id=2" -ForegroundColor White
Write-Host "   5. GET /api/books/stats/sales-chart?chartType=daily" -ForegroundColor White
Write-Host "   6. GET /api/books/stats/velocity-chart?chartType=trend" -ForegroundColor White
