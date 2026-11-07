# Test SePayWebhookServlet using localhost only.
param(
    [ValidateSet("A", "B_PARTIAL", "B_FULL", "B_DUPLICATE", "C_LATE",
        "WRONG_APIKEY", "UNKNOWN_CODE", "NUMERIC_ID", "SECOND_A")]
    [string]$Case = "A",
    [int]$ORDER_ID = 161,
    [string]$CODE = "DH161",
    [double]$AMOUNT = 2000,
    [double]$PARTIAL_AMOUNT = 1000,
    [string]$TX_ID = ""
)

$BASE_URL = "http://localhost:8080"
$WEBHOOK_URL = "$BASE_URL/api/sepay-webhook"
$envPath = Join-Path $PSScriptRoot ".env"
if (-not (Test-Path -LiteralPath $envPath)) {
    throw "Missing .env at repository root: $envPath"
}
$apiKeyLine = Get-Content -LiteralPath $envPath |
    Where-Object { $_ -match '^\s*SEPAY_API_KEY\s*=' } |
    Select-Object -First 1
if (-not $apiKeyLine) {
    throw "SEPAY_API_KEY is missing from .env"
}
$apiKey = ($apiKeyLine -replace '^\s*SEPAY_API_KEY\s*=\s*', '').Trim().Trim('"').Trim("'")
if ([string]::IsNullOrWhiteSpace($apiKey)) {
    throw "SEPAY_API_KEY is empty in .env"
}

$headers = @{
    Authorization = "Apikey $apiKey"
}
$wrongHeaders = @{
    Authorization = "Apikey invalid-test-key"
}

function Send-Webhook($payload, $requestHeaders, $description) {
    $body = $payload | ConvertTo-Json -Compress
    try {
        $response = Invoke-WebRequest -Uri $WEBHOOK_URL -Method Post `
            -Headers $requestHeaders -Body $body `
            -ContentType "application/json; charset=UTF-8" -UseBasicParsing
        Write-Output "$description | HTTP $($response.StatusCode) | $($response.Content)"
    } catch {
        if ($_.Exception.Response) {
            $status = [int]$_.Exception.Response.StatusCode
            $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
            Write-Output "$description | HTTP $status | $($reader.ReadToEnd())"
        } else {
            Write-Output "$description | ERROR $($_.Exception.Message)"
        }
    }
}

if ([string]::IsNullOrWhiteSpace($TX_ID)) {
    $TX_ID = "LOCAL_$Case`_$(Get-Date -Format 'yyyyMMddHHmmss')"
}

switch ($Case) {
    "A" {
        Send-Webhook @{
            id = $TX_ID; transferType = "in"; transferAmount = $AMOUNT
            content = "Chuyen khoan $CODE thanh toan"; code = $CODE; subAccount = "VA_001"
        } $headers "A - full amount"
    }
    "B_PARTIAL" {
        Send-Webhook @{
            id = $TX_ID; transferType = "in"; transferAmount = $PARTIAL_AMOUNT
            content = "Chuyen khoan $CODE"; code = $CODE; subAccount = "VA_001"
        } $headers "B - partial amount"
    }
    "B_FULL" {
        Send-Webhook @{
            id = $TX_ID; transferType = "in"; transferAmount = $AMOUNT
            content = "Chuyen khoan $CODE"; code = $CODE; subAccount = "VA_001"
        } $headers "B - full amount"
    }
    "B_DUPLICATE" {
        Send-Webhook @{
            id = $TX_ID; transferType = "in"; transferAmount = $AMOUNT
            content = "Chuyen khoan $CODE"; code = $CODE; subAccount = "VA_001"
        } $headers "B - duplicate id"
    }
    "C_LATE" {
        Send-Webhook @{
            id = $TX_ID; transferType = "in"; transferAmount = $AMOUNT
            content = "Chuyen khoan $CODE"; code = $CODE; subAccount = "VA_001"
        } $headers "C - late payment"
    }
    "WRONG_APIKEY" {
        Send-Webhook @{
            id = $TX_ID; transferType = "in"; transferAmount = $AMOUNT
            content = "Chuyen khoan $CODE"; code = $CODE; subAccount = "VA_001"
        } $wrongHeaders "Wrong Apikey"
    }
    "UNKNOWN_CODE" {
        Send-Webhook @{
            id = $TX_ID; transferType = "in"; transferAmount = $AMOUNT
            content = "Chuyen khoan DH99999999"; code = "DH99999999"; subAccount = "VA_001"
        } $headers "Unknown order code"
    }
    "NUMERIC_ID" {
        Send-Webhook @{
            id = 900001; transferType = "in"; transferAmount = $AMOUNT
            content = "Chuyen khoan $CODE"; code = $CODE; subAccount = "VA_001"
        } $headers "Numeric id"
    }
    "SECOND_A" {
        Send-Webhook @{
            id = $TX_ID; transferType = "in"; transferAmount = $AMOUNT
            content = "Chuyen khoan $CODE lan hai"; code = $CODE; subAccount = "VA_001"
        } $headers "A - second transfer"
    }
}
