# Test voucher dropdown API
Write-Host "=== TESTING VOUCHER DROPDOWN API ===" -ForegroundColor Green

# Test 1: Get all vouchers (no search param)
Write-Host "`n1. Testing without search parameter:" -ForegroundColor Yellow
try {
    $response1 = Invoke-WebRequest -Uri "http://localhost:8080/api/vouchers/dropdown" -Method GET -UseBasicParsing
    $data1 = $response1.Content | ConvertFrom-Json
    Write-Host "Status: $($data1.status)" -ForegroundColor Cyan
    Write-Host "Message: $($data1.message)" -ForegroundColor Cyan
    Write-Host "Total vouchers found: $($data1.data.Count)" -ForegroundColor Cyan
    
    if ($data1.data.Count -gt 0) {
        Write-Host "First few vouchers:" -ForegroundColor Cyan
        $data1.data | Select-Object -First 5 | ForEach-Object {
            Write-Host "  - ID: $($_.id), Code: $($_.code), Name: $($_.name)" -ForegroundColor White
        }
    }
} catch {
    Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
}

# Test 2: Search with "voucher"
Write-Host "`n2. Testing with search='voucher':" -ForegroundColor Yellow
try {
    $response2 = Invoke-WebRequest -Uri "http://localhost:8080/api/vouchers/dropdown?search=voucher" -Method GET -UseBasicParsing
    $data2 = $response2.Content | ConvertFrom-Json
    Write-Host "Status: $($data2.status)" -ForegroundColor Cyan
    Write-Host "Message: $($data2.message)" -ForegroundColor Cyan
    Write-Host "Vouchers found with 'voucher': $($data2.data.Count)" -ForegroundColor Cyan
    
    if ($data2.data.Count -gt 0) {
        $data2.data | ForEach-Object {
            Write-Host "  - ID: $($_.id), Code: $($_.code), Name: $($_.name)" -ForegroundColor White
        }
    }
} catch {
    Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
}

# Test 3: Search with "12"
Write-Host "`n3. Testing with search='12':" -ForegroundColor Yellow
try {
    $response3 = Invoke-WebRequest -Uri "http://localhost:8080/api/vouchers/dropdown?search=12" -Method GET -UseBasicParsing
    $data3 = $response3.Content | ConvertFrom-Json
    Write-Host "Status: $($data3.status)" -ForegroundColor Cyan
    Write-Host "Message: $($data3.message)" -ForegroundColor Cyan
    Write-Host "Vouchers found with '12': $($data3.data.Count)" -ForegroundColor Cyan
    
    if ($data3.data.Count -gt 0) {
        $data3.data | ForEach-Object {
            Write-Host "  - ID: $($_.id), Code: $($_.code), Name: $($_.name)" -ForegroundColor White
        }
    }
} catch {
    Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
}

# Test 4: Search with ID "12" specifically
Write-Host "`n4. Testing search for specific patterns that might match voucher ID 12:" -ForegroundColor Yellow

$searchTerms = @("12", "voucher12", "VOUCHER12", "sale12", "discount12", "promo12")

foreach ($term in $searchTerms) {
    Write-Host "`n   Testing search='$term':" -ForegroundColor Cyan
    try {
        $url = "http://localhost:8080/api/vouchers/dropdown?search=$term"
        $response = Invoke-WebRequest -Uri $url -Method GET -UseBasicParsing
        $data = $response.Content | ConvertFrom-Json
        
        if ($data.data.Count -gt 0) {
            Write-Host "     Found $($data.data.Count) voucher(s):" -ForegroundColor Green
            $data.data | ForEach-Object {
                Write-Host "       ID: $($_.id), Code: $($_.code), Name: $($_.name)" -ForegroundColor White
            }
        } else {
            Write-Host "     No vouchers found" -ForegroundColor Gray
        }
    } catch {
        Write-Host "     Error: $($_.Exception.Message)" -ForegroundColor Red
    }
}

Write-Host "`n=== TEST COMPLETED ===" -ForegroundColor Green
