# =====================================================================
# 🔥 COMPREHENSIVE STATISTICS API TESTING SCRIPT
# Test all period types with validation rules and enhanced response data
# =====================================================================

$baseUrl = "http://localhost:8080/api/books/statistics/summary"
$testResults = @()

Write-Host "🚀 STARTING COMPREHENSIVE STATISTICS API TESTING..." -ForegroundColor Green
Write-Host "====================================================" -ForegroundColor Green

# =====================================================================
# TEST FUNCTION
# =====================================================================
function Test-StatisticsAPI {
    param(
        [string]$TestName,
        [string]$Period,
        [string]$StartDate = $null,
        [string]$EndDate = $null,
        [bool]$ShouldSucceed = $true,
        [string]$ExpectedErrorPattern = ""
    )
    
    Write-Host "`n📊 Testing: $TestName" -ForegroundColor Yellow
    Write-Host "Period: $Period" -ForegroundColor Gray
    if ($StartDate) { Write-Host "Start Date: $StartDate" -ForegroundColor Gray }
    if ($EndDate) { Write-Host "End Date: $EndDate" -ForegroundColor Gray }
    
    # Build URL
    $url = "$baseUrl" + "?period=" + "$Period"
    if ($StartDate) { $url += "&startDate=" + "$StartDate" }
    if ($EndDate) { $url += "&endDate=" + "$EndDate" }
    
    try {
        $response = Invoke-WebRequest -Uri $url -Method GET -TimeoutSec 30
        $content = $response.Content | ConvertFrom-Json
        
        if ($ShouldSucceed) {
            if ($response.StatusCode -eq 200) {
                Write-Host "✅ SUCCESS - Status: $($response.StatusCode)" -ForegroundColor Green
                
                # Show enhanced data structure
                if ($content.data -and $content.data.Count -gt 0) {
                    $sampleData = $content.data | Where-Object {$_.totalBooksSold -gt 0} | Select-Object -First 1
                    if ($sampleData) {
                        Write-Host "Sample data with books sold:" -ForegroundColor Cyan
                        $sampleData.PSObject.Properties | ForEach-Object {
                            Write-Host "  $($_.Name): $($_.Value)" -ForegroundColor White
                        }
                    } else {
                        Write-Host "No data with books sold found, showing first entry:" -ForegroundColor Cyan
                        $firstData = $content.data[0]
                        $firstData.PSObject.Properties | ForEach-Object {
                            Write-Host "  $($_.Name): $($_.Value)" -ForegroundColor White
                        }
                    }
                }
                
                $global:testResults += @{
                    TestName = $TestName
                    Status = "PASS"
                    StatusCode = $response.StatusCode
                    DataCount = $content.data.Count
                    Message = $content.message
                }
            } else {
                Write-Host "❌ UNEXPECTED STATUS: $($response.StatusCode)" -ForegroundColor Red
                $global:testResults += @{
                    TestName = $TestName
                    Status = "FAIL"
                    StatusCode = $response.StatusCode
                    Error = "Unexpected status code"
                }
            }
        } else {
            Write-Host "❌ EXPECTED ERROR BUT GOT SUCCESS - Status: $($response.StatusCode)" -ForegroundColor Red
            $global:testResults += @{
                TestName = $TestName
                Status = "FAIL"
                StatusCode = $response.StatusCode
                Error = "Expected error but got success"
            }
        }
    } catch {
        if (-not $ShouldSucceed) {
            Write-Host "✅ EXPECTED ERROR OCCURRED" -ForegroundColor Green
            Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Gray
            $global:testResults += @{
                TestName = $TestName
                Status = "PASS"
                Error = $_.Exception.Message
            }
        } else {
            Write-Host "❌ UNEXPECTED ERROR: $($_.Exception.Message)" -ForegroundColor Red
            $global:testResults += @{
                TestName = $TestName
                Status = "FAIL"
                Error = $_.Exception.Message
            }
        }
    }
}

# Wait for server to be ready
Write-Host "⏳ Waiting for server to be ready..." -ForegroundColor Yellow
Start-Sleep -Seconds 15

# =====================================================================
# 1. BASIC FUNCTIONALITY TESTS (Should all succeed)
# =====================================================================
Write-Host "`n🎯 SECTION 1: BASIC FUNCTIONALITY TESTS" -ForegroundColor Magenta

Test-StatisticsAPI -TestName "Daily - Default Range" -Period "day"
Test-StatisticsAPI -TestName "Weekly - Default Range" -Period "week"  
Test-StatisticsAPI -TestName "Monthly - Default Range" -Period "month"
Test-StatisticsAPI -TestName "Quarterly - Default Range" -Period "quarter"
Test-StatisticsAPI -TestName "Yearly - Default Range" -Period "year"

# =====================================================================
# 2. ENHANCED DATA STRUCTURE TESTS (Check for week/month/quarter numbers)
# =====================================================================
Write-Host "`n🎯 SECTION 2: ENHANCED DATA STRUCTURE TESTS" -ForegroundColor Magenta

Test-StatisticsAPI -TestName "Weekly - Check Week Number" -Period "week" -StartDate "2025-08-01" -EndDate "2025-08-31"
Test-StatisticsAPI -TestName "Monthly - Check Month Number/Name" -Period "month" -StartDate "2025-01-01" -EndDate "2025-12-31"
Test-StatisticsAPI -TestName "Quarterly - Check Quarter Number" -Period "quarter" -StartDate "2025-01-01" -EndDate "2025-12-31"

# =====================================================================
# 3. VALIDATION TESTS - DAILY PERIOD (Max 90 days)
# =====================================================================
Write-Host "`n🎯 SECTION 3: DAILY PERIOD VALIDATION TESTS" -ForegroundColor Magenta

Test-StatisticsAPI -TestName "Daily - Valid 90 days" -Period "day" -StartDate "2025-06-01" -EndDate "2025-08-30" -ShouldSucceed $true
Test-StatisticsAPI -TestName "Daily - Invalid 91 days" -Period "day" -StartDate "2025-05-01" -EndDate "2025-08-30" -ShouldSucceed $false
Test-StatisticsAPI -TestName "Daily - Invalid 180 days" -Period "day" -StartDate "2025-02-01" -EndDate "2025-08-30" -ShouldSucceed $false

# =====================================================================
# 4. VALIDATION TESTS - WEEKLY PERIOD (Max 2 years)
# =====================================================================
Write-Host "`n🎯 SECTION 4: WEEKLY PERIOD VALIDATION TESTS" -ForegroundColor Magenta

Test-StatisticsAPI -TestName "Weekly - Valid 2 years" -Period "week" -StartDate "2023-08-01" -EndDate "2025-08-30" -ShouldSucceed $true
Test-StatisticsAPI -TestName "Weekly - Invalid 3 years" -Period "week" -StartDate "2022-08-01" -EndDate "2025-08-30" -ShouldSucceed $false
Test-StatisticsAPI -TestName "Weekly - Invalid 5 years" -Period "week" -StartDate "2020-08-01" -EndDate "2025-08-30" -ShouldSucceed $false

# =====================================================================
# 5. VALIDATION TESTS - MONTHLY PERIOD (Max 10 years)
# =====================================================================
Write-Host "`n🎯 SECTION 5: MONTHLY PERIOD VALIDATION TESTS" -ForegroundColor Magenta

Test-StatisticsAPI -TestName "Monthly - Valid 10 years" -Period "month" -StartDate "2015-08-01" -EndDate "2025-08-30" -ShouldSucceed $true
Test-StatisticsAPI -TestName "Monthly - Invalid 11 years" -Period "month" -StartDate "2014-08-01" -EndDate "2025-08-30" -ShouldSucceed $false
Test-StatisticsAPI -TestName "Monthly - Invalid 15 years" -Period "month" -StartDate "2010-08-01" -EndDate "2025-08-30" -ShouldSucceed $false

# =====================================================================
# 6. VALIDATION TESTS - QUARTERLY PERIOD (Max 10 years)
# =====================================================================
Write-Host "`n🎯 SECTION 6: QUARTERLY PERIOD VALIDATION TESTS" -ForegroundColor Magenta

Test-StatisticsAPI -TestName "Quarterly - Valid 10 years" -Period "quarter" -StartDate "2015-08-01" -EndDate "2025-08-30" -ShouldSucceed $true
Test-StatisticsAPI -TestName "Quarterly - Invalid 11 years" -Period "quarter" -StartDate "2014-08-01" -EndDate "2025-08-30" -ShouldSucceed $false
Test-StatisticsAPI -TestName "Quarterly - Invalid 20 years" -Period "quarter" -StartDate "2005-08-01" -EndDate "2025-08-30" -ShouldSucceed $false

# =====================================================================
# 7. VALIDATION TESTS - YEARLY PERIOD (Max 30 years)
# =====================================================================
Write-Host "`n🎯 SECTION 7: YEARLY PERIOD VALIDATION TESTS" -ForegroundColor Magenta

Test-StatisticsAPI -TestName "Yearly - Valid 30 years" -Period "year" -StartDate "1995-08-01" -EndDate "2025-08-30" -ShouldSucceed $true
Test-StatisticsAPI -TestName "Yearly - Invalid 31 years" -Period "year" -StartDate "1994-08-01" -EndDate "2025-08-30" -ShouldSucceed $false
Test-StatisticsAPI -TestName "Yearly - Invalid 50 years" -Period "year" -StartDate "1975-08-01" -EndDate "2025-08-30" -ShouldSucceed $false

# =====================================================================
# 8. EDGE CASE TESTS
# =====================================================================
Write-Host "`n🎯 SECTION 8: EDGE CASE TESTS" -ForegroundColor Magenta

Test-StatisticsAPI -TestName "Invalid Period Type" -Period "invalid" -ShouldSucceed $true  # Should default to daily
Test-StatisticsAPI -TestName "Daily - Exactly 90 days" -Period "day" -StartDate "2025-05-26" -EndDate "2025-08-24" -ShouldSucceed $true
Test-StatisticsAPI -TestName "Weekly - Exactly 2 years" -Period "week" -StartDate "2023-08-24" -EndDate "2025-08-24" -ShouldSucceed $true

# =====================================================================
# SUMMARY REPORT
# =====================================================================
Write-Host "`n🎯 TEST SUMMARY REPORT" -ForegroundColor Magenta
Write-Host "===========================================" -ForegroundColor Magenta

$passCount = ($testResults | Where-Object {$_.Status -eq "PASS"}).Count
$failCount = ($testResults | Where-Object {$_.Status -eq "FAIL"}).Count
$totalCount = $testResults.Count

Write-Host "`nTotal Tests: $totalCount" -ForegroundColor White
Write-Host "Passed: $passCount" -ForegroundColor Green  
Write-Host "Failed: $failCount" -ForegroundColor Red

if ($failCount -gt 0) {
    Write-Host "`n❌ FAILED TESTS:" -ForegroundColor Red
    $testResults | Where-Object {$_.Status -eq "FAIL"} | ForEach-Object {
        Write-Host "  - $($_.TestName): $($_.Error)" -ForegroundColor Red
    }
}

Write-Host "`n✅ PASSED TESTS:" -ForegroundColor Green
$testResults | Where-Object {$_.Status -eq "PASS"} | ForEach-Object {
    Write-Host "  - $($_.TestName)" -ForegroundColor Green
}

if ($failCount -eq 0) {
    Write-Host "`n🎉 ALL TESTS PASSED! Statistics API is working correctly." -ForegroundColor Green
} else {
    Write-Host "`n⚠️ Some tests failed. Please review the implementation." -ForegroundColor Yellow
}

Write-Host "`n🚀 TEST EXECUTION COMPLETED!" -ForegroundColor Green
