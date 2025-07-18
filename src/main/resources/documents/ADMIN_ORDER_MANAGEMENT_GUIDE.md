# 📋 API QUẢN LÝ ĐỐN HÀNG (ORDER MANAGEMENT) - HƯỚNG DẪN ADMIN

## 🎯 TỔNG QUAN NGHIỆP VỤ

### Quy tắc Voucher
- **Tối đa 2 voucher** cho mỗi đơn hàng:
  - **1 voucher thường** (PERCENTAGE hoặc FIXED_AMOUNT) 
  - **1 voucher miễn phí ship** (FREE_SHIPPING)

### Tự động phát hiện Flash Sale
- Hệ thống tự động tìm flash sale tốt nhất cho sản phẩm
- Ưu tiên flash sale có giá thấp nhất và còn stock
- Admin không cần chọn flash sale thủ công

---

## 💰 API TÍNH TOÁN TẠM TIỀN

### `POST /api/orders/calculate`
**Mục đích**: Admin xem trước số tiền cuối cùng user phải trả trước khi tạo đơn thực tế.

#### Request Body:
```json
{
  "userId": 1,
  "shippingFee": 25000,
  "items": [
    {
      "bookId": 123,
      "quantity": 2
    },
    {
      "bookId": 456, 
      "quantity": 1
    }
  ],
  "voucherIds": [1, 2]  // Optional: tối đa 2 voucher
}
```

#### Response Success (200):
```json
{
  "status": 200,
  "message": "Tính toán thành công",
  "data": {
    "userId": 1,
    "userEmail": "user@example.com",
    "userName": "Nguyen Van A",
    "itemDetails": [
      {
        "bookId": 123,
        "bookName": "Sách A",
        "bookCode": "BOOK123",
        "quantity": 2,
        "originalPrice": 200000,
        "unitPrice": 150000,     // Giá sau flash sale
        "itemTotal": 300000,
        "isFlashSale": true,
        "flashSaleItemId": 10,
        "savedAmount": 100000,   // Tiết kiệm từ flash sale
        "flashSaleName": "Flash Sale Cuối Năm"
      }
    ],
    "subtotal": 300000,                    // Tổng tiền hàng
    "shippingFee": 25000,                  // Phí ship
    "totalBeforeDiscount": 325000,         // Tổng trước giảm giá
    "regularVoucherDiscount": 30000,       // Giảm từ voucher thường
    "shippingVoucherDiscount": 25000,      // Giảm từ voucher ship
    "totalVoucherDiscount": 55000,         // Tổng giảm voucher
    "appliedVouchers": [
      {
        "voucherId": 1,
        "voucherCode": "DISCOUNT10",
        "voucherName": "Giảm 10%",
        "voucherType": "PERCENTAGE", 
        "discountApplied": 30000,
        "description": "Giảm 10% (tối đa 50000đ)"
      }
    ],
    "finalTotal": 270000,                  // TỔNG CUỐI CÙNG PHẢI TRẢ
    "message": "🔥 1 sản phẩm được áp dụng flash sale. 🎫 Áp dụng 2 voucher."
  }
}
```

---

## 🔧 API QUẢN LÝ ĐƠN HÀNG (CRUD)

### 1. Lấy danh sách đơn hàng - `GET /api/orders`
```
GET /api/orders?page=0&size=10&code=ORD&userId=1&orderStatus=PENDING&orderType=NORMAL
```

**Response**:
```json
{
  "status": 200,
  "message": "Thành công",
  "data": {
    "content": [
      {
        "orderId": 123,
        "orderCode": "ORD20250715001",
        "userId": 1,
        "userName": "Nguyen Van A",
        "userEmail": "user@example.com",
        "address": {
          "id": 5,
          "name": "Nguyen Van A - 123 ABC, Ward, District, Province (Mặc định)"
        },
        "orderType": "NORMAL",
        "orderStatus": "PENDING",
        "shippingFee": 25000,
        "subtotal": 300000,
        "totalBeforeDiscount": 325000,
        "regularVoucherDiscount": 30000,
        "shippingVoucherDiscount": 25000,
        "totalVoucherDiscount": 55000,
        "finalTotal": 270000,
        "itemDetails": [
          {
            "bookId": 123,
            "bookName": "Sách A",
            "bookCode": "BOOK123",
            "quantity": 2,
            "originalPrice": 200000,
            "unitPrice": 150000,
            "itemTotal": 300000,
            "isFlashSale": true,
            "flashSaleItemId": 10,
            "savedAmount": 100000,
            "flashSaleName": "Flash Sale Cuối Năm"
          },
          {
            "bookId": 456,
            "bookName": "Sách B",
            "bookCode": "BOOK456",
            "quantity": 1,
            "originalPrice": 150000,
            "unitPrice": 150000,
            "itemTotal": 150000,
            "isFlashSale": false
          }
        ],
        "appliedVouchers": [
          {
            "voucherId": 1,
            "voucherCode": "DISCOUNT10",
            "voucherName": "Giảm 10%",
            "voucherType": "PERCENTAGE",
            "discountApplied": 30000,
            "description": "Giảm 10% (tối đa 50000đ)"
          },
          {
            "voucherId": 2,
            "voucherCode": "FREESHIP25K",
            "voucherName": "Miễn phí ship 25K",
            "voucherType": "FREE_SHIPPING",
            "discountApplied": 25000,
            "description": "Giảm phí ship tối đa 25.000đ"
          }
        ],
        "notes": "Giao hàng trong giờ hành chính",
        "createdAt": 1720992000000,
        "updatedAt": 1720995600000,
        "staffId": 2,
        "staffName": "Admin B"
      }
      // ... các đơn hàng khác ...
    ],
    "pageNumber": 0,
    "pageSize": 10,
    "totalElements": 50,
    "totalPages": 5
  }
}
```

### 2. Lấy chi tiết đơn hàng - `GET /api/orders/{id}`
```
GET /api/orders/123
```

### 3. Tạo đơn hàng - `POST /api/orders`
```json
{
  "userId": 1,
  "staffId": 2,                    // Optional: admin tạo đơn
  "addressId": 5,
  "shippingFee": 25000,
  "orderStatus": "PENDING",
  "orderType": "NORMAL",           // NORMAL, EVENT_GIFT, PROMOTIONAL, SAMPLE
  "orderDetails": [
    {
      "bookId": 123,
      "quantity": 2
    }
  ],
  "voucherIds": [1, 2],           // Optional: tối đa 2 voucher  
  "notes": "Ghi chú đơn hàng"     // Optional
}
```

### 4. Cập nhật đơn hàng - `PUT /api/orders/{id}`
**Lưu ý**: Chỉ được cập nhật khi đơn hàng ở trạng thái `PENDING`

### 5. Cập nhật trạng thái - `PATCH /api/orders/{id}/status`
```
PATCH /api/orders/123/status?newStatus=CONFIRMED&staffId=2
```

### 6. Hủy đơn hàng - `PATCH /api/orders/{id}/cancel`
```
PATCH /api/orders/123/cancel?reason=Khách hàng yêu cầu&userId=1
```

---

## 📋 API DROPDOWN CHO KHÓA NGOẠI

### Users (Khách hàng)
```
GET /api/users/dropdown
Response: [{"id": 1, "name": "Nguyen Van A"}, ...]
```

### Staff (Nhân viên - cùng API users nhưng filter role)
```
GET /api/users/dropdown
```

### Addresses của User
```
GET /api/addresses/user/{userId}/dropdown  
Response: [{"id": 1, "name": "Nguyen Van A - 123 ABC, Ward, District, Province (Mặc định)"}, ...]
```

### Books (Sản phẩm)
```
GET /api/books/dropdown
Response: [{"id": 1, "name": "Tên sách"}, ...]
```

### Vouchers (Mã giảm giá)
```
GET /api/vouchers/dropdown  // Cần tạo thêm API này
Response: [{"id": 1, "name": "DISCOUNT10 - Giảm 10%"}, ...]
```

---

## 📊 ENUM OPTIONS

### Trạng thái đơn hàng - `GET /api/orders/order-statuses`
```json
[
  {"value": "PENDING", "label": "Chờ xử lý"},
  {"value": "CONFIRMED", "label": "Đã xác nhận"}, 
  {"value": "SHIPPED", "label": "Đang giao hàng"},
  {"value": "DELIVERED", "label": "Đã giao hàng"},
  {"value": "CANCELED", "label": "Đã hủy"},
  {"value": "REFUNDING", "label": "Đang hoàn tiền"},
  {"value": "REFUNDED", "label": "Đã hoàn tiền"},
  {"value": "RETURNED", "label": "Đã trả hàng"},
  {"value": "PARTIALLY_REFUNDED", "label": "Hoàn tiền một phần"}
]
```

### Loại đơn hàng - `GET /api/orders/order-types`
```json
[
  {"value": "NORMAL", "label": "Đơn hàng thường"},
  {"value": "EVENT_GIFT", "label": "Đơn hàng giao quà sự kiện"},
  {"value": "PROMOTIONAL", "label": "Đơn hàng khuyến mãi đặc biệt"},
  {"value": "SAMPLE", "label": "Đơn hàng gửi mẫu"}
]
```

---

## ⚠️ QUY TẮC NGHIỆP VỤ THỰC TẾ

### Trạng thái đơn hàng:
- ✅ **PENDING**: Có thể sửa đổi, hủy
- ✅ **CONFIRMED**: Có thể hủy, chuyển sang SHIPPED
- ❌ **SHIPPED**: Không thể sửa, chỉ có thể chuyển sang DELIVERED
- ❌ **DELIVERED**: Chỉ có thể hoàn hàng (RETURNED)
- ❌ **CANCELED/REFUNDED**: Không thể thay đổi

### Hoàn hàng:
- Chỉ cho phép hoàn hàng khi trạng thái là **DELIVERED**
- Cần lý do hoàn hàng rõ ràng
- Có thể hoàn toàn bộ hoặc một phần

### Validation nghiêm ngặt:
- ✅ Kiểm tra tồn kho thực tế
- ✅ Kiểm tra flash sale còn hiệu lực
- ✅ Kiểm tra voucher hợp lệ và chưa hết lượt
- ✅ Kiểm tra user có quyền sử dụng voucher
- ✅ Kiểm tra địa chỉ thuộc về user

### Logs và Audit:
- Ghi lại mọi thay đổi trạng thái đơn hàng
- Lưu thông tin nhân viên thực hiện thao tác
- Timestamp chính xác cho mọi hoạt động

---

**📌 Lưu ý**: Tài liệu này chỉ tập trung vào API backend. Frontend tự xử lý UI/UX và validation phía client.
