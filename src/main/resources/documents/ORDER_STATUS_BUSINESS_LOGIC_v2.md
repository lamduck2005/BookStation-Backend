# 📦 LOGIC NGHIỆP VỤ ĐƠN HÀNG - PHIÊN BẢN 2.0

## 🎯 **CHÍNH SÁCH MỚI: SOLD COUNT CHỈ TĂNG KHI GIAO THÀNH CÔNG**

### **📊 Nguyên Tắc Quản Lý Stock và Sold Count:**

1. **Khi tạo đơn hàng** → CHỈ TRỪ STOCK, CHƯA CỘNG SOLD COUNT
2. **Khi DELIVERED** → CHÍNH THỨC CỘNG SOLD COUNT  
3. **Khi DELIVERY_FAILED/CANCELED** → CHỈ KHÔI PHỤC STOCK
4. **Khi REFUND (đã delivered)** → KHÔI PHỤC STOCK + TRỪ SOLD COUNT

---

## 🔄 **CHI TIẾT LOGIC THEO TỪNG TRẠNG THÁI**

### **1. PENDING → CONFIRMED**
- ✅ **Stock**: Đã trừ khi tạo đơn
- ✅ **Sold Count**: Chưa cộng (vẫn = 0)
- ✅ **Action**: Chỉ cập nhật trạng thái

### **2. CONFIRMED → SHIPPED** 
- ✅ **Stock**: Không đổi
- ✅ **Sold Count**: Chưa cộng (vẫn = 0)
- ✅ **Action**: Chỉ cập nhật trạng thái

### **3. SHIPPED → DELIVERED** ⭐
- ✅ **Stock**: Không đổi
- 🔥 **Sold Count**: **CHÍNH THỨC CỘNG** (cả Book và FlashSaleItem)
- ✅ **Points**: Tích điểm cho user
- ✅ **Action**: `handleDeliveredBusinessLogic()`

### **4. SHIPPED → DELIVERY_FAILED** ❌
- 🔄 **Stock**: **KHÔI PHỤC** (+quantity)
- ✅ **Sold Count**: Không đổi (vẫn = 0, chưa cộng)
- ✅ **Action**: `handleDeliveryFailedBusinessLogic()`

### **5. ANY → CANCELED** ❌
- 🔄 **Stock**: **KHÔI PHỤC** (+quantity)  
- ✅ **Sold Count**: Không đổi (vẫn = 0, chưa cộng)
- ✅ **Action**: `handleCancellationBusinessLogic()`

---

## 🔄 **LOGIC HOÀN TRẢ/HOÀN TIỀN**

### **Partial Refund (`PARTIALLY_REFUNDED`)**
```java
boolean wasDelivered = order.getOrderStatus() == OrderStatus.DELIVERED || 
                       order.getOrderStatus() == OrderStatus.REFUNDING ||
                       order.getOrderStatus() == OrderStatus.REFUNDED ||
                       order.getOrderStatus() == OrderStatus.PARTIALLY_REFUNDED;

// ✅ Luôn khôi phục stock
flashSaleItem.setStockQuantity(flashSaleItem.getStockQuantity() + refundQuantity);

// ✅ CHỈ trừ sold count nếu đơn hàng đã DELIVERED
if (wasDelivered) {
    flashSaleItem.setSoldCount(flashSaleItem.getSoldCount() - refundQuantity);
}
```

### **Full Refund (`REFUNDED`)**
- Tương tự Partial Refund
- Áp dụng cho toàn bộ orderDetails

---

## 📋 **BẢNG TÓNG TẮT CÁC TRẠNG THÁI**

| Trạng Thái | Mô Tả | Stock Action | Sold Count Action |
|------------|-------|--------------|-------------------|
| `PENDING` | Chờ xử lý | ✅ Đã trừ khi tạo đơn | ❌ Chưa cộng |
| `CONFIRMED` | Đã xác nhận | ➖ Không đổi | ❌ Chưa cộng |
| `SHIPPED` | Đang giao hàng | ➖ Không đổi | ❌ Chưa cộng |
| `DELIVERED` | **Đã giao thành công** | ➖ Không đổi | ✅ **CHÍNH THỨC CỘNG** |
| `DELIVERY_FAILED` | **Giao hàng thất bại** | 🔄 **Khôi phục** | ➖ Không đổi |
| `CANCELED` | Đã hủy | 🔄 **Khôi phục** | ➖ Không đổi |
| `REFUND_REQUESTED` | Yêu cầu hoàn trả | ➖ Không đổi | ➖ Không đổi |
| `REFUNDING` | Đang hoàn tiền | 🔄 **Khôi phục** | 🔄 **Trừ** (nếu đã delivered) |
| `REFUNDED` | Đã hoàn tiền | 🔄 **Khôi phục** | 🔄 **Trừ** (nếu đã delivered) |
| `RETURNED` | Đã trả hàng về kho | 🔄 **Khôi phục** | 🔄 **Trừ** (nếu đã delivered) |
| `PARTIALLY_REFUNDED` | Hoàn tiền một phần | 🔄 **Khôi phục một phần** | 🔄 **Trừ một phần** (nếu đã delivered) |

---

## 🔧 **PHÂN BIỆT CÁC LOẠI HOÀN TRẢ**

### **1. HOÀN TIỀN (`REFUNDED`)**
- Khách hàng nhận lại tiền
- Stock được khôi phục vào kho
- Sản phẩm KHÔNG trả về vật lý

### **2. HOÀN HÀNG (`RETURNED`)**  
- Khách hàng trả sản phẩm về kho vật lý
- Stock được khôi phục
- Admin kiểm tra chất lượng sản phẩm

### **3. HOÀN TIỀN MỘT PHẦN (`PARTIALLY_REFUNDED`)**
- Chỉ hoàn một số sản phẩm trong đơn hàng
- Stock chỉ khôi phục cho sản phẩm được hoàn

---

## 🎯 **LỢI ÍCH CỦA LOGIC MỚI**

1. **📊 Báo cáo chính xác**: Sold count phản ánh đúng số lượng đã bán thành công
2. **🔄 Quản lý stock tốt hơn**: Khôi phục stock ngay khi giao hàng thất bại
3. **💰 Tránh gian lận**: Không tính sold count khi chưa giao thành công
4. **📈 Analytics đúng**: KPI bán hàng dựa trên đơn hàng thực sự completed
5. **🚚 Xử lý delivery failure**: Có cơ chế xử lý khi giao hàng thất bại

---

## 🚨 **BREAKING CHANGES**

### **Migration Cần Thiết:**
1. **Database**: Thêm trạng thái `DELIVERY_FAILED`
2. **Frontend**: Hiển thị trạng thái mới
3. **Analytics**: Cập nhật công thức tính sold count
4. **Reports**: Báo cáo bán hàng dựa trên DELIVERED, không phải ORDER_CREATED

### **API Impact:**
- ✅ `POST /api/orders/{id}/status?newStatus=DELIVERY_FAILED` (mới)
- ✅ `GET /api/orders/order-statuses` (cập nhật danh sách)
- ✅ Logic refund được cải thiện

---

## 📞 **LIÊN HỆ**
- **Dev Team**: Cập nhật frontend để hiển thị trạng thái mới
- **Business Team**: Review logic nghiệp vụ phù hợp với yêu cầu
- **Analytics Team**: Cập nhật dashboard theo logic mới
