-- =====================================================
-- EVENT SYSTEM - SAMPLE DATA INSERT SCRIPTS (SAFE VERSION)
-- Dành cho SQL Server - An toàn với Foreign Key
-- =====================================================

USE [BookStation];
GO

-- Tắt foreign key checks tạm thời để insert dữ liệu
EXEC sp_MSforeachtable "ALTER TABLE ? NOCHECK CONSTRAINT all"
GO

-- =====================================================
-- 1. EVENT_CATEGORY - Danh mục sự kiện
-- =====================================================

PRINT '=== INSERTING EVENT_CATEGORY ===';

-- Xóa dữ liệu cũ nếu có
DELETE FROM event_history;
DELETE FROM event_gift_claim;
DELETE FROM event_participant;
DELETE FROM event_gift;
DELETE FROM event;
DELETE FROM event_category;

-- Reset identity
DBCC CHECKIDENT ('event_category', RESEED, 0);
DBCC CHECKIDENT ('event', RESEED, 0);
DBCC CHECKIDENT ('event_gift', RESEED, 0);
DBCC CHECKIDENT ('event_participant', RESEED, 0);
DBCC CHECKIDENT ('event_gift_claim', RESEED, 0);
DBCC CHECKIDENT ('event_history', RESEED, 0);

-- Insert categories
INSERT INTO event_category (category_name, description, icon_url, is_active, created_at) VALUES
(N'Ra mắt sách mới', N'Các sự kiện giới thiệu và ra mắt những cuốn sách mới nhất', N'/icons/book-launch.png', 1, 1703001600000),
(N'Gặp gỡ tác giả', N'Sự kiện gặp gỡ, ký tặng và tương tác trực tiếp với các tác giả nổi tiếng', N'/icons/author-meet.png', 1, 1703001600000),
(N'Thử thách đọc sách', N'Các cuộc thi và thử thách khuyến khích việc đọc sách', N'/icons/reading-challenge.png', 1, 1703001600000),
(N'Hội chợ sách', N'Triển lãm và hội chợ sách với nhiều ưu đãi đặc biệt', N'/icons/book-fair.png', 1, 1703001600000),
(N'Sự kiện theo mùa', N'Sự kiện đặc biệt theo các dịp lễ tết, mùa trong năm', N'/icons/seasonal.png', 1, 1703001600000),
(N'Workshop & Hội thảo', N'Các buổi workshop, hội thảo về kỹ năng đọc, viết và chia sẻ kiến thức', N'/icons/workshop.png', 1, 1703001600000);

PRINT 'EVENT_CATEGORY inserted: ' + CAST(@@ROWCOUNT AS VARCHAR(10)) + ' rows';

-- =====================================================
-- 2. EVENT - Sự kiện chính
-- =====================================================

PRINT '=== INSERTING EVENT ===';

-- Lấy ID của categories đã insert
DECLARE @cat_book_launch INT = (SELECT id FROM event_category WHERE category_name = N'Ra mắt sách mới');
DECLARE @cat_author_meet INT = (SELECT id FROM event_category WHERE category_name = N'Gặp gỡ tác giả');
DECLARE @cat_reading_challenge INT = (SELECT id FROM event_category WHERE category_name = N'Thử thách đọc sách');
DECLARE @cat_book_fair INT = (SELECT id FROM event_category WHERE category_name = N'Hội chợ sách');
DECLARE @cat_seasonal INT = (SELECT id FROM event_category WHERE category_name = N'Sự kiện theo mùa');
DECLARE @cat_workshop INT = (SELECT id FROM event_category WHERE category_name = N'Workshop & Hội thảo');

-- Insert events với category IDs động
INSERT INTO event (event_name, description, event_type, event_category_id, status, start_date, end_date, max_participants, current_participants, 
                   image_url, location, rules, is_online, created_at, updated_at, created_by) VALUES

-- 1. Sự kiện đang diễn ra - Thử thách đọc sách
(N'Thử thách đọc 30 ngày - Tháng 12/2024', 
    N'Thử thách đọc ít nhất 5 cuốn sách trong 30 ngày. Người hoàn thành sẽ nhận được những phần quà hấp dẫn từ BookStation!', 
    'READING_CHALLENGE', @cat_reading_challenge, 'ONGOING', 
    1701388800000, 1704067200000, 1000, 156, 
    N'/images/events/reading-challenge-30days.jpg', 
    N'Online - Trên ứng dụng BookStation', 
    N'1. Đăng ký tham gia sự kiện trước ngày kết thúc' + CHAR(13) + CHAR(10) +
    N'2. Mua và đọc ít nhất 5 cuốn sách trong thời gian sự kiện' + CHAR(13) + CHAR(10) +
    N'3. Viết review cho mỗi cuốn sách đã đọc' + CHAR(13) + CHAR(10) +
    N'4. Chia sẻ cảm nhận lên mạng xã hội với hashtag #BookStation30Days', 
    1, 1701388800000, 1701388800000, 1),

-- 2. Sự kiện sắp bắt đầu - Gặp gỡ tác giả
(N'Gặp gỡ tác giả Nguyễn Nhật Ánh - "Tôi Thấy Hoa Vàng Trên Cỏ Xanh"', 
    N'Buổi gặp gỡ, ký tặng và tương tác cùng tác giả Nguyễn Nhật Ánh về tác phẩm kinh điển "Tôi Thấy Hoa Vàng Trên Cỏ Xanh"', 
    'AUTHOR_MEET', @cat_author_meet, 'PUBLISHED', 
    1704326400000, 1704340800000, 200, 45, 
    N'/images/events/nguyen-nhat-anh-meetup.jpg', 
    N'BookStation Flagship Store - 123 Nguyễn Du, Q1, TP.HCM', 
    N'1. Đăng ký tham gia trước 24h' + CHAR(13) + CHAR(10) +
    N'2. Mang theo sách để tác giả ký tặng' + CHAR(13) + CHAR(10) +
    N'3. Có thể đặt câu hỏi trong phần Q&A' + CHAR(13) + CHAR(10) +
    N'4. Tuân thủ quy định về giữ trật tự tại sự kiện', 
    0, 1703606400000, 1703606400000, 1),

-- 3. Sự kiện đã hoàn thành - Workshop
(N'Workshop "Kỹ thuật đọc nhanh và ghi nhớ hiệu quả"', 
    N'Workshop hướng dẫn các kỹ thuật đọc nhanh, cách ghi nhớ thông tin hiệu quả và tối ưu hóa thời gian học tập', 
    'WORKSHOP', @cat_workshop, 'COMPLETED', 
    1700496000000, 1700510400000, 50, 50, 
    N'/images/events/speed-reading-workshop.jpg', 
    N'Trung tâm BookStation Learning - 456 Lê Lợi, Q1, TP.HCM', 
    N'1. Chuẩn bị sẵn notebook và bút' + CHAR(13) + CHAR(10) +
    N'2. Mang theo 1 cuốn sách để thực hành' + CHAR(13) + CHAR(10) +
    N'3. Tham gia đầy đủ 4 tiếng workshop' + CHAR(13) + CHAR(10) +
    N'4. Hoàn thành bài tập thực hành', 
    0, 1700236800000, 1700583000000, 1),

-- 4. Sự kiện khuyến mãi đang diễn ra  
(N'Black Friday Sale 2024 - Giảm giá sốc tất cả sách', 
    N'Sự kiện khuyến mãi Black Friday với mức giảm giá lên đến 70% cho tất cả các đầu sách tại BookStation', 
    'PROMOTION', @cat_seasonal, 'ONGOING', 
    1700755200000, 1701187200000, 5000, 1247, 
    N'/images/events/black-friday-2024.jpg', 
    N'Tất cả cửa hàng BookStation và website', 
    N'1. Áp dụng cho tất cả sách trong kho' + CHAR(13) + CHAR(10) +
    N'2. Có thể kết hợp với voucher cá nhân' + CHAR(13) + CHAR(10) +
    N'3. Miễn phí ship cho đơn hàng từ 200k' + CHAR(13) + CHAR(10) +
    N'4. Số lượng có hạn, nhanh tay kẻo hết!', 
    1, 1700668800000, 1700668800000, 1),

-- 5. Sự kiện hội chợ sắp tới
(N'Hội chợ sách Tết 2025 - "Đón Xuân Tri Thức"', 
    N'Hội chợ sách lớn nhất năm với hàng ngàn đầu sách, các hoạt động văn hóa đặc sắc và nhiều ưu đãi hấp dẫn', 
    'BOOK_FAIR', @cat_book_fair, 'PUBLISHED', 
    1704672000000, 1705276800000, 10000, 234, 
    N'/images/events/tet-book-fair-2025.jpg', 
    N'Công viên Tao Đàn, Q1, TP.HCM', 
    N'1. Vé vào cửa miễn phí' + CHAR(13) + CHAR(10) +
    N'2. Ưu đãi đặc biệt cho khách hàng BookStation' + CHAR(13) + CHAR(10) +
    N'3. Có khu vực dành cho trẻ em' + CHAR(13) + CHAR(10) +
    N'4. Hoạt động diễn ra từ 9h-21h hàng ngày', 
    0, 1703520000000, 1703520000000, 1),

-- 6. Contest đang diễn ra
(N'Cuộc thi "Review sách hay 2024"', 
    N'Cuộc thi viết review sách với giải thưởng tổng trị giá 50 triệu đồng cho những bài review hay nhất', 
    'CONTEST', @cat_reading_challenge, 'ONGOING', 
    1702857600000, 1705449600000, 2000, 342, 
    N'/images/events/book-review-contest-2024.jpg', 
    N'Online - Trên website và app BookStation', 
    N'1. Viết review tối thiểu 500 từ' + CHAR(13) + CHAR(10) +
    N'2. Kèm ảnh sách thật' + CHAR(13) + CHAR(10) +
    N'3. Hashtag #BookReview2024' + CHAR(13) + CHAR(10) +
    N'4. Một người có thể nộp nhiều bài' + CHAR(13) + CHAR(10) +
    N'5. Ban giám khảo gồm các chuyên gia văn học', 
    1, 1702771200000, 1702771200000, 1),

-- 7. Sự kiện bản nháp
(N'Ra mắt sách "Nghệ thuật sống tối giản"', 
    N'Sự kiện ra mắt cuốn sách mới về lối sống tối giản, kèm tọa đàm với tác giả và chuyên gia tâm lý', 
    'BOOK_LAUNCH', @cat_book_launch, 'DRAFT', 
    1706947200000, 1706961600000, 300, 0, 
    N'/images/events/minimalism-book-launch.jpg', 
    N'BookStation Central - 789 Nguyễn Huệ, Q1, TP.HCM', 
    N'Đang cập nhật chi tiết...', 
    0, 1703865600000, 1703865600000, 1),

-- 8. Sự kiện đã hủy
(N'Workshop "Viết truyện ngắn" - Đã hủy', 
    N'Workshop hướng dẫn kỹ thuật viết truyện ngắn cho người mới bắt đầu', 
    'WORKSHOP', @cat_workshop, 'CANCELLED', 
    1699891200000, 1699905600000, 30, 15, 
    N'/images/events/short-story-workshop.jpg', 
    N'BookStation Learning Center', 
    N'Sự kiện đã bị hủy do tác giả bận đột xuất', 
    0, 1699545600000, 1699804800000, 1);

PRINT 'EVENT inserted: ' + CAST(@@ROWCOUNT AS VARCHAR(10)) + ' rows';

-- Lấy IDs của các events đã insert
DECLARE @event8 INT = (SELECT id FROM event WHERE event_name LIKE N'%Workshop "Viết truyện ngắn"%');

-- =====================================================
-- 3. EVENT_GIFT - Quà tặng sự kiện
-- =====================================================

PRINT '=== INSERTING EVENT_GIFT ===';

-- Lấy event IDs
DECLARE @event1 INT = (SELECT id FROM event WHERE event_name LIKE N'%Thử thách đọc 30 ngày%');
DECLARE @event2 INT = (SELECT id FROM event WHERE event_name LIKE N'%Nguyễn Nhật Ánh%');
DECLARE @event3 INT = (SELECT id FROM event WHERE event_name LIKE N'%Kỹ thuật đọc nhanh%');
DECLARE @event4 INT = (SELECT id FROM event WHERE event_name LIKE N'%Black Friday%');
DECLARE @event5 INT = (SELECT id FROM event WHERE event_name LIKE N'%Hội chợ sách Tết%');
DECLARE @event6 INT = (SELECT id FROM event WHERE event_name LIKE N'%Review sách hay%');
DECLARE @event7 INT = (SELECT id FROM event WHERE event_name LIKE N'%Nghệ thuật sống tối giản%');

-- Insert gifts với event IDs động
INSERT INTO event_gift (event_id, gift_name, description, gift_value, quantity, remaining_quantity, 
                        image_url, gift_type, book_id, voucher_id, point_value, is_active, created_at) VALUES

-- Quà tặng cho sự kiện "Thử thách đọc 30 ngày"
(@event1, N'Voucher giảm giá 100K', N'Voucher giảm giá 100.000đ cho đơn hàng từ 300K', 100000.00, 50, 32, 
    N'/images/gifts/voucher-100k.png', N'VOUCHER', NULL, NULL, NULL, 1, 1701388800000),

(@event1, N'Bộ bookmark cao cấp', N'Bộ 5 bookmark kim loại cao cấp với thiết kế độc đáo', 150000.00, 30, 18, 
    N'/images/gifts/premium-bookmarks.jpg', N'PHYSICAL_ITEM', NULL, NULL, NULL, 1, 1701388800000),

(@event1, N'Điểm thưởng 500 điểm', N'Tặng 500 điểm có thể sử dụng để mua sách', 50000.00, 100, 89, 
    N'/images/gifts/loyalty-points.png', N'POINT', NULL, NULL, 500, 1, 1701388800000),

-- Quà tặng cho sự kiện "Gặp gỡ tác giả Nguyễn Nhật Ánh"
(@event2, N'Sách "Tôi Thấy Hoa Vàng Trên Cỏ Xanh" có chữ ký', N'Cuốn sách được tác giả ký tặng trực tiếp', 250000.00, 50, 47, 
    N'/images/gifts/signed-book-hoa-vang.jpg', N'BOOK', NULL, NULL, NULL, 1, 1703606400000),

(@event2, N'Túi tote canvas BookStation', N'Túi tote canvas cao cấp với logo BookStation và chữ ký tác giả', 80000.00, 100, 92, 
    N'/images/gifts/bookstation-tote-bag.jpg', N'PHYSICAL_ITEM', NULL, NULL, NULL, 1, 1703606400000),

-- Quà tặng cho sự kiện "Workshop đọc nhanh" đã hoàn thành
(@event3, N'Chứng chỉ hoàn thành workshop', N'Chứng chỉ tham gia workshop kỹ thuật đọc nhanh', 0.00, 50, 0, 
    N'/images/gifts/workshop-certificate.jpg', N'PHYSICAL_ITEM', NULL, NULL, NULL, 1, 1700496000000),

(@event3, N'Sách "Speed Reading Mastery"', N'Cuốn sách chuyên sâu về kỹ thuật đọc nhanh', 180000.00, 20, 0, 
    N'/images/gifts/speed-reading-book.jpg', N'BOOK', NULL, NULL, NULL, 1, 1700496000000),

-- Quà tặng cho sự kiện "Black Friday Sale"
(@event4, N'Voucher giảm giá 200K', N'Voucher giảm giá 200.000đ cho đơn hàng từ 500K', 200000.00, 500, 287, 
    N'/images/gifts/voucher-200k.png', N'VOUCHER', NULL, NULL, NULL, 1, 1700668800000),

(@event4, N'Combo 3 sách bestseller', N'Combo 3 cuốn sách bán chạy nhất tháng', 450000.00, 100, 67, 
    N'/images/gifts/bestseller-combo.jpg', N'BOOK', NULL, NULL, NULL, 1, 1700668800000),

-- Quà tặng cho sự kiện "Hội chợ sách Tết"
(@event5, N'Lì xì BookStation 2025', N'Lì xì đặc biệt chứa voucher và quà tặng bất ngờ', 100000.00, 1000, 956, 
     N'/images/gifts/tet-lucky-money.jpg', N'PHYSICAL_ITEM', NULL, NULL, NULL, 1, 1703520000000),

(@event5, N'Bộ sách kinh điển Việt Nam', N'Bộ 5 cuốn sách kinh điển văn học Việt Nam', 800000.00, 50, 48, 
     N'/images/gifts/classic-vietnam-books.jpg', N'BOOK', NULL, NULL, NULL, 1, 1703520000000),

-- Quà tặng cho cuộc thi "Review sách hay"
(@event6, N'Giải nhất - Máy đọc sách Kindle', N'Máy đọc sách Kindle Paperwhite thế hệ mới nhất', 3000000.00, 1, 1, 
     N'/images/gifts/kindle-paperwhite.jpg', N'PHYSICAL_ITEM', NULL, NULL, NULL, 1, 1702771200000),

(@event6, N'Giải nhì - Voucher 500K', N'Voucher mua sách trị giá 500.000đ', 500000.00, 3, 3, 
     N'/images/gifts/voucher-500k.png', N'VOUCHER', NULL, NULL, NULL, 1, 1702771200000),

(@event6, N'Giải ba - Bộ sách tác giả yêu thích', N'Bộ sách của tác giả mà người thắng cuộc yêu thích', 300000.00, 10, 10, 
     N'/images/gifts/favorite-author-books.jpg', N'BOOK', NULL, NULL, NULL, 1, 1702771200000),

-- Quà tặng cho sự kiện bản nháp
(@event7, N'Sách "Nghệ thuật sống tối giản" tặng kèm', N'Cuốn sách được ra mắt trong sự kiện', 200000.00, 100, 100, 
     N'/images/gifts/minimalism-book-gift.jpg', N'BOOK', NULL, NULL, NULL, 1, 1703865600000);

PRINT 'EVENT_GIFT inserted: ' + CAST(@@ROWCOUNT AS VARCHAR(10)) + ' rows';

-- =====================================================
-- 4. EVENT_PARTICIPANT - Người tham gia sự kiện
-- =====================================================

PRINT '=== INSERTING EVENT_PARTICIPANT ===';

-- Insert participants (giả sử có user với ID 1-10 trong hệ thống)
INSERT INTO event_participant (event_id, user_id, joined_at, is_winner, completion_status, notes) VALUES

-- Người tham gia "Thử thách đọc 30 ngày"
(@event1, 1, 1701475200000, 1, 'COMPLETED', N'Đã hoàn thành đọc 7 cuốn sách và viết đầy đủ review'),
(@event1, 2, 1701561600000, 1, 'COMPLETED', N'Hoàn thành 5 cuốn sách đúng yêu cầu'),
(@event1, 3, 1701648000000, 0, 'IN_PROGRESS', N'Đang đọc cuốn sách thứ 4'),
(@event1, 4, 1701734400000, 1, 'COMPLETED', N'Đã hoàn thành và sẽ claim quà qua hệ thống EventGiftClaim'),

-- Người tham gia các sự kiện khác
(@event2, 1, 1703692800000, 0, 'JOINED', N'Đã đăng ký tham dự buổi gặp mặt'),
(@event2, 5, 1703779200000, 0, 'JOINED', N'Rất mong được gặp tác giả yêu thích'),
(@event3, 2, 1700409600000, 1, 'COMPLETED', N'Tham gia đầy đủ workshop và nhận chứng chỉ'),
(@event4, 1, 1700841600000, 1, 'COMPLETED', N'Mua đơn hàng 1.2M, nhận voucher 200K'),
(@event5, 2, 1703606400000, 0, 'JOINED', N'Đăng ký sớm để được ưu đãi đặc biệt'),
(@event6, 5, 1702944000000, 0, 'IN_PROGRESS', N'Đã nộp 2 bài review, chuẩn bị nộp thêm');

PRINT 'EVENT_PARTICIPANT inserted: ' + CAST(@@ROWCOUNT AS VARCHAR(10)) + ' rows';

-- =====================================================
-- 5. EVENT_GIFT_CLAIM - Yêu cầu nhận quà
-- =====================================================

PRINT '=== INSERTING EVENT_GIFT_CLAIM ===';

-- Lấy IDs cần thiết cho event_gift_claim
DECLARE @participant1_event1 INT = (SELECT id FROM event_participant WHERE event_id = @event1 AND user_id = 1);
DECLARE @participant2_event1 INT = (SELECT id FROM event_participant WHERE event_id = @event1 AND user_id = 2);
DECLARE @participant4_event1 INT = (SELECT id FROM event_participant WHERE event_id = @event1 AND user_id = 4);
DECLARE @participant2_event3 INT = (SELECT id FROM event_participant WHERE event_id = @event3 AND user_id = 2);
DECLARE @participant1_event4 INT = (SELECT id FROM event_participant WHERE event_id = @event4 AND user_id = 1);

DECLARE @gift1_voucher100k INT = (SELECT id FROM event_gift WHERE event_id = @event1 AND gift_name LIKE N'%Voucher giảm giá 100K%');
DECLARE @gift2_bookmark INT = (SELECT id FROM event_gift WHERE event_id = @event1 AND gift_name LIKE N'%Bộ bookmark cao cấp%');
DECLARE @gift3_points INT = (SELECT id FROM event_gift WHERE event_id = @event1 AND gift_name LIKE N'%Điểm thưởng 500 điểm%');
DECLARE @gift7_certificate INT = (SELECT id FROM event_gift WHERE event_id = @event3 AND gift_name LIKE N'%Chứng chỉ hoàn thành%');
DECLARE @gift8_speedbook INT = (SELECT id FROM event_gift WHERE event_id = @event3 AND gift_name LIKE N'%Speed Reading Mastery%');
DECLARE @gift9_voucher200k INT = (SELECT id FROM event_gift WHERE event_id = @event4 AND gift_name LIKE N'%Voucher giảm giá 200K%');

INSERT INTO event_gift_claim (event_participant_id, event_gift_id, claimed_at, claim_status, delivery_method, 
                              store_pickup_code, pickup_store_id, staff_confirmed_by, auto_delivered, completed_at, notes) VALUES

-- Các yêu cầu nhận quà đã hoàn thành
(@participant1_event1, @gift1_voucher100k, 1701562800000, 'COMPLETED', 'AUTO_DELIVERY', NULL, NULL, NULL, 1, 1701562860000, 
    N'Voucher đã được tự động cấp vào tài khoản'),

(@participant2_event1, @gift3_points, 1701650000000, 'COMPLETED', 'AUTO_DELIVERY', NULL, NULL, NULL, 1, 1701650060000, 
    N'500 điểm đã được cộng vào tài khoản'),

(@participant2_event3, @gift7_certificate, 1700510460000, 'COMPLETED', 'STORE_PICKUP', 'CERT2024001', 1, 5, 0, 1700596860000, 
    N'Đã nhận chứng chỉ tại cửa hàng BookStation Central'),

(@participant2_event3, @gift8_speedbook, 1700510520000, 'COMPLETED', 'STORE_PICKUP', 'BOOK2024001', 1, 5, 0, 1700596920000, 
    N'Đã nhận sách tại cửa hàng cùng với chứng chỉ'),

(@participant1_event4, @gift9_voucher200k, 1700841660000, 'COMPLETED', 'AUTO_DELIVERY', NULL, NULL, NULL, 1, 1700841720000, 
    N'Voucher Black Friday đã được cấp tự động'),

-- Các yêu cầu đang chờ xử lý
(@participant4_event1, @gift2_bookmark, 1701820000000, 'PENDING', 'STORE_PICKUP', 'BOOK2024002', 2, NULL, 0, NULL, 
    N'Chờ nhận bộ bookmark tại cửa hàng BookStation Flagship'),

(@participant1_event1, @gift2_bookmark, 1701821000000, 'APPROVED', 'ONLINE_SHIPPING', NULL, NULL, NULL, 0, NULL, 
    N'Đã duyệt, chuẩn bị giao hàng qua đơn vận chuyển');

PRINT 'EVENT_GIFT_CLAIM inserted: ' + CAST(@@ROWCOUNT AS VARCHAR(10)) + ' rows';

-- =====================================================
-- 6. EVENT_HISTORY - Lịch sử sự kiện
-- =====================================================

PRINT '=== INSERTING EVENT_HISTORY ===';

INSERT INTO event_history (event_id, action_type, description, performed_by, created_at, old_values, new_values) VALUES

-- Lịch sử cho sự kiện "Thử thách đọc 30 ngày"
(@event1, 'CREATED', N'Tạo sự kiện "Thử thách đọc 30 ngày - Tháng 12/2024"', 1, 1701388800000, NULL, 
    N'{"event_name":"Thử thách đọc 30 ngày - Tháng 12/2024","status":"DRAFT","max_participants":1000}'),

(@event1, 'PUBLISHED', N'Xuất bản sự kiện và mở đăng ký cho người dùng', 1, 1701475200000, 
    N'{"status":"DRAFT"}', N'{"status":"PUBLISHED"}'),

(@event1, 'STARTED', N'Sự kiện chính thức bắt đầu', 1, 1701475200000, 
    N'{"status":"PUBLISHED"}', N'{"status":"ONGOING"}'),

(@event1, 'UPDATED', N'Cập nhật số lượng người tham gia: 156/1000', NULL, 1703001600000, 
    N'{"current_participants":120}', N'{"current_participants":156}'),

-- Lịch sử cho sự kiện "Gặp gỡ tác giả Nguyễn Nhật Ánh"
(@event2, 'CREATED', N'Tạo sự kiện gặp gỡ tác giả Nguyễn Nhật Ánh', 1, 1703606400000, NULL, 
    N'{"event_name":"Gặp gỡ tác giả Nguyễn Nhật Ánh","location":"BookStation Flagship Store"}'),

(@event2, 'PUBLISHED', N'Xuất bản sự kiện và mở đăng ký', 1, 1703692800000, 
    N'{"status":"DRAFT"}', N'{"status":"PUBLISHED"}'),

-- Lịch sử cho sự kiện "Workshop đọc nhanh" đã hoàn thành
(@event3, 'CREATED', N'Tạo workshop kỹ thuật đọc nhanh', 1, 1700236800000, NULL, 
    N'{"event_name":"Workshop Kỹ thuật đọc nhanh","max_participants":50}'),

(@event3, 'PUBLISHED', N'Xuất bản workshop', 1, 1700323200000, 
    N'{"status":"DRAFT"}', N'{"status":"PUBLISHED"}'),

(@event3, 'STARTED', N'Workshop bắt đầu', 1, 1700496000000, 
    N'{"status":"PUBLISHED"}', N'{"status":"ONGOING"}'),

(@event3, 'COMPLETED', N'Workshop hoàn thành thành công với 50 người tham gia', 1, 1700510400000, 
    N'{"status":"ONGOING"}', N'{"status":"COMPLETED"}'),

-- Lịch sử cho sự kiện "Black Friday Sale"
(@event4, 'CREATED', N'Tạo sự kiện Black Friday Sale 2024', 1, 1700668800000, NULL, 
    N'{"event_name":"Black Friday Sale 2024","event_type":"PROMOTION"}'),

(@event4, 'PUBLISHED', N'Xuất bản sự kiện khuyến mãi', 1, 1700755200000, 
    N'{"status":"DRAFT"}', N'{"status":"PUBLISHED"}'),

(@event4, 'STARTED', N'Black Friday Sale chính thức bắt đầu', 1, 1700755200000, 
    N'{"status":"PUBLISHED"}', N'{"status":"ONGOING"}'),

(@event4, 'UPDATED', N'Cập nhật số lượng người tham gia: 1247/5000', NULL, 1701000000000, 
    N'{"current_participants":856}', N'{"current_participants":1247}'),

-- Lịch sử cho sự kiện "Hội chợ sách Tết"
(@event5, 'CREATED', N'Tạo sự kiện Hội chợ sách Tết 2025', 1, 1703520000000, NULL, 
    N'{"event_name":"Hội chợ sách Tết 2025","location":"Công viên Tao Đàn"}'),

(@event5, 'PUBLISHED', N'Xuất bản sự kiện Hội chợ sách Tết', 1, 1703606400000, 
    N'{"status":"DRAFT"}', N'{"status":"PUBLISHED"}'),

-- Lịch sử cho cuộc thi "Review sách hay"
(@event6, 'CREATED', N'Tạo cuộc thi Review sách hay 2024', 1, 1702771200000, NULL, 
    N'{"event_name":"Cuộc thi Review sách hay 2024","event_type":"CONTEST"}'),

(@event6, 'PUBLISHED', N'Xuất bản cuộc thi', 1, 1702857600000, 
    N'{"status":"DRAFT"}', N'{"status":"PUBLISHED"}'),

(@event6, 'STARTED', N'Cuộc thi chính thức bắt đầu', 1, 1702857600000, 
    N'{"status":"PUBLISHED"}', N'{"status":"ONGOING"}'),

-- Lịch sử cho sự kiện đã hủy
(@event8, 'CREATED', N'Tạo Workshop "Viết truyện ngắn"', 1, 1699545600000, NULL, 
    N'{"event_name":"Workshop Viết truyện ngắn","max_participants":30}'),

(@event8, 'PUBLISHED', N'Xuất bản workshop', 1, 1699632000000, 
    N'{"status":"DRAFT"}', N'{"status":"PUBLISHED"}'),

(@event8, 'CANCELLED', N'Hủy sự kiện do tác giả bận đột xuất', 1, 1699804800000, 
    N'{"status":"PUBLISHED","current_participants":15}', N'{"status":"CANCELLED","cancelled_reason":"Tác giả bận đột xuất"}');

PRINT 'EVENT_HISTORY inserted: ' + CAST(@@ROWCOUNT AS VARCHAR(10)) + ' rows';

-- Bật lại foreign key checks
EXEC sp_MSforeachtable "ALTER TABLE ? WITH CHECK CHECK CONSTRAINT all"
GO

-- =====================================================
-- QUERIES KIỂM TRA DỮ LIỆU
-- =====================================================

PRINT '=== KIỂM TRA SỐ LƯỢNG DỮ LIỆU ===';

SELECT 'EVENT_CATEGORY' as TableName, COUNT(*) as RecordCount FROM event_category
UNION ALL
SELECT 'EVENT' as TableName, COUNT(*) as RecordCount FROM event
UNION ALL
SELECT 'EVENT_GIFT' as TableName, COUNT(*) as RecordCount FROM event_gift
UNION ALL
SELECT 'EVENT_PARTICIPANT' as TableName, COUNT(*) as RecordCount FROM event_participant
UNION ALL
SELECT 'EVENT_GIFT_CLAIM' as TableName, COUNT(*) as RecordCount FROM event_gift_claim
UNION ALL
SELECT 'EVENT_HISTORY' as TableName, COUNT(*) as RecordCount FROM event_history;

-- Test queries
PRINT '=== TEST QUERIES ===';

SELECT 'ONGOING EVENTS:' as Info;
SELECT e.id, e.event_name, e.status, e.current_participants, e.max_participants
FROM event e 
WHERE e.status = 'ONGOING';

SELECT 'EVENT CATEGORIES:' as Info;
SELECT ec.id, ec.category_name, COUNT(e.id) as event_count
FROM event_category ec 
LEFT JOIN event e ON ec.id = e.event_category_id 
GROUP BY ec.id, ec.category_name;

PRINT '=== SAMPLE DATA INSERTION COMPLETED SUCCESSFULLY! ===';
