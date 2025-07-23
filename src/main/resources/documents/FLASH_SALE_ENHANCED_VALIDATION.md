# 🚀 FLASH SALE ENHANCED VALIDATION - LUỒNG HỆ THỐNG HOÀN THIỆN

## 🎯 **OVERVIEW**

Hệ thống đã được nâng cấp để xử lý đúng validation flash sale với **hai loại thông báo lỗi khác nhau** và **xử lý hoàn hàng tự động khôi phục quota**.

---

## ✅ **ĐÃ CẢI THIỆN**

### **1. 📝 Hai Loại Thông Báo Lỗi Flash Sale**

#### **Loại 1: Đã đạt giới hạn tối đa (đã mua hết)**
```
"Bạn đã mua đủ 5 sản phẩm flash sale 'Tên Sách' cho phép. Không thể mua thêm."
```
- **Khi nào**: `currentPurchased >= maxAllowed`
- **Tình huống**: User đã mua đủ số lượng cho phép, không thể mua thêm

#### **Loại 2: Chưa đạt giới hạn nhưng đặt quá số lượng**
```
"Bạn đã mua 2 sản phẩm, chỉ được mua thêm tối đa 3 sản phẩm flash sale 'Tên Sách'."
```
- **Khi nào**: `currentPurchased < maxAllowed` nhưng `quantity > remainingAllowed`
- **Tình huống**: User chưa đạt giới hạn nhưng đang cố mua quá số lượng còn lại

#### **Loại 3: Thông báo chung cho trường hợp khác**
```
"Bạn chỉ được mua tối đa 5 sản phẩm flash sale 'Tên Sách'."
```
- **Khi nào**: Các trường hợp đặc biệt khác

---

### **2. 🔄 Xử Lý Hoàn Hàng Khôi Phục Quota**

#### **Logic Đếm Số Lượng Đã Mua (Enhanced)**
```sql
-- Query cải tiến để tính đúng số lượng thực tế đã mua
SELECT COALESCE(
    (SELECT SUM(delivered.quantity) FROM OrderDetail delivered 
     WHERE delivered.flashSaleItem.id = :flashSaleItemId 
     AND delivered.order.user.id = :userId 
     AND delivered.order.orderStatus = 'DELIVERED') - 
    COALESCE((SELECT SUM(refunded.quantity) FROM OrderDetail refunded 
     WHERE refunded.flashSaleItem.id = :flashSaleItemId 
     AND refunded.order.user.id = :userId 
     AND refunded.order.orderStatus IN ('REFUNDED', 'PARTIALLY_REFUNDED')), 0), 0)
```

#### **Ví Dụ Thực Tế:**
- **Giới hạn**: 5 sản phẩm/user
- **Đã mua**: 5 sản phẩm (trạng thái DELIVERED)
- **Hoàn trả**: 3 sản phẩm (trạng thái REFUNDED)
- **Còn được mua**: 5 - (5 - 3) = 3 sản phẩm ✅

---

### **3. 🛠️ Files Đã Được Cập Nhật**

#### **Repository Layer:**
- **`FlashSaleItemRepository.java`**
  - ✅ Enhanced query `countUserPurchasedQuantity()` để tính đúng với hoàn hàng

#### **Service Layer:**
- **`PriceValidationServiceImpl.java`**
  - ✅ Ba loại thông báo lỗi chi tiết
  - ✅ Logic validate với hoàn hàng
  
- **`CartItemServiceImpl.java`**
  - ✅ Thông báo lỗi khi thêm vào giỏ hàng
  - ✅ Xử lý case đã mua hết vs chưa mua hết
  
- **`CheckoutSessionServiceImpl.java`**
  - ✅ Validate checkout session với flash sale limits
  - ✅ Thông báo lỗi chi tiết cho từng trường hợp
  
- **`OrderServiceImpl.java`**
  - ✅ Validate khi tạo đơn hàng
  - ✅ Thông báo lỗi rõ ràng cho user

---

## 🔥 **LUỒNG HOẠT ĐỘNG**

### **Khi User Mua Sản Phẩm Flash Sale:**
1. **Check Flash Sale Stock** → Đủ hàng flash sale?
2. **Check User Purchase Limit** → Còn được mua thêm?
   - Tính số lượng đã mua thực tế (trừ đi hoàn hàng)
   - Validate với `maxPurchasePerUser`
   - **Trả về thông báo lỗi phù hợp** (Loại 1, 2, hoặc 3)
3. **Validate Price** → Giá có đúng với flash sale?

### **Khi User Hoàn Hàng:**
1. **Admin xử lý hoàn hàng** → Order status = `REFUNDED`/`PARTIALLY_REFUNDED`
2. **System tự động** trừ số lượng đã mua
3. **User có thể mua lại** với quota được khôi phục

---

## 📋 **TESTING SCENARIOS**

### **Scenario 1: User chưa mua, đặt quá giới hạn**
- **Setup**: maxPurchasePerUser = 5, user chưa mua gì
- **Action**: User đặt 7 sản phẩm
- **Expected**: `"Bạn chỉ được mua tối đa 5 sản phẩm flash sale 'Tên Sách'."`

### **Scenario 2: User đã mua một phần, đặt quá số còn lại**
- **Setup**: maxPurchasePerUser = 5, user đã mua 2
- **Action**: User đặt thêm 4 sản phẩm
- **Expected**: `"Bạn đã mua 2 sản phẩm, chỉ được mua thêm tối đa 3 sản phẩm flash sale 'Tên Sách'."`

### **Scenario 3: User đã mua hết giới hạn**
- **Setup**: maxPurchasePerUser = 5, user đã mua 5
- **Action**: User đặt thêm 1 sản phẩm
- **Expected**: `"Bạn đã mua đủ 5 sản phẩm flash sale 'Tên Sách' cho phép. Không thể mua thêm."`

### **Scenario 4: User hoàn hàng rồi mua lại**
- **Setup**: maxPurchasePerUser = 5, user đã mua 5, hoàn 3
- **Action**: User đặt thêm 2 sản phẩm
- **Expected**: ✅ Cho phép mua (còn 3 quota)

---

## 🚀 **API ENDPOINTS AFFECTED**

### **Validation APIs:**
- `POST /api/orders/validate-prices?userId={userId}`
- `POST /api/carts/items`
- `POST /api/checkout-sessions`
- `POST /api/orders`

### **Refund APIs (hoạt động sẵn):**
- `POST /api/orders/{id}/partial-refund`
- `POST /api/orders/{id}/full-refund`
- `POST /api/orders/{orderId}/request-refund`

---

## ✅ **COMPLETED FEATURES**

- ✅ **Hai loại thông báo lỗi flash sale** chi tiết và thân thiện
- ✅ **Xử lý hoàn hàng tự động** khôi phục quota mua flash sale
- ✅ **Validation toàn diện** trên tất cả APIs liên quan
- ✅ **Logic đếm chính xác** số lượng đã mua với hoàn hàng
- ✅ **User experience tốt** với thông báo rõ ràng

---

## 🎯 **BUSINESS VALUE**

1. **User Experience**: Thông báo lỗi rõ ràng, dễ hiểu
2. **Business Logic**: Xử lý đúng hoàn hàng và quota
3. **System Integrity**: Validation nhất quán trên tất cả endpoints
4. **Flexibility**: Hỗ trợ partial refund và full refund
5. **Accuracy**: Đếm đúng số lượng đã mua với các trường hợp phức tạp

**🎉 Hệ thống flash sale validation đã hoàn thiện và sẵn sàng production!**
