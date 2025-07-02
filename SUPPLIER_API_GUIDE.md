# TÀI LIỆU API QUẢN LÝ NHÀ CUNG CẤP (SUPPLIER API)

## TỔNG QUAN

API Nhà cung cấp cung cấp đầy đủ các chức năng CRUD (Create, Read, Update, Delete) và các tính năng nâng cao như phân trang, tìm kiếm, lọc theo nhiều tiêu chí.

**Base URL:** `/api/suppliers`

---

## 1. API LẤY DANH SÁCH NHÀ CUNG CẤP (GET LIST)

### Endpoint
```
GET /api/suppliers
```

### Tham số truy vấn (Query Parameters)
| Tham số | Loại | Bắt buộc | Mặc định | Mô tả |
|---------|------|----------|----------|-------|
| page | int | Không | 0 | Trang hiện tại (bắt đầu từ 0) |
| size | int | Không | 10 | Số lượng nhà cung cấp trên mỗi trang |
| supplierName | String | Không | null | Tên nhà cung cấp để tìm kiếm |
| contactName | String | Không | null | Tên người liên hệ để tìm kiếm |
| email | String | Không | null | Email để tìm kiếm |
| status | String | Không | null | Trạng thái để lọc |

### Ví dụ Request
```http
GET /api/suppliers?page=0&size=10&supplierName=Fahasa&status=1
```

### Response thành công (200)
```json
{
  "content": [
    {
      "id": 1,
      "supplierName": "Công ty Fahasa",
      "address": "40 Nguyễn Huệ, Quận 1, TP.HCM",
      "contactName": "Lê Văn A",
      "email": "supplier@fahasa.com",
      "phone": "028-3822-5798",
      "website": "https://fahasa.com",
      "description": "Nhà phân phối sách hàng đầu Việt Nam",
      "status": 1,
      "createdBy": "admin",
      "updatedBy": "admin",
      "createdAt": 1625097600000,
      "updatedAt": 1625097600000
    }
  ],
  "pageNumber": 0,
  "pageSize": 10,
  "totalElements": 15,
  "totalPages": 2
}
```

### Cấu trúc dữ liệu SupplierResponse
| Trường | Loại | Mô tả |
|--------|------|-------|
| id | Integer | ID nhà cung cấp |
| supplierName | String | Tên nhà cung cấp |
| address | String | Địa chỉ |
| contactName | String | Tên người liên hệ |
| email | String | Email liên hệ |
| phone | String | Số điện thoại |
| website | String | Website |
| description | String | Mô tả |
| status | Integer | Trạng thái (1: Hoạt động, 0: Không hoạt động) |
| createdBy | String | Người tạo |
| updatedBy | String | Người cập nhật |
| createdAt | Long | Thời gian tạo (timestamp) |
| updatedAt | Long | Thời gian cập nhật (timestamp) |

---

## 2. API TẠO NHÀ CUNG CẤP MỚI (POST)

### Endpoint
```
POST /api/suppliers
```

### Request Body
```json
{
  "supplierName": "Công ty Thái Hà Books",
  "address": "56 Đỗ Quang Đẩu, Phường Trung Hoà, Cầu Giấy, Hà Nội",
  "contactName": "Nguyễn Thị B",
  "email": "contact@thaihabooks.com",
  "phone": "024-6253-4435",
  "website": "https://thaihabooks.com",
  "description": "Nhà phân phối sách giáo dục và học tập",
  "status": 1,
  "createdBy": "admin",
  "updatedBy": "admin"
}
```

### Cấu trúc SupplierRequest
| Trường | Loại | Bắt buộc | Mô tả | Validation |
|--------|------|----------|-------|------------|
| supplierName | String | Có | Tên nhà cung cấp | Duy nhất, không được trống, max 255 ký tự |
| address | String | Không | Địa chỉ | Max 500 ký tự |
| contactName | String | Không | Tên người liên hệ | Max 100 ký tự |
| email | String | Không | Email liên hệ | Format email hợp lệ, max 100 ký tự |
| phone | String | Không | Số điện thoại | Max 20 ký tự |
| website | String | Không | Website | URL hợp lệ, max 255 ký tự |
| description | String | Không | Mô tả | Max 1000 ký tự |
| status | Integer | Không | Trạng thái | 1 hoặc 0, mặc định 1 |
| createdBy | String | Có | Người tạo | Không được trống |
| updatedBy | String | Có | Người cập nhật | Không được trống |

### Response thành công (200)
```
Không có nội dung (Void response)
```

### Response lỗi
```json
{
  "error": "Tên nhà cung cấp đã tồn tại",
  "status": 400
}
```

---

## 3. API CẬP NHẬT NHÀ CUNG CẤP (PUT)

### Endpoint
```
PUT /api/suppliers
```

**Lưu ý:** API này không sử dụng ID trong path mà trong request body.

### Request Body
```json
{
  "id": 1,
  "supplierName": "Công ty Fahasa - Cập nhật",
  "address": "40 Nguyễn Huệ, Quận 1, TP.HCM",
  "contactName": "Lê Văn A",
  "email": "supplier@fahasa.com",
  "phone": "028-3822-5798",
  "website": "https://fahasa.com",
  "description": "Nhà phân phối sách hàng đầu Việt Nam - Đã cập nhật",
  "status": 1,
  "updatedBy": "admin"
}
```

### Response thành công (200)
```
Không có nội dung (Void response)
```

### Response lỗi
```json
{
  "error": "Không tìm thấy nhà cung cấp với ID: 999",
  "status": 404
}
```

---

## 4. API XÓA NHÀ CUNG CẤP (DELETE)

### Endpoint
```
DELETE /api/suppliers/{id}
```

### Tham số đường dẫn
| Tham số | Loại | Bắt buộc | Mô tả |
|---------|------|----------|-------|
| id | Integer | Có | ID của nhà cung cấp cần xóa |

### Response thành công (200)
```
Không có nội dung (Void response)
```

### Response lỗi
```json
{
  "error": "Không thể xóa nhà cung cấp đang được sử dụng",
  "status": 400
}
```

---

## 5. API CẬP NHẬT TRẠNG THÁI NHÀ CUNG CẤP

### Endpoint
```
PATCH /api/suppliers/status
```

### Tham số truy vấn
| Tham số | Loại | Bắt buộc | Mô tả |
|---------|------|----------|-------|
| id | Integer | Có | ID của nhà cung cấp |
| status | byte | Có | Trạng thái mới (1: Active, 0: Inactive) |
| updatedBy | String | Có | Người cập nhật |

### Ví dụ Request
```http
PATCH /api/suppliers/status?id=1&status=0&updatedBy=admin
```

### Response thành công (200)
```
Không có nội dung (Void response)
```

### Response lỗi
```json
{
  "error": "Không tìm thấy nhà cung cấp với ID: 999",
  "status": 404
}
```

---

## 6. API DROPDOWN NHÀ CUNG CẤP

### Endpoint
```
GET /api/suppliers/dropdown
```

### Response thành công (200)
```json
{
  "status": 200,
  "message": "Lấy danh sách nhà cung cấp thành công",
  "data": [
    {
      "id": 1,
      "name": "Công ty Fahasa"
    },
    {
      "id": 2,
      "name": "Công ty Thái Hà Books"
    },
    {
      "id": 3,
      "name": "Nhà sách Trí Tuệ"
    }
  ]
}
```

---

## MÃ TRẠNG THÁI HTTP

| Mã | Ý nghĩa | Mô tả |
|----|---------|--------|
| 200 | OK | Thành công |
| 400 | Bad Request | Dữ liệu không hợp lệ |
| 404 | Not Found | Không tìm thấy tài nguyên |
| 500 | Internal Server Error | Lỗi server |

---

## VALIDATION VÀ BUSINESS RULES

### 1. Tên nhà cung cấp (supplierName)
- Bắt buộc phải có
- Độ dài tối đa 255 ký tự
- Phải duy nhất trong hệ thống
- Không phân biệt hoa thường khi kiểm tra trùng lặp

### 2. Email
- Phải đúng format email
- Độ dài tối đa 100 ký tự
- Phải duy nhất trong hệ thống nếu có

### 3. Số điện thoại (phone)
- Độ dài tối đa 20 ký tự
- Chỉ chấp nhận số và các ký tự đặc biệt (+, -, space)

### 4. Website
- Phải đúng format URL nếu có
- Độ dài tối đa 255 ký tự

### 5. Trạng thái (status)
- 1: Hoạt động (Active)
- 0: Không hoạt động (Inactive)
- Mặc định là 1 khi tạo mới

### 6. Ràng buộc xóa
- Không thể xóa nhà cung cấp đang được sử dụng bởi sách
- Chỉ có thể thay đổi trạng thái thành không hoạt động

---

## NOTES QUAN TRỌNG CHO FRONTEND

### 1. 🔥 DROPDOWN API CHO FORM TẠO/SỬA SÁCH
- **Endpoint:** `GET /api/suppliers/dropdown`
- **Format response:** `{id: number, name: string}`
- **Cache:** Nên cache response và refresh khi có thay đổi

### 2. 🔥 VALIDATION BẮT BUỘC
- **supplierName:** Unique, không được trùng lặp
- **email:** Phải đúng format email và unique
- **createdBy/updatedBy:** Bắt buộc phải có

### 3. 🔥 KHÁC BIỆT VỚI CÁC API KHÁC
- **PUT API không có ID trong path:** Gửi ID trong request body
- **Response không có ApiResponse wrapper:** Trả về trực tiếp data hoặc void
- **Error handling khác:** Không có format ApiResponse chuẩn

### 4. Phân trang
- Trang bắt đầu từ 0
- Size mặc định là 10 (khác với Book API là 5)
- Luôn kiểm tra totalPages để tránh request trang không tồn tại

### 5. Timestamp
- Tất cả các trường thời gian đều là timestamp (milliseconds)
- Frontend cần convert để hiển thị cho user

### 6. Tìm kiếm và lọc
- Tất cả tham số tìm kiếm đều optional
- Có thể kết hợp nhiều tham số cùng lúc
- supplierName search hỗ trợ tìm kiếm gần đúng

### 7. Error Handling
- Response format khác với các API khác
- Không có ApiResponse wrapper
- Error message có thể không nhất quán

---

## CURL EXAMPLES

### Lấy danh sách nhà cung cấp với phân trang
```bash
curl -X GET "http://localhost:8080/api/suppliers?page=0&size=10&supplierName=Fahasa&status=1" \
-H "Content-Type: application/json"
```

### Tạo nhà cung cấp mới
```bash
curl -X POST "http://localhost:8080/api/suppliers" \
-H "Content-Type: application/json" \
-d '{
  "supplierName": "Công ty Văn Phẩm ABC",
  "address": "123 Trần Hưng Đạo, Quận 1, TP.HCM",
  "contactName": "Trần Văn C",
  "email": "contact@vanphamabc.com",
  "phone": "028-3923-4567",
  "website": "https://vanphamabc.com",
  "description": "Chuyên cung cấp văn phòng phẩm và sách giáo khoa",
  "status": 1,
  "createdBy": "admin",
  "updatedBy": "admin"
}'
```

### Cập nhật nhà cung cấp
```bash
curl -X PUT "http://localhost:8080/api/suppliers" \
-H "Content-Type: application/json" \
-d '{
  "id": 1,
  "supplierName": "Công ty Fahasa - Cập nhật",
  "address": "40 Nguyễn Huệ, Quận 1, TP.HCM",
  "contactName": "Lê Văn A",
  "email": "supplier@fahasa.com",
  "phone": "028-3822-5798",
  "website": "https://fahasa.com",
  "description": "Nhà phân phối sách hàng đầu Việt Nam - Đã cập nhật",
  "status": 1,
  "updatedBy": "admin"
}'
```

### Cập nhật trạng thái nhà cung cấp
```bash
curl -X PATCH "http://localhost:8080/api/suppliers/status?id=1&status=0&updatedBy=admin" \
-H "Content-Type: application/json"
```

### Xóa nhà cung cấp
```bash
curl -X DELETE "http://localhost:8080/api/suppliers/1" \
-H "Content-Type: application/json"
```

### Lấy dropdown nhà cung cấp
```bash
curl -X GET "http://localhost:8080/api/suppliers/dropdown" \
-H "Content-Type: application/json"
```

---

## ⚠️ CHÚ Ý ĐẶC BIỆT

### 1. Response Format Không Nhất Quán
- API Supplier không sử dụng ApiResponse wrapper như các API khác
- GET List trả về trực tiếp PaginationResponse
- POST/PUT/DELETE trả về void
- Dropdown có ApiResponse wrapper

### 2. Error Handling Khác Biệt
- Không có format lỗi chuẩn như các API khác
- Cần handle lỗi theo từng trường hợp cụ thể

### 3. API Design Khác Biệt
- PUT không có ID trong path (khác với Book, Publisher API)
- PATCH status có format tham số khác

### 4. Khuyến Nghị
- Frontend nên có consistent error handling cho Supplier API
- Cân nhắc refactor để thống nhất response format với các API khác
- Test kỹ các edge case do response format khác biệt

---

**Lưu ý:** Tài liệu này được tạo dựa trên phân tích source code hiện tại. API Supplier có design khác biệt so với các API khác trong hệ thống. Đảm bảo test tất cả các API trước khi triển khai frontend.
