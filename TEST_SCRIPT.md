# BookStation Backend - Test Script cho Counter Sales & Book Search

## Test Book Search với Multiple Keywords

### Test Case: Search "chí đắc" nên trả về "Đắc nhân tâm" và "Chí Phèo"

```bash
# Test book search với multiple keywords
curl -X GET "http://localhost:8080/api/books?name=chí đắc" \
  -H "Content-Type: application/json" | jq
```

**Expected Result**: Sẽ trả về cả hai quyển sách vì:
- "Chí Phèo" chứa từ "chí" 
- "Đắc nhân tâm" chứa từ "đắc"
- Logic OR sẽ tìm sách chứa BẤT KỲ từ khóa nào

---

## Test Voucher Search API

### Test 1: Tìm voucher theo mã
```bash
curl -X GET "http://localhost:8080/api/vouchers/search?query=SAVE10&limit=5" \
  -H "Content-Type: application/json" | jq
```

### Test 2: Tìm voucher theo tên
```bash
curl -X GET "http://localhost:8080/api/vouchers/search?query=giảm giá&limit=10" \
  -H "Content-Type: application/json" | jq
```

### Test 3: Lấy tất cả voucher active (không có query)
```bash
curl -X GET "http://localhost:8080/api/vouchers/search?limit=20" \
  -H "Content-Type: application/json" | jq
```

---

## Test Counter Sales API với Backend Calculation

### Test 1: Calculate Counter Sale (Preview)
```bash
curl -X POST "http://localhost:8080/api/counter-sales/calculate" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": null,
    "customerName": "Khách vãng lai", 
    "customerPhone": "0987654321",
    "orderDetails": [
      {
        "bookId": 1,
        "quantity": 2,
        "unitPrice": 999999,
        "frontendPrice": 999999
      }
    ],
    "voucherIds": [],
    "subtotal": 999999,
    "totalAmount": 999999,
    "staffId": 1,
    "paymentMethod": "CASH"
  }' | jq
```

**Expected**: Backend sẽ bỏ qua giá frontend (999999) và tính lại giá thực từ database.

### Test 2: Create Counter Sale
```bash
curl -X POST "http://localhost:8080/api/counter-sales" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": null,
    "customerName": "Trần Thị B",
    "customerPhone": "0912345678", 
    "orderDetails": [
      {
        "bookId": 1,
        "quantity": 1,
        "unitPrice": 888888,
        "frontendPrice": 888888
      }
    ],
    "voucherIds": [],
    "subtotal": 888888,
    "totalAmount": 888888,
    "staffId": 1,
    "paymentMethod": "CARD"
  }' | jq
```

### Test 3: Get Counter Sale Details
```bash
# Thay {orderId} bằng ID từ response test 2
curl -X GET "http://localhost:8080/api/counter-sales/{orderId}" \
  -H "Content-Type: application/json" | jq
```

### Test 4: Cancel Counter Sale (trong 24h)
```bash
# Thay {orderId} bằng ID từ response test 2
curl -X PATCH "http://localhost:8080/api/counter-sales/{orderId}/cancel?staffId=1&reason=Test hủy đơn" \
  -H "Content-Type: application/json" | jq
```

---

## Test Processing Quantity Fix

### Test: Kiểm tra processing quantity khi đơn hàng ở trạng thái GOODS_RECEIVED_FROM_CUSTOMER
```bash
curl -X GET "http://localhost:8080/api/books/{bookId}/processing-quantity" \
  -H "Content-Type: application/json" | jq
```

**Expected**: Khi có đơn hàng ở trạng thái GOODS_RECEIVED_FROM_CUSTOMER, processing quantity sẽ = 1.

---

## Workflow Test: Complete Counter Sales

### Step 1: Search for books
```bash
curl -X GET "http://localhost:8080/api/books?name=đắc chí" \
  -H "Content-Type: application/json" | jq '.data.content[] | {id, bookName, price}'
```

### Step 2: Search for vouchers (if customer has voucher)
```bash
curl -X GET "http://localhost:8080/api/vouchers/search?query=SAVE" \
  -H "Content-Type: application/json" | jq '.data[] | {id, code, name, discountPercentage}'
```

### Step 3: Calculate total (with voucher if applicable)
```bash
curl -X POST "http://localhost:8080/api/counter-sales/calculate" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "orderDetails": [
      {
        "bookId": 1,
        "quantity": 1,
        "unitPrice": 0,
        "frontendPrice": 0
      }
    ],
    "voucherIds": [1],
    "subtotal": 0,
    "totalAmount": 0,
    "staffId": 1,
    "paymentMethod": "CASH"
  }' | jq '.data | {subtotal, discountAmount, totalAmount}'
```

### Step 4: Create the order
```bash
curl -X POST "http://localhost:8080/api/counter-sales" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "orderDetails": [
      {
        "bookId": 1,
        "quantity": 1,
        "unitPrice": 0,
        "frontendPrice": 0
      }
    ],
    "voucherIds": [1],
    "subtotal": 0,
    "totalAmount": 0,
    "staffId": 1,
    "paymentMethod": "CASH"
  }' | jq
```

---

## Key Improvements Made

### 1. Book Search Logic ✅
- **Before**: AND logic - phải chứa TẤT CẢ từ khóa
- **After**: OR logic - chứa BẤT KỲ từ khóa nào
- **Result**: Search "chí đắc" = "Chí Phèo" OR "Đắc nhân tâm"

### 2. Processing Quantity Fix ✅
- **Added**: GOODS_RECEIVED_FROM_CUSTOMER vào PROCESSING_STATUSES
- **Result**: Khi admin nhận hàng hoàn trả, processing quantity = 1

### 3. Counter Sales Backend Calculation ✅
- **Price Calculation**: Hoàn toàn từ backend database
- **Frontend Validation**: Chỉ để warning, không trust frontend
- **Flash Sale**: Tự động áp dụng giá sale
- **Voucher**: Tích hợp với hệ thống voucher hiện có

### 4. Voucher Search API ✅
- **Search by Code**: Tìm theo mã voucher
- **Search by Name**: Tìm theo tên voucher
- **Active Only**: Chỉ voucher đang hoạt động
- **Usage Limit**: Kiểm tra voucher còn lượt sử dụng

---

## Notes
- Tất cả API đều có validation đầy đủ
- Error handling với BusinessException rõ ràng
- Transaction đảm bảo data consistency
- Logging cho debugging và monitoring
