# HƯỚNG DẪN TẠO ĐƠN HÀNG THỦ CÔNG CHO ADMIN - BookStation Backend

## 📋 TỔNG QUAN

Tài liệu này hướng dẫn frontend team cách tích hợp API để tạo đơn hàng thủ công cho users trong hệ thống admin. Bao gồm tất cả các bước từ lấy dropdown đến tạo đơn hàng hoàn chỉnh với validation đầy đủ.

## 🔗 DANH SÁCH API CẦN THIẾT

### 1. API Lấy Dropdown Địa Chỉ User
**Endpoint:** `GET /api/addresses/user/{userId}/dropdown`

**Mục đích:** Lấy danh sách địa chỉ của user để admin chọn khi tạo đơn

**Request:**
```
GET /api/addresses/user/123/dropdown
Authorization: Bearer {admin_token}
```

**Response thành công:**
```json
{
  "status": 200,
  "message": "Lấy danh sách địa chỉ thành công",
  "data": [
    {
      "id": 1,
      "name": "Nguyễn Văn A - 123 Đường ABC, Phường XYZ, Quận 1, TP.HCM (Mặc định)"
    },
    {
      "id": 2,
      "name": "Nguyễn Văn A - 456 Đường DEF, Phường GHI, Quận 2, TP.HCM"
    }
  ]
}
```

**Response lỗi:**
```json
{
  "status": 404,
  "message": "User không tồn tại hoặc chưa có địa chỉ",
  "data": null
}
```

### 2. API Lấy Dropdown Voucher Có Thể Sử Dụng
**Endpoint:** `GET /api/vouchers/user/{userId}/available`

**Mục đích:** Lấy danh sách voucher mà user có thể sử dụng (đã lọc theo thời gian hiệu lực, giới hạn sử dụng)

**Request:**
```
GET /api/vouchers/user/123/available
Authorization: Bearer {admin_token}
```

**Response thành công:**
```json
{
  "status": 200,
  "message": "Lấy danh sách voucher có thể sử dụng thành công",
  "data": [
    {
      "id": 1,
      "name": "SUMMER2024 - Giảm giá mùa hè (-20%)"
    },
    {
      "id": 2,
      "name": "FREESHIP - Miễn phí vận chuyển (-30000đ)"
    }
  ]
}
```

**Response lỗi:**
```json
{
  "status": 500,
  "message": "Lỗi khi lấy danh sách voucher: Internal server error",
  "data": null
}
```

### 3. API Tạo Đơn Hàng Thủ Công
**Endpoint:** `POST /api/orders`

**Mục đích:** Tạo đơn hàng hoàn chỉnh cho user với validation đầy đủ

**Request:**
```json
{
  "userId": 123,
  "staffId": 456,
  "addressId": 1,
  "shippingFee": 30000,
  "voucherIds": [1, 2],
  "notes": "Đơn hàng được tạo bởi admin",
  "orderType": "NORMAL", // ⚠️ BẮT BUỘC: Loại đơn hàng (NORMAL, EVENT_GIFT, PROMOTIONAL, SAMPLE)
  "orderDetails": [
    {
      "bookId": 10,
      "quantity": 2,
      "unitPrice": 150000
      // ✅ KHÔNG CẦN flashSaleItemId - Backend tự động phát hiện flash sale
    },
    {
      "bookId": 20,
      "quantity": 1,
      "unitPrice": 200000
      // ✅ Backend sẽ tự động áp dụng giá flash sale nếu có
    }
  ]
}
```
> ⚠️ Lưu ý: Các trường subtotal và totalAmount sẽ được backend tự động tính toán khi tạo đơn hàng. Frontend KHÔNG cần gửi hai trường này. Nếu gặp lỗi 500 liên quan subtotal hoặc totalAmount bị null, cần kiểm tra lại service backend để đảm bảo đã set giá trị trước khi lưu Order.

**Response thành công:**
```json
{
  "status": 201,
  "message": "Tạo đơn hàng thành công",
  "data": {
    "orderId": 789,
    "orderCode": "ORD20240713001",
    "totalAmount": 470000,
    "discountAmount": 50000,
    "finalAmount": 420000,
    "status": "PENDING"
  }
}
```

**Response lỗi validation:**
```json
{
  "status": 400,
  "message": "Lỗi validation: Sách ID 10 không đủ số lượng trong kho (còn 1, yêu cầu 2)",
  "data": null
}
```

## 📝 CẤU TRÚC DỮ LIỆU CHI TIẾT

### OrderRequest DTO
```json
{
  "userId": "Integer - ID của user (bắt buộc)",
  "staffId": "Integer - ID của admin/staff tạo đơn (tùy chọn)",
  "addressId": "Integer - ID địa chỉ giao hàng (bắt buộc)",
  "shippingFee": "BigDecimal - Phí vận chuyển (bắt buộc)",
  "voucherIds": "List<Integer> - Danh sách ID voucher áp dụng (tùy chọn, tối đa 2)",
  "notes": "String - Ghi chú đơn hàng (tùy chọn)",
  "orderType": "String - Loại đơn hàng (bắt buộc, các giá trị: NORMAL, EVENT_GIFT, PROMOTIONAL, SAMPLE)",
  "orderDetails": "List<OrderDetailRequest> - Chi tiết sản phẩm (bắt buộc, ít nhất 1 item)"
}
```

### OrderDetailRequest DTO
```json
{
  "bookId": "Integer - ID sách (bắt buộc)",
  "quantity": "Integer - Số lượng (bắt buộc, >= 1)", 
  "unitPrice": "BigDecimal - Đơn giá (bắt buộc, > 0)"
}
```

**⚠️ LƯU Ý QUAN TRỌNG:**
- **flashSaleItemId đã DEPRECATED** - Backend tự động phát hiện flash sale
- Khi có flash sale active cho bookId, backend sẽ tự động áp dụng giá flash sale
- unitPrice trong request chỉ để reference, backend sẽ tính toán lại pricing chính xác

## � FLASH SALE AUTO-DETECTION

### Cách Backend Xử lý Flash Sale
1. **Tự động phát hiện:** Backend tự động check flash sale cho mỗi bookId
2. **Business Rule:** 1 sách chỉ có 1 flash sale active tại 1 thời điểm
3. **Priority System:**
   - Admin control (status = 0/1) - Ưu tiên cao nhất
   - Time validation (startTime ≤ now ≤ endTime) - Ưu tiên thứ hai
4. **Stock validation:** Flash sale phải còn hàng (stockQuantity > 0)

### Pricing Logic
```
if (hasActiveFlashSale(bookId)) {
    unitPrice = flashSaleItem.getDiscountPrice();
    log("🔥 AUTO-DETECTED flash sale for book ${bookId}: regular=${book.price}, flash=${unitPrice}");
} else {
    unitPrice = book.getPrice();
    log("💰 Using regular price for book ${bookId}: ${unitPrice}");
}
```

## �🔄 QUY TRÌNH TẠO ĐƠN HÀNG

### Bước 1: Lấy thông tin user và dropdown
```javascript
// 1. Lấy danh sách địa chỉ
const addressResponse = await fetch(`/api/addresses/user/${userId}/dropdown`);
const addresses = await addressResponse.json();

// 2. Lấy danh sách voucher có thể sử dụng
const voucherResponse = await fetch(`/api/vouchers/user/${userId}/available`);
const vouchers = await voucherResponse.json();
```

### Bước 2: Hiển thị form và validate input
```javascript
const orderData = {
  userId: parseInt(userId),
  staffId: getCurrentStaffId(), // ID của admin đang đăng nhập
  addressId: selectedAddressId,
  shippingFee: calculateShippingFee(selectedAddress),
  voucherIds: selectedVoucherIds, // Array có thể rỗng hoặc có 1-2 phần tử
  notes: orderNotes || "",
  orderDetails: orderItems.map(item => ({
    bookId: item.bookId,
    quantity: item.quantity,
    unitPrice: item.unitPrice
    // ✅ Không cần flashSaleItemId - Backend tự động detect
  }))
};
```

### Bước 3: Gửi request tạo đơn hàng
```javascript
try {
  const response = await fetch('/api/orders', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${adminToken}`
    },
    body: JSON.stringify(orderData)
  });
  
  const result = await response.json();
  
  if (result.status === 201) {
    showSuccess(`Tạo đơn hàng thành công! Mã đơn: ${result.data.orderCode}`);
    redirectToOrderDetail(result.data.orderId);
  } else {
    showError(result.message);
  }
} catch (error) {
  showError('Lỗi kết nối server: ' + error.message);
}
```

## ⚠️ CÁC EDGE CASE VÀ VALIDATION

### 1. Validation Voucher
- **Tối đa 2 voucher:** Chỉ được áp dụng tối đa 2 voucher trên 1 đơn hàng
- **Không được dùng 2 voucher cùng loại:** Không thể dùng 2 voucher giảm giá thường hoặc 2 voucher freeship
- **Kiểm tra thời gian:** Voucher phải trong thời gian hiệu lực
- **Kiểm tra giới hạn:** User không được vượt quá số lần sử dụng cho phép
- **Đơn hàng tối thiểu:** Tổng giá trị đơn hàng phải đạt minimum order value của voucher

**Ví dụ lỗi:**
```json
{
  "status": 400,
  "message": "Chỉ được sử dụng tối đa 1 voucher thường trên 1 đơn hàng",
  "data": null
}
```

### 2. Validation Flash Sale
- **Kiểm tra thời gian:** Flash sale phải đang hoạt động
- **Kiểm tra số lượng:** Số lượng flash sale phải đủ
- **Giá đúng:** Unit price phải khớp với giá flash sale

**Ví dụ lỗi:**
```json
{
  "status": 400,
  "message": "Flash sale item ID 5 đã hết hạn hoặc hết số lượng",
  "data": null
}
```

### 3. Validation Inventory
- **Kiểm tra tồn kho:** Số lượng sách phải đủ trong kho
- **Trạng thái sách:** Sách phải đang hoạt động (không bị ẩn)

**Ví dụ lỗi:**
```json
{
  "status": 400,
  "message": "Sách 'Clean Code' không đủ số lượng trong kho (còn 3, yêu cầu 5)",
  "data": null
}
```

### 4. Validation Business Logic
- **Order details không được rỗng:** Phải có ít nhất 1 sản phẩm
- **Quantity > 0:** Số lượng phải lớn hơn 0
- **Unit price > 0:** Đơn giá phải lớn hơn 0
- **User và Address hợp lệ:** User phải tồn tại và address phải thuộc về user đó

## 🎯 XỬ LÝ LỖI VÀ THÔNG BÁO

### Các loại lỗi phổ biến:

#### 1. Lỗi 400 - Bad Request
```json
{
  "status": 400,
  "message": "Validation error message",
  "data": null
}
```
**Xử lý:** Hiển thị message cho user, focus vào field bị lỗi

#### 2. Lỗi 404 - Not Found
```json
{
  "status": 404,
  "message": "User/Book/Address không tồn tại",
  "data": null
}
```
**Xử lý:** Kiểm tra lại input, có thể data đã bị xóa

#### 3. Lỗi 500 - Internal Server Error
```json
{
  "status": 500,
  "message": "Lỗi hệ thống",
  "data": null
}
```
**Xử lý:** Thông báo lỗi hệ thống, thử lại sau

### Thông báo thành công:
```json
{
  "status": 201,
  "message": "Tạo đơn hàng thành công",
  "data": {
    "orderId": 789,
    "orderCode": "ORD20240713001"
  }
}
```

## 💡 BEST PRACTICES

### 1. Loading States
```javascript
// Hiển thị loading khi gọi API
setLoading(true);
try {
  const result = await createOrder(orderData);
  // Handle success
} catch (error) {
  // Handle error
} finally {
  setLoading(false);
}
```

### 2. Validation phía Frontend
```javascript
// Validate trước khi gửi request
function validateOrderData(orderData) {
  const errors = [];
  
  if (!orderData.userId) errors.push("Vui lòng chọn user");
  if (!orderData.addressId) errors.push("Vui lòng chọn địa chỉ giao hàng");
  if (orderData.orderDetails.length === 0) errors.push("Vui lòng thêm ít nhất 1 sản phẩm");
  if (orderData.voucherIds.length > 2) errors.push("Chỉ được chọn tối đa 2 voucher");
  
  return errors;
}
```

### 3. Debounce cho API calls
```javascript
// Sử dụng debounce khi search user/product
const debouncedSearch = useCallback(
  debounce(async (searchTerm) => {
    const results = await searchUsers(searchTerm);
    setSearchResults(results);
  }, 300),
  []
);
```

### 4. Caching cho dropdown data
```javascript
// Cache voucher và address data để tránh gọi API nhiều lần
const cachedData = useMemo(() => {
  return {
    addresses: addressData,
    vouchers: voucherData
  };
}, [userId]);
```

## 🚀 INTEGRATION EXAMPLES

### React Component Example:
```jsx
import React, { useState, useEffect } from 'react';

const CreateOrderForm = ({ userId, onSuccess }) => {
  const [addresses, setAddresses] = useState([]);
  const [vouchers, setVouchers] = useState([]);
  const [loading, setLoading] = useState(false);
  const [orderData, setOrderData] = useState({
    userId: userId,
    addressId: null,
    voucherIds: [],
    orderDetails: []
  });

  useEffect(() => {
    loadDropdownData();
  }, [userId]);

  const loadDropdownData = async () => {
    try {
      const [addressRes, voucherRes] = await Promise.all([
        fetch(`/api/addresses/user/${userId}/dropdown`),
        fetch(`/api/vouchers/user/${userId}/available`)
      ]);
      
      const addressData = await addressRes.json();
      const voucherData = await voucherRes.json();
      
      if (addressData.status === 200) setAddresses(addressData.data);
      if (voucherData.status === 200) setVouchers(voucherData.data);
    } catch (error) {
      console.error('Error loading dropdown data:', error);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    
    try {
      const response = await fetch('/api/orders', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(orderData)
      });
      
      const result = await response.json();
      
      if (result.status === 201) {
        onSuccess(result.data);
      } else {
        alert(result.message);
      }
    } catch (error) {
      alert('Lỗi kết nối: ' + error.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <form onSubmit={handleSubmit}>
      {/* Address dropdown */}
      <select 
        value={orderData.addressId || ''} 
        onChange={(e) => setOrderData({...orderData, addressId: e.target.value})}
      >
        <option value="">Chọn địa chỉ giao hàng</option>
        {addresses.map(addr => (
          <option key={addr.id} value={addr.id}>{addr.name}</option>
        ))}
      </select>

      {/* Voucher multi-select */}
      <select 
        multiple 
        value={orderData.voucherIds} 
        onChange={(e) => {
          const selected = Array.from(e.target.selectedOptions, option => parseInt(option.value));
          setOrderData({...orderData, voucherIds: selected});
        }}
      >
        {vouchers.map(voucher => (
          <option key={voucher.id} value={voucher.id}>{voucher.name}</option>
        ))}
      </select>

      <button type="submit" disabled={loading}>
        {loading ? 'Đang tạo đơn...' : 'Tạo đơn hàng'}
      </button>
    </form>
  );
};
```
