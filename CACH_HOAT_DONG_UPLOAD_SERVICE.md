# Cách Hoạt Động Bên Trong Service Upload Ảnh

## Tổng quan
File này giải thích cách backend xử lý upload và xóa ảnh để frontend hiểu rõ quy trình hoạt động.

## 1. Quy trình Upload nhiều ảnh

### Bước 1: Nhận request từ frontend
```
POST /api/upload/images/products
- Nhận: files[] (danh sách ảnh)
- Nhận: module = "products"
```

### Bước 2: Validate (Kiểm tra)
```java
// Backend kiểm tra:
- Module có hợp lệ không? (products, users, events...)
- File có phải ảnh không? (jpg, png, gif...)
- Kích thước file < 5MB?
- Số lượng file <= 5?
- Kích thước ảnh >= 200x200 pixel?
```

### Bước 3: Tạo thư mục lưu trữ
```
// Backend tự động tạo thư mục theo pattern:
uploads/
└── products/          <- module
    └── 2025/          <- năm hiện tại
        └── 06/        <- tháng hiện tại
```

### Bước 4: Đổi tên file và lưu
```java
// Với mỗi ảnh:
String originalName = "anh-san-pham.jpg"
String newName = "image" + timestamp + "_" + randomString + ".jpg"
// Ví dụ: image1719763200000_abc123xyz.jpg

// Lưu vào: uploads/products/2025/06/image1719763200000_abc123xyz.jpg
```

### Bước 5: Tạo URL trả về
```java
// Backend tạo URL đầy đủ:
String baseUrl = "http://localhost:8080"
String relativePath = "/uploads/products/2025/06/image1719763200000_abc123xyz.jpg"
String fullUrl = baseUrl + relativePath
```

### Bước 6: Trả về kết quả
```json
{
  "success": true,
  "urls": [
    "http://localhost:8080/uploads/products/2025/06/image1719763200000_abc123xyz.jpg",
    "http://localhost:8080/uploads/products/2025/06/image1719763200000_def456uvw.jpg"
  ],
  "message": "Upload successful"
}
```

## 2. Quy trình Xóa ảnh

### Bước 1: Nhận request xóa
```
DELETE /api/upload/image
Body: {
  "imageUrl": "http://localhost:8080/uploads/products/2025/06/image1719763200000_abc123xyz.jpg"
}
```

### Bước 2: Phân tích URL
```java
// Backend tách URL thành:
String fullUrl = "http://localhost:8080/uploads/products/2025/06/image1719763200000_abc123xyz.jpg"
String relativePath = "/uploads/products/2025/06/image1719763200000_abc123xyz.jpg"
String filePath = "uploads/products/2025/06/image1719763200000_abc123xyz.jpg"
```

### Bước 3: Kiểm tra file tồn tại
```java
File file = new File(filePath);
if (file.exists()) {
    // Tiếp tục xóa
} else {
    // Trả về lỗi "File không tồn tại"
}
```

### Bước 4: Xóa file khỏi hệ thống
```java
boolean deleted = file.delete();
if (deleted) {
    // Xóa thành công
} else {
    // Xóa thất bại
}
```

## 3. Cấu trúc thư mục chi tiết

### Tại sao lại tổ chức theo năm/tháng?
```
uploads/
├── products/
│   ├── 2025/
│   │   ├── 01/        <- Tháng 1/2025
│   │   ├── 02/        <- Tháng 2/2025
│   │   └── 06/        <- Tháng 6/2025 (hiện tại)
│   └── 2026/          <- Năm sau
│       └── 01/
├── users/
│   └── 2025/
│       └── 06/
└── events/
    └── 2025/
        └── 06/
```

**Lý do:**
- Dễ quản lý file theo thời gian
- Tránh quá nhiều file trong 1 thư mục
- Dễ backup/cleanup theo tháng

## 4. Tại sao đổi tên file?

### File gốc từ frontend:
```
"anh-san-pham.jpg"
"product-image.png"
"avatar.jpg"
```

### File sau khi backend xử lý:
```
"image1719763200000_abc123xyz.jpg"
"image1719763201000_def456uvw.png"
"image1719763202000_ghi789abc.jpg"
```

**Lý do đổi tên:**
- ✅ Tránh trùng tên file
- ✅ Bảo mật (không lộ tên file gốc)
- ✅ Dễ sắp xếp theo thời gian
- ✅ Tránh ký tự đặc biệt gây lỗi

## 5. Xử lý lỗi phổ biến

### Lỗi khi upload:
```json
// File không phải ảnh
{
  "error": "Invalid file type. Only JPG, PNG, GIF, WebP allowed",
  "code": "INVALID_FILE_TYPE"
}

// File quá lớn
{
  "error": "File size exceeds 5MB limit",
  "code": "FILE_TOO_LARGE"
}

// Module không hợp lệ
{
  "error": "Invalid module. Allowed: events, users, products, categories, orders, reviews",
  "code": "INVALID_MODULE"
}
```

### Lỗi khi xóa:
```json
// File không tồn tại
{
  "error": "File not found",
  "code": "FILE_NOT_FOUND"
}

// Không có quyền xóa
{
  "error": "Permission denied",
  "code": "PERMISSION_DENIED"
}
```

## 6. Flow hoàn chỉnh cho Frontend

### Upload flow:
```
1. User chọn file → Frontend validate cơ bản
2. Frontend gửi POST request → Backend validate chi tiết
3. Backend lưu file → Tạo URL
4. Backend trả URL → Frontend lưu URL vào database
5. Frontend hiển thị ảnh → User thấy ảnh đã upload
```

### Delete flow:
```
1. User click xóa → Frontend lấy URL từ database
2. Frontend gửi DELETE request → Backend tìm file
3. Backend xóa file → Trả về kết quả
4. Frontend xóa URL khỏi database → Ảnh biến mất
```

## 7. Lưu ý quan trọng cho Frontend

### URL được trả về:
- ✅ Có thể dùng trực tiếp trong `<img src="...">`
- ✅ Có thể lưu vào database
- ✅ Có thể share cho user khác

### Khi xóa ảnh:
- ⚠️ Phải xóa cả URL trong database
- ⚠️ Không thể khôi phục sau khi xóa
- ⚠️ Nên confirm trước khi xóa

### Best practices:
- 📝 Luôn check response.success trước khi sử dụng URL
- 📝 Handle error cases một cách graceful
- 📝 Show loading state khi upload
- 📝 Validate file ở frontend trước khi gửi (tăng UX)
