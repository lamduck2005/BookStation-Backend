# Order Management API Documentation

## Tổng quan
Hệ thống quản lý đơn hàng hoàn chỉnh với các chức năng CRUD và nhiều tính năng nâng cao.

## Cấu trúc Database

### Order Entity
- `id`: ID đơn hàng (PK)
- `code`: Mã đơn hàng (unique)
- `user_id`: ID người đặt hàng (FK)
- `staff_id`: ID nhân viên xử lý (FK) 
- `address_id`: ID địa chỉ giao hàng (FK)
- `order_date`: Ngày đặt hàng
- `total_amount`: Tổng tiền
- `status`: Trạng thái (byte)
- `order_status`: Trạng thái đơn hàng (enum)
- `order_type`: Loại đơn hàng
- `created_at`, `updated_at`: Timestamps
- `created_by`, `updated_by`: Người tạo/cập nhật

### OrderDetail Entity  
- `order_id` + `book_id`: Composite PK
- `flash_sale_item_id`: ID sản phẩm flash sale (FK)
- `quantity`: Số lượng
- `unit_price`: Đơn giá

### OrderVoucher Entity
- `order_id` + `voucher_id`: Composite PK
- Liên kết đơn hàng với voucher

## API Endpoints

### 1. Lấy danh sách đơn hàng có phân trang
```
GET /api/orders
Parameters:
- page: Trang (default: 0)
- size: Kích thước trang (default: 5) 
- code: Mã đơn hàng (optional)
- userId: ID người dùng (optional)
- orderStatus: Trạng thái đơn hàng (optional)
- orderType: Loại đơn hàng (optional)
- startDate: Ngày bắt đầu (optional)
- endDate: Ngày kết thúc (optional)
```

### 2. Lấy chi tiết đơn hàng
```
GET /api/orders/{id}
Response: OrderResponse với đầy đủ thông tin chi tiết
```

### 3. Tạo đơn hàng mới
```
POST /api/orders
Body: OrderRequest
{
  "userId": 1,
  "staffId": 2,
  "addressId": 3,
  "totalAmount": 150000,
  "orderStatus": "PENDING",
  "orderType": "NORMAL",
  "orderDetails": [
    {
      "bookId": 1,
      "flashSaleItemId": null,
      "quantity": 2,
      "unitPrice": 75000
    }
  ],
  "voucherIds": [1, 2],
  "notes": "Giao hàng cẩn thận"
}
```

### 4. Cập nhật đơn hàng
```
PUT /api/orders/{id}
Body: OrderRequest (tương tự tạo mới)
```

### 5. Cập nhật trạng thái đơn hàng
```
PATCH /api/orders/{id}/status
Parameters:
- newStatus: Trạng thái mới
- staffId: ID nhân viên (optional)
```

### 6. Hủy đơn hàng
```
PATCH /api/orders/{id}/cancel
Parameters:
- reason: Lý do hủy (optional)
- userId: ID người hủy
```

### 7. Xóa đơn hàng
```
DELETE /api/orders/{id}
```

### 8. Lấy đơn hàng theo người dùng
```
GET /api/orders/user/{userId}
```

### 9. Lấy đơn hàng theo trạng thái
```
GET /api/orders/status/{status}
```

### 10. Tìm ID đơn hàng theo mã
```
GET /api/orders/id?orderCode={code}
```

### 11. Lấy danh sách trạng thái đơn hàng
```
GET /api/orders/order-statuses
Response: List<EnumOptionResponse>
```

### 12. Lấy danh sách loại đơn hàng
```
GET /api/orders/order-types
Response: List<EnumOptionResponse>
```

### 13. Lấy dropdown đơn hàng
```
GET /api/orders/dropdown
Response: List<DropdownOptionResponse>
```

## Order Status Enum

- `PENDING`: Chờ xử lý
- `CONFIRMED`: Đã xác nhận
- `SHIPPED`: Đang giao hàng
- `DELIVERED`: Đã giao hàng
- `CANCELED`: Đã hủy

## Order Type Options

- `NORMAL`: Đơn hàng thường (khách mua)
- `EVENT_GIFT`: Đơn hàng giao quà sự kiện
- `PROMOTIONAL`: Đơn hàng khuyến mãi đặc biệt
- `SAMPLE`: Đơn hàng gửi mẫu

## Các tính năng đặc biệt

### 1. Tự động sinh mã đơn hàng
- Format: ORD + timestamp(6 digits) + random(8 chars)
- Ví dụ: ORD123456ABCD1234

### 2. Transaction Management
- Tất cả operations đều được bọc trong transaction
- Rollback tự động khi có lỗi

### 3. Validation Business Rules
- Chỉ cập nhật đơn hàng khi ở trạng thái PENDING
- Chỉ hủy đơn hàng khi ở trạng thái PENDING hoặc CONFIRMED
- Validate tồn tại của User, Address, Book, Voucher

### 4. Flexible Search
- Hỗ trợ tìm kiếm theo nhiều tiêu chí
- Phân trang và sắp xếp

### 5. Comprehensive Response
- OrderResponse bao gồm thông tin chi tiết đầy đủ
- Thông tin User, Staff, Address
- Chi tiết sản phẩm và voucher

## Response Format

### Success Response
```json
{
  "status": 200,
  "message": "Thành công",
  "data": { ... }
}
```

### Error Response
```json
{
  "status": 404,
  "message": "Không tìm thấy đơn hàng",
  "data": null
}
```

## Cách chạy ứng dụng

1. Đảm bảo đã cài đặt Java 17+ và Maven
2. Cấu hình database trong `application.properties`
3. Chạy lệnh: `mvn spring-boot:run`
4. API sẽ khả dụng tại: `http://localhost:8080/api/orders`

## Testing

Sử dụng Postman hoặc curl để test các API:

```bash
# Lấy danh sách đơn hàng
curl -X GET "http://localhost:8080/api/orders?page=0&size=10"

# Tạo đơn hàng mới
curl -X POST "http://localhost:8080/api/orders" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "addressId": 1,
    "totalAmount": 150000,
    "orderType": "NORMAL",
    "orderDetails": [
      {
        "bookId": 1,
        "quantity": 1,
        "unitPrice": 150000
      }
    ]
  }'
```
