# 🔄 ORDER TYPE ENUM CHANGES

## 📋 **THAY ĐỔI QUAN TRỌNG**

### **Trước đây:**
```
"ONLINE"     - Đơn hàng trực tuyến
"TẠI QUẦY"   - Đơn hàng tại quầy
```

### **Sau khi thay đổi:**
```
"ONLINE"     - Đơn hàng trực tuyến  
"COUNTER"    - Đơn hàng tại quầy
```

---

## 🎯 **CÁC FILE ĐÃ SỬA**

1. **OrderStatusTransitionServiceImpl.java**
   - `validateSpecialBusinessRules()` - Validation logic
   - `isValidOrderType()` - Order type check

2. **OrderServiceImpl.java**  
   - Order creation validation
   - Order update validation

3. **OrderController.java**
   - Response enum options

---

## 📝 **FRONTEND PAYLOAD CHANGES**

### **Tạo đơn hàng mới:**
```json
{
  "orderType": "COUNTER",  // ✅ Thay đổi từ "TẠI QUẦY" 
  "userId": 1,
  "addressId": 1,
  "items": [...],
  "staffId": 5             // ⚠️ REQUIRED cho đơn COUNTER
}
```

### **Chuyển trạng thái đơn hàng:**
```json
{
  "orderId": 123,
  "currentStatus": "PENDING",
  "newStatus": "CONFIRMED", 
  "performedBy": "Admin",
  "staffId": 5              // ⚠️ REQUIRED khi confirm đơn COUNTER
}
```

---

## ⚠️ **QUY TẮC NGHIỆP VỤ**

### **Đơn hàng COUNTER:**
- **Tạo đơn:** Có thể không cần `staffId`
- **Confirm đơn:** **BẮT BUỘC** phải có `staffId`  
- **Lý do:** Đơn tại quầy phải có nhân viên xác nhận

### **Đơn hàng ONLINE:**
- Không cần `staffId` trong mọi trường hợp
- Có thể tự động confirm hoặc manual confirm

---

## 🚨 **ERROR RESPONSES**

```json
{
  "status": 400,
  "message": "Kiểu đơn hàng chỉ được phép là 'ONLINE' hoặc 'COUNTER'",
  "data": null
}
```

```json
{
  "status": 400, 
  "message": "Đơn hàng tại quầy phải có nhân viên xác nhận",
  "data": null
}
```

---

## 💡 **MIGRATION GUIDE CHO FRONTEND**

### **1. Update Order Creation:**
```javascript
// ❌ Cũ
const orderData = {
  orderType: "TẠI QUẦY"
};

// ✅ Mới  
const orderData = {
  orderType: "COUNTER",
  staffId: currentStaff.id  // Required for counter orders
};
```

### **2. Update Order Status Transition:**
```javascript
// ✅ Confirm đơn COUNTER
const transitionData = {
  orderId: 123,
  currentStatus: "PENDING", 
  newStatus: "CONFIRMED",
  performedBy: "Staff Name",
  staffId: currentStaff.id  // Required for COUNTER orders
};
```

### **3. Update UI Labels:**
```javascript
const orderTypeOptions = [
  { value: "ONLINE", label: "Đơn hàng trực tuyến" },
  { value: "COUNTER", label: "Đơn hàng tại quầy" }  // ✅ Updated
];
```

---

## ✅ **CHECKLIST CHO FRONTEND**

- [ ] Thay đổi "TẠI QUẦY" → "COUNTER" trong forms
- [ ] Add `staffId` validation cho đơn COUNTER
- [ ] Update UI labels và options
- [ ] Test order creation với type COUNTER
- [ ] Test order status transition với staffId
- [ ] Update error handling cho new validation rules

**🎯 Tất cả đơn hàng cũ với type "TẠI QUẦY" vẫn hoạt động, nhưng đơn mới phải dùng "COUNTER"!**
