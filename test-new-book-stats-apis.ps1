# 📊 TEST SCRIPT CHO NEW BOOK STATISTICS APIs v4.0
# Test 2-Tier Architecture: Summary + Details APIs

Write-Host "🚀 Testing BookStation New Book Statistics APIs v4.0" -ForegroundColor Green
Write-Host "=" * 60

$baseUrl = "http://localhost:8080"

# ==============================================================
# TEST 1: TIER 1 - SUMMARY API (Chart Overview)
# ==============================================================

Write-Host "`n📊 TEST 1: Summary API - Different Periods" -ForegroundColor Cyan

# Test 1a: Daily summary (default)
Write-Host "`n🔸 Testing daily summary (default)..."
try {
    $response = Invoke-RestMethod -Uri "$baseUrl/api/admin/books/statistics/summary?period=day" -Method Get -ContentType "application/json"
    Write-Host "✅ Daily Summary Response:" -ForegroundColor Green
    $response | ConvertTo-Json -Depth 3
} catch {
    Write-Host "❌ Daily Summary Error: $($_.Exception.Message)" -ForegroundColor Red
}

# Test 1b: Weekly summary
Write-Host "`n🔸 Testing weekly summary..."
try {
    $response = Invoke-RestMethod -Uri "$baseUrl/api/admin/books/statistics/summary?period=week" -Method Get -ContentType "application/json"
    Write-Host "✅ Weekly Summary Response:" -ForegroundColor Green
    $response | ConvertTo-Json -Depth 3
} catch {
    Write-Host "❌ Weekly Summary Error: $($_.Exception.Message)" -ForegroundColor Red
}

# Test 1c: Monthly summary
Write-Host "`n🔸 Testing monthly summary..."
try {
    $response = Invoke-RestMethod -Uri "$baseUrl/api/admin/books/statistics/summary?period=month" -Method Get -ContentType "application/json"
    Write-Host "✅ Monthly Summary Response:" -ForegroundColor Green
    $response | ConvertTo-Json -Depth 3
} catch {
    Write-Host "❌ Monthly Summary Error: $($_.Exception.Message)" -ForegroundColor Red
}

# Test 1d: Custom period
Write-Host "`n🔸 Testing custom period (last 10 days)..."
$endDate = [DateTimeOffset]::UtcNow.ToUnixTimeMilliseconds()
$startDate = $endDate - (10 * 24 * 60 * 60 * 1000)  # 10 days ago
try {
    $response = Invoke-RestMethod -Uri "$baseUrl/api/admin/books/statistics/summary?period=custom&fromDate=$startDate&toDate=$endDate" -Method Get -ContentType "application/json"
    Write-Host "✅ Custom Period Summary Response:" -ForegroundColor Green
    $response | ConvertTo-Json -Depth 3
} catch {
    Write-Host "❌ Custom Period Summary Error: $($_.Exception.Message)" -ForegroundColor Red
}

# ==============================================================
# TEST 2: TIER 2 - DETAILS API (Drill-down)
# ==============================================================

Write-Host "`n`n📋 TEST 2: Details API - Book Details for Specific Date" -ForegroundColor Cyan

# Test 2a: Daily details for today
$today = Get-Date -Format "yyyy-MM-dd"
Write-Host "`n🔸 Testing daily details for today ($today)..."
try {
    $response = Invoke-RestMethod -Uri "$baseUrl/api/admin/books/statistics/details?period=day&date=$today&limit=5" -Method Get -ContentType "application/json"
    Write-Host "✅ Daily Details Response:" -ForegroundColor Green
    $response | ConvertTo-Json -Depth 3
} catch {
    Write-Host "❌ Daily Details Error: $($_.Exception.Message)" -ForegroundColor Red
}

# Test 2b: Weekly details
Write-Host "`n🔸 Testing weekly details..."
try {
    $response = Invoke-RestMethod -Uri "$baseUrl/api/admin/books/statistics/details?period=week&date=$today&limit=3" -Method Get -ContentType "application/json"
    Write-Host "✅ Weekly Details Response:" -ForegroundColor Green
    $response | ConvertTo-Json -Depth 3
} catch {
    Write-Host "❌ Weekly Details Error: $($_.Exception.Message)" -ForegroundColor Red
}

# Test 2c: Monthly details
Write-Host "`n🔸 Testing monthly details..."
try {
    $response = Invoke-RestMethod -Uri "$baseUrl/api/admin/books/statistics/details?period=month&date=$today&limit=7" -Method Get -ContentType "application/json"
    Write-Host "✅ Monthly Details Response:" -ForegroundColor Green
    $response | ConvertTo-Json -Depth 3
} catch {
    Write-Host "❌ Monthly Details Error: $($_.Exception.Message)" -ForegroundColor Red
}

# ==============================================================
# TEST 3: EDGE CASES
# ==============================================================

Write-Host "`n`n⚠️  TEST 3: Edge Cases & Error Handling" -ForegroundColor Yellow

# Test 3a: Invalid period
Write-Host "`n🔸 Testing invalid period..."
try {
    $response = Invoke-RestMethod -Uri "$baseUrl/api/admin/books/statistics/summary?period=invalid" -Method Get -ContentType "application/json"
    Write-Host "✅ Invalid Period Response:" -ForegroundColor Green
    $response | ConvertTo-Json -Depth 3
} catch {
    Write-Host "❌ Invalid Period Error (Expected): $($_.Exception.Message)" -ForegroundColor Yellow
}

# Test 3b: Missing required date for details
Write-Host "`n🔸 Testing missing date parameter..."
try {
    $response = Invoke-RestMethod -Uri "$baseUrl/api/admin/books/statistics/details?period=day&limit=5" -Method Get -ContentType "application/json"
    Write-Host "✅ Missing Date Response:" -ForegroundColor Green
    $response | ConvertTo-Json -Depth 3
} catch {
    Write-Host "❌ Missing Date Error (Expected): $($_.Exception.Message)" -ForegroundColor Yellow
}

# Test 3c: Custom period without dates
Write-Host "`n🔸 Testing custom period without fromDate/toDate..."
try {
    $response = Invoke-RestMethod -Uri "$baseUrl/api/admin/books/statistics/summary?period=custom" -Method Get -ContentType "application/json"
    Write-Host "✅ Custom Period Missing Dates Response:" -ForegroundColor Green
    $response | ConvertTo-Json -Depth 3
} catch {
    Write-Host "❌ Custom Period Missing Dates Error (Expected): $($_.Exception.Message)" -ForegroundColor Yellow
}

Write-Host "`n`n🎯 TESTING COMPLETED!" -ForegroundColor Green
Write-Host "=" * 60

# Summary
Write-Host "`n📋 SUMMARY OF NEW APIS:" -ForegroundColor Cyan
Write-Host "1. GET /api/admin/books/statistics/summary - Light data for chart overview"
Write-Host "2. GET /api/admin/books/statistics/details - Detailed book data with growth"
Write-Host "`n💡 FRONTEND INTEGRATION:"
Write-Host "- Call Summary API to render chart"
Write-Host "- Call Details API when user clicks on chart point"
Write-Host "`n🔄 PERFORMANCE BENEFITS:"
Write-Host "- Faster chart loading (lightweight summary)"
Write-Host "- On-demand detailed data (only when needed)"
Write-Host "- Better scalability and caching potential"

Write-Host "`nPress any key to continue..."
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
