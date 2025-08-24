param(
    [string]$Period = "day",
    [string]$BaseUrl = "http://localhost:8080"
)

$apiUrl = "$BaseUrl/api/books/statistics/summary?period=$Period"
Write-Host "Testing API: $apiUrl" -ForegroundColor Cyan

try {
    $response = Invoke-RestMethod -Uri $apiUrl -Method GET -ErrorAction Stop
    
    if ($response -and $response.data) {
        Write-Host "SUCCESS: API Response received" -ForegroundColor Green
        Write-Host "Total data points: $($response.data.Count)" -ForegroundColor White
        
        # Check if new netRevenue field exists
        $sampleData = $response.data | Select-Object -First 1
        $hasNetRevenue = $sampleData -and $sampleData.PSObject.Properties.Name -contains "netRevenue"
        
        if ($hasNetRevenue) {
            Write-Host "✅ SUCCESS: netRevenue field is present" -ForegroundColor Green
        } else {
            Write-Host "❌ ERROR: netRevenue field is missing" -ForegroundColor Red
        }
        
        # Show sample data structure
        Write-Host "`nSample data structure:" -ForegroundColor Yellow
        $sampleData | ConvertTo-Json | Write-Host -ForegroundColor Gray
        
        # Show recent data
        Write-Host "`nRecent data (last 3):" -ForegroundColor Yellow
        $response.data | Select-Object -Last 3 | ForEach-Object {
            Write-Host "Date: $($_.date), Sold: $($_.totalBooksSold)" -ForegroundColor White
            if ($_.netRevenue) {
                Write-Host "   Revenue: $($_.netRevenue)" -ForegroundColor Cyan
            }
        }
        
    } else {
        Write-Host "❌ ERROR: No data in response" -ForegroundColor Red
    }
    
} catch {
    Write-Host "❌ ERROR: $($_.Exception.Message)" -ForegroundColor Red
}
