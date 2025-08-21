# Kiểm tra tất cả vouchers kể cả inactive
Write-Host "=== KIỂM TRA TẤT CẢ VOUCHERS (Kể cả inactive) ===" -ForegroundColor Green

# Gọi API lấy tất cả vouchers với pagination
try {
    $url = "http://localhost:8080/api/vouchers?page=0&size=100"
    $response = Invoke-WebRequest -Uri $url -Method GET -UseBasicParsing
    $data = $response.Content | ConvertFrom-Json
    
    Write-Host "Total vouchers (all status): $($data.data.content.Count)" -ForegroundColor Cyan
    Write-Host "Total elements: $($data.data.totalElements)" -ForegroundColor Cyan
    
    Write-Host "`n=== DANH SÁCH TẤT CẢ VOUCHERS ===" -ForegroundColor Yellow
    $data.data.content | ForEach-Object {
        $statusText = if ($_.status -eq 1) { "ACTIVE" } else { "INACTIVE" }
        $color = if ($_.status -eq 1) { "Green" } else { "Red" }
        Write-Host "ID: $($_.id) | Code: $($_.code) | Status: $statusText | Name: $($_.name)" -ForegroundColor $color
    }
    
    # Tìm voucher chứa "12"
    Write-Host "`n=== VOUCHERS CHỨA '12' (All status) ===" -ForegroundColor Yellow
    $vouchers12 = $data.data.content | Where-Object { $_.code -like "*12*" -or $_.name -like "*12*" }
    if ($vouchers12.Count -gt 0) {
        $vouchers12 | ForEach-Object {
            $statusText = if ($_.status -eq 1) { "ACTIVE" } else { "INACTIVE" }
            Write-Host "FOUND: ID: $($_.id) | Code: $($_.code) | Status: $statusText | Name: $($_.name)" -ForegroundColor Green
        }
    } else {
        Write-Host "Vẫn không có voucher nào chứa '12'" -ForegroundColor Red
    }
    
    # Tìm voucher chứa "voucher"
    Write-Host "`n=== VOUCHERS CHỨA 'voucher' (All status) ===" -ForegroundColor Yellow
    $vouchersVoucher = $data.data.content | Where-Object { $_.code -like "*voucher*" -or $_.name -like "*voucher*" }
    if ($vouchersVoucher.Count -gt 0) {
        $vouchersVoucher | ForEach-Object {
            $statusText = if ($_.status -eq 1) { "ACTIVE" } else { "INACTIVE" }
            Write-Host "FOUND: ID: $($_.id) | Code: $($_.code) | Status: $statusText | Name: $($_.name)" -ForegroundColor Green
        }
    } else {
        Write-Host "Không có voucher nào chứa 'voucher'" -ForegroundColor Red
    }
    
} catch {
    Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n=== SUGGESTION ===" -ForegroundColor Magenta
Write-Host "Nếu bạn cần voucher 'voucher12', có thể:" -ForegroundColor White
Write-Host "1. Voucher chưa được tạo trong database" -ForegroundColor White
Write-Host "2. Voucher có tên code khác (ví dụ: VOUCHER12, V12, etc.)" -ForegroundColor White
Write-Host "3. Cần tạo voucher mới với code 'voucher12'" -ForegroundColor White
