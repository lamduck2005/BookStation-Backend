# 🛒 CHECKOUT SESSION API - HƯỚNG DẪN FRONTEND

## 📌 **TỔNG QUAN**

Checkout Session là hệ thống lưu trạng thái giữa **Cart** và **Order**, giúp user có thể:
- Lưu thông tin checkout khi bị gián đoạn (đóng app, mất mạng, v.v.)
- Chọn địa chỉ, voucher, phương thức thanh toán
- Validate toàn bộ đơn hàng trước khi đặt
- Tạo đơn hàng với tất cả business logic phức tạp

---

## 🎯 **LUỒNG SỬ DỤNG**

```
1. User thêm sản phẩm vào Cart
2. User nhấn "Thanh toán" → Tạo Checkout Session từ Cart
3. User chọn địa chỉ, voucher, phương thức thanh toán → Update Session  
4. User xem review đơn hàng → Validate Session
5. User nhấn "Đặt hàng" → Create Order từ Session
6. Session tự động completed, Order được tạo thành công
```

---

## 🚀 **API ENDPOINTS**

### **1. Tạo Session từ Cart**
```http
POST /api/checkout-sessions/from-cart?userId={userId}
```

**Request:**
```json
// Không cần body - tự động lấy từ cart
```

**Response:**
```json
{
  "status": 201,
  "message": "Tạo checkout session thành công",
  "data": {
    "id": 123,
    "userId": 456,
    "address": null,
    "shippingMethod": null,
    "shippingFee": 0,
    "estimatedDeliveryFrom": null,
    "estimatedDeliveryTo": null,
    "paymentMethod": null,
    "selectedVouchers": [],
    "checkoutItems": [
      {
        "bookId": 789,
        "bookName": "Clean Code",
        "bookImageUrl": "https://...",
        "quantity": 2,
        "unitPrice": 150000,
        "totalPrice": 300000,
        "isFlashSale": true,
        "flashSaleItemId": 101,
        "flashSalePrice": 120000,
        "flashSaleEndTime": 1720684800000
      }
    ],
    "subtotal": 300000,
    "totalDiscount": 0,
    "totalAmount": 300000,
    "status": 1,
    "expiresAt": 1720771200000,
    "createdAt": 1720684800000,
    "isExpired": false,
    "isActive": true
  }
}
```

### **2. Cập nhật Session (Chọn địa chỉ, voucher, v.v.)**
```http
PUT /api/checkout-sessions/{sessionId}
```

**Request:**
```json
{
  "userId": 456,
  "addressId": 789,
  "shippingMethod": "FAST_DELIVERY",
  "shippingFee": 25000,
  "estimatedDeliveryFrom": 1720771200000,
  "estimatedDeliveryTo": 1720857600000,
  "paymentMethod": "VNPAY",
  "selectedVoucherIds": [101, 102],
  "checkoutItems": [
    {
      "bookId": 789,
      "quantity": 2,
      "unitPrice": 120000,
      "totalPrice": 240000,
      "isFlashSale": true,
      "flashSaleItemId": 101
    }
  ],
  "notes": "Giao hàng trong giờ hành chính"
}
```

**Response:**
```json
{
  "status": 200,
  "message": "Cập nhật checkout session thành công",
  "data": {
    "id": 123,
    "userId": 456,
    "address": {
      "id": 789,
      "recipientName": "Nguyễn Văn A",
      "phoneNumber": "0123456789",
      "addressDetail": "123 Nguyễn Trãi, Q.1, TP.HCM"
    },
    "shippingMethod": "FAST_DELIVERY",
    "shippingFee": 25000,
    "estimatedDeliveryTime": "Từ 12/07 đến 13/07",
    "paymentMethod": "VNPAY",
    "selectedVouchers": [
      {
        "id": 101,
        "code": "WELCOME10",
        "name": "Giảm 10% đơn đầu",
        "discountAmount": 24000,
        "isValid": true
      }
    ],
    "subtotal": 240000,
    "totalDiscount": 24000,
    "totalAmount": 241000,
    "isActive": true
  }
}
```

### **3. Lấy Session mới nhất**
```http
GET /api/checkout-sessions/latest?userId={userId}
```

**Response:** Giống như response của Update Session

### **4. Validate Session (Trước khi đặt hàng)**
```http
POST /api/checkout-sessions/{sessionId}/validate?userId={userId}
```

**Response thành công:**
```json
{
  "status": 200,
  "message": "Validate session thành công",
  "data": {
    // ... session data
    "validationStatus": {
      "isValid": true,
      "errors": []
    }
  }
}
```

**Response có lỗi:**
```json
{
  "status": 400,
  "message": "❌ Có lỗi khi kiểm tra đơn hàng: Flash sale cho sách 'Clean Code' đã kết thúc; Voucher 'WELCOME10' đã hết hạn",
  "data": null
}
```

### **5. Tạo đơn hàng từ Session**
```http
POST /api/checkout-sessions/{sessionId}/create-order?userId={userId}
```

**Response thành công:**
```json
{
  "status": 201,
  "message": "Đặt hàng thành công! Mã đơn hàng: ORD20250711001",
  "data": 12345
}
```

**Response có lỗi:**
```json
{
  "status": 400,
  "message": "❌ Một số sản phẩm đã hết hàng. Vui lòng cập nhật lại giỏ hàng.",
  "data": null
}
```

### **6. Gia hạn Session**
```http
PATCH /api/checkout-sessions/{sessionId}/extend?userId={userId}&additionalMinutes=30
```

### **7. Tính lại giá**
```http
PATCH /api/checkout-sessions/{sessionId}/recalculate?userId={userId}
```

---

## ⚠️ **CÁC TRƯỜNG HỢP LỖI THƯỜNG GẶP**

### **400 - Bad Request**
- Session đã hết hạn
- Sản phẩm hết hàng
- Flash sale đã kết thúc
- Voucher không hợp lệ
- Giá sản phẩm thay đổi

### **404 - Not Found**
- Session không tồn tại
- User không có quyền truy cập session

### **500 - Internal Server Error**
- Lỗi hệ thống
- Lỗi kết nối database

---

## 💡 **BEST PRACTICES CHO FRONTEND**

### **1. Polling Session Status**
```javascript
// Kiểm tra session mỗi 30 giây
setInterval(async () => {
  const session = await getLatestSession(userId);
  if (session.isExpired) {
    showSessionExpiredDialog();
  }
}, 30000);
```

### **2. Auto-save khi User thay đổi**
```javascript
// Debounce update session khi user chọn voucher
const debouncedUpdateSession = debounce(async (sessionData) => {
  await updateSession(sessionId, sessionData);
}, 1000);
```

### **3. Validate trước khi đặt hàng**
```javascript
const handleOrderSubmit = async () => {
  // 1. Validate session trước
  const validateResult = await validateSession(sessionId, userId);
  
  if (validateResult.status !== 200) {
    showError(validateResult.message);
    return;
  }
  
  // 2. Tạo order
  const orderResult = await createOrderFromSession(sessionId, userId);
  
  if (orderResult.status === 201) {
    navigateToOrderSuccess(orderResult.data);
  } else {
    showError(orderResult.message);
  }
};
```

### **4. Handle Session Expiry**
```javascript
const checkSessionExpiry = (session) => {
  if (session.isExpired) {
    // Tự động tạo session mới từ cart
    return createSessionFromCart(userId);
  }
  return session;
};
```

---

## 🎯 **INTEGRATION EXAMPLES**

### **React Hook Example**
```javascript
const useCheckoutSession = (userId) => {
  const [session, setSession] = useState(null);
  const [loading, setLoading] = useState(false);
  
  const createFromCart = async () => {
    setLoading(true);
    try {
      const response = await api.post(`/checkout-sessions/from-cart?userId=${userId}`);
      setSession(response.data.data);
      return response.data.data;
    } catch (error) {
      console.error('Error creating session:', error);
      throw error;
    } finally {
      setLoading(false);
    }
  };
  
  const updateSession = async (sessionData) => {
    setLoading(true);
    try {
      const response = await api.put(`/checkout-sessions/${session.id}`, sessionData);
      setSession(response.data.data);
      return response.data.data;
    } catch (error) {
      console.error('Error updating session:', error);
      throw error;
    } finally {
      setLoading(false);
    }
  };
  
  const createOrder = async () => {
    setLoading(true);
    try {
      const response = await api.post(`/checkout-sessions/${session.id}/create-order?userId=${userId}`);
      return response.data.data;
    } catch (error) {
      console.error('Error creating order:', error);
      throw error;
    } finally {
      setLoading(false);
    }
  };
  
  return { session, loading, createFromCart, updateSession, createOrder };
};
```

### **Vue.js Composable Example**
```javascript
export const useCheckoutSession = () => {
  const session = ref(null);
  const loading = ref(false);
  
  const createSessionFromCart = async (userId) => {
    loading.value = true;
    try {
      const { data } = await $fetch(`/api/checkout-sessions/from-cart?userId=${userId}`, {
        method: 'POST'
      });
      session.value = data.data;
      return data.data;
    } finally {
      loading.value = false;
    }
  };
  
  return { session, loading, createSessionFromCart };
};
```

---

## 🔐 **SECURITY NOTES**

1. **User Validation**: Tất cả API đều require `userId` parameter để đảm bảo user chỉ truy cập được session của mình
2. **Session Expiry**: Session tự động hết hạn sau 24h để tránh chiếm dụng storage
3. **Data Validation**: Tất cả input đều được validate ở backend
4. **Race Condition**: Order creation được lock để tránh duplicate orders

---

## 📊 **MONITORING & DEBUGGING**

### **Logs cần theo dõi:**
- Session creation/update success rate
- Session expiry rate  
- Order creation success rate từ session
- Validation error patterns

### **Metrics quan trọng:**
- Average session lifetime
- Session to order conversion rate
- Most common validation errors

---

*📝 Document này được cập nhật theo version API hiện tại. Vui lòng check API changelog khi có updates.*
