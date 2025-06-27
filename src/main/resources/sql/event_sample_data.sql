-- Thêm dữ liệu mẫu cho event_category
INSERT INTO event_category (category_name, description, icon_url, is_active) VALUES
(N'Ra mắt sách mới', N'Sự kiện giới thiệu và ra mắt các đầu sách mới', '/icons/book-launch.svg', 1),
(N'Gặp gỡ tác giả', N'Buổi gặp gỡ và giao lưu với các tác giả nổi tiếng', '/icons/author-meet.svg', 1),
(N'Thử thách đọc sách', N'Các cuộc thi và thử thách liên quan đến việc đọc sách', '/icons/reading-challenge.svg', 1),
(N'Hội chợ sách', N'Triển lãm và hội chợ bán sách với nhiều ưu đãi', '/icons/book-fair.svg', 1),
(N'Sự kiện mùa', N'Các sự kiện theo mùa như Tết, Halloween, Giáng sinh', '/icons/seasonal.svg', 1),
(N'Khuyến mãi đặc biệt', N'Các chương trình khuyến mãi và giảm giá sách', '/icons/promotion.svg', 1),
(N'Cuộc thi', N'Các cuộc thi viết, vẽ, review sách', '/icons/contest.svg', 1),
(N'Hội thảo', N'Các buổi hội thảo về văn học, kỹ năng đọc', '/icons/workshop.svg', 1);

-- Thêm dữ liệu mẫu cho event (giả sử có user với id 1)
INSERT INTO event (event_name, description, event_type, event_category_id, status, start_date, end_date, max_participants, location, rules, is_online, created_by) VALUES
(N'Ra mắt tiểu thuyết "Những ngày xa nhà"', 
 N'Buổi ra mắt tiểu thuyết mới nhất của tác giả nổi tiếng với nhiều hoạt động hấp dẫn và quà tặng', 
 'BOOK_LAUNCH', 1, 'PUBLISHED', 
 '2025-07-15 19:00:00', '2025-07-15 21:00:00', 
 100, N'Nhà sách BookStation - Hà Nội', 
 N'- Tham gia đầy đủ buổi sự kiện\n- Tương tác tích cực với tác giả\n- Mua ít nhất 1 cuốn sách để nhận quà', 
 0, 1),

(N'Thử thách đọc 50 cuốn sách trong năm 2025', 
 N'Thử thách dành cho những người yêu sách, đọc 50 cuốn sách trong năm và nhận nhiều phần thưởng giá trị', 
 'READING_CHALLENGE', 3, 'ONGOING', 
 '2025-01-01 00:00:00', '2025-12-31 23:59:59', 
 500, N'Online', 
 N'- Đăng ký tham gia thử thách\n- Update tiến độ đọc sách hàng tháng\n- Review ít nhất 10 cuốn sách\n- Hoàn thành đúng hạn', 
 1, 1),

(N'Gặp gỡ tác giả Nguyễn Nhật Ánh', 
 N'Buổi gặp gỡ và ký tặng sách với tác giả Nguyễn Nhật Ánh - người cha của văn học thiếu nhi Việt Nam', 
 'AUTHOR_MEET', 2, 'PUBLISHED', 
 '2025-08-10 15:00:00', '2025-08-10 17:00:00', 
 200, N'Trung tâm sách TP.HCM', 
 N'- Mua vé tham gia 50,000 VNĐ\n- Mang theo sách để ký tặng\n- Chuẩn bị câu hỏi cho tác giả', 
 0, 1),

(N'Khuyến mãi Black Friday - Giảm đến 70%', 
 N'Sự kiện khuyến mãi lớn nhất trong năm với hàng ngàn đầu sách giảm giá sâu', 
 'PROMOTION', 6, 'DRAFT', 
 '2025-11-25 00:00:00', '2025-11-30 23:59:59', 
 1000, N'Toàn bộ hệ thống BookStation', 
 N'- Áp dụng cho tất cả sách có trong kho\n- Mỗi khách hàng được mua tối đa 20 cuốn\n- Không áp dụng cùng các chương trình khuyến mãi khác', 
 1, 1);

-- Thêm dữ liệu mẫu cho event_gift
INSERT INTO event_gift (event_id, gift_name, description, gift_value, quantity, remaining_quantity, gift_type, point_value) VALUES
(1, N'Sách ký tặng "Những ngày xa nhà"', N'Cuốn sách được tác giả ký tặng trực tiếp', 250000, 50, 50, 'BOOK', NULL),
(1, N'Bookmark cao cấp', N'Bookmark kim loại cao cấp với thiết kế độc đáo', 50000, 100, 100, 'PHYSICAL_ITEM', NULL),
(1, N'Điểm thưởng BookStation', N'500 điểm thưởng để mua sách', NULL, 100, 100, 'POINT', 500),

(2, N'Voucher giảm giá 100K', N'Voucher giảm 100,000 VNĐ cho đơn hàng từ 500K', 100000, 100, 100, 'VOUCHER', NULL),
(2, N'Huy hiệu "Reading Champion"', N'Huy hiệu đặc biệt dành cho những người hoàn thành thử thách', 0, 50, 50, 'PHYSICAL_ITEM', NULL),
(2, N'Điểm thưởng VIP', N'2000 điểm thưởng VIP', NULL, 50, 50, 'POINT', 2000),

(3, N'Bộ sách Nguyễn Nhật Ánh', N'Bộ 5 cuốn sách hay nhất của tác giả', 500000, 20, 20, 'BOOK', NULL),
(3, N'Ảnh ký tặng', N'Ảnh chụp cùng tác giả có chữ ký', 100000, 50, 50, 'PHYSICAL_ITEM', NULL),

(4, N'Voucher giảm 70%', N'Voucher giảm 70% cho 1 cuốn sách bất kỳ', 200000, 1000, 1000, 'VOUCHER', NULL),
(4, N'Điểm thưởng Black Friday', N'1000 điểm thưởng đặc biệt', NULL, 500, 500, 'POINT', 1000);

