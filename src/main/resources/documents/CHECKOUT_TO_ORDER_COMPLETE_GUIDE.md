# 📋 HƯỚNG DẪN HOÀN CHỈNH: LUỒNG ĐẶT HÀNG TỪ CHECKOUT SESSION

## 🚨 **CẬP NHẬT MỚI NHẤT**
**Tài liệu này đã được cập nhật hoàn toàn với đầy đủ tất cả các trường dữ liệu!**

👉 **VUI LÒNG XEM TÀI LIỆU MỚI TẠI:** `CHECKOUT_TO_ORDER_COMPLETE_GUIDE_FULL.md`

## 📖 TÓOM TẮT CẬP NHẬT

### ✅ **Đã Cập Nhật CheckoutSessionRequest** 
Thêm tất cả các trường có thể cập nhật:
- `addressId` - ID địa chỉ giao hàng
- `shippingMethod` - Phương thức vận chuyển  
- `estimatedDeliveryFrom/To` - Thời gian giao hàng dự kiến
- `paymentMethod` - Phương thức thanh toán
- `selectedVoucherIds` - Danh sách voucher được chọn
- `notes` - Ghi chú đơn hàng

### ✅ **Đã Cập Nhật Mapper Logic**
CheckoutSessionMapper giờ hỗ trợ cập nhật tất cả các trường:
```java
// Cập nhật địa chỉ, shipping, payment, vouchers, notes
// Tự động tính lại giá khi có thay đổi
```

### ✅ **Tài Liệu API Hoàn Chỉnh**
- Request/Response format đầy đủ
- Ví dụ JavaScript integration hoàn chỉnh
- Xử lý edge cases chi tiết
- Business logic và validation rules

## 🎯 **ĐIỂM QUAN TRỌNG**

1. **Tất cả trường optional:** Chỉ gửi trường nào cần cập nhật
2. **Auto-calculation:** Hệ thống tự tính lại giá
3. **Default address:** Tự động attach nếu không có
4. **Edge case handling:** Xử lý flash sale, voucher, inventory
5. **Frontend ready:** Có sẵn class JavaScript để tích hợp

---
## 📚 TÀI LIỆU CŨ (Deprecated)

---

## 📊 BƯỚC 1: TẠO CHECKOUT SESSION

### 🔗 API: Tạo Session từ Cart
```http
POST /api/checkout-sessions/from-cart?userId={userId}
Content-Type: application/json
```

**Request:** Không cần body, hệ thống tự lấy từ cart.

**Lưu ý:** Khi tạo session, backend sẽ **tự động gán địa chỉ mặc định của user** vào trường `address` nếu có. Nếu user chưa có địa chỉ mặc định, trường `address` sẽ là `null` và frontend cần yêu cầu user chọn địa chỉ trước khi đặt hàng.

**Ví dụ:**
- Nếu user đã có địa chỉ mặc định:
```json
{
  "status": 201,
  "message": "Tạo checkout session thành công",
  "data": {
    "id": 123,
    "address": {
      "id": 5,
      "fullName": "Nguyễn Văn A",
      "phoneNumber": "0123456789",
      "addressLine": "123 Đường ABC"
    },
    // ...existing fields...
  }
}
```
- Nếu user chưa có địa chỉ mặc định:
```json
{
  "status": 201,
  "message": "Tạo checkout session thành công",
  "data": {
    "id": 123,
    "address": null,
    // ...existing fields...
  }
}
```

**Response Success (201):**
```json
{
  "status": 201,
  "message": "Tạo checkout session thành công",
  "data": {
    "id": 123,
    "user": {
      "id": 1,
      "email": "user@example.com"
    },
    "checkoutItems": "[{\"bookId\":1,\"quantity\":2,\"isFlashSale\":true,\"flashSaleItemId\":5}]",
    "subtotal": 250000,
    "shippingFee": 30000,
    "totalDiscount": 0,
    "totalAmount": 280000,
    "selectedVoucherIds": null,
    "address": null,
    "notes": null,
    "createdAt": 1641234567890,
    "expiresAt": 1641237567890,
    "status": 1,
    "active": true,
    "expired": false
  }
}
```

**Response Error (400):**
```json
{
  "status": 400,
  "message": "Giỏ hàng trống, không thể tạo checkout session",
  "data": null
}
```

### 🎯 FRONTEND XỬ LÝ
```javascript
// Tạo checkout session từ cart
async function createCheckoutSession(userId) {
  try {
    const response = await fetch(`/api/checkout-sessions/from-cart?userId=${userId}`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      }
    });
    
    const result = await response.json();
    
    if (result.status === 201) {
      // Success - chuyển đến trang checkout
      window.location.href = `/checkout?sessionId=${result.data.id}`;
    } else {
      // Error - hiển thị lỗi
      showErrorMessage(result.message);
    }
  } catch (error) {
    showErrorMessage('Lỗi kết nối. Vui lòng thử lại.');
  }
}
```

---

## 📊 BƯỚC 2: HIỂN THỊ VÀ EDIT CHECKOUT SESSION

### 🔗 API: Lấy Session Detail
```http
GET /api/checkout-sessions/{sessionId}?userId={userId}
```

**Response Success (200):**
```json
{
  "status": 200,
  "message": "Lấy checkout session thành công",
  "data": {
    "id": 123,
    "checkoutItems": "[{\"bookId\":1,\"quantity\":2,\"isFlashSale\":true,\"flashSaleItemId\":5,\"unitPrice\":125000}]",
    "subtotal": 250000,
    "shippingFee": 30000,
    "totalAmount": 280000,
    "address": {
      "id": 5,
      "fullName": "Nguyễn Văn A",
      "phoneNumber": "0123456789",
      "addressLine": "123 Đường ABC"
    },
    "selectedVoucherIds": "[1,2]",
    "hasExpiredItems": false,
    "hasOutOfStockItems": false,
    "isExpired": false
  }
}
```

### 🔗 API: Update Session (Address, Vouchers, Notes)
```http
PUT /api/checkout-sessions/{sessionId}?userId={userId}
Content-Type: application/json
```

**Request Body:**
```json
{
  "addressId": 5,
  "selectedVoucherIds": [1, 2],
  "notes": "Giao hàng ngoài giờ hành chính"
}
```

### 🎯 FRONTEND XỬ LÝ
```javascript
// Load và hiển thị checkout session
async function loadCheckoutSession(sessionId, userId) {
  try {
    const response = await fetch(`/api/checkout-sessions/${sessionId}?userId=${userId}`);
    const result = await response.json();
    
    if (result.status === 200) {
      const session = result.data;
      
      // Parse checkout items
      const items = JSON.parse(session.checkoutItems);
      
      // Render items list
      renderCheckoutItems(items);
      
      // Show totals
      updateCheckoutSummary({
        subtotal: session.subtotal,
        shippingFee: session.shippingFee,
        totalAmount: session.totalAmount
      });
      
      // Check for issues
      if (session.hasExpiredItems || session.hasOutOfStockItems) {
        showWarningMessage("Có sản phẩm đã thay đổi. Vui lòng kiểm tra lại.");
        // Có thể gọi recalculate API
      }
      
    } else if (result.status === 404) {
      showErrorMessage("Session không tồn tại hoặc đã hết hạn");
      window.location.href = '/cart';
    }
  } catch (error) {
    showErrorMessage('Lỗi tải thông tin checkout');
  }
}

// Update address
async function updateAddress(sessionId, userId, addressId) {
  try {
    const response = await fetch(`/api/checkout-sessions/${sessionId}?userId=${userId}`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      },
      body: JSON.stringify({ addressId })
    });
    
    const result = await response.json();
    
    if (result.status === 200) {
      // Reload session để cập nhật shipping fee
      loadCheckoutSession(sessionId, userId);
    } else {
      showErrorMessage(result.message);
    }
  } catch (error) {
    showErrorMessage('Lỗi cập nhật địa chỉ');
  }
}
```

---

## 📊 BƯỚC 3: XỬ LÝ EDGE CASES VÀ VALIDATION

### 🔗 API: Validate Session trước khi đặt hàng
```http
POST /api/checkout-sessions/{sessionId}/validate?userId={userId}
```

**Response Success (200):**
```json
{
  "status": 200,
  "message": "Validate session thành công",
  "data": {
    "id": 123,
    "isValid": true,
    "validationErrors": []
  }
}
```

**Response Error - Flash Sale hết (400):**
```json
{
  "status": 400,
  "message": "Session validation failed: Flash sale cho sách 'Lập trình Java' đã kết thúc",
  "data": null
}
```

**Response Error - Voucher hết hạn (400):**
```json
{
  "status": 400,
  "message": "Voucher validation failed: Voucher 'SUMMER2024' đã hết hạn",
  "data": null
}
```

**Response Error - Hết hàng (400):**
```json
{
  "status": 400,
  "message": "Session validation failed: Sách 'Lập trình Python' chỉ còn 3 cuốn trong kho",
  "data": null
}
```

### 🔗 API: Recalculate Session (khi có thay đổi)
```http
PATCH /api/checkout-sessions/{sessionId}/recalculate?userId={userId}
```

**Response Success (200):**
```json
{
  "status": 200,
  "message": "Tính lại giá thành công",
  "data": {
    "id": 123,
    "subtotal": 200000,
    "totalAmount": 230000,
    "changedItems": [
      {
        "bookId": 1,
        "oldPrice": 125000,
        "newPrice": 100000,
        "reason": "Flash sale ended, using regular price"
      }
    ]
  }
}
```

### 🎯 FRONTEND XỬ LÝ EDGE CASES
```javascript
// Validate session trước khi đặt hàng
async function validateBeforeOrder(sessionId, userId) {
  try {
    const response = await fetch(`/api/checkout-sessions/${sessionId}/validate?userId=${userId}`, {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${token}`
      }
    });
    
    const result = await response.json();
    
    if (result.status === 200) {
      return true; // Session valid
    } else if (result.status === 400) {
      // Có lỗi validation
      const message = result.message;
      
      if (message.includes('Flash sale')) {
        // Flash sale đã hết
        showFlashSaleEndedModal(sessionId, userId);
        return false;
      } else if (message.includes('Voucher')) {
        // Voucher có vấn đề
        showVoucherIssueModal(sessionId, userId);
        return false;
      } else if (message.includes('hết hạn')) {
        // Session hết hạn
        showSessionExpiredModal();
        return false;
      } else {
        // Lỗi khác
        showErrorMessage(message);
        return false;
      }
    }
  } catch (error) {
    showErrorMessage('Lỗi kiểm tra thông tin');
    return false;
  }
}

// Xử lý khi flash sale hết
async function handleFlashSaleEnded(sessionId, userId) {
  showModal({
    title: 'Flash Sale Đã Kết Thúc',
    message: 'Một số sản phẩm flash sale đã kết thúc. Chúng tôi sẽ cập nhật lại giá cho bạn.',
    buttons: [
      {
        text: 'Cập Nhật Giá',
        action: async () => {
          await recalculateSession(sessionId, userId);
          loadCheckoutSession(sessionId, userId);
        }
      },
      {
        text: 'Quay Lại Giỏ Hàng',
        action: () => window.location.href = '/cart'
      }
    ]
  });
}

// Recalculate session
async function recalculateSession(sessionId, userId) {
  try {
    const response = await fetch(`/api/checkout-sessions/${sessionId}/recalculate?userId=${userId}`, {
      method: 'PATCH',
      headers: {
        'Authorization': `Bearer ${token}`
      }
    });
    
    const result = await response.json();
    
    if (result.status === 200) {
      showSuccessMessage('Đã cập nhật lại giá thành công');
      return true;
    } else {
      showErrorMessage(result.message);
      return false;
    }
  } catch (error) {
    showErrorMessage('Lỗi cập nhật giá');
    return false;
  }
}
```

---

## 📊 BƯỚC 4: TẠO ĐỞN HÀNG

### 🔗 API: Tạo Order từ Session
```http
POST /api/checkout-sessions/{sessionId}/create-order?userId={userId}
```

**Request:** Không cần body

**Response Success (201):**
```json
{
  "status": 201,
  "message": "Đặt hàng thành công! Mã đơn hàng: ORD20240713ABC123",
  "data": "ORD20240713ABC123"
}
```

**Response Error - Session hết hạn (400):**
```json
{
  "status": 400,
  "message": "Session đã hết hạn hoặc không khả dụng",
  "data": null
}
```

**Response Error - Flash sale đột ngột hết (400):**
```json
{
  "status": 400,
  "message": "❌ Flash sale đã kết thúc hoặc hết hàng. Vui lòng kiểm tra lại.",
  "data": null
}
```

**Response Error - Voucher hết lượt (400):**
```json
{
  "status": 400,
  "message": "❌ Voucher không hợp lệ hoặc đã hết hạn. Vui lòng kiểm tra lại.",
  "data": null
}
```

**Response Error - Hết hàng (400):**
```json
{
  "status": 400,
  "message": "❌ Sản phẩm đã hết hàng hoặc không đủ số lượng. Vui lòng cập nhật lại giỏ hàng.",
  "data": null
}
```

### 🎯 FRONTEND XỬ LÝ ĐẶT HÀNG
```javascript
// Xử lý khi user click "Đặt Hàng"
async function handlePlaceOrder(sessionId, userId) {
  try {
    // 1. Disable button để tránh double-click
    const orderButton = document.getElementById('place-order-btn');
    orderButton.disabled = true;
    orderButton.textContent = 'Đang xử lý...';
    
    // 2. Validate session trước
    const isValid = await validateBeforeOrder(sessionId, userId);
    if (!isValid) {
      orderButton.disabled = false;
      orderButton.textContent = 'Đặt Hàng';
      return;
    }
    
    // 3. Gọi API tạo order
    const response = await fetch(`/api/checkout-sessions/${sessionId}/create-order?userId=${userId}`, {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${token}`
      }
    });
    
    const result = await response.json();
    
    if (result.status === 201) {
      // SUCCESS - Đặt hàng thành công
      const orderCode = result.data;
      showOrderSuccessModal(orderCode);
      
      // Clear cart và redirect sau 3s
      setTimeout(() => {
        window.location.href = `/orders/${orderCode}`;
      }, 3000);
      
    } else if (result.status === 400) {
      // BUSINESS LOGIC ERRORS
      const message = result.message;
      
      if (message.includes('Flash sale')) {
        handleFlashSaleEndedDuringOrder(sessionId, userId);
      } else if (message.includes('Voucher')) {
        handleVoucherIssueDuringOrder(sessionId, userId);
      } else if (message.includes('hết hàng')) {
        handleOutOfStockDuringOrder(sessionId, userId);
      } else if (message.includes('hết hạn')) {
        handleSessionExpiredDuringOrder();
      } else {
        showErrorMessage(message);
      }
      
    } else if (result.status === 500) {
      // SYSTEM ERRORS
      showSystemErrorModal();
    }
    
  } catch (error) {
    console.error('Error placing order:', error);
    showErrorMessage('Lỗi kết nối. Vui lòng kiểm tra lại.');
  } finally {
    // Re-enable button
    const orderButton = document.getElementById('place-order-btn');
    orderButton.disabled = false;
    orderButton.textContent = 'Đặt Hàng';
  }
}

// Xử lý khi flash sale hết đột ngột trong lúc đặt hàng
function handleFlashSaleEndedDuringOrder(sessionId, userId) {
  showModal({
    title: 'Flash Sale Vừa Kết Thúc',
    message: 'Flash sale đã kết thúc trong khi bạn đang đặt hàng. Chúng tôi sẽ cập nhật lại giá.',
    buttons: [
      {
        text: 'Tiếp Tục Với Giá Mới',
        action: async () => {
          await recalculateSession(sessionId, userId);
          loadCheckoutSession(sessionId, userId);
        }
      },
      {
        text: 'Hủy Đặt Hàng',
        action: () => window.location.href = '/cart'
      }
    ]
  });
}

// Xử lý khi voucher có vấn đề trong lúc đặt hàng
function handleVoucherIssueDuringOrder(sessionId, userId) {
  showModal({
    title: 'Voucher Không Khả Dụng',
    message: 'Voucher đã hết lượt sử dụng hoặc hết hạn. Bạn có muốn tiếp tục không dùng voucher?',
    buttons: [
      {
        text: 'Bỏ Voucher & Tiếp Tục',
        action: async () => {
          // Remove vouchers and recalculate
          await updateSessionVouchers(sessionId, userId, []);
          loadCheckoutSession(sessionId, userId);
        }
      },
      {
        text: 'Hủy Đặt Hàng',
        action: () => window.location.href = '/cart'
      }
    ]
  });
}

// Xử lý khi hết hàng trong lúc đặt hàng
function handleOutOfStockDuringOrder(sessionId, userId) {
  showModal({
    title: 'Sản Phẩm Hết Hàng',
    message: 'Một số sản phẩm vừa hết hàng. Vui lòng quay lại giỏ hàng để cập nhật.',
    buttons: [
      {
        text: 'Quay Lại Giỏ Hàng',
        action: () => window.location.href = '/cart'
      }
    ]
  });
}

// Show success modal
function showOrderSuccessModal(orderCode) {
  showModal({
    title: 'Đặt Hàng Thành Công! 🎉',
    message: `Mã đơn hàng: ${orderCode}\nChúng tôi sẽ xử lý đơn hàng trong thời gian sớm nhất.`,
    buttons: [
      {
        text: 'Xem Đơn Hàng',
        action: () => window.location.href = `/orders/${orderCode}`
      },
      {
        text: 'Tiếp Tục Mua Sắm',
        action: () => window.location.href = '/books'
      }
    ]
  });
}
```

---

## 🔄 WORKFLOW TỰ ĐỘNG CỦA BACKEND

### 🛡️ VALIDATION LAYERS
1. **Session Validation:**
   - Kiểm tra session còn active
   - Kiểm tra quyền truy cập của user
   - Kiểm tra expiry time

2. **Items Validation:**
   - Kiểm tra sách còn tồn tại và active
   - Kiểm tra stock quantity
   - TỰ ĐỘNG detect flash sale hiện tại (không tin frontend)
   - Validate giá theo thời gian thực

3. **Voucher Validation:**
   - Kiểm tra voucher còn active
   - Kiểm tra thời gian hiệu lực
   - Kiểm tra usage limit và user limit
   - Validate điều kiện áp dụng

4. **Address Validation:**
   - Kiểm tra địa chỉ tồn tại
   - Validate thông tin giao hàng

### 🔄 AUTO-CORRECTION MECHANISMS
1. **Flash Sale Auto-Detection:**
   ```java
   // Backend tự động tìm flash sale tốt nhất
   Optional<FlashSaleItem> bestFlashSale = flashSaleItemRepository
       .findActiveFlashSalesByBookId(bookId, currentTime)
       .stream()
       .filter(fs -> fs.getStockQuantity() >= quantity)
       .findFirst();
   ```

2. **Price Auto-Calculation:**
   ```java
   // Backend tự tính giá, không tin frontend
   BigDecimal unitPrice = bestFlashSale.isPresent() 
       ? bestFlashSale.get().getDiscountPrice()
       : book.getPrice();
   ```

3. **Auto Cart Clear:**
   - Sau khi tạo order thành công, tự động clear cart
   - Nếu clear cart fail, vẫn trả về success (order đã tạo)

---

## 📱 CÁC TRƯỜNG HỢP FRONTEND CẦN XỬ LÝ

### 🔍 CASE 1: Session Hết Hạn
**Khi:** Session > 30 phút hoặc user đóng browser lâu
**Response:** `400 - "Checkout session đã hết hạn"`
**Xử lý:**
```javascript
if (response.message.includes('hết hạn')) {
  showModal({
    title: 'Phiên Checkout Hết Hạn',
    message: 'Vui lòng tạo phiên checkout mới từ giỏ hàng',
    buttons: [{
      text: 'Quay Lại Giỏ Hàng',
      action: () => window.location.href = '/cart'
    }]
  });
}
```

### 🔍 CASE 2: Flash Sale Kết Thúc Đột Ngột
**Khi:** Flash sale hết thời gian hoặc hết hàng trong lúc user đang checkout
**Response:** `400 - "Flash sale đã kết thúc hoặc hết hàng"`
**Xử lý:** Đưa ra 2 option: cập nhật giá mới hoặc quay lại cart

### 🔍 CASE 3: Voucher Hết Lượt/Hết Hạn
**Khi:** Voucher hết usage limit hoặc expired trong lúc checkout
**Response:** `400 - "Voucher không hợp lệ hoặc đã hết hạn"`
**Xử lý:** Option bỏ voucher và tiếp tục hoặc cancel order

### 🔍 CASE 4: Sản Phẩm Hết Hàng
**Khi:** Stock = 0 hoặc stock < quantity trong lúc đặt hàng
**Response:** `400 - "Sản phẩm đã hết hàng hoặc không đủ số lượng"`
**Xử lý:** Redirect về cart để user điều chỉnh

### 🔍 CASE 5: Race Condition (Multiple Users)
**Khi:** 2 user cùng đặt hàng sản phẩm cuối cùng
**Response:** `400 - "Sản phẩm đã hết hàng"`
**Xử lý:** Thông báo và suggest sản phẩm tương tự

---

## 🏁 COMPLETE WORKFLOW EXAMPLE

```javascript
// FULL CHECKOUT FLOW
class CheckoutManager {
  constructor(userId) {
    this.userId = userId;
    this.sessionId = null;
  }
  
  // STEP 1: Initialize checkout from cart
  async initializeCheckout() {
    try {
      const response = await this.apiCall('POST', '/api/checkout-sessions/from-cart', {
        params: { userId: this.userId }
      });
      
      if (response.status === 201) {
        this.sessionId = response.data.id;
        window.location.href = `/checkout?sessionId=${this.sessionId}`;
      } else {
        this.handleError(response);
      }
    } catch (error) {
      this.handleError(error);
    }
  }
  
  // STEP 2: Load checkout page
  async loadCheckoutPage() {
    const urlParams = new URLSearchParams(window.location.search);
    this.sessionId = urlParams.get('sessionId');
    
    if (!this.sessionId) {
      window.location.href = '/cart';
      return;
    }
    
    await this.loadSession();
    this.setupEventListeners();
  }
  
  // STEP 3: Load session data
  async loadSession() {
    try {
      const response = await this.apiCall('GET', `/api/checkout-sessions/${this.sessionId}`, {
        params: { userId: this.userId }
      });
      
      if (response.status === 200) {
        this.renderSession(response.data);
      } else if (response.status === 404) {
        this.handleSessionNotFound();
      }
    } catch (error) {
      this.handleError(error);
    }
  }
  
  // STEP 4: Handle place order
  async placeOrder() {
    try {
      // Validate first
      const isValid = await this.validateSession();
      if (!isValid) return;
      
      // Create order
      const response = await this.apiCall('POST', `/api/checkout-sessions/${this.sessionId}/create-order`, {
        params: { userId: this.userId }
      });
      
      if (response.status === 201) {
        this.handleOrderSuccess(response.data);
      } else {
        this.handleOrderError(response);
      }
    } catch (error) {
      this.handleError(error);
    }
  }
  
  // Utility methods
  async apiCall(method, url, options = {}) {
    const config = {
      method,
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${this.getToken()}`
      }
    };
    
    if (options.params) {
      url += '?' + new URLSearchParams(options.params);
    }
    
    if (options.body) {
      config.body = JSON.stringify(options.body);
    }
    
    const response = await fetch(url, config);
    return await response.json();
  }
  
  handleOrderSuccess(orderCode) {
    showModal({
      title: 'Đặt Hàng Thành Công! 🎉',
      message: `Mã đơn hàng: ${orderCode}`,
      onClose: () => window.location.href = `/orders/${orderCode}`
    });
  }
  
  handleOrderError(response) {
    const message = response.message;
    
    if (message.includes('Flash sale')) {
      this.handleFlashSaleEnded();
    } else if (message.includes('Voucher')) {
      this.handleVoucherIssue();
    } else if (message.includes('hết hàng')) {
      this.handleOutOfStock();
    } else {
      showErrorMessage(message);
    }
  }
}

// Usage
const checkoutManager = new CheckoutManager(getCurrentUserId());

// On cart page
document.getElementById('checkout-btn').onclick = () => {
  checkoutManager.initializeCheckout();
};

// On checkout page
document.addEventListener('DOMContentLoaded', () => {
  checkoutManager.loadCheckoutPage();
});
```

---

## 🎯 KẾT LUẬN VÀ BEST PRACTICES

### ✅ CÁC ĐIỂM MẠNH CỦA HỆ THỐNG
1. **Backend-Driven:** Mọi logic tính toán do backend xử lý
2. **Auto-Detection:** Tự động detect flash sale, không phụ thuộc frontend
3. **Multi-Layer Validation:** Validate tại nhiều tầng
4. **Error Recovery:** Có mechanism để recover từ errors
5. **Race Condition Safe:** Sử dụng synchronized để tránh race condition

### 🚨 LƯU Ý QUAN TRỌNG CHO FRONTEND
1. **KHÔNG TIN GIÁ TỪ FRONTEND:** Backend luôn tự tính giá
2. **VALIDATE TRƯỚC KHI ĐẶT HÀNG:** Luôn gọi validate API
3. **XỬ LÝ ERRORS GRACEFULLY:** Đưa ra options cho user
4. **AUTO-REFRESH:** Tự động refresh session khi có changes
5. **SESSION EXPIRY:** Monitor và thông báo khi session sắp hết hạn

### 📋 CHECKLIST CHO FRONTEND DEVELOPER
- [ ] Implement error handling cho tất cả edge cases
- [ ] Show loading states cho các async operations
- [ ] Validate input trước khi gửi requests
- [ ] Handle session expiry gracefully
- [ ] Implement auto-refresh cho checkout page
- [ ] Show clear messages cho users về các issues
- [ ] Provide fallback options khi có errors
- [ ] Monitor session time và warn user
- [ ] Clear any cached data after successful order
- [ ] Implement proper loading/error states cho UX tốt

### 🔍 MONITORING VÀ DEBUGGING
1. **Log all API calls** cho checkout flow
2. **Track session conversion rates**
3. **Monitor error patterns** để improve UX
4. **Alert on high error rates** cho flash sale events
5. **Track voucher usage failures**

---

## 📞 SUPPORT INFORMATION
- **Backend API Docs:** `/swagger-ui.html`
- **Error Codes Reference:** `Error Code Guide`
- **Flash Sale Logic:** `FLASH_SALE_ANALYSIS.md`
- **Voucher Logic:** `VOUCHER_BUSINESS_LOGIC.md`
- **Cart Integration:** `CART_BUSINESS_LOGIC.md`

---

*Tài liệu này được cập nhật lần cuối: 13/07/2025*
*Version: 1.0*
