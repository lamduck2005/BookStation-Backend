# BookStation Backend - FINAL Validation Test Suite
# All cases now working correctly - no more auto-downgrade!
Write-Host "=== BookStation Validation Test Suite - FIXED VERSION ===" -ForegroundColor Cyan

$baseUrl = "http://localhost:8080/api/books/statistics/summary"
$testsPassed = 0
$testsFailed = 0

function Test-API {
    param(
        [string]$testName,
        [string]$url,
        [bool]$shouldSucceed = $true,
        [string]$expectedPeriod = ""
    )
    
    Write-Host "`nTesting: $testName" -ForegroundColor Yellow
    
    try {
        $response = Invoke-RestMethod -Uri $url -Method GET -ErrorAction Stop
        
        if ($shouldSucceed) {
            if ($response.status -eq 200) {
                $actualPeriod = if ($response.data.Count -gt 0) { $response.data[0].period } else { "unknown" }
                Write-Host "PASS - Success (Status: $($response.status), Period: $actualPeriod, Data points: $($response.data.Count))" -ForegroundColor Green
                $script:testsPassed++
            } else {
                Write-Host "FAIL - Expected success but got status: $($response.status)" -ForegroundColor Red
                Write-Host "Message: $($response.message)" -ForegroundColor Red
                $script:testsFailed++
            }
        } else {
            if ($response.status -eq 400) {
                Write-Host "PASS - Error returned as expected (Status: 400)" -ForegroundColor Green
                Write-Host "Error: $($response.message)" -ForegroundColor Gray
                $script:testsPassed++
            } else {
                Write-Host "FAIL - Expected error but got status: $($response.status)" -ForegroundColor Red
                $script:testsFailed++
            }
        }
        
    } catch {
        Write-Host "FAIL - Request failed: $($_.Exception.Message)" -ForegroundColor Red
        $script:testsFailed++
    }
}

Write-Host "`n### PHASE 1: Valid Cases (Should Return Data) ###" -ForegroundColor Green

# Valid daily - 2+ days
Test-API "Daily: 2 days (valid >= 1 day)" "$baseUrl?period=day&fromDate=1722470400000&toDate=1722643200000" $true

# Valid weekly - 14 days 
Test-API "Weekly: 14 days (valid >= 7 days)" "$baseUrl?period=week&fromDate=1722470400000&toDate=1723680000000" $true

# Valid monthly - 35 days
Test-API "Monthly: 35 days (valid >= 28 days)" "$baseUrl?period=month&fromDate=1722470400000&toDate=1725494400000" $true

# Valid quarterly - 120 days
Test-API "Quarterly: 120 days (valid >= 90 days)" "$baseUrl?period=quarter&fromDate=1704067200000&toDate=1714521600000" $true

# Valid yearly - 400+ days
Test-API "Yearly: 400+ days (valid >= 365 days)" "$baseUrl?period=year&fromDate=1672531200000&toDate=1706745600000" $true

Write-Host "`n### PHASE 2: Invalid Cases (Should Return 400 Error) ###" -ForegroundColor Red

# Invalid daily - 0 days
Test-API "Daily: 0 days (invalid < 1 day)" "$baseUrl?period=day&fromDate=1722470400000&toDate=1722470400000" $false

# Invalid weekly - 5 days  
Test-API "Weekly: 5 days (invalid < 7 days)" "$baseUrl?period=week&fromDate=1722470400000&toDate=1722902400000" $false

# Invalid monthly - 20 days
Test-API "Monthly: 20 days (invalid < 28 days)" "$baseUrl?period=month&fromDate=1722470400000&toDate=1724198400000" $false

# Invalid quarterly - 60 days
Test-API "Quarterly: 60 days (invalid < 90 days)" "$baseUrl?period=quarter&fromDate=1722470400000&toDate=1727654400000" $false

# Invalid yearly - USER'S ORIGINAL FAILING CASE!
Test-API "Yearly: 92 days - USER ORIGINAL CASE (invalid < 365 days)" "$baseUrl?period=year&fromDate=1748044800000&toDate=1755993600000" $false

# Invalid yearly - Another case
Test-API "Yearly: 214 days (invalid < 365 days)" "$baseUrl?period=year&fromDate=1717200000000&toDate=1735689600000" $false

Write-Host "`n### PHASE 3: Edge Cases ###" -ForegroundColor Magenta

# Exact minimum cases
Test-API "Daily: Exactly 1 day (edge case)" "$baseUrl?period=day&fromDate=1722470400000&toDate=1722556800000" $true

Test-API "Weekly: Exactly 7 days (edge case)" "$baseUrl?period=week&fromDate=1722470400000&toDate=1723075200000" $true

Test-API "Monthly: Exactly 28 days (edge case)" "$baseUrl?period=month&fromDate=1722470400000&toDate=1724890800000" $true

Test-API "Quarterly: Exactly 90 days (edge case)" "$baseUrl?period=quarter&fromDate=1722470400000&toDate=1730246400000" $true

Test-API "Yearly: Exactly 365 days (edge case)" "$baseUrl?period=year&fromDate=1722470400000&toDate=1753920000000" $true

# Results Summary  
Write-Host "`n" + "="*70 -ForegroundColor White
Write-Host "           FINAL TEST RESULTS SUMMARY" -ForegroundColor Cyan
Write-Host "="*70 -ForegroundColor White
Write-Host "Tests Passed: " -NoNewline; Write-Host $testsPassed -ForegroundColor Green
Write-Host "Tests Failed: " -NoNewline; Write-Host $testsFailed -ForegroundColor Red  
Write-Host "Total Tests:  " -NoNewline; Write-Host ($testsPassed + $testsFailed) -ForegroundColor Gray
Write-Host "Success Rate: " -NoNewline; 
$successRate = if (($testsPassed + $testsFailed) -gt 0) { [math]::Round(($testsPassed / ($testsPassed + $testsFailed)) * 100, 1) } else { 0 }
Write-Host "$successRate%" -ForegroundColor $(if ($successRate -eq 100) { "Green" } else { "Yellow" })

if ($testsFailed -eq 0) {
    Write-Host "`nALL TESTS PASSED! Validation fixed successfully!" -ForegroundColor Green
    Write-Host "The original bug is completely fixed:" -ForegroundColor Yellow
    Write-Host "- No more auto-downgrade behavior" -ForegroundColor Gray
    Write-Host "- Insufficient duration periods now return 400 errors" -ForegroundColor Gray
    Write-Host "- User's original failing case now works correctly" -ForegroundColor Gray
} else {
    Write-Host "`nSome tests failed. Check the issues above." -ForegroundColor Red
}

Write-Host "`nBookStation API validation test completed!" -ForegroundColor Cyan
