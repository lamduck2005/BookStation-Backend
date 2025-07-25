# 🛒 COUNTER SALES API DOCUMENTATION

## Tổng quan
API bán hàng tại quầy cho phép admin/staff tạo đơn hàng trực tiếp tại cửa hàng, không cần địa chỉ giao hàng và có thể bán cho khách vãng lai.

## Base URL
```
/api/counter-sales
```

## 🔥 1. Tạo đơn hàng tại quầy
**Endpoint:** `POST /api/counter-sales`

### Request Body
```json
{
  "userId": 123,                    // Optional: ID khách hàng có tài khoản
  "customerName": "Nguyễn Văn A",   // Optional nếu có userId
  "customerPhone": "0987654321",    // Optional nếu có userId  
  "staffId": 5,                     // Required: ID nhân viên bán hàng
  "paymentMethod": "CASH",          // CASH|BANK_TRANSFER
  "orderDetails": [
    {
      "bookId": 101,
      "quantity": 2,
      "unitPrice": 150000,
      "flashSaleItemId": null       // Optional: nếu là flash sale
    },
    {
      "bookId": 102, 
      "quantity": 1,
      "unitPrice": 200000,
      "flashSaleItemId": 45         // Flash sale item
    }
  ],
  "voucherIds": [10, 15],          // Optional: voucher áp dụng
  "subtotal": 500000,
  "totalAmount": 450000,
  "notes": "Khách hàng VIP"
}
```

### Response Success (201)
```json
{
  "status": 200,
  "message": "Tạo đơn hàng tại quầy thành công",
  "data": {
    "orderId": 1001,
    "orderCode": "ORD1738089234567",
    "orderStatus": "DELIVERED",
    "orderType": "COUNTER",
    "userId": 123,
    "customerName": "Nguyễn Văn A",
    "customerPhone": "0987654321", 
    "subtotal": 500000,
    "discountAmount": 50000,
    "totalAmount": 450000,
    "paymentMethod": "CASH",
    "staffId": 5,
    "staffName": "Nhân viên A",
    "orderDate": 1738089234567,
    "items": [
      {
        "bookId": 101,
        "bookName": "Đắc nhân tâm",
        "bookCode": "BOOK001",
        "quantity": 2,
        "unitPrice": 150000,
        "totalPrice": 300000,
        "isFlashSale": false,
        "originalPrice": 150000,
        "savedAmount": 0
      },
      {
        "bookId": 102,
        "bookName": "Chí Phèo", 
        "bookCode": "BOOK002",
        "quantity": 1,
        "unitPrice": 200000,
        "totalPrice": 200000,
        "isFlashSale": true,
        "flashSaleItemId": 45,
        "originalPrice": 250000,
        "savedAmount": 50000
      }
    ],
    "appliedVouchers": [
      {
        "voucherId": 10,
        "voucherCode": "DISCOUNT10",
        "voucherName": "Giảm 10%",
        "discountAmount": 50000,
        "voucherType": "REGULAR_PERCENTAGE"
      }
    ]
  }
}
```

### Response Error (400)
```json
{
  "status": 400,
  "message": "Sách 'Đắc nhân tâm' không đủ tồn kho. Còn lại: 1",
  "data": null
}
```

---

## 🧮 2. Tính toán giá tại quầy (Preview)
**Endpoint:** `POST /api/counter-sales/calculate`

### Request Body
Giống như API tạo đơn hàng

### Response Success (200)
```json
{
  "status": 200,
  "message": "Tính toán thành công",
  "data": {
    "subtotal": 500000,
    "discountAmount": 50000,
    "totalAmount": 450000,
    "items": [...],
    "appliedVouchers": [...]
  }
}
```

---

## 📋 3. Xem chi tiết đơn hàng tại quầy
**Endpoint:** `GET /api/counter-sales/{orderId}`

### Response Success (200)
```json
{
  "status": 200,
  "message": "Lấy thông tin đơn hàng thành công",
  "data": {
    "id": 1001,
    "code": "ORD1738089234567",
    "orderStatus": "DELIVERED",
    "orderType": "COUNTER",
    "user": {
      "id": 123,
      "fullName": "Nguyễn Văn A",
      "phoneNumber": "0987654321"
    },
    "staff": {
      "id": 5,
      "fullName": "Nhân viên A"
    },
    "subtotal": 500000,
    "totalAmount": 450000,
    "orderDate": 1738089234567,
    "orderDetails": [...],
    "vouchers": [...]
  }
}
```

---

## ❌ 4. Hủy đơn hàng tại quầy
**Endpoint:** `PATCH /api/counter-sales/{orderId}/cancel`

### Query Parameters
- `staffId` (required): ID nhân viên thực hiện hủy
- `reason` (optional): Lý do hủy

### Response Success (200)
```json
{
  "status": 200,
  "message": "Hủy đơn hàng thành công",
  "data": {
    "id": 1001,
    "orderStatus": "CANCELED",
    "cancelReason": "Khách hàng đổi ý"
  }
}
```

### Response Error (400)
```json
{
  "status": 400,
  "message": "Chỉ có thể hủy đơn hàng tại quầy trong vòng 24 giờ",
  "data": null
}
```

---

## 🚫 Các trường hợp lỗi

### 1. Validation Errors
- **400**: Thiếu thông tin bắt buộc
- **400**: Số lượng <= 0
- **400**: Giá <= 0  
- **400**: Khách vãng lai thiếu tên/số điện thoại

### 2. Business Logic Errors
- **400**: Sách không đủ tồn kho
- **400**: Flash sale item không đủ stock
- **404**: Không tìm thấy sách/staff/voucher
- **400**: Voucher không hợp lệ hoặc hết lượt

### 3. Cancel Order Errors
- **400**: Đơn hàng không phải loại COUNTER
- **400**: Quá thời gian hủy (24h)
- **400**: Đơn hàng đã bị hủy/hoàn trước đó

---

## 📝 Lưu ý đặc biệt

### Khách hàng vãng lai
```json
{
  "userId": null,                   // Không có tài khoản
  "customerName": "Khách vãng lai", // Bắt buộc
  "customerPhone": "0987654321",    // Bắt buộc
  "staffId": 5
}
```

### Khách hàng có tài khoản
```json
{
  "userId": 123,                    // Có tài khoản
  "customerName": null,             // Optional (lấy từ user)
  "customerPhone": null,            // Optional (lấy từ user)
  "staffId": 5
}
```

### Thanh toán
- **CASH**: Tiền mặt (mặc định)

- **BANK_TRANSFER**: Chuyển khoản ngân hàng

### Trạng thái đơn hàng
- Mặc định: **DELIVERED** (đã thanh toán và nhận hàng ngay)
- Có thể hủy: **CANCELED** (trong vòng 24h)

### Tồn kho và Voucher
- Tất cả logic tồn kho, flash sale, voucher hoạt động như đơn hàng online
- Stock được trừ ngay khi tạo đơn thành công
- Sold count được cộng ngay khi đơn hàng DELIVERED
- Voucher usage được tăng và có thể được hoàn lại khi hủy đơn

---

## � 5. Tìm kiếm Voucher cho Counter Sales
**Endpoint:** `GET /api/vouchers/search`

### Query Parameters
- `query` (optional): Mã voucher hoặc tên voucher để tìm kiếm
- `limit` (optional, default=10): Số lượng kết quả tối đa

### Request Example
```http
GET /api/vouchers/search?query=SAVE10&limit=5
```

### Response Success (200)
```json
{
  "status": 200,
  "message": "Tìm kiếm voucher thành công",
  "data": [
    {
      "id": 5,
      "code": "SAVE10",
      "name": "Giảm giá 10%",
      "description": "Giảm 10% cho đơn hàng từ 500K",
      "voucherCategory": "NORMAL",
      "discountType": "PERCENTAGE", 
      "discountPercentage": 10,
      "discountAmount": null,
      "minOrderValue": 500000,
      "maxDiscountValue": 100000,
      "startTime": 1751234567890,
      "endTime": 1759234567890,
      "usageLimit": 100,
      "usedCount": 25,
      "status": 1
    }
  ]
}
```

### Use Case
1. **Staff nhập mã voucher**: Khách đưa voucher "SAVE10", staff search để kiểm tra
2. **Tìm theo tên**: Staff gõ "giảm giá" để tìm tất cả voucher giảm giá
3. **Kiểm tra số lượng**: Xem voucher còn bao nhiêu lượt sử dụng

---

## �🔧 Error Handling

### Frontend nên xử lý:
1. **Validation errors**: Hiển thị lỗi tại field tương ứng
2. **Stock errors**: Thông báo không đủ hàng, đề xuất số lượng có sẵn
3. **Voucher errors**: Thông báo voucher không hợp lệ
4. **Cancel errors**: Thông báo không thể hủy và lý do

### Retry Logic:
- API tính toán có thể retry khi lỗi network
- API tạo đơn **KHÔNG** nên retry tự động (tránh tạo trùng)
- API hủy đơn có thể retry an toàn
