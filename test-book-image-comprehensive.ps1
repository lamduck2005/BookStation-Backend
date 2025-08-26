#!/usr/bin/env pwsh

# Comprehensive test script for book image fix
param(
    [string]$BaseUrl = "http://localhost:8080",
    [int]$UserId = 4,
    [int]$OrderId = 1
)

Write-Host "=== BookStation Backend - Book Image Fix Test ===" -ForegroundColor Magenta
Write-Host "Base URL: $BaseUrl" -ForegroundColor Gray
Write-Host "Test User ID: $UserId" -ForegroundColor Gray
Write-Host "Test Order ID: $OrderId" -ForegroundColor Gray

# Check server health
Write-Host "`n1. Checking server health..." -ForegroundColor Cyan
try {
    $health = Invoke-RestMethod -Uri "$BaseUrl/actuator/health" -Method GET -TimeoutSec 5
    if ($health.status -eq "UP") {
        Write-Host "   Server is UP and running" -ForegroundColor Green
    } else {
        Write-Host "   Server status: $($health.status)" -ForegroundColor Yellow
    }
} catch {
    Write-Host "   Server is not accessible: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# Test endpoints
$testResults = @()
$endpoints = @(
    @{ Name = "Get orders by user ID"; Url = "$BaseUrl/api/orders/user/$UserId" },
    @{ Name = "Get order by ID"; Url = "$BaseUrl/api/orders/$OrderId" },
    @{ Name = "Get user orders with pagination"; Url = "$BaseUrl/api/orders/user/$UserId/pagination?page=0&size=5" }
)

Write-Host "`n2. Testing order API endpoints..." -ForegroundColor Cyan

foreach ($endpoint in $endpoints) {
    Write-Host "`n   Testing: $($endpoint.Name)" -ForegroundColor Yellow
    Write-Host "   URL: $($endpoint.Url)" -ForegroundColor Gray
    
    try {
        $response = Invoke-RestMethod -Uri $endpoint.Url -Method GET -TimeoutSec 10
        
        if ($response.status -eq 200) {
            $orderData = $null
            
            # Handle different response structures
            if ($endpoint.Name -like "*pagination*") {
                $orderData = $response.data.content[0]
            } elseif ($endpoint.Name -like "*by ID*") {
                $orderData = $response.data
            } else {
                $orderData = $response.data[0]
            }
            
            if ($orderData -and $orderData.orderDetails -and $orderData.orderDetails.Count -gt 0) {
                $detail = $orderData.orderDetails[0]
                $hasImage = [bool]$detail.bookImageUrl
                
                $result = @{
                    Endpoint = $endpoint.Name
                    Success = $true
                    OrderCode = $orderData.code
                    BookName = $detail.bookName
                    BookCode = $detail.bookCode
                    HasImage = $hasImage
                    ImageUrl = $detail.bookImageUrl
                    Status = if ($hasImage) { "PASS" } else { "FAIL - No Image" }
                }
                
                $testResults += $result
                
                if ($hasImage) {
                    Write-Host "   ✓ Order: $($orderData.code)" -ForegroundColor Green
                    Write-Host "   ✓ Book: $($detail.bookName)" -ForegroundColor Green
                    Write-Host "   ✓ Image: $($detail.bookImageUrl)" -ForegroundColor Green
                    Write-Host "   RESULT: PASS" -ForegroundColor Green
                } else {
                    Write-Host "   ✓ Order: $($orderData.code)" -ForegroundColor Green
                    Write-Host "   ✓ Book: $($detail.bookName)" -ForegroundColor Green
                    Write-Host "   ✗ Image: NULL" -ForegroundColor Red
                    Write-Host "   RESULT: FAIL" -ForegroundColor Red
                }
            } else {
                Write-Host "   ✗ No order details found" -ForegroundColor Red
                $testResults += @{ Endpoint = $endpoint.Name; Success = $false; Status = "FAIL - No Data" }
            }
        } else {
            Write-Host "   ✗ API returned status: $($response.status)" -ForegroundColor Red
            $testResults += @{ Endpoint = $endpoint.Name; Success = $false; Status = "FAIL - API Error" }
        }
    } catch {
        Write-Host "   ✗ Request failed: $($_.Exception.Message)" -ForegroundColor Red
        $testResults += @{ Endpoint = $endpoint.Name; Success = $false; Status = "FAIL - Exception" }
    }
}

# Summary
Write-Host "`n3. Test Summary" -ForegroundColor Cyan
Write-Host "=" * 80 -ForegroundColor Gray

$passCount = ($testResults | Where-Object { $_.Status -eq "PASS" }).Count
$failCount = $testResults.Count - $passCount

Write-Host "Total Tests: $($testResults.Count)" -ForegroundColor White
Write-Host "Passed: $passCount" -ForegroundColor Green
Write-Host "Failed: $failCount" -ForegroundColor $(if ($failCount -eq 0) { "Green" } else { "Red" })

Write-Host "`nDetailed Results:" -ForegroundColor White
foreach ($result in $testResults) {
    $color = if ($result.Status -eq "PASS") { "Green" } else { "Red" }
    Write-Host "  $($result.Endpoint): $($result.Status)" -ForegroundColor $color
    if ($result.BookName) {
        Write-Host "    Book: $($result.BookName)" -ForegroundColor Gray
        if ($result.ImageUrl) {
            Write-Host "    Image: $($result.ImageUrl)" -ForegroundColor Gray
        }
    }
}

Write-Host "`n4. Final Result" -ForegroundColor Cyan
Write-Host "=" * 80 -ForegroundColor Gray

if ($failCount -eq 0) {
    Write-Host "🎉 ALL TESTS PASSED!" -ForegroundColor Green
    Write-Host "Book images are successfully included in all order API responses." -ForegroundColor Green
    Write-Host "`nThe fix is working correctly! ✅" -ForegroundColor Green
} else {
    Write-Host "❌ SOME TESTS FAILED!" -ForegroundColor Red
    Write-Host "Book images are not properly included in some API responses." -ForegroundColor Red
    Write-Host "`nPlease check the failed endpoints and fix the issues. ⚠️" -ForegroundColor Yellow
}

Write-Host "`nTest completed at $(Get-Date)" -ForegroundColor Gray
Write-Host "=" * 80 -ForegroundColor Gray
