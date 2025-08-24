# Direct database check for Week 34

Write-Host "=== DIRECT DATABASE CHECK FOR WEEK 34 ===" -ForegroundColor Yellow

# Calculate exact timestamp range for Week 34
$mondayTs = 1755475200000
$sundayTs = 1756080000000 - 1  # End of Sunday

# First check - are there any orders in this exact range?
$query1 = @"
SELECT 
    COUNT(*) as total_orders,
    MIN(created_at) as first_order,
    MAX(created_at) as last_order
FROM [order] 
WHERE created_at >= $mondayTs AND created_at <= $sundayTs
"@

Write-Host "Query 1: Basic order count in Week 34" -ForegroundColor Cyan
Write-Host "Timestamp range: $mondayTs to $sundayTs" -ForegroundColor White

# Second check - detailed order and book info
$query2 = @"
SELECT TOP 5
    o.id,
    o.code, 
    o.created_at,
    o.order_status,
    od.book_id,
    b.book_name,
    od.quantity
FROM [order] o
JOIN order_detail od ON o.id = od.order_id  
JOIN book b ON od.book_id = b.id
WHERE o.created_at >= $mondayTs AND o.created_at <= $sundayTs
    AND o.order_status IN ('DELIVERED', 'PARTIALLY_REFUNDED')
ORDER BY o.created_at DESC
"@

Write-Host "`nQuery 2: Sample orders with book details" -ForegroundColor Cyan

# Create a test API call to run these queries
$testUrl = "http://localhost:8080/api/debug/query"
$postData = @{
    query1 = $query1
    query2 = $query2  
} | ConvertTo-Json

Write-Host "`nWe need to run these SQL queries to diagnose:" -ForegroundColor Red
Write-Host $query1 -ForegroundColor Gray
Write-Host "`n" 
Write-Host $query2 -ForegroundColor Gray

Write-Host "`nTimestamp conversions:" -ForegroundColor Yellow
Write-Host "Monday 1755475200000 = $(Get-Date '1970-01-01').AddMilliseconds(1755475200000)" -ForegroundColor White
Write-Host "Sunday 1756079999999 = $(Get-Date '1970-01-01').AddMilliseconds(1756079999999)" -ForegroundColor White
