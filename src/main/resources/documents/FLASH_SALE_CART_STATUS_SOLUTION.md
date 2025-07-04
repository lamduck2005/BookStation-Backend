# 🎯 Flash Sale Cart - Status Solution

## ✅ GIẢI PHÁP ĐÃ IMPLEMENT

### 🔍 Vấn đề:
- Cart item bị `null` flash sale data khi admin gia hạn flash sale
- User phải remove và add lại sản phẩm

### 🎯 Giải pháp:
**Sử dụng `status` trong `FlashSaleItem` thay vì set `flashSaleItemId = null`**

---

## 📊 LOGIC MỚI

### 🕐 Auto-Update Status dựa trên thời gian:
```java
// Tự động update status
if (flashSale.endTime > currentTime) {
    flashSaleItem.status = 1;  // Active
} else {
    flashSaleItem.status = 0;  // Expired
}
```

### 🛒 Cart Response Logic:
```java
// Chỉ trả flash sale data khi status = 1
if (cartItem.flashSaleItem != null && cartItem.flashSaleItem.status == 1) {
    return flashSaleData;  // Hiển thị giá flash sale
} else {
    return regularData;    // Hiển thị giá gốc
}
```

---

## 🔄 FLOW HOẠT ĐỘNG

### Khi Flash Sale Hết Hạn:
1. ✅ **NEW:** Scheduler gọi `FlashSaleService.autoUpdateFlashSaleItemsStatus()` 
2. `flashSaleItem.status` → `0` (expired)
3. Cart response → Regular price (giá gốc)
4. **FlashSaleItemId vẫn được giữ nguyên**

### Khi Admin Gia Hạn:
1. Update `flashSale.endTime` 
2. ✅ **NEW:** `FlashSaleService.updateFlashSale()` tự động gọi `autoUpdateFlashSaleItemsStatus()`
3. `flashSaleItem.status` → `1` (active)
4. Cart response → Flash sale price (giá sale)
5. **Không cần user làm gì cả**

### Khi User Add Thêm:
- Luôn merge với cart item cũ (dựa trên bookId)
- Tự động apply flash sale nếu active (status = 1)

---

## 🧪 TEST SCENARIOS

### Test 1: Flash Sale Hết Hạn
```bash
# 1. User có sản phẩm flash sale trong cart
GET /api/cart/user/1
# Response: flashSalePrice, flashSaleName có giá trị

# 2. Flash sale hết hạn (tự động hoặc manual)
# 3. Check cart lại
GET /api/cart/user/1
# Response: flashSalePrice, flashSaleName = null, itemType = "REGULAR"
```

### Test 2: Admin Gia Hạn
```bash
# 1. Admin gia hạn flash sale
PUT /api/admin/flash-sales/1
{
    "endTime": 1751999999999  // Thời gian mới (sau hiện tại)
}

# 2. Check cart ngay lập tức
GET /api/cart/user/1
# Response: flashSalePrice, flashSaleName có giá trị trở lại
```

### Test 3: Add Sản Phẩm Sau Khi Gia Hạn
```bash
# 1. Add cùng sản phẩm vào cart
POST /api/cart/add
{
    "userId": 1,
    "bookId": 1,
    "quantity": 2
}

# Response: Quantity được cộng dồn, không tạo bản ghi mới
```

---

## 🎯 LỢI ÍCH

✅ **Bảo toàn dữ liệu**: Không mất quan hệ `flashSaleItemId`  
✅ **Tự động sync**: Admin gia hạn → User thấy ngay  
✅ **Không duplicate**: Luôn merge cart items  
✅ **User-friendly**: Không cần remove/add lại  
✅ **Admin-friendly**: Gia hạn là xong, không cần thao tác thêm  

---

## 🔧 FILES THAY ĐỔI

1. **FlashSaleItem.java** → Thêm `@ColumnDefault("1")` cho `status`
2. **FlashSaleServiceImpl.java** → Thêm `autoUpdateFlashSaleItemsStatus()`
3. **CartItemServiceImpl.java** → Update logic không set `null`
4. **CartItemResponseMapper.java** → Check `status == 1`
5. **DataInitializationService.java** → Set `status = 1` khi khởi tạo

---

## 🚀 DEPLOYMENT

### Production Ready:
- ✅ Backward compatible (data cũ vẫn hoạt động)
- ✅ No breaking changes
- ✅ Auto-migration logic

### Migration Script (nếu cần):
```sql
-- Set status = 1 cho các FlashSaleItem hiện tại
UPDATE flash_sale_item 
SET status = 1 
WHERE status IS NULL;
```
