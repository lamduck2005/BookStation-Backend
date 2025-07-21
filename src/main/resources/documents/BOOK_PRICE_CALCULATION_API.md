# 🔧 API TÍN GIÁ SÁCH CHO FRONTEND

## **API Endpoint**
```
POST /api/books/calculate-price
```

## **Mô tả**
API này giúp Frontend tính giá sách ngay lập tức khi admin nhập discount (theo số tiền hoặc phần trăm), đồng thời hiển thị thông tin flash sale nếu có.

## **Request Body**
```json
{
  "bookId": 123,
  "discountValue": 50000,      // Giảm giá theo số tiền (VND) - tùy chọn
  "discountPercent": 10,       // Giảm giá theo % - tùy chọn  
  "discountActive": true       // Có áp dụng discount hay không
}
```

## **Response**
```json
{
  "status": 200,
  "message": "Tính giá thành công",
  "data": {
    "bookId": 123,
    "bookName": "Tên sách",
    "originalPrice": 200000,           // Giá gốc
    "finalPrice": 150000,              // Giá cuối cùng sau discount
    "discountAmount": 50000,           // Số tiền được giảm
    "discountPercent": 25,             // % giảm giá thực tế
    "hasDiscount": true,               // Có áp dụng discount
    "discountType": "VALUE",           // "VALUE" hoặc "PERCENT"
    
    // Thông tin Flash Sale (nếu có)
    "hasFlashSale": true,              // Có flash sale đang active
    "flashSalePrice": 120000,          // Giá flash sale
    "flashSavings": 80000,             // Tiền tiết kiệm từ flash sale
    "flashSaleName": "Flash Sale 50%"  // Tên flash sale
  }
}
```

## **Logic Tính Toán**

### **1. Discount theo số tiền:**
```
finalPrice = originalPrice - discountValue
discountPercent = (discountValue / originalPrice) * 100
```

### **2. Discount theo phần trăm:**
```
discountAmount = originalPrice * (discountPercent / 100)
finalPrice = originalPrice - discountAmount
```

### **3. So sánh với Flash Sale:**
- Nếu có flash sale đang active → hiển thị cả 2 giá
- Frontend có thể so sánh `finalPrice` vs `flashSalePrice` để suggest giá tốt nhất

## **Use Cases cho Frontend**

### **1. Nhập discount theo số tiền**
```javascript
const request = {
  bookId: 123,
  discountValue: 50000,
  discountActive: true
};

// Response: finalPrice = 150000, discountPercent = 25%
```

### **2. Nhập discount theo phần trăm**
```javascript
const request = {
  bookId: 123,
  discountPercent: 15,
  discountActive: true
};

// Response: finalPrice = 170000, discountAmount = 30000
```

### **3. Không áp dụng discount**
```javascript
const request = {
  bookId: 123,
  discountActive: false
};

// Response: finalPrice = originalPrice, hasDiscount = false
```

## **Validation Rules**
- `bookId` bắt buộc
- `discountValue` >= 0
- `discountPercent` >= 0 và <= 100
- Nếu `discountActive = false` → bỏ qua discount
- Không thể set cả `discountValue` và `discountPercent` cùng lúc (chọn 1 trong 2)

## **Error Cases**
```json
// Book không tồn tại
{
  "status": 404,
  "message": "Không tìm thấy sách",
  "data": null
}

// Validation lỗi
{
  "status": 400,
  "message": "Discount value phải >= 0",
  "data": null
}
```

---

# 🚨 CÁC LỖI ĐÃ SỬA TRONG ORDER MANAGEMENT

## **1. Lỗi không trừ tồn kho Book thông thường**
**❌ Trước:** Chỉ trừ tồn kho FlashSaleItem, bỏ qua Book thông thường
**✅ Sau:** Trừ tồn kho cho cả Book và FlashSaleItem

## **2. Lỗi không cộng soldCount cho Book**
**❌ Trước:** Chỉ cộng soldCount cho FlashSaleItem
**✅ Sau:** Cộng soldCount cho cả Book và FlashSaleItem

## **3. Lỗi không restore stock khi cancel/refund**
**❌ Trước:** Chỉ restore stock cho FlashSaleItem
**✅ Sau:** Restore stock cho cả Book và FlashSaleItem

## **4. Logic tích điểm khi DELIVERED**
**✅ Cải thiện:** Tích điểm ngay khi order status = DELIVERED (không chỉ từ SHIPPED)
**✅ An toàn:** Có kiểm tra để không tích điểm 2 lần

---

## **API TEST CASES**

### **Test 1: Tính giá với discount theo số tiền**
```bash
POST /api/books/calculate-price
{
  "bookId": 1,
  "discountValue": 30000,
  "discountActive": true
}
```

### **Test 2: Tính giá với discount theo %**
```bash
POST /api/books/calculate-price
{
  "bookId": 1,
  "discountPercent": 20,
  "discountActive": true
}
```

### **Test 3: Không áp dụng discount**
```bash
POST /api/books/calculate-price
{
  "bookId": 1,
  "discountActive": false
}
```
