# 🎯 Flash Sale Status Priority Rules

## 🔄 **PRIORITY HIERARCHY**

### **Priority 1: Admin Control (HIGHEST)**
```
flashSale.status = 0 → flashSaleItem.status = 0
```
- **Quyền cao nhất**: Admin có thể tắt flash sale bất cứ lúc nào
- **Vượt qua thời gian**: Dù flash sale còn hiệu lực, admin tắt → tắt hết
- **Use cases**: Lỗi giá, hết hàng đột xuất, vấn đề pháp lý

### **Priority 2: Time Validation (SECOND)**
```
flashSale.status = 1 → Check time validity:
├── startTime <= currentTime <= endTime → flashSaleItem.status = 1
└── else → flashSaleItem.status = 0
```
- **Điều kiện**: Flash sale phải được bật (status = 1)
- **Kiểm tra thời gian**: Chỉ active khi trong khoảng thời gian hiệu lực

---

## 📊 **DECISION MATRIX**

| Flash Sale Status | Time Valid | Flash Sale Item Status | Reason |
|-------------------|------------|------------------------|---------|
| 0 | ✅ Valid | 0 | Admin disabled |
| 0 | ❌ Invalid | 0 | Admin disabled |
| 1 | ✅ Valid | 1 | Active |
| 1 | ❌ Not started | 0 | Not started yet |
| 1 | ❌ Expired | 0 | Expired |

---

## 🎯 **CÁC TÌNH HUỐNG NGHIỆP VỤ**

### **Tình huống 1: Admin tắt khẩn cấp**
```
Flash Sale: status = 0, endTime = tương lai
Flash Sale Items: status = 0 (bị admin ép tắt)
Cart Items: Hiển thị giá gốc
```

### **Tình huống 2: Flash Sale hoạt động & thời gian hợp lệ**
```
Flash Sale: status = 1, startTime <= now <= endTime
Flash Sale Items: status = 1 (đang hoạt động)
Cart Items: Hiển thị giá flash sale
```

### **Tình huống 3: Flash Sale được bật nhưng chưa bắt đầu**
```
Flash Sale: status = 1, startTime > now
Flash Sale Items: status = 0 (chưa bắt đầu)
Cart Items: Hiển thị giá gốc
```

### **Tình huống 4: Flash Sale được bật nhưng đã hết hạn**
```
Flash Sale: status = 1, endTime < now
Flash Sale Items: status = 0 (đã hết hạn)
Cart Items: Hiển thị giá gốc
```

---

## 🔧 **IMPLEMENTATION LOGIC**

```java
// Priority 1: Admin Control
if (flashSale.getStatus() == 0) {
    newStatus = (byte) 0;
    reason = "flash sale disabled by admin";
} else {
    // Priority 2: Time Validation
    boolean isTimeValid = (flashSale.getStartTime() <= currentTime) && 
                         (currentTime <= flashSale.getEndTime());
    
    newStatus = isTimeValid ? (byte) 1 : (byte) 0;
    reason = isTimeValid ? "active (valid time)" : 
            (currentTime < flashSale.getStartTime() ? "not started yet" : "expired");
}
```

---

## 🎨 **FRONTEND INTEGRATION**

### **API Response khi Admin tắt Flash Sale:**
```json
{
  "flashSaleItemId": 123,     // ✅ Luôn có
  "flashSalePrice": null,     // ❌ Ẩn khi bị tắt
  "flashSaleName": null,      // ❌ Ẩn khi bị tắt
  "itemType": "REGULAR",      // ✅ Hiển thị như sản phẩm thường
  "unitPrice": 85000          // ✅ Giá gốc
}
```

### **Logic hiển thị Frontend:**
```javascript
if (item.itemType === "REGULAR") {
    if (item.flashSaleItemId) {
        // Hiển thị "Flash sale tạm thời không khả dụng"
        showFlashSaleUnavailable();
    } else {
        // Hiển thị sản phẩm thường bình thường
        showRegularItem();
    }
}
```

---

## 🚀 **TRIGGER POINTS**

### **🔧 KHI NÀO CẬP NHẬT TRẠNG THÁI:**
1. **Admin cập nhật flash sale** → `autoUpdateFlashSaleItemsStatus(flashSaleId)`
2. **Admin bật/tắt flash sale** → `autoUpdateFlashSaleItemsStatus(flashSaleId)`
3. **Flash sale hết hạn** → Scheduler gọi `autoUpdateFlashSaleItemsStatus(flashSaleId)`

### **NHỮNG GÌ KHÔNG BAO GIỜ XẢY RA:**
- ❌ Set `flashSaleItemId = null` trong cart items
- ❌ Mất mối quan hệ dữ liệu
- ❌ Scheduled tasks chạy mỗi 30 giây

---

## 🛒 **VẤN ĐỀ ĐỒNG BỘ GIỎ HÀNG & GIẢI PHÁP**

### **❌ VẤN ĐỀ: Sản phẩm trong giỏ hàng bỏ lỡ flash sale**
```
Tình huống:
1. User thêm Sách A vào giỏ hàng (flashSaleItemId = null)
2. Admin tạo flash sale cho Sách A
3. Giỏ hàng vẫn hiển thị giá gốc → User bị mất giảm giá!
```

### **✅ GIẢI PHÁP: Tự động đồng bộ giỏ hàng**
```java
// Khi admin tạo flash sale item mới
@Override
public ApiResponse<FlashSaleItemResponse> create(FlashSaleItemRequest request) {
    // ...code hiện tại...
    FlashSaleItem savedItem = flashSaleItemRepository.save(item);
    
    // 🔥 TỰ ĐỘNG ĐỒNG BỘ: Cập nhật giỏ hàng
    cartItemService.syncCartItemsWithNewFlashSale(flashSale.getId());
    
    return success(savedItem);
}
```

### **🔄 LOGIC ĐỒNG BỘ**
```java
// Tìm các sản phẩm trong giỏ hàng chưa có flash sale cho cuốn sách này
List<CartItem> cartItems = cartItemRepository.findCartItemsWithoutFlashSale(bookId);

// Gán flash sale cho các sản phẩm đã có trong giỏ hàng
for (CartItem cartItem : cartItems) {
    cartItem.setFlashSaleItem(flashSaleItem);
    cartItemRepository.save(cartItem);
}
```

### **📱 LỢI ÍCH CHO FRONTEND**
- User thấy giảm giá ngay lập tức mà không cần refresh
- Giỏ hàng tự động cập nhật sang giá flash sale
- Không cần làm mới trang thủ công

---

## ✅ **LỢI ÍCH**

1. **Quyền điều khiển Admin**: Tắt flash sale khẩn cấp bất cứ lúc nào
2. **Tính toàn vẹn dữ liệu**: Luôn giữ mối quan hệ giỏ hàng ↔ flash sale
3. **Tự động đồng bộ giỏ hàng**: Giỏ hàng tự động cập nhật khi admin tạo flash sale mới
4. **Hiệu suất**: Chỉ update khi cần thiết
5. **Trải nghiệm người dùng**: Chuyển đổi mượt mà giữa flash sale và giá thường
6. **Logic nghiệp vụ**: Quy tắc ưu tiên rõ ràng, dễ hiểu và bảo trì

---

**Tóm lại**: 
- **Ưu tiên**: Admin có quyền tối cao, thời gian chỉ được kiểm tra khi admin cho phép (status = 1)
- **Đồng bộ giỏ hàng**: Tự động đồng bộ cart items khi admin tạo/cập nhật flash sale để user không bỏ lỡ discount
