# Nghiệp vụ Giỏ hàng (Cart & CartItem) - BookStation

## 1. Tổng quan nghiệp vụ

### 1.1 Mục đích
Giỏ hàng là nơi khách hàng tạm thời lưu trữ các sản phẩm sách muốn mua trước khi tiến hành đặt hàng. Mỗi user có một giỏ hàng duy nhất, giỏ hàng chứa nhiều CartItem.

### 1.2 Cấu trúc dữ liệu
- **Cart**: Giỏ hàng chính của user
- **CartItem**: Từng item sách trong giỏ hàng

## 2. Quy tắc nghiệp vụ Cart

### 2.1 Tạo giỏ hàng
- Mỗi user chỉ có **1 giỏ hàng duy nhất**
- Giỏ hàng được tạo tự động khi user thêm sản phẩm đầu tiên
- Nếu user đã có giỏ hàng, sử dụng giỏ hàng hiện tại

### 2.2 Trạng thái giỏ hàng
- **status = 1**: Giỏ hàng hoạt động
- **status = 0**: Giỏ hàng bị vô hiệu hóa

## 3. Quy tắc nghiệp vụ CartItem

### 3.1 Thêm sản phẩm vào giỏ hàng
- **Sách thường**: Chỉ cần bookId, quantity
- **Sách flash sale**: Cần bookId, flashSaleItemId, quantity
- **Validation**:
  - Sách phải tồn tại và đang hoạt động (status = 1)
  - Quantity > 0 và <= stock hiện có
  - Nếu là flash sale: phải còn trong thời gian và còn số lượng

### 3.2 Kiểm tra sản phẩm đã tồn tại
```
IF (bookId = existing_book AND flashSaleItemId = existing_flashSaleItemId) 
THEN UPDATE quantity = existing_quantity + new_quantity
ELSE INSERT new CartItem
```

### 3.3 Cập nhật số lượng
- **Tăng quantity**: Kiểm tra không vượt quá stock
- **Giảm quantity**: Minimum = 1
- **quantity = 0**: Xóa CartItem

### 3.4 Xử lý Flash Sale
- **Ưu tiên**: Flash sale price > regular price
- **Validation flash sale**:
  - FlashSale phải active (status = 1)
  - Trong thời gian (startTime <= now <= endTime)  
  - FlashSaleItem còn stock
- **Khi flash sale hết hạn**: 
  - Chuyển CartItem về regular book
  - Set flashSaleItemId = NULL

## 4. Các tình huống nghiệp vụ

### 4.1 User chưa có giỏ hàng (Auto-detect Flash Sale)
```
POST /api/carts/items
{
  "userId": 1,
  "bookId": 1,
  "quantity": 2
}

Backend Logic:
1. Tìm flash sale active tốt nhất cho bookId
2. Nếu có: tạo CartItem với flash sale price
3. Nếu không: tạo CartItem với regular price
4. Tạo Cart mới cho user nếu chưa có

=> Tạo Cart mới + CartItem (tự động detect flash sale)
```

### 4.2 Thêm sách đã có trong giỏ
```
Existing: Cart has BookId=1, quantity=3
Add: BookId=1, quantity=2

=> Update quantity = 3 + 2 = 5
```

### 4.3 Thêm flash sale item (Auto-detect)
```
POST /api/carts/items
{
  "userId": 1,
  "bookId": 1,
  "quantity": 1
}

Backend Logic:
1. Tự động tìm flash sale active tốt nhất cho bookId
2. Kiểm tra flash sale còn hiệu lực và còn stock
3. Tạo CartItem với flash sale price nếu hợp lệ
4. Fallback về regular price nếu flash sale không khả dụng

=> Backend tự động chọn giá tốt nhất cho user
```

### 4.4 **Vấn đề Flash Sale Item ID - Giải pháp chi tiết**

#### 4.4.1 Tại sao cần flashSaleItemId?
- **1 Book có thể có Flash Sale**: Cùng 1 sách chỉ có 1 flash sale active tại 1 thời điểm
- **Giá khác nhau**: Mỗi flash sale có thể có mức giá khác nhau
- **Thời gian khác nhau**: Các flash sale có thể chạy song song hoặc liên tiếp
- **Stock riêng biệt**: Mỗi flash sale có quota riêng

#### 4.4.2 Cách Frontend lấy flashSaleItemId

**Giải pháp 1: API lấy flash sale active theo bookId**
```
GET /api/flash-sales/book/{bookId}/active
Response:
{
  "flashSaleItemId": 5,
  "bookId": 1,
  "salePrice": 150000,
  "stockQuantity": 100,
  "startTime": "2025-07-04T00:00:00",
  "endTime": "2025-07-04T23:59:59"
}
```

**Giải pháp 2: Tích hợp trong API Book Detail**
```
GET /api/books/{bookId}
Response:
{
  "id": 1,
  "title": "Sách ABC",
  "price": 200000,
  "stockQuantity": 50,
  "activeFlashSale": {
    "flashSaleItemId": 5,
    "salePrice": 150000,
    "stockQuantity": 100,
    "startTime": "2025-07-04T00:00:00",
    "endTime": "2025-07-04T23:59:59"
  }
}
```

**Giải pháp 3: Luồng đơn giản hóa (Khuyến nghị)**
```
POST /api/carts/items
{
  "bookId": 1,
  "quantity": 1,
  "useFlashSale": true  // Optional, backend tự tìm flash sale active
}

Backend Logic:
1. Tìm flash sale active của bookId
2. Nếu có: áp dụng flash sale price
3. Nếu không: áp dụng regular price
```

#### 4.4.3 Business Rule đơn giản hóa

**Business rule**: 1 sách chỉ có 1 flash sale active tại 1 thời điểm

**Quy tắc kiểm tra:**
1. **Status = 1** (Active)
2. **Thời gian hiện tại** nằm trong khoảng [startTime, endTime]
3. **Còn stock** > 0
4. **Không cần so sánh giá**: Chỉ có 1 flash sale nên áp dụng luôn

**Query logic:**
```sql
SELECT * FROM flash_sale_items fsi
JOIN flash_sales fs ON fsi.flash_sale_id = fs.id
WHERE fsi.book_id = ?
  AND fs.status = 1
  AND fs.start_time <= NOW()
  AND fs.end_time >= NOW()
  AND fsi.stock_quantity > 0
LIMIT 1;  -- Chỉ có 1 flash sale active
```

### 4.5 Flash sale item hết hạn (Dynamic Event-Driven)
```
Dynamic Scheduler approach:
1. Khi tạo Flash Sale: Tự động schedule task tại endTime
2. Khi đến endTime: Task tự động chạy
3. Update tất cả CartItem của flash sale đó về regular price
4. Log activity và remove task khỏi scheduler
5. Efficient: Chỉ chạy đúng lúc cần, không waste tài nguyên

Example:
- Flash Sale A: endTime = "2025-07-04 15:00:00"
- Scheduler tự động tạo task chạy đúng 15:00
- Không cần check mỗi phút/mỗi giờ
```

### 4.6 Sách hết stock
```
GET /api/carts/user/{userId}
=> Hiển thị cảnh báo cho items có quantity > available_stock
=> Suggestion: "Còn lại X sản phẩm, bạn có muốn điều chỉnh?"
```

### 4.7 Xóa toàn bộ giỏ hàng
```
DELETE /api/carts/user/{userId}/clear
=> Xóa tất cả CartItems, giữ lại Cart (soft delete)
```

## 5. API Endpoints (Updated)

### 5.1 Cart Management
- `GET /api/carts/user/{userId}` - Lấy giỏ hàng của user
- `DELETE /api/carts/user/{userId}/clear` - Xóa toàn bộ giỏ hàng
- `GET /api/carts/user/{userId}/summary` - Tóm tắt giỏ hàng (số lượng, tổng tiền)
- `POST /api/carts/user/{userId}/validate` - Validate và cập nhật giỏ hàng

### 5.2 CartItem Management (Simplified)
- `POST /api/carts/items` - **Thêm sản phẩm (auto-detect flash sale)**
- `PUT /api/carts/items/{id}` - Cập nhật quantity CartItem
- `DELETE /api/carts/items/{id}` - Xóa CartItem
- `POST /api/carts/items/batch` - Thêm nhiều sản phẩm cùng lúc
- `POST /api/carts/items/user/{userId}/validate` - Validate CartItems của user
- ~~`POST /api/carts/items/smart`~~ - **Deprecated** (sử dụng endpoint chính)

### 5.3 New: Auto Flash Sale Detection
Tất cả Cart APIs giờ đây **tự động detect flash sale** mà không cần frontend truyền thêm thông tin.

## 6. Validation Rules (Updated)

### 6.1 Add CartItem (Simplified)
- userId không null và user phải tồn tại
- bookId không null và book phải active
- quantity > 0 và <= available stock
- **Backend tự động tìm flash sale tốt nhất** (không cần flashSaleItemId)
- **Tự động fallback** về regular price nếu flash sale không khả dụng

### 6.2 Update Quantity
- quantity >= 1 (nếu = 0 thì xóa)
- quantity <= available stock
- **Scheduler tự động convert** flash sale hết hạn về regular

### 6.3 Stock Validation (Auto-detect)
```java
// Backend tự động detect và validate stock
if (cartItem.getFlashSaleItem() == null) {
    // Regular stock validation
    return cartItem.getQuantity() <= book.getStockQuantity();
} else {
    // Flash sale stock validation
    return cartItem.getQuantity() <= flashSaleItem.getStockQuantity() 
           && flashSaleService.isFlashSaleValid(flashSaleItem.getId());
}
```

### 6.4 Dynamic Flash Sale Expiration Handling
```java
// Khi tạo Flash Sale
@Service
public class FlashSaleServiceImpl {
    public FlashSale createFlashSale(FlashSaleRequest request) {
        FlashSale flashSale = // ... create flash sale
        
        // Schedule expiration task tại endTime
        flashSaleExpirationScheduler.scheduleFlashSaleExpiration(
            flashSale.getId(), 
            flashSale.getEndTime()
        );
        
        return flashSale;
    }
}

// Task tự động chạy tại endTime
public void handleSpecificFlashSaleExpiration(Integer flashSaleId) {
    // Chỉ update cart items của flash sale này
    // Efficient và chính xác
}
```

## 7. Error Handling

### 7.1 Lỗi thường gặp
- `CART_NOT_FOUND`: User chưa có giỏ hàng
- `BOOK_NOT_FOUND`: Sách không tồn tại  
- `BOOK_INACTIVE`: Sách đã ngừng bán
- `INSUFFICIENT_STOCK`: Không đủ hàng tồn kho
- `FLASH_SALE_EXPIRED`: Flash sale đã hết hạn
- `FLASH_SALE_OUT_OF_STOCK`: Flash sale đã hết hàng
- `INVALID_QUANTITY`: Số lượng không hợp lệ

### 7.2 Response format
```json
{
  "status": 400,
  "message": "Không đủ hàng tồn kho. Còn lại: 3 sản phẩm",
  "data": null,
  "errors": [
    {
      "field": "quantity", 
      "message": "Số lượng vượt quá hàng tồn kho"
    }
  ]
}
```

## 8. Performance Considerations (Updated)

### 8.1 Database Queries
- Index on `cart.user_id` 
- Index on `cart_item.cart_id`
- Index on `cart_item.book_id`
- **Index on `cart_item.flash_sale_item_id`** (cho scheduler queries)

### 8.2 Caching (Khuyến nghị)
- **Cache active flash sale list** để tránh query nhiều lần
- Cache stock quantity của sách hot
- **Cache flash sale expiration times** cho scheduler

### 8.3 Batch Operations
- Hỗ trợ thêm nhiều items cùng lúc
- **Batch update expired flash sales** trong scheduler
- Validation một lần cho toàn bộ batch

### 8.4 Dynamic Scheduler Performance
- **Event-driven scheduling**: Task chỉ chạy đúng lúc flash sale kết thúc
- **Resource efficient**: Không waste CPU/memory với polling
- **Precise timing**: Update cart items ngay khi flash sale hết hạn
- **Scalable**: Có thể handle hàng nghìn flash sales đồng thời
- **Memory management**: Auto cleanup completed tasks

---

**Lưu ý**: Cart nghiệp vụ giờ đây **hoàn toàn tự động** và **resource-efficient** trong việc xử lý flash sale. Dynamic scheduling đảm bảo cart items được update chính xác tại thời điểm flash sale kết thúc.

## 9. Dynamic Event-Based Flash Sale Expiration System

### 9.1 Dynamic Scheduler Approach (Optimal)
```java
// Khi tạo Flash Sale
flashSaleExpirationScheduler.scheduleFlashSaleExpiration(flashSaleId, endTime);

// Task tự động chạy tại endTime
@ScheduledTask(time = flashSale.endTime)
public void handleSpecificFlashSaleExpiration(Integer flashSaleId) {
    // Chỉ xử lý flash sale này, không check toàn bộ database
}
```

### 9.2 Benefits của Dynamic Approach
- **Resource Efficient**: Không check liên tục, chỉ chạy khi cần
- **Precise Timing**: Update đúng thời điểm flash sale kết thúc  
- **Scalable**: Có thể handle unlimited số lượng flash sales
- **Memory Optimal**: Task tự cleanup sau khi complete
- **No Polling**: Không waste tài nguyên với fixed-rate checking

### 9.3 Implementation Flow
```
1. Admin tạo Flash Sale với endTime = "2025-07-04 15:00:00"
2. FlashSaleService gọi scheduler.scheduleFlashSaleExpiration(flashSaleId, endTime)
3. Scheduler tạo task chạy đúng 15:00:00
4. Đến 15:00:00: Task tự động chạy
5. Update tất cả cart items của flash sale này về regular price
6. Task complete và tự cleanup
```

### 9.4 Performance Comparison
```
❌ Fixed-Rate Polling (old):
- Check mỗi phút = 1440 checks/day
- Waste 99.9% resources (khi không có gì expire)
- Database load cao

✅ Dynamic Scheduling (new):  
- Chỉ chạy khi flash sale thực sự kết thúc
- 0% waste resources
- Minimal database load
- Precise timing
```

## 9. Giải thích về FlashSaleItemId

### 9.1 Tại sao cần flashSaleItemId?
Khi một sản phẩm có flash sale, chúng ta cần phân biệt:
- **Regular booking**: Mua với giá gốc
- **Flash sale booking**: Mua với giá flash sale

FlashSaleItemId giúp:
- Xác định chính xác flash sale nào đang áp dụng
- Kiểm tra stock riêng của flash sale (khác với stock gốc)
- Áp dụng giá flash sale chính xác
- Đơn giản hóa logic backend (chỉ có 1 flash sale active)

### 9.2 Cách Frontend lấy flashSaleItemId

#### 9.2.1 Giải pháp 1: API lấy flash sale đang active
```http
GET /api/flash-sales/book/{bookId}/active
Response:
{
  "status": 200,
  "data": {
    "flashSaleItemId": 5,
    "flashSaleId": 2,
    "discountPrice": 150000,
    "originalPrice": 200000,
    "stockQuantity": 50,
    "startTime": "2025-07-04T08:00:00",
    "endTime": "2025-07-04T20:00:00"
  }
}
```

#### 9.2.2 Giải pháp 2: Thông tin flash sale trong API book detail
```http
GET /api/books/{bookId}
Response:
{
  "status": 200,
  "data": {
    "id": 1,
    "title": "Sách ABC",
    "price": 200000,
    "stockQuantity": 100,
    "activeFlashSale": {
      "flashSaleItemId": 5,
      "discountPrice": 150000,
      "stockQuantity": 50,
      "endTime": "2025-07-04T20:00:00"
    }
  }
}
```

#### 9.2.3 Giải pháp 3: Auto-detect trong API add cart (Đơn giản nhất)
```http
POST /api/carts/items
{
  "bookId": 1,
  "quantity": 1,
  "preferFlashSale": true  // Frontend chỉ cần báo muốn flash sale
}

Backend sẽ tự động:
- Tìm flash sale active cho bookId
- Áp dụng nếu có, không thì dùng giá gốc
```

### 9.3 Business rule đơn giản hóa

#### 9.3.1 Quy tắc đơn giản
**Business rule**: 1 sách chỉ có 1 flash sale active tại 1 thời điểm

Quy tắc kiểm tra:
1. **Status = 1** (đang hoạt động)
2. **Thời gian hợp lệ** (startTime <= now <= endTime)
3. **Còn stock** (stockQuantity > 0)
4. **Không cần so sánh giá**: Chỉ có 1 flash sale active

#### 9.3.2 Logic implementation
```java
@Service
public class FlashSaleService {
    
    public Optional<FlashSaleItem> findActiveFlashSaleForBook(Long bookId) {
        // Business rule: 1 sách chỉ có 1 flash sale active tại 1 thời điểm
        return flashSaleItemRepository.findActiveFlashSaleByBookId(bookId, LocalDateTime.now())
            .filter(item -> item.getStockQuantity() > 0);
    }
}
```

#### 9.3.3 Repository query
```java
@Repository
public interface FlashSaleItemRepository extends JpaRepository<FlashSaleItem, Long> {
    
    @Query("SELECT fsi FROM FlashSaleItem fsi " +
           "JOIN fsi.flashSale fs " +
           "WHERE fsi.book.id = :bookId " +
           "AND fs.status = 1 " +
           "AND fs.startTime <= :now " +
           "AND fs.endTime >= :now " +
           "ORDER BY fsi.discountPrice ASC")
    List<FlashSaleItem> findActiveFlashSalesByBookId(@Param("bookId") Long bookId, 
                                                    @Param("now") LocalDateTime now);
}
```

### 9.4 Luồng hoạt động Frontend

#### 9.4.1 Luồng 1: Explicit flashSaleItemId
```javascript
// Frontend lấy thông tin flash sale
const flashSaleInfo = await getActiveFlashSale(bookId);
if (flashSaleInfo) {
    // Thêm với flash sale
    await addToCart({
        bookId: bookId,
        flashSaleItemId: flashSaleInfo.flashSaleItemId,
        quantity: 1
    });
} else {
    // Thêm với giá gốc
    await addToCart({
        bookId: bookId,
        quantity: 1
    });
}
```

#### 9.4.2 Luồng 2: Auto-detect (Khuyến nghị)
```javascript
// Frontend chỉ cần báo muốn flash sale
await addToCart({
    bookId: bookId,
    quantity: 1,
    preferFlashSale: true
});
// Backend tự động chọn flash sale tốt nhất
```

### 9.5 API bổ sung cần thiết

#### 9.5.1 GET Flash Sale cho sách
```http
GET /api/flash-sales/book/{bookId}/active
GET /api/flash-sales/book/{bookId}/all  // Tất cả flash sale của sách
```

#### 9.5.2 POST Cart với auto-detect
```http
POST /api/carts/items/smart
{
  "bookId": 1,
  "quantity": 1,
  "preferFlashSale": true
}
```

### 9.6 Xử lý Edge Cases

#### 9.6.1 Flash sale hết stock khi add cart
```java
// Trong CartService
if (flashSaleItem.getStockQuantity() < quantity) {
    // Fallback về regular booking
    return addRegularItemToCart(bookId, quantity);
}
```

#### 9.6.2 Flash sale kết thúc giữa chừng
```java
// Background job hoặc khi user view cart
@Scheduled(fixedRate = 60000) // Check mỗi phút
public void validateCartFlashSales() {
    List<CartItem> expiredItems = cartItemRepository.findExpiredFlashSaleItems();
    for (CartItem item : expiredItems) {
        item.setFlashSaleItem(null); // Chuyển về giá gốc
        cartItemRepository.save(item);
    }
}
```

---

**Khuyến nghị**: Sử dụng **Giải pháp 3** (auto-detect) để đơn giản hóa frontend, backend sẽ tự động chọn flash sale tốt nhất cho user.

---
