#!/usr/bin/env pwsh

# Test script to verify revenue calculation fix in order-statistics API
# Compares revenue between overview API and summary API to ensure consistency

$baseUrl = "http://localhost:8080"
$overviewApi = "$baseUrl/api/order-statistics/overview"
$summaryApi = "$baseUrl/api/orders/statistics/summary?period=day"

Write-Host "Testing Revenue Calculation Fix" -ForegroundColor Cyan
Write-Host "=================================" -ForegroundColor Cyan

Write-Host "`nTesting Revenue Consistency..." -ForegroundColor Green

try {
    # Test overview API
    Write-Host "`nCalling Overview API: $overviewApi"
    $overviewResponse = Invoke-RestMethod -Uri $overviewApi -Method GET -ContentType "application/json"
    
    # Test summary API 
    Write-Host "Calling Summary API: $summaryApi"
    $summaryResponse = Invoke-RestMethod -Uri $summaryApi -Method GET -ContentType "application/json"
    
    # Extract revenue values
    $overviewRevenueToday = $overviewResponse.data.revenueToday
    $overviewRevenueMonth = $overviewResponse.data.revenueThisMonth
    
    # Sum up today's revenue from summary API (latest data point)
    $summaryData = $summaryResponse.data.data
    $todayData = $summaryData | Sort-Object { [DateTime]$_.date } | Select-Object -Last 1
    $summaryRevenueToday = if ($todayData) { $todayData.netRevenue } else { 0 }
    
    # Sum up total revenue from summary API for comparison
    $summaryTotalRevenue = ($summaryData | Measure-Object -Property netRevenue -Sum).Sum
    
    Write-Host "`nRevenue Comparison Results:" -ForegroundColor Magenta
    Write-Host "================================"
    Write-Host "Overview API - Today: $overviewRevenueToday VND" -ForegroundColor White
    Write-Host "Summary API - Today:  $summaryRevenueToday VND" -ForegroundColor White
    Write-Host ""
    Write-Host "Overview API - Month: $overviewRevenueMonth VND" -ForegroundColor White
    Write-Host "Summary API - Total:  $summaryTotalRevenue VND" -ForegroundColor White
    
    # Check consistency
    $todayDiff = [Math]::Abs($overviewRevenueToday - $summaryRevenueToday)
    $isConsistent = $todayDiff -lt 0.01  # Allow for small rounding differences
    
    Write-Host "`nConsistency Check:" -ForegroundColor Yellow
    if ($isConsistent) {
        Write-Host "PASS: Revenue calculations are consistent!" -ForegroundColor Green
        Write-Host "The fix has resolved the revenue calculation issue." -ForegroundColor Green
    } else {
        Write-Host "FAIL: Revenue calculations are still inconsistent!" -ForegroundColor Red
        Write-Host "Difference: $todayDiff VND" -ForegroundColor Red
        Write-Host "Further investigation needed." -ForegroundColor Red
    }
    
    # Display additional metrics for verification
    Write-Host "`nAdditional Overview Metrics:" -ForegroundColor Cyan
    Write-Host "- Net Profit Today: $($overviewResponse.data.netProfitToday) VND"
    Write-Host "- Net Profit Month: $($overviewResponse.data.netProfitThisMonth) VND"
    Write-Host "- Avg Revenue/Order Today: $($overviewResponse.data.averageRevenuePerOrderToday) VND"
    Write-Host "- Avg Revenue/Order Month: $($overviewResponse.data.averageRevenuePerOrderThisMonth) VND"
    Write-Host "- Refunded Orders Month: $($overviewResponse.data.refundedOrdersThisMonth)"
    
    Write-Host "`nExpected Behavior:" -ForegroundColor Blue
    Write-Host "- Revenue should be NET revenue (after refunds)"
    Write-Host "- Should match between overview and summary APIs"
    Write-Host "- Should not be raw subtotal but calculated net revenue"

} catch {
    Write-Host "Error during API testing: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "Make sure the backend is running on port 8080" -ForegroundColor Yellow
    
    # Try to start the application
    Write-Host "Starting BookStation Backend..." -ForegroundColor Yellow
    Start-Process powershell -ArgumentList "-Command", "cd '$PWD'; ./mvnw spring-boot:run" -WindowStyle Hidden
    Write-Host "Waiting for application to start (30 seconds)..."
    Start-Sleep 30
    
    # Retry the test
    Write-Host "Retrying test..."
    try {
        $overviewResponse = Invoke-RestMethod -Uri $overviewApi -Method GET -ContentType "application/json"
        $summaryResponse = Invoke-RestMethod -Uri $summaryApi -Method GET -ContentType "application/json"
        
        $overviewRevenueToday = $overviewResponse.data.revenueToday
        $summaryData = $summaryResponse.data.data
        $todayData = $summaryData | Sort-Object { [DateTime]$_.date } | Select-Object -Last 1
        $summaryRevenueToday = if ($todayData) { $todayData.netRevenue } else { 0 }
        
        Write-Host "`nRetry Results:"
        Write-Host "Overview API - Today: $overviewRevenueToday VND"
        Write-Host "Summary API - Today:  $summaryRevenueToday VND"
        
        $todayDiff = [Math]::Abs($overviewRevenueToday - $summaryRevenueToday)
        $isConsistent = $todayDiff -lt 0.01
        
        if ($isConsistent) {
            Write-Host "SUCCESS: Fix verified!" -ForegroundColor Green
        } else {
            Write-Host "Issue still exists. Difference: $todayDiff VND" -ForegroundColor Red
        }
    } catch {
        Write-Host "Failed to connect even after starting application." -ForegroundColor Red
    }
}

Write-Host "`nTest completed!" -ForegroundColor Green
