# Test Book Statistics API v4
# Script để test các case theo yêu cầu của user

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  TESTING BOOK STATISTICS API v4" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

$baseUrl = "http://localhost:8080/api/books/statistics"
$headers = @{ "Content-Type" = "application/json" }

Write-Host ""
Write-Host "1. Testing DEFAULT period (should return 30 days)" -ForegroundColor Yellow
Write-Host "GET $baseUrl/summary?period=day" -ForegroundColor Gray

try {
    $response1 = Invoke-RestMethod -Uri "$baseUrl/summary?period=day" -Method GET -Headers $headers
    Write-Host "Status: $($response1.status)" -ForegroundColor Green
    Write-Host "Message: $($response1.message)" -ForegroundColor Green
    Write-Host "Data count: $($response1.data.Count) records" -ForegroundColor Green
    
    if ($response1.data.Count -gt 0) {
        Write-Host "First record: $($response1.data[0] | ConvertTo-Json -Compress)" -ForegroundColor Gray
        Write-Host "Last record: $($response1.data[-1] | ConvertTo-Json -Compress)" -ForegroundColor Gray
    }
} catch {
    Write-Host "Error: $_" -ForegroundColor Red
}

Write-Host ""
Write-Host "2. Testing CUSTOM period (should auto-group by rules)" -ForegroundColor Yellow

# Test case 1: Custom period 15 days (should group by day)
$fromDate = 1754179200000  # User's provided timestamp
$toDate = 1755129600000    # User's provided timestamp
$customUrl = "$baseUrl/summary?period=custom&fromDate=$fromDate&toDate=$toDate"

Write-Host "GET $customUrl" -ForegroundColor Gray
Write-Host "Days difference: $(($toDate - $fromDate) / (24 * 60 * 60 * 1000)) days (should group by DAY)" -ForegroundColor Gray

try {
    $response2 = Invoke-RestMethod -Uri $customUrl -Method GET -Headers $headers
    Write-Host "Status: $($response2.status)" -ForegroundColor Green
    Write-Host "Message: $($response2.message)" -ForegroundColor Green
    Write-Host "Data count: $($response2.data.Count) records" -ForegroundColor Green
    
    if ($response2.data.Count -gt 0) {
        Write-Host "First record: $($response2.data[0] | ConvertTo-Json -Compress)" -ForegroundColor Gray
        Write-Host "Period type: $($response2.data[0].period)" -ForegroundColor Cyan
    }
} catch {
    Write-Host "Error: $_" -ForegroundColor Red
}

Write-Host ""
Write-Host "3. Testing CUSTOM period for weekly grouping (32+ days)" -ForegroundColor Yellow

# Test case 2: Custom period 60 days (should group by week)
$fromDate60 = (Get-Date).AddDays(-60).ToUniversalTime().Subtract([DateTime]"1970-01-01").TotalMilliseconds
$toDate60 = (Get-Date).ToUniversalTime().Subtract([DateTime]"1970-01-01").TotalMilliseconds
$customUrl60 = "$baseUrl/summary?period=custom&fromDate=$([int64]$fromDate60)&toDate=$([int64]$toDate60)"

Write-Host "GET $customUrl60" -ForegroundColor Gray
Write-Host "Days difference: 60 days (should group by WEEK)" -ForegroundColor Gray

try {
    $response3 = Invoke-RestMethod -Uri $customUrl60 -Method GET -Headers $headers
    Write-Host "Status: $($response3.status)" -ForegroundColor Green
    Write-Host "Data count: $($response3.data.Count) records" -ForegroundColor Green
    
    if ($response3.data.Count -gt 0) {
        Write-Host "Period type: $($response3.data[0].period)" -ForegroundColor Cyan
    }
} catch {
    Write-Host "Error: $_" -ForegroundColor Red
}

Write-Host ""
Write-Host "4. Testing CUSTOM period for monthly grouping (180+ days)" -ForegroundColor Yellow

# Test case 3: Custom period 200 days (should group by month)
$fromDate200 = (Get-Date).AddDays(-200).ToUniversalTime().Subtract([DateTime]"1970-01-01").TotalMilliseconds
$toDate200 = (Get-Date).ToUniversalTime().Subtract([DateTime]"1970-01-01").TotalMilliseconds
$customUrl200 = "$baseUrl/summary?period=custom&fromDate=$([int64]$fromDate200)&toDate=$([int64]$toDate200)"

Write-Host "GET $customUrl200" -ForegroundColor Gray
Write-Host "Days difference: 200 days (should group by MONTH)" -ForegroundColor Gray

try {
    $response4 = Invoke-RestMethod -Uri $customUrl200 -Method GET -Headers $headers
    Write-Host "Status: $($response4.status)" -ForegroundColor Green
    Write-Host "Data count: $($response4.data.Count) records" -ForegroundColor Green
    
    if ($response4.data.Count -gt 0) {
        Write-Host "Period type: $($response4.data[0].period)" -ForegroundColor Cyan
    }
} catch {
    Write-Host "Error: $_" -ForegroundColor Red
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  TEST COMPLETED" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

Write-Host ""
Write-Host "Expected Results:" -ForegroundColor Yellow
Write-Host "1. Default period=day: Should return 30 days with 0 for days with no sales" -ForegroundColor White
Write-Host "2. Custom ≤31 days: Should group by DAY (period='daily')" -ForegroundColor White
Write-Host "3. Custom 32-180 days: Should group by WEEK (period='weekly')" -ForegroundColor White
Write-Host "4. Custom >180 days: Should group by MONTH (period='monthly')" -ForegroundColor White
