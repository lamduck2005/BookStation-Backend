# TÀI LIỆU API QUẢN LÝ NHÀ XUẤT BẢN (PUBLISHER API)

## TỔNG QUAN

API Nhà xuất bản cung cấp đầy đủ các chức năng CRUD (Create, Read, Update, Delete) và các tính năng nâng cao như phân trang, tìm kiếm, lọc theo nhiều tiêu chí.

**Base URL:** `/api/publishers`

---

## 1. API LẤY DANH SÁCH NHÀ XUẤT BẢN (GET LIST)

### Endpoint
```
GET /api/publishers
```

### Tham số truy vấn (Query Parameters)
| Tham số | Loại | Bắt buộc | Mặc định | Mô tả |
|---------|------|----------|----------|-------|
| page | int | Không | 0 | Trang hiện tại (bắt đầu từ 0) |
| size | int | Không | 5 | Số lượng nhà xuất bản trên mỗi trang |
| name | String | Không | null | Tên nhà xuất bản để tìm kiếm |
| email | String | Không | null | Email để tìm kiếm |
| status | String | Không | null | Trạng thái ("1": Active, "0": Inactive) |

### Ví dụ Request
```http
GET /api/publishers?page=0&size=10&name=Kim Đồng&status=1
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
        "publisherName": "NXB Kim Đồng",
        "address": "55 Quang Trung, Hai Bà Trưng, Hà Nội",
        "contactName": "Nguyễn Văn A",
        "email": "contact@kimdong.com.vn",
        "phone": "024-3971-0999",
        "website": "https://nxbkimdong.com.vn",
        "description": "Nhà xuất bản chuyên về sách thiếu nhi",
        "status": "1",
        "createdBy": "admin",
        "updatedBy": "admin",
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

### Cấu trúc dữ liệu PublisherResponse
| Trường | Loại | Mô tả |
|--------|------|-------|
| id | Integer | ID nhà xuất bản |
| publisherName | String | Tên nhà xuất bản |
| address | String | Địa chỉ |
| contactName | String | Tên người liên hệ |
| email | String | Email liên hệ |
| phone | String | Số điện thoại |
| website | String | Website |
| description | String | Mô tả |
| status | String | Trạng thái ("1": Hoạt động, "0": Không hoạt động) |
| createdBy | String | Người tạo |
| updatedBy | String | Người cập nhật |
| createdAt | Long | Thời gian tạo (timestamp) |
| updatedAt | Long | Thời gian cập nhật (timestamp) |

---

## 2. API TẠO NHÀ XUẤT BẢN MỚI (POST)

### Endpoint
```
POST /api/publishers
```

### Request Body
```json
{
  "publisherName": "NXB Trẻ",
  "address": "161B Lý Chính Thắng, Quận 3, TP.HCM",
  "contactName": "Trần Thị B",
  "email": "contact@nxbtre.com.vn",
  "phone": "028-3930-5001",
  "website": "https://nxbtre.com.vn",
  "description": "Nhà xuất bản chuyên về sách giáo dục và văn học",
  "status": "1",
  "createdBy": "admin",
  "updatedBy": "admin"
}
```

### Cấu trúc PublisherRequest
| Trường | Loại | Bắt buộc | Mô tả | Validation |
|--------|------|----------|-------|------------|
| publisherName | String | Có | Tên nhà xuất bản | Duy nhất, không được trống, max 255 ký tự |
| address | String | Không | Địa chỉ | Max 2000 ký tự |
| contactName | String | Không | Tên người liên hệ | Max 100 ký tự |
| email | String | Không | Email liên hệ | Format email hợp lệ, max 100 ký tự |
| phone | String | Không | Số điện thoại | Max 20 ký tự, chỉ chứa số và ký tự đặc biệt |
| website | String | Không | Website | URL hợp lệ (bắt đầu với http/https), max 255 ký tự |
| description | String | Không | Mô tả | Max 2000 ký tự |
| status | String | Không | Trạng thái | "1" hoặc "0", mặc định "1" |
| createdBy | String | Có | Người tạo | Không được trống |
| updatedBy | String | Có | Người cập nhật | Không được trống |

### Response thành công (201)
```json
{
  "status": 201,
  "message": "Tạo nhà xuất bản thành công",
  "data": null
}
```

### Response lỗi

#### Tên nhà xuất bản đã tồn tại (500)
```json
{
  "status": 500,
  "message": "Lỗi khi tạo nhà xuất bản: Tên nhà xuất bản đã tồn tại",
  "data": null
}
```

#### Email đã tồn tại (500)
```json
{
  "status": 500,
  "message": "Lỗi khi tạo nhà xuất bản: Email đã được sử dụng",
  "data": null
}
```

#### Lỗi validation chi tiết (400)
```json
{
  "status": 400,
  "message": "Dữ liệu không hợp lệ",
  "data": [
    {
      "field": "publisherName",
      "message": "Tên nhà xuất bản không được để trống",
      "rejectedValue": null
    },
    {
      "field": "email",
      "message": "Email không đúng định dạng",
      "rejectedValue": "invalid-email"
    }
  ]
}
```

#### Lỗi trùng lặp tên nhà xuất bản (409)
```json
{
  "status": 409,
  "message": "Tên nhà xuất bản đã tồn tại",
  "data": null
}
```

#### Lỗi JSON không hợp lệ (400)
```json
{
  "status": 400,
  "message": "Dữ liệu JSON không hợp lệ",
  "data": null
}
```

#### Lỗi business logic (400)
```json
{
  "status": 400,
  "message": "Website phải bắt đầu bằng http:// hoặc https://",
  "data": null
}
```

---

## 3. API CẬP NHẬT NHÀ XUẤT BẢN (PUT)

### Endpoint
```
PUT /api/publishers/{id}
```

### Tham số đường dẫn
| Tham số | Loại | Bắt buộc | Mô tả |
|---------|------|----------|-------|
| id | Integer | Có | ID của nhà xuất bản cần cập nhật |

### Request Body
```json
{
  "publisherName": "NXB Trẻ - Cập nhật",
  "address": "161B Lý Chính Thắng, Quận 3, TP.HCM",
  "contactName": "Trần Thị B",
  "email": "contact@nxbtre.com.vn",
  "phone": "028-3930-5001",
  "website": "https://nxbtre.com.vn",
  "description": "Nhà xuất bản chuyên về sách giáo dục và văn học - Đã cập nhật",
  "status": "1",
  "updatedBy": "admin"
}
```

### Response thành công (200)
```json
{
  "status": 200,
  "message": "Cập nhật nhà xuất bản thành công",
  "data": null
}
```

### Response lỗi

#### Không tìm thấy nhà xuất bản (404)
```json
{
  "status": 404,
  "message": "Không tìm thấy nhà xuất bản với ID: 999",
  "data": null
}
```

---

## 4. API XÓA NHÀ XUẤT BẢN (DELETE)

### Endpoint
```
DELETE /api/publishers/{id}
```

### Tham số đường dẫn
| Tham số | Loại | Bắt buộc | Mô tả |
|---------|------|----------|-------|
| id | Integer | Có | ID của nhà xuất bản cần xóa |

### Response thành công (200)
```json
{
  "status": 200,
  "message": "Xóa nhà xuất bản thành công",
  "data": null
}
```

### Response lỗi (404)
```json
{
  "status": 404,
  "message": "Không tìm thấy nhà xuất bản với ID: 999",
  "data": null
}
```

---

## 5. API CẬP NHẬT TRẠNG THÁI NHÀ XUẤT BẢN

### Endpoint
```
PATCH /api/publishers/{id}/status
```

### Tham số đường dẫn
| Tham số | Loại | Bắt buộc | Mô tả |
|---------|------|----------|-------|
| id | Integer | Có | ID của nhà xuất bản |

### Tham số truy vấn
| Tham số | Loại | Bắt buộc | Mặc định | Mô tả |
|---------|------|----------|----------|-------|
| status | byte | Có | - | Trạng thái mới (1: Active, 0: Inactive) |
| updatedBy | String | Không | "1" | Người cập nhật |

### Ví dụ Request
```http
PATCH /api/publishers/1/status?status=0&updatedBy=admin
```

### Response thành công (200)
```json
{
  "status": 200,
  "message": "Cập nhật trạng thái thành công",
  "data": null
}
```

---

## 6. API DROPDOWN NHÀ XUẤT BẢN

### Endpoint
```
GET /api/publishers/dropdown
```

### Response thành công (200)
```json
{
  "status": 200,
  "message": "Lấy danh sách nhà xuất bản thành công",
  "data": [
    {
      "id": 1,
      "name": "NXB Kim Đồng"
    },
    {
      "id": 2,
      "name": "NXB Trẻ"
    },
    {
      "id": 3,
      "name": "NXB Giáo dục Việt Nam"
    }
  ]
}
```

---

## MÃ TRẠNG THÁI HTTP

| Mã | Ý nghĩa | Mô tả |
|----|---------|--------|
| 200 | OK | Thành công |
| 201 | Created | Tạo mới thành công |
| 404 | Not Found | Không tìm thấy tài nguyên |
| 500 | Internal Server Error | Lỗi server |

---

## VALIDATION VÀ BUSINESS RULES

### 1. Tên nhà xuất bản (publisherName)
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
- "1": Hoạt động (Active)
- "0": Không hoạt động (Inactive)
- Mặc định là "1" khi tạo mới

---

## NOTES QUAN TRỌNG CHO FRONTEND

### 1. 🔥 DROPDOWN API CHO FORM TẠO/SỬA SÁCH
- **Endpoint:** `GET /api/publishers/dropdown`
- **Format response:** `{id: number, name: string}`
- **Cache:** Nên cache response và refresh khi có thay đổi

### 2. 🔥 VALIDATION BẮT BUỘC
- **publisherName:** Unique, không được trùng lặp
- **email:** Phải đúng format email và unique
- **createdBy/updatedBy:** Bắt buộc phải có

### 3. Phân trang
- Trang bắt đầu từ 0
- Size mặc định là 5
- Luôn kiểm tra totalPages để tránh request trang không tồn tại

### 4. Timestamp
- Tất cả các trường thời gian đều là timestamp (milliseconds)
- Frontend cần convert để hiển thị cho user

### 5. Tìm kiếm và lọc
- Tất cả tham số tìm kiếm đều optional
- Có thể kết hợp nhiều tham số cùng lúc
- name search hỗ trợ tìm kiếm gần đúng

### 6. Error Handling
- Luôn kiểm tra status code trong response
- Message được trả về bằng tiếng Việt
- Data sẽ là null trong hầu hết các response

---

## CURL EXAMPLES

### Lấy danh sách nhà xuất bản với phân trang
```bash
curl -X GET "http://localhost:8080/api/publishers?page=0&size=10&name=Kim&status=1" \
-H "Content-Type: application/json"
```

### Tạo nhà xuất bản mới
```bash
curl -X POST "http://localhost:8080/api/publishers" \
-H "Content-Type: application/json" \
-d '{
  "publisherName": "NXB Văn học",
  "address": "18 Nguyễn Trường Tộ, Ba Đình, Hà Nội",
  "contactName": "Phạm Văn C",
  "email": "contact@nxbvanhoc.vn",
  "phone": "024-3825-4091",
  "website": "https://nxbvanhoc.vn",
  "description": "Chuyên xuất bản văn học trong nước và nước ngoài",
  "status": "1",
  "createdBy": "admin",
  "updatedBy": "admin"
}'
```

### Cập nhật nhà xuất bản
```bash
curl -X PUT "http://localhost:8080/api/publishers/1" \
-H "Content-Type: application/json" \
-d '{
  "publisherName": "NXB Kim Đồng - Cập nhật",
  "address": "55 Quang Trung, Hai Bà Trưng, Hà Nội",
  "contactName": "Nguyễn Văn A",
  "email": "contact@kimdong.com.vn",
  "phone": "024-3971-0999",
  "website": "https://nxbkimdong.com.vn",
  "description": "Nhà xuất bản chuyên về sách thiếu nhi - Đã cập nhật",
  "status": "1",
  "updatedBy": "admin"
}'
```

### Cập nhật trạng thái nhà xuất bản
```bash
curl -X PATCH "http://localhost:8080/api/publishers/1/status?status=0&updatedBy=admin" \
-H "Content-Type: application/json"
```

### Lấy dropdown nhà xuất bản
```bash
curl -X GET "http://localhost:8080/api/publishers/dropdown" \
-H "Content-Type: application/json"
```

---

**Lưu ý:** Tài liệu này được tạo dựa trên phân tích source code hiện tại. Đảm bảo test tất cả các API trước khi triển khai frontend.
