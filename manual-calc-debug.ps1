#!/usr/bin/env pwsh
# Let's verify what Java is actually calculating

Write-Host "=== MANUAL CALCULATION VERIFICATION ===" -ForegroundColor Cyan

# Manual calculation for the problematic timestamps
$timestamps = @(
    1755475200000,  # 2025-08-18 Monday (Week 34 start)
    1756080000000,  # 2025-08-25 Monday (Week 35 start)
    1756030908539   # 2025-08-24 Sunday (actual order date)
)

foreach ($ts in $timestamps) {
    $date = [DateTimeOffset]::FromUnixTimeMilliseconds($ts)
    Write-Host "Timestamp: $ts = $($date.ToString('yyyy-MM-dd HH:mm:ss dddd'))" -ForegroundColor Yellow
    
    # Calculate Monday of that week (same as Java logic should be)
    $dayOfWeek = [int]$date.DayOfWeek
    $daysToSubtract = ($dayOfWeek + 6) % 7  # Convert Sunday=0 to Monday=0 system
    $monday = $date.Date.AddDays(-$daysToSubtract)
    $sunday = $monday.AddDays(6)
    
    Write-Host "  Week should be: $($monday.ToString('yyyy-MM-dd')) to $($sunday.ToString('yyyy-MM-dd'))" -ForegroundColor Cyan
    
    # Convert back to timestamp range
    $weekStartTs = $monday.ToUniversalTime().Subtract([DateTimeOffset]::Parse("1970-01-01")).TotalMilliseconds
    $weekEndTs = $sunday.Date.AddDays(1).AddTicks(-1).ToUniversalTime().Subtract([DateTimeOffset]::Parse("1970-01-01")).TotalMilliseconds
    
    Write-Host "  Week timestamp range: $([long]$weekStartTs) to $([long]$weekEndTs)" -ForegroundColor Gray
    Write-Host ""
}

# Check where the actual orders fall
Write-Host "Actual orders are on 2025-08-24 Sunday:" -ForegroundColor Yellow
Write-Host "  Should be in Week 34: 2025-08-18 to 2025-08-24" -ForegroundColor Green
Write-Host "  Should NOT be in Week 35: 2025-08-25 to 2025-08-31" -ForegroundColor Red
Write-Host "  But API returns opposite!" -ForegroundColor Red

Write-Host "=== END VERIFICATION ===" -ForegroundColor Cyan
