# HƯỚNG DẪN ĐẦY ĐỦ: TỪ CHECKOUT SESSION ĐẾN HOÀN THÀNH ĐƠN HÀNG

## 📖 TỔNG QUAN HỆ THỐNG
Hệ thống đặt hàng BookStation được thiết kế với luồng xử lý phức tạp từ checkout session đến hoàn thành đơn hàng, bao gồm xử lý flash sale, voucher, và nhiều edge case.

## 🔄 LUỒNG HOÀN CHỈNH
```
1. Frontend gửi items từ cart → Tạo Checkout Session
2. User cập nhật thông tin → Update Session (address, payment, vouchers...)
3. Validate session → Kiểm tra tất cả edge cases
4. Tạo đơn hàng → OrderService xử lý với logic phức tạp
5. Clear cart → Hoàn thành
```

## 🛠️ API ENDPOINTS CHI TIẾT

### 1. TẠO CHECKOUT SESSION
```http
POST /api/checkout-sessions
Content-Type: application/json
```

**Request Body (Cơ bản):**
```json
{
  "items": [
    {
      "bookId": 1,
      "quantity": 2
    },
    {
      "bookId": 5,
      "quantity": 1
    }
  ]
}
```

**Request Body (Đầy đủ tất cả trường):**
```json
{
  "items": [
    {
      "bookId": 1,
      "quantity": 2
    },
    {
      "bookId": 5,
      "quantity": 1
    }
  ],
  "addressId": 123,
  "shippingMethod": "STANDARD",
  "estimatedDeliveryFrom": 1737561600000,
  "estimatedDeliveryTo": 1737648000000,
  "paymentMethod": "COD",
  "selectedVoucherIds": [10, 15],
  "notes": "Giao hàng giờ hành chính"
}
```

**Response:**
```json
{
  "status": 200,
  "message": "Tạo checkout session thành công",
  "data": {
    "sessionId": 456,
    "userId": 789,
    "checkoutItems": [
      {
        "bookId": 1,
        "bookTitle": "Sách ABC",
        "bookImage": "image1.jpg",
        "quantity": 2,
        "unitPrice": 150000,
        "totalPrice": 300000,
        "isFlashSale": true,
        "flashSalePrice": 120000,
        "flashSaleId": 10,
        "originalPrice": 150000
      }
    ],
    "address": {
      "id": 123,
      "recipientName": "Nguyễn Văn A",
      "phoneNumber": "0912345678",
      "addressDetail": "123 Đường ABC, Quận 1",
      "provinceName": "TP.HCM",
      "districtName": "Quận 1",
      "wardName": "Phường 1"
    },
    "shippingMethod": "STANDARD",
    "estimatedDeliveryFrom": 1737561600000,
    "estimatedDeliveryTo": 1737648000000,
    "paymentMethod": "COD",
    "selectedVoucherIds": [10, 15],
    "appliedVouchers": [
      {
        "id": 10,
        "code": "DISCOUNT10",
        "name": "Giảm 10%",
        "discountType": "PERCENTAGE",
        "discountValue": 10.00,
        "applicableAmount": 300000
      }
    ],
    "subtotal": 300000,
    "shippingFee": 30000,
    "voucherDiscount": 30000,
    "totalAmount": 300000,
    "notes": "Giao hàng giờ hành chính",
    "expiresAt": 1737475200000,
    "isActive": true,
    "createdAt": 1737388800000,
    "updatedAt": 1737388800000
  }
}
```

### 2. CẬP NHẬT CHECKOUT SESSION
```http
PUT /api/checkout-sessions/{sessionId}?userId={userId}
Content-Type: application/json
```

**Các trường có thể cập nhật:**
```json
{
  "items": [
    {
      "bookId": 1,
      "quantity": 3
    }
  ],
  "addressId": 124,
  "shippingMethod": "EXPRESS",
  "estimatedDeliveryFrom": 1737475200000,
  "estimatedDeliveryTo": 1737561600000,
  "paymentMethod": "BANK_TRANSFER",
  "selectedVoucherIds": [10, 20],
  "notes": "Giao nhanh trong ngày"
}
```

**Lưu ý quan trọng:**
- Tất cả các trường đều **optional** trong update request
- Chỉ gửi các trường cần cập nhật
- Hệ thống sẽ tự động tính lại giá khi có thay đổi
- `addressId` sẽ tự động attach địa chỉ mặc định nếu không có

### 3. LẤY THÔNG TIN SESSION
```http
GET /api/checkout-sessions/{sessionId}?userId={userId}
```

### 4. LẤY SESSION MỚI NHẤT
```http
GET /api/checkout-sessions/latest?userId={userId}
```

### 5. VALIDATE SESSION
```http
POST /api/checkout-sessions/{sessionId}/validate?userId={userId}
```

### 6. TÍNH LẠI GIÁ
```http
PATCH /api/checkout-sessions/{sessionId}/recalculate?userId={userId}
```

### 7. TẠO ĐƠN HÀNG TỪ SESSION
```http
POST /api/checkout-sessions/{sessionId}/create-order?userId={userId}
```

## 🎯 XỬ LÝ EDGE CASES

### 1. **Flash Sale Kết Thúc Đột Ngột**
**Tình huống:** User đang ở checkout session, flash sale kết thúc giữa chừng

**Xử lý backend:**
```java
// Trong OrderServiceImpl.validateFlashSaleItem()
if (flashSaleItem.getEndTime() <= currentTime) {
    // ❌ Flash sale đã kết thúc
    log.warn("Flash sale ended for item: {}", flashSaleItem.getId());
    
    // Tự động chuyển về giá gốc
    BigDecimal originalPrice = book.getFinalPrice();
    orderDetail.setUnitPrice(originalPrice);
    
    // Thông báo cho frontend
    throw new RuntimeException("Flash sale đã kết thúc cho sản phẩm: " + book.getTitle() + 
                              ". Giá đã được cập nhật về giá gốc: " + originalPrice);
}
```

**Response error:**
```json
{
  "status": 400,
  "message": "Flash sale đã kết thúc cho sản phẩm: Sách ABC. Giá đã được cập nhật về giá gốc: 150000",
  "data": null
}
```

### 2. **Voucher Hết Hạn Hoặc Hết Lượt**
**Xử lý backend:**
```java
// Trong VoucherCalculationServiceImpl.validateVoucherApplication()
if (currentTime < voucher.getStartTime() || currentTime > voucher.getEndTime()) {
    throw new RuntimeException("Voucher " + voucher.getCode() + " đã hết hạn hoặc chưa có hiệu lực");
}

if (voucher.getUsageLimit() != null && voucher.getUsedCount() >= voucher.getUsageLimit()) {
    throw new RuntimeException("Voucher " + voucher.getCode() + " đã hết lượt sử dụng");
}
```

### 3. **Sản Phẩm Hết Hàng**
```java
// Validate quantity
if (book.getStock() < requestedQuantity) {
    throw new RuntimeException("Sản phẩm " + book.getTitle() + 
                              " chỉ còn " + book.getStock() + " trong kho");
}
```

### 4. **Session Hết Hạn**
```java
// Trong CheckoutSessionServiceImpl
if (!existingSession.isActive()) {
    return new ApiResponse<>(400, "Checkout session đã hết hạn", null);
}
```

## 📱 FRONTEND INTEGRATION

### JavaScript Example - Tạo và Cập Nhật Session
```javascript
class CheckoutSessionManager {
    constructor(baseUrl, userId) {
        this.baseUrl = baseUrl;
        this.userId = userId;
        this.currentSession = null;
    }

    // Tạo session từ cart
    async createSessionFromCart(cartItems) {
        const requestData = {
            items: cartItems.map(item => ({
                bookId: item.bookId,
                quantity: item.quantity
            }))
        };

        try {
            const response = await fetch(`${this.baseUrl}/api/checkout-sessions?userId=${this.userId}`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(requestData)
            });

            const result = await response.json();
            if (result.status === 200) {
                this.currentSession = result.data;
                return result.data;
            } else {
                throw new Error(result.message);
            }
        } catch (error) {
            console.error('❌ Error creating checkout session:', error);
            throw error;
        }
    }

    // Cập nhật địa chỉ
    async updateAddress(addressId) {
        return this.updateSession({ addressId });
    }

    // Cập nhật phương thức thanh toán
    async updatePaymentMethod(paymentMethod) {
        return this.updateSession({ paymentMethod });
    }

    // Cập nhật vouchers
    async updateVouchers(voucherIds) {
        return this.updateSession({ selectedVoucherIds: voucherIds });
    }

    // Cập nhật thông tin giao hàng
    async updateShipping(shippingMethod, estimatedDeliveryFrom, estimatedDeliveryTo) {
        return this.updateSession({ 
            shippingMethod, 
            estimatedDeliveryFrom, 
            estimatedDeliveryTo 
        });
    }

    // Cập nhật số lượng sản phẩm
    async updateItems(items) {
        return this.updateSession({ items });
    }

    // Phương thức cập nhật chung
    async updateSession(updates) {
        if (!this.currentSession) {
            throw new Error('Không có session hiện tại');
        }

        try {
            const response = await fetch(
                `${this.baseUrl}/api/checkout-sessions/${this.currentSession.sessionId}?userId=${this.userId}`,
                {
                    method: 'PUT',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(updates)
                }
            );

            const result = await response.json();
            if (result.status === 200) {
                this.currentSession = result.data;
                return result.data;
            } else {
                throw new Error(result.message);
            }
        } catch (error) {
            console.error('❌ Error updating session:', error);
            throw error;
        }
    }

    // Validate session trước khi tạo order
    async validateSession() {
        if (!this.currentSession) {
            throw new Error('Không có session hiện tại');
        }

        try {
            const response = await fetch(
                `${this.baseUrl}/api/checkout-sessions/${this.currentSession.sessionId}/validate?userId=${this.userId}`,
                { method: 'POST' }
            );

            const result = await response.json();
            if (result.status !== 200) {
                throw new Error(result.message);
            }
            return result.data;
        } catch (error) {
            console.error('❌ Session validation failed:', error);
            throw error;
        }
    }

    // Tạo đơn hàng
    async createOrder() {
        // Validate trước khi tạo
        await this.validateSession();

        try {
            const response = await fetch(
                `${this.baseUrl}/api/checkout-sessions/${this.currentSession.sessionId}/create-order?userId=${this.userId}`,
                { method: 'POST' }
            );

            const result = await response.json();
            if (result.status === 200) {
                this.currentSession = null; // Clear session sau khi tạo order thành công
                return result.data;
            } else {
                throw new Error(result.message);
            }
        } catch (error) {
            console.error('❌ Error creating order:', error);
            throw error;
        }
    }
}

// Sử dụng
const checkoutManager = new CheckoutSessionManager('http://localhost:8080', 123);

// Workflow hoàn chỉnh
async function completeCheckoutFlow() {
    try {
        // 1. Tạo session từ cart
        const cartItems = [
            { bookId: 1, quantity: 2 },
            { bookId: 5, quantity: 1 }
        ];
        
        console.log('📝 Creating checkout session...');
        const session = await checkoutManager.createSessionFromCart(cartItems);
        console.log('✅ Session created:', session.sessionId);

        // 2. Cập nhật địa chỉ
        console.log('📍 Updating address...');
        await checkoutManager.updateAddress(123);

        // 3. Cập nhật phương thức thanh toán
        console.log('💳 Updating payment method...');
        await checkoutManager.updatePaymentMethod('BANK_TRANSFER');

        // 4. Áp dụng vouchers
        console.log('🎫 Applying vouchers...');
        await checkoutManager.updateVouchers([10, 15]);

        // 5. Cập nhật phương thức giao hàng
        console.log('🚚 Updating shipping...');
        await checkoutManager.updateShipping(
            'EXPRESS', 
            Date.now() + 86400000,  // Ngày mai
            Date.now() + 172800000  // 2 ngày sau
        );

        // 6. Validate và tạo đơn hàng
        console.log('✅ Validating and creating order...');
        const orderResult = await checkoutManager.createOrder();
        console.log('🎉 Order created successfully!', orderResult);

    } catch (error) {
        console.error('❌ Checkout failed:', error.message);
        
        // Xử lý các lỗi cụ thể
        if (error.message.includes('Flash sale đã kết thúc')) {
            alert('Flash sale đã kết thúc! Giá sản phẩm đã được cập nhật.');
            // Reload session để cập nhật giá mới
            window.location.reload();
        } else if (error.message.includes('Voucher')) {
            alert('Voucher không khả dụng: ' + error.message);
            // Remove voucher và retry
        } else if (error.message.includes('hết hàng')) {
            alert('Sản phẩm không đủ số lượng: ' + error.message);
            // Cập nhật lại quantity
        }
    }
}
```

## 🔧 XỬ LÝ LỖI VÀ EDGE CASES

### 1. **Auto-correction Logic**
Backend tự động sửa các vấn đề khi có thể:
```java
// Flash sale hết hạn → chuyển về giá gốc
// Voucher hết hạn → bỏ voucher
// Quantity vượt stock → giảm quantity về stock
```

### 2. **Frontend Error Handling**
```javascript
function handleCheckoutError(error) {
    const errorMessage = error.message;
    
    if (errorMessage.includes('Flash sale đã kết thúc')) {
        // Hiển thị popup thông báo giá thay đổi
        showPriceUpdateModal();
        
    } else if (errorMessage.includes('Voucher')) {
        // Hiển thị danh sách voucher còn khả dụng
        showAlternativeVouchers();
        
    } else if (errorMessage.includes('hết hàng')) {
        // Cập nhật UI với số lượng khả dụng
        updateAvailableQuantity();
        
    } else if (errorMessage.includes('hết hạn')) {
        // Tạo session mới
        createNewSession();
    }
}
```

## 📋 TẤT CẢ CÁC TRƯỜNG DỮ LIỆU CHECKOUT SESSION

### CheckoutSessionRequest - Tất cả trường có thể gửi:
```typescript
interface CheckoutSessionRequest {
  // REQUIRED - Danh sách sản phẩm
  items: Array<{
    bookId: number;      // ID sản phẩm
    quantity: number;    // Số lượng
  }>;
  
  // OPTIONAL - Thông tin địa chỉ
  addressId?: number;    // ID địa chỉ giao hàng
  
  // OPTIONAL - Thông tin vận chuyển
  shippingMethod?: string;           // Phương thức vận chuyển: "STANDARD", "EXPRESS"
  estimatedDeliveryFrom?: number;    // Thời gian giao hàng dự kiến (từ) - timestamp
  estimatedDeliveryTo?: number;      // Thời gian giao hàng dự kiến (đến) - timestamp
  
  // OPTIONAL - Thông tin thanh toán
  paymentMethod?: string;            // Phương thức thanh toán: "COD", "BANK_TRANSFER", "VNPAY"
  
  // OPTIONAL - Voucher
  selectedVoucherIds?: number[];     // Danh sách ID voucher được chọn (tối đa 2)
  
  // OPTIONAL - Ghi chú
  notes?: string;                    // Ghi chú cho đơn hàng
}
```

### CheckoutSessionResponse - Tất cả trường trả về:
```typescript
interface CheckoutSessionResponse {
  // Thông tin session
  sessionId: number;
  userId: number;
  
  // Sản phẩm trong session
  checkoutItems: Array<{
    bookId: number;
    bookTitle: string;
    bookImage: string;
    quantity: number;
    unitPrice: number;
    totalPrice: number;
    isFlashSale: boolean;
    flashSalePrice?: number;
    flashSaleId?: number;
    originalPrice: number;
  }>;
  
  // Thông tin địa chỉ
  address?: {
    id: number;
    recipientName: string;
    phoneNumber: string;
    addressDetail: string;
    provinceName: string;
    districtName: string;
    wardName: string;
    isDefault: boolean;
  };
  
  // Thông tin vận chuyển
  shippingMethod?: string;
  estimatedDeliveryFrom?: number;
  estimatedDeliveryTo?: number;
  
  // Thông tin thanh toán
  paymentMethod?: string;
  
  // Voucher
  selectedVoucherIds?: number[];
  appliedVouchers?: Array<{
    id: number;
    code: string;
    name: string;
    discountType: string;
    discountValue: number;
    applicableAmount: number;
  }>;
  
  // Tính toán giá
  subtotal: number;           // Tổng tiền sản phẩm
  shippingFee: number;        // Phí vận chuyển
  voucherDiscount: number;    // Giảm giá từ voucher
  totalAmount: number;        // Tổng tiền cuối cùng
  
  // Metadata
  notes?: string;
  expiresAt: number;          // Thời gian hết hạn session
  isActive: boolean;          // Session còn hiệu lực
  createdAt: number;
  updatedAt: number;
}
```

## 📊 BUSINESS LOGIC SUMMARY

1. **Session Expiration:** 24 giờ
2. **Maximum Vouchers:** 2 vouchers (1 regular + 1 shipping)
3. **Flash Sale Priority:** Luôn kiểm tra real-time
4. **Address Auto-attach:** Địa chỉ mặc định nếu không có
5. **Price Recalculation:** Tự động khi có thay đổi
6. **Inventory Check:** Real-time khi tạo order
7. **Cart Clear:** Tự động sau khi tạo order thành công

## 🎯 KẾT LUẬN

Hệ thống checkout session được thiết kế để xử lý mọi edge case phức tạp, đảm bảo trải nghiệm người dùng mượt mà và dữ liệu luôn chính xác. Frontend cần implement logic xử lý lỗi tương ứng để tạo UX tốt nhất.

**Điểm quan trọng:**
- ✅ Tất cả các trường trong CheckoutSessionRequest đều optional (trừ items)
- ✅ Có thể cập nhật từng phần một cách linh hoạt
- ✅ Tự động tính lại giá khi có thay đổi
- ✅ Xử lý edge case tự động với thông báo rõ ràng
- ✅ Support đầy đủ các tính năng phức tạp (flash sale, voucher, inventory)
