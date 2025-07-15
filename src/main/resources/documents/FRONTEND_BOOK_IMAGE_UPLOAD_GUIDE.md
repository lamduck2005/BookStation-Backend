# Hướng dẫn upload và sử dụng nhiều ảnh cho sách (Book)

## Tóm tắt thay đổi (bắt buộc đọc kỹ)
- **Tất cả API liên quan đến sách (POST, PUT, GET list, GET trending, GET detail)** đều đã trả về trường `images` là **mảng URL** (List<String>), bao gồm cả API trending (sách nổi bật/xu hướng).
- **Trường `images` là mảng các URL ảnh chính của sách** (dùng cho gallery, carousel, chi tiết sản phẩm, v.v.).
- **Trường `coverImageUrl` chỉ dùng cho ảnh bìa nhỏ (thumbnail, hiển thị danh sách)**, không dùng cho ảnh chính.
- Khi tạo/cập nhật sách, frontend gửi lên trường `images` là mảng URL, backend sẽ lưu và trả về đúng định dạng mảng cho mọi API.
- Nếu code cũ lấy ảnh từ `coverImageUrl` để hiển thị ảnh chính, **bắt buộc sửa lại** để lấy từ `images`.
- **API trending (GET trending books)** cũng trả về trường `images` là mảng, frontend phải lấy ảnh chính từ đây.

---

## 1. API chi tiết và ví dụ dữ liệu trả về

### 1.1. API lấy danh sách sách (GET list)
- **Endpoint:** `GET /api/books`
- **Dữ liệu trả về (có phân trang):**
```json
{
  "data": [
    {
      "id": 1,
      "bookName": "Sách ABC",
      "description": "Mô tả sách ABC",
      "price": 120000,
      "stockQuantity": 50,
      "publicationDate": 1719900000000,
      "categoryId": 3,
      "categoryName": "Tiểu thuyết",
      "supplierId": 2,
      "supplierName": "NXB Trẻ",
      "bookCode": "ABC123",
      "status": 1,
      "createdAt": 1719900000000,
      "updatedAt": 1719900000000,
      "authors": [
        {"id": 1, "authorName": "Tác giả A", "biography": "Tiểu sử", "birthDate": 946684800000, "status": 1}
      ],
      "publisherId": 1,
      "publisherName": "NXB Kim Đồng",
      "coverImageUrl": "https://cdn.example.com/cover.jpg",
      "translator": "Người dịch A",
      "isbn": "978-604-2-12345-6",
      "pageCount": 320,
      "language": "Tiếng Việt",
      "weight": 400,
      "dimensions": "20x13x2",
      "images": [
        "https://cdn.example.com/img1.jpg",
        "https://cdn.example.com/img2.jpg"
      ]
    }
  ],
  "page": 1,
  "size": 20,
  "total": 100
}
```
- **Lưu ý:** Luôn lấy ảnh chính từ trường `images` (mảng), không lấy từ `coverImageUrl`.

### 1.2. API lấy danh sách sách trending (GET trending)
- **Endpoint:** `GET /api/books/trending`
- **Dữ liệu trả về (có phân trang):**
```json
{
  "data": [
    {
      "id": 2,
      "bookName": "Sách Trending",
      "description": "Sách hot nhất tháng này",
      "price": 150000,
      "originalPrice": 180000,
      "discountPercentage": 17,
      "stockQuantity": 30,
      "imageUrl": "https://cdn.example.com/cover_trend.jpg",
      "bookCode": "TREND001",
      "publicationDate": 1719900000000,
      "categoryId": 4,
      "categoryName": "Kinh tế",
      "authors": [
        {"id": 2, "authorName": "Tác giả B", "biography": "Tiểu sử B", "birthDate": 915148800000, "status": 1}
      ],
      "supplierId": 3,
      "supplierName": "NXB Lao Động",
      "rating": 4.8,
      "reviewCount": 120,
      "soldCount": 200,
      "orderCount": 180,
      "trendingScore": 9.2,
      "trendingRank": 1,
      "isInFlashSale": true,
      "flashSalePrice": 120000,
      "flashSaleStockQuantity": 10,
      "createdAt": 1719900000000,
      "updatedAt": 1719900000000,
      "images": [
        "https://cdn.example.com/trend1.jpg",
        "https://cdn.example.com/trend2.jpg"
      ],
      "coverImageUrl": "https://cdn.example.com/cover_trend.jpg"
    }
  ],
  "page": 1,
  "size": 10,
  "total": 30
}
```
- **Lưu ý:** API trending cũng trả về trường `images` là mảng. Không lấy ảnh chính từ `coverImageUrl`.

### 1.3. API chi tiết sách theo ID (GET detail)
- **Endpoint:** `GET /api/books/{id}`
- **Dữ liệu trả về:**
```json
{
  "id": 1,
  "bookName": "Sách ABC",
  "description": "Mô tả sách ABC",
  "price": 120000,
  "stockQuantity": 50,
  "publicationDate": 1719900000000,
  "categoryId": 3,
  "categoryName": "Tiểu thuyết",
  "supplierId": 2,
  "supplierName": "NXB Trẻ",
  "bookCode": "ABC123",
  "status": 1,
  "createdAt": 1719900000000,
  "updatedAt": 1719900000000,
  "authors": [
    {"id": 1, "authorName": "Tác giả A", "biography": "Tiểu sử", "birthDate": 946684800000, "status": 1}
  ],
  "publisherId": 1,
  "publisherName": "NXB Kim Đồng",
  "coverImageUrl": "https://cdn.example.com/cover.jpg",
  "translator": "Người dịch A",
  "isbn": "978-604-2-12345-6",
  "pageCount": 320,
  "language": "Tiếng Việt",
  "weight": 400,
  "dimensions": "20x13x2",
  "images": [
    "https://cdn.example.com/img1.jpg",
    "https://cdn.example.com/img2.jpg"
  ]
}
```
- **Lưu ý:** Khi render chi tiết, lấy ảnh từ `images` để hiển thị gallery/carousel.

### 1.4. API cập nhật sách (PUT update)
- **Endpoint:** `PUT /api/books/{id}`
- **Request body:**
```json
{
  "bookName": "Sách ABC sửa",
  "description": "Mô tả mới",
  "price": 130000,
  "stockQuantity": 40,
  "publicationDate": 1719900000000,
  "categoryId": 3,
  "supplierId": 2,
  "bookCode": "ABC123",
  "status": 1,
  "authors": [1],
  "publisherId": 1,
  "coverImageUrl": "https://cdn.example.com/cover_new.jpg",
  "translator": "Người dịch A",
  "isbn": "978-604-2-12345-6",
  "pageCount": 320,
  "language": "Tiếng Việt",
  "weight": 400,
  "dimensions": "20x13x2",
  "images": [
    "https://cdn.example.com/new1.jpg",
    "https://cdn.example.com/new2.jpg"
  ]
}
```
- **Dữ liệu trả về:**
```json
{
  "id": 1,
  "bookName": "Sách ABC sửa",
  "description": "Mô tả mới",
  "price": 130000,
  "stockQuantity": 40,
  "publicationDate": 1719900000000,
  "categoryId": 3,
  "categoryName": "Tiểu thuyết",
  "supplierId": 2,
  "supplierName": "NXB Trẻ",
  "bookCode": "ABC123",
  "status": 1,
  "createdAt": 1719900000000,
  "updatedAt": 1719900000000,
  "authors": [
    {"id": 1, "authorName": "Tác giả A", "biography": "Tiểu sử", "birthDate": 946684800000, "status": 1}
  ],
  "publisherId": 1,
  "publisherName": "NXB Kim Đồng",
  "coverImageUrl": "https://cdn.example.com/cover_new.jpg",
  "translator": "Người dịch A",
  "isbn": "978-604-2-12345-6",
  "pageCount": 320,
  "language": "Tiếng Việt",
  "weight": 400,
  "dimensions": "20x13x2",
  "images": [
    "https://cdn.example.com/new1.jpg",
    "https://cdn.example.com/new2.jpg"
  ]
}
```
- **Lưu ý:** Nếu cập nhật không thành công (không đổi được ảnh), kiểm tra lại request gửi lên đã đúng dạng mảng chưa, hoặc liên hệ backend để kiểm tra API.

### 1.5. API xóa sách (DELETE)
- **Endpoint:** `DELETE /api/books/{id}`
- **Dữ liệu trả về:**
```json
{
  "message": "Xóa sách thành công"
}
```
- **Lưu ý:** Nếu xóa không thành công, kiểm tra lại quyền hoặc liên hệ backend.

---

## 0. Ví dụ payload khi tạo/cập nhật sách (POST/PUT)
- **Payload mẫu:**
```json
{
  "bookName": "Doraemon tập mới #1751865434861",
  "description": "Câu chuyện hấp dẫn với nhiều tình tiết bất ngờ",
  "price": 120000,
  "stockQuantity": 10,
  "publicationDate": 1663772223332,
  "categoryId": 16,
  "supplierId": 2,
  "bookCode": "BOOK1751865434861",
  "status": 1,
  "authorIds": [8, 1, 6],
  "publisherId": 6,
  "coverImageUrl": "",
  "images": [],
  "translator": "Nguyễn Văn A",
  "isbn": "978-604-2-12345-6",
  "pageCount": 320,
  "language": "Tiếng Việt",
  "weight": 400,
  "dimensions": "20x13x2"
}
```
- **Lưu ý:**
  - Trường `authorIds` là mảng số, bắt buộc phải có ít nhất 1 ID tác giả.
  - Trường `images` là mảng URL ảnh chính (có thể rỗng nếu chưa có ảnh).
  - Các trường khác điền đúng kiểu dữ liệu như trên.

## 2. Lưu ý cho frontend
- Khi upload nhiều ảnh, hãy upload lên storage (S3, Cloudinary, v.v.) và lấy danh sách URL, gửi lên backend qua trường `images` (mảng).
- Khi hiển thị chi tiết sách, lấy danh sách ảnh từ trường `images` để render gallery/carousel.
- Nếu không có ảnh, trường `images` sẽ là mảng rỗng (`[]`).
- Trường `coverImageUrl` chỉ dùng cho ảnh bìa nhỏ, không phải ảnh chính.
- **Luôn sử dụng `images` để hiển thị ảnh chính sản phẩm**. Không lấy ảnh từ `coverImageUrl`.

## 3. Xử lý lỗi khi cập nhật/xóa ảnh
- Nếu cập nhật/xóa ảnh không thành công:
  - Kiểm tra lại request gửi lên đã đúng dạng chưa (`images` là mảng URL).
  - Kiểm tra response trả về có thông báo lỗi gì không.
  - Nếu vẫn lỗi, liên hệ backend để kiểm tra log và xử lý.

---
**Mọi thắc mắc vui lòng liên hệ backend để được hỗ trợ.**
