# EVENT SYSTEM - SAMPLE DATA INSERT SCRIPTS

Tài liệu này cung cấp các câu lệnh SQL INSERT để tạo dữ liệu mẫu cho hệ thống Event.
Dữ liệu mẫu bao gồm các kịch bản thực tế để test toàn bộ workflow của hệ thống event.

## Lưu ý quan trọng
- **Sử dụng SQL Server**: Script được viết cho SQL Server với `IDENTITY(1,1)` và `DBCC CHECKIDENT`
- **Chạy các script theo thứ tự**: Từ trên xuống dưới để đảm bảo ràng buộc foreign key
- **Timestamp**: Sử dụng miliseconds (Unix timestamp * 1000)
- **Unicode**: Tất cả chuỗi tiếng Việt đều có tiền tố `N` để hỗ trợ Unicode
- **Dữ liệu mẫu**: Giả định đã có user và một số book/voucher trong hệ thống
- **Các ID**: Sẽ tự động tăng bởi IDENTITY, không cần chỉ định

## 1. EVENT_CATEGORY - Danh mục sự kiện

```sql
-- Xóa dữ liệu mẫu cũ (nếu có)
DELETE FROM event_category WHERE id BETWEEN 1 AND 6;

-- Reset identity seed (SQL Server)
DBCC CHECKIDENT ('event_category', RESEED, 0);

-- Chèn dữ liệu danh mục sự kiện (không chỉ định ID, để IDENTITY tự tăng)
INSERT INTO event_category (category_name, description, icon_url, is_active, created_at) VALUES
(N'Ra mắt sách mới', N'Các sự kiện giới thiệu và ra mắt những cuốn sách mới nhất', N'/icons/book-launch.png', 1, 1703001600000),
(N'Gặp gỡ tác giả', N'Sự kiện gặp gỡ, ký tặng và tương tác trực tiếp với các tác giả nổi tiếng', N'/icons/author-meet.png', 1, 1703001600000),
(N'Thử thách đọc sách', N'Các cuộc thi và thử thách khuyến khích việc đọc sách', N'/icons/reading-challenge.png', 1, 1703001600000),
(N'Hội chợ sách', N'Triển lãm và hội chợ sách với nhiều ưu đãi đặc biệt', N'/icons/book-fair.png', 1, 1703001600000),
(N'Sự kiện theo mùa', N'Sự kiện đặc biệt theo các dịp lễ tết, mùa trong năm', N'/icons/seasonal.png', 1, 1703001600000),
(N'Workshop & Hội thảo', N'Các buổi workshop, hội thảo về kỹ năng đọc, viết và chia sẻ kiến thức', N'/icons/workshop.png', 1, 1703001600000);
```

## 2. EVENT - Sự kiện chính

```sql
-- Xóa dữ liệu mẫu cũ (nếu có)
DELETE FROM event WHERE id BETWEEN 1 AND 8;

-- Reset identity seed (SQL Server)
DBCC CHECKIDENT ('event', RESEED, 0);

-- Chèn dữ liệu sự kiện (không chỉ định ID, để IDENTITY tự tăng)
INSERT INTO event (event_name, description, event_type, event_category_id, status, start_date, end_date, max_participants, current_participants, 
                   image_url, location, rules, is_online, created_at, updated_at, created_by) VALUES

-- 1. Sự kiện đang diễn ra - Thử thách đọc sách
(N'Thử thách đọc 30 ngày - Tháng 12/2024', 
    N'Thử thách đọc ít nhất 5 cuốn sách trong 30 ngày. Người hoàn thành sẽ nhận được những phần quà hấp dẫn từ BookStation!', 
    'READING_CHALLENGE', 3, 'ONGOING', 
    1701388800000, 1704067200000, 1000, 156, 
    N'/images/events/reading-challenge-30days.jpg', 
    N'Online - Trên ứng dụng BookStation', 
    N'1. Đăng ký tham gia sự kiện trước ngày kết thúc
2. Mua và đọc ít nhất 5 cuốn sách trong thời gian sự kiện
3. Viết review cho mỗi cuốn sách đã đọc
4. Chia sẻ cảm nhận lên mạng xã hội với hashtag #BookStation30Days', 
    1, 1701388800000, 1701388800000, 1),

-- 2. Sự kiện sắp bắt đầu - Gặp gỡ tác giả
(N'Gặp gỡ tác giả Nguyễn Nhật Ánh - "Tôi Thấy Hoa Vàng Trên Cỏ Xanh"', 
    N'Buổi gặp gỡ, ký tặng và tương tác cùng tác giả Nguyễn Nhật Ánh về tác phẩm kinh điển "Tôi Thấy Hoa Vàng Trên Cỏ Xanh"', 
    'AUTHOR_MEET', 2, 'PUBLISHED', 
    1704326400000, 1704340800000, 200, 45, 
    N'/images/events/nguyen-nhat-anh-meetup.jpg', 
    N'BookStation Flagship Store - 123 Nguyễn Du, Q1, TP.HCM', 
    N'1. Đăng ký tham gia trước 24h
2. Mang theo sách để tác giả ký tặng
3. Có thể đặt câu hỏi trong phần Q&A
4. Tuân thủ quy định về giữ trật tự tại sự kiện', 
    0, 1703606400000, 1703606400000, 1),

-- 3. Sự kiện đã hoàn thành - Workshop
(N'Workshop "Kỹ thuật đọc nhanh và ghi nhớ hiệu quả"', 
    N'Workshop hướng dẫn các kỹ thuật đọc nhanh, cách ghi nhớ thông tin hiệu quả và tối ưu hóa thời gian học tập', 
    'WORKSHOP', 6, 'COMPLETED', 
    1700496000000, 1700510400000, 50, 50, 
    N'/images/events/speed-reading-workshop.jpg', 
    N'Trung tâm BookStation Learning - 456 Lê Lợi, Q1, TP.HCM', 
    N'1. Chuẩn bị sẵn notebook và bút
2. Mang theo 1 cuốn sách để thực hành
3. Tham gia đầy đủ 4 tiếng workshop
4. Hoàn thành bài tập thực hành', 
    0, 1700236800000, 1700583000000, 1),

-- 4. Sự kiện khuyến mãi đang diễn ra  
(N'Black Friday Sale 2024 - Giảm giá sốc tất cả sách', 
    N'Sự kiện khuyến mãi Black Friday với mức giảm giá lên đến 70% cho tất cả các đầu sách tại BookStation', 
    'PROMOTION', 5, 'ONGOING', 
    1700755200000, 1701187200000, 5000, 1247, 
    N'/images/events/black-friday-2024.jpg', 
    N'Tất cả cửa hàng BookStation và website', 
    N'1. Áp dụng cho tất cả sách trong kho
2. Có thể kết hợp với voucher cá nhân
3. Miễn phí ship cho đơn hàng từ 200k
4. Số lượng có hạn, nhanh tay kẻo hết!', 
    1, 1700668800000, 1700668800000, 2),

-- 5. Sự kiện hội chợ sắp tới
(N'Hội chợ sách Tết 2025 - "Đón Xuân Tri Thức"', 
    N'Hội chợ sách lớn nhất năm với hàng ngàn đầu sách, các hoạt động văn hóa đặc sắc và nhiều ưu đãi hấp dẫn', 
    'BOOK_FAIR', 4, 'PUBLISHED', 
    1704672000000, 1705276800000, 10000, 234, 
    N'/images/events/tet-book-fair-2025.jpg', 
    N'Công viên Tao Đàn, Q1, TP.HCM', 
    N'1. Vé vào cửa miễn phí
2. Ưu đãi đặc biệt cho khách hàng BookStation
3. Có khu vực dành cho trẻ em
4. Hoạt động diễn ra từ 9h-21h hàng ngày', 
    0, 1703520000000, 1703520000000, 1),

-- 6. Contest đang diễn ra
(N'Cuộc thi "Review sách hay 2024"', 
    N'Cuộc thi viết review sách với giải thưởng tổng trị giá 50 triệu đồng cho những bài review hay nhất', 
    'CONTEST', 3, 'ONGOING', 
    1702857600000, 1705449600000, 2000, 342, 
    N'/images/events/book-review-contest-2024.jpg', 
    N'Online - Trên website và app BookStation', 
    N'1. Viết review tối thiểu 500 từ
2. Kèm ảnh sách thật
3. Hashtag #BookReview2024
4. Một người có thể nộp nhiều bài
5. Ban giám khảo gồm các chuyên gia văn học', 
    1, 1702771200000, 1702771200000, 2),

-- 7. Sự kiện bản nháp
(N'Ra mắt sách "Nghệ thuật sống tối giản"', 
    N'Sự kiện ra mắt cuốn sách mới về lối sống tối giản, kèm tọa đàm với tác giả và chuyên gia tâm lý', 
    'BOOK_LAUNCH', 1, 'DRAFT', 
    1706947200000, 1706961600000, 300, 0, 
    N'/images/events/minimalism-book-launch.jpg', 
    N'BookStation Central - 789 Nguyễn Huệ, Q1, TP.HCM', 
    N'Đang cập nhật chi tiết...', 
    0, 1703865600000, 1703865600000, 1),

-- 8. Sự kiện đã hủy
(N'Workshop "Viết truyện ngắn" - Đã hủy', 
    N'Workshop hướng dẫn kỹ thuật viết truyện ngắn cho người mới bắt đầu', 
    'WORKSHOP', 6, 'CANCELLED', 
    1699891200000, 1699905600000, 30, 15, 
    N'/images/events/short-story-workshop.jpg', 
    N'BookStation Learning Center', 
    N'Sự kiện đã bị hủy do tác giả bận đột xuất', 
    0, 1699545600000, 1699804800000, 2);
```

## 3. EVENT_GIFT - Quà tặng sự kiện

```sql
-- Xóa dữ liệu mẫu cũ (nếu có)
DELETE FROM event_gift WHERE id BETWEEN 1 AND 15;

-- Reset identity seed (SQL Server)
DBCC CHECKIDENT ('event_gift', RESEED, 0);

-- Chèn dữ liệu quà tặng (không chỉ định ID, để IDENTITY tự tăng)
INSERT INTO event_gift (event_id, gift_name, description, gift_value, quantity, remaining_quantity, 
                        image_url, gift_type, book_id, voucher_id, point_value, is_active, created_at) VALUES

-- Quà tặng cho sự kiện "Thử thách đọc 30 ngày" (event_id = 1)
(1, N'Voucher giảm giá 100K', N'Voucher giảm giá 100.000đ cho đơn hàng từ 300K', 100000.00, 50, 32, 
    N'/images/gifts/voucher-100k.png', 'VOUCHER', NULL, 1, NULL, 1, 1701388800000),

(1, N'Bộ bookmark cao cấp', N'Bộ 5 bookmark kim loại cao cấp với thiết kế độc đáo', 150000.00, 30, 18, 
    N'/images/gifts/premium-bookmarks.jpg', 'PHYSICAL_ITEM', NULL, NULL, NULL, 1, 1701388800000),

(1, N'Điểm thưởng 500 điểm', N'Tặng 500 điểm có thể sử dụng để mua sách', 50000.00, 100, 89, 
    N'/images/gifts/loyalty-points.png', 'POINT', NULL, NULL, 500, 1, 1701388800000),

-- Quà tặng cho sự kiện "Gặp gỡ tác giả Nguyễn Nhật Ánh" (event_id = 2)
(2, N'Sách "Tôi Thấy Hoa Vàng Trên Cỏ Xanh" có chữ ký', N'Cuốn sách được tác giả ký tặng trực tiếp', 250000.00, 50, 47, 
    N'/images/gifts/signed-book-hoa-vang.jpg', 'BOOK', 1, NULL, NULL, 1, 1703606400000),

(2, N'Túi tote canvas BookStation', N'Túi tote canvas cao cấp với logo BookStation và chữ ký tác giả', 80000.00, 100, 92, 
    N'/images/gifts/bookstation-tote-bag.jpg', 'PHYSICAL_ITEM', NULL, NULL, NULL, 1, 1703606400000),

-- Quà tặng cho sự kiện "Workshop đọc nhanh" đã hoàn thành (event_id = 3)
(3, N'Chứng chỉ hoàn thành workshop', N'Chứng chỉ tham gia workshop kỹ thuật đọc nhanh', 0.00, 50, 0, 
    N'/images/gifts/workshop-certificate.jpg', 'PHYSICAL_ITEM', NULL, NULL, NULL, 1, 1700496000000),

(3, N'Sách "Speed Reading Mastery"', N'Cuốn sách chuyên sâu về kỹ thuật đọc nhanh', 180000.00, 20, 0, 
    N'/images/gifts/speed-reading-book.jpg', 'BOOK', 2, NULL, NULL, 1, 1700496000000),

-- Quà tặng cho sự kiện "Black Friday Sale" (event_id = 4)
(4, N'Voucher giảm giá 200K', N'Voucher giảm giá 200.000đ cho đơn hàng từ 500K', 200000.00, 500, 287, 
    N'/images/gifts/voucher-200k.png', 'VOUCHER', NULL, 2, NULL, 1, 1700668800000),

(4, N'Combo 3 sách bestseller', N'Combo 3 cuốn sách bán chạy nhất tháng', 450000.00, 100, 67, 
    N'/images/gifts/bestseller-combo.jpg', 'BOOK', 3, NULL, NULL, 1, 1700668800000),

-- Quà tặng cho sự kiện "Hội chợ sách Tết" (event_id = 5)
(5, N'Lì xì BookStation 2025', N'Lì xì đặc biệt chứa voucher và quà tặng bất ngờ', 100000.00, 1000, 956, 
     N'/images/gifts/tet-lucky-money.jpg', 'PHYSICAL_ITEM', NULL, NULL, NULL, 1, 1703520000000),

(5, N'Bộ sách kinh điển Việt Nam', N'Bộ 5 cuốn sách kinh điển văn học Việt Nam', 800000.00, 50, 48, 
     N'/images/gifts/classic-vietnam-books.jpg', 'BOOK', 4, NULL, NULL, 1, 1703520000000),

-- Quà tặng cho cuộc thi "Review sách hay" (event_id = 6)  
(6, N'Giải nhất - Máy đọc sách Kindle', N'Máy đọc sách Kindle Paperwhite thế hệ mới nhất', 3000000.00, 1, 1, 
     N'/images/gifts/kindle-paperwhite.jpg', 'PHYSICAL_ITEM', NULL, NULL, NULL, 1, 1702771200000),

(6, N'Giải nhì - Voucher 500K', N'Voucher mua sách trị giá 500.000đ', 500000.00, 3, 3, 
     N'/images/gifts/voucher-500k.png', 'VOUCHER', NULL, 3, NULL, 1, 1702771200000),

(6, N'Giải ba - Bộ sách tác giả yêu thích', N'Bộ sách của tác giả mà người thắng cuộc yêu thích', 300000.00, 10, 10, 
     N'/images/gifts/favorite-author-books.jpg', 'BOOK', 5, NULL, NULL, 1, 1702771200000),

-- Quà tặng cho sự kiện bản nháp (event_id = 7)
(7, N'Sách "Nghệ thuật sống tối giản" tặng kèm', N'Cuốn sách được ra mắt trong sự kiện', 200000.00, 100, 100, 
     N'/images/gifts/minimalism-book-gift.jpg', 'BOOK', 6, NULL, NULL, 1, 1703865600000);
```

## 4. EVENT_PARTICIPANT - Người tham gia sự kiện

```sql
-- Xóa dữ liệu mẫu cũ (nếu có)
DELETE FROM event_participant WHERE id BETWEEN 1 AND 20;

-- Reset identity seed (SQL Server)
DBCC CHECKIDENT ('event_participant', RESEED, 0);

-- Chèn dữ liệu người tham gia (không chỉ định ID, để IDENTITY tự tăng)
INSERT INTO event_participant (event_id, user_id, joined_at, is_winner, gift_received_id, gift_claimed_at, 
                               completion_status, notes) VALUES

-- Người tham gia "Thử thách đọc 30 ngày" (event_id = 1)
(1, 1, 1701475200000, 1, 1, 1702080000000, 'COMPLETED', N'Đã hoàn thành đọc 7 cuốn sách và viết đầy đủ review'),
(1, 2, 1701561600000, 1, 3, 1702166400000, 'COMPLETED', N'Hoàn thành 5 cuốn sách đúng yêu cầu'),
(1, 3, 1701648000000, 0, NULL, NULL, 'IN_PROGRESS', N'Đang đọc cuốn sách thứ 4'),
(1, 4, 1701734400000, 1, 2, NULL, 'COMPLETED', N'Đã hoàn thành nhưng chưa claim quà'),

-- Người tham gia "Gặp gỡ tác giả Nguyễn Nhật Ánh" (event_id = 2)
(2, 1, 1703692800000, 0, NULL, NULL, 'JOINED', N'Đã đăng ký tham dự buổi gặp mặt'),
(2, 5, 1703779200000, 0, NULL, NULL, 'JOINED', N'Rất mong được gặp tác giả yêu thích'),
(2, 6, 1703865600000, 0, NULL, NULL, 'JOINED', N'Sẽ mang theo 3 cuốn sách để ký tặng'),

-- Người tham gia "Workshop đọc nhanh" đã hoàn thành (event_id = 3)
(3, 2, 1700409600000, 1, 6, 1700510400000, 'COMPLETED', N'Tham gia đầy đủ workshop và nhận chứng chỉ'),
(3, 7, 1700409600000, 1, 7, 1700596800000, 'COMPLETED', N'Học viên xuất sắc, được tặng thêm sách'),
(3, 8, 1700409600000, 0, NULL, NULL, 'COMPLETED', N'Hoàn thành workshop nhưng không nhận quà'),

-- Người tham gia "Black Friday Sale" (event_id = 4)
(4, 1, 1700841600000, 1, 8, 1700928000000, 'COMPLETED', N'Mua đơn hàng 1.2M, nhận voucher 200K'),
(4, 3, 1700928000000, 1, 9, NULL, 'COMPLETED', N'Trúng combo sách bestseller, chưa claim'),
(4, 9, 1701014400000, 0, NULL, NULL, 'IN_PROGRESS', N'Đang tham gia sự kiện'),

-- Người tham gia "Hội chợ sách Tết" (event_id = 5)  
(5, 2, 1703606400000, 0, NULL, NULL, 'JOINED', N'Đăng ký sớm để được ưu đãi đặc biệt'),
(5, 4, 1703692800000, 0, NULL, NULL, 'JOINED', N'Hào hứng với hội chợ sách lần này'),

-- Người tham gia "Cuộc thi Review sách hay" (event_id = 6)
(6, 5, 1702944000000, 0, NULL, NULL, 'IN_PROGRESS', N'Đã nộp 2 bài review, chuẩn bị nộp thêm'),
(6, 6, 1703030400000, 0, NULL, NULL, 'IN_PROGRESS', N'Bài review đầu tiên được đánh giá cao'),
(6, 10, 1703116800000, 0, NULL, NULL, 'JOINED', N'Mới tham gia, đang viết bài review đầu tiên'),

-- Người tham gia sự kiện đã hủy (event_id = 8)
(8, 7, 1699632000000, 0, NULL, NULL, 'FAILED', N'Sự kiện bị hủy, không thể hoàn thành'),
(8, 8, 1699718400000, 0, NULL, NULL, 'FAILED', N'Đã đăng ký nhưng sự kiện bị hủy');
```

## 5. EVENT_GIFT_CLAIM - Yêu cầu nhận quà

```sql
-- Xóa dữ liệu mẫu cũ (nếu có)
DELETE FROM event_gift_claim WHERE id BETWEEN 1 AND 12;

-- Reset identity seed (SQL Server)
DBCC CHECKIDENT ('event_gift_claim', RESEED, 0);

-- Chèn dữ liệu yêu cầu nhận quà (không chỉ định ID, để IDENTITY tự tăng)
INSERT INTO event_gift_claim (event_participant_id, event_gift_id, claimed_at, claim_status, delivery_method, 
                              delivery_order_id, store_pickup_code, pickup_store_id, staff_confirmed_by, 
                              auto_delivered, completed_at, notes) VALUES

-- Claim từ "Thử thách đọc 30 ngày"
(1, 1, 1702080000000, 'DELIVERED', 'DIGITAL_DELIVERY', NULL, NULL, NULL, NULL, 1, 1702080000000, 
   N'Voucher đã được tự động thêm vào tài khoản'),

(2, 3, 1702166400000, 'DELIVERED', 'DIGITAL_DELIVERY', NULL, NULL, NULL, NULL, 1, 1702166400000, 
   N'Điểm thưởng đã được cộng vào tài khoản'),

(4, 2, 1702252800000, 'APPROVED', 'ONLINE_SHIPPING', NULL, NULL, NULL, NULL, 0, NULL, 
   N'Đã duyệt, chuẩn bị tạo đơn giao hàng cho bộ bookmark'),

-- Claim từ "Workshop đọc nhanh" 
(8, 6, 1700510400000, 'DELIVERED', 'DIRECT_HANDOVER', NULL, NULL, NULL, 1, 0, 1700510400000, 
   N'Chứng chỉ đã được trao trực tiếp tại workshop'),

(9, 7, 1700596800000, 'DELIVERED', 'STORE_PICKUP', NULL, N'PICKUP001', 1, 2, 0, 1700683200000, 
   N'Đã nhận sách tại cửa hàng BookStation Q1'),

-- Claim từ "Black Friday Sale"
(11, 8, 1700928000000, 'DELIVERED', 'DIGITAL_DELIVERY', NULL, NULL, NULL, NULL, 1, 1700928000000, 
   N'Voucher Black Friday đã được kích hoạt'),

(12, 9, 1701187200000, 'PENDING', 'ONLINE_SHIPPING', NULL, NULL, NULL, NULL, 0, NULL, 
   N'Chờ xử lý đơn giao combo sách bestseller'),

-- Claim cho các sự kiện khác
(5, 4, 1703952000000, 'PENDING', 'STORE_PICKUP', NULL, N'PICKUP002', 1, NULL, 0, NULL, 
   N'Chờ nhận sách có chữ ký tại cửa hàng'),

(16, 13, 1703203200000, 'REJECTED', 'DIGITAL_DELIVERY', NULL, NULL, NULL, NULL, 0, NULL, 
   N'Bài review chưa đạt yêu cầu tối thiểu để nhận giải'),

(6, 5, 1703894400000, 'APPROVED', 'STORE_PICKUP', NULL, N'PICKUP003', 1, NULL, 0, NULL, 
    N'Túi tote đã sẵn sàng để nhận tại cửa hàng'),

-- Claim hết hạn
(17, 14, 1703289600000, 'EXPIRED', 'ONLINE_SHIPPING', NULL, NULL, NULL, NULL, 0, NULL, 
    N'Quá thời hạn claim quà tặng (7 ngày sau khi thắng)'),

-- Claim thành công với đơn hàng
(13, 8, 1701100800000, 'ORDER_CREATED', 'ONLINE_SHIPPING', 1001, NULL, NULL, NULL, 0, NULL, 
    N'Đã tạo đơn hàng #1001 để giao voucher qua đường bưu điện');
```

## 6. EVENT_HISTORY - Lịch sử sự kiện

```sql
-- Xóa dữ liệu mẫu cũ (nếu có)  
DELETE FROM event_history WHERE id BETWEEN 1 AND 25;

-- Reset identity seed (SQL Server)
DBCC CHECKIDENT ('event_history', RESEED, 0);

-- Chèn dữ liệu lịch sử sự kiện (không chỉ định ID, để IDENTITY tự tăng)
INSERT INTO event_history (event_id, action_type, description, performed_by, created_at, old_values, new_values) VALUES

-- Lịch sử cho "Thử thách đọc 30 ngày" (event_id = 1)
(1, 'CREATED', N'Tạo sự kiện "Thử thách đọc 30 ngày - Tháng 12/2024"', 1, 1701388800000, 
   NULL, N'{"event_name":"Thử thách đọc 30 ngày - Tháng 12/2024","status":"DRAFT","max_participants":1000}'),

(1, 'UPDATED', N'Cập nhật mô tả và quy định sự kiện', 1, 1701402000000,
   N'{"description":"Mô tả cũ..."}', N'{"description":"Thử thách đọc ít nhất 5 cuốn sách trong 30 ngày..."}'),

(1, 'PUBLISHED', N'Công bố sự kiện ra công chúng', 1, 1701415200000,
   N'{"status":"DRAFT"}', N'{"status":"PUBLISHED"}'),

(1, 'STARTED', N'Sự kiện chính thức bắt đầu', 1, 1701475200000,
   N'{"status":"PUBLISHED"}', N'{"status":"ONGOING","current_participants":0}'),

-- Lịch sử cho "Gặp gỡ tác giả Nguyễn Nhật Ánh" (event_id = 2)
(2, 'CREATED', N'Tạo sự kiện gặp gỡ tác giả Nguyễn Nhật Ánh', 1, 1703606400000,
   NULL, N'{"event_name":"Gặp gỡ tác giả Nguyễn Nhật Ánh","status":"DRAFT","max_participants":200}'),

(2, 'UPDATED', N'Cập nhật địa điểm tổ chức sự kiện', 1, 1703620800000,
   N'{"location":"Chưa xác định"}', N'{"location":"BookStation Flagship Store - 123 Nguyễn Du, Q1, TP.HCM"}'),

(2, 'PUBLISHED', N'Công bố sự kiện gặp gỡ tác giả', 1, 1703635200000,
   N'{"status":"DRAFT"}', N'{"status":"PUBLISHED"}'),

-- Lịch sử cho "Workshop đọc nhanh" (event_id = 3)
(3, 'CREATED', N'Tạo workshop kỹ thuật đọc nhanh', 1, 1700236800000,
   NULL, N'{"event_name":"Workshop Kỹ thuật đọc nhanh","status":"DRAFT"}'),

(3, 'PUBLISHED', N'Công bố workshop', 1, 1700323200000,
   N'{"status":"DRAFT"}', N'{"status":"PUBLISHED"}'),

(3, 'STARTED', N'Workshop bắt đầu', 1, 1700496000000,
    N'{"status":"PUBLISHED"}', N'{"status":"ONGOING","current_participants":50}'),

(3, 'COMPLETED', N'Workshop kết thúc thành công', 1, 1700510400000,
    N'{"status":"ONGOING"}', N'{"status":"COMPLETED","current_participants":50}'),

-- Lịch sử cho "Black Friday Sale" (event_id = 4)
(4, 'CREATED', N'Tạo sự kiện Black Friday Sale 2024', 2, 1700668800000,
    NULL, N'{"event_name":"Black Friday Sale 2024","status":"DRAFT","max_participants":5000}'),

(4, 'PUBLISHED', N'Công bố chương trình Black Friday', 2, 1700682000000,
    N'{"status":"DRAFT"}', N'{"status":"PUBLISHED"}'),

(4, 'STARTED', N'Black Friday Sale chính thức bắt đầu', 2, 1700755200000,
    N'{"status":"PUBLISHED"}', N'{"status":"ONGOING","current_participants":0}'),

-- Lịch sử cho "Hội chợ sách Tết" (event_id = 5)
(5, 'CREATED', N'Tạo sự kiện Hội chợ sách Tết 2025', 1, 1703520000000,
    NULL, N'{"event_name":"Hội chợ sách Tết 2025","status":"DRAFT","max_participants":10000}'),

(5, 'UPDATED', N'Cập nhật thông tin venue và logistics', 1, 1703534400000,
    N'{"location":"Chưa xác định"}', N'{"location":"Công viên Tao Đàn, Q1, TP.HCM"}'),

(5, 'PUBLISHED', N'Công bố Hội chợ sách Tết', 1, 1703548800000,
    N'{"status":"DRAFT"}', N'{"status":"PUBLISHED"}'),

-- Lịch sử cho "Cuộc thi Review sách hay" (event_id = 6)
(6, 'CREATED', N'Tạo cuộc thi Review sách hay 2024', 2, 1702771200000,
    NULL, N'{"event_name":"Cuộc thi Review sách hay 2024","status":"DRAFT","max_participants":2000}'),

(6, 'UPDATED', N'Cập nhật tiêu chí đánh giá và giải thưởng', 2, 1702785600000,
    N'{"rules":"Quy định cũ..."}', N'{"rules":"1. Viết review tối thiểu 500 từ..."}'),

(6, 'PUBLISHED', N'Công bố cuộc thi review sách', 2, 1702800000000,
    N'{"status":"DRAFT"}', N'{"status":"PUBLISHED"}'),

(6, 'STARTED', N'Cuộc thi chính thức bắt đầu', 2, 1702857600000,
    N'{"status":"PUBLISHED"}', N'{"status":"ONGOING","current_participants":0}'),

-- Lịch sử cho sự kiện bản nháp (event_id = 7)
(7, 'CREATED', N'Tạo sự kiện ra mắt sách "Nghệ thuật sống tối giản"', 1, 1703865600000,
    NULL, N'{"event_name":"Ra mắt sách Nghệ thuật sống tối giản","status":"DRAFT"}'),

-- Lịch sử cho sự kiện đã hủy (event_id = 8)
(8, 'CREATED', N'Tạo workshop "Viết truyện ngắn"', 2, 1699545600000,
    NULL, N'{"event_name":"Workshop Viết truyện ngắn","status":"DRAFT","max_participants":30}'),

(8, 'PUBLISHED', N'Công bố workshop viết truyện ngắn', 2, 1699632000000,
    N'{"status":"DRAFT"}', N'{"status":"PUBLISHED"}'),

(8, 'CANCELLED', N'Hủy workshop do tác giả bận đột xuất', 2, 1699804800000,
    N'{"status":"PUBLISHED"}', N'{"status":"CANCELLED","current_participants":15}');
```

## Hướng dẫn sử dụng

### 1. Chạy script theo thứ tự
```sql
-- Chạy lần lượt từ EVENT_CATEGORY -> EVENT -> EVENT_GIFT -> EVENT_PARTICIPANT -> EVENT_GIFT_CLAIM -> EVENT_HISTORY
-- Sử dụng SQL Server Management Studio (SSMS) hoặc Azure Data Studio
```

### 2. Điều chỉnh dữ liệu
- Thay đổi `user_id` trong các bảng EVENT_PARTICIPANT theo user thực tế trong hệ thống của bạn
- Điều chỉnh `book_id`, `voucher_id` trong EVENT_GIFT theo dữ liệu book/voucher có sẵn
- Cập nhật `created_by` trong EVENT theo admin user ID của bạn

### 3. Timestamp
- Tất cả timestamp sử dụng định dạng Unix timestamp * 1000 (miliseconds)
- Có thể dùng tool online để convert: https://www.epochconverter.com/

### 4. SQL Server specifics
- Sử dụng `DBCC CHECKIDENT` để reset identity seed thay vì `AUTO_INCREMENT`
- Tất cả chuỗi Unicode đều có tiền tố `N`
- Không chỉ định ID trong INSERT, để IDENTITY tự động tạo

### 4. Test scenarios được cover
- ✅ Sự kiện đang diễn ra (ONGOING)
- ✅ Sự kiện sắp bắt đầu (PUBLISHED) 
- ✅ Sự kiện đã hoàn thành (COMPLETED)
- ✅ Sự kiện bản nháp (DRAFT)
- ✅ Sự kiện đã hủy (CANCELLED)
- ✅ Các loại quà tặng: BOOK, VOUCHER, POINT, PHYSICAL_ITEM
- ✅ Các phương thức giao quà: ONLINE_SHIPPING, STORE_PICKUP, DIGITAL_DELIVERY, DIRECT_HANDOVER
- ✅ Các trạng thái claim: PENDING, APPROVED, ORDER_CREATED, DELIVERED, REJECTED, EXPIRED
- ✅ Lịch sử đầy đủ các hành động: CREATED, UPDATED, PUBLISHED, STARTED, COMPLETED, CANCELLED

### 5. Queries hữu ích để test
```sql
-- Xem tất cả sự kiện đang diễn ra
SELECT * FROM event WHERE status = 'ONGOING';

-- Xem người thắng cuộc chưa claim quà
SELECT ep.*, eg.gift_name 
FROM event_participant ep 
JOIN event_gift eg ON ep.gift_received_id = eg.id 
WHERE ep.is_winner = 1 AND ep.gift_claimed_at IS NULL;

-- Xem lịch sử của một sự kiện
SELECT * FROM event_history WHERE event_id = 1 ORDER BY created_at;

-- Thống kê số lượng người tham gia theo sự kiện
SELECT e.event_name, COUNT(ep.id) as participant_count
FROM event e 
LEFT JOIN event_participant ep ON e.id = ep.event_id 
GROUP BY e.id, e.event_name;
```

---
**Chúc bạn test thành công hệ thống Event! 🎉📚**
