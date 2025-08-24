# BookStation Backend - Complete Validation Test Suite
Write-Host "Starting BookStation Validation Test Suite..." -ForegroundColor Cyan

$baseUrl = "http://localhost:8080/api/books/statistics/summary"
$testsPassed = 0
$testsFailed = 0

function Test-API {
    param(
        [string]$testName,
        [string]$url,
        [bool]$shouldSucceed = $true
    )
    
    Write-Host "`nTesting: $testName" -ForegroundColor Yellow
    
    try {
        $response = Invoke-RestMethod -Uri $url -Method GET -ErrorAction Stop
        
        if ($shouldSucceed) {
            if ($response.status -eq 200) {
                Write-Host "PASS - Success as expected" -ForegroundColor Green
                $script:testsPassed++
            } else {
                Write-Host "FAIL - Expected success but got status: $($response.status)" -ForegroundColor Red
                $script:testsFailed++
            }
        } else {
            if ($response.status -eq 400) {
                Write-Host "PASS - Error returned as expected" -ForegroundColor Green
                Write-Host "Error: $($response.message)" -ForegroundColor Gray
                $script:testsPassed++
            } else {
                Write-Host "FAIL - Expected error but got status: $($response.status)" -ForegroundColor Red
                $script:testsFailed++
            }
        }
        
    } catch {
        if ($shouldSucceed) {
            Write-Host "FAIL - Request failed: $($_.Exception.Message)" -ForegroundColor Red
            $script:testsFailed++
        } else {
            Write-Host "PASS - Request failed as expected" -ForegroundColor Green
            $script:testsPassed++
        }
    }
}

Write-Host "`nPHASE 1: Valid Scenarios" -ForegroundColor Magenta

# Valid daily - 2 days
$fromDate = 1722470400000  # 2024-08-01
$toDate = 1722643200000    # 2024-08-03
Test-API "Daily: 2 days (valid)" "$baseUrl?period=day&fromDate=$fromDate&toDate=$toDate" $true

# Valid weekly - 14 days
$fromDate = 1722470400000  # 2024-08-01
$toDate = 1723680000000    # 2024-08-15
Test-API "Weekly: 14 days (valid)" "$baseUrl?period=week&fromDate=$fromDate&toDate=$toDate" $true

# Valid monthly - 62 days
$fromDate = 1722470400000  # 2024-08-01
$toDate = 1727740800000    # 2024-10-01
Test-API "Monthly: 62 days (valid)" "$baseUrl?period=month&fromDate=$fromDate&toDate=$toDate" $true

# Valid quarterly - 180 days
$fromDate = 1704067200000  # 2024-01-01
$toDate = 1719792000000    # 2024-07-01
Test-API "Quarterly: 182 days (valid)" "$baseUrl?period=quarter&fromDate=$fromDate&toDate=$toDate" $true

# Valid yearly - 400 days
$fromDate = 1672531200000  # 2023-01-01
$toDate = 1706745600000    # 2024-02-01
Test-API "Yearly: 396 days (valid)" "$baseUrl?period=year&fromDate=$fromDate&toDate=$toDate" $true

Write-Host "`nPHASE 2: Invalid Scenarios" -ForegroundColor Magenta

# Invalid daily - 0 days
$fromDate = 1722470400000  # 2024-08-01 00:00
$toDate = 1722470400000    # 2024-08-01 00:00 (same time)
Test-API "Daily: 0 days (invalid)" "$baseUrl?period=day&fromDate=$fromDate&toDate=$toDate" $false

# Invalid weekly - 5 days
$fromDate = 1722470400000  # 2024-08-01
$toDate = 1722902400000    # 2024-08-06 
Test-API "Weekly: 5 days (invalid)" "$baseUrl?period=week&fromDate=$fromDate&toDate=$toDate" $false

# Invalid monthly - 20 days
$fromDate = 1722470400000  # 2024-08-01
$toDate = 1724198400000    # 2024-08-21
Test-API "Monthly: 20 days (invalid)" "$baseUrl?period=month&fromDate=$fromDate&toDate=$toDate" $false

# Invalid quarterly - 60 days
$fromDate = 1722470400000  # 2024-08-01
$toDate = 1727654400000    # 2024-09-30
Test-API "Quarterly: 60 days (invalid)" "$baseUrl?period=quarter&fromDate=$fromDate&toDate=$toDate" $false

# ORIGINAL FAILING CASE - Invalid yearly - 214 days
$fromDate = 1717200000000  # 2024-06-01
$toDate = 1735689600000    # 2025-01-01
Test-API "Yearly: 214 days - ORIGINAL FAILING CASE (invalid)" "$baseUrl?period=year&fromDate=$fromDate&toDate=$toDate" $false

# Results Summary
Write-Host "`n" + "="*50 -ForegroundColor White
Write-Host "TEST SUMMARY RESULTS" -ForegroundColor Cyan
Write-Host "="*50 -ForegroundColor White
Write-Host "Tests Passed: $testsPassed" -ForegroundColor Green
Write-Host "Tests Failed: $testsFailed" -ForegroundColor Red
Write-Host "Total Tests: $($testsPassed + $testsFailed)" -ForegroundColor Gray

if ($testsFailed -eq 0) {
    Write-Host "`nALL TESTS PASSED! Validation is working correctly!" -ForegroundColor Green
    Write-Host "The original bug is fixed - yearly period with insufficient duration now returns errors." -ForegroundColor Yellow
} else {
    Write-Host "`nSome tests failed. Check the validation logic." -ForegroundColor Yellow
}

Write-Host "`nBookStation validation suite completed!" -ForegroundColor Green
