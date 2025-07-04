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

## 🎯 **BUSINESS SCENARIOS**

### **Scenario 1: Admin Emergency Disable**
```
Flash Sale: status = 0, endTime = future
Flash Sale Items: status = 0 (forced by admin)
Cart Items: Show regular price
```

### **Scenario 2: Flash Sale Active & Time Valid**
```
Flash Sale: status = 1, startTime <= now <= endTime
Flash Sale Items: status = 1 (active)
Cart Items: Show flash sale price
```

### **Scenario 3: Flash Sale Enabled But Not Started**
```
Flash Sale: status = 1, startTime > now
Flash Sale Items: status = 0 (not started)
Cart Items: Show regular price
```

### **Scenario 4: Flash Sale Enabled But Expired**
```
Flash Sale: status = 1, endTime < now
Flash Sale Items: status = 0 (expired)
Cart Items: Show regular price
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

### **API Response When Admin Disables Flash Sale:**
```json
{
  "flashSaleItemId": 123,     // ✅ Always present
  "flashSalePrice": null,     // ❌ Hidden when disabled
  "flashSaleName": null,      // ❌ Hidden when disabled
  "itemType": "REGULAR",      // ✅ Shows as regular
  "unitPrice": 85000          // ✅ Regular price
}
```

### **Frontend Display Logic:**
```javascript
if (item.itemType === "REGULAR") {
    if (item.flashSaleItemId) {
        // Show "Flash sale temporarily unavailable"
        showFlashSaleUnavailable();
    } else {
        // Show normal regular item
        showRegularItem();
    }
}
```

---

## 🚀 **TRIGGER POINTS**

### **When Status Update Happens:**
1. **Admin updates flash sale** → `autoUpdateFlashSaleItemsStatus(flashSaleId)`
2. **Admin toggles flash sale status** → `autoUpdateFlashSaleItemsStatus(flashSaleId)`
3. **Flash sale expires** → Scheduler calls `autoUpdateFlashSaleItemsStatus(flashSaleId)`

### **What NEVER Happens:**
- ❌ Set `flashSaleItemId = null` in cart items
- ❌ Lose data relationship
- ❌ Scheduled tasks every 30 seconds

---

## ✅ **BENEFITS**

1. **Admin Control**: Tắt flash sale khẩn cấp bất cứ lúc nào
2. **Data Integrity**: Luôn giữ mối quan hệ cart ↔ flash sale
3. **Performance**: Chỉ update khi cần thiết
4. **User Experience**: Smooth transition giữa flash sale và regular price
5. **Business Logic**: Clear priority rules, dễ hiểu và maintain

---

**Tóm lại**: Admin có quyền tối cao, thời gian chỉ được kiểm tra khi admin cho phép (status = 1).
