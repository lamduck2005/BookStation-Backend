# TÀI LIỆU API QUẢN LÝ SÁCH (BOOK API)
| categoryId | Integer | Không | ID danh mục sách |
| supplierId | Integer | Không | ID nhà cung cấp |
| publisherId | Integer | Không | ID nhà xuất bản |
| minPrice | BigDecimal | Không | null | Giá tối thiểu |
| maxPrice | BigDecimal | Không | null | Giá tối đa |
| status | Byte | Không | null | Trạng thái (1: Active, 0: Inactive) |
| bookCode | String | Không | null | Mã sách |️ BREAKING CHANGE - THÔNG BÁO QUAN TRỌNG

**🚨 CẬP NHẬT NGÀY 01/07/2025:**

API Sách đã được cập nhật để **BẮT BUỘC** phải có thông tin tác giả. Đây là thay đổi **B### 7.6. API Lấy sách theo danh mục
```
GET /api/books/category/{categoryId}
```

### 7.7. API Lấy sách theo nhà cung cấp
```
GET /api/books/supplier/{supplierId}
```

### 7.8. API Lấy sách đang hoạt động
```
GET /api/books/active
```

### 7.9. API Test chuyển đổi ngày xuất bản:

### Thay đổi API POST/PUT Book:
- **Thêm field bắt buộc:** `authorIds` (List<Integer>) trong request
- **Validation:** Danh sách tác giả không được rỗng và tất cả ID phải tồn tại
- **Response:** Tất cả response của Book giờ có thêm field `authors`

### Impact cho Frontend:
1. **Phải cập nhật form tạo/sửa sách** để có trường chọn tác giả
2. **Phải cập nhật validation** frontend để kiểm tra authorIds
3. **Phải cập nhật hiển thị** danh sách sách để show thông tin tác giả
4. **API cũ sẽ trả lỗi 400** nếu không có authorIds

## TỔNG QUAN

Hệ thống API quản lý sách cung cấp đầy đủ các chức năng CRUD (Create, Read, Update, Delete) và các tính năng nâng cao như phân trang, tìm kiếm, lọc theo nhiều tiêu chí.

**Base URL:** `/api/books`

---

## 1. API LẤY DANH SÁCH SÁCH (GET LIST)

### Endpoint
```
GET /api/books
```

### Tham số truy vấn (Query Parameters)
| Tham số | Loại | Bắt buộc | Mặc định | Mô tả |
|---------|------|----------|----------|-------|
| page | int | Không | 0 | Trang hiện tại (bắt đầu từ 0) |
| size | int | Không | 5 | Số lượng sách trên mỗi trang |
| bookName | String | Không | null | Tên sách để tìm kiếm |
| categoryId | Integer | Không | null | ID danh mục sách |
| supplierId | Integer | Không | null | ID nhà cung cấp |
| minPrice | BigDecimal | Không | null | Giá tối thiểu |
| maxPrice | BigDecimal | Không | null | Giá tối đa |
| status | Byte | Không | null | Trạng thái (1: Active, 0: Inactive) |
| bookCode | String | Không | null | Mã sách |

### Ví dụ Request
```http
GET /api/books?page=0&size=10&bookName=Doraemon&categoryId=1&publisherId=1&minPrice=50000&maxPrice=200000&status=1
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
        "bookName": "Doraemon tập 1",
        "description": "Truyện tranh Doraemon tập đầu tiên",
        "price": 75000,
        "stockQuantity": 100,
        "publicationDate": 1577836800000,
        "categoryId": 1,
        "categoryName": "Truyện tranh",
        "supplierId": 1,
        "supplierName": "NXB Kim Đồng",
        "publisherId": 1,
        "publisherName": "NXB Trẻ",
        "bookCode": "BOOK001",
        "status": 1,
        "createdAt": 1625097600000,
        "updatedAt": 1625097600000,
        "authors": [
          {
            "id": 1,
            "authorName": "Fujiko Fujio",
            "biography": "Nhóm tác giả truyện tranh Nhật Bản",
            "birthDate": "1933-12-01",
            "status": 1
          }
        ]
      }
    ],
    "pageNumber": 0,
    "pageSize": 10,
    "totalElements": 50,
    "totalPages": 5
  }
}
```

### Cấu trúc dữ liệu BookResponse
| Trường | Loại | Mô tả |
|--------|------|-------|
| id | Integer | ID sách |
| bookName | String | Tên sách |
| description | String | Mô tả sách |
| price | BigDecimal | Giá sách (VNĐ) |
| stockQuantity | Integer | Số lượng tồn kho |
| publicationDate | Long | Ngày xuất bản (timestamp) |
| categoryId | Integer | ID danh mục |
| categoryName | String | Tên danh mục |
| supplierId | Integer | ID nhà cung cấp |
| supplierName | String | Tên nhà cung cấp |
| publisherId | Integer | ID nhà xuất bản |
| publisherName | String | Tên nhà xuất bản |
| bookCode | String | Mã sách duy nhất |
| status | Byte | Trạng thái (1: Hoạt động, 0: Không hoạt động) |
| createdAt | Long | Thời gian tạo (timestamp) |
| updatedAt | Long | Thời gian cập nhật (timestamp) |
| **authors** | **List<AuthorResponse>** | **Danh sách tác giả của sách** |

### Cấu trúc AuthorResponse
| Trường | Loại | Mô tả |
|--------|------|-------|
| id | Integer | ID tác giả |
| authorName | String | Tên tác giả |
| biography | String | Tiểu sử tác giả |
| birthDate | LocalDate | Ngày sinh |
| status | Byte | Trạng thái (1: Hoạt động, 0: Không hoạt động) |

---

## 2. API LẤY CHI TIẾT SÁCH THEO ID

### Endpoint
```
GET /api/books/{id}
```

### Tham số đường dẫn
| Tham số | Loại | Bắt buộc | Mô tả |
|---------|------|----------|-------|
| id | Integer | Có | ID của sách |

### Ví dụ Request
```http
GET /api/books/1
```

### Response thành công (200)
```json
{
  "status": 200,
  "message": "Thành công",
  "data": {
    "id": 1,
    "bookName": "Doraemon tập 1",
    "description": "Truyện tranh Doraemon tập đầu tiên",
    "price": 75000,
    "stockQuantity": 100,
    "publicationDate": 1577836800000,
    "categoryId": 1,
    "categoryName": "Truyện tranh",
    "supplierId": 1,
    "supplierName": "NXB Kim Đồng",
    "publisherId": 1,
    "publisherName": "NXB Trẻ",
    "bookCode": "BOOK001",
    "status": 1,
    "createdAt": 1625097600000,
    "updatedAt": 1625097600000,
    "authors": [
      {
        "id": 1,
        "authorName": "Fujiko Fujio",
        "biography": "Nhóm tác giả truyện tranh Nhật Bản",
        "birthDate": "1933-12-01",
        "status": 1
      }
    ]
  }
}
```

### Response lỗi (404)
```json
{
  "status": 404,
  "message": "Không tìm thấy sách",
  "data": null
}
```

---

## 3. API TẠO SÁCH MỚI (POST)

### Endpoint
```
POST /api/books
```

### Request Body
```json
{
  "bookName": "Tên sách",
  "description": "Mô tả sách",
  "price": 100000,
  "stockQuantity": 50,
  "publicationDate": 1577836800000,
  "categoryId": 1,
  "supplierId": 1,
  "publisherId": 1,
  "bookCode": "BOOK002",
  "status": 1,
  "authorIds": [1, 2]
}
```

### Cấu trúc BookRequest
| Trường | Loại | Bắt buộc | Mô tả | Validation |
|--------|------|----------|-------|------------|
| bookName | String | Có | Tên sách | Duy nhất, không được trống |
| description | String | Không | Mô tả sách | |
| price | BigDecimal | Có | Giá sách | > 0 |
| stockQuantity | Integer | Có | Số lượng tồn kho | >= 0 |
| publicationDate | Long | Không | Ngày xuất bản (timestamp) | |
| categoryId | Integer | Không | ID danh mục | Phải tồn tại trong hệ thống |
| supplierId | Integer | Không | ID nhà cung cấp | Phải tồn tại trong hệ thống |
| publisherId | Integer | Không | ID nhà xuất bản | Phải tồn tại trong hệ thống |
| bookCode | String | Không | Mã sách | Duy nhất, tự động tạo nếu không có |
| status | Byte | Không | Trạng thái | 1 hoặc 0, mặc định là 1 |
| **authorIds** | **List<Integer>** | **Có** | **Danh sách ID tác giả** | **Không được rỗng, tất cả ID phải tồn tại** |

### Response thành công (201)
```json
{
  "status": 201,
  "message": "Tạo sách thành công",
  "data": {
    "id": 2,
    "bookName": "Tên sách",
    "description": "Mô tả sách",
    "price": 100000,
    "stockQuantity": 50,
    "publicationDate": 1577836800000,
    "categoryId": 1,
    "categoryName": "Truyện tranh",
    "supplierId": 1,
    "supplierName": "NXB Kim Đồng",
    "publisherId": 1,
    "publisherName": "NXB Trẻ",
    "bookCode": "BOOK002",
    "status": 1,
    "createdAt": 1704067200000,
    "updatedAt": 1704067200000,
    "authors": [
      {
        "id": 1,
        "authorName": "Fujiko Fujio",
        "biography": "Nhóm tác giả truyện tranh Nhật Bản",
        "birthDate": "1933-12-01",
        "status": 1
      },
      {
        "id": 2,
        "authorName": "Hiroshi Fujimoto",
        "biography": "Họa sĩ truyện tranh Nhật Bản",
        "birthDate": "1933-12-01",
        "status": 1
      }
    ]
  }
}
```

### Response lỗi

#### Tên sách đã tồn tại (400)
```json
{
  "status": 400,
  "message": "Tên sách đã tồn tại",
  "data": null
}
```

#### Mã sách đã tồn tại (400)
```json
{
  "status": 400,
  "message": "Mã sách đã tồn tại",
  "data": null
}
```

#### Không tìm thấy danh mục (404)
```json
{
  "status": 404,
  "message": "Không tìm thấy danh mục",
  "data": null
}
```

#### Không tìm thấy nhà cung cấp (404)
```json
{
  "status": 404,
  "message": "Không tìm thấy nhà cung cấp",
  "data": null
}
```

#### Danh sách tác giả không được rỗng (400)
```json
{
  "status": 400,
  "message": "Danh sách tác giả không được rỗng",
  "data": null
}
```

#### Không tìm thấy tác giả (404)
```json
{
  "status": 404,
  "message": "Không tìm thấy tác giả với ID: 999",
  "data": null
}
```

---

## 4. API CẬP NHẬT SÁCH (PUT)

### Endpoint
```
PUT /api/books/{id}
```

### Tham số đường dẫn
| Tham số | Loại | Bắt buộc | Mô tả |
|---------|------|----------|-------|
| id | Integer | Có | ID của sách cần cập nhật |

### Request Body
```json
{
  "bookName": "Tên sách đã cập nhật",
  "description": "Mô tả sách đã cập nhật",
  "price": 120000,
  "stockQuantity": 75,
  "publicationDate": 1577836800000,
  "categoryId": 2,
  "supplierId": 2,
  "publisherId": 2,
  "bookCode": "BOOK002_UPDATED",
  "status": 1,
  "authorIds": [3, 4]
}
```

### Response thành công (200)
```json
{
  "status": 200,
  "message": "Cập nhật sách thành công",
  "data": {
    "id": 2,
    "bookName": "Tên sách đã cập nhật",
    "description": "Mô tả sách đã cập nhật",
    "price": 120000,
    "stockQuantity": 75,
    "publicationDate": 1577836800000,
    "categoryId": 2,
    "categoryName": "Tiểu thuyết",
    "supplierId": 2,
    "supplierName": "NXB Trẻ",
    "publisherId": 2,
    "publisherName": "NXB Giáo dục",
    "bookCode": "BOOK002_UPDATED",
    "status": 1,
    "createdAt": 1704067200000,
    "updatedAt": 1704153600000,
    "authors": [
      {
        "id": 3,
        "authorName": "Tác giả 3",
        "biography": "Tiểu sử tác giả 3",
        "birthDate": "1980-05-15",
        "status": 1
      },
      {
        "id": 4,
        "authorName": "Tác giả 4", 
        "biography": "Tiểu sử tác giả 4",
        "birthDate": "1975-08-20",
        "status": 1
      }
    ]
  }
}
```

### Response lỗi

#### Không tìm thấy sách (404)
```json
{
  "status": 404,
  "message": "Không tìm thấy sách",
  "data": null
}
```

#### Tên sách đã tồn tại (400)
```json
{
  "status": 400,
  "message": "Tên sách đã tồn tại",
  "data": null
}
```

---

## 5. API XÓA SÁCH (DELETE)

### Endpoint
```
DELETE /api/books/{id}
```

### Tham số đường dẫn
| Tham số | Loại | Bắt buộc | Mô tả |
|---------|------|----------|-------|
| id | Integer | Có | ID của sách cần xóa |

### Response thành công (204)
```
Không có nội dung (No Content)
```

---

## 6. API CHUYỂN TRẠNG THÁI SÁCH

### Endpoint
```
PATCH /api/books/{id}/toggle-status
```

### Tham số đường dẫn
| Tham số | Loại | Bắt buộc | Mô tả |
|---------|------|----------|-------|
| id | Integer | Có | ID của sách |

### Response thành công (200)
```json
{
  "status": 200,
  "message": "Cập nhật trạng thái thành công",
  "data": {
    "id": 1,
    "bookName": "Doraemon tập 1",
    "status": 0,
    "updatedAt": 1704153600000
  }
}
```

### Response lỗi (404)
```json
{
  "status": 404,
  "message": "Không tìm thấy sách",
  "data": null
}
```

---

## 7. CÁC API PHỤ TRỢ

### 7.1. API Lấy dropdown sách
```
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
      "name": "Doraemon tập 1"
    }
  ]
}
```

### 7.2. 🔥 API Lấy dropdown tác giả (CHO FORM TẠO/SỬA SÁCH)
```
GET /api/authors/dropdown
```

**Response:**
```json
{
  "status": 200,
  "message": "Lấy danh sách tác giả thành công",
  "data": [
    {
      "id": 1,
      "name": "Fujiko Fujio"
    },
    {
      "id": 2,
      "name": "Hiroshi Fujimoto"
    }
  ]
}
```

### 7.3. 🔥 API Lấy dropdown danh mục (CHO FORM TẠO/SỬA SÁCH)
```
GET /api/categories/dropdown
```

**Response:**
```json
{
  "status": 200,
  "message": "Lấy danh sách danh mục thành công",
  "data": [
    {
      "id": 1,
      "name": "Truyện tranh"
    },
    {
      "id": 2,
      "name": "Tiểu thuyết"
    }
  ]
}
```

### 7.4. 🔥 API Lấy dropdown nhà xuất bản (CHO FORM TẠO/SỬA SÁCH)
```
GET /api/publishers/dropdown
```

**Response:**
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
    }
  ]
}
```

### 7.5. 🔥 API Lấy dropdown nhà cung cấp (CHO FORM TẠO/SỬA SÁCH)
### 7.5. 🔥 API Lấy dropdown nhà cung cấp (CHO FORM TẠO/SỬA SÁCH)
```
GET /api/suppliers/dropdown
```

**Response:**
```json
{
  "status": 200,
  "message": "Lấy danh sách nhà cung cấp thành công",
  "data": [
    {
      "id": 1,
      "name": "NXB Kim Đồng"
    },
    {
      "id": 2,
      "name": "NXB Trẻ"
    }
  ]
}
```

### 7.6. API Lấy sách theo danh mục
```
GET /api/books/category/{categoryId}
```

### 7.6. API Lấy sách theo nhà cung cấp
```
GET /api/books/supplier/{supplierId}
```

### 7.7. API Lấy sách đang hoạt động
```
GET /api/books/active
```

### 7.8. API Test chuyển đổi ngày xuất bản
```
GET /api/books/test-publication-date
```

---

## MÃ TRẠNG THÁI HTTP

| Mã | Ý nghĩa | Mô tả |
|----|---------|--------|
| 200 | OK | Thành công |
| 201 | Created | Tạo mới thành công |
| 204 | No Content | Xóa thành công |
| 400 | Bad Request | Dữ liệu không hợp lệ |
| 404 | Not Found | Không tìm thấy tài nguyên |
| 500 | Internal Server Error | Lỗi server |

---

## VALIDATION VÀ BUSINESS RULES

### 1. Tên sách (bookName)
- Bắt buộc phải có
- Độ dài tối đa 255 ký tự
- Phải duy nhất trong hệ thống
- Không phân biệt hoa thường khi kiểm tra trùng lặp

### 2. Mã sách (bookCode)
- Độ dài tối đa 255 ký tự
- Phải duy nhất trong hệ thống
- Nếu không cung cấp, hệ thống sẽ tự động tạo theo format: "BOOK{timestamp}"

### 3. Giá sách (price)
- Bắt buộc phải có
- Phải là số dương
- Định dạng BigDecimal với độ chính xác 10 chữ số, 2 chữ số thập phân

### 4. Số lượng tồn kho (stockQuantity)
- Bắt buộc phải có
- Phải là số nguyên không âm

### 5. Ngày xuất bản (publicationDate)
- Định dạng timestamp (milliseconds)
- Có thể null

### 6. Danh mục và nhà cung cấp
- CategoryId, SupplierId và PublisherId phải tồn tại trong hệ thống
- Có thể null

### 7. Trạng thái (status)
- 1: Hoạt động (Active)
- 0: Không hoạt động (Inactive)
- Mặc định là 1 khi tạo mới

---

## NOTES QUAN TRỌNG CHO FRONTEND

### 1. 🔥 DROPDOWN APIs CHO FORM TẠO/SỬA SÁCH
- **Bắt buộc call trước khi load form:**
  - `GET /api/authors/dropdown` - Lấy danh sách tác giả
  - `GET /api/categories/dropdown` - Lấy danh sách danh mục  
  - `GET /api/suppliers/dropdown` - Lấy danh sách nhà cung cấp
  - `GET /api/publishers/dropdown` - Lấy danh sách nhà xuất bản
- **Format response:** `{id: number, name: string}`
- **Cache:** Nên cache response và refresh khi cần

### 2. 🔥 VALIDATION BẮT BUỘC
- **authorIds:** Không được rỗng, phải chọn ít nhất 1 tác giả
- **categoryId & supplierId:** Phải có trong dropdown list  
- **bookName:** Unique, không được trùng lặp
- **price:** Phải > 0
- **stockQuantity:** Phải >= 0

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
- bookName search hỗ trợ tìm kiếm gần đúng

### 6. Error Handling
- Luôn kiểm tra status code trong response
- Message được trả về bằng tiếng Việt
- Data sẽ là null khi có lỗi

### 7. Performance
- Sử dụng API dropdown cho các combobox
- Cache danh sách category, supplier, author
- Implement pagination để tránh load quá nhiều dữ liệu

---

## CURL EXAMPLES

### Lấy danh sách sách với phân trang
```bash
curl -X GET "http://localhost:8080/api/books?page=0&size=10&bookName=Doraemon&status=1" \
-H "Content-Type: application/json"
```

### Tạo sách mới
```bash
curl -X POST "http://localhost:8080/api/books" \
-H "Content-Type: application/json" \
-d '{
  "bookName": "Sách mới",
  "description": "Mô tả sách mới",
  "price": 150000,
  "stockQuantity": 30,
  "categoryId": 1,
  "supplierId": 1,
  "publisherId": 1,
  "authorIds": [1, 2]
}'
```

### Cập nhật sách
```bash
curl -X PUT "http://localhost:8080/api/books/1" \
-H "Content-Type: application/json" \
-d '{
  "bookName": "Sách đã cập nhật",
  "price": 180000,
  "stockQuantity": 25,
  "publisherId": 2,
  "authorIds": [3]
}'
```

### Chuyển trạng thái sách
```bash
curl -X PATCH "http://localhost:8080/api/books/1/toggle-status" \
-H "Content-Type: application/json"
```

### 🔥 Lấy dropdown cho form (Quan trọng!)
```bash
# Lấy dropdown tác giả
curl -X GET "http://localhost:8080/api/authors/dropdown" \
-H "Content-Type: application/json"

# Lấy dropdown danh mục
curl -X GET "http://localhost:8080/api/categories/dropdown" \
-H "Content-Type: application/json"

# Lấy dropdown nhà xuất bản
curl -X GET "http://localhost:8080/api/publishers/dropdown" \
-H "Content-Type: application/json"

# Lấy dropdown nhà cung cấp
curl -X GET "http://localhost:8080/api/suppliers/dropdown" \
-H "Content-Type: application/json"
```

---

**Lưu ý:** Tài liệu này được tạo dựa trên phân tích source code hiện tại. Đảm bảo test tất cả các API trước khi triển khai frontend.
