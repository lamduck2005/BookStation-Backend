# 🧪 Order Statistics API Validation Test Script
# Test tất cả các trường hợp validation limits và incomplete period handling

$baseUrl = "http://localhost:8080/api/orders/statistics/summary"
$headers = @{ "Content-Type" = "application/json" }

Write-Host "🔥 Testing Order Statistics API Validation Limits..." -ForegroundColor Yellow
Write-Host "=================================================" -ForegroundColor Yellow

# Helper function để tính timestamp
function Get-TimestampDaysAgo($days) {
    $date = (Get-Date).AddDays(-$days)
    return [Math]::Floor(($date - [datetime]"1970-01-01T00:00:00Z").TotalMilliseconds)
}

function Get-TimestampFromDate($dateStr) {
    $date = [datetime]::ParseExact($dateStr, "yyyy-MM-dd", $null)
    return [Math]::Floor(($date - [datetime]"1970-01-01T00:00:00Z").TotalMilliseconds)
}

# Test Daily Period Limits
Write-Host "`n🔸 DAILY PERIOD VALIDATION:" -ForegroundColor Cyan

# Test case 1: Valid daily (30 ngày)
$from30Days = Get-TimestampDaysAgo 30
$toToday = Get-TimestampDaysAgo 0
$url = "$baseUrl?period=day&fromDate=$from30Days&toDate=$toToday"
Write-Host "✅ Valid Daily (30 days): " -NoNewline
try {
    $response = Invoke-RestMethod -Uri $url -Method GET -Headers $headers
    if ($response.status -eq 200) {
        Write-Host "SUCCESS - Got $($response.data.Count) records" -ForegroundColor Green
    } else {
        Write-Host "FAILED - Status: $($response.status)" -ForegroundColor Red
    }
} catch {
    Write-Host "ERROR - $($_.Exception.Message)" -ForegroundColor Red
}

# Test case 2: Daily too long (100 ngày)
$from100Days = Get-TimestampDaysAgo 100
$url = "$baseUrl?period=day&fromDate=$from100Days&toDate=$toToday"
Write-Host "❌ Daily Too Long (100 days): " -NoNewline
try {
    $response = Invoke-RestMethod -Uri $url -Method GET -Headers $headers
    if ($response.status -eq 400) {
        Write-Host "CORRECT VALIDATION - $($response.message)" -ForegroundColor Green
    } else {
        Write-Host "FAILED - Should be 400 but got: $($response.status)" -ForegroundColor Red
    }
} catch {
    Write-Host "ERROR - $($_.Exception.Message)" -ForegroundColor Red
}

# Test Weekly Period Limits
Write-Host "`n🔸 WEEKLY PERIOD VALIDATION:" -ForegroundColor Cyan

# Test case 3: Valid weekly (2 tháng = 60 ngày)
$from60Days = Get-TimestampDaysAgo 60
$url = "$baseUrl?period=week&fromDate=$from60Days&toDate=$toToday"
Write-Host "✅ Valid Weekly (60 days): " -NoNewline
try {
    $response = Invoke-RestMethod -Uri $url -Method GET -Headers $headers
    if ($response.status -eq 200) {
        Write-Host "SUCCESS - Got $($response.data.Count) records" -ForegroundColor Green
    } else {
        Write-Host "FAILED - Status: $($response.status)" -ForegroundColor Red
    }
} catch {
    Write-Host "ERROR - $($_.Exception.Message)" -ForegroundColor Red
}

# Test case 4: Weekly too short (5 ngày)
$from5Days = Get-TimestampDaysAgo 5
$url = "$baseUrl?period=week&fromDate=$from5Days&toDate=$toToday"
Write-Host "❌ Weekly Too Short (5 days): " -NoNewline
try {
    $response = Invoke-RestMethod -Uri $url -Method GET -Headers $headers
    if ($response.status -eq 400) {
        Write-Host "CORRECT VALIDATION - $($response.message)" -ForegroundColor Green
    } else {
        Write-Host "FAILED - Should be 400 but got: $($response.status)" -ForegroundColor Red
    }
} catch {
    Write-Host "ERROR - $($_.Exception.Message)" -ForegroundColor Red
}

# Test case 5: Weekly too long (3 năm = 1095 ngày)
$from3Years = Get-TimestampDaysAgo 1095
$url = "$baseUrl?period=week&fromDate=$from3Years&toDate=$toToday"
Write-Host "❌ Weekly Too Long (3 years): " -NoNewline
try {
    $response = Invoke-RestMethod -Uri $url -Method GET -Headers $headers
    if ($response.status -eq 400) {
        Write-Host "CORRECT VALIDATION - $($response.message)" -ForegroundColor Green
    } else {
        Write-Host "FAILED - Should be 400 but got: $($response.status)" -ForegroundColor Red
    }
} catch {
    Write-Host "ERROR - $($_.Exception.Message)" -ForegroundColor Red
}

# Test Monthly Period Limits
Write-Host "`n🔸 MONTHLY PERIOD VALIDATION:" -ForegroundColor Cyan

# Test case 6: Valid monthly (1 năm)
$from1Year = Get-TimestampDaysAgo 365
$url = "$baseUrl?period=month&fromDate=$from1Year&toDate=$toToday"
Write-Host "✅ Valid Monthly (365 days): " -NoNewline
try {
    $response = Invoke-RestMethod -Uri $url -Method GET -Headers $headers
    if ($response.status -eq 200) {
        Write-Host "SUCCESS - Got $($response.data.Count) records" -ForegroundColor Green
    } else {
        Write-Host "FAILED - Status: $($response.status)" -ForegroundColor Red
    }
} catch {
    Write-Host "ERROR - $($_.Exception.Message)" -ForegroundColor Red
}

# Test case 7: Monthly too short (20 ngày)
$from20Days = Get-TimestampDaysAgo 20
$url = "$baseUrl?period=month&fromDate=$from20Days&toDate=$toToday"
Write-Host "❌ Monthly Too Short (20 days): " -NoNewline
try {
    $response = Invoke-RestMethod -Uri $url -Method GET -Headers $headers
    if ($response.status -eq 400) {
        Write-Host "CORRECT VALIDATION - $($response.message)" -ForegroundColor Green
    } else {
        Write-Host "FAILED - Should be 400 but got: $($response.status)" -ForegroundColor Red
    }
} catch {
    Write-Host "ERROR - $($_.Exception.Message)" -ForegroundColor Red
}

# Test Quarterly Period Limits
Write-Host "`n🔸 QUARTERLY PERIOD VALIDATION:" -ForegroundColor Cyan

# Test case 8: Valid quarterly (2 năm)
$from2Years = Get-TimestampDaysAgo 730
$url = "$baseUrl?period=quarter&fromDate=$from2Years&toDate=$toToday"
Write-Host "✅ Valid Quarterly (730 days): " -NoNewline
try {
    $response = Invoke-RestMethod -Uri $url -Method GET -Headers $headers
    if ($response.status -eq 200) {
        Write-Host "SUCCESS - Got $($response.data.Count) records" -ForegroundColor Green
    } else {
        Write-Host "FAILED - Status: $($response.status)" -ForegroundColor Red
    }
} catch {
    Write-Host "ERROR - $($_.Exception.Message)" -ForegroundColor Red
}

# Test case 9: Quarterly too short (60 ngày)
$url = "$baseUrl?period=quarter&fromDate=$from60Days&toDate=$toToday"
Write-Host "❌ Quarterly Too Short (60 days): " -NoNewline
try {
    $response = Invoke-RestMethod -Uri $url -Method GET -Headers $headers
    if ($response.status -eq 400) {
        Write-Host "CORRECT VALIDATION - $($response.message)" -ForegroundColor Green
    } else {
        Write-Host "FAILED - Should be 400 but got: $($response.status)" -ForegroundColor Red
    }
} catch {
    Write-Host "ERROR - $($_.Exception.Message)" -ForegroundColor Red
}

# Test Yearly Period Limits  
Write-Host "`n🔸 YEARLY PERIOD VALIDATION:" -ForegroundColor Cyan

# Test case 10: Valid yearly (3 năm)
$url = "$baseUrl?period=year&fromDate=$from3Years&toDate=$toToday"
Write-Host "✅ Valid Yearly (3 years): " -NoNewline
try {
    $response = Invoke-RestMethod -Uri $url -Method GET -Headers $headers
    if ($response.status -eq 200) {
        Write-Host "SUCCESS - Got $($response.data.Count) records" -ForegroundColor Green
    } else {
        Write-Host "FAILED - Status: $($response.status)" -ForegroundColor Red
    }
} catch {
    Write-Host "ERROR - $($_.Exception.Message)" -ForegroundColor Red
}

# Test case 11: Yearly too short (200 ngày)
$from200Days = Get-TimestampDaysAgo 200
$url = "$baseUrl?period=year&fromDate=$from200Days&toDate=$toToday"
Write-Host "❌ Yearly Too Short (200 days): " -NoNewline
try {
    $response = Invoke-RestMethod -Uri $url -Method GET -Headers $headers
    if ($response.status -eq 400) {
        Write-Host "CORRECT VALIDATION - $($response.message)" -ForegroundColor Green
    } else {
        Write-Host "FAILED - Should be 400 but got: $($response.status)" -ForegroundColor Red
    }
} catch {
    Write-Host "ERROR - $($_.Exception.Message)" -ForegroundColor Red
}

# Test Incomplete Period Handling
Write-Host "`n🔸 INCOMPLETE PERIOD HANDLING:" -ForegroundColor Cyan

# Test case 12: Weekly với chỉ 10 ngày (sẽ chấp nhận vì >= 7 ngày, nhưng endDate sẽ reflect đúng range)
$from10Days = Get-TimestampDaysAgo 10
$url = "$baseUrl?period=week&fromDate=$from10Days&toDate=$toToday"
Write-Host "⚠️  Weekly Incomplete (10 days): " -NoNewline
try {
    $response = Invoke-RestMethod -Uri $url -Method GET -Headers $headers
    if ($response.status -eq 200) {
        $firstRecord = $response.data[0]
        $endDate = $firstRecord.endDate
        $startDate = $firstRecord.startDate
        Write-Host "SUCCESS - startDate: $startDate, endDate: $endDate" -ForegroundColor Green
        if ($firstRecord.dateRange) {
            Write-Host "          dateRange: $($firstRecord.dateRange)" -ForegroundColor Yellow
        }
    } else {
        Write-Host "FAILED - Status: $($response.status)" -ForegroundColor Red
    }
} catch {
    Write-Host "ERROR - $($_.Exception.Message)" -ForegroundColor Red
}

# Test case 13: Monthly với 35 ngày (sẽ chấp nhận vì >= 28 ngày)
$from35Days = Get-TimestampDaysAgo 35
$url = "$baseUrl?period=month&fromDate=$from35Days&toDate=$toToday"
Write-Host "⚠️  Monthly Incomplete (35 days): " -NoNewline
try {
    $response = Invoke-RestMethod -Uri $url -Method GET -Headers $headers
    if ($response.status -eq 200) {
        $firstRecord = $response.data[0]
        $endDate = $firstRecord.endDate
        $startDate = $firstRecord.startDate
        Write-Host "SUCCESS - startDate: $startDate, endDate: $endDate" -ForegroundColor Green
        if ($firstRecord.dateRange) {
            Write-Host "          dateRange: $($firstRecord.dateRange)" -ForegroundColor Yellow
        }
    } else {
        Write-Host "FAILED - Status: $($response.status)" -ForegroundColor Red
    }
} catch {
    Write-Host "ERROR - $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n🔥 Validation Testing Complete!" -ForegroundColor Yellow
Write-Host "=================================================" -ForegroundColor Yellow
