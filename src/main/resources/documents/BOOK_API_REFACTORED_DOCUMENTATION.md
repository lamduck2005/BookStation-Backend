# 📚 BOOK API DOCUMENTATION - REFACTORED VERSION

## 🔧 API Improvements

### ✅ What's Fixed:
1. **Clean RESTful Design**: Sử dụng proper DTO thay vì Map raw
2. **Validation**: Sử dụng `@Valid` với Bean Validation  
3. **Separation of Concerns**: Tách logic business ra khỏi controller
4. **Type Safety**: Thay thế `Map<String, Object>` bằng typed DTO
5. **Error Handling**: Consistent error response format

---

## 📋 API ENDPOINTS

### 1. 🔽 GET Dropdown Books (Enhanced)
```http
GET /api/books/dropdown
```

**Response:**
```json
{
  "status": 200,
  "message": "Lấy danh sách sách thành công",
  "data": [
    {
      "id": 1,
      "name": "Sách Example",
      "normalPrice": 150000,      // Giá bình thường (đã tính discount nếu có)
      "flashSalePrice": 120000,   // Giá flash sale (null nếu không có)
      "isFlashSale": true         // Có đang flash sale không
    },
    {
      "id": 2,
      "name": "Sách Example 2",
      "normalPrice": 200000,
      "flashSalePrice": null,
      "isFlashSale": false
    }
  ]
}
```

**Logic tính giá:**
- `normalPrice`: Giá gốc - discount (nếu có) 
- `flashSalePrice`: Giá flash sale (ưu tiên cao nhất)
- `isFlashSale`: `true` nếu có flash sale active

---

### 2. ✅ POST Validate Quantity (New)
```http
POST /api/books/validate-quantity
```

**Request:**
```json
{
  "bookId": 1,
  "quantity": 5
}
```

**Response (Success):**
```json
{
  "status": 200,
  "message": "Validate thành công",
  "data": {
    "valid": true,
    "message": "Số lượng hợp lệ",
    "availableQuantity": 100
  }
}
```

**Response (Failed):**
```json
{
  "status": 200,
  "message": "Validate thành công", 
  "data": {
    "valid": false,
    "message": "Số lượng không hợp lệ, tồn kho hiện tại: 3",
    "availableQuantity": 3
  }
}
```

**Response (Book Not Found):**
```json
{
  "status": 200,
  "message": "Validate thất bại",
  "data": {
    "valid": false,
    "message": "Không tìm thấy sách",
    "availableQuantity": 0
  }
}
```

**Validation Rules:**
- `bookId`: Required, not null
- `quantity`: Required, > 0

---
 
## 📝 ERROR HANDLING

### Validation Errors:
```json
{
  "status": 400,
  "message": "Validation failed",
  "data": {
    "bookId": "ID sách không được để trống",
    "quantity": "Số lượng phải lớn hơn 0"
  }
}
```

### Business Logic Errors:
```json
{
  "status": 200,
  "message": "Validate thành công",
  "data": {
    "valid": false,
    "message": "Số lượng không hợp lệ, tồn kho hiện tại: 5",
    "availableQuantity": 5
  }
}
```

---

 