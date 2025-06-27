# 🎯 WORKFLOW HOÀN CHỈNH - VÍ DỤ ĐƠN GIẢN

## 📚 **SỰ KIỆN: "ĐĂNG REVIEW SÁCH - NHẬN QUÀ"**

### **🎪 MÔ TẢ SỰ KIỆN:**
> BookStation tổ chức sự kiện: "Viết review cho 3 cuốn sách bất kỳ, nhận ngay voucher 100K"

---

## 🗓️ **TIMELINE HOÀN CHỈNH (7 NGÀY):**

### **📅 NGÀY 1: ADMIN TẠO SỰ KIỆN**

#### **Bước 1.1: Tạo danh mục sự kiện**
```sql
-- Bảng: event_category
INSERT INTO event_category (category_name, description, created_at) VALUES
('Cuộc thi review', 'Các cuộc thi viết review sách', 1719417600000);
-- ID = 1
```

#### **Bước 1.2: Tạo sự kiện chính**
```sql
-- Bảng: event  
INSERT INTO event (event_name, description, event_category_id, status, start_date, end_date, max_participants, created_by) VALUES
('Đăng Review Nhận Quà - Tháng 7/2025', 
 'Viết review cho 3 cuốn sách bất kỳ và nhận voucher 100K', 
 1, 'PUBLISHED', 
 1719504000000, -- 27/06 20:00
 1720108800000, -- 04/07 20:00 (7 ngày sau)
 100, 1);
-- event_id = 1
```

#### **Bước 1.3: Tạo quà tặng**
```sql
-- Bảng: event_gift
INSERT INTO event_gift (event_id, gift_name, description, gift_type, quantity, remaining_quantity, created_at) VALUES
(1, 'Voucher giảm giá 100K', 'Voucher áp dụng cho đơn hàng từ 300K', 'VOUCHER', 100, 100, 1719417600000);
-- gift_id = 1
```

---

### **📅 NGÀY 2: USER THAM GIA**

#### **Bước 2.1: User đăng ký tham gia**
```sql
-- Bảng: event_participant
INSERT INTO event_participant (event_id, user_id, joined_at, completion_status) VALUES
(1, 101, 1719590400000, 'JOINED'); -- User Nguyễn Văn A tham gia
-- participant_id = 1
```

#### **Bước 2.2: Cập nhật số lượng tham gia**
```sql
UPDATE event SET current_participants = current_participants + 1 WHERE id = 1;
-- current_participants = 1
```

---

### **📅 NGÀY 3-5: USER THỰC HIỆN NHIỆM VỤ**

#### **User viết review dần dần:**
- Ngày 3: Review sách "Sapiens" ✅
- Ngày 4: Review sách "Atomic Habits" ✅  
- Ngày 5: Review sách "Rich Dad Poor Dad" ✅

---

### **📅 NGÀY 5: USER HOÀN THÀNH**

#### **Bước 3.1: Admin xác nhận hoàn thành**
```sql
-- Cập nhật trạng thái participant
UPDATE event_participant 
SET completion_status = 'COMPLETED',
    notes = 'Đã viết đủ 3 review chất lượng cao'
WHERE id = 1;
```

#### **Bước 3.2: User được quyền claim quà**
```sql
-- Hệ thống tự động tạo eligibility
INSERT INTO event_gift_claim (event_participant_id, event_gift_id, claimed_at, claim_status, delivery_method, auto_delivered) VALUES
(1, 1, 1719936000000, 'PENDING', 'DIGITAL_DELIVERY', 1);
-- claim_id = 1
```

#### **Bước 3.3: Thông báo cho user**
```
📱 Notification: "🎉 Chúc mừng! Bạn đã hoàn thành sự kiện. Voucher 100K đang chờ claim!"
```

---

### **📅 NGÀY 6: USER CLAIM QUÀ**

#### **Bước 4.1: User click "Claim Gift"**
```sql
-- User xác nhận muốn nhận voucher
UPDATE event_gift_claim 
SET claim_status = 'APPROVED',
    notes = 'User xác nhận claim voucher 100K'
WHERE id = 1;
```

#### **Bước 4.2: Hệ thống tự động cộng voucher**
```sql
-- Thêm voucher vào tài khoản user
INSERT INTO user_voucher (user_id, voucher_id, obtained_date, source_type, source_id) VALUES
(101, 1, 1720022400000, 'EVENT_GIFT', 1);

-- Cập nhật trạng thái claim hoàn thành
UPDATE event_gift_claim 
SET claim_status = 'DELIVERED',
    completed_at = 1720022400000,
    notes = 'Voucher đã được cộng vào tài khoản'
WHERE id = 1;

-- Giảm số lượng quà còn lại
UPDATE event_gift SET remaining_quantity = remaining_quantity - 1 WHERE id = 1;
-- remaining_quantity = 99
```

#### **Bước 4.3: Thông báo hoàn thành**
```
📱 Notification: "✅ Voucher 100K đã được thêm vào tài khoản. Chúc bạn mua sách vui vẻ!"
```

---

### **📅 NGÀY 7: KẾT THÚC SỰ KIỆN**

#### **Bước 5.1: Admin kết thúc sự kiện**
```sql
UPDATE event 
SET status = 'COMPLETED',
    updated_at = 1720108800000
WHERE id = 1;
```

#### **Bước 5.2: Ghi lại lịch sử**
```sql
INSERT INTO event_history (event_id, action_type, description, performed_by, created_at, new_values) VALUES
(1, 'COMPLETED', 'Sự kiện kết thúc thành công', 1, 1720108800000, 
 '{"total_participants": 1, "completed": 1, "gifts_claimed": 1, "remaining_gifts": 99}');
```

---

## 📊 **TỔNG KẾT SAU SỰ KIỆN:**

### **📈 Báo cáo nhanh:**
```sql
SELECT 
    e.event_name,
    e.status,
    e.current_participants,
    COUNT(CASE WHEN ep.completion_status = 'COMPLETED' THEN 1 END) as completed_users,
    COUNT(egc.id) as gifts_claimed,
    eg.remaining_quantity
FROM event e
LEFT JOIN event_participant ep ON e.id = ep.event_id
LEFT JOIN event_gift_claim egc ON ep.id = egc.event_participant_id
LEFT JOIN event_gift eg ON e.id = eg.event_id
WHERE e.id = 1
GROUP BY e.id, e.event_name, e.status, e.current_participants, eg.remaining_quantity;
```

**Kết quả:**
- Tên sự kiện: "Đăng Review Nhận Quà - Tháng 7/2025"
- Trạng thái: COMPLETED
- Người tham gia: 1
- Hoàn thành: 1  
- Quà đã claim: 1
- Quà còn lại: 99

---

## 🎯 **SUMMARY - 6 BẢNG QUAN TRỌNG:**

| Bảng | Vai trò | Dữ liệu trong ví dụ |
|-------|---------|---------------------|
| **event_category** | Phân loại sự kiện | "Cuộc thi review" |
| **event** | Thông tin sự kiện chính | "Đăng Review Nhận Quà" |
| **event_gift** | Quà tặng sự kiện | "Voucher 100K" |
| **event_participant** | Người tham gia | User 101 - COMPLETED |
| **event_gift_claim** | Yêu cầu nhận quà | User claim voucher - DELIVERED |
| **event_history** | Lịch sử thay đổi | "Sự kiện kết thúc thành công" |

---

## 🔄 **WORKFLOW ĐƠN GIẢN HÓA:**

```
1. 👨‍💼 ADMIN TẠO SỰ KIỆN
   ├─ event_category: "Cuộc thi review"
   ├─ event: "Đăng Review Nhận Quà"  
   └─ event_gift: "Voucher 100K"

2. 👤 USER THAM GIA
   └─ event_participant: User 101 JOINED

3. 👤 USER HOÀN THÀNH NHIỆM VỤ
   └─ event_participant: User 101 COMPLETED

4. 👤 USER CLAIM QUÀ
   ├─ event_gift_claim: PENDING → DELIVERED
   └─ user_voucher: Voucher được cộng

5. 👨‍💼 ADMIN KẾT THÚC
   ├─ event: COMPLETED
   └─ event_history: Ghi lại kết quả
```

---

## 💡 **ĐIỂM QUAN TRỌNG:**

### **✅ Đơn giản thế này thôi!**
- **1 sự kiện** = **1 nhiệm vụ** = **1 quà**
- **User làm xong** → **Claim** → **Nhận quà**
- **Admin theo dõi** từ đầu đến cuối

### **🎯 Các bảng phụ:**
- **EventGiftClaim**: Không tự động gửi, để user chọn
- **EventHistory**: Audit trail để biết ai làm gì khi nào

**→ Thế thôi! Rất đơn giản phải không? 😊**
