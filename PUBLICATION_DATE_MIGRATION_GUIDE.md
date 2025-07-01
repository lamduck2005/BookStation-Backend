# Thay đổi trường publicationDate từ LocalDate sang Long

## Mô tả
Trường `publicationDate` trong hệ thống BookStation đã được thay đổi từ kiểu `LocalDate` sang `Long` để lưu trữ dưới dạng timestamp (milliseconds since Unix epoch).

## Lý do thay đổi
- **Tính nhất quán**: Các trường khác như `createdAt`, `updatedAt` đã sử dụng `Long` timestamp
- **Hiệu suất database**: `BIGINT` có hiệu suất tốt hơn `DATE` trong MySQL
- **Linh hoạt**: Có thể lưu trữ thời gian chính xác đến millisecond
- **API consistency**: Frontend có thể xử lý timestamp một cách nhất quán

## Các thay đổi được thực hiện

### 1. Entity Book
```java
// Trước
@Column(name = "publication_date")
private LocalDate publicationDate;

// Sau  
@Column(name = "publication_date")
private Long publicationDate;
```

### 2. BookRequest DTO
```java
// Trước
private LocalDate publicationDate;

// Sau
private Long publicationDate;
```

### 3. BookResponse DTO  
```java
// Trước
private LocalDate publicationDate;

// Sau
private Long publicationDate;
```

### 4. DataInitializationService
Tất cả data mẫu đã được cập nhật để sử dụng timestamp:
```java
// Ví dụ: 2010-01-01 = 1262304000000L
createBook("Tôi thấy hoa vàng trên cỏ xanh", "...", price, stock, 1262304000000L, ...)
```

## Migration Database

### Script SQL Migration
File: `migration_publicationDate_to_bigint.sql`

```sql
-- Thay đổi cột publication_date từ DATE sang BIGINT
ALTER TABLE book ADD COLUMN publication_date_new BIGINT;
UPDATE book SET publication_date_new = UNIX_TIMESTAMP(publication_date) * 1000 WHERE publication_date IS NOT NULL;
ALTER TABLE book DROP COLUMN publication_date;
ALTER TABLE book RENAME COLUMN publication_date_new TO publication_date;
```

## Utility Class
Đã tạo `DateTimeUtil` để hỗ trợ convert giữa `LocalDate` và `Long` timestamp:

```java
// Convert LocalDate sang timestamp
Long timestamp = DateTimeUtil.dateToTimestamp(LocalDate.of(2010, 1, 1));

// Convert timestamp sang LocalDate  
LocalDate date = DateTimeUtil.timestampToDate(1262304000000L);

// Tạo timestamp cho ngày cụ thể
Long timestamp = DateTimeUtil.createTimestamp(2010, 1, 1);
```

## API Testing
Endpoint test được thêm vào:
```
GET /api/books/test-publication-date
```

Response:
```json
{
  "status": 200,
  "message": "Test publicationDate conversion thành công",
  "data": {
    "originalDate": "2010-01-01",
    "timestamp": 1262304000000,
    "convertedBack": "2010-01-01", 
    "isEqual": true,
    "currentTimestamp": 1751464200000
  }
}
```

## Cách sử dụng trong Frontend

### JavaScript
```javascript
// Convert timestamp sang Date object
const publicationDate = new Date(1262304000000);

// Format hiển thị  
const formatDate = new Date(book.publicationDate).toLocaleDateString('vi-VN');

// Gửi data tới API
const bookData = {
  bookName: "Tên sách",
  publicationDate: new Date("2010-01-01").getTime() // Convert to timestamp
};
```

### Ví dụ timestamps cho các năm phổ biến
```
1970-01-01: 0L
1990-01-01: 631152000000L  
2000-01-01: 946684800000L
2010-01-01: 1262304000000L
2020-01-01: 1577836800000L
2025-01-01: 1735689600000L
```

## Testing
Chạy test để verify conversion:
```bash
./mvnw test -Dtest=PublicationDateConversionTest
```

## Lưu ý quan trọng
1. **Backup database** trước khi chạy migration
2. **Cập nhật frontend** để xử lý timestamp thay vì LocalDate
3. **Test kỹ lưỡng** các chức năng liên quan đến ngày xuất bản
4. **Timezone**: Timestamp được lưu theo UTC, cần convert khi hiển thị theo timezone local
