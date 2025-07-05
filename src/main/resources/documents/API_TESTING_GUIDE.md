# 🧪 API Testing Guide - Flash Sale Cart System

## Test APIs với Postman/curl

### 1. Test Auto-Detection Flash Sale

#### A. Add Cart Item (Auto-detect flash sale)
```http
POST http://localhost:8080/api/carts/items
Content-Type: application/json

{
  "userId": 1,
  "bookId": 1,
  "quantity": 2
}
```

**Expected Response (with flash sale):**
```json
{
  "status": 200,
  "message": "Thêm sản phẩm vào giỏ hàng thành công 🔥 Đã áp dụng flash sale!",
  "data": {
    "id": 123,
    "bookId": 1,
    "quantity": 2,
    "unitPrice": 150000,
    "totalPrice": 300000,
    "isFlashSale": true,
    "flashSaleItemId": 5,
    "savedAmount": 100000
  }
}
```

**Expected Response (no flash sale):**
```json
{
  "status": 200,
  "message": "Thêm sản phẩm vào giỏ hàng thành công",
  "data": {
    "id": 124,
    "bookId": 1,
    "quantity": 2,
    "unitPrice": 200000,
    "totalPrice": 400000,
    "isFlashSale": false
  }
}
```

#### B. Add Cart Item (Flash sale out of stock)
```http
POST http://localhost:8080/api/carts/items
Content-Type: application/json

{
  "userId": 1,
  "bookId": 1,
  "quantity": 50
}
```

**Expected Response:**
```json
{
  "status": 200,
  "message": "Thêm sản phẩm vào giỏ hàng thành công ⚠️ Flash sale không đủ hàng, đã áp dụng giá gốc",
  "data": {
    "id": 125,
    "bookId": 1,
    "quantity": 50,
    "unitPrice": 200000,
    "isFlashSale": false
  }
}
```

#### C. Add Cart Item (Exceed stock completely)
```http
POST http://localhost:8080/api/carts/items
Content-Type: application/json

{
  "userId": 1,
  "bookId": 1,
  "quantity": 1000
}
```

**Expected Response:**
```json
{
  "status": 400,
  "message": "Không đủ hàng tồn kho. Còn lại: 100",
  "data": null
}
```

### 2. Test Cart Validation

#### A. Validate user cart
```http
POST http://localhost:8080/api/carts/items/user/1/validate
```

**Expected Response:**
```json
{
  "status": 200,
  "message": "Đã cập nhật 2 sản phẩm flash sale hết hạn. Cảnh báo: Flash sale 'Sách ABC' sẽ hết hạn trong 3 phút",
  "data": [
    {
      "id": 123,
      "bookId": 1,
      "quantity": 2,
      "unitPrice": 200000,
      "isFlashSale": false
    }
  ]
}
```

### 3. Test Update Cart Item

#### A. Update quantity
```http
PUT http://localhost:8080/api/carts/items/123?quantity=5
```

#### B. Remove item (set quantity = 0)
```http
PUT http://localhost:8080/api/carts/items/123?quantity=0
```

### 4. Test Get Cart Items

#### A. Get all items for user
```http
GET http://localhost:8080/api/carts/items/user/1
```

### 5. Test Batch Add

#### A. Add multiple items
```http
POST http://localhost:8080/api/carts/items/batch
Content-Type: application/json

{
  "userId": 1,
  "items": [
    {
      "bookId": 1,
      "quantity": 2
    },
    {
      "bookId": 2,
      "quantity": 1
    },
    {
      "bookId": 3,
      "quantity": 3
    }
  ]
}
```

---

## Test Scenarios Manual

### Scenario 1: Flash Sale Active
1. Tạo flash sale cho book ID = 1
2. Set thời gian: startTime < now < endTime
3. Set stock = 10, price = 150k (original = 200k)
4. Call API add cart với quantity = 2
5. Verify: Nhận được flash sale price

### Scenario 2: Flash Sale Out of Stock
1. Setup flash sale với stock = 5
2. Call API với quantity = 10
3. Verify: Fallback về regular price với message warning

### Scenario 3: Multiple Flash Sales (Business Rule Test)
1. Tạo 2 flash sales cho cùng 1 book (should not happen)
2. Call API add cart
3. Verify: Chỉ áp dụng 1 flash sale (theo business rule)

### Scenario 4: Flash Sale Expiration
1. Tạo flash sale với endTime = now + 2 minutes
2. Add vào cart với flash sale price
3. Đợi 2 phút cho flash sale expire
4. Call validate API
5. Verify: Cart items được update về regular price

### Scenario 5: Existing Cart Item
1. Add book 1 với quantity = 3 (total = 3)
2. Add book 1 lần nữa với quantity = 2 (total = 5)
3. Verify: Chỉ có 1 cart item với quantity = 5

### Scenario 6: Stock Validation
1. Book có stock = 8, user đã có 6 trong cart
2. Add thêm 5 (total would be 11 > 8)
3. Verify: Error message "Bạn đã có 6 trong giỏ. Còn lại: 2"

---

## curl Commands

### Add to cart
```bash
curl -X POST http://localhost:8080/api/carts/items \
  -H "Content-Type: application/json" \
  -d '{"userId": 1, "bookId": 1, "quantity": 2}'
```

### Validate cart
```bash
curl -X POST http://localhost:8080/api/carts/items/user/1/validate
```

### Get cart items
```bash
curl -X GET http://localhost:8080/api/carts/items/user/1
```

### Update cart item
```bash
curl -X PUT "http://localhost:8080/api/carts/items/123?quantity=5"
```

---

## Expected Logs

### Application startup
```
🔥 FlashSaleExpirationScheduler initialized
📦 CartItemService with auto-detection ready
✅ Application started successfully
```

### API calls
```
✅ ADD CART: User 1 added book 1 with auto-detected flash sale 5
✅ STOCK VALIDATION: Flash sale stock = 10, requested = 2, available
🔥 AUTO-DETECT: Found active flash sale for book 1, price 150000
```

### Scheduler activity
```
🔥 SCHEDULED: Batch task for time 2025-07-04T15:30:00 with flash sale 123
🔥 BATCH EXPIRATION: Processing 2 flash sales expiring at 2025-07-04T15:30:00
🔥 BATCH EXPIRATION: Updated 8 cart items for 2 expired flash sales
```

---

**🎯 Test Priority:**
1. **Highest**: Basic add cart with auto-detection  
2. **High**: Stock validation và fallback scenarios
3. **Medium**: Batch expiration và validation API
4. **Low**: Edge cases và error handling
