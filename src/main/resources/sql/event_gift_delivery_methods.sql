-- ===== XỬ LÝ QUÀ TẶNG CHO CẢ ONLINE VÀ OFFLINE =====

-- TÌNH HUỐNG 1: SỰ KIỆN ONLINE - GIAO HÀNG TẬN NHÀ
-- User tham gia sự kiện online, muốn nhận quà giao tận nhà

INSERT INTO event_gift_claim (event_participant_id, event_gift_id, claimed_at, claim_status, delivery_method, notes) VALUES
(1, 1, 1725148800000, 'PENDING', 'ONLINE_SHIPPING', N'User yêu cầu giao bộ sách tận nhà');

-- Admin duyệt và tạo đơn hàng giao quà
INSERT INTO [order] (user_id, total_amount, status, order_date, shipping_address, order_type, notes) VALUES
(1, 0.00, 'CONFIRMED', 1725235200000, N'123 Đường ABC, Quận 1, TP.HCM', 'EVENT_GIFT', N'Đơn hàng giao quà sự kiện');

-- Cập nhật claim với order_id
UPDATE event_gift_claim 
SET claim_status = 'ORDER_CREATED', 
    delivery_order_id = 1001,
    notes = CONCAT(notes, N' | Tạo đơn hàng #1001')
WHERE event_participant_id = 1 AND event_gift_id = 1;

-- ===== ===== ===== ===== ===== ===== ===== ===== ===== =====

-- TÌNH HUỐNG 2: SỰ KIỆN TẠI CỬA HÀNG - NHẬN TẠI CHỖ
-- User tham gia sự kiện tại cửa hàng, nhận quà ngay tại chỗ

INSERT INTO event_gift_claim (event_participant_id, event_gift_id, claimed_at, claim_status, delivery_method, pickup_store_id, notes) VALUES
(2, 2, 1725148800000, 'APPROVED', 'DIRECT_HANDOVER', 1, N'Nhận quà trực tiếp tại sự kiện cửa hàng Hà Nội');

-- Nhân viên trao quà và xác nhận
UPDATE event_gift_claim 
SET claim_status = 'DELIVERED', 
    staff_confirmed_by = 101, -- ID nhân viên
    completed_at = 1725148800000,
    notes = CONCAT(notes, N' | Đã trao quà trực tiếp, xác nhận bởi NV #101')
WHERE event_participant_id = 2 AND event_gift_id = 2;

-- ===== ===== ===== ===== ===== ===== ===== ===== ===== =====

-- TÌNH HUỐNG 3: SỰ KIỆN ONLINE - NHẬN TẠI CỬA HÀNG
-- User tham gia online nhưng muốn nhận quà tại cửa hàng (tiết kiệm phí ship)

INSERT INTO event_gift_claim (event_participant_id, event_gift_id, claimed_at, claim_status, delivery_method, store_pickup_code, pickup_store_id, notes) VALUES
(3, 3, 1725148800000, 'APPROVED', 'STORE_PICKUP', 'GIFT2025001', 2, N'User chọn nhận quà tại cửa hàng TP.HCM');

-- User đến cửa hàng nhận quà
UPDATE event_gift_claim 
SET claim_status = 'DELIVERED', 
    staff_confirmed_by = 102,
    completed_at = 1725235200000,
    notes = CONCAT(notes, N' | Đã nhận quà tại cửa hàng, mã: GIFT2025001, NV #102')
WHERE store_pickup_code = 'GIFT2025001';

-- ===== ===== ===== ===== ===== ===== ===== ===== ===== =====

-- TÌNH HUỐNG 4: QUÀ SỐ - TỰ ĐỘNG XỬ LÝ
-- Voucher và điểm thưởng được cộng tự động, không cần giao hàng

INSERT INTO event_gift_claim (event_participant_id, event_gift_id, claimed_at, claim_status, delivery_method, auto_delivered, notes) VALUES
-- Voucher
(4, 4, 1725148800000, 'DELIVERED', 'DIGITAL_DELIVERY', 1, N'Voucher 500K đã được thêm vào tài khoản'),
-- Điểm thưởng  
(4, 5, 1725148800000, 'DELIVERED', 'DIGITAL_DELIVERY', 1, N'2000 điểm VIP đã được cộng tự động');

-- Tự động cộng voucher và điểm
INSERT INTO user_voucher (user_id, voucher_id, obtained_date, source_type, source_id) VALUES
(4, 1, 1725148800000, 'EVENT_GIFT', 1);

UPDATE [user] SET total_points = total_points + 2000 WHERE id = 4;

-- ===== ===== ===== ===== ===== ===== ===== ===== ===== =====

-- TÌNH HUỐNG 5: QUÀ LỚN - COMBO NHIỀU PHƯƠNG THỨC
-- User thắng lớn, nhận nhiều quà với các phương thức khác nhau

INSERT INTO event_gift_claim (event_participant_id, event_gift_id, claimed_at, claim_status, delivery_method, notes) VALUES
-- Kindle - giao tận nhà
(5, 6, 1725148800000, 'PENDING', 'ONLINE_SHIPPING', N'Kindle Paperwhite - giao tận nhà'),
-- Bộ sách - nhận tại cửa hàng
(5, 7, 1725148800000, 'APPROVED', 'STORE_PICKUP', N'Bộ sách bestseller - nhận tại cửa hàng'),
-- Voucher - tự động
(5, 8, 1725148800000, 'DELIVERED', 'DIGITAL_DELIVERY', N'Voucher 1 triệu - tự động cộng');

-- Xử lý từng loại quà
-- 1. Kindle: Tạo đơn hàng giao tận nhà
INSERT INTO [order] (user_id, total_amount, order_type, status, shipping_address) VALUES
(5, 0.00, 'EVENT_GIFT', 'CONFIRMED', N'789 Đường XYZ, Quận 3, TP.HCM');

UPDATE event_gift_claim SET delivery_order_id = 1002, claim_status = 'ORDER_CREATED' 
WHERE event_participant_id = 5 AND event_gift_id = 6;

-- 2. Bộ sách: Tạo mã pickup
UPDATE event_gift_claim 
SET store_pickup_code = 'GIFT2025002', pickup_store_id = 1
WHERE event_participant_id = 5 AND event_gift_id = 7;

-- 3. Voucher: Đã tự động xử lý

-- ===== BÁO CÁO THEO PHƯƠNG THỨC GIAO QUÀ =====

SELECT 
    egc.delivery_method,
    COUNT(*) as total_claims,
    COUNT(CASE WHEN egc.claim_status = 'DELIVERED' THEN 1 END) as completed_claims,
    COUNT(egc.delivery_order_id) as orders_created,
    COUNT(CASE WHEN egc.store_pickup_code IS NOT NULL THEN 1 END) as pickup_codes_generated
FROM event_gift_claim egc
JOIN event_participant ep ON egc.event_participant_id = ep.id
WHERE ep.event_id = 1
GROUP BY egc.delivery_method;

-- ===== QUERY THEO DÕI QUẦN LÝ CỬA HÀNG =====

-- Xem quà chờ nhận tại cửa hàng
SELECT 
    egc.store_pickup_code,
    u.full_name,
    u.phone,
    eg.gift_name,
    egc.claimed_at,
    egc.pickup_store_id
FROM event_gift_claim egc
JOIN event_participant ep ON egc.event_participant_id = ep.id
JOIN [user] u ON ep.user_id = u.id
JOIN event_gift eg ON egc.event_gift_id = eg.id
WHERE egc.delivery_method = 'STORE_PICKUP' 
  AND egc.claim_status = 'APPROVED'
ORDER BY egc.claimed_at;

-- Xem lịch sử trao quà của nhân viên
SELECT 
    egc.staff_confirmed_by,
    COUNT(*) as gifts_delivered,
    STRING_AGG(eg.gift_name, ', ') as gift_types
FROM event_gift_claim egc
JOIN event_gift eg ON egc.event_gift_id = eg.id
WHERE egc.staff_confirmed_by IS NOT NULL
GROUP BY egc.staff_confirmed_by;

-- ===== KẾT LUẬN =====
/*
🎯 HỆ THỐNG LINH HOẠT HỖ TRỢ NHIỀU TÌNH HUỐNG:

1. 🚚 ONLINE_SHIPPING: Giao hàng tận nhà
   - Tạo Order với order_type = 'EVENT_GIFT'  
   - Sử dụng hệ thống giao hàng có sẵn
   - Tracking như đơn hàng thường

2. 🏪 STORE_PICKUP: Nhận tại cửa hàng
   - Tạo mã pickup duy nhất
   - Không cần tạo Order (tiết kiệm phí ship)
   - Nhân viên scan mã để xác nhận

3. 📱 DIGITAL_DELIVERY: Quà số
   - Tự động cộng voucher/điểm
   - Không cần can thiệp thủ công
   - Instant delivery

4. 🤝 DIRECT_HANDOVER: Trao tay trực tiếp
   - Tại sự kiện offline
   - Nhân viên xác nhận ngay
   - Không cần logistics

💡 LỢI ÍCH:
- Tối ưu chi phí (không ship không cần thiết)
- Trải nghiệm linh hoạt cho user
- Quản lý tập trung nhưng đa dạng phương thức
- Audit trail đầy đủ cho mọi trường hợp
*/
