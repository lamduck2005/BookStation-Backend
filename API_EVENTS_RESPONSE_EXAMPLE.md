# API Events - Response Example với Image URL

## Response mẫu sau khi cập nhật (bao gồm imageUrl):

```json
{
    "status": 200,
    "message": "Thành công",
    "data": {
        "content": [
            {
                "id": 8,
                "name": "Gặp gỡ tác giả Nguyễn Nhật Ánh - \"Tôi Thấy Hoa Vàng Trên Cỏ Xanh\"",
                "description": "Buổi gặp gỡ, ký tặng và tương tác cùng tác giả Nguyễn Nhật Ánh về tác phẩm kinh điển",
                "categoryId": 1,
                "categoryName": "Gặp gỡ tác giả",
                "eventType": "AUTHOR_MEET",
                "eventTypeName": "Gặp gỡ tác giả",
                "imageUrl": "https://example.com/images/event-nguyen-nhat-anh.jpg",
                "startDate": 1749725040000,
                "endDate": 1753353840000,
                "registrationDeadline": null,
                "maxParticipants": 100,
                "currentParticipants": 45,
                "entryFee": null,
                "status": 1,
                "createdAt": 1751107497648,
                "updatedAt": 1751107497648
            },
            {
                "id": 6,
                "name": "Ra mắt sách \"Nghệ thuật sống tối giản\"",
                "description": "Sự kiện ra mắt cuốn sách mới về lối sống tối giản, kèm tọa đàm với tác giả và chuyên gia tâm lý",
                "categoryId": 0,
                "categoryName": "Ra mắt sách mới",
                "eventType": "BOOK_LAUNCH",
                "eventTypeName": "Sự kiện ra mắt sách mới",
                "imageUrl": "https://example.com/images/book-launch-minimalism.jpg",
                "startDate": 1706947200000,
                "endDate": 1706961600000,
                "registrationDeadline": null,
                "maxParticipants": 300,
                "currentParticipants": 156,
                "entryFee": null,
                "status": 2,
                "createdAt": 1703865600000,
                "updatedAt": 1703865600000
            },
            {
                "id": 4,
                "name": "Hội chợ sách Tết 2025 - \"Đón Xuân Tri Thức\"",
                "description": "Hội chợ sách lớn nhất năm với hàng ngàn đầu sách, các hoạt động văn hóa đặc sắc và nhiều ưu đãi hấp dẫn",
                "categoryId": 3,
                "categoryName": "Hội chợ sách",
                "eventType": "BOOK_FAIR",
                "eventTypeName": "Hội chợ sách",
                "imageUrl": "https://example.com/images/book-fair-tet-2025.jpg",
                "startDate": 1704672000000,
                "endDate": 1705276800000,
                "registrationDeadline": null,
                "maxParticipants": 10000,
                "currentParticipants": 2847,
                "entryFee": null,
                "status": 1,
                "createdAt": 1703520000000,
                "updatedAt": 1703520000000
            },
            {
                "id": 5,
                "name": "Cuộc thi \"Review sách hay 2024\"",
                "description": "Cuộc thi viết review sách với giải thưởng tổng trị giá 50 triệu đồng cho những bài review hay nhất",
                "categoryId": 2,
                "categoryName": "Thử thách đọc sách",
                "eventType": "CONTEST",
                "eventTypeName": "Cuộc thi",
                "imageUrl": null,
                "startDate": 1702857600000,
                "endDate": 1705449600000,
                "registrationDeadline": null,
                "maxParticipants": 2000,
                "currentParticipants": 892,
                "entryFee": null,
                "status": 3,
                "createdAt": 1702771200000,
                "updatedAt": 1702771200000
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

## Thay đổi chính:

✅ **Đã thêm trường `imageUrl` vào response:**
- Trường này chứa URL của hình ảnh sự kiện
- Có thể có giá trị `null` nếu sự kiện chưa thiết lập hình ảnh
- Frontend có thể sử dụng để hiển thị thumbnail/banner cho từng sự kiện

## Lưu ý cho Frontend:

1. **Xử lý null value**: Cần check `imageUrl != null` trước khi hiển thị
2. **Fallback image**: Nên có ảnh mặc định khi `imageUrl` là null
3. **Image loading**: Nên có loading placeholder khi tải ảnh
4. **Error handling**: Xử lý trường hợp ảnh load lỗi

## Cấu trúc response hoàn chỉnh hiện tại:

```typescript
interface EventResponse {
    id: number;
    name: string;
    description: string;
    categoryId: number;
    categoryName: string;
    eventType: string;           // Enum value
    eventTypeName: string;       // Display name
    imageUrl: string | null;     // ← MỚI THÊM
    startDate: number;           // Timestamp
    endDate: number;             // Timestamp
    registrationDeadline: number | null;
    maxParticipants: number;
    currentParticipants: number;
    entryFee: number | null;
    status: number;              // 0=DRAFT, 1=PUBLISHED, 2=ONGOING, 3=COMPLETED, 4=CANCELLED
    createdAt: number;           // Timestamp
    updatedAt: number;           // Timestamp
}
```
