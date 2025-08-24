#!/usr/bin/env pwsh

Write-Host "🧪 Testing Refund Logic Fix" -ForegroundColor Yellow
Write-Host "===========================" -ForegroundColor Yellow

# Start the server and test the API
Write-Host "📋 Starting server..." -ForegroundColor Green

# Test the specific API with the refund case
$testDate = "1756038008017"  # The timestamp of your Order ID 3

Write-Host "🎯 Testing Order ID 3 case:" -ForegroundColor Cyan
Write-Host "  - Order has 2 books (quantity=2)" -ForegroundColor White
Write-Host "  - 1 book was refunded (refund_quantity=1)" -ForegroundColor White  
Write-Host "  - Expected result: quantitySold=1, revenue=75,650đ" -ForegroundColor White

Write-Host "`n🔧 Expected SQL Query Changes:" -ForegroundColor Magenta
Write-Host "  OLD: SUM(od.quantity) as quantitySold" -ForegroundColor Red
Write-Host "  NEW: SUM(od.quantity) - COALESCE(SUM(refunds.refund_quantity), 0) as quantitySold" -ForegroundColor Green
Write-Host ""
Write-Host "  OLD: SUM(...) as revenue" -ForegroundColor Red  
Write-Host "  NEW: SUM(...) - COALESCE(SUM(refund_amount), 0) as revenue" -ForegroundColor Green

Write-Host "`n⚡ Run this command to test when server is ready:" -ForegroundColor Yellow
Write-Host "  Invoke-RestMethod -Uri 'http://localhost:8080/api/books/statistics/details?period=day&date=$testDate&limit=10' -Method Get" -ForegroundColor White

Write-Host "`n📊 Expected vs Current Results:" -ForegroundColor Cyan
Write-Host "  Current (WRONG):    quantitySold=2, revenue=151,300" -ForegroundColor Red
Write-Host "  Expected (FIXED):   quantitySold=1, revenue=~75,650" -ForegroundColor Green
