# TÀI LIỆU API BOOK CONTROLLER

## TỔNG QUAN HỆ THỐNG BOOK CONTROLLER

### Các API chính:
1. **GET /api/books** - Danh sách Book có phân trang và filter
2. **POST /api/books/trending** - Sản phẩm xu hướng 
3. **GET /api/books/{id}** - Chi tiết Book
4. **POST /api/books** - Tạo Book mới
5. **PUT /api/books/{id}** - Cập nhật Book
6. **DELETE /api/books/{id}** - Xóa Book
7. **PATCH /api/books/{id}/toggle-status** - Bật/tắt trạng thái
8. **GET /api/books/dropdown** - Dropdown Books
9. **GET /api/books/category/{id}** - Books theo danh mục
10. **GET /api/books/supplier/{id}** - Books theo nhà cung cấp
11. **GET /api/books/active** - Books đang hoạt động

---

## 1. DANH SÁCH BOOK (GET /api/books)

### Request Parameters:
```
page (int): Trang hiện tại (default: 0)
size (int): Số phần tử/trang (default: 5)
bookName (string): Tìm theo tên sách
categoryId (int): Lọc theo danh mục
supplierId (int): Lọc theo nhà cung cấp  
publisherId (int): Lọc theo nhà xuất bản
minPrice (decimal): Giá tối thiểu
maxPrice (decimal): Giá tối đa
status (byte): Trạng thái (0: inactive, 1: active)
bookCode (string): Mã sách
```

### Response (BookResponse):
```json
{
    "status": 200,
    "message": "Thành công",
    "data": {
        "content": [
            {
                "id": 1,
                "bookName": "Tên sách",
                "description": "Mô tả",
                "price": 150000,
                "stockQuantity": 100,
                "publicationDate": 1641859200000,
                "categoryName": "Văn học",
                "categoryId": 1,
                "supplierName": "NXB Kim Đồng",
                "supplierId": 1,
                "publisherName": "NXB Trẻ",
                "publisherId": 1,
                "bookCode": "BOOK001",
                "status": 1,
                "createdAt": 1641859200000,
                "updatedAt": 1641859200000,
                "authors": [
                    {
                        "id": 1,
                        "authorName": "Nguyễn Nhật Ánh",
                        "biography": "Tiểu sử tác giả",
                        "birthDate": 123456789000,
                        "status": 1
                    }
                ],
                "coverImageUrl": "https://example.com/cover.jpg",
                "translator": "Người dịch",
                "isbn": "978-604-2-00001-1",
                "pageCount": 200,
                "language": "Tiếng Việt",
                "weight": 300,
                "dimensions": "20x14x1.5",
                "images": [
                    "https://example.com/image1.jpg",
                    "https://example.com/image2.jpg"
                ],
                "soldCount": 150,
                "discountValue": 20000,
                "discountPercent": null,
                "discountActive": true,
                "isInFlashSale": true,
                "flashSalePrice": 120000,
                "flashSaleSoldCount": 50,
                "flashSaleEndTime": 1641945600000
            }
        ],
        "pageNumber": 0,
        "pageSize": 5,
        "totalElements": 100,
        "totalPages": 20
    }
}
```

### Nghiệp vụ:
- **ADMIN QUAN TRỌNG**: Danh sách này trả về ĐẦY ĐỦ thông tin cần thiết cho admin theo dõi:
  - `soldCount`: Tổng số đã bán (tính từ OrderDetail có status đơn hàng hợp lệ)
  - `discountValue/discountPercent`: Giảm giá trực tiếp của sách (không phải flash sale)
  - `isInFlashSale`: Có đang trong flash sale hay không
  - `flashSalePrice`: Giá flash sale hiện tại
  - `flashSaleSoldCount`: Số lượng đã bán riêng trong flash sale
  - `stockQuantity`: Tồn kho hiện tại
- **Filter mạnh mẽ**: Hỗ trợ filter theo nhiều tiêu chí
- **Phân trang**: Pagination với thông tin đầy đủ

---

## 2. SAN PHẨM XU HƯỚNG (POST /api/books/trending)

### Request Body (TrendingRequest):
```json
{
    "type": "DAILY_TRENDING",
    "page": 0,
    "size": 10
}
```

### Loại trending:
- **"DAILY_TRENDING"**: Xu hướng theo ngày (dựa trên sales + reviews)
- **"HOT_DISCOUNT"**: Sách hot giảm sốc (flash sale + discount cao)

### Response (TrendingBookResponse):
```json
{
    "status": 200,
    "message": "Lấy danh sách sản phẩm xu hướng theo ngày thành công",
    "data": {
        "content": [
            {
                "id": 1,
                "bookName": "Sách xu hướng",
                "description": "Mô tả ngắn",
                "price": 150000,
                "originalPrice": 180000,
                "discountPercentage": 17,
                "stockQuantity": 100,
                "imageUrl": "https://example.com/cover.jpg",
                "bookCode": "BOOK001",
                "publicationDate": 1641859200000,
                "categoryId": 1,
                "categoryName": "Văn học",
                "authors": [
                    {
                        "id": 1,
                        "authorName": "Nguyễn Nhật Ánh"
                    }
                ],
                "supplierId": 1,
                "supplierName": "NXB Kim Đồng",
                "rating": 4.5,
                "reviewCount": 120,
                "soldCount": 300,
                "orderCount": 180,
                "trendingScore": 8.5,
                "trendingRank": 1,
                "isInFlashSale": true,
                "flashSalePrice": 120000,
                "flashSaleStockQuantity": 50,
                "createdAt": 1641859200000,
                "updatedAt": 1641859200000,
                "images": [
                    "https://example.com/image1.jpg",
                    "https://example.com/image2.jpg"
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

### Nghiệp vụ:
- **FRONTEND QUAN TRỌNG**: 
  - `images`: Danh sách ảnh sản phẩm (KHÔNG phải ảnh nền)
  - `soldCount`: Số lượng đã bán trong 30 ngày
  - `isInFlashSale`: Đánh dấu để frontend biết có flash sale
  - `flashSalePrice`: Giá flash sale nếu có
  - `discountPercentage`: % giảm giá chính xác
- **Không còn bộ lọc**: API trending chỉ nhận type, page, size, không nhận các filter khác.
- **Cache**: Có cache 30 phút cho performance
- **Fallback**: Nếu không đủ dữ liệu trending, sẽ fallback sang sách phổ biến

---

## 3. CHI TIẾT BOOK (GET /api/books/{id})

### Response (BookDetailResponse):
```json
{
    "status": 200,
    "message": "Thành công",
    "data": {
        "id": 1,
        "bookName": "Tên sách chi tiết",
        "description": "Mô tả đầy đủ",
        "price": 150000,
        "stockQuantity": 100,
        "publicationDate": 1641859200000,
        "categoryName": "Văn học",
        "categoryId": 1,
        "supplierName": "NXB Kim Đồng",
        "supplierId": 1,
        "bookCode": "BOOK001",
        "status": 1,
        "createdAt": 1641859200000,
        "updatedAt": 1641859200000,
        "authors": [
            {
                "id": 1,
                "authorName": "Nguyễn Nhật Ánh",
                "biography": "Tiểu sử đầy đủ",
                "birthDate": 123456789000,
                "status": 1
            }
        ],
        "publisherName": "NXB Trẻ",
        "publisherId": 1,
        "coverImageUrl": "https://example.com/cover.jpg",
        "translator": "Người dịch",
        "isbn": "978-604-2-00001-1",
        "pageCount": 200,
        "language": "Tiếng Việt",
        "weight": 300,
        "dimensions": "20x14x1.5",
        "flashSalePrice": 120000,
        "flashSaleDiscount": 20.0,
        "flashSaleEndTime": 1641945600000,
        "flashSaleSoldCount": 50,
        "discountValue": null,
        "discountPercent": null,
        "serverTime": 1641859200000
    }
}
```

### Nghiệp vụ:
- **Flash Sale Info**: Thông tin flash sale nếu đang có
- **Direct Discount**: Thông tin giảm giá cố định của book
- **Server Time**: Thời gian server để frontend sync thời gian
- **404**: Trả về 404 nếu không tìm thấy sách

---

## 4. TẠO BOOK MỚI (POST /api/books)

### Request Body (BookRequest):
```json
{
    "bookName": "Tên sách mới",
    "description": "Mô tả sách",
    "price": 150000,
    "stockQuantity": 100,
    "publicationDate": 1641859200000,
    "categoryId": 1,
    "supplierId": 1,
    "publisherId": 1,
    "bookCode": "BOOK002",
    "status": 1,
    "authorIds": [1, 2],
    "coverImageUrl": "https://example.com/cover.jpg",
    "images": [
        "https://example.com/image1.jpg",
        "https://example.com/image2.jpg"
    ],
    "translator": "Người dịch",
    "isbn": "978-604-2-00002-1",
    "pageCount": 250,
    "language": "Tiếng Việt",
    "weight": 350,
    "dimensions": "21x14x2"
}
```

### Validation:
- **bookName**: Bắt buộc, không trùng
- **authorIds**: Bắt buộc, ít nhất 1 tác giả
- **price**: Bắt buộc, > 0
- **stockQuantity**: Bắt buộc, >= 0
- **categoryId, supplierId, publisherId**: Phải tồn tại trong DB
- **bookCode**: Không trùng, auto-generate nếu null

### Response Success (201):
```json
{
    "status": 201,
    "message": "Tạo sách thành công",
    "data": {
        // BookResponse object
    }
}
```

### Response Error:
```json
{
    "status": 400,
    "message": "Tên sách đã tồn tại",
    "data": null
}
```

### Nghiệp vụ:
- **Auto-generate bookCode** nếu không cung cấp
- **Tạo relationship** AuthorBook tự động
- **Validate images** URL format
- **Status = 1** (active) mặc định
- **Multi-image support**: Lưu nhiều ảnh cách nhau bằng dấu phẩy

---

## 5. CẬP NHẬT BOOK (PUT /api/books/{id})

### Request Body: Giống như tạo mới
### Response Success (200):
```json
{
    "status": 200,
    "message": "Cập nhật sách thành công",
    "data": {
        // BookResponse object
    }
}
```

### Response Error:
```json
{
    "status": 404,
    "message": "Không tìm thấy sách",
    "data": null
}
```

### Nghiệp vụ:
- **Validate uniqueness** (loại trừ chính nó)
- **Update relationships**: Xóa AuthorBook cũ, tạo mới
- **Partial update**: Chỉ update field được cung cấp
- **Image management**: Update danh sách ảnh
- **Audit trail**: Tự động update updatedAt, updatedBy

---

## 6. XÓA BOOK (DELETE /api/books/{id})

### Response: 204 No Content

### Nghiệp vụ:
- **Hard delete**: Xóa hoàn toàn khỏi DB
- **Cascade**: AuthorBook sẽ được xóa tự động
- **Không có validation**: Có thể xóa ngay cả khi có đơn hàng

---

## 7. BẬT/TẮT TRẠNG THÁI (PATCH /api/books/{id}/toggle-status)

### Response Success (200):
```json
{
    "status": 200,
    "message": "Cập nhật trạng thái thành công",
    "data": {
        // BookResponse object với status đã thay đổi
    }
}
```

### Nghiệp vụ:
- **Toggle**: 1 (active) ↔ 0 (inactive)
- **Soft control**: Không ảnh hưởng đến data, chỉ ẩn/hiện

---

## CÁC ENDPOINT KHÁC

### 8. Dropdown Books (GET /api/books/dropdown)
- **Mục đích**: Để chọn sách trong form
- **Response**: `[{id, bookName}]`
- **Chỉ active books**

### 9. Books theo Category (GET /api/books/category/{id})
- **Response**: Array BookResponse
- **Không phân trang**

### 10. Books theo Supplier (GET /api/books/supplier/{id})
- **Response**: Array BookResponse  
- **Không phân trang**

### 11. Active Books (GET /api/books/active)
- **Response**: Array BookResponse
- **Chỉ status = 1**

---

## LỖI PHỔ BIẾN VÀ XỬ LÝ

### 400 Bad Request:
- Tên sách trùng
- Mã sách trùng  
- Thiếu tác giả
- Validation failed

### 404 Not Found:
- Sách không tồn tại
- Category/Supplier/Publisher không tồn tại
- Tác giả không tồn tại

### 500 Internal Server Error:
- Lỗi database
- Lỗi không mong muốn

---

## NOTES QUAN TRỌNG CHO FRONTEND

### 1. Phân biệt loại giảm giá:
- **discountValue/discountPercent**: Giảm giá cố định của sách
- **flashSalePrice**: Giá flash sale (có thời hạn)
- **isInFlashSale**: Kiểm tra để hiển thị UI phù hợp

### 2. Hình ảnh:
- **images**: Mảng ảnh sản phẩm chính (dùng để hiển thị)
- **coverImageUrl**: Ảnh bìa (có thể dùng làm fallback)

### 3. Trending:
- **DAILY_TRENDING**: Không dùng categoryId
- **HOT_DISCOUNT**: Dùng categoryId để filter
- **isInFlashSale**: Đánh dấu để hiển thị badge flash sale

### 4. Số lượng đã bán:
- **soldCount**: Tổng đã bán (cho trending/list)
- **flashSaleSoldCount**: Đã bán trong flash sale hiện tại

### 5. Thời gian:
- **Timestamp format**: Long (milliseconds)
- **serverTime**: Dùng để sync thời gian client-server

---

## CẢI THIỆN HOÀN THIỆN HỆ THỐNG

### Các sửa đổi đã thực hiện:

#### 1. ✅ BookResponse - Thêm thông tin admin cần:
- `soldCount`: Tổng số lượng đã bán (từ OrderDetail có order status hợp lệ)
- `discountValue/discountPercent/discountActive`: Thông tin giảm giá trực tiếp
- `isInFlashSale`: Đánh dấu có flash sale không
- `flashSalePrice/flashSaleSoldCount/flashSaleEndTime`: Thông tin flash sale chi tiết

#### 2. ✅ TrendingBookResponse - Thêm trường thiếu:
- `flashSaleSoldCount`: Số lượng đã bán riêng trong flash sale
- `images`: Danh sách ảnh sản phẩm hoàn chỉnh

#### 3. ✅ BookRequest - Validation chặt chẽ:
- `@NotBlank` cho bookName
- `@NotNull @DecimalMin` cho price
- `@NotNull @Min` cho stockQuantity
- `@NotEmpty` cho authorIds

#### 4. ✅ Repository Methods - Thêm queries mới:
- `OrderDetailRepository.countSoldQuantityByBook()`: Đếm tổng đã bán
- `FlashSaleItemRepository.findActiveFlashSaleByBook()`: Tìm flash sale hiện tại

#### 5. ✅ BookResponseMapper - Logic tính toán:
- Tự động tính `soldCount` từ OrderDetail
- Kiểm tra `Flash Sale` hiện tại và set thông tin
- Xử lý `images` từ chuỗi thành mảng

#### 6. ✅ Mapper Updates:
- TrendingBookMapper hỗ trợ `flashSaleSoldCount` và `images`
- Fallback strategy cho sách chưa có dữ liệu trending

---

## KIỂM TRA HỆ THỐNG HOÀN THIỆN

### ✅ CRUD Book - Đúng nghiệp vụ:
1. **CREATE**: Validation đầy đủ, auto-generate bookCode, tạo AuthorBook relationships
2. **READ**: Trả về đầy đủ thông tin admin cần (sold count, discounts, flash sale)
3. **UPDATE**: Partial update, preserve relationships, audit trail
4. **DELETE**: Hard delete với cascade relationships
5. **TOGGLE STATUS**: Soft control không ảnh hưởng data

### ✅ List Books - Thông tin đầy đủ:
- **Phân trang**: Có pagination với filter mạnh mẽ
- **Admin Info**: soldCount, stock, discounts, flash sale status
- **Filter**: Theo tên, category, supplier, publisher, price range, status
- **Performance**: Được optimize với proper indexing

### ✅ Trending API - Nghiệp vụ đúng:
- **DAILY_TRENDING**: Xu hướng tổng thể (không filter category)
- **HOT_DISCOUNT**: Sách giảm giá (có filter category)
- **Images**: Ảnh sản phẩm (không phải ảnh nền)
- **Flash Sale Info**: Đầy đủ thông tin và đánh dấu cho frontend
- **Sold Count**: Riêng biệt cho trending và flash sale

### ✅ Detail API - Thông tin chi tiết:
- **Flash Sale**: Giá, thời gian kết thúc, số lượng đã bán
- **Direct Discount**: Giảm giá cố định của sách
- **Server Time**: Sync thời gian cho frontend
- **Complete Info**: Tất cả thông tin cần thiết để hiển thị

---

## KẾT LUẬN

Hệ thống BookController hiện tại đã hoàn thiện với:

🎯 **Đáp ứng yêu cầu admin**: Đầy đủ thông tin để theo dõi và quản lý
🎯 **Đáp ứng yêu cầu frontend**: API rõ ràng, response đầy đủ
🎯 **Nghiệp vụ đúng**: CRUD logic đúng thực tế, validation chặt chẽ
🎯 **Performance tốt**: Cache, pagination, optimize queries
🎯 **Error handling**: Rõ ràng, đầy đủ status codes

**Sẵn sàng production và integration với frontend!**
