# Hệ thống Quản lý Sự kiện BookStation

## Tổng quan

Hệ thống quản lý sự kiện cho phép BookStation tạo và quản lý các sự kiện liên quan đến sách, tác giả và khuyến mãi. Hệ thống bao gồm các tính năng:

- Tạo và quản lý sự kiện
- Phân loại sự kiện theo danh mục
- Quản lý quà tặng cho sự kiện  
- Theo dõi người tham gia
- Quản lý việc nhận và phát quà
- Lưu trữ lịch sử sự kiện

## Các Entity

### 1. EventCategory
Phân loại các sự kiện theo danh mục như:
- Ra mắt sách mới
- Gặp gỡ tác giả
- Thử thách đọc sách
- Hội chợ sách
- Sự kiện mùa
- Khuyến mãi đặc biệt
- Cuộc thi
- Hội thảo

### 2. Event
Entity chính quản lý thông tin sự kiện:
- Thông tin cơ bản: tên, mô tả, thời gian
- Loại sự kiện (EventType enum)
- Danh mục sự kiện (EventCategory)
- Trạng thái: DRAFT, PUBLISHED, ONGOING, COMPLETED, CANCELLED
- Số lượng người tham gia tối đa và hiện tại
- Địa điểm (online/offline)
- Quy định tham gia

### 3. EventGift
Quản lý các phần quà cho sự kiện:
- Thông tin quà tặng: tên, mô tả, giá trị
- Số lượng và số lượng còn lại
- Loại quà: BOOK, VOUCHER, POINT, PHYSICAL_ITEM
- Liên kết với Book/Voucher (nếu có)
- Điểm thưởng (nếu quà là điểm)

### 4. EventParticipant
Theo dõi người tham gia sự kiện:
- Thông tin user tham gia
- Thời gian tham gia
- Trạng thái hoàn thành
- Thông tin về việc trúng thưởng
- Quà đã nhận

### 5. EventGiftClaim
Quản lý việc nhận quà của người tham gia:
- Thông tin người nhận và quà tặng
- Trạng thái giao hàng
- Địa chỉ giao hàng
- Thời gian nhận và giao

### 6. EventHistory
Lưu trữ lịch sử thay đổi của sự kiện:
- Loại hành động: CREATED, UPDATED, PUBLISHED, STARTED, COMPLETED, CANCELLED
- Người thực hiện thay đổi
- Giá trị cũ và mới (JSON format)

## Các Enum

### EventType
- BOOK_LAUNCH: Ra mắt sách
- AUTHOR_MEET: Gặp gỡ tác giả  
- READING_CHALLENGE: Thử thách đọc sách
- BOOK_FAIR: Hội chợ sách
- SEASONAL_EVENT: Sự kiện theo mùa
- PROMOTION: Khuyến mãi
- CONTEST: Cuộc thi
- WORKSHOP: Hội thảo
- OTHER: Khác

### EventStatus
- DRAFT: Bản nháp
- PUBLISHED: Đã công bố
- ONGOING: Đang diễn ra
- COMPLETED: Đã kết thúc
- CANCELLED: Đã hủy

## Workflow

### 1. Tạo sự kiện
1. Admin tạo sự kiện với trạng thái DRAFT
2. Thiết lập thông tin cơ bản, thời gian, địa điểm
3. Thêm các quà tặng cho sự kiện
4. Publish sự kiện (chuyển trạng thái thành PUBLISHED)

### 2. Tham gia sự kiện
1. User đăng ký tham gia sự kiện
2. Hệ thống tạo record EventParticipant
3. Cập nhật số lượng người tham gia hiện tại

### 3. Hoàn thành và nhận quà
1. User hoàn thành yêu cầu sự kiện
2. Hệ thống cập nhật completion_status
3. Nếu trúng thưởng, user có thể claim quà
4. Tạo record EventGiftClaim để tracking việc giao quà

### 4. Kết thúc sự kiện
1. Sự kiện tự động chuyển thành COMPLETED khi hết thời gian
2. Thống kê kết quả và gửi thông báo
3. Lưu lịch sử vào EventHistory

## Files đã tạo

### Entity Classes
- `Event.java` - Entity chính
- `EventCategory.java` - Danh mục sự kiện
- `EventGift.java` - Quà tặng sự kiện
- `EventParticipant.java` - Người tham gia
- `EventGiftClaim.java` - Claim quà tặng
- `EventHistory.java` - Lịch sử sự kiện

### Enums
- `EventType.java` - Loại sự kiện
- `EventStatus.java` - Trạng thái sự kiện

### Database Scripts
- `create_event_tables.sql` - Script tạo bảng
- `event_sample_data.sql` - Dữ liệu mẫu

## Cách sử dụng

1. **Chạy script tạo bảng**: Thực thi `create_event_tables.sql` để tạo các bảng cần thiết
2. **Import dữ liệu mẫu**: Chạy `event_sample_data.sql` để có dữ liệu test
3. **Tạo Repository**: Tạo các repository interface cho từng entity
4. **Tạo Service**: Implement business logic trong service layer
5. **Tạo Controller**: Tạo REST API endpoints để quản lý sự kiện

## Ví dụ sử dụng

### Tạo sự kiện mới
```java
Event event = new Event();
event.setEventName("Ra mắt sách ABC");
event.setDescription("Sự kiện ra mắt sách mới...");
event.setEventType(EventType.BOOK_LAUNCH);
event.setStartDate(LocalDateTime.now().plusDays(7));
event.setMaxParticipants(100);
```

### Tham gia sự kiện
```java
EventParticipant participant = new EventParticipant();
participant.setEvent(event);
participant.setUser(user);
participant.setCompletionStatus("JOINED");
```

### Thêm quà tặng
```java
EventGift gift = new EventGift();
gift.setEvent(event);
gift.setGiftName("Sách ký tặng");
gift.setGiftType("BOOK");
gift.setQuantity(50);
```
