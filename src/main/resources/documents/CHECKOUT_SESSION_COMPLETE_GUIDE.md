# CHECKOUT SESSION SYSTEM - COMPLETE DOCUMENTATION

## 📋 TỔNG QUAN HỆ THỐNG

Checkout Session là một **bảng trung gian** giữa Cart và Order, được thiết kế để:
- ✅ **Lưu trạng thái checkout** khi user bị gián đoạn
- ✅ **Hỗ trợ multiple concurrent sessions** cho cùng một user
- ✅ **Tự động hết hạn** để tránh lưu trữ dữ liệu cũ
- ✅ **Tích hợp flash sale, voucher, shipping** phức tạp
- ✅ **Chuẩn bị dữ liệu sẵn sàng** cho việc tạo order

---

## 🏗️ KIẾN TRÚC HỆ THỐNG

### Database Schema
```sql
CREATE TABLE checkout_sessions (
    id INTEGER PRIMARY KEY AUTO_INCREMENT,
    user_id INTEGER NOT NULL,
    address_id INTEGER,
    checkout_items CLOB NOT NULL,        -- JSON array of checkout items
    selected_voucher_ids CLOB,           -- JSON array of voucher IDs
    subtotal DECIMAL(15,2),              -- Tổng tiền sản phẩm
    shipping_fee DECIMAL(15,2),          -- Phí vận chuyển
    total_discount DECIMAL(15,2),        -- Tổng discount (voucher)
    total_amount DECIMAL(15,2),          -- Tổng tiền cuối cùng
    shipping_method VARCHAR(50),         -- Phương thức vận chuyển
    payment_method VARCHAR(50),          -- Phương thức thanh toán
    expected_delivery_time BIGINT,       -- Thời gian dự kiến giao hàng
    notes TEXT,                          -- Ghi chú
    status TINYINT DEFAULT 1,            -- 1: Active, 2: Completed, 0: Expired
    expires_at BIGINT NOT NULL,          -- Timestamp hết hạn
    created_at BIGINT NOT NULL,
    updated_at BIGINT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (address_id) REFERENCES addresses(id)
);
```

### JSON Field Structures

#### checkout_items (CLOB)
```json
[
  {
    "bookId": 123,
    "quantity": 2,
    "isFlashSale": true,
    "flashSaleItemId": 456,
    "unitPrice": 150000.00,
    "totalPrice": 300000.00
  },
  {
    "bookId": 789,
    "quantity": 1,
    "isFlashSale": false,
    "flashSaleItemId": null,
    "unitPrice": 200000.00,
    "totalPrice": 200000.00
  }
]
```

#### selected_voucher_ids (CLOB)
```json
[101, 202, 303]
```

---

## 🎯 CÁC USE CASE THỰC TẾ

### 1. **Tạo Checkout Session từ Cart**
```
Frontend Flow:
1. User click "Checkout" từ cart page
2. Gọi POST /api/checkout-sessions/from-cart?userId=123
3. Backend tự động:
   - Lấy tất cả cart items của user
   - Loại bỏ out-of-stock items
   - Tự động detect flash sale items
   - Tính giá cho từng item
   - Tạo checkout session
4. Frontend redirect đến checkout page với sessionId
```

**API Call:**
```bash
POST /api/checkout-sessions/from-cart?userId=123
Response: {
  "status": 201,
  "message": "Tạo checkout session từ giỏ hàng thành công",
  "data": {
    "id": 5001,
    "subtotal": 500000.00,
    "checkoutItems": [...],
    "expiresAt": 1735678800000
  }
}
```

### 2. **Frontend Lưu Thông Tin Checkout**
```
Frontend Flow:
1. User chọn địa chỉ giao hàng → PUT /api/checkout-sessions/{id}
2. User chọn voucher → PUT /api/checkout-sessions/{id}
3. User chọn phương thức thanh toán → PUT /api/checkout-sessions/{id}
4. Mỗi lần update, backend sẽ:
   - Recalculate pricing
   - Validate voucher eligibility
   - Update expires_at
```

**API Call:**
```bash
PUT /api/checkout-sessions/5001
{
  "userId": 123,
  "addressId": 456,
  "selectedVoucherIds": [101, 202],
  "shippingFee": 30000.00,
  "paymentMethod": "BANK_TRANSFER",
  "checkoutItems": [...]
}
```

### 3. **Khôi Phục Checkout sau Gián Đoạn**
```
Frontend Flow:
1. User quay lại website sau khi tắt browser
2. Gọi GET /api/checkout-sessions/latest?userId=123
3. Nếu có session active:
   - Redirect đến checkout page
   - Load lại toàn bộ thông tin đã chọn
4. Nếu session expired:
   - Thông báo "Session đã hết hạn"
   - Redirect về cart
```

### 4. **Validate Trước Khi Đặt Hàng**
```
Frontend Flow:
1. User click "Đặt hàng"
2. Gọi POST /api/checkout-sessions/{id}/validate?userId=123
3. Backend kiểm tra:
   - Session còn active không?
   - Stock availability
   - Flash sale còn hiệu lực?
   - Voucher còn valid?
4. Nếu có lỗi: Hiển thị thông báo, không cho đặt hàng
5. Nếu OK: Proceed to create order
```

### 5. **Tạo Order từ Checkout Session**
```
Frontend Flow:
1. Sau khi validate OK
2. Gọi POST /api/checkout-sessions/{id}/create-order?userId=123
3. Backend:
   - Convert checkout session → OrderRequest
   - Gọi OrderService.create() với toàn bộ logic business
   - Handle flash sale stock reduction
   - Apply vouchers với full calculation
   - Mark session as completed
4. Response: orderId để redirect đến order success page
```

---

## 🔄 LUỒNG TÍCH HOP ĐẦY ĐỦ

### A. Luồng Checkout Hoàn Chỉnh
```mermaid
graph TD
    A[Cart Page] --> B[Click Checkout]
    B --> C[POST /from-cart]
    C --> D[Checkout Page]
    D --> E[Chọn Address]
    E --> F[PUT /sessions/{id}]
    F --> G[Chọn Shipping Method]
    G --> H[PUT /sessions/{id}]
    H --> I[Chọn Vouchers]
    I --> J[PUT /sessions/{id}]
    J --> K[Click Đặt Hàng]
    K --> L[POST /validate]
    L --> M{Valid?}
    M -->|No| N[Show Errors]
    M -->|Yes| O[POST /create-order]
    O --> P[Order Success Page]
```

### B. Luồng Pricing Calculation
```
1. calculateSessionPricing():
   - Tính subtotal từ checkout items
   - Apply voucher discounts (nếu có)
   - Cộng shipping fee
   - Trừ total discounts
   - Đảm bảo amount >= 0

2. Voucher Integration:
   - Gọi VoucherCalculationService
   - Validate voucher eligibility
   - Calculate product & shipping discounts
   - Store discount details
```

---

## 📡 API REFERENCE HOÀN CHỈNH

### 1. **Tạo Session từ Cart**
```http
POST /api/checkout-sessions/from-cart?userId={id}
Response: CheckoutSessionResponse
```

### 2. **Tạo Session Custom**
```http
POST /api/checkout-sessions
Body: CheckoutSessionRequest
Response: CheckoutSessionResponse
```

### 3. **Update Session**
```http
PUT /api/checkout-sessions/{sessionId}
Body: CheckoutSessionRequest
Response: CheckoutSessionResponse
```

### 4. **Get Session by ID**
```http
GET /api/checkout-sessions/{sessionId}?userId={id}
Response: CheckoutSessionResponse
```

### 5. **Get Latest Session**
```http
GET /api/checkout-sessions/latest?userId={id}
Response: CheckoutSessionResponse
```

### 6. **Get User Sessions (History)**
```http
GET /api/checkout-sessions/user?userId={id}&page=0&size=10
Response: PaginationResponse<CheckoutSessionResponse>
```

### 7. **Validate Session**
```http
POST /api/checkout-sessions/{sessionId}/validate?userId={id}
Response: CheckoutSessionResponse with validation status
```

### 8. **Create Order**
```http
POST /api/checkout-sessions/{sessionId}/create-order?userId={id}
Response: ApiResponse<Integer> (orderId)
```

### 9. **Recalculate Pricing**
```http
PATCH /api/checkout-sessions/{sessionId}/recalculate?userId={id}
Response: CheckoutSessionResponse
```

### 10. **Extend Expiry**
```http
PATCH /api/checkout-sessions/{sessionId}/extend?userId={id}&additionalMinutes=30
Response: CheckoutSessionResponse
```

### 11. **Mark Completed**
```http
PATCH /api/checkout-sessions/{sessionId}/complete?userId={id}
Response: CheckoutSessionResponse
```

### 12. **Delete Session**
```http
DELETE /api/checkout-sessions/{sessionId}?userId={id}
Response: ApiResponse<String>
```

### 13. **Admin - Get All Sessions**
```http
GET /api/checkout-sessions/admin?page=0&size=10&userId={id}&status={status}&startDate={date}&endDate={date}
Response: PaginationResponse<CheckoutSessionResponse>
```

### 14. **Admin - Cleanup Expired**
```http
POST /api/checkout-sessions/admin/cleanup
Response: ApiResponse<Integer> (số session đã cleanup)
```

---

## 📝 REQUEST/RESPONSE EXAMPLES

### CheckoutSessionRequest
```json
{
  "userId": 123,
  "addressId": 456,
  "checkoutItems": [
    {
      "bookId": 789,
      "quantity": 2,
      "isFlashSale": true,
      "flashSaleItemId": 101,
      "unitPrice": 150000.00,
      "totalPrice": 300000.00
    }
  ],
  "selectedVoucherIds": [201, 202],
  "shippingFee": 30000.00,
  "shippingMethod": "EXPRESS",
  "paymentMethod": "BANK_TRANSFER",
  "expectedDeliveryTime": 1735678800000,
  "notes": "Giao hàng giờ hành chính"
}
```

### CheckoutSessionResponse
```json
{
  "id": 5001,
  "userId": 123,
  "userEmail": "user@example.com",
  "addressInfo": {
    "id": 456,
    "recipientName": "Nguyễn Văn A",
    "phoneNumber": "0901234567",
    "addressDetail": "123 Lê Lợi, Q1, TP.HCM",
    "deliveryTimeText": "Giao trong 2-3 ngày"
  },
  "checkoutItems": [
    {
      "bookId": 789,
      "bookName": "Lập trình Java",
      "bookCode": "JAVA001",
      "bookImageUrl": "https://example.com/book.jpg",
      "quantity": 2,
      "unitPrice": 150000.00,
      "originalPrice": 200000.00,
      "totalPrice": 300000.00,
      "isFlashSale": true,
      "flashSaleItemId": 101,
      "flashSaleInfo": {
        "discountPercentage": 25.00,
        "remainingQuantity": 50,
        "endTime": 1735678800000
      }
    }
  ],
  "selectedVoucherIds": [201, 202],
  "voucherSummary": [
    {
      "voucherId": 201,
      "voucherCode": "NEWUSER20",
      "voucherName": "Giảm 20% cho khách hàng mới",
      "discountAmount": 60000.00,
      "isValid": true,
      "validationMessage": "Voucher hợp lệ"
    }
  ],
  "subtotal": 300000.00,
  "shippingFee": 30000.00,
  "totalDiscount": 60000.00,
  "totalAmount": 270000.00,
  "shippingMethod": "EXPRESS",
  "paymentMethod": "BANK_TRANSFER",
  "expectedDeliveryTime": 1735678800000,
  "notes": "Giao hàng giờ hành chính",
  "status": 1,
  "statusText": "Active",
  "isExpired": false,
  "isActive": true,
  "expiresAt": 1735678800000,
  "createdAt": 1735589200000,
  "updatedAt": 1735589800000
}
```

---

## ⚡ FLASH SALE INTEGRATION

### Flash Sale Detection Logic
```java
// Trong createSessionFromCart()
if (cartItem.isFlashSale()) {
    item.setIsFlashSale(true);
    item.setFlashSaleItemId(cartItem.getFlashSaleItemId());
    item.setUnitPrice(cartItem.getFlashSalePrice()); // Giá flash sale
} else {
    item.setIsFlashSale(false);
    item.setUnitPrice(cartItem.getRegularPrice()); // Giá gốc
}
```

### Flash Sale Validation
```java
// Trong validateSessionItems()
if (item.getIsFlashSale() && item.getFlashSaleItemId() != null) {
    FlashSaleItem flashSaleItem = flashSaleItemRepository.findById(item.getFlashSaleItemId());
    
    // Check expiry
    if (flashSaleItem.getFlashSale().getEndTime() < System.currentTimeMillis()) {
        errors.add("Flash sale đã hết hạn");
    }
    
    // Check stock
    if (flashSaleItem.getStockQuantity() < item.getQuantity()) {
        errors.add("Flash sale stock không đủ");
    }
}
```

---

## 🎫 VOUCHER INTEGRATION

### Voucher Application Process
```java
// Trong calculateSessionPricing()
if (voucherIds != null && !voucherIds.isEmpty()) {
    // Tạo temporary order để tính voucher
    Order tempOrder = new Order();
    tempOrder.setSubtotal(subtotal);
    tempOrder.setShippingFee(shippingFee);
    
    // Gọi VoucherCalculationService
    VoucherCalculationResult result = voucherCalculationService
        .calculateVoucherDiscount(tempOrder, voucherIds, userId);
    
    // Apply discounts
    totalDiscount = result.getTotalProductDiscount() 
                  + result.getTotalShippingDiscount();
}
```

### Voucher Validation
```java
// Trong validateSessionVouchers()
for (Integer voucherId : voucherIds) {
    Voucher voucher = voucherRepository.findById(voucherId);
    
    // Check validity
    if (voucher.getStatus() != 1) {
        errors.add("Voucher đã bị vô hiệu hóa");
    }
    
    // Check expiry
    long currentTime = System.currentTimeMillis();
    if (currentTime < voucher.getStartTime() || currentTime > voucher.getEndTime()) {
        errors.add("Voucher đã hết hạn");
    }
    
    // Check usage limits
    if (!voucherCalculationService.canUserUseVoucher(userId, voucherId)) {
        errors.add("Đã sử dụng hết lượt voucher");
    }
}
```

---

## 🔒 SECURITY & VALIDATION

### User Access Control
```java
// Tất cả methods đều có security check
Optional<CheckoutSession> sessionOpt = checkoutSessionRepository
    .findByIdAndUserId(sessionId, userId);

if (sessionOpt.isEmpty()) {
    return new ApiResponse<>(404, "Không tìm thấy checkout session", null);
}
```

### Session Expiry Management
```java
// Tự động cleanup sessions hết hạn
@Scheduled(fixedRate = 3600000) // Chạy mỗi giờ
public void autoCleanup() {
    int cleaned = checkoutSessionService.cleanupExpiredSessions();
    log.info("Auto cleaned {} expired sessions", cleaned);
}
```

### Data Validation
```java
private void validateCheckoutSessionRequest(CheckoutSessionRequest request) {
    if (request.getCheckoutItems() == null || request.getCheckoutItems().isEmpty()) {
        throw new IllegalArgumentException("Danh sách sản phẩm không được để trống");
    }
    
    for (CheckoutItemRequest item : request.getCheckoutItems()) {
        if (item.getBookId() == null || item.getQuantity() == null || item.getQuantity() <= 0) {
            throw new IllegalArgumentException("Thông tin sản phẩm không hợp lệ");
        }
    }
}
```

---

## 🚀 PERFORMANCE OPTIMIZATIONS

### JSON Field Indexing
```sql
-- MySQL 8.0+ JSON indexing
ALTER TABLE checkout_sessions 
ADD COLUMN user_id_virtual INTEGER AS (JSON_UNQUOTE(JSON_EXTRACT(checkout_items, '$[0].userId'))) VIRTUAL,
ADD INDEX idx_user_checkout (user_id, status, expires_at);
```

### Batch Operations
```java
// Cleanup expired sessions in batches
public int cleanupExpiredSessions() {
    long currentTime = System.currentTimeMillis();
    
    // Mark expired
    int markedExpired = checkoutSessionRepository.markExpiredSessions(currentTime);
    
    // Delete old expired (after 7 days)
    long sevenDaysAgo = currentTime - (7 * 24 * 60 * 60 * 1000L);
    int deletedOld = checkoutSessionRepository.deleteOldExpiredSessions(sevenDaysAgo);
    
    return markedExpired + deletedOld;
}
```

---

## 🎯 FRONTEND INTEGRATION EXAMPLES

### React Checkout Flow
```javascript
// 1. Tạo session từ cart
const createCheckoutSession = async (userId) => {
  const response = await fetch(`/api/checkout-sessions/from-cart?userId=${userId}`, {
    method: 'POST'
  });
  const data = await response.json();
  return data.data; // CheckoutSessionResponse
};

// 2. Update session khi user thay đổi
const updateSession = async (sessionId, updateData) => {
  const response = await fetch(`/api/checkout-sessions/${sessionId}`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(updateData)
  });
  return response.json();
};

// 3. Validate trước khi đặt hàng
const validateSession = async (sessionId, userId) => {
  const response = await fetch(`/api/checkout-sessions/${sessionId}/validate?userId=${userId}`, {
    method: 'POST'
  });
  const result = await response.json();
  return result.status === 200;
};

// 4. Tạo order
const createOrder = async (sessionId, userId) => {
  const response = await fetch(`/api/checkout-sessions/${sessionId}/create-order?userId=${userId}`, {
    method: 'POST'
  });
  const result = await response.json();
  return result.data; // orderId
};
```

### Vue.js Checkout Store
```javascript
// Vuex/Pinia store for checkout
export const useCheckoutStore = defineStore('checkout', {
  state: () => ({
    currentSession: null,
    isLoading: false,
    errors: []
  }),
  
  actions: {
    async initFromCart(userId) {
      this.isLoading = true;
      try {
        const session = await createCheckoutSession(userId);
        this.currentSession = session;
      } catch (error) {
        this.errors.push(error.message);
      } finally {
        this.isLoading = false;
      }
    },
    
    async updateAddress(addressId) {
      if (!this.currentSession) return;
      
      const updateData = {
        ...this.currentSession,
        addressId
      };
      
      const updated = await updateSession(this.currentSession.id, updateData);
      this.currentSession = updated.data;
    },
    
    async applyVoucher(voucherId) {
      if (!this.currentSession) return;
      
      const currentVouchers = this.currentSession.selectedVoucherIds || [];
      if (!currentVouchers.includes(voucherId)) {
        currentVouchers.push(voucherId);
      }
      
      const updateData = {
        ...this.currentSession,
        selectedVoucherIds: currentVouchers
      };
      
      const updated = await updateSession(this.currentSession.id, updateData);
      this.currentSession = updated.data;
    }
  }
});
```

---

## 🧪 TESTING SCENARIOS

### Unit Test Cases
```java
@Test
public void testCreateCheckoutSessionFromCart() {
    // Given
    Integer userId = 123;
    List<CartItemResponse> cartItems = createMockCartItems();
    when(cartItemService.getCartItemsByUserId(userId)).thenReturn(cartItems);
    
    // When
    ApiResponse<CheckoutSessionResponse> response = checkoutSessionService.createSessionFromCart(userId);
    
    // Then
    assertEquals(201, response.getStatus());
    assertNotNull(response.getData());
    assertEquals(2, response.getData().getCheckoutItems().size());
}

@Test
public void testSessionExpiry() {
    // Given
    CheckoutSession expiredSession = createExpiredSession();
    
    // When
    boolean isActive = expiredSession.isActive();
    
    // Then
    assertFalse(isActive);
}

@Test
public void testVoucherApplication() {
    // Given
    CheckoutSession session = createSessionWithItems();
    List<Integer> voucherIds = Arrays.asList(101, 102);
    
    // When
    // Test voucher calculation logic
    
    // Then
    // Verify discount calculations
}
```

---

## 📊 MONITORING & ANALYTICS

### Key Metrics to Track
1. **Session Creation Rate**: Số session tạo/giờ
2. **Session Completion Rate**: % session thành công tạo order
3. **Session Abandonment Rate**: % session bị abandon
4. **Average Session Duration**: Thời gian trung bình một session
5. **Flash Sale Session Performance**: Conversion rate cho flash sale items
6. **Voucher Usage in Sessions**: % session có apply voucher

### Logging Examples
```java
// Trong CheckoutSessionServiceImpl
log.info("Session created: sessionId={}, userId={}, itemCount={}, totalAmount={}", 
         session.getId(), session.getUser().getId(), 
         session.getCheckoutItems().size(), session.getTotalAmount());

log.warn("Session validation failed: sessionId={}, userId={}, errors={}", 
         sessionId, userId, String.join(", ", validationErrors));

log.info("Order created from session: sessionId={}, orderId={}, totalAmount={}", 
         sessionId, orderId, session.getTotalAmount());
```

---

## 🔧 TROUBLESHOOTING GUIDE

### Common Issues & Solutions

1. **Session Expired Too Quickly**
   ```java
   // Solution: Extend default expiry time
   session.setExpiresAt(System.currentTimeMillis() + (60 * 60 * 1000L)); // 1 hour
   ```

2. **Voucher Calculation Errors**
   ```java
   // Solution: Add try-catch around voucher calculations
   try {
       VoucherCalculationResult result = voucherCalculationService.calculateVoucherDiscount(...);
   } catch (Exception e) {
       log.warn("Voucher calculation failed, continuing without vouchers: {}", e.getMessage());
       // Continue without voucher discounts
   }
   ```

3. **Flash Sale Stock Issues**
   ```java
   // Solution: Double-check stock in validateSessionItems()
   if (flashSaleItem.getStockQuantity() < item.getQuantity()) {
       errors.add("Flash sale stock not available: requested=" + item.getQuantity() + 
                  ", available=" + flashSaleItem.getStockQuantity());
   }
   ```

4. **JSON Parsing Errors**
   ```java
   // Solution: Add null checks and error handling
   private List<CheckoutItemRequest> parseCheckoutItems(String json) {
       try {
           if (json == null || json.trim().isEmpty()) {
               return new ArrayList<>();
           }
           return objectMapper.readValue(json, new TypeReference<List<CheckoutItemRequest>>() {});
       } catch (JsonProcessingException e) {
           log.error("Error parsing checkout items JSON: {}", json, e);
           return null;
       }
   }
   ```

---

## 🎉 TỔNG KẾT

Checkout Session System đã được implement **hoàn chỉnh** với:

✅ **Database**: Entity, Repository với custom queries  
✅ **Business Logic**: Service với 15+ methods  
✅ **API Layer**: Controller với 14 endpoints  
✅ **Data Transfer**: Request/Response DTOs với validation  
✅ **Mapping**: Entity ↔ DTO mappers với JSON handling  
✅ **Integration**: Flash Sale, Voucher, Cart, Order systems  
✅ **Security**: User access control, validation  
✅ **Performance**: Batch operations, cleanup jobs  
✅ **Documentation**: Complete API docs với examples  

Hệ thống sẵn sàng để **frontend tích hợp** và **production deployment**! 🚀
