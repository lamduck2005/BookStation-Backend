# Debug Week 34 issue - check raw response

Write-Host "=== DEBUGGING WEEK 34 ISSUE ===" -ForegroundColor Red

$mondayTimestamp = 1755475200000
$url = "http://localhost:8080/api/books/statistics/details?period=week&date=$mondayTimestamp&limit=10"

Write-Host "Testing URL: $url" -ForegroundColor Yellow

try {
    $response = Invoke-WebRequest -Uri $url -Method GET -ContentType "application/json"
    Write-Host "Status Code: $($response.StatusCode)" -ForegroundColor Green
    Write-Host "Raw Response:" -ForegroundColor Cyan
    Write-Host $response.Content
    
    if ($response.Content) {
        $data = $response.Content | ConvertFrom-Json
        Write-Host "Parsed JSON - Count: $($data.Count)" -ForegroundColor Yellow
        if ($data.Count -gt 0) {
            $data | ForEach-Object { Write-Host "Book: $($_.title)" }
        }
    }
} catch {
    Write-Host "Error occurred: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "Full error: $($_.Exception)" -ForegroundColor DarkRed
}

Write-Host "`n=== ALSO TEST SUMMARY API ===" -ForegroundColor Blue
$summaryUrl = "http://localhost:8080/api/books/statistics/summary?period=week&date=$mondayTimestamp"
try {
    $summaryResponse = Invoke-RestMethod -Uri $summaryUrl -Method GET
    Write-Host "Summary API response:" -ForegroundColor Green
    Write-Host "Total books: $($summaryResponse.totalBooks)" -ForegroundColor White
} catch {
    Write-Host "Summary API error: $($_.Exception.Message)" -ForegroundColor Red
}
