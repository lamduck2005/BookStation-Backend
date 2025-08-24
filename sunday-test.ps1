#!/usr/bin/env pwsh
# Test with Sunday timestamp (day that has data)

Write-Host "=== TESTING WITH SUNDAY TIMESTAMP ===" -ForegroundColor Cyan

$baseUrl = "http://localhost:8080/api/books"

# Sunday 2025-08-24 có dữ liệu - test với timestamp đó
$sundayStart = 1755993600000  # 2025-08-24 00:00:00
$sundayActualOrder = 1756030908539  # Timestamp của order thực tế

Write-Host "Testing week query with Sunday timestamps:" -ForegroundColor Yellow

$testCases = @(
    @{ Name = "Sunday start (00:00)"; Timestamp = $sundayStart },
    @{ Name = "Sunday actual order time"; Timestamp = $sundayActualOrder }
)

foreach ($test in $testCases) {
    Write-Host "Testing $($test.Name):" -ForegroundColor Cyan
    $weekUrl = "$baseUrl/statistics/details?period=week`&date=$($test.Timestamp)`&limit=10"
    
    try {
        $response = Invoke-RestMethod -Uri $weekUrl -Method GET
        $count = if ($response.data) { $response.data.Count } else { 0 }
        $total = if ($response.data) { 
            ($response.data | Measure-Object -Property quantitySold -Sum).Sum 
        } else { 0 }
        
        Write-Host "  Week query result: $count books, $total total quantity" -ForegroundColor Green
        
        if ($count -gt 0) {
            foreach ($book in $response.data) {
                Write-Host "    $($book.name): $($book.quantitySold)" -ForegroundColor Yellow
            }
            
            # Convert timestamp to see what week it thinks this is
            $date = [DateTimeOffset]::FromUnixTimeMilliseconds($test.Timestamp)
            Write-Host "    Input: $($date.ToString('yyyy-MM-dd dddd'))" -ForegroundColor Gray
            
            # Calculate Monday of that week
            $dayOfWeek = [int]$date.DayOfWeek
            $daysToSubtract = if ($dayOfWeek -eq 0) { 6 } else { $dayOfWeek - 1 }  # Sunday = 0, Monday = 1
            $monday = $date.Date.AddDays(-$daysToSubtract)
            $sunday = $monday.AddDays(6)
            Write-Host "    Should be week: $($monday.ToString('yyyy-MM-dd')) to $($sunday.ToString('yyyy-MM-dd'))" -ForegroundColor Gray
        }
    } catch {
        Write-Host "  Error: $($_.Exception.Message)" -ForegroundColor Red
    }
    
    Write-Host ""
}

Write-Host "If these work, then the issue is specifically with Monday timestamps" -ForegroundColor Yellow
Write-Host "=== END TEST ===" -ForegroundColor Cyan
