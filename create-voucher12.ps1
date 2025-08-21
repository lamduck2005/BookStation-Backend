# Script tạo voucher mới với code "voucher12"
Write-Host "=== TẠO VOUCHER MỚI VỚI CODE 'voucher12' ===" -ForegroundColor Green

# Tạo voucher request body
$voucherData = @{
    code = "voucher12"
    name = "Voucher 12 Test"
    description = "Voucher test có mã voucher12"
    voucherCategory = "NORMAL"
    discountType = "FIXED_AMOUNT"
    discountAmount = 50000
    startTime = [long]([DateTimeOffset]::Now.AddDays(-1).ToUnixTimeMilliseconds())
    endTime = [long]([DateTimeOffset]::Now.AddDays(30).ToUnixTimeMilliseconds())
    minOrderValue = 100000
    maxDiscountValue = 50000
    usageLimit = 100
    usedCount = 0
    usageLimitPerUser = 1
    status = 1
    createdBy = "admin"
    updatedBy = "admin"
}

$jsonBody = $voucherData | ConvertTo-Json

Write-Host "Voucher data to create:" -ForegroundColor Yellow
Write-Host $jsonBody -ForegroundColor White

try {
    # Tạo voucher mới
    Write-Host "`nCreating voucher..." -ForegroundColor Yellow
    $response = Invoke-WebRequest -Uri "http://localhost:8080/api/vouchers" -Method POST -ContentType "application/json" -Body $jsonBody -UseBasicParsing
    
    Write-Host "Voucher created successfully!" -ForegroundColor Green
    Write-Host "Response: $($response.StatusCode) - $($response.StatusDescription)" -ForegroundColor Cyan
    
    # Đợi một chút để database update
    Start-Sleep -Seconds 2
    
    # Test tìm kiếm voucher vừa tạo
    Write-Host "`n=== TESTING SEARCH FOR 'voucher12' ===" -ForegroundColor Yellow
    $searchResponse = Invoke-WebRequest -Uri "http://localhost:8080/api/vouchers/dropdown?search=voucher12" -Method GET -UseBasicParsing
    $searchData = $searchResponse.Content | ConvertFrom-Json
    
    Write-Host "Search results:" -ForegroundColor Cyan
    Write-Host "Status: $($searchData.status)" -ForegroundColor Cyan
    Write-Host "Message: $($searchData.message)" -ForegroundColor Cyan
    Write-Host "Found vouchers: $($searchData.data.Count)" -ForegroundColor Cyan
    
    if ($searchData.data.Count -gt 0) {
        $searchData.data | ForEach-Object {
            Write-Host "✅ FOUND VOUCHER:" -ForegroundColor Green
            Write-Host "  ID: $($_.id)" -ForegroundColor White
            Write-Host "  Code: $($_.code)" -ForegroundColor White
            Write-Host "  Name: $($_.name)" -ForegroundColor White
            Write-Host "  Description: $($_.description)" -ForegroundColor White
            Write-Host "  Status: $($_.status)" -ForegroundColor White
        }
    } else {
        Write-Host "❌ Không tìm thấy voucher sau khi tạo!" -ForegroundColor Red
    }
    
} catch {
    $errorDetails = $_.Exception.Message
    if ($_.Exception.Response) {
        $reader = [System.IO.StreamReader]::new($_.Exception.Response.GetResponseStream())
        $responseText = $reader.ReadToEnd()
        $reader.Close()
        Write-Host "Error response: $responseText" -ForegroundColor Red
    }
    Write-Host "Error creating voucher: $errorDetails" -ForegroundColor Red
}

Write-Host "`n=== COMPLETED ===" -ForegroundColor Green
