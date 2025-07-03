# BOOK PRICING LOGIC - FINAL IMPLEMENTATION

## 📌 Tổng quan

Hệ thống đã được thiết kế với logic xử lý giá và discount rõ ràng, backend tự động xử lý, frontend chỉ nhận dữ liệu và hiển thị.

## 🎯 Logic ưu tiên

**Backend tự động xử lý theo thứ tự ưu tiên:**
1. **Flash Sale** (ưu tiên cao nhất) → nếu có flash sale đang active
2. **Direct Discount** → nếu không có flash sale, nhưng có discount trực tiếp
3. **Original Price** → nếu không có flash sale và discount

## 🔥 API Chi tiết sách (GET /api/books/{id})

### Response: BookDetailResponse
```json
{
  "id": 1,
  "bookName": "Sách Example",
  "price": 180000,              // ← GIÁ ĐÃ XỬ LÝ (Flash Sale hoặc Discount hoặc Gốc)
  "stockQuantity": 50,          // ← SỐ LƯỢNG ĐÃ XỬ LÝ (Flash Sale stock hoặc Book stock)
  
  // ✅ Flash Sale info (null nếu không có)
  "flashSalePrice": 180000,     // Giá flash sale
  "flashSaleDiscount": 10,      // % giảm giá flash sale
  "flashSaleEndTime": 1234567890000,  // Timestamp kết thúc
  "flashSaleSoldCount": 25,     // Đã bán bao nhiêu trong flash sale
  
  // ✅ Direct Discount info (null nếu không có hoặc đang flash sale)
  "discountValue": null,        // Giảm theo giá trị (VD: 50000)
  "discountPercent": null,      // Giảm theo % (VD: 20)
  
  "serverTime": 1234567890000   // 🔥 Server time chống hack client
}
```

### Logic Backend (BookDetailResponseMapper)

```java
private void setPriceAndDiscountFields(BookDetailResponse response, Book book) {
    // 1. Check Flash Sale active
    if (có flash sale active) {
        response.setPrice(flashSale.discountPrice);           // Override giá
        response.setStockQuantity(flashSale.stockQuantity);   // Override stock
        response.setFlashSalePrice(flashSale.discountPrice);
        response.setFlashSaleDiscount(flashSale.discountPercent);
        response.setFlashSaleEndTime(flashSale.endTime);
        response.setFlashSaleSoldCount(soldCount);
        
        // Clear direct discount
        response.setDiscountValue(null);
        response.setDiscountPercent(null);
        return;
    }
    
    // 2. Check Direct Discount
    if (book.discountActive && có discount) {
        BigDecimal discountedPrice = tính giá sau discount;
        response.setPrice(discountedPrice);                   // Override giá
        response.setDiscountValue(book.discountValue);
        response.setDiscountPercent(book.discountPercent);
        
        // Clear flash sale
        response.setFlashSalePrice(null);
        response.setFlashSaleDiscount(null);
        return;
    }
    
    // 3. Original Price - không có gì
    // Giá và stock giữ nguyên từ book gốc
    response.setFlashSalePrice(null);
    response.setDiscountValue(null);
}
```

## 📝 API List/Trending (GET /api/books, POST /api/books/trending)

### Response: BookResponse
```json
{
  "id": 1,
  "bookName": "Sách Example",
  "price": 200000,              // ← GIÁ GỐC (không xử lý discount/flash sale)
  "stockQuantity": 100,         // ← STOCK GỐC
  
  // ❌ KHÔNG CÓ: flashSalePrice, discountValue, serverTime, v.v.
}
```

### Logic Backend (BookResponseMapper)
```java
public BookResponse toResponse(Book book) {
    response.setPrice(book.getPrice());           // ← Chỉ lấy giá gốc
    response.setStockQuantity(book.getStockQuantity()); // ← Chỉ lấy stock gốc
    // KHÔNG CÓ logic flash sale/discount
    return response;
}
```

## 🛡️ Frontend Implementation

### 1. API List/Trending - Đơn giản
```javascript
// Gọi API list/trending
fetch('/api/books')
  .then(response => response.json())
  .then(data => {
    // Chỉ hiển thị giá gốc, không cần logic phức tạp
    displayPrice(book.price);
  });
```

### 2. API Chi tiết sách - Hiển thị giá đã xử lý
```javascript
// Gọi API chi tiết 1 lần duy nhất
fetch('/api/books/123')
  .then(response => response.json())
  .then(book => {
    // Backend đã xử lý giá, frontend chỉ hiển thị
    displayPrice(book.price);  // ← Giá đã xử lý
    
    // Check flash sale
    if (book.flashSalePrice !== null) {
      showFlashSaleBadge();
      setupCountdownTimer(book.flashSaleEndTime, book.serverTime);
    }
    
    // Check direct discount
    if (book.discountValue !== null || book.discountPercent !== null) {
      showDiscountBadge();
    }
  });
```

### 3. Countdown Timer - Dùng Server Time
```javascript
function setupCountdownTimer(flashSaleEndTime, serverTime) {
  const clientTime = Date.now();
  const timeDiff = serverTime - clientTime;  // Chênh lệch client-server
  
  setInterval(() => {
    const now = Date.now() + timeDiff;  // Sync với server time
    const remaining = flashSaleEndTime - now;
    
    if (remaining <= 0) {
      // Flash sale đã kết thúc → reload trang để cập nhật giá
      location.reload();
    } else {
      displayCountdown(remaining);
    }
  }, 1000);
}
```

## 📊 Database Schema

### Book Entity - Direct Discount
```sql
ALTER TABLE book 
ADD COLUMN discount_value DECIMAL(10,2) NULL,      -- Giảm theo giá trị
ADD COLUMN discount_percent INT NULL,              -- Giảm theo %
ADD COLUMN discount_active BOOLEAN DEFAULT FALSE; -- Bật/tắt discount
```

### Flash Sale Tables (đã có)
- `flash_sale`: Thông tin flash sale
- `flash_sale_item`: Chi tiết sách trong flash sale
- `flash_sale_order_item`: Tracking đã bán

## ✅ Lợi ích của thiết kế này

1. **Backend chủ động**: Tự động xử lý logic giá, frontend chỉ hiển thị
2. **Performance tốt**: API list/trending nhẹ, không query flash sale
3. **Bảo mật**: Server time chống hack countdown timer
4. **Đơn giản**: Frontend không cần logic phức tạp
5. **Maintainable**: Logic tập trung ở backend, dễ debug và sửa

## 🚀 Migration Guide

```sql
-- Nếu có dữ liệu cũ với discount_start_time, discount_end_time
UPDATE book 
SET discount_active = FALSE 
WHERE discount_start_time IS NOT NULL OR discount_end_time IS NOT NULL;

-- Xóa cột cũ (nếu có)
ALTER TABLE book 
DROP COLUMN discount_start_time,
DROP COLUMN discount_end_time;
```
