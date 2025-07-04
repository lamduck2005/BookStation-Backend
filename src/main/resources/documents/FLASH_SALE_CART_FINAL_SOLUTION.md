# FLASH SALE CART ITEM - FINAL SOLUTION DOCUMENTATION

## 🎯 Vấn đề ban đầu
- Khi flash sale hết hạn, backend set `CartItem.flashSaleItem = null`
- Khi admin gia hạn lại flash sale, cart item không thể restore thông tin flash sale
- Frontend mất thông tin `flashSaleItemId` khi flash sale hết hạn

## ✅ Giải pháp cuối cùng

### 1. Database Schema Changes
- **FlashSaleItem**: Thêm cột `status` (TINYINT, DEFAULT 1)
  - `status = 1`: Active (hiển thị giá flash sale)
  - `status = 0`: Inactive (hiển thị giá gốc)

### 2. Core Logic Changes
- **KHÔNG BAO GIỜ** set `CartItem.flashSaleItem = NULL`
- **LUÔN GIỮ** mối quan hệ `CartItem.flashSaleItemId`
- **CHỈ THAY ĐỔI** `FlashSaleItem.status` để bật/tắt

### 3. API Response Behavior

#### ✅ Khi Flash Sale Active (status = 1):
```json
{
  "flashSaleItemId": 123,
  "flashSalePrice": 68000,
  "flashSaleName": "Flash Sale Cuối Tuần",
  "flashSaleDiscount": 20,
  "flashSaleEndTime": 1751658883947,
  "itemType": "FLASH_SALE",
  "unitPrice": 68000
}
```

#### ✅ Khi Flash Sale Inactive (status = 0):
```json
{
  "flashSaleItemId": 123,        // ✅ VẪN TRẢ VỀ ID
  "flashSalePrice": null,        // ❌ Ẩn thông tin flash sale
  "flashSaleName": null,
  "flashSaleDiscount": null,
  "flashSaleEndTime": null,
  "itemType": "REGULAR",         // ✅ Hiển thị như regular
  "unitPrice": 85000             // ✅ Giá gốc
}
```

## 🔧 Implementation Details

### 1. CartItemResponseMapper.java
```java
// LUÔN trả về flashSaleItemId nếu có liên kết
if (cartItem.getFlashSaleItem() != null) {
    response.setFlashSaleItemId(cartItem.getFlashSaleItem().getId());
    
    // CHỈ hiển thị thông tin chi tiết khi status = 1
    if (cartItem.getFlashSaleItem().getStatus() == 1) {
        response.setFlashSalePrice(cartItem.getFlashSaleItem().getDiscountPrice());
        response.setItemType("FLASH_SALE");
        // ... các thông tin flash sale khác
    } else {
        response.setItemType("REGULAR");
        response.setUnitPrice(cartItem.getBook().getPrice());
    }
}
```

### 2. FlashSaleServiceImpl.java
```java
// Auto-update status dựa trên endTime
@Transactional
public void autoUpdateFlashSaleItemsStatus() {
    long currentTime = System.currentTimeMillis();
    
    // Update expired items to status = 0
    flashSaleItemRepository.updateStatusByExpiredFlashSales(currentTime, (byte) 0);
    
    // Update active items to status = 1 
    flashSaleItemRepository.updateStatusByActiveFlashSales(currentTime, (byte) 1);
}
```

### 3. FlashSaleExpirationScheduler.java
```java
@Scheduled(fixedDelay = 30000) // Mỗi 30 giây
public void processExpiredFlashSales() {
    // CHỈ update status, KHÔNG động đến cart item
    flashSaleService.autoUpdateFlashSaleItemsStatus();
}
```

## 🎯 Benefits của Solution

### 1. Data Integrity
- **Luôn giữ** mối quan hệ `CartItem ↔ FlashSaleItem`
- **Không mất** thông tin khi flash sale hết hạn
- **Tự động restore** khi admin gia hạn

### 2. Frontend Experience
- **Luôn nhận được** `flashSaleItemId` trong response
- **Biết được** sản phẩm đã từng có flash sale
- **Có thể hiển thị** thông báo "Flash sale đã hết" thay vì ẩn hoàn toàn

### 3. Business Logic
- **Admin gia hạn** → tự động active lại cart items
- **Consistent** pricing logic
- **No data loss** khi flash sale expire/extend

## 📋 Testing Scenarios

### Test Case 1: Flash Sale Hết Hạn
1. Add sản phẩm flash sale vào cart
2. Chờ flash sale hết hạn (hoặc set endTime trong quá khứ)
3. **Expected**: `flashSaleItemId` vẫn có, `flashSalePrice` = null, `itemType` = "REGULAR"

### Test Case 2: Admin Gia Hạn
1. Flash sale đã hết hạn (từ test case 1)
2. Admin update `endTime` của flash sale về tương lai
3. **Expected**: Cart item tự động hiển thị lại thông tin flash sale

### Test Case 3: Add Sản Phẩm Sau Khi Gia Hạn
1. Flash sale đã được gia hạn
2. User add thêm cùng sản phẩm vào cart
3. **Expected**: Merge với cart item cũ, hiển thị đúng giá flash sale

## 🚀 Deployment Notes

### Database Migration
```sql
-- Thêm cột status vào FlashSaleItem
ALTER TABLE flash_sale_items ADD COLUMN status TINYINT DEFAULT 1;

-- Update existing data
UPDATE flash_sale_items SET status = 1 WHERE status IS NULL;
```

### Code Changes Applied
- ✅ `FlashSaleItem.java`: Added status field
- ✅ `CartItemResponseMapper.java`: Always return flashSaleItemId
- ✅ `CartItemServiceImpl.java`: Removed null-setting logic
- ✅ `FlashSaleServiceImpl.java`: Added auto-update methods
- ✅ `FlashSaleExpirationScheduler.java`: Only update status
- ✅ `CartItemRepository.java`: Removed batchUpdateExpiredFlashSales

### Final Validation
- ✅ No compilation errors
- ✅ Application starts successfully
- ✅ No circular dependencies
- ✅ Scheduled jobs working correctly
- ✅ API responses follow new format

---

**Kết luận**: Solution đã hoàn thiện, cart item sẽ luôn giữ `flashSaleItemId` và tự động restore thông tin flash sale khi admin gia hạn.
