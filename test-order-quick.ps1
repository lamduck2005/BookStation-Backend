#!/usr/bin/env pwsh
# ================================================================
# Quick Order Statistics Validation Script
# ================================================================

Write-Host "Testing Order Statistics API" -ForegroundColor Green

$baseUrl = "http://localhost:8080"

# Quick test function
function Quick-Test {
    param([string]$url, [string]$name)
    
    try {
        Write-Host "`nTesting: $name" -ForegroundColor Cyan
        $response = Invoke-RestMethod -Uri $url -Method Get
        
        if ($response.code -eq 200) {
            Write-Host "SUCCESS: $name" -ForegroundColor Green
            if ($response.data -is [array]) {
                Write-Host "  Records: $($response.data.Count)" -ForegroundColor Gray
            }
        } else {
            Write-Host "FAILED: $name ($($response.code))" -ForegroundColor Red
        }
    } catch {
        Write-Host "ERROR: $name - $($_.Exception.Message)" -ForegroundColor Red
    }
}

# Test Summary APIs
Quick-Test "$baseUrl/api/orders/statistics/summary?period=day" "Daily Summary"
Quick-Test "$baseUrl/api/orders/statistics/summary?period=week" "Weekly Summary"
Quick-Test "$baseUrl/api/orders/statistics/summary?period=month" "Monthly Summary"

# Test Details APIs
$today = [DateTimeOffset]::Now.ToUnixTimeMilliseconds()
Quick-Test "$baseUrl/api/orders/statistics/details?period=day&date=$today&limit=5" "Daily Details"

Write-Host "`nQuick validation completed!" -ForegroundColor Green
