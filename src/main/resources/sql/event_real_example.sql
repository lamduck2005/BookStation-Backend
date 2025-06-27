-- ===== VÍ DỤ THỰC TẾ: SỰ KIỆN "THỬ THÁCH ĐỌC SÁCH MÙA HÈ 2025" =====

-- Bước 1: Tạo danh mục sự kiện
INSERT INTO event_category (category_name, description, icon_url, is_active, created_at) VALUES
(N'Thử thách đọc sách', N'Các cuộc thi và thử thách đọc sách theo mùa', '/icons/reading-challenge.svg', 1, 1719417600000); -- 2025-06-26 20:00:00

-- Bước 2: Tạo sự kiện chính
INSERT INTO event (event_name, description, event_type, event_category_id, status, start_date, end_date, max_participants, current_participants, image_url, location, rules, is_online, created_at, updated_at, created_by) VALUES
(N'Thử thách đọc sách mùa hè 2025 - Đọc 10 cuốn nhận quà khủng!', 
 N'Sự kiện đọc sách lớn nhất mùa hè! Đọc 10 cuốn sách trong 3 tháng hè và nhận những phần quà cực kỳ hấp dẫn từ BookStation. Bao gồm sách miễn phí, voucher giảm giá và điểm thưởng VIP!', 
 'READING_CHALLENGE', 1, 'PUBLISHED', 
 1719504000000, -- 2025-06-27 20:00:00 (bắt đầu)
 1727712000000, -- 2025-08-31 23:59:59 (kết thúc)
 1000, 0, 
 '/images/events/summer-reading-challenge-2025.jpg', 
 N'Online - Toàn quốc', 
 N'📖 LUẬT CHƠI:\n1. Đăng ký tham gia sự kiện\n2. Đọc và review ít nhất 10 cuốn sách từ BookStation\n3. Mỗi review phải có ít nhất 100 từ và 4⭐ trở lên\n4. Hoàn thành trước 31/08/2025\n5. Chỉ tính sách mua từ BookStation trong thời gian diễn ra sự kiện', 
 1, 1719417600000, 1719417600000, 1);

-- Bước 3: Tạo các phần quà cho sự kiện (event_id = 1)
INSERT INTO event_gift (event_id, gift_name, description, gift_value, quantity, remaining_quantity, image_url, gift_type, book_id, voucher_id, point_value, is_active, created_at) VALUES
-- Quà cho người hoàn thành 10 cuốn
(1, N'Bộ sách bestseller "Tâm lý học đám đông"', N'Bộ 3 cuốn sách tâm lý học bán chạy nhất 2025, bìa cứng cao cấp', 750000, 100, 100, '/images/gifts/psychology-book-set.jpg', 'BOOK', NULL, NULL, NULL, 1, 1719417600000),

-- Quà cho top 50 người đầu tiên hoàn thành
(1, N'Voucher giảm 500K cho đơn hàng từ 1 triệu', N'Voucher giảm giá đặc biệt dành cho top 50 người hoàn thành sớm nhất', 500000, 50, 50, '/images/gifts/voucher-500k.jpg', 'VOUCHER', NULL, 1, NULL, 1, 1719417600000),

-- Quà cho tất cả người hoàn thành
(1, N'Điểm thưởng BookStation VIP', N'2000 điểm thưởng VIP có thể đổi sách miễn phí', NULL, 1000, 1000, '/images/gifts/vip-points.jpg', 'POINT', NULL, NULL, 2000, 1, 1719417600000),

-- Quà đặc biệt cho người có review hay nhất
(1, N'Máy đọc sách Kindle Paperwhite', N'Máy đọc sách Kindle Paperwhite 2025 chính hãng, dành cho người có review xuất sắc nhất', 3500000, 3, 3, '/images/gifts/kindle-paperwhite.jpg', 'PHYSICAL_ITEM', NULL, NULL, NULL, 1, 1719417600000),

-- Quà an ủi cho người chưa hoàn thành nhưng đọc ít nhất 5 cuốn
(1, N'Bookmark cao cấp + Điểm thưởng', N'Bookmark kim loại cao cấp và 500 điểm thưởng cho những nỗ lực đáng khen', 100000, 200, 200, '/images/gifts/bookmark-consolation.jpg', 'PHYSICAL_ITEM', NULL, NULL, 500, 1, 1719417600000);

-- ===== SIMULATION: QUÁ TRÌNH NGƯỜI DÙNG THAM GIA =====

-- Bước 4: Người dùng bắt đầu đăng ký tham gia (giả sử có 5 user: id 1,2,3,4,5)
INSERT INTO event_participant (event_id, user_id, joined_at, is_winner, completion_status, notes) VALUES
-- User 1: Tham gia ngày đầu
(1, 1, 1719504000000, 0, 'JOINED', N'Đăng ký tham gia sự kiện, mục tiêu đọc 15 cuốn!'),
-- User 2: Tham gia sau 1 tuần  
(1, 2, 1720108800000, 0, 'JOINED', N'Yêu thích sách tâm lý học, quyết tâm hoàn thành thử thách'),
-- User 3: Tham gia giữa tháng 7
(1, 3, 1721318400000, 0, 'JOINED', N'Đọc sách để cải thiện kiến thức và kỹ năng'),
-- User 4: Tham gia muộn tháng 7
(1, 4, 1722528000000, 0, 'JOINED', N'Nghe bạn bè giới thiệu, tham gia thử xem'),
-- User 5: Tham gia cuối tháng 7
(1, 5, 1723132800000, 0, 'JOINED', N'Fan BookStation, không thể bỏ lỡ sự kiện này!');

-- Cập nhật số lượng người tham gia hiện tại
UPDATE event SET current_participants = 5 WHERE id = 1;

-- ===== SIMULATION: QUÁ TRÌNH ĐỌC SÁCH VÀ HOÀN THÀNH =====

-- Ngày 15/08: User 1 hoàn thành 10 cuốn sách (người đầu tiên!)
UPDATE event_participant 
SET completion_status = 'COMPLETED', 
    notes = N'🎉 HOÀN THÀNH! Đọc xong 12 cuốn sách, viết 12 review chất lượng cao. Người đầu tiên hoàn thành thử thách!'
WHERE event_id = 1 AND user_id = 1;

-- Ngày 20/08: User 2 hoàn thành 10 cuốn sách
UPDATE event_participant 
SET completion_status = 'COMPLETED',
    notes = N'✨ HOÀN THÀNH! Đọc 11 cuốn sách tâm lý học, mỗi review đều rất chi tiết và hữu ích'
WHERE event_id = 1 AND user_id = 2;

-- Ngày 25/08: User 5 hoàn thành 10 cuốn sách 
UPDATE event_participant 
SET completion_status = 'COMPLETED',
    notes = N'💪 HOÀN THÀNH! Dù tham gia muộn nhưng đã nỗ lực đọc 13 cuốn trong 1 tháng!'
WHERE event_id = 1 AND user_id = 5;

-- Ngày 30/08: User 3 chỉ đọc được 6 cuốn (chưa hoàn thành)
UPDATE event_participant 
SET completion_status = 'PARTIAL',
    notes = N'😔 Chưa hoàn thành được 10 cuốn, chỉ đọc được 6 cuốn nhưng rất chất lượng'
WHERE event_id = 1 AND user_id = 3;

-- User 4 bỏ cuộc giữa chừng
UPDATE event_participant 
SET completion_status = 'DROPPED',
    notes = N'❌ Bỏ cuộc sau 3 cuốn vì bận học tập'
WHERE event_id = 1 AND user_id = 4;

-- ===== TRAO GIẢI VÀ PHÂN PHÁT QUÀ =====

-- Ngày 01/09: Admin xác định người thắng cuộc và phân phối quà

-- User 1: Top 1 hoàn thành sớm nhất -> Nhận tất cả quà
UPDATE event_participant 
SET is_winner = 1, gift_received_id = 1, -- Bộ sách bestseller
    completion_status = 'WINNER'
WHERE event_id = 1 AND user_id = 1;

-- Tạo bản ghi claim quà cho User 1
INSERT INTO event_gift_claim (event_participant_id, event_gift_id, claimed_at, delivery_status, delivery_address, notes) VALUES
-- Nhận bộ sách (User 1 = participant_id 1)
(1, 1, 1725148800000, 'PENDING', N'123 Đường ABC, Quận 1, TP.HCM', N'Người hoàn thành đầu tiên - nhận bộ sách bestseller'),
-- Nhận voucher 500K  
(1, 2, 1725148800000, 'PENDING', N'123 Đường ABC, Quận 1, TP.HCM', N'Top 50 hoàn thành sớm - voucher 500K'),
-- Nhận điểm thưởng VIP
(1, 3, 1725148800000, 'DELIVERED', NULL, N'Điểm thưởng đã được cộng vào tài khoản'),
-- Nhận Kindle (vì có review hay nhất)
(1, 4, 1725148800000, 'PROCESSING', N'123 Đường ABC, Quận 1, TP.HCM', N'Review xuất sắc nhất tháng - Kindle Paperwhite');

-- User 2: Hoàn thành trong top 50
INSERT INTO event_gift_claim (event_participant_id, event_gift_id, claimed_at, delivery_status, delivery_address, notes) VALUES
(2, 2, 1725235200000, 'PENDING', N'456 Đường XYZ, Quận 3, TP.HCM', N'Top 50 hoàn thành - voucher 500K'),
(2, 3, 1725235200000, 'DELIVERED', NULL, N'Điểm thưởng VIP đã được cộng');

-- User 5: Hoàn thành muộn hơn nhưng vẫn đủ điều kiện
INSERT INTO event_gift_claim (event_participant_id, event_gift_id, claimed_at, delivery_status, notes) VALUES
(5, 3, 1725321600000, 'DELIVERED', N'Điểm thưởng VIP cho người hoàn thành thử thách');

-- User 3: Chưa hoàn thành nhưng đọc được 6 cuốn -> Quà an ủi
INSERT INTO event_gift_claim (event_participant_id, event_gift_id, claimed_at, delivery_status, delivery_address, notes) VALUES
(3, 5, 1725321600000, 'PENDING', N'789 Đường DEF, Quận 7, TP.HCM', N'Quà an ủi cho nỗ lực đọc 6 cuốn sách');

-- Cập nhật số lượng quà còn lại
UPDATE event_gift SET remaining_quantity = remaining_quantity - 1 WHERE id = 1; -- Bộ sách: 99 còn lại
UPDATE event_gift SET remaining_quantity = remaining_quantity - 2 WHERE id = 2; -- Voucher 500K: 48 còn lại  
UPDATE event_gift SET remaining_quantity = remaining_quantity - 3 WHERE id = 3; -- Điểm VIP: 997 còn lại
UPDATE event_gift SET remaining_quantity = remaining_quantity - 1 WHERE id = 4; -- Kindle: 2 còn lại
UPDATE event_gift SET remaining_quantity = remaining_quantity - 1 WHERE id = 5; -- Bookmark: 199 còn lại

-- ===== THEO DÕI GIAO HÀNG =====

-- Ngày 03/09: Giao thành công bộ sách cho User 1
UPDATE event_gift_claim 
SET delivery_status = 'DELIVERED', 
    delivered_at = 1725321600000,
    notes = CONCAT(notes, N' | ✅ Đã giao thành công ngày 03/09/2025')
WHERE event_participant_id = 1 AND event_gift_id = 1;

-- Ngày 05/09: Giao thành công Kindle cho User 1  
UPDATE event_gift_claim 
SET delivery_status = 'DELIVERED',
    delivered_at = 1725494400000,
    notes = CONCAT(notes, N' | ✅ Giao Kindle thành công, khách hàng rất hài lòng!')
WHERE event_participant_id = 1 AND event_gift_id = 4;

-- ===== KẾT THÚC SỰ KIỆN =====

-- Ngày 01/09: Cập nhật trạng thái sự kiện thành COMPLETED
UPDATE event 
SET status = 'COMPLETED', 
    updated_at = 1725148800000
WHERE id = 1;

-- Thêm lịch sử kết thúc sự kiện
INSERT INTO event_history (event_id, action_type, description, performed_by, created_at, new_values) VALUES
(1, 'COMPLETED', N'Sự kiện "Thử thách đọc sách mùa hè 2025" đã kết thúc thành công. Tổng cộng 3/5 người tham gia hoàn thành thử thách.', 1, 1725148800000, N'{"total_participants": 5, "completed": 3, "gifts_distributed": 8, "total_gift_value": "5,150,000 VNĐ"}');

-- ===== QUERY ĐỂ XEM KẾT QUẢ TỔNG QUAN =====

-- Xem tổng quan sự kiện
SELECT 
    e.event_name,
    e.status,
    e.max_participants,
    e.current_participants,
    COUNT(ep.id) as actual_participants,
    COUNT(CASE WHEN ep.completion_status = 'COMPLETED' OR ep.completion_status = 'WINNER' THEN 1 END) as completed_count,
    COUNT(egc.id) as total_gifts_claimed
FROM event e
LEFT JOIN event_participant ep ON e.id = ep.event_id  
LEFT JOIN event_gift_claim egc ON ep.id = egc.event_participant_id
WHERE e.id = 1
GROUP BY e.id, e.event_name, e.status, e.max_participants, e.current_participants;

-- Xem chi tiết người tham gia và quà nhận được
SELECT 
    u.full_name,
    ep.joined_at,
    ep.completion_status,
    ep.is_winner,
    eg.gift_name,
    egc.delivery_status,
    egc.delivered_at
FROM event_participant ep
JOIN [user] u ON ep.user_id = u.id
LEFT JOIN event_gift_claim egc ON ep.id = egc.event_participant_id
LEFT JOIN event_gift eg ON egc.event_gift_id = eg.id
WHERE ep.event_id = 1
ORDER BY ep.joined_at;
