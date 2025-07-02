# TỔNG QUAN API HỆ THỐNG QUẢN LÝ SÁCH - BOOKSTATION

## 📚 MỤC LỤC TÀI LIỆU API

### 🔥 APIs CHÍNH CHO FRONTEND

1. **[BOOK API - API Quản lý Sách](./BOOK_API_DOCUMENTATION.md)**
   - API chính của hệ thống
   - **BẮT BUỘC** có authorIds khi tạo/sửa sách
   - Hỗ trợ phân trang, tìm kiếm, lọc
   - Có publisherId (nhà xuất bản)

2. **[AUTHOR API - API Quản lý Tác giả](./AUTHOR_API_GUIDE.md)**
   - Quản lý tác giả sách
   - **QUAN TRỌNG:** Dropdown cho form tạo/sửa sách
   - Relationship Many-to-Many với Book

3. **[CATEGORY API - API Quản lý Danh mục](./CATEGORY_API_GUIDE.md)**
   - Quản lý danh mục sách (hỗ trợ cây danh mục)
   - **QUAN TRỌNG:** Dropdown cho form tạo/sửa sách
   - Cấu trúc parent-child

4. **[PUBLISHER API - API Quản lý Nhà xuất bản](./PUBLISHER_API_GUIDE.md)**
   - Quản lý nhà xuất bản
   - **QUAN TRỌNG:** Dropdown cho form tạo/sửa sách
   - Response format chuẩn ApiResponse

5. **[SUPPLIER API - API Quản lý Nhà cung cấp](./SUPPLIER_API_GUIDE.md)**
   - Quản lý nhà cung cấp/phân phối sách
   - **QUAN TRỌNG:** Dropdown cho form tạo/sửa sách
   - **CHÚ Ý:** Response format khác với các API khác

---

## 🚨 THÔNG BÁO QUAN TRỌNG - BREAKING CHANGES

### 📅 Cập nhật ngày 01/07/2025:

**BOOK API** đã được cập nhật với **BREAKING CHANGE**:
- **BẮT BUỘC** phải có `authorIds` (List<Integer>) khi tạo/sửa sách
- **BẮT BUỘC** phải chọn ít nhất 1 tác giả
- Tất cả response Book giờ có `authors` array
- **API cũ sẽ trả lỗi 400** nếu không có authorIds

---

## 🔥 WORKFLOW TẠO/SỬA SÁCH - BẮT BUỘC CHO FRONTEND

### Bước 1: Load các dropdown cần thiết
```javascript
// BẮT BUỘC call trước khi hiển thị form
const [authors, categories, suppliers, publishers] = await Promise.all([
  fetch('/api/authors/dropdown'),
  fetch('/api/categories/dropdown'),
  fetch('/api/suppliers/dropdown'),
  fetch('/api/publishers/dropdown')
]);
```

### Bước 2: Validation form
```javascript
const validateBookForm = (formData) => {
  // BẮT BUỘC
  if (!formData.authorIds || formData.authorIds.length === 0) {
    throw new Error('Phải chọn ít nhất 1 tác giả');
  }
  
  if (!formData.bookName || formData.bookName.trim() === '') {
    throw new Error('Tên sách không được trống');
  }
  
  if (!formData.price || formData.price <= 0) {
    throw new Error('Giá sách phải lớn hơn 0');
  }
  
  // TÙY CHỌN nhưng nên có
  if (formData.categoryId && !categories.find(c => c.id === formData.categoryId)) {
    throw new Error('Danh mục không hợp lệ');
  }
};
```

### Bước 3: Submit data
```javascript
const bookData = {
  bookName: "Tên sách",
  description: "Mô tả sách",
  price: 100000,
  stockQuantity: 50,
  categoryId: 1,          // TÙY CHỌN
  supplierId: 1,          // TÙY CHỌN
  publisherId: 1,         // TÙY CHỌN
  authorIds: [1, 2],      // BẮT BUỘC - ít nhất 1 tác giả
  status: 1
};

// Tạo sách mới
const response = await fetch('/api/books', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify(bookData)
});
```

---

## 📊 RESPONSE FORMAT KHÁC NHAU

### 🔥 Cần chú ý các format khác nhau:

#### 1. **Chuẩn ApiResponse** (Book, Author, Publisher):
```json
{
  "status": 200,
  "message": "Thành công",
  "data": { /* actual data */ }
}
```

#### 2. **Direct Response** (Category, Supplier GET):
```json
// Trả về trực tiếp array hoặc object
[
  { "id": 1, "name": "Category 1" }
]
```

#### 3. **Pagination Response**:
```json
{
  "content": [ /* items */ ],
  "pageNumber": 0,
  "pageSize": 10,
  "totalElements": 100,
  "totalPages": 10
}
```

---

## 🛠️ DROPDOWN APIs CHO FORMS

### Tất cả dropdown endpoints:
```javascript
const DROPDOWN_APIS = {
  AUTHORS: '/api/authors/dropdown',       // ApiResponse format
  CATEGORIES: '/api/categories/dropdown', // ApiResponse format  
  SUPPLIERS: '/api/suppliers/dropdown',   // ApiResponse format
  PUBLISHERS: '/api/publishers/dropdown'  // ApiResponse format
};
```

### Cách sử dụng:
```javascript
const loadDropdowns = async () => {
  const responses = await Promise.all([
    fetch(DROPDOWN_APIS.AUTHORS),
    fetch(DROPDOWN_APIS.CATEGORIES),
    fetch(DROPDOWN_APIS.SUPPLIERS),
    fetch(DROPDOWN_APIS.PUBLISHERS)
  ]);
  
  const [authorsRes, categoriesRes, suppliersRes, publishersRes] = 
    await Promise.all(responses.map(r => r.json()));
  
  // Tất cả đều có format: { status: 200, message: "...", data: [{id, name}] }
  return {
    authors: authorsRes.data,
    categories: categoriesRes.data,
    suppliers: suppliersRes.data,
    publishers: publishersRes.data
  };
};
```

---

## ⚠️ CHÚ Ý QUAN TRỌNG CHO FRONTEND

### 1. **Response Format Inconsistency**
- **Book/Author/Publisher:** Có ApiResponse wrapper
- **Category GET ALL:** Array trực tiếp (không có wrapper)
- **Supplier GET LIST:** PaginationResponse trực tiếp
- **Dropdowns:** Tất cả đều có ApiResponse wrapper

### 2. **Error Handling**
```javascript
const handleApiError = (response, apiType) => {
  if (apiType === 'SUPPLIER_LIST') {
    // Supplier API không có chuẩn error format
    if (!response.ok) {
      throw new Error('Supplier API error');
    }
  } else {
    // Standard format
    if (response.status !== 200) {
      throw new Error(response.message || 'API Error');
    }
  }
};
```

### 3. **Pagination Handling**
```javascript
const PAGINATION_DEFAULTS = {
  BOOK: { page: 0, size: 5 },
  AUTHOR: { page: 0, size: 5 },
  PUBLISHER: { page: 0, size: 5 },
  SUPPLIER: { page: 0, size: 10 }  // Khác với các API khác!
};
```

### 4. **Date Format Handling**
```javascript
const handleDates = (bookData) => {
  // Author birthDate: YYYY-MM-DD format
  // Book/Publisher timestamps: milliseconds since epoch
  // Category: không có date fields
  
  if (bookData.publicationDate) {
    // Convert to timestamp for Book API
    bookData.publicationDate = new Date(bookData.publicationDate).getTime();
  }
};
```

---

## 🚀 PERFORMANCE OPTIMIZATION

### 1. **Caching Strategy**
```javascript
// Cache dropdown data
const CACHE_DURATION = 5 * 60 * 1000; // 5 minutes
const dropdownCache = new Map();

const getCachedDropdown = async (endpoint) => {
  const cached = dropdownCache.get(endpoint);
  if (cached && (Date.now() - cached.timestamp < CACHE_DURATION)) {
    return cached.data;
  }
  
  const response = await fetch(endpoint);
  const data = await response.json();
  
  dropdownCache.set(endpoint, {
    data: data.data,
    timestamp: Date.now()
  });
  
  return data.data;
};
```

### 2. **Lazy Loading**
```javascript
// Load dropdown chỉ khi cần
const loadDropdownOnDemand = (type) => {
  if (!dropdowns[type]) {
    dropdowns[type] = getCachedDropdown(DROPDOWN_APIS[type]);
  }
  return dropdowns[type];
};
```

---

## 🔧 TESTING CHECKLIST

### Trước khi deploy frontend:

#### ✅ Book API:
- [ ] Tạo sách với authorIds
- [ ] Cập nhật sách với authorIds
- [ ] Validate authorIds không rỗng
- [ ] Test với publisherId
- [ ] Phân trang hoạt động
- [ ] Tìm kiếm hoạt động

#### ✅ Dropdown APIs:
- [ ] Tất cả dropdown APIs trả về đúng format
- [ ] Cache dropdown hoạt động
- [ ] Refresh cache khi cần

#### ✅ Error Handling:
- [ ] Handle response format khác nhau
- [ ] Validate dữ liệu trước khi submit
- [ ] Show error message thân thiện

#### ✅ Performance:
- [ ] Load time < 2s cho form tạo sách
- [ ] Pagination mượt mà
- [ ] Search/filter responsive

---

## 📞 HỖ TRỢ PHÁT TRIỂN

### Nếu gặp vấn đề:

1. **Kiểm tra tài liệu API chi tiết** trong từng file MD
2. **Test với cURL examples** trong từng tài liệu
3. **Validate request format** trước khi gửi
4. **Check response format** để handle đúng

### Quan trọng nhất:
- **BOOK API** là trung tâm của hệ thống
- **AuthorIds BẮT BUỘC** khi tạo/sửa sách
- **Dropdown APIs** cần được load trước khi hiển thị form

---

**Lưu ý:** Tài liệu này được cập nhật dựa trên source code hiện tại (03/07/2025). Đảm bảo test tất cả APIs trước khi triển khai production.
