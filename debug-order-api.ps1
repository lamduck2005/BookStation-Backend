#!/usr/bin/env pwsh
# Simple error debugging script

Write-Host "Testing Order Statistics API with detailed error info" -ForegroundColor Green

try {
    $response = Invoke-WebRequest -Uri "http://localhost:8080/api/orders/statistics/summary?period=day" -Method Get
    Write-Host "Success: $($response.StatusCode)" -ForegroundColor Green
    Write-Host "Content: $($response.Content)" -ForegroundColor Gray
} catch {
    Write-Host "Error Details:" -ForegroundColor Red
    Write-Host "Status Code: $($_.Exception.Response.StatusCode)" -ForegroundColor Red
    Write-Host "Status Description: $($_.Exception.Response.StatusDescription)" -ForegroundColor Red
    
    if ($_.Exception.Response.GetResponseStream) {
        $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
        $responseBody = $reader.ReadToEnd()
        Write-Host "Response Body: $responseBody" -ForegroundColor Red
    }
}
