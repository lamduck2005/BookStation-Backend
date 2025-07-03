# 🚀 FRONTEND GUIDE - Book Detail API

## 📌 API Endpoint
```
GET /api/books/{id}
```

## 📋 Response Structure
```json
{
  "status": 200,
  "message": "Thành công",
  "data": {
    "id": 1,
    "bookName": "Tên sách",
    "price": 180000,              // ← GIÁ ĐÃ XỬ LÝ (backend tự động)
    "stockQuantity": 50,          // ← STOCK ĐÃ XỬ LÝ
    
    // Flash Sale Info
    "flashSalePrice": 180000,     // null nếu không có flash sale
    "flashSaleDiscount": 10,      // % giảm giá flash sale
    "flashSaleEndTime": 1672531200000,  // timestamp kết thúc
    "flashSaleSoldCount": 25,     // số lượng đã bán
    
    // Direct Discount Info
    "discountValue": 50000,       // giảm theo VND (null nếu không có)
    "discountPercent": 20,        // giảm theo % (null nếu không có)
    
    "serverTime": 1672531200000,  // 🔥 Server time (chống hack)
    
    // Thông tin khác...
    "description": "Mô tả sách",
    "categoryName": "Danh mục",
    "authors": [{"id": 1, "authorName": "Tác giả"}],
    "coverImageUrl": "https://...",
    "isbn": "978-123456789"
  }
}
```

## 🎯 2 Trường hợp chính

### 🔥 Case 1: Sản phẩm có Flash Sale
```json
{
  "id": 1,
  "bookName": "Sách Flash Sale",
  "price": 180000,              // ← GIÁ FLASH SALE
  "stockQuantity": 30,          // ← STOCK FLASH SALE
  
  "flashSalePrice": 180000,     // ← CÓ FLASH SALE
  "flashSaleDiscount": 25,      // ← Giảm 25%
  "flashSaleEndTime": 1672531200000,
  "flashSaleSoldCount": 45,
  
  "discountValue": null,        // ← NULL (flash sale ưu tiên)
  "discountPercent": null,      // ← NULL
  
  "serverTime": 1672531200000
}
```

**Frontend Logic:**
```javascript
if (book.flashSalePrice !== null) {
  // ✅ Có flash sale
  showFlashSaleBadge();
  setupCountdownTimer(book.flashSaleEndTime, book.serverTime);
  displayPrice(book.price);  // Giá đã là flash sale
}
```

### 📦 Case 2: Sản phẩm bình thường (có discount cố định)
```json
{
  "id": 2,
  "bookName": "Sách Discount",
  "price": 160000,              // ← GIÁ SAU DISCOUNT (200k - 20% = 160k)
  "stockQuantity": 100,         // ← STOCK BOOK GỐC
  
  "flashSalePrice": null,       // ← KHÔNG CÓ FLASH SALE
  "flashSaleDiscount": null,
  "flashSaleEndTime": null,
  "flashSaleSoldCount": null,
  
  "discountValue": null,        // ← Hoặc có giá trị (VD: 50000)
  "discountPercent": 20,        // ← Giảm 20%
  
  "serverTime": 1672531200000
}
```

**Frontend Logic:**
```javascript
if (book.flashSalePrice === null) {
  // ✅ Không có flash sale
  if (book.discountValue !== null || book.discountPercent !== null) {
    // ✅ Có discount cố định
    showDiscountBadge();
  }
  displayPrice(book.price);  // Giá đã xử lý discount
}
```

## 🛡️ Countdown Timer - Chống Hack

```javascript
function setupCountdownTimer(flashSaleEndTime, serverTime) {
  const clientTime = Date.now();
  const timeDiff = serverTime - clientTime;  // Chênh lệch client-server
  
  const timer = setInterval(() => {
    const now = Date.now() + timeDiff;  // Sync với server time
    const remaining = flashSaleEndTime - now;
    
    if (remaining <= 0) {
      clearInterval(timer);
      location.reload();  // Reload để cập nhật giá mới
    } else {
      const hours = Math.floor(remaining / (1000 * 60 * 60));
      const minutes = Math.floor((remaining % (1000 * 60 * 60)) / (1000 * 60));
      const seconds = Math.floor((remaining % (1000 * 60)) / 1000);
      
      document.getElementById('countdown').textContent = 
        `${hours}:${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`;
    }
  }, 1000);
}
```

## 💡 Key Points

1. **Backend xử lý tất cả** → Frontend chỉ hiển thị
2. **`price` luôn là giá cuối cùng** → Không cần tính toán
3. **Check `flashSalePrice !== null`** → Có flash sale
4. **Dùng `serverTime`** → Chống hack countdown
5. **Gọi API 1 lần** → Không cần polling

## 🚫 Những gì KHÔNG cần làm

❌ Không tính toán giá ở frontend  
❌ Không check thời gian flash sale ở frontend  
❌ Không polling API liên tục  
❌ Không dùng `Date.now()` cho countdown  

## ✅ Workflow đơn giản

```javascript
// 1. Gọi API 1 lần
fetch(`/api/books/${bookId}`)
  .then(response => response.json())
  .then(data => {
    const book = data.data;
    
    // 2. Hiển thị giá (đã xử lý)
    displayPrice(book.price);
    
    // 3. Check flash sale
    if (book.flashSalePrice !== null) {
      showFlashSaleBadge();
      setupCountdownTimer(book.flashSaleEndTime, book.serverTime);
    }
    
    // 4. Check discount
    if (book.discountValue !== null || book.discountPercent !== null) {
      showDiscountBadge();
    }
  });
```

**Xong! Đơn giản vậy thôi!** 🎉
