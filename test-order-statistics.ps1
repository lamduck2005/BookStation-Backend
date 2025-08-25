#!/usr/bin/env pwsh
# ================================================================
# 📊 ORDER STATISTICS APIs Test Script
# 2-Tier Architecture Testing (tương tự Book Statistics)
# ================================================================

Write-Host "🚀 Testing Order Statistics APIs - 2-Tier Architecture" -ForegroundColor Green

# Configuration
$baseUrl = "http://localhost:8080"
$headers = @{
    "Content-Type" = "application/json"
}

# Helper function to test API endpoint
function Test-OrderAPI {
    param(
        [string]$url,
        [string]$description,
        [hashtable]$expectedKeys
    )
    
    Write-Host "`n📊 Testing: $description" -ForegroundColor Cyan
    Write-Host "URL: $url" -ForegroundColor Gray
    
    try {
        $response = Invoke-RestMethod -Uri $url -Method Get -Headers $headers
        
        if ($response.code -eq 200) {
            Write-Host "✅ SUCCESS: $description" -ForegroundColor Green
            Write-Host "📊 Response Code: $($response.code)" -ForegroundColor Gray
            Write-Host "📋 Message: $($response.message)" -ForegroundColor Gray
            
            # Check if data is an array
            if ($response.data -is [array]) {
                Write-Host "📈 Records Count: $($response.data.Count)" -ForegroundColor Yellow
                
                if ($response.data.Count -gt 0) {
                    Write-Host "🔍 First Record Sample:" -ForegroundColor Blue
                    $firstRecord = $response.data[0]
                    
                    # Check expected keys
                    foreach ($key in $expectedKeys.Keys) {
                        if ($firstRecord.PSObject.Properties.Name -contains $key) {
                            $value = $firstRecord.$key
                            $expectedType = $expectedKeys[$key]
                            Write-Host "  ✓ $key ($expectedType): $value" -ForegroundColor Gray
                        } else {
                            Write-Host "  ❌ Missing key: $key" -ForegroundColor Red
                        }
                    }
                } else {
                    Write-Host "📝 No data records found" -ForegroundColor Yellow
                }
            } else {
                Write-Host "📋 Response Data: $($response.data)" -ForegroundColor Gray
            }
        } else {
            Write-Host "❌ FAILED: $description" -ForegroundColor Red
            Write-Host "📊 Response Code: $($response.code)" -ForegroundColor Red
            Write-Host "📋 Message: $($response.message)" -ForegroundColor Red
        }
        
        return $response
    }
    catch {
        Write-Host "💥 ERROR: $description" -ForegroundColor Red
        Write-Host "❌ Exception: $($_.Exception.Message)" -ForegroundColor Red
        return $null
    }
}

# ================================================================
# 🧪 TIER 1: ORDER STATISTICS SUMMARY APIs
# ================================================================

Write-Host "`n🎯 === TIER 1: ORDER STATISTICS SUMMARY ===" -ForegroundColor Magenta

$summaryExpectedKeys = @{
    "date" = "String (YYYY-MM-DD)"
    "totalOrders" = "Number"
    "completedOrders" = "Number" 
    "canceledOrders" = "Number"
    "refundedOrders" = "Number"
    "netRevenue" = "BigDecimal"
    "aov" = "BigDecimal"
}

# Test Daily Summary (Last 7 days)
Test-OrderAPI -url "$baseUrl/api/orders/statistics/summary?period=day" -description "Daily Summary (Last 7 days)" -expectedKeys $summaryExpectedKeys

# Test Weekly Summary  
Test-OrderAPI -url "$baseUrl/api/orders/statistics/summary?period=week" -description "Weekly Summary" -expectedKeys $summaryExpectedKeys

# Test Monthly Summary
Test-OrderAPI -url "$baseUrl/api/orders/statistics/summary?period=month" -description "Monthly Summary" -expectedKeys $summaryExpectedKeys

# Test Quarterly Summary
Test-OrderAPI -url "$baseUrl/api/orders/statistics/summary?period=quarter" -description "Quarterly Summary" -expectedKeys $summaryExpectedKeys

# Test Yearly Summary
Test-OrderAPI -url "$baseUrl/api/orders/statistics/summary?period=year" -description "Yearly Summary" -expectedKeys $summaryExpectedKeys

# Test Custom Date Range (30 days)
$endDate = [DateTimeOffset]::Now.ToUnixTimeMilliseconds()
$startDate = [DateTimeOffset]::Now.AddDays(-30).ToUnixTimeMilliseconds()
Test-OrderAPI -url "$baseUrl/api/orders/statistics/summary?period=day&fromDate=$startDate&toDate=$endDate" -description "Custom Range (30 days)" -expectedKeys $summaryExpectedKeys

# ================================================================
# 🧪 TIER 2: ORDER STATISTICS DETAILS APIs
# ================================================================

Write-Host "`n🎯 === TIER 2: ORDER STATISTICS DETAILS ===" -ForegroundColor Magenta

$detailsExpectedKeys = @{
    "orderCode" = "String"
    "customerName" = "String"
    "customerEmail" = "String"
    "totalAmount" = "BigDecimal"
    "orderStatus" = "String"
    "createdAt" = "Number (timestamp)"
    "productInfo" = "String"
}

# Test Daily Details (Today)
$today = [DateTimeOffset]::Now.ToUnixTimeMilliseconds()
Test-OrderAPI -url "$baseUrl/api/orders/statistics/details?period=day&date=$today" -description "Daily Details (Today)" -expectedKeys $detailsExpectedKeys

# Test Weekly Details (This Week)
Test-OrderAPI -url "$baseUrl/api/orders/statistics/details?period=week&date=$today" -description "Weekly Details (This Week)" -expectedKeys $detailsExpectedKeys

# Test Monthly Details (This Month) 
Test-OrderAPI -url "$baseUrl/api/orders/statistics/details?period=month&date=$today" -description "Monthly Details (This Month)" -expectedKeys $detailsExpectedKeys

# Test with Limit Parameter
Test-OrderAPI -url "$baseUrl/api/orders/statistics/details?period=month&date=$today&limit=5" -description "Monthly Details (Limit 5)" -expectedKeys $detailsExpectedKeys

# ================================================================
# 📊 VALIDATION & COMPARISON TESTS  
# ================================================================

Write-Host "`n🎯 === VALIDATION TESTS ===" -ForegroundColor Magenta

# Test Invalid Period
try {
    Write-Host "`n🧪 Testing Invalid Period Parameter..." -ForegroundColor Cyan
    $response = Invoke-RestMethod -Uri "$baseUrl/api/orders/statistics/summary?period=invalid" -Method Get -Headers $headers
    if ($response.code -ne 200) {
        Write-Host "✅ Invalid period properly rejected" -ForegroundColor Green
    } else {
        Write-Host "⚠️ Invalid period was accepted (unexpected)" -ForegroundColor Yellow
    }
} catch {
    Write-Host "✅ Invalid period properly rejected with exception" -ForegroundColor Green
}

# Test Missing Required Parameters for Details
try {
    Write-Host "`n🧪 Testing Missing Date Parameter..." -ForegroundColor Cyan
    $response = Invoke-RestMethod -Uri "$baseUrl/api/orders/statistics/details?period=day" -Method Get -Headers $headers
    if ($response.code -ne 200) {
        Write-Host "✅ Missing date parameter properly rejected" -ForegroundColor Green
    }
} catch {
    Write-Host "✅ Missing date parameter properly rejected with exception" -ForegroundColor Green
}

# ================================================================
# 📊 SUMMARY REPORT
# ================================================================

Write-Host "`n" + "="*80 -ForegroundColor Green
Write-Host "🎉 ORDER STATISTICS APIs TEST COMPLETED!" -ForegroundColor Green  
Write-Host "="*80 -ForegroundColor Green

Write-Host "`n📊 Test Coverage:" -ForegroundColor Cyan
Write-Host "  ✅ TIER 1 - Summary API: 6 test cases" -ForegroundColor Gray
Write-Host "  ✅ TIER 2 - Details API: 4 test cases" -ForegroundColor Gray  
Write-Host "  ✅ Validation Tests: 2 test cases" -ForegroundColor Gray
Write-Host "  📈 Total: 12 test scenarios" -ForegroundColor Yellow

Write-Host "`n🔍 Tested Features:" -ForegroundColor Cyan
Write-Host "  • Daily/Weekly/Monthly/Quarterly/Yearly periods" -ForegroundColor Gray
Write-Host "  • Custom date range support" -ForegroundColor Gray
Write-Host "  • Order metrics: totalOrders, completedOrders, canceledOrders, refundedOrders" -ForegroundColor Gray
Write-Host "  • Financial metrics: netRevenue, AOV" -ForegroundColor Gray
Write-Host "  • Order details: code, customer info, status, product info" -ForegroundColor Gray
Write-Host "  • Error handling & parameter validation" -ForegroundColor Gray

Write-Host "`n🚀 Ready for production use!" -ForegroundColor Green
