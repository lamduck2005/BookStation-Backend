# TÀI LIỆU API QUẢN LÝ TÁC GIẢ (AUTHOR API)

## TỔNG QUAN

API Tác giả cung cấp đầy đủ các chức năng CRUD (Create, Read, Update, Delete) và các tính năng nâng cao như phân trang, tìm kiếm, lọc theo nhiều tiêu chí.

**Base URL:** `/api/authors`

---

## 1. API LẤY DANH SÁCH TÁC GIẢ (GET LIST)

### Endpoint
```
GET /api/authors
```

### Tham số truy vấn (Query Parameters)
| Tham số | Loại | Bắt buộc | Mặc định | Mô tả |
|---------|------|----------|----------|-------|
| page | int | Không | 0 | Trang hiện tại (bắt đầu từ 0) |
| size | int | Không | 5 | Số lượng tác giả trên mỗi trang |
| name | String | Không | null | Tên tác giả để tìm kiếm |
| status | Byte | Không | null | Trạng thái (1: Active, 0: Inactive) |

### Ví dụ Request
```http
GET /api/authors?page=0&size=10&name=Nguyễn&status=1
```

### Response thành công (200)
```json
{
  "status": 200,
  "message": "Thành công",
  "data": {
    "content": [
      {
        "id": 1,
        "authorName": "Nguyễn Nhật Ánh",
        "biography": "Nhà văn nổi tiếng với nhiều tác phẩm thiếu nhi",
        "birthDate": "1955-05-07",
        "status": 1,
        "createdAt": 1625097600000,
        "updatedAt": 1625097600000
      }
    ],
    "pageNumber": 0,
    "pageSize": 10,
    "totalElements": 25,
    "totalPages": 3
  }
}
```

### Cấu trúc dữ liệu AuthorResponse
| Trường | Loại | Mô tả |
|--------|------|-------|
| id | Integer | ID tác giả |
| authorName | String | Tên tác giả |
| biography | String | Tiểu sử tác giả |
| birthDate | LocalDate | Ngày sinh (format: YYYY-MM-DD) |
| status | Byte | Trạng thái (1: Hoạt động, 0: Không hoạt động) |
| createdAt | Long | Thời gian tạo (timestamp) |
| updatedAt | Long | Thời gian cập nhật (timestamp) |

---

## 2. API LẤY CHI TIẾT TÁC GIẢ THEO ID

### Endpoint
```
GET /api/authors/{id}
```

### Tham số đường dẫn
| Tham số | Loại | Bắt buộc | Mô tả |
|---------|------|----------|-------|
| id | Integer | Có | ID của tác giả |

### Ví dụ Request
```http
GET /api/authors/1
```

### Response thành công (200)
```json
{
  "status": 200,
  "message": "Thành công",
  "data": {
    "id": 1,
    "authorName": "Nguyễn Nhật Ánh",
    "biography": "Nhà văn nổi tiếng với nhiều tác phẩm thiếu nhi",
    "birthDate": "1955-05-07",
    "status": 1,
    "createdAt": 1625097600000,
    "updatedAt": 1625097600000
  }
}
```

### Response lỗi (404)
```json
{
  "status": 404,
  "message": "Không tìm thấy tác giả",
  "data": null
}
```

---

## 3. API TẠO TÁC GIẢ MỚI (POST)

### Endpoint
```
POST /api/authors
```

### Request Body
```json
{
  "authorName": "Tô Hoài",
  "biography": "Nhà văn nổi tiếng với tác phẩm Dế Mèn phiêu lưu ký",
  "birthDate": "1920-09-27",
  "status": 1
}
```

### Cấu trúc AuthorRequest
| Trường | Loại | Bắt buộc | Mô tả | Validation |
|--------|------|----------|-------|------------|
| authorName | String | Có | Tên tác giả | Duy nhất, không được trống, max 255 ký tự |
| biography | String | Không | Tiểu sử tác giả | Max 2000 ký tự |
| birthDate | LocalDate | Không | Ngày sinh | Format: YYYY-MM-DD |
| status | Byte | Không | Trạng thái | 1 hoặc 0, mặc định 1 |

### Response thành công (200)
```json
{
  "status": 200,
  "message": "Tạo tác giả thành công",
  "data": {
    "id": 2,
    "authorName": "Tô Hoài",
    "biography": "Nhà văn nổi tiếng với tác phẩm Dế Mèn phiêu lưu ký",
    "birthDate": "1920-09-27",
    "status": 1,
    "createdAt": 1704067200000,
    "updatedAt": 1704067200000
  }
}
```

### Response lỗi

#### Tên tác giả đã tồn tại (400)
```json
{
  "status": 400,
  "message": "Tên tác giả đã tồn tại",
  "data": null
}
```

---

## 4. API CẬP NHẬT TÁC GIẢ (PUT)

### Endpoint
```
PUT /api/authors/{id}
```

### Tham số đường dẫn
| Tham số | Loại | Bắt buộc | Mô tả |
|---------|------|----------|-------|
| id | Integer | Có | ID của tác giả cần cập nhật |

### Request Body
```json
{
  "authorName": "Tô Hoài",
  "biography": "Nhà văn nổi tiếng với tác phẩm Dế Mèn phiêu lưu ký - Cập nhật",
  "birthDate": "1920-09-27",
  "status": 1
}
```

### Response thành công (200)
```json
{
  "status": 200,
  "message": "Cập nhật tác giả thành công",
  "data": {
    "id": 2,
    "authorName": "Tô Hoài",
    "biography": "Nhà văn nổi tiếng với tác phẩm Dế Mèn phiêu lưu ký - Cập nhật",
    "birthDate": "1920-09-27",
    "status": 1,
    "createdAt": 1704067200000,
    "updatedAt": 1704153600000
  }
}
```

### Response lỗi

#### Không tìm thấy tác giả (404)
```json
{
  "status": 404,
  "message": "Không tìm thấy tác giả",
  "data": null
}
```

#### Tên tác giả đã tồn tại (400)
```json
{
  "status": 400,
  "message": "Tên tác giả đã tồn tại",
  "data": null
}
```

---

## 5. API XÓA TÁC GIẢ (DELETE)

### Endpoint
```
DELETE /api/authors/{id}
```

### Tham số đường dẫn
| Tham số | Loại | Bắt buộc | Mô tả |
|---------|------|----------|-------|
| id | Integer | Có | ID của tác giả cần xóa |

### Response thành công (204)
```
Không có nội dung (No Content)
```

### Response lỗi

#### Không thể xóa tác giả đang được sử dụng (400)
```json
{
  "status": 400,
  "message": "Không thể xóa tác giả đang được sử dụng bởi sách",
  "data": null
}
```

---

## 6. API CHUYỂN TRẠNG THÁI TÁC GIẢ

### Endpoint
```
PATCH /api/authors/{id}/toggle-status
```

### Tham số đường dẫn
| Tham số | Loại | Bắt buộc | Mô tả |
|---------|------|----------|-------|
| id | Integer | Có | ID của tác giả |

### Response thành công (200)
```json
{
  "status": 200,
  "message": "Cập nhật trạng thái thành công",
  "data": {
    "id": 1,
    "authorName": "Nguyễn Nhật Ánh",
    "biography": "Nhà văn nổi tiếng với nhiều tác phẩm thiếu nhi",
    "birthDate": "1955-05-07",
    "status": 0,
    "createdAt": 1625097600000,
    "updatedAt": 1704153600000
  }
}
```

### Response lỗi (404)
```json
{
  "status": 404,
  "message": "Không tìm thấy",
  "data": null
}
```

---

## 7. API DROPDOWN TÁC GIẢ

### Endpoint
```
GET /api/authors/dropdown
```

### Response thành công (200)
```json
{
  "status": 200,
  "message": "Lấy danh sách tác giả thành công",
  "data": [
    {
      "id": 1,
      "name": "Nguyễn Nhật Ánh"
    },
    {
      "id": 2,
      "name": "Tô Hoài"
    },
    {
      "id": 3,
      "name": "Nam Cao"
    }
  ]
}
```

---

## MÃ TRẠNG THÁI HTTP

| Mã | Ý nghĩa | Mô tả |
|----|---------|--------|
| 200 | OK | Thành công |
| 204 | No Content | Xóa thành công |
| 400 | Bad Request | Dữ liệu không hợp lệ |
| 404 | Not Found | Không tìm thấy tài nguyên |
| 500 | Internal Server Error | Lỗi server |

---

## VALIDATION VÀ BUSINESS RULES

### 1. Tên tác giả (authorName)
- Bắt buộc phải có
- Độ dài tối đa 255 ký tự
- Phải duy nhất trong hệ thống
- Không phân biệt hoa thường khi kiểm tra trùng lặp

### 2. Tiểu sử (biography)
- Tùy chọn
- Độ dài tối đa 2000 ký tự
- Có thể chứa HTML tags đơn giản

### 3. Ngày sinh (birthDate)
- Tùy chọn
- Format: YYYY-MM-DD (LocalDate)
- Phải là ngày trong quá khứ
- Không được lớn hơn ngày hiện tại

### 4. Trạng thái (status)
- 1: Hoạt động (Active)
- 0: Không hoạt động (Inactive)
- Mặc định là 1 khi tạo mới

### 5. Ràng buộc xóa
- Không thể xóa tác giả đang được liên kết với sách
- Chỉ có thể thay đổi trạng thái thành không hoạt động

---

## NOTES QUAN TRỌNG CHO FRONTEND

### 1. 🔥 DROPDOWN API CHO FORM TẠO/SỬA SÁCH
- **Endpoint:** `GET /api/authors/dropdown`
- **Format response:** `{id: number, name: string}`
- **Cache:** Nên cache response và refresh khi có thay đổi
- **Quan trọng:** API này được dùng trong form tạo/sửa sách

### 2. 🔥 VALIDATION BẮT BUỘC
- **authorName:** Unique, không được trùng lặp
- **birthDate:** Phải đúng format YYYY-MM-DD và không được lớn hơn ngày hiện tại

### 3. Phân trang
- Trang bắt đầu từ 0
- Size mặc định là 5
- Luôn kiểm tra totalPages để tránh request trang không tồn tại

### 4. Date Handling
- birthDate sử dụng LocalDate format (YYYY-MM-DD)
- createdAt/updatedAt sử dụng timestamp (milliseconds)
- Frontend cần convert đúng format

### 5. Tìm kiếm và lọc
- Tất cả tham số tìm kiếm đều optional
- Có thể kết hợp nhiều tham số cùng lúc
- name search hỗ trợ tìm kiếm gần đúng

### 6. Error Handling
- Luôn kiểm tra status code trong response
- Message được trả về bằng tiếng Việt
- Data sẽ là null khi có lỗi

### 7. Performance
- Sử dụng API dropdown cho các combobox
- Cache danh sách tác giả cho form sách
- Implement pagination để tránh load quá nhiều dữ liệu

---

## CURL EXAMPLES

### Lấy danh sách tác giả với phân trang
```bash
curl -X GET "http://localhost:8080/api/authors?page=0&size=10&name=Nguyễn&status=1" \
-H "Content-Type: application/json"
```

### Tạo tác giả mới
```bash
curl -X POST "http://localhost:8080/api/authors" \
-H "Content-Type: application/json" \
-d '{
  "authorName": "Haruki Murakami",
  "biography": "Tiểu thuyết gia nổi tiếng người Nhật Bản",
  "birthDate": "1949-01-12",
  "status": 1
}'
```

### Cập nhật tác giả
```bash
curl -X PUT "http://localhost:8080/api/authors/1" \
-H "Content-Type: application/json" \
-d '{
  "authorName": "Nguyễn Nhật Ánh",
  "biography": "Nhà văn nổi tiếng với nhiều tác phẩm thiếu nhi - Cập nhật",
  "birthDate": "1955-05-07",
  "status": 1
}'
```

### Chuyển trạng thái tác giả
```bash
curl -X PATCH "http://localhost:8080/api/authors/1/toggle-status" \
-H "Content-Type: application/json"
```

### Xóa tác giả
```bash
curl -X DELETE "http://localhost:8080/api/authors/1" \
-H "Content-Type: application/json"
```

### Lấy dropdown tác giả (Quan trọng cho form sách!)
```bash
curl -X GET "http://localhost:8080/api/authors/dropdown" \
-H "Content-Type: application/json"
```

---

## ⚠️ CHÚ Ý ĐẶC BIỆT

### 1. Liên Kết Với Sách
- Tác giả có mối quan hệ Many-to-Many với Sách
- Khi xóa tác giả, cần kiểm tra xem có sách nào đang sử dụng không
- Toggle status an toàn hơn việc xóa hoàn toàn

### 2. Date Format Khác Biệt
- birthDate: LocalDate format (YYYY-MM-DD)
- createdAt/updatedAt: Timestamp format (milliseconds)
- Frontend cần handle 2 format khác nhau

### 3. Tích Hợp Với Book API
- API dropdown được sử dụng trong form tạo/sửa sách
- Khi sách được tạo với authorIds, hệ thống sẽ tạo liên kết tự động
- Cần đảm bảo tác giả active để hiển thị trong dropdown

### 4. Khuyến Nghị
- Luôn validate tác giả active trước khi tạo sách
- Cache dropdown authors để cải thiện performance
- Implement soft delete thay vì hard delete

---

**Lưu ý:** Tài liệu này được tạo dựa trên phân tích source code hiện tại. API Author có mối liên hệ mật thiết với Book API. Đảm bảo test tất cả các API trước khi triển khai frontend.
