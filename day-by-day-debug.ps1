#!/usr/bin/env pwsh
# Test với different date ranges để hiểu query issue

Write-Host "=== QUERY RANGE DEBUGGING ===" -ForegroundColor Cyan

$baseUrl = "http://localhost:8080/api/books"

# Test với range bao gồm toàn bộ khoảng thời gian có order
Write-Host "Testing with full date range including all orders:" -ForegroundColor Yellow

# Orders are from 1756030908539 to 1756064534247 (all on 2025-08-24)
# Test với range rộng để chắc chắn bao gồm tất cả
$wideRangeStart = 1755475200000  # 2025-08-18 00:00:00 (Monday)
$wideRangeEnd = 1756108799999    # 2025-08-24 23:59:59 (Sunday)

Write-Host "Wide range: $wideRangeStart to $wideRangeEnd" -ForegroundColor Gray
Write-Host "Covers: 2025-08-18 00:00:00 to 2025-08-24 23:59:59" -ForegroundColor Gray

# Test day by day trong tuần này
$testDays = @(
    @{ Name = "Monday 2025-08-18"; Date = "2025-08-18"; Timestamp = 1755475200000 },
    @{ Name = "Tuesday 2025-08-19"; Date = "2025-08-19"; Timestamp = 1755561600000 },
    @{ Name = "Wednesday 2025-08-20"; Date = "2025-08-20"; Timestamp = 1755648000000 },
    @{ Name = "Thursday 2025-08-21"; Date = "2025-08-21"; Timestamp = 1755734400000 },
    @{ Name = "Friday 2025-08-22"; Date = "2025-08-22"; Timestamp = 1755820800000 },
    @{ Name = "Saturday 2025-08-23"; Date = "2025-08-23"; Timestamp = 1755907200000 },
    @{ Name = "Sunday 2025-08-24"; Date = "2025-08-24"; Timestamp = 1755993600000 }
)

foreach ($day in $testDays) {
    Write-Host "Testing $($day.Name):" -ForegroundColor Cyan
    $dayUrl = "$baseUrl/statistics/details?period=day`&date=$($day.Timestamp)`&limit=10"
    
    try {
        $response = Invoke-RestMethod -Uri $dayUrl -Method GET
        $count = if ($response.data) { $response.data.Count } else { 0 }
        $total = if ($response.data) { 
            ($response.data | Measure-Object -Property quantitySold -Sum).Sum 
        } else { 0 }
        
        Write-Host "  Day result: $count books, $total total quantity" -ForegroundColor $(if ($count -gt 0) { "Green" } else { "Gray" })
        
        if ($count -gt 0) {
            foreach ($book in $response.data) {
                Write-Host "    $($book.name): $($book.quantitySold)" -ForegroundColor Yellow
            }
        }
    } catch {
        Write-Host "  Error: $($_.Exception.Message)" -ForegroundColor Red
    }
}

Write-Host ""
Write-Host "From the results above, we can see which day(s) have the actual orders" -ForegroundColor Yellow
Write-Host "=== END DEBUG ===" -ForegroundColor Cyan
