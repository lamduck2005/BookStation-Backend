#!/usr/bin/env pwsh
# Test with actual week start timestamps

Write-Host "=== WEEK START TIMESTAMP DEBUG ===" -ForegroundColor Cyan

$baseUrl = "http://localhost:8080/api/books"

# Exact timestamps for Monday week starts
$mondayTimestamps = @(
    @{ Name = "Monday 2025-08-18 00:00 (Week 34)"; Timestamp = 1755475200000 },  # This should work but doesn't
    @{ Name = "Monday 2025-08-25 00:00 (Week 35)"; Timestamp = 1756080000000 },  # This returns wrong data
    @{ Name = "Sunday 2025-08-24 (actual order day)"; Timestamp = 1756030908539 } # This works
)

foreach ($test in $mondayTimestamps) {
    Write-Host "Testing $($test.Name)" -ForegroundColor Yellow
    
    # Convert timestamp to readable date
    $date = [DateTimeOffset]::FromUnixTimeMilliseconds($test.Timestamp)
    Write-Host "  Timestamp $($test.Timestamp) = $($date.ToString('yyyy-MM-dd HH:mm:ss dddd'))" -ForegroundColor Gray
    
    $testUrl = "$baseUrl/statistics/details?period=week`&date=$($test.Timestamp)`&limit=10"
    
    try {
        $response = Invoke-RestMethod -Uri $testUrl -Method GET
        Write-Host "  Week query result: $($response.data.Count) books" -ForegroundColor Green
        
        if ($response.data.Count -gt 0) {
            $totalQuantity = 0
            foreach ($book in $response.data) {
                Write-Host "    $($book.name): $($book.quantitySold) sold" -ForegroundColor Cyan
                $totalQuantity += $book.quantitySold
            }
            Write-Host "    TOTAL: $totalQuantity books" -ForegroundColor Magenta
        } else {
            Write-Host "    NO DATA - this is the bug!" -ForegroundColor Red
        }
    } catch {
        Write-Host "  Error: $($_.Exception.Message)" -ForegroundColor Red
    }
    
    Write-Host ""
}

# Test what the Java backend is calculating for these timestamps
Write-Host "Expected behavior:" -ForegroundColor Yellow
Write-Host "  - All orders are on 2025-08-24 Sunday" -ForegroundColor Cyan
Write-Host "  - Week 34: Monday 2025-08-18 to Sunday 2025-08-24 (should have ALL orders)" -ForegroundColor Cyan
Write-Host "  - Week 35: Monday 2025-08-25 to Sunday 2025-08-31 (should have NO orders)" -ForegroundColor Cyan
Write-Host "  - But we get opposite result!" -ForegroundColor Red

Write-Host "=== END DEBUG ===" -ForegroundColor Cyan
