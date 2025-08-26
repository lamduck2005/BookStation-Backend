#!/usr/bin/env pwsh

# Quick test script to verify the book image fix
Write-Host "🔧 Quick Book Image Test" -ForegroundColor Cyan

$baseUrl = "http://localhost:8080"
$userId = 4

# Wait for server to be ready
Write-Host "⏳ Waiting for server to be ready..." -ForegroundColor Yellow

$maxAttempts = 30
$attempt = 0

while ($attempt -lt $maxAttempts) {
    try {
        $health = Invoke-RestMethod -Uri "$baseUrl/actuator/health" -Method GET -TimeoutSec 5
        if ($health.status -eq "UP") {
            Write-Host "✅ Server is ready!" -ForegroundColor Green
            break
        }
    }
    catch {
        # Server not ready yet
    }
    
    $attempt++
    Write-Host "." -NoNewline -ForegroundColor Gray
    Start-Sleep -Seconds 2
}

if ($attempt -eq $maxAttempts) {
    Write-Host "`n❌ Server failed to start within timeout" -ForegroundColor Red
    exit 1
}

Write-Host "`n🧪 Testing order API for book images..." -ForegroundColor Blue

try {
    $response = Invoke-RestMethod -Uri "$baseUrl/api/orders/user/$userId" -Method GET
    
    if ($response.status -eq 200 -and $response.data) {
        $order = $response.data[0]
        $detail = $order.orderDetails[0]
        
        Write-Host "📋 Order: $($order.code)" -ForegroundColor White
        Write-Host "📚 Book: $($detail.bookName)" -ForegroundColor White
        
        if ($detail.bookImageUrl) {
            Write-Host "✅ Book Image URL: $($detail.bookImageUrl)" -ForegroundColor Green
            Write-Host "🎉 SUCCESS: Book image is now included in the response!" -ForegroundColor Green
        } else {
            Write-Host "❌ Book Image URL: null" -ForegroundColor Red
            Write-Host "💔 FAILED: Book image is still missing!" -ForegroundColor Red
        }
        
        # Show full order detail structure for debugging
        Write-Host "`n📝 Full order detail response:" -ForegroundColor Cyan
        $detail | ConvertTo-Json -Depth 2 | Write-Host
        
    } else {
        Write-Host "❌ No orders found or API error" -ForegroundColor Red
    }
}
catch {
    Write-Host "❌ API Test Failed: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n🏁 Test completed" -ForegroundColor Magenta
