Write-Host "Testing minigame APIs..." -ForegroundColor Green

# Test 1: Voucher dropdown
Write-Host "1. Testing voucher dropdown API..." -ForegroundColor Cyan
try {
    $response = Invoke-WebRequest -Uri "http://localhost:8080/api/vouchers/dropdown" -Method GET
    Write-Host "Status: $($response.StatusCode)" -ForegroundColor Green
    $data = $response.Content | ConvertFrom-Json
    Write-Host "Vouchers found: $($data.length)" -ForegroundColor White
} catch {
    Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
}

# Test 2: Create campaign
Write-Host "`n2. Testing create campaign..." -ForegroundColor Cyan
$campaignData = @{
    name = "Test Campaign PS1"
    description = "Test created by PowerShell"
    startDate = [DateTimeOffset]::Now.ToUnixTimeMilliseconds()
    endDate = [DateTimeOffset]::Now.AddDays(30).ToUnixTimeMilliseconds()
    status = 1
    configFreeLimit = 3
    configPointCost = 100
    createdBy = 1
} | ConvertTo-Json

try {
    $response = Invoke-WebRequest -Uri "http://localhost:8080/api/campaigns" -Method POST -Body $campaignData -ContentType "application/json"
    Write-Host "Status: $($response.StatusCode)" -ForegroundColor Green
    $campaignResult = $response.Content | ConvertFrom-Json
    Write-Host "Created campaign ID: $($campaignResult.id)" -ForegroundColor White
    $campaignId = $campaignResult.id
} catch {
    Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
    $campaignId = $null
}

# Test 3: Create reward if campaign exists
if ($campaignId) {
    Write-Host "`n3. Testing create reward..." -ForegroundColor Cyan
    $rewardData = @{
        campaignId = $campaignId
        name = "Test Voucher Reward"
        description = "Test reward from PS1"
        rewardType = "VOUCHER"
        voucherId = 1
        probability = 30.0
        status = 1
        createdBy = 1
    } | ConvertTo-Json

    try {
        $response = Invoke-WebRequest -Uri "http://localhost:8080/api/rewards" -Method POST -Body $rewardData -ContentType "application/json"
        Write-Host "Status: $($response.StatusCode)" -ForegroundColor Green
        $rewardResult = $response.Content | ConvertFrom-Json
        Write-Host "Created reward ID: $($rewardResult.id)" -ForegroundColor White
    } catch {
        Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
    }

    # Test 4: Open box
    Write-Host "`n4. Testing box opening..." -ForegroundColor Cyan
    $openBoxData = @{
        campaignId = $campaignId
        userId = 1
        openType = "FREE"
    } | ConvertTo-Json

    try {
        $response = Invoke-WebRequest -Uri "http://localhost:8080/api/minigame/open-box" -Method POST -Body $openBoxData -ContentType "application/json"
        Write-Host "Status: $($response.StatusCode)" -ForegroundColor Green
        $openResult = $response.Content | ConvertFrom-Json
        Write-Host "Box opened! Win: $($openResult.win)" -ForegroundColor White
        if ($openResult.win) {
            Write-Host "Reward: $($openResult.reward.name)" -ForegroundColor Yellow
        }
    } catch {
        Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
    }
}

Write-Host "`nTest completed!" -ForegroundColor Green
