# Simple validation test
Write-Host "🧪 Testing Statistics API Validation Rules" -ForegroundColor Green

# Helper function
function Test-Validation {
    param([string]$TestName, [string]$Url, [bool]$ShouldFail)
    
    Write-Host "`n📊 $TestName" -ForegroundColor Yellow
    Write-Host "URL: $Url" -ForegroundColor Gray
    
    try {
        $response = Invoke-RestMethod -Uri $Url -Method GET
        if ($ShouldFail) {
            Write-Host "❌ EXPECTED ERROR BUT GOT: Status $($response.status), Message: $($response.message)" -ForegroundColor Red
        } else {
            Write-Host "✅ SUCCESS: Status $($response.status), Message: $($response.message)" -ForegroundColor Green
            Write-Host "   Data points: $($response.data.Count)" -ForegroundColor Gray
        }
    } catch {
        if ($ShouldFail) {
            Write-Host "✅ EXPECTED ERROR: $($_.Exception.Message)" -ForegroundColor Green
        } else {
            Write-Host "❌ UNEXPECTED ERROR: $($_.Exception.Message)" -ForegroundColor Red
        }
    }
}

$base = "http://localhost:8080/api/books/statistics/summary"

# Test daily validation (>90 days should fail)
Write-Host "`n🎯 DAILY VALIDATION TESTS (Max 90 days)" -ForegroundColor Magenta
Test-Validation "Daily 89 days (Valid)" "$base?period=day&startDate=2025-05-27&endDate=2025-08-24" $false
Test-Validation "Daily 91 days (Invalid)" "$base?period=day&startDate=2025-05-25&endDate=2025-08-24" $true
Test-Validation "Daily 145 days (Invalid)" "$base?period=day&startDate=2025-04-01&endDate=2025-08-24" $true

# Test weekly validation (>2 years should fail)
Write-Host "`n🎯 WEEKLY VALIDATION TESTS (Max 2 years)" -ForegroundColor Magenta
Test-Validation "Weekly 2 years (Valid)" "$base?period=week&startDate=2023-08-24&endDate=2025-08-24" $false
Test-Validation "Weekly 3 years (Invalid)" "$base?period=week&startDate=2022-08-24&endDate=2025-08-24" $true

# Test monthly validation (>10 years should fail)
Write-Host "`n🎯 MONTHLY VALIDATION TESTS (Max 10 years)" -ForegroundColor Magenta
Test-Validation "Monthly 10 years (Valid)" "$base?period=month&startDate=2015-08-24&endDate=2025-08-24" $false
Test-Validation "Monthly 11 years (Invalid)" "$base?period=month&startDate=2014-08-24&endDate=2025-08-24" $true

Write-Host "`n✅ Validation tests completed!" -ForegroundColor Green
