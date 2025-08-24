#!/usr/bin/env pwsh
# Test debug endpoint to understand the issue

Write-Host "=== DEBUG ENDPOINT TEST ===" -ForegroundColor Cyan

$baseUrl = "http://localhost:8080/api/books"

# Test problematic timestamps
$timestamps = @(
    @{ Name = "Monday Week 34 (problem)"; Timestamp = 1755475200000 },
    @{ Name = "Sunday Week 34 (works)"; Timestamp = 1755993600000 },
    @{ Name = "Monday Week 35 (wrong data)"; Timestamp = 1756080000000 }
)

foreach ($test in $timestamps) {
    Write-Host "Testing $($test.Name):" -ForegroundColor Yellow
    $debugUrl = "$baseUrl/debug-week-calculation?timestamp=$($test.Timestamp)"
    
    try {
        $response = Invoke-RestMethod -Uri $debugUrl -Method GET
        
        if ($response.status -eq 200) {
            $data = $response.data
            Write-Host "  Input: $($data.inputDate) ($($data.inputDayOfWeek))" -ForegroundColor Cyan
            Write-Host "  Week: $($data.weekStart) to $($data.weekEnd)" -ForegroundColor Cyan
            Write-Host "  Timestamps: $($data.startTimestamp) to $($data.endTimestamp)" -ForegroundColor Gray
            Write-Host "  Books found: $($data.booksFoundCount)" -ForegroundColor $(if ($data.booksFoundCount -gt 0) { "Green" } else { "Red" })
            
            if ($data.booksFound -and $data.booksFound.Count -gt 0) {
                foreach ($book in $data.booksFound) {
                    Write-Host "    $($book.bookName): $($book.quantitySold) sold" -ForegroundColor Yellow
                }
            }
        } else {
            Write-Host "  Error: $($response.message)" -ForegroundColor Red
        }
    } catch {
        Write-Host "  API Error: $($_.Exception.Message)" -ForegroundColor Red
    }
    
    Write-Host ""
}

Write-Host "=== END DEBUG ===" -ForegroundColor Cyan
