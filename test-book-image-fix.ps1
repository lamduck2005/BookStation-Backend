#!/usr/bin/env pwsh

# Test script to verify book image fix in order API response
Write-Host "🔧 Testing Book Image Fix in Order API" -ForegroundColor Cyan

# Configuration
$baseUrl = "http://localhost:8080"
$userId = 4
$testOrderId = 1

# Function to test API endpoint
function Test-ApiEndpoint {
    param(
        [string]$endpoint,
        [string]$description
    )
    
    Write-Host "`n📡 Testing: $description" -ForegroundColor Yellow
    Write-Host "Endpoint: $endpoint" -ForegroundColor Gray
    
    try {
        $response = Invoke-RestMethod -Uri $endpoint -Method GET -ContentType "application/json"
        
        if ($response -and $response.status -eq 200) {
            Write-Host "✅ Status: $($response.status) - $($response.message)" -ForegroundColor Green
            
            # Check if data exists and has orders
            if ($response.data -and $response.data.Count -gt 0) {
                $order = $response.data[0]
                Write-Host "📋 Order ID: $($order.id), Code: $($order.code)" -ForegroundColor White
                
                # Check order details for book images
                if ($order.orderDetails -and $order.orderDetails.Count -gt 0) {
                    Write-Host "`n📚 Checking book images in order details:" -ForegroundColor Cyan
                    
                    foreach ($detail in $order.orderDetails) {
                        $bookName = $detail.bookName
                        $bookImageUrl = $detail.bookImageUrl
                        
                        if ($bookImageUrl) {
                            Write-Host "  ✅ Book: '$bookName' - Image: $bookImageUrl" -ForegroundColor Green
                        } else {
                            Write-Host "  ❌ Book: '$bookName' - Image: NULL (ISSUE!)" -ForegroundColor Red
                        }
                    }
                } else {
                    Write-Host "⚠️  No order details found" -ForegroundColor Yellow
                }
            } else {
                Write-Host "⚠️  No orders found in response" -ForegroundColor Yellow
            }
        } else {
            Write-Host "❌ API Error: Status $($response.status)" -ForegroundColor Red
            return $false
        }
        
        return $true
    }
    catch {
        Write-Host "❌ Request Failed: $($_.Exception.Message)" -ForegroundColor Red
        return $false
    }
}

# Function to check if server is running
function Test-ServerRunning {
    try {
        $response = Invoke-RestMethod -Uri "$baseUrl/actuator/health" -Method GET -TimeoutSec 5
        return $true
    }
    catch {
        return $false
    }
}

# Main test execution
Write-Host "🚀 Starting Book Image Fix Test" -ForegroundColor Magenta
Write-Host "Base URL: $baseUrl" -ForegroundColor Gray
Write-Host "User ID: $userId" -ForegroundColor Gray

# Check if server is running
Write-Host "`n🔍 Checking server status..." -ForegroundColor Blue
if (-not (Test-ServerRunning)) {
    Write-Host "❌ Server is not running at $baseUrl" -ForegroundColor Red
    Write-Host "Please start the BookStation Backend server first" -ForegroundColor Yellow
    exit 1
}
Write-Host "✅ Server is running" -ForegroundColor Green

# Test different API endpoints that return order data with book images
$testEndpoints = @(
    @{
        Url = "$baseUrl/api/orders/user/$userId"
        Description = "Get orders by user ID"
    },
    @{
        Url = "$baseUrl/api/orders/$testOrderId"
        Description = "Get order by ID"
    },
    @{
        Url = "$baseUrl/api/orders/user/$userId/pagination?page=0&size=5"
        Description = "Get user orders with pagination"
    }
)

$allTestsPassed = $true

foreach ($test in $testEndpoints) {
    $result = Test-ApiEndpoint -endpoint $test.Url -description $test.Description
    if (-not $result) {
        $allTestsPassed = $false
    }
}

# Summary
Write-Host "`n" + "="*60 -ForegroundColor Cyan
if ($allTestsPassed) {
    Write-Host "🎉 ALL TESTS PASSED!" -ForegroundColor Green
    Write-Host "Book images are now properly included in order API responses" -ForegroundColor Green
} else {
    Write-Host "❌ SOME TESTS FAILED!" -ForegroundColor Red
    Write-Host "Please check the server logs and fix any issues" -ForegroundColor Yellow
}

Write-Host "`n📝 Test completed at $(Get-Date)" -ForegroundColor Gray
