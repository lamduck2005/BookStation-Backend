-- ===== VÍ DỤ THỰC TẾ: LUỒNG GIAO QUÀ TẶNG LIÊN KẾT VỚI ORDER =====

-- BƯỚC 1: Tạo sự kiện và người dùng claim quà (giống như trước)
-- ... (các bước trước đã có)

-- BƯỚC 2: XỬ LÝ CLAIM QUÀ TẶNG CHI TIẾT

-- 🎁 User 1 claim quà: Bộ sách + Voucher + Điểm + Kindle
INSERT INTO event_gift_claim (event_participant_id, event_gift_id, claimed_at, claim_status, auto_delivered, notes) VALUES
-- Bộ sách (cần giao hàng) 
(1, 1, 1725148800000, 'PENDING', 0, N'Yêu cầu giao bộ sách bestseller'),
-- Voucher (tự động) 
(1, 2, 1725148800000, 'APPROVED', 1, N'Voucher 500K đã tự động thêm vào tài khoản'),
-- Điểm thưởng (tự động)
(1, 3, 1725148800000, 'DELIVERED', 1, N'2000 điểm VIP đã được cộng tự động'),
-- Kindle (cần giao hàng)
(1, 4, 1725148800000, 'PENDING', 0, N'Yêu cầu giao Kindle Paperwhite');

-- 🎁 User 2 claim quà: Voucher + Điểm  
INSERT INTO event_gift_claim (event_participant_id, event_gift_id, claimed_at, claim_status, auto_delivered, notes) VALUES
(2, 2, 1725235200000, 'APPROVED', 1, N'Voucher 500K đã tự động thêm vào tài khoản'),
(2, 3, 1725235200000, 'DELIVERED', 1, N'2000 điểm VIP đã được cộng tự động');

-- 🎁 User 3 claim quà an ủi: Bookmark (cần giao hàng)
INSERT INTO event_gift_claim (event_participant_id, event_gift_id, claimed_at, claim_status, auto_delivered, notes) VALUES
(3, 5, 1725321600000, 'PENDING', 0, N'Yêu cầu giao bookmark cao cấp');

-- BƯỚC 3: ADMIN DUYỆT QUÀ VÀ TẠO ĐỚN HÀNG GIAO QUÀ

-- 📦 Tạo đơn hàng giao quà cho User 1 (Bộ sách)
INSERT INTO [order] (user_id, total_amount, status, order_date, shipping_address, order_type, notes) VALUES
(1, 0.00, 'CONFIRMED', 1725235200000, N'123 Đường ABC, Phường 1, Quận 1, TP.HCM', 'EVENT_GIFT', N'Đơn hàng giao quà sự kiện - Bộ sách bestseller');

-- Giả sử order_id vừa tạo = 1001
UPDATE event_gift_claim 
SET claim_status = 'ORDER_CREATED', 
    delivery_order_id = 1001,
    notes = CONCAT(notes, N' | Đã tạo đơn hàng giao quà #1001')
WHERE event_participant_id = 1 AND event_gift_id = 1;

-- 📦 Tạo đơn hàng giao quà cho User 1 (Kindle) 
INSERT INTO [order] (user_id, total_amount, status, order_date, shipping_address, order_type, notes) VALUES
(1, 0.00, 'CONFIRMED', 1725235200000, N'123 Đường ABC, Phường 1, Quận 1, TP.HCM', 'EVENT_GIFT', N'Đơn hàng giao quà sự kiện - Kindle Paperwhite');

-- Giả sử order_id = 1002
UPDATE event_gift_claim 
SET claim_status = 'ORDER_CREATED', 
    delivery_order_id = 1002,
    notes = CONCAT(notes, N' | Đã tạo đơn hàng giao quà #1002')
WHERE event_participant_id = 1 AND event_gift_id = 4;

-- 📦 Tạo đơn hàng giao quà cho User 3 (Bookmark)
INSERT INTO [order] (user_id, total_amount, status, order_date, shipping_address, order_type, notes) VALUES
(3, 0.00, 'CONFIRMED', 1725321600000, N'789 Đường DEF, Phường 5, Quận 7, TP.HCM', 'EVENT_GIFT', N'Đơn hàng giao quà sự kiện - Bookmark cao cấp');

-- Giả sử order_id = 1003
UPDATE event_gift_claim 
SET claim_status = 'ORDER_CREATED', 
    delivery_order_id = 1003,
    notes = CONCAT(notes, N' | Đã tạo đơn hàng giao quà #1003')
WHERE event_participant_id = 3 AND event_gift_id = 5;

-- BƯỚC 4: QUÁ TRÌNH GIAO HÀNG (SỬ DỤNG HỆ THỐNG ORDER CÓ SẴN)

-- 🚚 Ngày 03/09: Giao thành công bộ sách cho User 1
UPDATE [order] SET status = 'DELIVERED' WHERE id = 1001;
UPDATE event_gift_claim 
SET claim_status = 'DELIVERED', 
    completed_at = 1725321600000,
    notes = CONCAT(notes, N' | ✅ Giao thành công 03/09/2025')
WHERE delivery_order_id = 1001;

-- 🚚 Ngày 05/09: Giao thành công Kindle cho User 1
UPDATE [order] SET status = 'DELIVERED' WHERE id = 1002;
UPDATE event_gift_claim 
SET claim_status = 'DELIVERED', 
    completed_at = 1725494400000,
    notes = CONCAT(notes, N' | ✅ Giao Kindle thành công, khách hàng hài lòng!')
WHERE delivery_order_id = 1002;

-- 🚚 Ngày 07/09: Giao thành công bookmark cho User 3
UPDATE [order] SET status = 'DELIVERED' WHERE id = 1003;
UPDATE event_gift_claim 
SET claim_status = 'DELIVERED', 
    completed_at = 1725667200000,
    notes = CONCAT(notes, N' | ✅ Giao bookmark thành công')
WHERE delivery_order_id = 1003;

-- BƯỚC 5: XỬ LÝ QUÀ TỰ ĐỘNG (VOUCHER, ĐIỂM THƯỞNG)

-- 💳 Tự động thêm voucher vào tài khoản user
-- Giả sử có bảng user_voucher để lưu voucher của user
INSERT INTO user_voucher (user_id, voucher_id, obtained_date, source_type, source_id) VALUES
(1, 1, 1725148800000, 'EVENT_GIFT', 1), -- User 1 nhận voucher từ claim_id = 1 
(2, 1, 1725235200000, 'EVENT_GIFT', 2); -- User 2 nhận voucher từ claim_id = 2

-- 🎯 Tự động cộng điểm thưởng
UPDATE [user] SET total_points = total_points + 2000 WHERE id = 1; -- User 1 +2000 điểm
UPDATE [user] SET total_points = total_points + 2000 WHERE id = 2; -- User 2 +2000 điểm
UPDATE [user] SET total_points = total_points + 500 WHERE id = 3;  -- User 3 +500 điểm (từ bookmark)

-- BƯỚC 6: BÁO CÁO TỔNG HỢP

-- 📊 Xem tổng quan claim quà theo trạng thái
SELECT 
    egc.claim_status,
    COUNT(*) as total_claims,
    COUNT(egc.delivery_order_id) as orders_created,
    COUNT(CASE WHEN egc.auto_delivered = 1 THEN 1 END) as auto_delivered_count
FROM event_gift_claim egc
JOIN event_participant ep ON egc.event_participant_id = ep.id
WHERE ep.event_id = 1
GROUP BY egc.claim_status;

-- 📊 Xem chi tiết quà và đơn hàng giao quà
SELECT 
    u.full_name,
    eg.gift_name,
    eg.gift_type,
    egc.claim_status,
    egc.auto_delivered,
    o.id as order_id,
    o.status as order_status,
    o.shipping_address
FROM event_gift_claim egc
JOIN event_participant ep ON egc.event_participant_id = ep.id
JOIN [user] u ON ep.user_id = u.id
JOIN event_gift eg ON egc.event_gift_id = eg.id
LEFT JOIN [order] o ON egc.delivery_order_id = o.id
WHERE ep.event_id = 1
ORDER BY u.full_name, eg.gift_name;

-- 📊 Thống kê hiệu quả sự kiện
SELECT 
    'Tổng số người tham gia' as metric, 
    COUNT(DISTINCT ep.user_id) as value
FROM event_participant ep WHERE ep.event_id = 1
UNION ALL
SELECT 
    'Số người hoàn thành', 
    COUNT(DISTINCT ep.user_id)
FROM event_participant ep WHERE ep.event_id = 1 AND ep.completion_status IN ('COMPLETED', 'WINNER')
UNION ALL
SELECT 
    'Tổng số quà được claim', 
    COUNT(*)
FROM event_gift_claim egc
JOIN event_participant ep ON egc.event_participant_id = ep.id
WHERE ep.event_id = 1
UNION ALL
SELECT 
    'Số đơn hàng giao quà', 
    COUNT(DISTINCT egc.delivery_order_id)
FROM event_gift_claim egc
JOIN event_participant ep ON egc.event_participant_id = ep.id
WHERE ep.event_id = 1 AND egc.delivery_order_id IS NOT NULL
UNION ALL
SELECT 
    'Quà đã giao thành công', 
    COUNT(*)
FROM event_gift_claim egc
JOIN event_participant ep ON egc.event_participant_id = ep.id
WHERE ep.event_id = 1 AND egc.claim_status = 'DELIVERED';

-- ===== KẾT LUẬN =====
/*
🎯 LUỒNG HOẠT ĐỘNG HOÀN CHỈNH:

1. USER CLAIM QUÀ → event_gift_claim (PENDING)

2. PHÂN LOẠI QUÀ:
   - Quà vật lý (sách, Kindle, bookmark) → Cần tạo Order giao hàng
   - Quà số (voucher, điểm) → Tự động xử lý (auto_delivered = true)

3. TẠO ĐỚN HÀNG GIAO QUÀ:
   - Sử dụng bảng Order hiện có với order_type = 'EVENT_GIFT'
   - total_amount = 0 (miễn phí)
   - Liên kết delivery_order_id

4. GIAO HÀNG:
   - Sử dụng hệ thống giao hàng có sẵn
   - Cập nhật trạng thái Order → Cập nhật claim_status

5. XỬ LÝ QUÀ SỐ:
   - Voucher → Thêm vào user_voucher
   - Điểm → Cộng vào user.total_points

6. THEO DÕI & BÁO CÁO:
   - Trạng thái chi tiết từng bước
   - Liên kết với hệ thống Order để tracking
   - Báo cáo hiệu quả sự kiện

🚀 LỢI ÍCH:
- Tái sử dụng hệ thống Order/shipping có sẵn
- Quản lý thống nhất địa chỉ giao hàng
- Theo dõi chi tiết từng bước
- Tự động hóa quà số (voucher/điểm)
- Báo cáo tổng hợp hiệu quả
*/
