# 🎯 PHÂN TÍCH CHI TIẾT MỤC ĐÍCH TỪNG BẢNG



## 📋 **TÓM TẮT 6 BẢNG CHÍNH:**

| STT | Tên Bảng | Vai Trò Chính | Tương Tự Như |
|-----|-----------|---------------|--------------|
| 1 | **event_category** | Phân loại sự kiện | Danh mục sản phẩm |
| 2 | **event** | Thông tin sự kiện | Sản phẩm |
| 3 | **event_gift** | Quà tặng của sự kiện | Chi tiết ưu đãi |
| 4 | **event_participant** | Người tham gia | Khách hàng đặt hàng |
| 5 | **event_gift_claim** | Yêu cầu nhận quà | Đơn hàng |
| 6 | **event_history** | Lịch sử thay đổi | Log hệ thống |

## 🎯 **MỤC ĐÍCH:** Xác nhận tính cần thiết của từng bảng trong hệ thống Event

---

## 📊 **1. BẢNG `event_category`**

### **🎪 Ý nghĩa:**
**Phân loại sự kiện theo NHÓM HOẠT ĐỘNG**

### **🤔 Tại sao cần bảng này?**

#### **❌ Nếu KHÔNG có `event_category`:**
```sql
-- Tất cả sự kiện nằm chung trong 1 bảng
SELECT * FROM event WHERE event_name LIKE '%review%';
SELECT * FROM event WHERE event_name LIKE '%đọc sách%';
SELECT * FROM event WHERE event_name LIKE '%gặp gỡ%';

-- ❌ Vấn đề:
-- 1. Khó filter theo loại sự kiện
-- 2. Không có template chung cho cùng loại
-- 3. Báo cáo phải rely vào text matching
-- 4. Không có branding/icon riêng cho từng loại
```

#### **✅ Với `event_category`:**
```sql
-- Dễ dàng filter và quản lý
SELECT * FROM event WHERE event_category_id = 1; -- Tất cả cuộc thi review
SELECT * FROM event WHERE event_category_id = 2; -- Tất cả sự kiện gặp gỡ tác giả

-- ✅ Lợi ích:
-- 1. Thống kê theo từng nhóm sự kiện
-- 2. Template UI riêng cho từng category (icon, màu sắc, layout)
-- 3. Rules chung cho cùng loại sự kiện
-- 4. Marketing campaign theo vertical
```

### **🎯 Ví dụ thực tế:**
```sql
-- BookStation có thể có các category:
INSERT INTO event_category VALUES
(1, 'Cuộc thi Review', 'Viết review sách nhận quà', '/icons/review.svg'),
(2, 'Thử thách Đọc sách', 'Đọc nhiều sách trong khoảng thời gian', '/icons/reading.svg'),
(3, 'Gặp gỡ Tác giả', 'Buổi giao lưu với tác giả', '/icons/author.svg'),
(4, 'Khuyến mãi Mùa', 'Giảm giá theo mùa/dịp lễ', '/icons/sale.svg'),
(5, 'Workshop Kỹ năng', 'Học kỹ năng đọc, viết', '/icons/workshop.svg');

-- Mỗi category có thể có:
-- - Icon riêng cho UI
-- - Template email riêng  
-- - Rules validation riêng
-- - Landing page layout riêng
```

### **💡 Kết luận về `event_category`:**
**✅ CẦN THIẾT** - Không thể thay thế bằng cách khác hiệu quả

---

## 📊 **2. BẢNG `event`**

### **🎪 Ý nghĩa:**
**Thông tin CHI TIẾT của từng sự kiện cụ thể**

### **🤔 Tại sao cần bảng này?**
**→ Đây là bảng CORE, không thể thiếu!**

### **🎯 Chứa thông tin gì?**
```sql
-- Thông tin cơ bản
event_name: "Thử thách đọc 50 cuốn sách 2025"
description: "Đọc 50 cuốn sách trong năm và nhận quà khủng"

-- Thời gian
start_date: 1704067200000  -- 01/01/2025
end_date: 1735689599000    -- 31/12/2025

-- Quy mô
max_participants: 1000
current_participants: 247

-- Trạng thái lifecycle
status: 'ONGOING' -- DRAFT → PUBLISHED → ONGOING → COMPLETED

-- Rules và điều kiện
rules: "1. Đăng ký trước 31/01\n2. Review tối thiểu 100 từ\n3. Chỉ tính sách mua từ BookStation"

-- Logistics
location: "Online - Toàn quốc"
is_online: true
```

### **💡 Kết luận về `event`:**
**✅ CORE TABLE** - Không thể thiếu

---

## 📊 **3. BẢNG `event_gift`**

### **🎪 Ý nghĩa:**
**Quản lý QUÀ TẶNG cho từng sự kiện**

### **🤔 Tại sao không gộp vào bảng `event`?**

#### **❌ Nếu gộp vào `event`:**
```sql
-- Bảng event sẽ phình to
ALTER TABLE event ADD COLUMN gift_name VARCHAR(255);
ALTER TABLE event ADD COLUMN gift_quantity INT;
ALTER TABLE event ADD COLUMN gift_description TEXT;
-- ... +10 columns nữa

-- ❌ Vấn đề:
-- 1. MỘT sự kiện chỉ có MỘT loại quà? → Sai!
-- 2. Không thể có quà theo tier (Bronze, Silver, Gold)
-- 3. Không flexible cho complex reward system
```

#### **✅ Với bảng `event_gift` riêng:**
```sql
-- Một sự kiện có thể có NHIỀU quà
INSERT INTO event_gift VALUES
(1, 1, 'Voucher 100K', 'Top 100 người đầu', 'VOUCHER', 100, 100),
(2, 1, 'Bộ sách bestseller', 'Top 50 người đầu', 'BOOK', 50, 50),  
(3, 1, 'Kindle Paperwhite', 'Top 10 người đầu', 'PHYSICAL_ITEM', 10, 10),
(4, 1, 'Điểm thưởng VIP', 'Tất cả người hoàn thành', 'POINT', 1000, 1000);

-- ✅ Lợi ích:
-- 1. Flexible reward tiers
-- 2. Dễ quản lý inventory cho từng loại quà
-- 3. Có thể add/remove quà mà không ảnh hưởng event
-- 4. Phân quyền: Staff chỉ quản lý gifts, không động vào event info
```

### **🎯 Ví dụ thực tế:**
```sql
-- Sự kiện "Thử thách đọc 50 cuốn" có reward system phức tạp:
-- 📖 Đọc 10 cuốn → Voucher 50K
-- 📖 Đọc 25 cuốn → Voucher 100K + Bookmark
-- 📖 Đọc 50 cuốn → Kindle + Bộ sách + Voucher 500K
-- 📖 Top 3 reviewer → Gặp gỡ tác giả nổi tiếng

-- Với bảng riêng → Dễ dàng quản lý!
```

### **💡 Kết luận về `event_gift`:**
**✅ CẦN THIẾT** - Không thể gộp vào `event`

---

## 📊 **4. BẢNG `event_participant`**

### **🎪 Ý nghĩa:**
**Theo dõi AI THAM GIA sự kiện và TRẠNG THÁI của họ**

### **🤔 Tại sao không dùng bảng `user` trực tiếp?**

#### **❌ Nếu thêm cột vào `user`:**
```sql
ALTER TABLE user ADD COLUMN current_event_id INT;
ALTER TABLE user ADD COLUMN event_status VARCHAR(20);

-- ❌ Vấn đề:
-- 1. User chỉ tham gia ĐƯỢC MỘT sự kiện cùng lúc? → Sai!
-- 2. Lịch sử tham gia sự kiện sẽ bị ghi đè
-- 3. Không track được timeline join/complete
-- 4. Không có notes/progress specific cho từng sự kiện
```

#### **✅ Với bảng `event_participant`:**
```sql
-- User 101 có thể tham gia đồng thời nhiều sự kiện:
INSERT INTO event_participant VALUES
(1, 1, 101, 1719504000000, 'COMPLETED', 'Hoàn thành xuất sắc'),    -- Event đọc sách
(2, 2, 101, 1719590400000, 'ONGOING', 'Đang tiến hành'),          -- Event review
(3, 3, 101, 1719676800000, 'JOINED', 'Vừa mới tham gia');        -- Event gặp tác giả

-- ✅ Lợi ích:
-- 1. Multi-event participation
-- 2. Track timeline và progress
-- 3. Notes riêng cho từng sự kiện
-- 4. Báo cáo engagement per event
-- 5. Lịch sử đầy đủ không bị mất
```

### **🎯 Thông tin quan trọng trong bảng:**
```sql
event_id: 1           -- Sự kiện nào
user_id: 101          -- User nào
joined_at: timestamp  -- Khi nào join
completion_status:    -- Trạng thái hiện tại
  'JOINED'     → Vừa tham gia
  'ONGOING'    → Đang thực hiện  
  'COMPLETED'  → Hoàn thành
  'WINNER'     → Thắng giải
  'DROPPED'    → Bỏ cuộc
is_winner: boolean    -- Có phải winner không
notes: text           -- Ghi chú riêng cho user này trong event này
```

### **💡 Kết luận về `event_participant`:**
**✅ CẦN THIẾT** - Không thể thay thế

---

## 📊 **5. BẢNG `event_gift_claim`**

### **🎪 Ý nghĩa:**
**Quản lý QUÁ TRÌNH NHẬN QUÀ của từng user**

### **🤔 Tại sao không tự động gửi quà?**
**(Đã giải thích chi tiết ở trên, tóm tắt lại:)**

#### **❌ Tự động gửi quà:**
- Không biết gửi về địa chỉ nào
- User có thể không muốn nhận
- Lãng phí chi phí logistics  
- Vi phạm GDPR (dùng thông tin không đồng ý)

#### **✅ Với `event_gift_claim`:**
```sql
-- User chủ động lựa chọn:
claim_status: 
  'PENDING'      → Chờ user claim
  'APPROVED'     → User đã claim, chờ xử lý
  'ORDER_CREATED'→ Đã tạo đơn hàng (nếu cần ship)
  'DELIVERED'    → Hoàn thành

delivery_method:
  'ONLINE_SHIPPING'  → Giao tận nhà (tạo Order)
  'STORE_PICKUP'     → Nhận tại cửa hàng (mã pickup)
  'DIGITAL_DELIVERY' → Quà số (auto)
  'DIRECT_HANDOVER'  → Trao tay (offline event)
```

### **💡 Kết luận về `event_gift_claim`:**
**✅ CẦN THIẾT** - Không thể tự động hóa hoàn toàn

---

## 📊 **6. BẢNG `event_history`**

### **🎪 Ý nghĩa:**
**AUDIT TRAIL - Lưu lại TẤT CẢ thay đổi quan trọng**

### **🤔 Có thực sự cần thiết?**

#### **❌ Nếu không có `event_history`:**
```sql
-- Khi có vấn đề, không thể trả lời:
-- "Ai đã thay đổi sự kiện này?"
-- "Khi nào status chuyển từ ONGOING → COMPLETED?"  
-- "Tại sao số lượng quà bị sai?"
-- "Event này đã được modify bao nhiêu lần?"

-- Compliance issue:
-- - Không có audit trail
-- - Không trace được accountability  
-- - Khó debug khi có lỗi
```

#### **✅ Với `event_history`:**
```sql
-- Track mọi thay đổi quan trọng:
INSERT INTO event_history VALUES
(1, 1, 'CREATED', 'Admin tạo sự kiện mới', 1, 1719417600000, NULL, '{"status":"DRAFT"}'),
(2, 1, 'PUBLISHED', 'Sự kiện được publish', 1, 1719504000000, '{"status":"DRAFT"}', '{"status":"PUBLISHED"}'),
(3, 1, 'COMPLETED', 'Sự kiện kết thúc', 1, 1720108800000, '{"status":"ONGOING"}', '{"status":"COMPLETED","participants":247}');

-- ✅ Lợi ích:
-- 1. Full audit trail
-- 2. Compliance với các chuẩn bảo mật
-- 3. Debug và troubleshoot
-- 4. Báo cáo cho management
-- 5. Accountability tracking
```

### **💡 Kết luận về `event_history`:**
**✅ CẦN THIẾT** - Compliance và troubleshooting

---

## 🎯 **KẾT LUẬN CUỐI CÙNG:**

### **📊 BẢNG NÀO CÓ THỂ BỎ?**

| Bảng | Cần thiết? | Lý do |
|------|------------|-------|
| **event_category** | ✅ **BẮT BUỘC** | Không thể group/filter hiệu quả |
| **event** | ✅ **CORE** | Bảng chính, không thể thiếu |
| **event_gift** | ✅ **BẮT BUỘC** | Multi-reward system cần flexibility |
| **event_participant** | ✅ **BẮT BUỘC** | Multi-event participation + timeline |
| **event_gift_claim** | ✅ **BẮT BUỘC** | User choice + compliance |
| **event_history** | ✅ **BẮT BUỘC** | Audit trail + compliance |

### **🚀 KẾT LUẬN:**
**TẤT CẢ 6 BẢNG ĐỀU CẦN THIẾT!** 

Mỗi bảng giải quyết một vấn đề cụ thể mà không thể merge hoặc thay thế bằng cách khác hiệu quả.

### **💡 Analogy:**
```
Giống như một công ty:
- event_category = Phòng ban (Marketing, Sales, Tech...)
- event = Dự án cụ thể  
- event_gift = Budget/reward cho dự án
- event_participant = Nhân viên tham gia dự án
- event_gift_claim = Quy trình nhận thưởng
- event_history = Meeting minutes/audit log
```

**→ Bỏ bảng nào cũng làm hệ thống thiếu chức năng quan trọng! 💪**
