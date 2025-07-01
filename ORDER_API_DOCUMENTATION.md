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
- `subtotal`: Tổng tiền sản phẩm (chưa tính phí ship, chưa giảm giá)
- `shipping_fee`: Phí vận chuyển
- `discount_amount`: Tổng giảm giá từ voucher sản phẩm
- `discount_shipping`: Giảm giá phí ship từ voucher freeship
- `total_amount`: Tổng tiền cuối cùng khách phải trả
- `status`: Trạng thái (byte)
- `order_status`: Trạng thái đơn hàng (enum)
- `order_type`: Loại đơn hàng
- `notes`: Ghi chú đơn hàng
- `cancel_reason`: Lý do hủy/hoàn trả
- `regular_voucher_count`: Số lượng voucher thường đã áp dụng (0-1)
- `shipping_voucher_count`: Số lượng voucher freeship đã áp dụng (0-1)
- `created_at`, `updated_at`: Timestamps
- `created_by`, `updated_by`: Người tạo/cập nhật

### OrderDetail Entity  
- `order_id` + `book_id`: Composite PK
- `flash_sale_item_id`: ID sản phẩm flash sale (FK)
- `quantity`: Số lượng
- `unit_price`: Đơn giá

### OrderVoucher Entity
- `order_id` + `voucher_id`: Composite PK
- `voucher_type`: Loại voucher được áp dụng (enum)
- `discount_applied`: Số tiền giảm giá thực tế được áp dụng
- `applied_at`: Thời gian áp dụng voucher
- Liên kết đơn hàng với voucher

### Voucher Entity
- `id`: ID voucher (PK)
- `code`: Mã voucher (unique)
- `name`: Tên voucher
- `description`: Mô tả voucher
- `voucher_type`: Loại voucher (PERCENTAGE, FIXED_AMOUNT, FREE_SHIPPING)
- `discount_percentage`: Phần trăm giảm giá (cho PERCENTAGE)
- `discount_amount`: Số tiền giảm cố định (cho FIXED_AMOUNT)
- `start_time`, `end_time`: Thời gian hiệu lực
- `min_order_value`: Giá trị đơn hàng tối thiểu
- `max_discount_value`: Giá trị giảm tối đa
- `usage_limit`: Số lượng voucher có thể sử dụng
- `used_count`: Số lượng đã sử dụng
- `usage_limit_per_user`: Giới hạn sử dụng trên 1 user

### UserVoucher Entity  
- `id`: ID (PK)
- `user_id`: ID người dùng (FK)
- `voucher_id`: ID voucher (FK)
- `used_count`: Số lần đã sử dụng voucher này
- `created_at`: Thời gian tạo
- Theo dõi việc sử dụng voucher của từng user

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
  "shippingFee": 30000,
  "orderStatus": "PENDING",
  "orderType": "NORMAL",
  "notes": "Giao hàng cẩn thận",
  "orderDetails": [
    {
      "bookId": 1,
      "flashSaleItemId": null,
      "quantity": 2,
      "unitPrice": 75000  // Sẽ được ignore, tự động tính từ Book.price hoặc FlashSaleItem.discountPrice
    }
  ],
  "voucherIds": [1, 2]
}

Response: OrderResponse
{
  "id": 1,
  "code": "ORD123456ABCD1234",
  "subtotal": 150000,      // Tự động tính: sum(quantity * actual_price)
  "shippingFee": 30000,
  "discountAmount": 15000,  // Từ voucher thường
  "discountShipping": 30000, // Từ voucher freeship
  "totalAmount": 135000,    // subtotal + shipping - discountAmount - discountShipping
  "regularVoucherCount": 1,
  "shippingVoucherCount": 1,
  ...
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

### 14. Hoàn tiền đơn hàng
```
POST /api/orders/{id}/refund
Parameters:
- refundAmount: Số tiền hoàn (optional, mặc định hoàn toàn bộ)
- reason: Lý do hoàn tiền
- staffId: ID nhân viên thực hiện
Body: RefundRequest
{
  "refundAmount": 135000,
  "reason": "Sản phẩm lỗi",
  "refundMethod": "ORIGINAL_PAYMENT"
}
```

### 15. Lấy lịch sử hoàn tiền của đơn hàng
```
GET /api/orders/{id}/refunds
Response: List<RefundResponse>
```

### 16. Kiểm tra voucher có thể áp dụng
```
POST /api/orders/validate-vouchers
Body: VoucherValidationRequest
{
  "userId": 1,
  "voucherIds": [1, 2],
  "subtotal": 150000,
  "shippingFee": 30000
}
Response: VoucherValidationResponse
{
  "valid": true,
  "totalProductDiscount": 15000,
  "totalShippingDiscount": 30000,
  "regularVoucherCount": 1,
  "shippingVoucherCount": 1,
  "errors": []
}
```

### 17. Lấy voucher khả dụng cho user
```
GET /api/vouchers/available
Parameters:
- userId: ID người dùng
- orderValue: Giá trị đơn hàng để lọc voucher
Response: List<AvailableVoucherResponse>
```

### 18. Lấy thống kê sử dụng voucher
```
GET /api/vouchers/{id}/usage-stats
Response: VoucherUsageStatsResponse
{
  "totalUsage": 150,
  "remainingUsage": 350,
  "usageByUser": {...},
  "revenueImpact": 2500000
}
```

## Order Status Enum

- `PENDING`: Chờ xử lý
- `CONFIRMED`: Đã xác nhận
- `SHIPPED`: Đang giao hàng
- `DELIVERED`: Đã giao hàng
- `CANCELED`: Đã hủy
- `REFUNDING`: Đang hoàn tiền
- `REFUNDED`: Đã hoàn tiền  
- `RETURNED`: Đã trả hàng
- `PARTIALLY_REFUNDED`: Hoàn tiền một phần

## Order Type Options

- `NORMAL`: Đơn hàng thường (khách mua)
- `EVENT_GIFT`: Đơn hàng giao quà sự kiện
- `PROMOTIONAL`: Đơn hàng khuyến mãi đặc biệt
- `SAMPLE`: Đơn hàng gửi mẫu

## Voucher System

### VoucherType Enum
- `PERCENTAGE`: Giảm giá theo phần trăm
- `FIXED_AMOUNT`: Giảm giá cố định
- `FREE_SHIPPING`: Miễn phí vận chuyển

### Business Rules for Vouchers
1. **Giới hạn số lượng voucher**: Tối đa 2 voucher trên 1 đơn hàng
2. **Giới hạn loại voucher**: 
   - Tối đa 1 voucher thường (PERCENTAGE hoặc FIXED_AMOUNT)
   - Tối đa 1 voucher freeship (FREE_SHIPPING)
3. **Kiểm tra điều kiện**:
   - Thời gian hiệu lực
   - Giá trị đơn hàng tối thiểu
   - Số lần sử dụng tối đa
   - Số lần sử dụng trên 1 user
4. **Tính toán giảm giá**:
   - PERCENTAGE: `subtotal * discount_percentage / 100`
   - FIXED_AMOUNT: `min(discount_amount, subtotal)`
   - FREE_SHIPPING: `min(shipping_fee, max_discount_value)`

### Order Amount Calculation
```
subtotal = sum(quantity * unit_price) for all order details
shipping_fee = calculated based on address and weight
discount_amount = sum of product discounts from vouchers
discount_shipping = shipping discount from freeship voucher
total_amount = subtotal + shipping_fee - discount_amount - discount_shipping
```

## Flash Sale System

### Flash Sale Business Rules
1. **Price Calculation**:
   - Regular items: Use `Book.price`
   - Flash sale items: Use `FlashSaleItem.discountPrice`
   - System tự động detect và sử dụng giá đúng

2. **Flash Sale Validation**:
   - **Time validity**: `FlashSale.startTime` ≤ current_time ≤ `FlashSale.endTime`
   - **Status check**: `FlashSale.status = 1` AND `FlashSaleItem.status = 1`
   - **Stock check**: `requested_quantity` ≤ `FlashSaleItem.stockQuantity`
   - **Per-user limit**: `requested_quantity` ≤ `FlashSaleItem.maxPurchasePerUser` (if set)

3. **Flash Sale Price Logic**:
   ```
   Original Price: Book.price = 100,000 VNĐ
   Flash Sale Discount: FlashSaleItem.discountPercentage = 30%
   Flash Sale Price: FlashSaleItem.discountPrice = 70,000 VNĐ
   
   Order calculation:
   - System uses discountPrice (70,000 VNĐ) for flash sale items
   - Voucher applies on top of already discounted price
   ```

### Order Calculation Flow
1. **Calculate Subtotal**:
   ```javascript
   subtotal = 0
   for each orderDetail:
     if (orderDetail.flashSaleItemId != null):
       price = FlashSaleItem.discountPrice
     else:
       price = Book.price
     subtotal += price * quantity
   ```

2. **Apply Vouchers**:
   ```javascript
   discountAmount = calculate_voucher_discount(subtotal, vouchers)
   discountShipping = calculate_shipping_discount(shippingFee, vouchers)
   ```

3. **Calculate Total**:
   ```javascript
   totalAmount = subtotal + shippingFee - discountAmount - discountShipping
   ```

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
- **Voucher Business Rules**:
  - Tối đa 2 voucher trên 1 đơn hàng (1 thường + 1 freeship)
  - Kiểm tra thời gian hiệu lực voucher
  - Kiểm tra giá trị đơn hàng tối thiểu
  - Kiểm tra số lần sử dụng voucher
  - Kiểm tra số lần sử dụng trên 1 user
- **Order Amount Validation**:
  - `subtotal` >= 0
  - `shipping_fee` >= 0  
  - `total_amount` = `subtotal` + `shipping_fee` - `discount_amount` - `discount_shipping`
  - `total_amount` >= 0

### 4. Flexible Search
- Hỗ trợ tìm kiếm theo nhiều tiêu chí
- Phân trang và sắp xếp

### 5. Comprehensive Response
- OrderResponse bao gồm thông tin chi tiết đầy đủ
- Thông tin User, Staff, Address
- Chi tiết sản phẩm và voucher đã áp dụng
- Thông tin chi phí chi tiết (subtotal, shipping, discounts)
- Số lượng voucher đã sử dụng theo từng loại

### 6. Advanced Voucher Management
- **Multi-type voucher support**: PERCENTAGE, FIXED_AMOUNT, FREE_SHIPPING
- **Smart voucher validation**: Kiểm tra điều kiện phức tạp
- **Usage tracking**: Theo dõi số lần sử dụng voucher
- **User-specific limits**: Giới hạn số lần sử dụng trên 1 user
- **Real-time discount calculation**: Tính toán giảm giá chính xác

### 7. Order Financial Management
- **Detailed cost breakdown**: Phân tách rõ các khoản chi phí
- **Multiple discount types**: Hỗ trợ giảm giá sản phẩm và phí ship
- **Refund support**: Hỗ trợ hoàn tiền toàn phần và một phần
- **Audit trail**: Theo dõi lịch sử thay đổi đơn hàng

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

## Ví dụ thực tế theo mô hình Shopee

### Scenario 1: Đơn hàng Flash Sale + Voucher
```json
{
  "orderRequest": {
    "userId": 1,
    "shippingFee": 25000,
    "orderDetails": [
      {
        "bookId": 1,
        "flashSaleItemId": 101,
        "quantity": 2
      }
    ],
    "voucherIds": [201]
  },
  "flashSaleItem": {
    "id": 101,
    "bookId": 1,
    "discountPrice": 70000,  // Book.price = 100000, đã giảm 30%
    "stockQuantity": 50,
    "maxPurchasePerUser": 3
  },
  "voucher": {
    "id": 201,
    "type": "PERCENTAGE", 
    "discountPercentage": 10,
    "maxDiscountValue": 20000
  },
  "calculation": {
    "subtotal": 140000,        // 70000 * 2 (dùng flash sale price)
    "productDiscount": 14000,  // min(140000*10%, 20000) = 14000
    "shippingFee": 25000,
    "shippingDiscount": 0,
    "totalAmount": 151000      // 140000 + 25000 - 14000
  }
}
```

### Scenario 2: Đơn hàng Mix (Thường + Flash Sale) + Voucher
```json
{
  "orderRequest": {
    "userId": 1,
    "shippingFee": 30000,
    "orderDetails": [
      {
        "bookId": 1,
        "flashSaleItemId": null,  // Sản phẩm thường
        "quantity": 1
      },
      {
        "bookId": 2,
        "flashSaleItemId": 102,   // Sản phẩm flash sale
        "quantity": 2
      }
    ],
    "voucherIds": [301, 302]
  },
  "items": [
    {
      "bookId": 1,
      "price": 100000,  // Giá thường
      "calculation": "100000 * 1 = 100000"
    },
    {
      "bookId": 2,
      "originalPrice": 80000,
      "flashSalePrice": 60000,  // Đã giảm 25%
      "calculation": "60000 * 2 = 120000"
    }
  ],
  "vouchers": [
    {
      "id": 301,
      "type": "FIXED_AMOUNT",
      "discountAmount": 30000
    },
    {
      "id": 302,
      "type": "FREE_SHIPPING",
      "maxDiscountValue": 30000
    }
  ],
  "calculation": {
    "subtotal": 220000,        // 100000 + 120000
    "productDiscount": 30000,  // FIXED_AMOUNT voucher
    "shippingFee": 30000,
    "shippingDiscount": 30000, // FREE_SHIPPING voucher
    "totalAmount": 190000      // 220000 + 30000 - 30000 - 30000
  }
}
```

### Scenario 3: Flash Sale Validation Errors
```json
{
  "case1_time_invalid": {
    "error": "Flash sale không trong thời gian hiệu lực",
    "details": "currentTime = 1625097600, flashSale.endTime = 1625097000"
  },
  "case2_stock_insufficient": {
    "error": "Số lượng flash sale không đủ. Có sẵn: 5",
    "details": "requestedQuantity = 10, flashSaleItem.stockQuantity = 5"
  },
  "case3_max_per_user_exceeded": {
    "error": "Vượt quá giới hạn mua 3 sản phẩm trên 1 user",
    "details": "requestedQuantity = 5, flashSaleItem.maxPurchasePerUser = 3"
  }
}
```

### Scenario 4: Đơn hàng với voucher giảm 10% + freeship (Regular products)
```json
{
  "orderRequest": {
    "userId": 1,
    "subtotal": 200000,
    "shippingFee": 25000,
    "voucherIds": [101, 201]
  },
  "vouchers": [
    {
      "id": 101,
      "type": "PERCENTAGE", 
      "discountPercentage": 10,
      "maxDiscountValue": 50000,
      "minOrderValue": 100000
    },
    {
      "id": 201,
      "type": "FREE_SHIPPING",
      "maxDiscountValue": 30000
    }
  ],
  "calculation": {
    "subtotal": 200000,
    "productDiscount": 20000,  // min(200000*10%, 50000) = 20000
    "shippingFee": 25000,
    "shippingDiscount": 25000, // min(25000, 30000) = 25000
    "totalAmount": 180000      // 200000 + 25000 - 20000 - 25000
  }
}
```

### Scenario 5: Đơn hàng với voucher giảm cố định
```json
{
  "orderRequest": {
    "userId": 1,
    "subtotal": 200000,
    "shippingFee": 25000,
    "voucherIds": [101, 201]
  },
  "vouchers": [
    {
      "id": 101,
      "type": "PERCENTAGE", 
      "discountPercentage": 10,
      "maxDiscountValue": 50000,
      "minOrderValue": 100000
    },
    {
      "id": 201,
      "type": "FREE_SHIPPING",
      "maxDiscountValue": 30000
    }
  ],
  "calculation": {
    "subtotal": 200000,
    "productDiscount": 20000,  // min(200000*10%, 50000) = 20000
    "shippingFee": 25000,
    "shippingDiscount": 25000, // min(25000, 30000) = 25000
    "totalAmount": 180000      // 200000 + 25000 - 20000 - 25000
  }
}
```

### Scenario 2: Đơn hàng với voucher giảm cố định
```json
{
  "orderRequest": {
    "userId": 1, 
    "subtotal": 50000,
    "shippingFee": 15000,
    "voucherIds": [102]
  },
  "vouchers": [
    {
      "id": 102,
      "type": "FIXED_AMOUNT",
      "discountAmount": 30000,
      "minOrderValue": 40000
    }
  ],
  "calculation": {
    "subtotal": 50000,
    "productDiscount": 30000,  // min(30000, 50000) = 30000
    "shippingFee": 15000,
    "shippingDiscount": 0,
    "totalAmount": 35000       // 50000 + 15000 - 30000
  }
}
```

### Scenario 3: Validation Error - Quá số lượng voucher
```json
{
  "orderRequest": {
    "voucherIds": [101, 102, 103]  // 3 voucher
  },
  "error": {
    "code": "VOUCHER_LIMIT_EXCEEDED",
    "message": "Chỉ được áp dụng tối đa 2 voucher trên 1 đơn hàng"
  }
}
```
