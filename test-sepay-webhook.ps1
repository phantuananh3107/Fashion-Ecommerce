# =============================================================
# test-sepay-webhook.ps1
# Kiểm thử SePayWebhookServlet — POST /api/sepay-webhook
# =============================================================
# CÁCH DÙNG:
#   .\test-sepay-webhook.ps1
#
# Trước khi chạy:
#   1. Khởi động Tomcat (ứng dụng đang chạy)
#   2. Đảm bảo file .env có SEPAY_API_KEY đúng
#   3. Đổi $APIKEY bên dưới nếu cần
#   4. Tạo thủ công 1 đơn SEPAY trong DB để lấy orderId và paymentCode,
#      sau đó điền vào $VALID_ORDER_ID và $VALID_CODE bên dưới
# =============================================================

# ── Cấu hình ──────────────────────────────────────────────────
$BASE_URL    = "http://localhost:8080/jsp-servlet"    # Đổi nếu context path khác
$WEBHOOK_URL = "$BASE_URL/api/sepay-webhook"
$APIKEY      = "YOUR_SEPAY_API_KEY_HERE"              # Lấy từ .env (SEPAY_API_KEY)

# Đơn SEPAY đang "Chờ thanh toán online" — cần tạo thủ công trong DB
$VALID_ORDER_ID  = 123                        # ← Sửa thành orderId thực
$VALID_CODE      = "DH123"                    # ← Tương ứng payment_code trong DB
$VALID_AMOUNT    = 500000                     # ← Tổng tiền đơn (total_amount)
$PARTIAL_AMOUNT  = 1000                       # ← Số tiền thiếu (< total_amount)
$WRONG_APIKEY    = "wrongkey123"
$EXPIRED_CODE    = "DH999"                    # ← Đơn đã ở trạng thái "Hết hạn (SePay)"

$headers = @{
    "Authorization" = "Apikey $APIKEY"
    "Content-Type"  = "application/json; charset=UTF-8"
}

$wrong_headers = @{
    "Authorization" = "Apikey $WRONG_APIKEY"
    "Content-Type"  = "application/json; charset=UTF-8"
}

# ── Hàm tiện ích ───────────────────────────────────────────────
function Send-Webhook($payload, $hdrs = $headers, $desc = "") {
    Write-Host ""
    Write-Host ("=" * 60)
    Write-Host ">>> CA: $desc"
    Write-Host ("=" * 60)
    $body = $payload | ConvertTo-Json -Compress
    Write-Host "BODY: $body"
    try {
        $resp = Invoke-RestMethod -Uri $WEBHOOK_URL -Method POST `
                    -Headers $hdrs -Body $body -ContentType "application/json; charset=UTF-8"
        Write-Host "RESPONSE: $($resp | ConvertTo-Json -Compress)"
    } catch {
        $statusCode = $_.Exception.Response.StatusCode.value__
        Write-Host "HTTP STATUS: $statusCode"
        try {
            $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
            $errBody = $reader.ReadToEnd()
            Write-Host "ERROR BODY: $errBody"
        } catch {}
    }
}

# =============================================================
# CA 1 — ĐÚNG: thanh toán đủ tiền, mã đúng
# Kỳ vọng: HTTP 200 {"success":true}, đơn → "Đã thanh toán (SePay)"
# =============================================================
Send-Webhook @{
    id             = "TXID_CA1_$(Get-Date -f 'yyyyMMddHHmmss')"
    transferType   = "in"
    transferAmount = $VALID_AMOUNT
    content        = "Chuyen khoan $VALID_CODE thanh toan"
    code           = $VALID_CODE
    subAccount     = "VA_001"
} -desc "CA1 — Dung: du tien, ma dung"

# =============================================================
# CA 2 — SAI APIKEY
# Kỳ vọng: HTTP 401 {"success":false,"error":"unauthorized"}
# =============================================================
Send-Webhook @{
    id             = "TXID_CA2_$(Get-Date -f 'yyyyMMddHHmmss')"
    transferType   = "in"
    transferAmount = $VALID_AMOUNT
    content        = "Chuyen khoan $VALID_CODE"
    code           = $VALID_CODE
    subAccount     = "VA_001"
} -hdrs $wrong_headers -desc "CA2 — Sai Apikey"

# =============================================================
# CA 3 — THIẾU TIỀN
# Kỳ vọng: HTTP 200 {"success":true}, ghi payment_transaction,
#          KHÔNG đổi trạng thái đơn
# =============================================================
Send-Webhook @{
    id             = "TXID_CA3_$(Get-Date -f 'yyyyMMddHHmmss')"
    transferType   = "in"
    transferAmount = $PARTIAL_AMOUNT
    content        = "Chuyen khoan $VALID_CODE"
    code           = $VALID_CODE
    subAccount     = "VA_001"
} -desc "CA3 — Thieu tien ($PARTIAL_AMOUNT < $VALID_AMOUNT)"

# =============================================================
# CA 4 — GỬI TRÙNG CÙNG ID (duplicate txId)
# Kỳ vọng: HTTP 200 {"success":true}, không làm gì, log "Giao dịch trùng"
# =============================================================
$DUP_TX_ID = "TXID_CA4_DUPLICATE_FIXED"
Send-Webhook @{
    id             = $DUP_TX_ID
    transferType   = "in"
    transferAmount = $VALID_AMOUNT
    content        = "Chuyen khoan $VALID_CODE"
    code           = $VALID_CODE
    subAccount     = "VA_001"
} -desc "CA4 — Lan 1 (gui lan dau txId=$DUP_TX_ID)"

Send-Webhook @{
    id             = $DUP_TX_ID
    transferType   = "in"
    transferAmount = $VALID_AMOUNT
    content        = "Chuyen khoan $VALID_CODE"
    code           = $VALID_CODE
    subAccount     = "VA_001"
} -desc "CA4 — Lan 2 (trung txId=$DUP_TX_ID)"

# =============================================================
# CA 5 — MÃ KHÔNG TỒN TẠI (paymentCode không có trong DB)
# Kỳ vọng: HTTP 200 {"success":true}, log "Không tìm thấy đơn"
# =============================================================
Send-Webhook @{
    id             = "TXID_CA5_$(Get-Date -f 'yyyyMMddHHmmss')"
    transferType   = "in"
    transferAmount = 999999
    content        = "Chuyen khoan DH99999999"
    code           = "DH99999999"
    subAccount     = "VA_001"
} -desc "CA5 — Ma khong ton tai (DH99999999)"

# =============================================================
# CA 6 — TIỀN VỀ SAU HẠN (đơn đã "Hết hạn (SePay)")
# Kỳ vọng: HTTP 200, paymentStatus → "Đã thanh toán sau hạn - cần kiểm tra"
# Chuẩn bị: đơn $EXPIRED_CODE phải đang ở trạng thái "Hết hạn (SePay)"
# =============================================================
Send-Webhook @{
    id             = "TXID_CA6_$(Get-Date -f 'yyyyMMddHHmmss')"
    transferType   = "in"
    transferAmount = $VALID_AMOUNT
    content        = "Chuyen khoan $EXPIRED_CODE muon"
    code           = $EXPIRED_CODE
    subAccount     = "VA_001"
} -desc "CA6 — Tien ve sau han (don da 'Het han (SePay)')"

# =============================================================
# CA 7 — DH1 SO VỚI DH12 (không nhầm lẫn partial match)
# Subcase A: content chứa "DH1" — kỳ vọng khớp DH1, KHÔNG khớp DH12
# Subcase B: content chứa "DH12" — kỳ vọng khớp DH12, không khớp DH1
# Subcase C: content chứa "DH1 va DH12" — nhiều mã → log, không xử lý
# =============================================================
Send-Webhook @{
    id             = "TXID_CA7A_$(Get-Date -f 'yyyyMMddHHmmss')"
    transferType   = "in"
    transferAmount = 100000
    content        = "Thanh toan don hang DH1 thang 10"
    code           = $null
    subAccount     = "VA_001"
} -desc "CA7A — Content 'DH1' (khong nham voi DH12)"

Send-Webhook @{
    id             = "TXID_CA7B_$(Get-Date -f 'yyyyMMddHHmmss')"
    transferType   = "in"
    transferAmount = 100000
    content        = "Thanh toan don hang DH12 thang 10"
    code           = $null
    subAccount     = "VA_001"
} -desc "CA7B — Content 'DH12' (khong nham voi DH1)"

Send-Webhook @{
    id             = "TXID_CA7C_$(Get-Date -f 'yyyyMMddHHmmss')"
    transferType   = "in"
    transferAmount = 100000
    content        = "Thanh toan DH1 va DH12 gop"
    code           = $null
    subAccount     = "VA_001"
} -desc "CA7C — Content co hai ma DH1 va DH12 → nhieu ma, khong xu ly"

Write-Host ""
Write-Host ("=" * 60)
Write-Host "Hoan thanh tat ca ca kiem thu."
Write-Host "Kiem tra log Tomcat (catalina.out) de xem ket qua chi tiet."
Write-Host ("=" * 60)
