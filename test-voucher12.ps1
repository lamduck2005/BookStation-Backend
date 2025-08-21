# Test tìm voucher có mã "voucher12"
Write-Host "=== TESTING VOUCHER DROPDOWN API - Tìm mã voucher12 ===" -ForegroundColor Green

# Test các pattern có thể có cho voucher12
$searchTerms = @(
    "voucher12",
    "VOUCHER12", 
    "Voucher12",
    "voucher 12",
    "VOUCHER 12",
    "Voucher 12"
)

foreach ($term in $searchTerms) {
    Write-Host "`nTesting search='$term':" -ForegroundColor Yellow
    try {
        $url = "http://localhost:8080/api/vouchers/dropdown?search=" + [System.Web.HttpUtility]::UrlEncode($term)
        $response = Invoke-WebRequest -Uri $url -Method GET -UseBasicParsing
        $data = $response.Content | ConvertFrom-Json
        
        Write-Host "Status: $($data.status)" -ForegroundColor Cyan
        if ($data.data.Count -gt 0) {
            Write-Host "Found $($data.data.Count) voucher(s):" -ForegroundColor Green
            $data.data | ForEach-Object {
                Write-Host "  ID: $($_.id)" -ForegroundColor White
                Write-Host "  Code: $($_.code)" -ForegroundColor White  
                Write-Host "  Name: $($_.name)" -ForegroundColor White
                Write-Host "  Description: $($_.description)" -ForegroundColor Gray
                Write-Host "  Status: $($_.status)" -ForegroundColor Gray
                Write-Host "  ---" -ForegroundColor Gray
            }
        } else {
            Write-Host "No vouchers found for '$term'" -ForegroundColor Red
        }
    } catch {
        Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
    }
}

# Hiển thị tất cả voucher để xem có voucher12 không
Write-Host "`n=== TẤT CẢ VOUCHERS HIỆN TẠI ===" -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "http://localhost:8080/api/vouchers/dropdown" -Method GET -UseBasicParsing
    $data = $response.Content | ConvertFrom-Json
    
    Write-Host "Total vouchers: $($data.data.Count)" -ForegroundColor Cyan
    $data.data | ForEach-Object {
        Write-Host "ID: $($_.id) | Code: $($_.code) | Name: $($_.name)" -ForegroundColor White
    }
    
    # Tìm voucher có chứa "12" trong code hoặc name
    Write-Host "`n=== VOUCHERS CHỨA '12' ===" -ForegroundColor Yellow
    $vouchers12 = $data.data | Where-Object { $_.code -like "*12*" -or $_.name -like "*12*" }
    if ($vouchers12.Count -gt 0) {
        $vouchers12 | ForEach-Object {
            Write-Host "FOUND: ID: $($_.id) | Code: $($_.code) | Name: $($_.name)" -ForegroundColor Green
        }
    } else {
        Write-Host "Không có voucher nào chứa '12' trong code hoặc name" -ForegroundColor Red
    }
    
} catch {
    Write-Host "Error getting all vouchers: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n=== TEST COMPLETED ===" -ForegroundColor Green
