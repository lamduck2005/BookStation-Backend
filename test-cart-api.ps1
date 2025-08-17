# Test Cart API
$headers = @{'Content-Type' = 'application/json'}

# Test adding flash sale item with quantity exceeding stock (15 > 13)
$body = @{
    bookId = 3
    quantity = 15
    flashSaleItemId = 3
    userId = 4
} | ConvertTo-Json

Write-Host "Testing POST /api/carts/items with quantity 15 (should fail because stock is only 13):"
try {
    $response = Invoke-RestMethod -Uri 'http://localhost:8080/api/carts/items' -Method POST -Headers $headers -Body $body
    $response | ConvertTo-Json -Depth 10
} catch {
    Write-Host "Error occurred:"
    Write-Host $_.Exception.Message
    if ($_.Exception.Response) {
        $responseBody = $_.Exception.Response.GetResponseStream()
        $reader = New-Object System.IO.StreamReader($responseBody)
        $responseText = $reader.ReadToEnd()
        Write-Host "Response body: $responseText"
    }
}
