# TÀI LIỆU API QUẢN LÝ DANH MỤC (CATEGORY API)

## TỔNG QUAN

API Danh mục cung cấp đầy đủ các chức năng CRUD (Create, Read, Update, Delete) và hỗ trợ cấu trúc danh mục cha-con (Parent-Child Category Tree).

**Base URL:** `/api/categories`

---

## 1. API LẤY TẤT CẢ DANH MỤC (GET ALL)

### Endpoint
```
GET /api/categories
```

### Response thành công (200)
```json
[
  {
    "id": 1,
    "categoryName": "Tiểu thuyết",
    "description": "Sách tiểu thuyết",
    "parentId": null,
    "status": 1,
    "createdAt": 1625097600000,
    "updatedAt": 1625097600000
  },
  {
    "id": 2,
    "categoryName": "Lãng mạn",
    "description": "Tiểu thuyết lãng mạn",
    "parentId": 1,
    "status": 1,
    "createdAt": 1625097600000,
    "updatedAt": 1625097600000
  }
]
```

### Cấu trúc dữ liệu Category
| Trường | Loại | Mô tả |
|--------|------|-------|
| id | Integer | ID danh mục |
| categoryName | String | Tên danh mục |
| description | String | Mô tả danh mục |
| parentId | Integer | ID danh mục cha (null nếu là danh mục gốc) |
| status | Byte | Trạng thái (1: Hoạt động, 0: Không hoạt động) |
| createdAt | Long | Thời gian tạo (timestamp) |
| updatedAt | Long | Thời gian cập nhật (timestamp) |

---

## 2. API LẤY DANH MỤC CÂY (PARENT CATEGORIES)

### Endpoint
```
GET /api/categories/parentcategories
```

### Response thành công (200)
```json
[
  {
    "id": 1,
    "categoryName": "Tiểu thuyết",
    "description": "Sách tiểu thuyết",
    "parentId": null,
    "status": 1,
    "children": [
      {
        "id": 2,
        "categoryName": "Lãng mạn",
        "description": "Tiểu thuyết lãng mạn",
        "parentId": 1,
        "status": 1,
        "children": []
      },
      {
        "id": 3,
        "categoryName": "Trinh thám",
        "description": "Tiểu thuyết trinh thám",
        "parentId": 1,
        "status": 1,
        "children": []
      }
    ]
  },
  {
    "id": 4,
    "categoryName": "Thiếu nhi",
    "description": "Sách dành cho thiếu nhi",
    "parentId": null,
    "status": 1,
    "children": [
      {
        "id": 5,
        "categoryName": "Truyện tranh",
        "description": "Truyện tranh thiếu nhi",
        "parentId": 4,
        "status": 1,
        "children": []
      }
    ]
  }
]
```

### Cấu trúc dữ liệu ParentCategoryResponse
| Trường | Loại | Mô tả |
|--------|------|-------|
| id | Integer | ID danh mục |
| categoryName | String | Tên danh mục |
| description | String | Mô tả danh mục |
| parentId | Integer | ID danh mục cha |
| status | Byte | Trạng thái |
| children | List<ParentCategoryResponse> | Danh sách danh mục con |

---

## 3. API LẤY CHI TIẾT DANH MỤC THEO ID

### Endpoint
```
GET /api/categories/{id}
```

### Tham số đường dẫn
| Tham số | Loại | Bắt buộc | Mô tả |
|---------|------|----------|-------|
| id | Integer | Có | ID của danh mục |

### Ví dụ Request
```http
GET /api/categories/1
```

### Response thành công (200)
```json
{
  "id": 1,
  "categoryName": "Tiểu thuyết",
  "description": "Sách tiểu thuyết",
  "parentId": null,
  "status": 1,
  "createdAt": 1625097600000,
  "updatedAt": 1625097600000
}
```

### Response lỗi (404)
```json
{
  "error": "Không tìm thấy danh mục với ID: 999",
  "status": 404
}
```

---

## 4. API TẠO DANH MỤC MỚI (POST)

### Endpoint
```
POST /api/categories
```

### Request Body
```json
{
  "categoryName": "Khoa học viễn tưởng",
  "description": "Sách khoa học viễn tưởng",
  "parentId": 1,
  "status": 1
}
```

### Cấu trúc CategoryRequest
| Trường | Loại | Bắt buộc | Mô tả | Validation |
|--------|------|----------|-------|------------|
| categoryName | String | Có | Tên danh mục | Duy nhất, không được trống, max 255 ký tự |
| description | String | Không | Mô tả danh mục | Max 1000 ký tự |
| parentId | Integer | Không | ID danh mục cha | Phải tồn tại trong hệ thống nếu có |
| status | Byte | Không | Trạng thái | 1 hoặc 0, mặc định 1 |

### Response thành công (200)
```json
{
  "id": 6,
  "categoryName": "Khoa học viễn tưởng",
  "description": "Sách khoa học viễn tưởng",
  "parentId": 1,
  "status": 1,
  "createdAt": 1704067200000,
  "updatedAt": 1704067200000
}
```

### Response lỗi

#### Tên danh mục đã tồn tại (400)
```json
{
  "error": "Tên danh mục đã tồn tại",
  "status": 400
}
```

#### Danh mục cha không tồn tại (404)
```json
{
  "error": "Không tìm thấy danh mục cha với ID: 999",
  "status": 404
}
```

---

## 5. API CẬP NHẬT DANH MỤC (PUT)

### Endpoint
```
PUT /api/categories/{id}
```

### Tham số đường dẫn
| Tham số | Loại | Bắt buộc | Mô tả |
|---------|------|----------|-------|
| id | Integer | Có | ID của danh mục cần cập nhật |

### Request Body
```json
{
  "categoryName": "Khoa học viễn tưởng - Cập nhật",
  "description": "Sách khoa học viễn tưởng và fantasy",
  "parentId": 1,
  "status": 1
}
```

### Response thành công (200)
```json
{
  "id": 6,
  "categoryName": "Khoa học viễn tưởng - Cập nhật",
  "description": "Sách khoa học viễn tưởng và fantasy",
  "parentId": 1,
  "status": 1,
  "createdAt": 1704067200000,
  "updatedAt": 1704153600000
}
```

### Response lỗi

#### Không tìm thấy danh mục (404)
```json
{
  "error": "Không tìm thấy danh mục với ID: 999",
  "status": 404
}
```

#### Không thể làm danh mục cha của chính mình (400)
```json
{
  "error": "Danh mục không thể là cha của chính nó",
  "status": 400
}
```

---

## 6. API XÓA DANH MỤC (DELETE)

### Endpoint
```
DELETE /api/categories/{id}
```

### Tham số đường dẫn
| Tham số | Loại | Bắt buộc | Mô tả |
|---------|------|----------|-------|
| id | Integer | Có | ID của danh mục cần xóa |

### Response thành công (200)
```json
{
  "id": null,
  "categoryName": null,
  "description": null,
  "parentId": null,
  "status": 0,
  "createdAt": null,
  "updatedAt": null
}
```

### Response lỗi

#### Không thể xóa danh mục có danh mục con (400)
```json
{
  "error": "Không thể xóa danh mục có danh mục con",
  "status": 400
}
```

#### Không thể xóa danh mục đang được sử dụng (400)
```json
{
  "error": "Không thể xóa danh mục đang được sử dụng bởi sách",
  "status": 400
}
```

---

## 7. API DROPDOWN DANH MỤC

### Endpoint
```
GET /api/categories/dropdown
```

### Response thành công (200)
```json
{
  "status": 200,
  "message": "Lấy danh sách danh mục thành công",
  "data": [
    {
      "id": 1,
      "name": "Tiểu thuyết"
    },
    {
      "id": 2,
      "name": "Lãng mạn"
    },
    {
      "id": 3,
      "name": "Trinh thám"
    },
    {
      "id": 4,
      "name": "Thiếu nhi"
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

### 1. Tên danh mục (categoryName)
- Bắt buộc phải có
- Độ dài tối đa 255 ký tự
- Phải duy nhất trong hệ thống
- Không phân biệt hoa thường khi kiểm tra trùng lặp

### 2. Mô tả (description)
- Tùy chọn
- Độ dài tối đa 1000 ký tự
- Có thể chứa HTML tags đơn giản

### 3. Danh mục cha (parentId)
- Tùy chọn (null = danh mục gốc)
- Phải tồn tại trong hệ thống nếu có
- Không được là chính danh mục đó (tránh vòng lặp)
- Không được tạo vòng lặp trong cây danh mục

### 4. Trạng thái (status)
- 1: Hoạt động (Active)
- 0: Không hoạt động (Inactive)
- Mặc định là 1 khi tạo mới

### 5. Ràng buộc xóa
- Không thể xóa danh mục có danh mục con
- Không thể xóa danh mục đang được sử dụng bởi sách
- Phải xóa tất cả danh mục con trước khi xóa danh mục cha

### 6. Cấu trúc cây
- Hỗ trợ nhiều cấp độ (không giới hạn độ sâu)
- Mỗi danh mục chỉ có thể có một danh mục cha
- Một danh mục có thể có nhiều danh mục con

---

## NOTES QUAN TRỌNG CHO FRONTEND

### 1. 🔥 DROPDOWN API CHO FORM TẠO/SỬA SÁCH
- **Endpoint:** `GET /api/categories/dropdown`
- **Format response:** `{id: number, name: string}`
- **Cache:** Nên cache response và refresh khi có thay đổi
- **Quan trọng:** API này được dùng trong form tạo/sửa sách

### 2. 🔥 CẤU TRÚC CÂY DANH MỤC
- **Endpoint:** `GET /api/categories/parentcategories`
- **Dùng cho:** Hiển thị cây danh mục trong admin panel
- **Format:** Nested structure với children array
- **Performance:** Cache để tránh call API nhiều lần

### 3. 🔥 VALIDATION BẮT BUỘC
- **categoryName:** Unique, không được trùng lặp
- **parentId:** Phải tồn tại và không tạo vòng lặp
- **Kiểm tra vòng lặp:** Quan trọng khi update parentId

### 4. Response Format Khác Biệt
- **GET ALL:** Trả về Array trực tiếp (không có ApiResponse wrapper)
- **GET Parent Categories:** Trả về Array với structure khác
- **Dropdown:** Có ApiResponse wrapper
- **CRUD Operations:** Trả về entity trực tiếp

### 5. Tree Structure Handling
- Frontend cần implement tree component để hiển thị
- Hỗ trợ expand/collapse cho danh mục có con
- Drag & drop để thay đổi parent-child relationship
- Breadcrumb navigation cho danh mục con

### 6. Error Handling
- Kiểm tra circular reference khi cập nhật parentId
- Validate dependencies trước khi xóa
- Handle các lỗi constraint violation

### 7. Performance Optimization
- Cache tree structure
- Lazy loading cho danh mục có nhiều con
- Pagination nếu danh mục quá nhiều

---

## CURL EXAMPLES

### Lấy tất cả danh mục
```bash
curl -X GET "http://localhost:8080/api/categories" \
-H "Content-Type: application/json"
```

### Lấy cây danh mục
```bash
curl -X GET "http://localhost:8080/api/categories/parentcategories" \
-H "Content-Type: application/json"
```

### Tạo danh mục mới
```bash
curl -X POST "http://localhost:8080/api/categories" \
-H "Content-Type: application/json" \
-d '{
  "categoryName": "Kinh dị",
  "description": "Sách kinh dị và thriller",
  "parentId": 1,
  "status": 1
}'
```

### Cập nhật danh mục
```bash
curl -X PUT "http://localhost:8080/api/categories/1" \
-H "Content-Type: application/json" \
-d '{
  "categoryName": "Tiểu thuyết văn học",
  "description": "Tiểu thuyết văn học trong nước và nước ngoài",
  "parentId": null,
  "status": 1
}'
```

### Xóa danh mục
```bash
curl -X DELETE "http://localhost:8080/api/categories/1" \
-H "Content-Type: application/json"
```

### Lấy dropdown danh mục (Quan trọng cho form sách!)
```bash
curl -X GET "http://localhost:8080/api/categories/dropdown" \
-H "Content-Type: application/json"
```

---

## ⚠️ CHÚ Ý ĐẶC BIỆT

### 1. Response Format Không Nhất Quán
- **GET /categories:** Array trực tiếp (không có wrapper)
- **GET /parentcategories:** Array với nested structure
- **GET /dropdown:** Có ApiResponse wrapper
- **CRUD:** Entity object trực tiếp

### 2. Tree Structure Complexity
- Cần validate circular reference
- Xử lý cascade delete
- Performance với tree có nhiều cấp

### 3. Tích Hợp Với Book API
- Dropdown được sử dụng trong form tạo/sửa sách
- Category ID trong sách phải active và tồn tại
- Không thể xóa category đang được sử dụng

### 4. Frontend Implementation
- Implement tree component cho admin
- Breadcrumb cho navigation
- Search trong tree structure
- Validation khi thay đổi parent-child

### 5. Khuyến Nghị
- Cache tree structure để cải thiện performance
- Implement soft delete thay vì hard delete
- Validation phía frontend cho circular reference
- Progressive loading cho tree lớn

---

**Lưu ý:** Tài liệu này được tạo dựa trên phân tích source code hiện tại. API Category hỗ trợ cấu trúc cây phức tạp và có response format khác nhau. Đảm bảo test tất cả các API trước khi triển khai frontend.
