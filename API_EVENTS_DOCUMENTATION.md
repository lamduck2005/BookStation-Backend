# API Events - Tài liệu chi tiết

## 1. API GET List Events với Pagination và Filtering

### Endpoint:
```
GET /api/events
```

### Tham số truy vấn (Query Parameters):
| Tham số | Kiểu dữ liệu | Bắt buộc | Mặc định | Mô tả |
|---------|-------------|----------|----------|-------|
| `page` | `int` | Không | `0` | Số trang (bắt đầu từ 0) |
| `size` | `int` | Không | `5` | Số lượng bản ghi trên mỗi trang |
| `name` | `String` | Không | - | Tìm kiếm theo tên sự kiện (không phân biệt hoa thường) |
| `categoryId` | `Integer` | Không | - | Lọc theo ID danh mục sự kiện |
| `status` | `EventStatus` | Không | - | Lọc theo trạng thái (DRAFT, PUBLISHED, ONGOING, COMPLETED, CANCELLED) |
| `eventType` | `EventType` | Không | - | Lọc theo loại sự kiện (BOOK_LAUNCH, AUTHOR_MEET, READING_CHALLENGE, etc.) |
| `startDate` | `Long` | Không | - | Lọc sự kiện có ngày bắt đầu >= giá trị này (timestamp) |
| `endDate` | `Long` | Không | - | Lọc sự kiện có ngày kết thúc <= giá trị này (timestamp) |

### Ví dụ các request:

#### 1. Lấy tất cả sự kiện (trang đầu):
```
GET /api/events
```

#### 2. Tìm kiếm theo tên:
```
GET /api/events?name=gặp gỡ tác giả
```

#### 3. Lọc theo danh mục:
```
GET /api/events?categoryId=1
```

#### 4. Lọc theo trạng thái:
```
GET /api/events?status=PUBLISHED
```

#### 5. Lọc theo loại sự kiện:
```
GET /api/events?eventType=AUTHOR_MEET
```

#### 6. Lọc theo khoảng thời gian:
```
GET /api/events?startDate=1704067200000&endDate=1706745600000
```

#### 7. Kết hợp nhiều bộ lọc:
```
GET /api/events?page=0&size=10&name=sách&categoryId=1&status=PUBLISHED&eventType=BOOK_LAUNCH&startDate=1704067200000
```

### Response format:
```json
{
    "status": 200,
    "message": "Thành công",
    "data": {
        "content": [
            {
                "id": 8,
                "name": "Gặp gỡ tác giả Nguyễn Nhật Ánh",
                "description": "Buổi gặp gỡ, ký tặng và tương tác cùng tác giả",
                "categoryId": 1,
                "categoryName": "Gặp gỡ tác giả",
                "eventType": "AUTHOR_MEET",
                "eventTypeName": "Gặp gỡ tác giả",
                "imageUrl": "https://example.com/event-image.jpg",
                "startDate": 1749725040000,
                "endDate": 1753353840000,
                "registrationDeadline": null,
                "maxParticipants": 100,
                "currentParticipants": 0,
                "entryFee": null,
                "status": 1,
                "createdAt": 1751107497648,
                "updatedAt": 1751107497648
            }
        ],
        "pageNumber": 0,
        "pageSize": 5,
        "totalElements": 9,
        "totalPages": 2,
        "last": false
    }
}
```

## 2. Các enum values có thể sử dụng:

### EventStatus:
- `DRAFT` (0) - Bản nháp
- `PUBLISHED` (1) - Đã công bố  
- `ONGOING` (2) - Đang diễn ra
- `COMPLETED` (3) - Đã kết thúc
- `CANCELLED` (4) - Đã hủy

### EventType:
- `BOOK_LAUNCH` - Sự kiện ra mắt sách mới
- `AUTHOR_MEET` - Gặp gỡ tác giả
- `READING_CHALLENGE` - Thử thách đọc sách
- `BOOK_FAIR` - Hội chợ sách
- `SEASONAL_EVENT` - Sự kiện theo mùa
- `PROMOTION` - Sự kiện khuyến mãi
- `CONTEST` - Cuộc thi
- `WORKSHOP` - Hội thảo
- `DAILY_CHECKIN` - Điểm danh hàng ngày
- `LOYALTY_PROGRAM` - Chương trình khách hàng thân thiết
- `POINT_EARNING` - Sự kiện tích điểm
- `OTHER` - Khác

## 3. Cấu trúc Response data:

### EventResponse fields:
- `id`: ID sự kiện
- `name`: Tên sự kiện
- `description`: Mô tả sự kiện
- `categoryId`: ID danh mục sự kiện
- `categoryName`: Tên danh mục sự kiện
- `eventType`: Loại sự kiện (enum value)
- `eventTypeName`: Tên loại sự kiện (hiển thị)
- `imageUrl`: URL hình ảnh sự kiện
- `startDate`: Ngày bắt đầu (timestamp)
- `endDate`: Ngày kết thúc (timestamp)
- `registrationDeadline`: Hạn đăng ký (timestamp, có thể null)
- `maxParticipants`: Số người tham gia tối đa
- `currentParticipants`: Số người đã tham gia hiện tại
- `entryFee`: Phí tham gia (có thể null)
- `status`: Trạng thái sự kiện (số)
- `createdAt`: Thời gian tạo (timestamp)
- `updatedAt`: Thời gian cập nhật (timestamp)

### Pagination fields:
- `content`: Mảng dữ liệu sự kiện
- `pageNumber`: Số trang hiện tại
- `pageSize`: Kích thước trang
- `totalElements`: Tổng số bản ghi
- `totalPages`: Tổng số trang
- `last`: Có phải trang cuối hay không

## 4. Lưu ý cho Frontend:
1. Tất cả các tham số filter đều là optional
2. Có thể combine nhiều filter cùng lúc
3. Search theo tên không phân biệt hoa thường và tìm kiếm partial match
4. Timestamp sử dụng định dạng milliseconds
5. Response hiện tại đã bổ sung trường `eventType`, `eventTypeName` và `imageUrl`
6. Trường `imageUrl` có thể null nếu sự kiện chưa có hình ảnh
