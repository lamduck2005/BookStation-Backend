# API Chuyển Trạng Thái Đơn Hàng - Hướng Dẫn Đầy Đủ

## Tổn### 3. Chuyển Trạng Thái Đơn Hàng Quan Hệ Thống

Hệ thống chuyển trạng thái đơn hàng BookStation được thiết kế để xử lý đầy đủ các luồng nghiệp vụ thực tế, bao gồm:
- Luồng đặt hàng và giao hàng bình thường
- Xử lý giao hàng thất bại và giao lại
- Quy trình hoàn trả hàng và hoàn tiền
- Quản lý tồn kho khi có thay đổi trạng thái
- **Hiển thị trạng thái tiếng Việt và trạng thái có thể chuyển trực tiếp trong response**

## ⭐ Điểm Mới: Tích Hợp Trạng Thái Có Thể Chuyển

Thay vì tạo API riêng để lấy trạng thái có thể chuyển (gây overload), thông tin này được tích hợp trực tiếp vào `OrderResponse`:

### OrderResponse Structure
```json
{
  "id": 123,
  "code": "ORD-2025-001",
  "orderStatus": "SHIPPED",
  "orderStatusDisplay": "Đang giao hàng",
  "availableTransitions": [
    {
      "targetStatus": "DELIVERED",
      "displayName": "Đã giao hàng thành công",
      "actionDescription": "Đánh dấu giao hàng thành công",
      "requiresConfirmation": false,
      "businessImpactNote": null
    },
    {
      "targetStatus": "DELIVERY_FAILED",
      "displayName": "Giao hàng thất bại",
      "actionDescription": "Đánh dấu giao hàng thất bại",
      "requiresConfirmation": true,
      "businessImpactNote": "Đơn hàng có thể được giao lại hoặc trả về kho"
    }
  ]
  // ... other fields
}
```

**Lợi ích:**
- ✅ Không cần API riêng → Giảm số lượng request
- ✅ Dữ liệu nhất quán trong cùng 1 response  
- ✅ Frontend chỉ cần call API list/detail đơn hàng
- ✅ Tự động hiển thị tiếng Việt

## Luồng Trạng Thái Chi Tiết

### 1. Luồng Đơn Hàng Bình Thường
```
PENDING → CONFIRMED → SHIPPED → DELIVERED
```

### 2. Luồng Hủy Đơn Hàng
```
PENDING → CANCELED
CONFIRMED → CANCELED
```

### 3. Luồng Giao Hàng Thất Bại ⭐ MỚI
```
SHIPPED → DELIVERY_FAILED → REDELIVERING → DELIVERED
                          ↘ RETURNING_TO_WAREHOUSE → GOODS_RETURNED_TO_WAREHOUSE → REFUNDED
```

### 4. Luồng Hoàn Trả Hàng
```
DELIVERED → REFUND_REQUESTED → REFUNDING → GOODS_RECEIVED_FROM_CUSTOMER → GOODS_RETURNED_TO_WAREHOUSE → REFUNDED
```

### 5. Luồng Hoàn Tiền Một Phần
```
DELIVERED → PARTIALLY_REFUNDED → REFUNDING → ... → REFUNDED
```

## API Endpoints

### 1. Lấy Danh Sách Đơn Hàng (Bao Gồm Trạng Thái Có Thể Chuyển)

**GET** `/api/orders`

**Mô tả:** API chính để lấy danh sách đơn hàng với pagination. Mỗi đơn hàng đã bao gồm thông tin trạng thái có thể chuyển.

**Parameters:**
- `page`: Số trang (default: 0)
- `size`: Kích thước trang (default: 5) 
- `code`: Mã đơn hàng (optional)
- `userId`: ID khách hàng (optional)
- `orderStatus`: Trạng thái đơn hàng (optional)
- `orderType`: Loại đơn hàng (optional)

**Response:**
```json
{
  "status": 200,
  "message": "Thành công",
  "data": {
    "content": [
      {
        "id": 123,
        "code": "ORD-2025-001",
        "orderStatus": "SHIPPED",
        "orderStatusDisplay": "Đang giao hàng",
        "availableTransitions": [
          {
            "targetStatus": "DELIVERED",
            "displayName": "Đã giao hàng thành công",
            "actionDescription": "Đánh dấu giao hàng thành công",
            "requiresConfirmation": false,
            "businessImpactNote": null
          },
          {
            "targetStatus": "DELIVERY_FAILED", 
            "displayName": "Giao hàng thất bại",
            "actionDescription": "Đánh dấu giao hàng thất bại",
            "requiresConfirmation": true,
            "businessImpactNote": "Đơn hàng có thể được giao lại hoặc trả về kho"
          }
        ]
        // ... other fields
      }
    ],
    "totalElements": 100,
    "totalPages": 20
  }
}
```

### 2. Lấy Chi Tiết Đơn Hàng

**GET** `/api/orders/{id}`

**Mô tả:** Lấy chi tiết đơn hàng theo ID, bao gồm thông tin trạng thái có thể chuyển.

**Response:** Tương tự như trên nhưng với đầy đủ thông tin chi tiết đơn hàng.

**POST** `/api/orders/{orderId}/status-transition`

**Request Body:**
```json
{
  "currentStatus": "SHIPPED",
  "newStatus": "DELIVERY_FAILED",
  "performedBy": 1,
  "reason": "Khách hàng không có mặt tại địa chỉ giao hàng",
  "notes": "Đã gọi điện 3 lần nhưng không liên lạc được"
}
```

**Response:**
```json
{
  "status": 200,
  "message": "Chuyển trạng thái thành công",
  "data": {
    "orderId": 123,
    "oldStatus": "SHIPPED",
    "newStatus": "DELIVERY_FAILED",
    "transitionDescription": "Chuyển từ Đang giao hàng sang Giao hàng thất bại",
    "performedBy": 1,
    "performedAt": 1722412800000,
    "businessImpact": {
      "stockChanged": false,
      "pointsAffected": false,
      "vouchersAffected": false,
      "notificationSent": true,
      "nextRecommendedActions": [
        "Liên hệ khách hàng để xác nhận địa chỉ",
        "Lên lịch giao hàng lại",
        "Hoặc trả hàng về kho nếu khách không nhận"
      ]
    }
  }
}
```

## Chi Tiết Luồng Nghiệp Vụ

### Luồng Giao Hàng Thất Bại

**Khi nào sử dụng:** Shipper không thể giao hàng thành công (khách không có mặt, từ chối nhận hàng, địa chỉ sai...)

**Các bước xử lý:**

1. **SHIPPED → DELIVERY_FAILED**
   - Shipper báo cáo giao hàng thất bại
   - Lý do: khách không có mặt, từ chối nhận hàng, địa chỉ sai...
   
2. **DELIVERY_FAILED → REDELIVERING**
   - Quyết định giao lại
   - Liên hệ khách hàng xác nhận địa chỉ/thời gian
   - Lên lịch giao hàng lần 2
   
3. **DELIVERY_FAILED → RETURNING_TO_WAREHOUSE**
   - Khách hàng không muốn nhận hàng nữa
   - Hoặc giao thất bại nhiều lần
   - Bắt đầu trả hàng về kho

4. **RETURNING_TO_WAREHOUSE → GOODS_RETURNED_TO_WAREHOUSE**
   - Hàng đã về đến kho
   - Tự động cộng lại số lượng tồn kho
   - Nếu có flash sale, cộng lại số lượng flash sale

5. **GOODS_RETURNED_TO_WAREHOUSE → REFUNDED** (nếu đã thanh toán online)
   - Hoàn tiền cho khách hàng
   - Không áp dụng cho đơn COD

### Xử Lý Thanh Toán

**Đơn hàng COD (Cash on Delivery):**
- Khi DELIVERY_FAILED → RETURNING_TO_WAREHOUSE: Không cần hoàn tiền
- Khách chưa thanh toán → chỉ cần trả hàng về kho

**Đơn hàng đã thanh toán online:**
- Khi DELIVERY_FAILED → RETURNING_TO_WAREHOUSE: Cần hoàn tiền
- GOODS_RETURNED_TO_WAREHOUSE → REFUNDED: Hoàn tiền về tài khoản/ví

### Tác Động Tồn Kho

**Khi hàng về kho (GOODS_RETURNED_TO_WAREHOUSE):**
1. Cộng lại số lượng `stock` cho từng sách
2. Nếu có sách flash sale, cộng lại `flashSaleQuantity`
3. Trừ `soldCount` của sách (nếu đã cộng trước đó)
4. Cập nhật trạng thái available của sách nếu cần

## Validation Rules

### 1. Luồng Chuyển Trạng Thái Hợp Lệ

| Từ Trạng Thái | Có Thể Chuyển Sang |
|---|---|
| PENDING | CONFIRMED, CANCELED |
| CONFIRMED | SHIPPED, CANCELED |
| SHIPPED | DELIVERED, DELIVERY_FAILED |
| DELIVERED | REFUND_REQUESTED |
| DELIVERY_FAILED | REDELIVERING, RETURNING_TO_WAREHOUSE |
| REDELIVERING | DELIVERED, RETURNING_TO_WAREHOUSE |
| RETURNING_TO_WAREHOUSE | GOODS_RETURNED_TO_WAREHOUSE |
| CANCELED | REFUNDING (nếu đã thanh toán) |
| REFUND_REQUESTED | REFUNDING, DELIVERED (từ chối) |
| REFUNDING | GOODS_RECEIVED_FROM_CUSTOMER |
| GOODS_RECEIVED_FROM_CUSTOMER | GOODS_RETURNED_TO_WAREHOUSE |
| GOODS_RETURNED_TO_WAREHOUSE | REFUNDED |
| PARTIALLY_REFUNDED | REFUNDING, REFUNDED |
| REFUNDED | (Trạng thái cuối) |

### 2. Business Rules

1. **Không thể chuyển ngược về trạng thái trước đó** (trừ trường hợp đặc biệt như từ chối hoàn trả)
2. **CANCELED và REFUNDED là trạng thái cuối** - không thể chuyển sang trạng thái khác
3. **Giao hàng thất bại bắt buộc phải có lý do**
4. **Hoàn tiền chỉ áp dụng cho đơn đã thanh toán online**

## Error Handling

### Các Lỗi Thường Gặp

1. **400 - Bad Request**
   ```json
   {
     "status": 400,
     "message": "Không thể chuyển từ DELIVERED sang PENDING",
     "data": null
   }
   ```

2. **404 - Not Found**
   ```json
   {
     "status": 404,
     "message": "Không tìm thấy đơn hàng với ID: 999",
     "data": null
   }
   ```

3. **400 - Business Rule Violation**
   ```json
   {
     "status": 400,
     "message": "Đơn hàng đã được giao thành công, không thể chuyển sang trạng thái giao hàng thất bại",
     "data": null
   }
   ```

## Notification & Logging

### 1. Notifications
- Gửi thông báo cho khách hàng khi trạng thái thay đổi
- Email/SMS thông báo giao hàng thất bại
- Thông báo hoàn tiền thành công

### 2. Activity Logs
- Ghi log mọi thay đổi trạng thái
- Lưu thông tin người thực hiện
- Ghi lý do và ghi chú (nếu có)

## Testing Scenarios

### 1. Test Case: Giao Hàng Thất Bại và Giao Lại Thành Công
```
1. Tạo đơn hàng: PENDING
2. Xác nhận: CONFIRMED
3. Bắt đầu giao: SHIPPED
4. Giao thất bại: DELIVERY_FAILED (lý do: khách không có mặt)
5. Giao lại: REDELIVERING
6. Giao thành công: DELIVERED
```

### 2. Test Case: Giao Hàng Thất Bại và Trả Về Kho
```
1. Tạo đơn hàng online (đã thanh toán): PENDING
2. Xác nhận: CONFIRMED  
3. Bắt đầu giao: SHIPPED
4. Giao thất bại: DELIVERY_FAILED (lý do: khách từ chối nhận)
5. Trả về kho: RETURNING_TO_WAREHOUSE
6. Hàng đã về kho: GOODS_RETURNED_TO_WAREHOUSE (tự động cộng tồn kho)
7. Hoàn tiền: REFUNDED (hoàn tiền về tài khoản khách)
```

### 3. Test Case: Validation Error
```
1. Đơn hàng đang ở trạng thái: DELIVERED
2. Cố gắng chuyển sang: SHIPPED
3. Kết quả: Error 400 - "Không thể chuyển từ DELIVERED sang SHIPPED"
```

## Frontend Integration

### 1. Hiển Thị Dropdown Trạng Thái (Cách Mới)
```javascript
// Lấy danh sách đơn hàng (đã bao gồm trạng thái có thể chuyển)
const response = await fetch('/api/orders?page=0&size=10');
const data = await response.json();

// Render dropdown cho từng đơn hàng
data.data.content.forEach(order => {
  const statusOptions = order.availableTransitions.map(transition => ({
    value: transition.targetStatus,
    label: transition.displayName,
    description: transition.actionDescription,
    requiresConfirmation: transition.requiresConfirmation,
    businessImpact: transition.businessImpactNote
  }));
  
  // Render dropdown với statusOptions
  renderStatusDropdown(order.id, statusOptions);
});
```

### 2. Xử Lý Chuyển Trạng Thái (Không Đổi)
```javascript
const changeStatus = async (orderId, newStatus, reason) => {
  const response = await fetch(`/api/orders/${orderId}/status-transition`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      newStatus: newStatus,
      performedBy: currentUserId,
      reason: reason
    })
  });
  
  if (response.ok) {
    // Reload order data
    await loadOrdersList();
    showSuccessMessage('Chuyển trạng thái thành công!');
  } else {
    const error = await response.json();
    showErrorMessage(error.message);
  }
};
```

### 3. Hiển Thị Trạng Thái Tiếng Việt
```javascript
// Không cần mapping thủ công nữa
// orderStatusDisplay đã là tiếng Việt từ API
const displayStatus = order.orderStatusDisplay; // "Đang giao hàng"
const enumValue = order.orderStatus; // "SHIPPED"
```

## Kết Luận

Hệ thống chuyển trạng thái đơn hàng đã được cải tiến để xử lý đầy đủ các tình huống thực tế:

### ✅ Tối Ưu Hóa Performance
- **Không có API riêng cho trạng thái có thể chuyển**
- **Tích hợp thông tin vào response chính**
- **Giảm số lượng request từ frontend**

### ✅ Hiển Thị Tiếng Việt Hoàn Toàn  
- **Tất cả trạng thái hiển thị tiếng Việt**
- **Thông báo lỗi bằng tiếng Việt**
- **Mô tả hành động bằng tiếng Việt**

### ✅ Luồng Nghiệp Vụ Đầy Đủ
- **Luồng giao hàng bình thường**
- **Xử lý giao hàng thất bại và giao lại**  
- **Trả hàng về kho khi khách không nhận**
- **Hoàn tiền cho đơn đã thanh toán online**
- **Quản lý tồn kho chính xác**

### ✅ API Chuẩn REST và Dễ Sử Dụng
- **Response structure nhất quán**
- **Error handling chuẩn HTTP status code**
- **Frontend integration đơn giản**

### ✅ Các Enum Status Mới
- `REDELIVERING` - Đang giao lại
- `RETURNING_TO_WAREHOUSE` - Đang trả về kho

Hệ thống đảm bảo tính nhất quán dữ liệu, performance tốt và tuân thủ các quy tắc nghiệp vụ thực tế.
