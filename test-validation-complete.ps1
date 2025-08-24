# 📊 BookStation Backend - Complete Validation Test Suite
# Test all period validation scenarios after fixing API
Write-Host "🔍 Starting BookStation Validation Test Suite..." -ForegroundColor Cyan

$baseUrl = "http://localhost:8080/api/books/statistics/summary"
$testsPassed = 0
$testsFailed = 0

function Test-API {
    param(
        [string]$testName,
        [string]$url,
        [bool]$shouldSucceed = $true,
        [string]$expectedError = ""
    )
    
    Write-Host "`n📝 Testing: $testName" -ForegroundColor Yellow
    Write-Host "URL: $url" -ForegroundColor Gray
    
    try {
        $response = Invoke-RestMethod -Uri $url -Method GET -ErrorAction Stop
        
        if ($shouldSucceed) {
            if ($response.status -eq 200) {
                Write-Host "✅ PASS - Success as expected" -ForegroundColor Green
                Write-Host "   Data points: $($response.data.Count)" -ForegroundColor Gray
                $script:testsPassed++
            } else {
                Write-Host "❌ FAIL - Expected success but got status: $($response.status)" -ForegroundColor Red
                Write-Host "   Message: $($response.message)" -ForegroundColor Red
                $script:testsFailed++
            }
        } else {
            if ($response.status -eq 400) {
                Write-Host "✅ PASS - Error returned as expected" -ForegroundColor Green
                Write-Host "   Error: $($response.message)" -ForegroundColor Gray
                $script:testsPassed++
            } else {
                Write-Host "❌ FAIL - Expected error but got status: $($response.status)" -ForegroundColor Red
                $script:testsFailed++
            }
        }
        
    } catch {
        if ($shouldSucceed) {
            Write-Host "❌ FAIL - Request failed: $($_.Exception.Message)" -ForegroundColor Red
            $script:testsFailed++
        } else {
            Write-Host "✅ PASS - Request failed as expected" -ForegroundColor Green
            $script:testsPassed++
        }
    }
}

# 🎯 Test Cases
Write-Host "`n🎯 PHASE 1: Valid Scenarios (Should Pass)" -ForegroundColor Magenta

# Valid daily - 2 days
$fromDate = [DateTimeOffset]::new(2024, 8, 1, 0, 0, 0, [TimeSpan]::Zero).ToUnixTimeMilliseconds()
$toDate = [DateTimeOffset]::new(2024, 8, 3, 0, 0, 0, [TimeSpan]::Zero).ToUnixTimeMilliseconds()
Test-API "Daily: 2 days (valid)" "$baseUrl?period=day&fromDate=$fromDate&toDate=$toDate" $true

# Valid weekly - 14 days (2 weeks)
$fromDate = [DateTimeOffset]::new(2024, 8, 1, 0, 0, 0, [TimeSpan]::Zero).ToUnixTimeMilliseconds()
$toDate = [DateTimeOffset]::new(2024, 8, 15, 0, 0, 0, [TimeSpan]::Zero).ToUnixTimeMilliseconds()
Test-API "Weekly: 14 days (valid)" "$baseUrl?period=week&fromDate=$fromDate&toDate=$toDate" $true

# Valid monthly - 62 days (2+ months)
$fromDate = [DateTimeOffset]::new(2024, 8, 1, 0, 0, 0, [TimeSpan]::Zero).ToUnixTimeMilliseconds()
$toDate = [DateTimeOffset]::new(2024, 10, 2, 0, 0, 0, [TimeSpan]::Zero).ToUnixTimeMilliseconds()
Test-API "Monthly: 62 days (valid)" "$baseUrl?period=month&fromDate=$fromDate&toDate=$toDate" $true

# Valid quarterly - 180 days (2 quarters)
$fromDate = [DateTimeOffset]::new(2024, 1, 1, 0, 0, 0, [TimeSpan]::Zero).ToUnixTimeMilliseconds()
$toDate = [DateTimeOffset]::new(2024, 7, 1, 0, 0, 0, [TimeSpan]::Zero).ToUnixTimeMilliseconds()
Test-API "Quarterly: 182 days (valid)" "$baseUrl?period=quarter&fromDate=$fromDate&toDate=$toDate" $true

# Valid yearly - 400 days (1+ year)
$fromDate = [DateTimeOffset]::new(2023, 1, 1, 0, 0, 0, [TimeSpan]::Zero).ToUnixTimeMilliseconds()
$toDate = [DateTimeOffset]::new(2024, 2, 5, 0, 0, 0, [TimeSpan]::Zero).ToUnixTimeMilliseconds()
Test-API "Yearly: 400 days (valid)" "$baseUrl?period=year&fromDate=$fromDate&toDate=$toDate" $true

Write-Host "`n🚨 PHASE 2: Invalid Scenarios (Should Fail)" -ForegroundColor Magenta

# Invalid daily - 0 days
$fromDate = [DateTimeOffset]::new(2024, 8, 1, 12, 0, 0, [TimeSpan]::Zero).ToUnixTimeMilliseconds()
$toDate = [DateTimeOffset]::new(2024, 8, 1, 12, 0, 0, [TimeSpan]::Zero).ToUnixTimeMilliseconds()
Test-API "Daily: 0 days (invalid)" "$baseUrl?period=day&fromDate=$fromDate&toDate=$toDate" $false

# Invalid weekly - 5 days (< 7 days)
$fromDate = [DateTimeOffset]::new(2024, 8, 1, 0, 0, 0, [TimeSpan]::Zero).ToUnixTimeMilliseconds()
$toDate = [DateTimeOffset]::new(2024, 8, 6, 0, 0, 0, [TimeSpan]::Zero).ToUnixTimeMilliseconds()
Test-API "Weekly: 5 days (invalid)" "$baseUrl?period=week&fromDate=$fromDate&toDate=$toDate" $false

# Invalid monthly - 20 days (< 28 days)
$fromDate = [DateTimeOffset]::new(2024, 8, 1, 0, 0, 0, [TimeSpan]::Zero).ToUnixTimeMilliseconds()
$toDate = [DateTimeOffset]::new(2024, 8, 21, 0, 0, 0, [TimeSpan]::Zero).ToUnixTimeMilliseconds()
Test-API "Monthly: 20 days (invalid)" "$baseUrl?period=month&fromDate=$fromDate&toDate=$toDate" $false

# Invalid quarterly - 60 days (< 90 days)
$fromDate = [DateTimeOffset]::new(2024, 8, 1, 0, 0, 0, [TimeSpan]::Zero).ToUnixTimeMilliseconds()
$toDate = [DateTimeOffset]::new(2024, 9, 30, 0, 0, 0, [TimeSpan]::Zero).ToUnixTimeMilliseconds()
Test-API "Quarterly: 60 days (invalid)" "$baseUrl?period=quarter&fromDate=$fromDate&toDate=$toDate" $false

# Invalid yearly - 214 days (< 365 days) - Original failing case!
$fromDate = [DateTimeOffset]::new(2024, 6, 1, 0, 0, 0, [TimeSpan]::Zero).ToUnixTimeMilliseconds()
$toDate = [DateTimeOffset]::new(2025, 1, 1, 0, 0, 0, [TimeSpan]::Zero).ToUnixTimeMilliseconds()
Test-API "Yearly: 214 days - ORIGINAL FAILING CASE (invalid)" "$baseUrl?period=year&fromDate=$fromDate&toDate=$toDate" $false

Write-Host "`n⚖️ PHASE 3: Maximum Limits (Should Fail if over limits)" -ForegroundColor Magenta

# Daily over 90 days limit
$fromDate = [DateTimeOffset]::new(2024, 1, 1, 0, 0, 0, [TimeSpan]::Zero).ToUnixTimeMilliseconds()
$toDate = [DateTimeOffset]::new(2024, 4, 1, 0, 0, 0, [TimeSpan]::Zero).ToUnixTimeMilliseconds()
Test-API "Daily: 91 days (over max limit)" "$baseUrl?period=day&fromDate=$fromDate&toDate=$toDate" $false

# Weekly over 2 years limit
$fromDate = [DateTimeOffset]::new(2021, 1, 1, 0, 0, 0, [TimeSpan]::Zero).ToUnixTimeMilliseconds()
$toDate = [DateTimeOffset]::new(2024, 1, 2, 0, 0, 0, [TimeSpan]::Zero).ToUnixTimeMilliseconds()
Test-API "Weekly: 3+ years (over max limit)" "$baseUrl?period=week&fromDate=$fromDate&toDate=$toDate" $false

# Monthly over 5 years limit
$fromDate = [DateTimeOffset]::new(2018, 1, 1, 0, 0, 0, [TimeSpan]::Zero).ToUnixTimeMilliseconds()
$toDate = [DateTimeOffset]::new(2024, 1, 2, 0, 0, 0, [TimeSpan]::Zero).ToUnixTimeMilliseconds()
Test-API "Monthly: 6+ years (over max limit)" "$baseUrl?period=month&fromDate=$fromDate&toDate=$toDate" $false

# Yearly over 25 years limit
$fromDate = [DateTimeOffset]::new(1990, 1, 1, 0, 0, 0, [TimeSpan]::Zero).ToUnixTimeMilliseconds()
$toDate = [DateTimeOffset]::new(2024, 1, 2, 0, 0, 0, [TimeSpan]::Zero).ToUnixTimeMilliseconds()
Test-API "Yearly: 34+ years (over max limit)" "$baseUrl?period=year&fromDate=$fromDate&toDate=$toDate" $false

# 📊 Results Summary
Write-Host "`n" + "="*60 -ForegroundColor White
Write-Host "📊 TEST SUMMARY RESULTS" -ForegroundColor Cyan
Write-Host "="*60 -ForegroundColor White
Write-Host "✅ Tests Passed: $testsPassed" -ForegroundColor Green
Write-Host "❌ Tests Failed: $testsFailed" -ForegroundColor Red
Write-Host "📝 Total Tests: $($testsPassed + $testsFailed)" -ForegroundColor Gray

if ($testsFailed -eq 0) {
    Write-Host "`n🎉 ALL TESTS PASSED! Validation is working correctly!" -ForegroundColor Green
    Write-Host "✨ The original bug is fixed - yearly period with insufficient duration now returns errors instead of auto-downgrading." -ForegroundColor Yellow
} else {
    Write-Host "`n⚠️  Some tests failed. Check the validation logic." -ForegroundColor Yellow
}

Write-Host "`n🔧 Technical Summary:" -ForegroundColor Cyan
Write-Host "   - Fixed controller parameter type conversion issue" -ForegroundColor Gray
Write-Host "   - Implemented comprehensive validateDateRangeForPeriod method" -ForegroundColor Gray  
Write-Host "   - Added minimum duration checks for all period types" -ForegroundColor Gray
Write-Host "   - Added maximum duration limits as requested" -ForegroundColor Gray
Write-Host "   - Vietnamese error messages included" -ForegroundColor Gray
Write-Host "`nBookStation validation suite completed!" -ForegroundColor Green
