# 🎯 Flash Sale Cart - Status-Based Implementation Guide

## 📋 TÓM TẮT THAY ĐỔI

### ❌ VẤN ĐỀ CŨ:
- Khi flash sale hết hạn → set `CartItem.flashSaleItem = null`
- Khi admin gia hạn → CartItem không được cập nhật tự động
- Mất mối quan hệ dữ liệu, khó tracking và phục hồi

### ✅ GIẢI PHÁP MỚI:
- Sử dụng `FlashSaleItem.status` để bật/tắt thay vì set null
- Bảo toàn mối quan hệ dữ liệu CartItem ↔ FlashSaleItem
- Tự động enable/disable khi admin gia hạn/hết hạn
- Frontend chỉ cần check status để hiển thị

---

## 🏗️ KIẾN TRÚC MỚI

### 1. **FlashSaleItem Status Logic**
```sql
-- FlashSaleItem.status:
-- 1 = ACTIVE   (hiển thị flash sale price)
-- 0 = INACTIVE (hiển thị regular price)
```

### 2. **CartItem Logic**
```java
// CartItem LUÔN giữ reference đến FlashSaleItem
// Frontend chỉ hiển thị flash sale info khi:
cartItem.flashSaleItem != null && cartItem.flashSaleItem.status == 1
```

### 3. **Auto Status Management**
```
🕒 Hết hạn → FlashSaleItem.status = 0 (auto by scheduler)
⏰ Gia hạn → FlashSaleItem.status = 1 (auto when admin update)
```

---

## 🔧 CÁC THAY ĐỔI CODE

### 1. **FlashSaleItem Entity**
```java
@ColumnDefault("1")
@Column(name = "status")
Byte status; // 1=active, 0=inactive
```

### 2. **CartItemResponseMapper Logic**
```java
// CHỈ hiển thị flash sale khi status = 1
if (cartItem.getFlashSaleItem() != null && cartItem.getFlashSaleItem().getStatus() == 1) {
    // Hiển thị flash sale price, name, discount...
    response.setItemType("FLASH_SALE");
    response.setUnitPrice(cartItem.getFlashSaleItem().getDiscountPrice());
} else {
    // Hiển thị regular price
    response.setItemType("REGULAR");  
    response.setUnitPrice(cartItem.getBook().getPrice());
}
```

### 3. **Auto Enable/Disable Logic**
```java
// Khi hết hạn
flashSaleItem.setStatus((byte) 0); // Disable

// Khi admin gia hạn  
flashSaleItem.setStatus((byte) 1); // Enable
```

---

## 🚀 API USAGE GUIDE

### 1. **Thêm sản phẩm vào Cart**

#### Request:
```http
POST /api/cart-items
Content-Type: application/json

{
    "userId": 1,
    "bookId": 1,
    "quantity": 2,
    "flashSaleItemId": 123  // Optional
}
```

#### Response khi có Flash Sale Active:
```json
{
    "status": 200,
    "message": "Thêm sản phẩm vào giỏ hàng thành công",
    "data": {
        "id": 28,
        "cartId": 6,
        "bookId": 1,
        "bookName": "Tôi thấy hoa vàng trên cỏ xanh",
        "bookPrice": 85000.00,
        "flashSaleItemId": 123,
        "flashSaleName": "Flash Sale Cuối Tuần",
        "flashSalePrice": 68000.00,
        "flashSaleDiscount": 20.00,
        "flashSaleEndTime": 1751658883947,
        "quantity": 2,
        "unitPrice": 68000.00,
        "totalPrice": 136000.00,
        "itemType": "FLASH_SALE",
        "flashSaleExpired": false
    }
}
```

#### Response khi Flash Sale Inactive/Expired:
```json
{
    "status": 200,
    "message": "Thêm sản phẩm vào giỏ hàng thành công",
    "data": {
        "id": 28,
        "cartId": 6,
        "bookId": 1,
        "bookName": "Tôi thấy hoa vàng trên cỏ xanh",
        "bookPrice": 85000.00,
        "flashSaleItemId": 123,      // ✅ LUÔN trả về nếu có liên kết
        "flashSaleName": null,       // ❌ null khi status = 0
        "flashSalePrice": null,      // ❌ null khi status = 0
        "flashSaleDiscount": null,   // ❌ null khi status = 0
        "flashSaleEndTime": null,    // ❌ null khi status = 0
        "quantity": 2,
        "unitPrice": 85000.00,       // ✅ giá gốc
        "totalPrice": 170000.00,
        "itemType": "REGULAR",       // ✅ hiển thị REGULAR
        "flashSaleExpired": false
    }
}
```

### 2. **Lấy danh sách Cart Items**

#### Request:
```http
GET /api/cart-items/user/1
```

#### Response:
```json
{
    "status": 200,
    "message": "Lấy danh sách sản phẩm thành công",
    "data": [
        {
            "id": 28,
            "cartId": 6,
            "bookId": 1,
            "bookName": "Tôi thấy hoa vàng trên cỏ xanh",
            "bookCode": "BOOK1751525291414",
            "bookImageUrl": null,
            "bookPrice": 85000.00,
            "flashSaleItemId": 123,      // ✅ Vẫn có reference
            "flashSaleName": "Flash Sale Cuối Tuần",  // ✅ Hiển thị khi status=1
            "flashSalePrice": 68000.00,   // ✅ Giá flash sale
            "flashSaleDiscount": 20.00,   // ✅ % giảm giá
            "flashSaleEndTime": 1751658883947,
            "quantity": 2,
            "unitPrice": 68000.00,        // ✅ Dùng flash sale price
            "totalPrice": 136000.00,
            "itemType": "FLASH_SALE",     // ✅ Nhận diện loại
            "flashSaleExpired": false,
            "canAddMore": true
        }
    ]
}
```

### 3. **Admin Gia hạn Flash Sale**

#### Request:
```http
PUT /api/flash-sales/123
Content-Type: application/json

{
    "name": "Flash Sale Cuối Tuần - Extended",
    "startTime": 1751650000000,
    "endTime": 1751750000000,    // ✅ Thời gian mới
    "status": 1
}
```

#### Backend Auto Process:
```
1. ✅ Enable FlashSaleItems: status = 1
2. ✅ Schedule new expiration task 
3. ✅ Sync existing cart items
4. ✅ Log: "Enabled 5 flash sale items for flash sale 123"
```

### 4. **Frontend Display Logic**

#### JavaScript Example:
```javascript
function displayCartItem(item) {
    if (item.itemType === "FLASH_SALE") {
        // Hiển thị flash sale UI
        return `
            <div class="cart-item flash-sale">
                <span class="original-price">${item.bookPrice}</span>
                <span class="flash-price">${item.flashSalePrice}</span>
                <span class="discount">-${item.flashSaleDiscount}%</span>
                <span class="flash-badge">Flash Sale</span>
            </div>
        `;
    } else {
        // Hiển thị regular UI
        return `
            <div class="cart-item regular">
                <span class="regular-price">${item.unitPrice}</span>
            </div>
        `;
    }
}
```

---

## 🔍 BUSINESS FLOW

### Scenario 1: **Flash Sale Hết Hạn**
```
1. 🕒 Schedule task detect expiration
2. ❌ Set FlashSaleItem.status = 0  
3. 📱 Frontend call API → itemType = "REGULAR"
4. 💰 User sees regular price
```

### Scenario 2: **Admin Gia Hạn**
```
1. 👨‍💼 Admin update endTime via API
2. ✅ Auto enable: FlashSaleItem.status = 1
3. 🔄 Auto sync existing cart items  
4. 📱 Frontend call API → itemType = "FLASH_SALE"
5. 💰 User sees flash sale price again
```

### Scenario 3: **User Thêm Sản Phẩm**
```
1. 🛒 User add book to cart
2. 🔍 System check active flash sale (status=1)
3. ✅ Auto apply best flash sale nếu có
4. 📱 Response với đúng price type
```

---

## 🎨 FRONTEND INTEGRATION

### 1. **Cart Item Display Component**
```jsx
const CartItemCard = ({ item }) => {
  const isFlashSale = item.itemType === "FLASH_SALE";
  
  return (
    <div className={`cart-item ${isFlashSale ? 'flash-sale' : 'regular'}`}>
      <div className="product-info">
        <h3>{item.bookName}</h3>
        <p>{item.bookCode}</p>
      </div>
      
      <div className="price-info">
        {isFlashSale ? (
          <div className="flash-sale-price">
            <span className="original-price">{formatPrice(item.bookPrice)}</span>
            <span className="flash-price">{formatPrice(item.flashSalePrice)}</span>
            <span className="discount-badge">-{item.flashSaleDiscount}%</span>
            <div className="flash-sale-timer">
              Còn: {formatTimeRemaining(item.flashSaleEndTime)}
            </div>
          </div>
        ) : (
          <div className="regular-price">
            <span className="price">{formatPrice(item.unitPrice)}</span>
          </div>
        )}
      </div>
      
      <div className="quantity-controls">
        <button onClick={() => updateQuantity(item.id, item.quantity - 1)}>-</button>
        <span>{item.quantity}</span>
        <button onClick={() => updateQuantity(item.id, item.quantity + 1)}>+</button>
      </div>
      
      <div className="total-price">
        {formatPrice(item.totalPrice)}
      </div>
    </div>
  );
};
```

### 2. **API Integration Service**
```javascript
class CartService {
  static async addToCart(userId, bookId, quantity, flashSaleItemId = null) {
    const response = await fetch('/api/cart-items', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ userId, bookId, quantity, flashSaleItemId })
    });
    return response.json();
  }
  
  static async getCartItems(userId) {
    const response = await fetch(`/api/cart-items/user/${userId}`);
    return response.json();
  }
  
  static async updateQuantity(cartItemId, quantity) {
    const response = await fetch(`/api/cart-items/${cartItemId}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ quantity })
    });
    return response.json();
  }
}
```

---

## 🧪 TESTING SCENARIOS

### Test Case 1: **Flash Sale Expiration**
```bash
1. Add sản phẩm flash sale vào cart
2. Wait hoặc manually trigger expiration
3. Verify: itemType = "REGULAR", unitPrice = bookPrice
```

### Test Case 2: **Flash Sale Extension**  
```bash
1. Add sản phẩm vào cart khi flash sale active
2. Wait expiration → verify regular price
3. Admin extend flash sale
4. Verify: itemType = "FLASH_SALE", unitPrice = flashSalePrice
```

### Test Case 3: **Duplicate Prevention**
```bash
1. Add sản phẩm flash sale vào cart
2. Flash sale expires
3. Admin extends flash sale  
4. Add same product again
5. Verify: Quantity merged, không tạo duplicate
```

---

## 📊 ADMIN MONITORING

### Flash Sale Status Dashboard:
```sql
-- Query để monitor flash sale status
SELECT 
    fs.name as flash_sale_name,
    fs.start_time,
    fs.end_time, 
    fs.status as flash_sale_status,
    COUNT(fsi.id) as total_items,
    COUNT(CASE WHEN fsi.status = 1 THEN 1 END) as active_items,
    COUNT(CASE WHEN fsi.status = 0 THEN 1 END) as inactive_items
FROM flash_sale fs
LEFT JOIN flash_sale_item fsi ON fs.id = fsi.flash_sale_id
GROUP BY fs.id, fs.name, fs.start_time, fs.end_time, fs.status
ORDER BY fs.created_at DESC;
```

### Cart Items với Flash Sale Status:
```sql
-- Query để xem cart items và flash sale status
SELECT 
    ci.id as cart_item_id,
    b.book_name,
    u.full_name as user_name,
    ci.quantity,
    CASE 
        WHEN fsi.status = 1 THEN 'FLASH_SALE'
        ELSE 'REGULAR' 
    END as item_type,
    CASE 
        WHEN fsi.status = 1 THEN fsi.discount_price
        ELSE b.price 
    END as unit_price
FROM cart_item ci
JOIN book b ON ci.book_id = b.id
JOIN cart c ON ci.cart_id = c.id
JOIN user u ON c.user_id = u.id
LEFT JOIN flash_sale_item fsi ON ci.flash_sale_item_id = fsi.id
ORDER BY ci.created_at DESC;
```

---

## 🔧 DEPLOYMENT CHECKLIST

### 1. **Database Migration**
```sql
-- Đảm bảo FlashSaleItem có column status
ALTER TABLE flash_sale_item ADD COLUMN status TINYINT DEFAULT 1;
UPDATE flash_sale_item SET status = 1 WHERE status IS NULL;
```

### 2. **Application Config**
```properties
# application.properties
flash-sale.auto-sync.enabled=true
flash-sale.status-based.enabled=true
```

### 3. **Frontend Updates**
- Update CartItemCard component
- Update API integration service  
- Test UI với cả FLASH_SALE và REGULAR items
- Update CSS cho flash sale badges

### 4. **Monitoring Setup**
- Monitor FlashSaleItem status changes
- Track cart sync performance
- Alert on sync failures

---

## 🏆 BENEFITS

### ✅ **Data Integrity**
- Không mất mối quan hệ CartItem ↔ FlashSaleItem
- Có thể tracking full history
- Dễ analytics và reporting

### ✅ **User Experience**  
- Seamless transition khi flash sale expire/extend
- Không duplicate cart items
- Real-time price updates

### ✅ **Admin Management**
- Easy enable/disable flash sales
- Clear monitoring dashboard
- Automated sync processes

### ✅ **Developer Experience**
- Clean, maintainable code
- Clear separation of concerns
- Easy testing and debugging

---

Cách tiếp cận mới này đảm bảo tính toàn vẹn dữ liệu, user experience tốt hơn, và dễ dàng quản lý cho admin! 🎉
