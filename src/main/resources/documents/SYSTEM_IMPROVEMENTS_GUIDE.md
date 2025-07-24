# 📋 TÀI LIỆU THAY ĐỔI HỆ THỐNG QUẢN LÝ ĐƠN HÀNG VÀ TÍCH ĐIỂM

## 🎯 TỔNG QUAN CÁC CẢI TIẾN MỚI

### 1. ✅ THÊM HÌNH THỨC SÁCH (BookFormat)
- **Enum mới**: `BookFormat` với các giá trị: HARDCOVER, PAPERBACK, AUDIOBOOK, EBOOK, MAGAZINE, COMIC, TEXTBOOK, NOTEBOOK, JOURNAL, WORKBOOK
- **Entity**: Thêm field `bookFormat` vào `Book` entity
- **Database**: Thêm cột `book_format` vào bảng `book`

### 2. ✅ VALIDATION GIÁ SẢN PHẨM KHI ĐẶT HÀNG
- **Mục đích**: Ngăn chặn đặt hàng với giá cũ khi admin đã thay đổi giá hoặc flash sale
- **Cơ chế**: Frontend gửi giá hiện tại, backend so sánh với giá thực tế
- **Service**: `PriceValidationService` xử lý validation logic

### 3. ✅ HỆ THỐNG TÍCH ĐIỂM VÀ RANK TỰ ĐỘNG
- **Tích điểm**: Tự động tích điểm khi đơn hàng DELIVERED
- **Trừ điểm**: Tự động trừ điểm khi hủy/trả đơn hàng đã tích điểm
- **Rank**: Tự động cập nhật rank dựa trên tổng chi tiêu
- **Multiplier**: Rank cao có hệ số tích điểm cao hơn

### 4. ✅ QUẢN LÝ VOUCHER CHẶT CHẼ
- **Validation**: Kiểm tra voucher hợp lệ trước khi sử dụng
- **Usage limit**: Kiểm tra giới hạn số lần sử dụng tổng và per user
- **Refund**: Tự động hoàn lại voucher khi hủy đơn hàng

### 5. ✅ LOGIC CHUYỂN TRẠNG THÁI ĐƠN HÀNG
- **Business Rules**: Ràng buộc chuyển trạng thái theo quy tắc nghiệp vụ
- **Auto Processing**: Tự động xử lý điểm và voucher khi chuyển trạng thái

---

## 🔧 CHI TIẾT CÁC API MỚI/CẬP NHẬT

### 📍 **1. BOOK APIs - Thêm BookFormat**

#### GET /api/books/{id}
**Response mới**:
```json
{
  "status": 200,
  "message": "Thành công",
  "data": {
    "id": 1,
    "bookName": "Tên sách",
    "price": 50000,
    "bookFormat": "PAPERBACK",  // ✅ MỚI
    // ... các field khác
  }
}
```

#### POST/PUT /api/books
**Request mới**:
```json
{
  "bookName": "Tên sách",
  "price": 50000,
  "bookFormat": "HARDCOVER",  // ✅ MỚI - ENUM value
  // ... các field khác
}
```

---

### 📍 **2. ORDER APIs - Validation và Point Management**

#### POST /api/orders
**Request với validation giá (chỉ truyền một trường giá)**:
```json
{
  "userId": 1,
  "addressId": 1,
  "orderDetails": [
    {
      "bookId": 1,
      "quantity": 2,
      "frontendPrice": 50000 // ✅ Giá hiện tại frontend biết, backend tự xác định loại giá (thường/flash sale)
    }
  ],
  "voucherIds": [1, 2],
  // ... các field khác
}
```

**Response khi giá thay đổi**:
```json
{
  "status": 400,
  "message": "Giá sản phẩm đã thay đổi: Giá của sách 'Tên sách' đã thay đổi từ 50,000 VND thành 45,000 VND",
  "data": null
}
```

#### POST /api/orders/validate-prices
**Mục đích**: Validate giá trước khi đặt hàng
**Request**: Giống POST /api/orders (chỉ truyền một trường giá frontendPrice)
**Response**: 
```json
{
  "status": 200,
  "message": "Tất cả giá sản phẩm hợp lệ",
  "data": "valid"
}
```

#### PATCH /api/orders/{id}/status
**Chức năng mới**: Tự động xử lý điểm và voucher
**Request**: 
```
PATCH /api/orders/123/status?newStatus=DELIVERED&staffId=2
```

**Logic tự động**:
- `DELIVERED`: Tự động tích điểm cho user
- `CANCELED`: Hoàn voucher + trừ điểm (nếu đã tích)
- `RETURNED`: Hoàn voucher + trừ điểm

---

### 📍 **3. POINT APIs - Tự động hóa**

#### Tích điểm tự động
- **Trigger**: Khi order chuyển thành DELIVERED
- **Formula**: `points = (totalAmount / 1000) * rankMultiplier`
- **Rank Multiplier**: VÀNG=1.5, BẠC=1.2, KIM CƯƠNG=2.0

#### Trừ điểm tự động  
- **Trigger**: Khi hủy/trả đơn hàng đã tích điểm
- **Logic**: Trừ chính xác số điểm đã tích cho đơn hàng đó

---

### 📍 **4. VOUCHER APIs - Validation chặt chẽ**

#### Validation Rules
1. **Thời gian**: Voucher phải trong thời gian hiệu lực
2. **Số lượng**: Voucher chưa hết usage_limit
3. **Per User**: User chưa vượt quá usage_limit_per_user
4. **Status**: Voucher phải có status = 1

#### Auto Refund
- Khi hủy đơn hàng: Tự động giảm used_count của voucher

---

## 🎯 LUỒNG NGHIỆP VỤ MỚI

### 1. **Luồng Đặt Hàng**
```
Frontend → Validate Prices → Create Order → Use Vouchers → Success
     ↓              ↓              ↓            ↓           ↓
   Gửi giá    So sánh giá    Validation    Update       Tạo đơn
   hiện tại   với backend    voucher       used_count   thành công
```

### 2. **Luồng Chuyển Trạng Thái**
```
Admin Update Status → Validate Transition → Auto Process → Update Rank
        ↓                     ↓                  ↓             ↓
   PATCH /status      Kiểm tra rules      Tích/trừ điểm   Cập nhật rank
```

### 3. **Luồng Hủy Đơn Hàng**
```
Cancel Order → Refund Vouchers → Deduct Points → Update Rank
     ↓               ↓               ↓              ↓
  Đổi status     Giảm used_count   Trừ điểm      Cập nhật rank
```

---

## ⚠️ QUY TẮC CHUYỂN TRẠNG THÁI

### Allowed Transitions:
- **PENDING** → CONFIRMED, CANCELED
- **CONFIRMED** → SHIPPED, CANCELED  
- **SHIPPED** → DELIVERED, RETURNED
- **DELIVERED** → RETURNED
- **CANCELED** → PENDING (khôi phục)
- **RETURNED** → không chuyển được

### Business Logic:
- **Tích điểm**: Chỉ khi DELIVERED lần đầu
- **Trừ điểm**: Khi CANCELED/RETURNED từ DELIVERED
- **Voucher refund**: Khi CANCELED/RETURNED

---


