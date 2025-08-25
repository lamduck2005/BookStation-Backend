# Quick Validation Test Script
$baseUrl = "http://localhost:8080/api/orders/statistics/summary"

Write-Host "🧪 Testing Order API Validation..." -ForegroundColor Yellow

# Test case 1: Quarterly with only 23 days (should fail)
Write-Host "`n1. Testing quarterly with 23 days (should fail):" -NoNewline
$url = "${baseUrl}?period=quarter&fromDate=1754154000000&toDate=1756141199000"
try {
    $response = Invoke-RestMethod -Uri $url -Method GET
    if ($response.status -eq 400) {
        Write-Host " ✅ PASS - Validation working" -ForegroundColor Green
        Write-Host "   Message: $($response.message)" -ForegroundColor Yellow
    } else {
        Write-Host " ❌ FAIL - Got status $($response.status)" -ForegroundColor Red
        Write-Host "   This should return 400 error" -ForegroundColor Red
    }
} catch {
    Write-Host " ❌ ERROR: $($_.Exception.Message)" -ForegroundColor Red
}

# Test case 2: Weekly with 5 days (should fail)  
Write-Host "`n2. Testing weekly with 5 days (should fail):" -NoNewline
$from5Days = [Math]::Floor(((Get-Date).AddDays(-5) - [datetime]"1970-01-01T00:00:00Z").TotalMilliseconds)
$toToday = [Math]::Floor(((Get-Date) - [datetime]"1970-01-01T00:00:00Z").TotalMilliseconds)
$url = "${baseUrl}?period=week&fromDate=$from5Days&toDate=$toToday"
try {
    $response = Invoke-RestMethod -Uri $url -Method GET
    if ($response.status -eq 400) {
        Write-Host " ✅ PASS - Validation working" -ForegroundColor Green
        Write-Host "   Message: $($response.message)" -ForegroundColor Yellow
    } else {
        Write-Host " ❌ FAIL - Got status $($response.status)" -ForegroundColor Red
        Write-Host "   This should return 400 error" -ForegroundColor Red
    }
} catch {
    Write-Host " ❌ ERROR: $($_.Exception.Message)" -ForegroundColor Red
}

# Test case 3: Daily with 30 days (should pass)
Write-Host "`n3. Testing daily with 30 days (should pass):" -NoNewline
$from30Days = [Math]::Floor(((Get-Date).AddDays(-30) - [datetime]"1970-01-01T00:00:00Z").TotalMilliseconds)
$url = "${baseUrl}?period=day&fromDate=$from30Days&toDate=$toToday"
try {
    $response = Invoke-RestMethod -Uri $url -Method GET
    if ($response.status -eq 200) {
        Write-Host " ✅ PASS - Valid request accepted" -ForegroundColor Green
        Write-Host "   Got $($response.data.Count) records" -ForegroundColor Yellow
    } else {
        Write-Host " ❌ FAIL - Got status $($response.status)" -ForegroundColor Red
    }
} catch {
    Write-Host " ❌ ERROR: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n🔥 Test Complete!" -ForegroundColor Yellow
