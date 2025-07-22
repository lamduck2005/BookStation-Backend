# 🛍️ HƯỚNG DẪN LUỒNG MUA HÀNG HOÀN CHỈNH CHO FRONTEND

## 📋 **TỔNG QUAN**

Document này hướng dẫn chi tiết **toàn bộ luồng mua hàng** từ khi user thêm sản phẩm vào giỏ hàng cho đến khi tạo đơn hàng thành công, bao gồm tất cả các API cần thiết, request/response mẫu và xử lý lỗi.

---

## 🎯 **CÁC LUỒNG MUA HÀNG CHÍNH**

### **1. LUỒNG THÊM VÀO GIỎ HÀNG**
User duyệt sản phẩm → Ấn "Thêm vào giỏ" → Chọn số lượng → Thêm thành công

### **2. LUỒNG MUA NGAY**  
User chọn sản phẩm → Ấn "Mua ngay" → Chuyển thẳng đến checkout → Thanh toán

### **3. LUỒNG CHECKOUT TỪ GIỎ HÀNG**
User vào giỏ hàng → Chọn sản phẩm → Ấn "Thanh toán" → Điền thông tin → Đặt hàng

---

## 🛒 **PHẦN 1: LUỒNG THÊM VÀO GIỎ HÀNG**

### **Bước 1: Thêm sản phẩm vào giỏ hàng**

#### **API Call:**
```http
POST /api/carts/items
Content-Type: application/json

{
  "userId": 4,
  "bookId": 3,
  "quantity": 1
}
```

#### **Success Response (200):**
```json
{
  "status": 200,
  "message": "Thêm sản phẩm vào giỏ hàng thành công 🔥 Đã áp dụng flash sale!",
  "data": {
    "id": 456,
    "cartId": 123,
    "bookId": 3,
    "bookName": "Chí Phèo",
    "bookCode": "CP001",
    "bookPrice": 36000.00,
    "flashSaleItemId": 789,
    "flashSaleName": "Flash Sale Tết",
    "flashSalePrice": 25000.00,
    "quantity": 1,
    "unitPrice": 25000.00,
    "totalPrice": 25000.00,
    "availableStock": 50,
    "isOutOfStock": false,
    "selected": true,
    "itemType": "FLASH_SALE",
    "canAddMore": true
  }
}
```

#### **Error Responses:**
```json
// Hết hàng
{
  "status": 400,
  "message": "Sách 'Chí Phèo' chỉ còn 0 cuốn trong kho",
  "data": null
}

// Số lượng vượt quá stock
{
  "status": 400,
  "message": "Bạn đã có 2 trong giỏ. Sách 'Chí Phèo' chỉ còn 3 cuốn trong kho",
  "data": null
}
```


### **Bước 2: Lấy danh sách sản phẩm trong giỏ hàng (cart items)**

#### **API Call:**
```http
GET /api/carts/items/user/4
```

#### **Success Response (200):**
```json
{
  "status": 200,
  "message": "Lấy danh sách sản phẩm thành công",
  "data": [
    {
      "id": 456,
      "cartId": 123,
      "bookId": 3,
      "bookName": "Chí Phèo",
      "bookCode": "CP001",
      "bookImageUrl": "https://example.com/image.jpg",
      "bookPrice": 36000.00,
      "flashSaleItemId": 789,
      "flashSaleName": "Flash Sale Tết",
      "flashSalePrice": 25000.00,
      "flashSaleDiscount": 30.56,
      "flashSaleEndTime": 1752689090392,
      "isFlashSaleExpired": false,
      "quantity": 1,
      "unitPrice": 25000.00,
      "totalPrice": 25000.00,
      "availableStock": 50,
      "isOutOfStock": false,
      "selected": true,
      "itemType": "FLASH_SALE",
      "canAddMore": true,
      "createdAt": 1752602690405
    }
    // ... các sản phẩm khác
  ]
}
```

---

## ⚡ **PHẦN 2: LUỒNG MUA NGAY**

### **Bước 1: Tạo checkout session trực tiếp**

#### **API Call:**
```http
POST /api/checkout-sessions
Content-Type: application/json

{
  "items": [
    {
      "bookId": 3,
      "quantity": 1
    }
  ],
  "addressId": 1
}
```
**Query Parameter:** `userId=4`

#### **Success Response (201):**
```json
{
  "status": 201,
  "message": "Tạo checkout session thành công",
  "data": {
    "id": 214,
    "userId": 4,
    "addressId": 1,
    "addressFullText": "Địa chỉ 1, Phường 1, Quận 1, TP.HCM",
    "recipientName": "Lê Văn C",
    "recipientPhone": "0123456789",
    "shippingMethod": null,
    "shippingFee": 0,
    "paymentMethod": null,
    "selectedVoucherIds": null,
    "selectedVouchers": [],
    "checkoutItems": [
      {
        "bookId": 3,
        "bookTitle": "Chí Phèo",
        "bookImage": null,
        "quantity": 1,
        "unitPrice": 25000.00,
        "originalPrice": 36000.00,
        "totalPrice": 25000.00,
        "isFlashSale": true,
        "flashSaleName": "Flash Sale Tết",
        "savings": 11000.00,
        "isOutOfStock": false,
        "availableStock": 50
      }
    ],
    "subtotal": 25000.00,
    "totalDiscount": 0,
    "totalAmount": 25000.00,
    "status": 1,
    "statusText": "Active",
    "expiresAt": 1752689090392,
    "isExpired": false,
    "isActive": true,
    "timeRemaining": 86399979,
    "createdAt": 1752602690405,
    "updatedAt": 1752602690405
  }
}
```

### **Bước 2: Cập nhật thông tin checkout session**

#### **API Call:**
```http
PUT /api/checkout-sessions/214?userId=4
Content-Type: application/json

{
  "items": [
    {
      "bookId": 3,
      "quantity": 1
    }
  ],
  "addressId": 1,
  "paymentMethod": "COD",
  "selectedVoucherIds": [1, 2],
  "notes": "Giao hàng buổi chiều"
}
```

#### **Success Response (200):**
```json
{
  "status": 200,
  "message": "Cập nhật checkout session thành công",
  "data": {
    "id": 214,
    "paymentMethod": "COD",
    "selectedVoucherIds": [1, 2],
    "selectedVouchers": [
      {
        "id": 1,
        "code": "WELCOME10",
        "discountAmount": 5000.00
      }
    ],
    "subtotal": 25000.00,
    "totalDiscount": 5000.00,
    "totalAmount": 20000.00,
    "notes": "Giao hàng buổi chiều"
  }
}
```

#### **Error Response - Thiếu items:**
```json
{
  "status": 400,
  "message": "Dữ liệu không hợp lệ",
  "data": [
    {
      "field": "items",
      "message": "Danh sách sản phẩm không được để trống",
      "rejectedValue": null
    }
  ]
}
```

### **Bước 3: Tạo đơn hàng từ session**

#### **API Call:**
```http
POST /api/checkout-sessions/214/create-order?userId=4
```

#### **Success Response (201):**
```json
{
  "status": 201,
  "message": "Đặt hàng thành công! Mã đơn hàng: ORD20250716001",
  "data": "12345"
}
```

---

## 🛍️ **PHẦN 3: LUỒNG CHECKOUT TỪ GIỎ HÀNG**

### **Bước 1: Validate giỏ hàng trước khi checkout**

#### **API Call:**
```http
POST /api/carts/user/4/validate
```

#### **Success Response (200):**
```json
{
  "status": 200,
  "message": "Cập nhật giỏ hàng thành công",
  "data": {
    "cartId": 1,
    "totalItems": 2,
    "items": [
      {
        "id": 456,
        "bookId": 3,
        "isOutOfStock": false,
        "isFlashSaleExpired": false
      }
    ]
  }
}
```

### **Bước 2: Tạo checkout session từ giỏ hàng**

#### **API Call:**
```http
POST /api/checkout-sessions/from-cart?userId=4
```

#### **Success Response (201):**
```json
{
  "status": 201,
  "message": "Tạo checkout session thành công",
  "data": {
    "id": 215,
    "userId": 4,
    "addressId": 1,
    "addressFullText": "Địa chỉ mặc định...",
    "checkoutItems": [
      {
        "bookId": 3,
        "quantity": 1,
        "isFlashSale": true,
        "unitPrice": 25000.00
      }
    ],
    "subtotal": 25000.00,
    "totalAmount": 25000.00,
    "status": 1,
    "isActive": true
  }
}
```

#### **Error Responses:**
```json
// Giỏ hàng trống
{
  "status": 400,
  "message": "Giỏ hàng trống, không thể tạo checkout session",
  "data": null
}

// Không có sản phẩm được chọn
{
  "status": 400,
  "message": "Không có sản phẩm nào được chọn để checkout",
  "data": null
}
```

### **Bước 3: Lấy checkout session mới nhất**

#### **API Call:**
```http
GET /api/checkout-sessions/latest?userId=4
```

#### **Success Response (200):**
```json
{
  "status": 200,
  "message": "Lấy checkout session mới nhất thành công",
  "data": {
    "id": 215,
    "userId": 4,
    "checkoutItems": [...],
    "subtotal": 25000.00,
    "totalAmount": 25000.00,
    "isActive": true,
    "timeRemaining": 86399979
  }
}
```

### **Bước 4: Cập nhật thông tin giao hàng và thanh toán**

#### **API Call:**
```http
PUT /api/checkout-sessions/215?userId=4
Content-Type: application/json

{
  "items": [
    {
      "bookId": 3,
      "quantity": 1
    }
  ],
  "addressId": 2,
  "shippingMethod": "EXPRESS",
  "paymentMethod": "VNPAY",
  "selectedVoucherIds": [1],
  "notes": "Giao nhanh"
}
```

### **Bước 5: Validate session trước khi đặt hàng**

#### **API Call:**
```http
POST /api/checkout-sessions/215/validate?userId=4
```

#### **Success Response (200):**
```json
{
  "status": 200,
  "message": "Validate session thành công",
  "data": {
    "id": 215,
    "isValid": true,
    "checkoutItems": [...],
    "totalAmount": 20000.00
  }
}
```

#### **Error Response - Session hết hạn:**
```json
{
  "status": 400,
  "message": "Checkout session đã hết hạn",
  "data": null
}
```

### **Bước 6: Tạo đơn hàng**

#### **API Call:**
```http
POST /api/checkout-sessions/215/create-order?userId=4
```

---

## 🚨 **PHẦN 4: XỬ LÝ LỖI CHI TIẾT**

### **Lỗi khi tạo đơn hàng:**

#### **400 - Session hết hạn:**
```json
{
  "status": 400,
  "message": "❌ Phiên checkout đã hết hạn. Vui lòng tạo phiên mới.",
  "data": null
}
```
**Xử lý:** Chuyển user về giỏ hàng, tạo session mới

#### **400 - Sản phẩm hết hàng:**
```json
{
  "status": 400,
  "message": "❌ Sách 'Chí Phèo' chỉ còn 2 cuốn trong kho",
  "data": null
}
```
**Xử lý:** Show dialog thông báo, chuyển về giỏ hàng để cập nhật

#### **400 - Flash sale kết thúc:**
```json
{
  "status": 400,
  "message": "❌ Flash sale cho sách 'Chí Phèo' đã kết thúc",
  "data": null
}
```
**Xử lý:** Tính lại giá, cập nhật session

#### **400 - Voucher hết hạn:**
```json
{
  "status": 400,
  "message": "❌ Voucher 'WELCOME10' đã hết hạn",
  "data": null
}
```
**Xử lý:** Bỏ voucher, tính lại tổng tiền

#### **400 - Giá thay đổi:**
```json
{
  "status": 400,
  "message": "❌ Giá sách 'Chí Phèo' đã thay đổi. Vui lòng cập nhật lại giỏ hàng",
  "data": null
}
```
**Xử lý:** Refresh session, hiển thị giá mới

---

## 💡 **PHẦN 5: LOGIC XỬ LÝ SAU KHI TẠO ĐƠN THÀNH CÔNG**

### **Các bước tự động được backend thực hiện:**

1. **Trừ số lượng tồn kho:** `book.stockQuantity -= quantity`
2. **Cập nhật số lượng đã bán:** `book.soldCount += quantity`
3. **Trừ flash sale stock:** `flashSaleItem.stockQuantity -= quantity`
4. **Cập nhật flash sale sold:** `flashSaleItem.soldCount += quantity`
5. **Sử dụng voucher:** `voucher.usedCount += 1`
6. **Mark session completed:** `session.status = 2`
7. **Clear giỏ hàng:** Xóa tất cả items đã checkout

---

## 🔧 **PHẦN 6: CÁC API BỔ SUNG QUAN TRỌNG**

### **1. Lấy địa chỉ mặc định:**
```http
GET /api/addresses/user/4/default
```

### **2. Lấy danh sách voucher khả dụng:**
```http
GET /api/vouchers/available?userId=4&subtotal=25000
```

### **3. Tính phí vận chuyển:**
```http
POST /api/shipping/calculate
{
  "addressId": 1,
  "items": [{"bookId": 3, "quantity": 1}]
}
```

### **4. Lấy chi tiết sách:**
```http
GET /api/books/3
```

### **5. Kiểm tra flash sale active:**
```http
GET /api/flash-sales/book/3/active
```

---

## 📱 **PHẦN 7: IMPLEMENTATION GUIDE CHO FRONTEND**

### **1. JavaScript/TypeScript Code Example:**

```javascript
class CheckoutService {
  // Thêm vào giỏ hàng
  async addToCart(userId, bookId, quantity) {
    try {
      const response = await fetch('/api/carts/items', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ userId, bookId, quantity })
      });
      
      const result = await response.json();
      
      if (result.status === 200) {
        this.showSuccessMessage(result.message);
        this.updateCartCount();
      } else {
        this.handleError(result.message);
      }
      
      return result;
    } catch (error) {
      this.handleError('Lỗi kết nối. Vui lòng thử lại.');
    }
  }

  // Mua ngay
  async buyNow(userId, bookId, quantity) {
    try {
      // 1. Tạo checkout session
      const sessionResponse = await fetch('/api/checkout-sessions', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          items: [{ bookId, quantity }]
        })
      });
      
      const sessionResult = await sessionResponse.json();
      
      if (sessionResult.status === 201) {
        // 2. Chuyển đến trang checkout
        window.location.href = `/checkout/${sessionResult.data.id}`;
      } else {
        this.handleError(sessionResult.message);
      }
      
    } catch (error) {
      this.handleError('Lỗi khi tạo phiên checkout');
    }
  }

  // Checkout từ giỏ hàng
  async checkoutFromCart(userId) {
    try {
      // 1. Validate giỏ hàng
      await this.validateCart(userId);
      
      // 2. Tạo session từ giỏ hàng
      const response = await fetch(`/api/checkout-sessions/from-cart?userId=${userId}`, {
        method: 'POST'
      });
      
      const result = await response.json();
      
      if (result.status === 201) {
        window.location.href = `/checkout/${result.data.id}`;
      } else {
        this.handleError(result.message);
      }
      
    } catch (error) {
      this.handleError('Lỗi khi tạo phiên checkout từ giỏ hàng');
    }
  }

  // Cập nhật checkout session
  async updateCheckoutSession(sessionId, userId, updateData) {
    try {
      const response = await fetch(`/api/checkout-sessions/${sessionId}?userId=${userId}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(updateData)
      });
      
      const result = await response.json();
      
      if (result.status === 200) {
        return result.data;
      } else {
        throw new Error(result.message);
      }
      
    } catch (error) {
      this.handleError(error.message);
    }
  }

  // Tạo đơn hàng
  async createOrder(sessionId, userId) {
    try {
      this.showLoading('Đang xử lý đơn hàng...');
      
      // 1. Validate session trước
      const validateResult = await this.validateSession(sessionId, userId);
      if (validateResult.status !== 200) {
        throw new Error(validateResult.message);
      }
      
      // 2. Tạo đơn hàng
      const response = await fetch(`/api/checkout-sessions/${sessionId}/create-order?userId=${userId}`, {
        method: 'POST'
      });
      
      const result = await response.json();
      
      this.hideLoading();
      
      if (result.status === 201) {
        // Thành công - chuyển đến trang thành công
        window.location.href = `/order-success/${result.data}`;
      } else {
        this.handleOrderError(result.message, sessionId, userId);
      }
      
    } catch (error) {
      this.hideLoading();
      this.handleError(error.message);
    }
  }

  // Xử lý lỗi khi tạo đơn hàng
  handleOrderError(errorMessage, sessionId, userId) {
    if (errorMessage.includes('hết hạn')) {
      this.showDialog({
        title: 'Phiên hết hạn',
        message: 'Phiên checkout đã hết hạn. Chúng tôi sẽ tạo phiên mới cho bạn.',
        onConfirm: () => this.checkoutFromCart(userId)
      });
    } else if (errorMessage.includes('hết hàng')) {
      this.showDialog({
        title: 'Sản phẩm hết hàng',
        message: errorMessage + '\n\nBạn sẽ được chuyển về giỏ hàng để cập nhật.',
        onConfirm: () => window.location.href = '/cart'
      });
    } else if (errorMessage.includes('thay đổi')) {
      this.showDialog({
        title: 'Giá thay đổi',
        message: errorMessage + '\n\nVui lòng kiểm tra lại đơn hàng.',
        onConfirm: () => this.refreshSession(sessionId, userId)
      });
    } else {
      this.showError(errorMessage);
    }
  }

  // Validate session
  async validateSession(sessionId, userId) {
    const response = await fetch(`/api/checkout-sessions/${sessionId}/validate?userId=${userId}`, {
      method: 'POST'
    });
    return await response.json();
  }

  // Helper methods
  showSuccessMessage(message) { /* Toast success */ }
  showError(message) { /* Toast error */ }
  showLoading(message) { /* Show loading spinner */ }
  hideLoading() { /* Hide loading spinner */ }
  showDialog(options) { /* Show confirmation dialog */ }
  updateCartCount() { /* Update cart item count in header */ }
  handleError(message) { /* Generic error handler */ }
}
```

### **2. React Hook Example:**

```javascript
const useCheckout = () => {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  
  const addToCart = useCallback(async (userId, bookId, quantity) => {
    setLoading(true);
    setError(null);
    
    try {
      const result = await checkoutService.addToCart(userId, bookId, quantity);
      return result;
    } catch (err) {
      setError(err.message);
      return null;
    } finally {
      setLoading(false);
    }
  }, []);

  const createOrder = useCallback(async (sessionId, userId) => {
    setLoading(true);
    setError(null);
    
    try {
      const result = await checkoutService.createOrder(sessionId, userId);
      return result;
    } catch (err) {
      setError(err.message);
      return null;
    } finally {
      setLoading(false);
    }
  }, []);

  return {
    loading,
    error,
    addToCart,
    createOrder,
    clearError: () => setError(null)
  };
};
```

---

## ⚠️ **PHẦN 8: LƯU Ý QUAN TRỌNG**

### **1. Validation Request Body:**
- **LUÔN LUÔN** gửi trường `items` trong PUT checkout session
- Trường `items` phải chứa ít nhất 1 sản phẩm
- Mỗi item phải có `bookId` và `quantity` hợp lệ

### **2. Xử lý địa chỉ:**
- Nếu không truyền `addressId`, backend sẽ dùng địa chỉ mặc định
- Luôn kiểm tra user có địa chỉ mặc định trước khi checkout

### **3. Session Management:**
- Session có thời hạn 24 giờ
- Kiểm tra `isExpired` trước khi thao tác
- Có thể gia hạn session bằng API extend

### **4. Auto Detection:**
- Backend tự động detect flash sale cho sản phẩm
- Không cần frontend truyền `flashSaleItemId`

### **5. Error Handling:**
- Phân loại lỗi theo status code
- Hiển thị message cụ thể cho user
- Redirect phù hợp theo từng loại lỗi

---

## 📊 **PHẦN 9: TESTING CHECKLIST**

### **Test Cases cho Thêm vào giỏ hàng:**
- ✅ Thêm sản phẩm bình thường
- ✅ Thêm sản phẩm có flash sale
- ✅ Thêm vượt quá số lượng tồn
- ✅ Thêm sản phẩm hết hàng
- ✅ Thêm sản phẩm đã có trong giỏ

### **Test Cases cho Checkout:**
- ✅ Tạo session từ 1 sản phẩm (mua ngay)
- ✅ Tạo session từ giỏ hàng nhiều sản phẩm
- ✅ Cập nhật session với địa chỉ, voucher
- ✅ Tạo đơn hàng thành công
- ✅ Xử lý session hết hạn
- ✅ Xử lý sản phẩm hết hàng khi đặt
- ✅ Xử lý flash sale kết thúc khi đặt
- ✅ Xử lý voucher hết hạn khi đặt

### **Test Cases cho Error Handling:**
- ✅ Network error handling
- ✅ Session timeout handling  
- ✅ Validation error display
- ✅ Redirect after error

---

*📝 Document này cover đầy đủ luồng mua hàng cho frontend. Update khi có thay đổi API hoặc business logic.*
