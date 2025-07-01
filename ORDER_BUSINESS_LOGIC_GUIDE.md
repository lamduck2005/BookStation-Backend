# Hướng Dẫn Nghiệp Vụ Order System - Flash Sale & Voucher

## 📋 Tổng Quan
Tài liệu này giải thích chi tiết nghiệp vụ quản lý đơn hàng phức tạp trong hệ thống e-commerce hiện đại, bao gồm Flash Sale, Voucher Stacking, và Order Calculation theo mô hình Shopee/Lazada.

## 🎯 Mục Tiêu Hệ Thống
- **Tự động hóa**: Hệ thống tự động detect và tính toán giá, giảm giá, phí ship
- **Validation nghiêm ngặt**: Validate đầy đủ các rule về flash sale, voucher, stock
- **Transparency**: Người dùng thấy rõ cách tính toán mỗi khoản phí
- **Flexibility**: Hỗ trợ mix sản phẩm thường + flash sale trong 1 đơn hàng

---

## 🔥 FLASH SALE SYSTEM

### 1. Cơ Chế Hoạt Động

#### a) Flash Sale Detection
```
Khi user thêm sản phẩm vào giỏ hàng:
┌─────────────────────────────────────┐
│ OrderDetail có flashSaleItemId?     │
├─────────────────────────────────────┤
│ YES → Sử dụng FlashSaleItem.discountPrice │
│ NO  → Sử dụng Book.price             │
└─────────────────────────────────────┘
```

#### b) Flash Sale Validation Flow
```
1. ✅ Time Check: startTime ≤ now ≤ endTime
2. ✅ Status Check: FlashSale.status = 1 AND FlashSaleItem.status = 1  
3. ✅ Stock Check: requestedQuantity ≤ stockQuantity
4. ✅ User Limit: requestedQuantity ≤ maxPurchasePerUser (if set)
5. ✅ User History: check previous purchases của user này
```

### 2. Ví Dụ Thực Tế

#### Case 1: Sản phẩm KHÔNG có Flash Sale
```json
{
  "book": {
    "id": 1,
    "name": "Sách Lập Trình Java",
    "price": 100000
  },
  "orderDetail": {
    "bookId": 1,
    "flashSaleItemId": null,  // ← KHÔNG có flash sale
    "quantity": 2
  },
  "calculation": {
    "unitPrice": 100000,      // ← Dùng Book.price
    "lineTotal": 200000       // 100000 × 2
  }
}
```

#### Case 2: Sản phẩm CÓ Flash Sale
```json
{
  "book": {
    "id": 1,
    "name": "Sách Lập Trình Java", 
    "price": 100000           // ← Giá gốc
  },
  "flashSale": {
    "id": 10,
    "name": "Flash Sale Cuối Tuần",
    "startTime": "2025-07-01 00:00:00",
    "endTime": "2025-07-01 23:59:59",
    "status": 1
  },
  "flashSaleItem": {
    "id": 101,
    "bookId": 1,
    "flashSaleId": 10,
    "discountPrice": 70000,   // ← Giá flash sale (đã giảm 30%)
    "stockQuantity": 100,
    "maxPurchasePerUser": 5,
    "status": 1
  },
  "orderDetail": {
    "bookId": 1,
    "flashSaleItemId": 101,   // ← CÓ flash sale
    "quantity": 2
  },
  "calculation": {
    "unitPrice": 70000,       // ← Dùng FlashSaleItem.discountPrice
    "lineTotal": 140000,      // 70000 × 2
    "savings": 60000          // (100000 - 70000) × 2
  }
}
```

### 3. Validation Scenarios

#### ❌ Lỗi Thời Gian
```json
{
  "error": "FLASH_SALE_TIME_INVALID",
  "message": "Flash sale không trong thời gian hiệu lực",
  "details": {
    "currentTime": "2025-07-02 10:00:00",
    "flashSaleEndTime": "2025-07-01 23:59:59"
  }
}
```

#### ❌ Lỗi Hết Hàng
```json
{
  "error": "FLASH_SALE_STOCK_INSUFFICIENT", 
  "message": "Sản phẩm flash sale đã hết hàng",
  "details": {
    "requestedQuantity": 10,
    "availableStock": 5
  }
}
```

#### ❌ Lỗi Vượt Giới Hạn User
```json
{
  "error": "FLASH_SALE_USER_LIMIT_EXCEEDED",
  "message": "Bạn đã mua tối đa 3 sản phẩm này trong flash sale",
  "details": {
    "requestedQuantity": 5,
    "maxPurchasePerUser": 3,
    "userPurchasedQuantity": 1
  }
}
```

---

## 🎫 VOUCHER SYSTEM

### 1. Loại Voucher

| Loại | Mô Tả | Cách Tính |
|------|-------|-----------|
| **PERCENTAGE** | Giảm % trên tổng tiền sản phẩm | `min(subtotal × percentage, maxDiscountValue)` |
| **FIXED_AMOUNT** | Giảm số tiền cố định | `min(discountAmount, subtotal)` |
| **FREE_SHIPPING** | Miễn phí vận chuyển | `min(shippingFee, maxDiscountValue)` |

### 2. Stacking Rules (Quy Tắc Xếp Chồng)

```
┌─────────────────────────────────────┐
│        VOUCHER STACKING RULES       │
├─────────────────────────────────────┤
│ ✅ Tối đa 2 voucher trên 1 đơn hàng  │
│ ✅ Tối đa 1 voucher thường           │
│    (PERCENTAGE hoặc FIXED_AMOUNT)   │
│ ✅ Tối đa 1 voucher FREE_SHIPPING    │
│ ❌ KHÔNG được 2 voucher cùng loại    │
└─────────────────────────────────────┘
```

### 3. Validation Process

#### a) Voucher Validation Flow
```
1. ✅ Quantity Check: ≤ 2 voucher total
2. ✅ Type Check: ≤ 1 regular, ≤ 1 freeship  
3. ✅ Time Check: startTime ≤ now ≤ endTime
4. ✅ Usage Check: usedCount < usageLimit
5. ✅ User Limit: userUsedCount < usageLimitPerUser
6. ✅ Min Order: orderValue ≥ minOrderValue
```

#### b) Ví Dụ Validation

##### ✅ Hợp Lệ: 1 Giảm Giá + 1 Freeship
```json
{
  "vouchers": [
    {
      "id": 101,
      "type": "PERCENTAGE",
      "discountPercentage": 10,
      "maxDiscountValue": 50000
    },
    {
      "id": 201, 
      "type": "FREE_SHIPPING",
      "maxDiscountValue": 30000
    }
  ],
  "validation": "✅ VALID - 1 regular + 1 freeship"
}
```

##### ❌ Không Hợp Lệ: 2 Voucher Cùng Loại
```json
{
  "vouchers": [
    {
      "id": 101,
      "type": "PERCENTAGE",
      "discountPercentage": 10
    },
    {
      "id": 102,
      "type": "FIXED_AMOUNT", 
      "discountAmount": 20000
    }
  ],
  "error": "❌ INVALID - Chỉ được 1 voucher thường (PERCENTAGE hoặc FIXED_AMOUNT)"
}
```

---

## 🧮 ORDER CALCULATION

### 1. Calculation Flow

```
┌─────────────────────────────────────┐
│           ORDER CALCULATION         │
├─────────────────────────────────────┤
│ Step 1: Calculate Subtotal          │
│ Step 2: Apply Product Vouchers      │
│ Step 3: Apply Shipping Vouchers     │
│ Step 4: Calculate Final Total       │
└─────────────────────────────────────┘
```

### 2. Detailed Calculation Logic

#### Step 1: Calculate Subtotal
```javascript
subtotal = 0
for (orderDetail in orderDetails) {
  if (orderDetail.flashSaleItemId != null) {
    // Flash sale item
    price = FlashSaleItem.discountPrice
  } else {
    // Regular item  
    price = Book.price
  }
  subtotal += price * orderDetail.quantity
}
```

#### Step 2: Apply Product Vouchers
```javascript
productDiscount = 0
for (voucher in vouchers) {
  if (voucher.type == "PERCENTAGE") {
    discount = min(subtotal * voucher.discountPercentage / 100, voucher.maxDiscountValue)
  } else if (voucher.type == "FIXED_AMOUNT") {
    discount = min(voucher.discountAmount, subtotal)
  }
  productDiscount += discount
}
```

#### Step 3: Apply Shipping Vouchers
```javascript
shippingDiscount = 0
for (voucher in vouchers) {
  if (voucher.type == "FREE_SHIPPING") {
    discount = min(shippingFee, voucher.maxDiscountValue)
    shippingDiscount += discount
  }
}
```

#### Step 4: Calculate Final Total
```javascript
totalAmount = subtotal + shippingFee - productDiscount - shippingDiscount
```

---

## 📊 SCENARIOS THỰC TẾ

### Scenario 1: Đơn Hàng Chỉ Flash Sale

```json
{
  "description": "User mua 2 sản phẩm flash sale, không dùng voucher",
  "input": {
    "orderDetails": [
      {
        "bookId": 1,
        "flashSaleItemId": 101,
        "quantity": 2
      }
    ],
    "shippingFee": 25000,
    "voucherIds": []
  },
  "data": {
    "book": {
      "id": 1,
      "price": 100000
    },
    "flashSaleItem": {
      "id": 101,
      "discountPrice": 70000
    }
  },
  "calculation": {
    "subtotal": 140000,        // 70000 × 2 (flash sale price)
    "productDiscount": 0,      // Không có voucher
    "shippingFee": 25000,
    "shippingDiscount": 0,     // Không có voucher freeship
    "totalAmount": 165000      // 140000 + 25000
  },
  "savings": {
    "fromFlashSale": 60000,    // (100000 - 70000) × 2
    "fromVoucher": 0,
    "total": 60000
  }
}
```

### Scenario 2: Đơn Hàng Mix (Thường + Flash Sale)

```json
{
  "description": "User mua 1 sản phẩm thường + 2 sản phẩm flash sale",
  "input": {
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
    "shippingFee": 30000,
    "voucherIds": []
  },
  "data": {
    "regularItem": {
      "bookId": 1,
      "price": 150000
    },
    "flashSaleItem": {
      "bookId": 2,
      "originalPrice": 80000,
      "discountPrice": 60000
    }
  },
  "calculation": {
    "subtotal": 270000,        // 150000×1 + 60000×2
    "productDiscount": 0,
    "shippingFee": 30000,
    "shippingDiscount": 0,
    "totalAmount": 300000
  },
  "breakdown": {
    "regularItemTotal": 150000,    // 150000 × 1
    "flashSaleItemTotal": 120000,  // 60000 × 2
    "flashSaleSavings": 40000      // (80000 - 60000) × 2
  }
}
```

### Scenario 3: Flash Sale + Voucher Stacking

```json
{
  "description": "User mua flash sale + dùng voucher giảm 10% + freeship",
  "input": {
    "orderDetails": [
      {
        "bookId": 1,
        "flashSaleItemId": 101,
        "quantity": 3
      }
    ],
    "shippingFee": 35000,
    "voucherIds": [301, 302]
  },
  "data": {
    "flashSaleItem": {
      "originalPrice": 100000,
      "discountPrice": 75000
    },
    "vouchers": [
      {
        "id": 301,
        "type": "PERCENTAGE",
        "discountPercentage": 10,
        "maxDiscountValue": 50000,
        "minOrderValue": 100000
      },
      {
        "id": 302,
        "type": "FREE_SHIPPING",
        "maxDiscountValue": 40000
      }
    ]
  },
  "calculation": {
    "subtotal": 225000,        // 75000 × 3 (flash sale price)
    "productDiscount": 22500,  // min(225000×10%, 50000) = 22500
    "shippingFee": 35000,
    "shippingDiscount": 35000, // min(35000, 40000) = 35000
    "totalAmount": 202500      // 225000 + 35000 - 22500 - 35000
  },
  "savings": {
    "fromFlashSale": 75000,    // (100000 - 75000) × 3
    "fromVoucher": 57500,      // 22500 + 35000
    "total": 132500,
    "originalTotal": 335000,   // 100000×3 + 35000
    "finalTotal": 202500,
    "savingsPercentage": 39.6  // 132500/335000 × 100%
  }
}
```

### Scenario 4: Validation Error Cases

#### a) Voucher Stacking Error
```json
{
  "description": "User cố gắng dùng 2 voucher giảm giá cùng lúc",
  "input": {
    "voucherIds": [101, 102, 103]
  },
  "vouchers": [
    {"id": 101, "type": "PERCENTAGE"},
    {"id": 102, "type": "FIXED_AMOUNT"},
    {"id": 103, "type": "FREE_SHIPPING"}
  ],
  "error": {
    "code": "VOUCHER_STACKING_INVALID",
    "message": "Chỉ được áp dụng tối đa 1 voucher thường (PERCENTAGE hoặc FIXED_AMOUNT)",
    "details": {
      "regularVoucherCount": 2,
      "maxRegularVoucher": 1
    }
  }
}
```

#### b) Flash Sale Stock Error
```json
{
  "description": "User mua vượt quá số lượng flash sale có sẵn",
  "input": {
    "orderDetails": [
      {
        "bookId": 1,
        "flashSaleItemId": 101,
        "quantity": 15
      }
    ]
  },
  "flashSaleItem": {
    "id": 101,
    "stockQuantity": 10
  },
  "error": {
    "code": "FLASH_SALE_STOCK_INSUFFICIENT",
    "message": "Số lượng flash sale không đủ. Có sẵn: 10",
    "details": {
      "requestedQuantity": 15,
      "availableStock": 10
    }
  }
}
```

#### c) Voucher Min Order Error
```json
{
  "description": "User dùng voucher nhưng đơn hàng chưa đủ giá trị tối thiểu",
  "input": {
    "subtotal": 80000,
    "voucherIds": [201]
  },
  "voucher": {
    "id": 201,
    "type": "PERCENTAGE",
    "discountPercentage": 15,
    "minOrderValue": 100000
  },
  "error": {
    "code": "VOUCHER_MIN_ORDER_NOT_MET",
    "message": "Đơn hàng chưa đủ giá trị tối thiểu để áp dụng voucher",
    "details": {
      "currentOrderValue": 80000,
      "requiredMinValue": 100000,
      "voucherCode": "GIAMGIA15"
    }
  }
}
```

---

## 🔍 BUSINESS LOGIC DEEP DIVE

### 1. Price Priority Logic

```
┌─────────────────────────────────────┐
│        PRICE SELECTION LOGIC        │
├─────────────────────────────────────┤
│ IF flashSaleItemId != null:         │
│   ✅ Check flash sale validity      │
│   ✅ Use FlashSaleItem.discountPrice│
│ ELSE:                               │
│   ✅ Use Book.price                 │
└─────────────────────────────────────┘
```

### 2. Voucher Application Order

```
┌─────────────────────────────────────┐
│      VOUCHER APPLICATION ORDER      │
├─────────────────────────────────────┤
│ 1. Validate all vouchers            │
│ 2. Apply PERCENTAGE voucher first    │
│ 3. Apply FIXED_AMOUNT voucher        │
│ 4. Apply FREE_SHIPPING voucher       │
│ 5. Calculate final total             │
└─────────────────────────────────────┘
```

### 3. Stock Management

#### Flash Sale Stock Flow
```
┌─────────────────────────────────────┐
│         STOCK MANAGEMENT            │
├─────────────────────────────────────┤
│ 1. Check FlashSaleItem.stockQuantity│
│ 2. Validate against requested qty   │
│ 3. Reserve stock during order       │
│ 4. Deduct stock on order confirm    │
│ 5. Restore stock on order cancel    │
└─────────────────────────────────────┘
```

### 4. User Purchase Limit Tracking

```sql
-- Kiểm tra user đã mua bao nhiêu sản phẩm flash sale này
SELECT SUM(od.quantity) as purchasedQuantity
FROM orders o
JOIN order_details od ON o.id = od.order_id  
WHERE o.user_id = :userId
  AND od.flash_sale_item_id = :flashSaleItemId
  AND o.order_status NOT IN ('CANCELED', 'REFUNDED')
```

---

## 🎨 UI/UX Considerations

### 1. Hiển Thị Giá Cho User

#### Sản Phẩm Thường
```
┌─────────────────────────────────────┐
│  📚 Sách Lập Trình Java             │
│  💰 100.000₫                        │
│  🛒 Thêm vào giỏ hàng                │
└─────────────────────────────────────┘
```

#### Sản Phẩm Flash Sale
```
┌─────────────────────────────────────┐
│  📚 Sách Lập Trình Java             │
│  🔥 FLASH SALE                      │
│  💰 70.000₫  💸 100.000₫           │
│  🎯 Tiết kiệm 30.000₫ (30%)         │
│  ⏰ Còn 2 giờ 30 phút               │
│  📦 Còn 15 sản phẩm                 │
│  🛒 Thêm vào giỏ hàng                │
└─────────────────────────────────────┘
```

### 2. Hiển Thị Voucher

```
┌─────────────────────────────────────┐
│  🎫 Voucher Khả Dụng                │
│  ├─ Giảm 10% tối đa 50K             │
│  │  Đơn từ 100K                     │
│  │  [Áp dụng]                       │
│  └─ Freeship tối đa 30K             │
│     [Áp dụng]                       │
└─────────────────────────────────────┘
```

### 3. Order Summary

```
┌─────────────────────────────────────┐
│           CHI TIẾT ĐỚN HÀNG         │
├─────────────────────────────────────┤
│ Tổng tiền sản phẩm:    225.000₫    │
│ Giảm giá flash sale:   -75.000₫    │
│ Giảm giá voucher:      -22.500₫    │
│ Phí vận chuyển:         35.000₫    │
│ Giảm phí ship:         -35.000₫    │
│ ─────────────────────────────────── │
│ TỔNG CỘNG:             127.500₫    │
│ Tiết kiệm được:        132.500₫    │
└─────────────────────────────────────┘
```

---

## 🚀 PERFORMANCE OPTIMIZATION

### 1. Database Optimization

#### Indexes
```sql
-- Flash sale queries
CREATE INDEX idx_flash_sale_item_book_id ON flash_sale_items(book_id);
CREATE INDEX idx_flash_sale_item_flash_sale_id ON flash_sale_items(flash_sale_id);
CREATE INDEX idx_flash_sale_time ON flash_sales(start_time, end_time);

-- Voucher queries  
CREATE INDEX idx_voucher_time ON vouchers(start_time, end_time);
CREATE INDEX idx_user_voucher_user_voucher ON user_vouchers(user_id, voucher_id);

-- Order queries
CREATE INDEX idx_order_user_id ON orders(user_id);
CREATE INDEX idx_order_detail_flash_sale ON order_details(flash_sale_item_id);
```

### 2. Caching Strategy

```java
// Cache flash sale items
@Cacheable(value = "flashSaleItems", key = "#bookId")
public FlashSaleItem getActiveFlashSaleItem(Long bookId) {
    // Implementation
}

// Cache voucher validation
@Cacheable(value = "voucherValidation", key = "#userId + '_' + #voucherId")
public boolean isVoucherValidForUser(Long userId, Long voucherId) {
    // Implementation  
}
```

### 3. Race Condition Handling

```java
// Pessimistic locking for flash sale stock
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT f FROM FlashSaleItem f WHERE f.id = :id")
FlashSaleItem findByIdWithLock(@Param("id") Long id);
```

---

## 🔒 SECURITY CONSIDERATIONS

### 1. Flash Sale Security

```java
// Validate flash sale authenticity
public void validateFlashSaleIntegrity(FlashSaleItem item) {
    // 1. Check if flash sale is still active
    // 2. Verify discount price is not manipulated
    // 3. Check stock availability
    // 4. Validate per-user limits
}
```

### 2. Voucher Security

```java
// Prevent voucher abuse
public void validateVoucherUsage(Long userId, Long voucherId) {
    // 1. Check if voucher is still valid
    // 2. Verify user hasn't exceeded usage limit
    // 3. Check if voucher code is authentic
    // 4. Validate order meets minimum requirements
}
```

---

## 📈 MONITORING & METRICS

### 1. Flash Sale Metrics

```sql
-- Flash sale performance
SELECT 
    fs.name,
    SUM(od.quantity) as totalSold,
    SUM(od.quantity * fsi.discount_price) as totalRevenue,
    SUM(od.quantity * (b.price - fsi.discount_price)) as totalSavings
FROM flash_sales fs
JOIN flash_sale_items fsi ON fs.id = fsi.flash_sale_id
JOIN order_details od ON fsi.id = od.flash_sale_item_id
JOIN books b ON fsi.book_id = b.id
GROUP BY fs.id;
```

### 2. Voucher Analytics

```sql
-- Voucher usage analytics
SELECT 
    v.code,
    v.voucher_type,
    COUNT(ov.order_id) as usageCount,
    SUM(ov.discount_applied) as totalDiscount,
    AVG(ov.discount_applied) as avgDiscount
FROM vouchers v
LEFT JOIN order_vouchers ov ON v.id = ov.voucher_id
GROUP BY v.id;
```

---

## 🎓 TRAINING SCENARIOS

### Scenario A: Developer onboarding
**Câu hỏi**: Làm sao để kiểm tra một sản phẩm có đang trong flash sale không?

**Trả lời**: 
1. Kiểm tra `orderDetail.flashSaleItemId != null`
2. Validate `FlashSale.startTime <= now <= FlashSale.endTime`
3. Kiểm tra `FlashSale.status = 1` và `FlashSaleItem.status = 1`
4. Sử dụng `FlashSaleItem.discountPrice` thay vì `Book.price`

### Scenario B: Business logic testing
**Câu hỏi**: User mua 2 sản phẩm flash sale 70K, dùng voucher giảm 10% tối đa 20K. Tính tổng tiền?

**Trả lời**:
```
Subtotal: 70000 × 2 = 140000
Discount: min(140000 × 10%, 20000) = 14000  
Shipping: 25000
Total: 140000 + 25000 - 14000 = 151000
```

### Scenario C: Error handling
**Câu hỏi**: User cố gắng dùng 2 voucher giảm giá cùng lúc. Hệ thống xử lý sao?

**Trả lời**: 
1. System detect 2 voucher cùng type (PERCENTAGE/FIXED_AMOUNT)
2. Throw ValidationException với message "Chỉ được áp dụng tối đa 1 voucher thường"
3. Return error response cho client
4. User phải chọn lại voucher

---

## 🏆 BEST PRACTICES

### 1. Code Organization
```java
// ✅ Good: Separate validation logic
@Service
public class FlashSaleValidationService {
    public void validateFlashSaleOrder(OrderDetail detail) {
        validateTimeWindow();
        validateStock();
        validateUserLimit();
    }
}

// ✅ Good: Separate calculation logic  
@Service
public class OrderCalculationService {
    public OrderCalculation calculateOrder(OrderRequest request) {
        // Clean calculation logic
    }
}
```

### 2. Error Handling
```java
// ✅ Good: Specific exception types
public class FlashSaleException extends RuntimeException {
    private final FlashSaleErrorCode errorCode;
    private final Object details;
}

// ✅ Good: Detailed error messages
throw new FlashSaleException(
    FlashSaleErrorCode.STOCK_INSUFFICIENT,
    "Flash sale stock insufficient. Available: " + availableStock,
    Map.of("requestedQuantity", quantity, "availableStock", availableStock)
);
```

### 3. Testing Strategy
```java
// ✅ Good: Comprehensive test scenarios
@Test
void testFlashSaleOrderWithVoucherStacking() {
    // Given: Flash sale item + 2 vouchers
    // When: Create order
    // Then: Verify correct price calculation
}

@Test
void testFlashSaleStockConcurrency() {
    // Given: Multiple users buy same flash sale item
    // When: Concurrent orders
    // Then: Verify stock consistency
}
```

---

## 🎯 CONCLUSION

Hệ thống Order với Flash Sale và Voucher là một nghiệp vụ phức tạp đòi hỏi:

1. **Validation nghiêm ngặt** - Kiểm tra đầy đủ tất cả điều kiện
2. **Calculation chính xác** - Tính toán đúng từng khoản phí
3. **Performance tối ưu** - Xử lý nhanh với lượng lớn đơn hàng
4. **Security cao** - Bảo vệ khỏi fraud và abuse
5. **Monitoring tốt** - Theo dõi metrics và performance

Bằng cách tuân thủ các nguyên tắc trong tài liệu này, team có thể:
- Develop features mới một cách consistent
- Debug issues nhanh chóng
- Maintain code quality cao
- Scale hệ thống hiệu quả

**Remember**: Luôn test thoroughly với các edge cases và maintain documentation updated khi có thay đổi business logic!
