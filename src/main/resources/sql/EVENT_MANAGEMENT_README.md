# 📚 HỆ THỐNG QUẢN LÝ SỰ KIỆN BOOKSTATION - HƯỚNG DẪN CHI TIẾT

## 🎯 TỔNG QUAN
Hệ thống quản lý sự kiện cho phép BookStation tổ chức các sự kiện đa dạng như thử thách đọc sách, gặp gỡ tác giả, khuyến mãi và trao quà tặng cho người tham gia.

---

## 📊 CẤU TRÚC BẢNG VÀ MỐI QUAN HỆ

### 1. **event_category** (Danh mục sự kiện)
- Phân loại các loại sự kiện: Ra mắt sách, Gặp gỡ tác giả, Thử thách đọc...
- Mỗi category có icon và mô tả riêng

### 2. **event** (Sự kiện chính) 
- Thông tin chi tiết sự kiện: tên, mô tả, thời gian, địa điểm, luật chơi
- Trạng thái: DRAFT → PUBLISHED → ONGOING → COMPLETED/CANCELLED
- Giới hạn số người tham gia và theo dõi số người hiện tại

### 3. **event_gift** (Quà tặng)
- Các loại quà: Sách, Voucher, Điểm thưởng, Vật phẩm
- Quản lý số lượng và số lượng còn lại
- Liên kết với Book/Voucher nếu quà là sách/voucher

### 4. **event_participant** (Người tham gia)
- Theo dõi người đăng ký và trạng thái hoàn thành
- Xác định người thắng cuộc và quà nhận được

### 5. **event_gift_claim** (Nhận quà)
- Chi tiết việc claim và giao quà
- Theo dõi trạng thái giao hàng và địa chỉ

### 6. **event_history** (Lịch sử)
- Audit trail tất cả thay đổi của sự kiện
- Lưu trữ giá trị cũ/mới dạng JSON

---

## 🔄 LUỒNG HOẠT ĐỘNG THỰC TẾ

### **GIAI ĐOẠN 1: TẠO SỰ KIỆN**
```
Admin tạo sự kiện "Thử thách đọc sách mùa hè 2025"
├── Chọn category: "Thử thách đọc sách"  
├── Thiết lập thông tin: thời gian, luật chơi, giới hạn người tham gia
├── Tạo các phần quà:
│   ├── Bộ sách bestseller (100 phần)
│   ├── Voucher 500K (50 phần - top 50)  
│   ├── Điểm thưởng VIP (1000 phần - tất cả)
│   ├── Kindle (3 phần - review hay nhất)
│   └── Bookmark + điểm (200 phần - an ủi)
└── Publish sự kiện → Trạng thái: PUBLISHED
```

### **GIAI ĐOẠN 2: NGƯỜI DÙNG THAM GIA**
```
User 1 (27/06): Đăng ký tham gia
├── INSERT vào event_participant
├── UPDATE current_participants += 1
└── Trạng thái: JOINED

User 2,3,4,5: Lần lượt đăng ký...
└── Tổng cộng 5 người tham gia
```

### **GIAI ĐOẠN 3: QUÁ TRÌNH THỰC HIỆN**
```
User đọc sách và hoàn thành thử thách:

User 1 (15/08): ✅ Hoàn thành 12 cuốn - COMPLETED (người đầu tiên!)
User 2 (20/08): ✅ Hoàn thành 11 cuốn - COMPLETED  
User 5 (25/08): ✅ Hoàn thành 13 cuốn - COMPLETED
User 3 (30/08): ⚠️ Chỉ 6 cuốn - PARTIAL (chưa đủ)
User 4 (giữa chừng): ❌ Bỏ cuộc - DROPPED
```

### **GIAI ĐOẠN 4: TRAO GIẢI VÀ PHÂN PHÁT QUÀ**
```
Admin xác định người thắng cuộc (01/09):

User 1 - WINNER (hoàn thành đầu tiên + review hay nhất):
├── INSERT event_gift_claim: Bộ sách bestseller
├── INSERT event_gift_claim: Voucher 500K (top 50)
├── INSERT event_gift_claim: Điểm VIP 2000
├── INSERT event_gift_claim: Kindle Paperwhite
└── UPDATE is_winner = true, completion_status = 'WINNER'

User 2 - Top 50:
├── INSERT event_gift_claim: Voucher 500K  
└── INSERT event_gift_claim: Điểm VIP 2000

User 5 - Hoàn thành:
└── INSERT event_gift_claim: Điểm VIP 2000

User 3 - Quà an ủi (đọc 6 cuốn):
└── INSERT event_gift_claim: Bookmark + 500 điểm

User 4 - Không nhận quà (bỏ cuộc)
```

### **GIAI ĐOẠN 5: GIAO HÀNG VÀ HOÀN TẤT**
```
Theo dõi giao quà:

03/09: User 1 nhận bộ sách
├── UPDATE delivery_status = 'DELIVERED'
├── UPDATE delivered_at = timestamp
└── UPDATE notes += "Giao thành công"

05/09: User 1 nhận Kindle  
├── UPDATE delivery_status = 'DELIVERED'
└── Customer feedback: "Rất hài lòng!"

Điểm thưởng VIP:
└── Tự động cộng vào tài khoản (delivery_status = 'DELIVERED')
```

### **GIAI ĐOẠN 6: KẾT THÚC SỰ KIỆN**
```
01/09: Cập nhật trạng thái sự kiện
├── UPDATE event SET status = 'COMPLETED'
├── INSERT event_history: Ghi lại kết quả tổng quan
└── Báo cáo: 3/5 người hoàn thành, 8 quà được trao
```

---

## 📈 BÁO CÁO VÀ THỐNG KÊ

### **Query xem tổng quan sự kiện:**
```sql
SELECT 
    e.event_name,
    e.current_participants,
    COUNT(CASE WHEN ep.completion_status IN ('COMPLETED','WINNER') THEN 1 END) as completed,
    COUNT(egc.id) as total_gifts_claimed,
    SUM(eg.gift_value) as total_gift_value
FROM event e
LEFT JOIN event_participant ep ON e.id = ep.event_id  
LEFT JOIN event_gift_claim egc ON ep.id = egc.event_participant_id
LEFT JOIN event_gift eg ON egc.event_gift_id = eg.id
WHERE e.id = 1
GROUP BY e.id, e.event_name, e.current_participants;
```

### **Query xem chi tiết người tham gia:**
```sql
SELECT 
    u.full_name,
    ep.completion_status,
    ep.is_winner,
    STRING_AGG(eg.gift_name, ', ') as gifts_received
FROM event_participant ep
JOIN [user] u ON ep.user_id = u.id
LEFT JOIN event_gift_claim egc ON ep.id = egc.event_participant_id
LEFT JOIN event_gift eg ON egc.event_gift_id = eg.id
WHERE ep.event_id = 1
GROUP BY u.full_name, ep.completion_status, ep.is_winner;
```

---

## 💡 TÍNH NĂNG NỔI BẬT

### 🎁 **Quà tặng linh hoạt**
- **Sách**: Liên kết trực tiếp với bảng Book
- **Voucher**: Liên kết với bảng Voucher hiện có  
- **Điểm thưởng**: Tự động cộng vào tài khoản
- **Vật phẩm**: Bookmark, Kindle, merchandise...

### 📊 **Theo dõi chi tiết**
- Lịch sử tham gia và hoàn thành
- Trạng thái giao quà realtime
- Audit trail đầy đủ

### 🏆 **Xếp hạng và trao giải**
- Top performer (hoàn thành sớm nhất)
- Best reviewer (review chất lượng cao)
- Participation rewards (quà an ủi)

### 🌐 **Hỗ trợ đa dạng**
- Sự kiện online/offline
- Giới hạn số lượng tham gia
- Thời gian linh hoạt

---

## 🚀 KẾT QUẢ MONG ĐỢI

### **Tăng engagement:**
- Khuyến khích người dùng đọc nhiều sách hơn
- Tạo cộng đồng yêu sách tích cực
- Tăng tương tác và review sách

### **Tăng doanh thu:**
- Thúc đẩy mua sách để tham gia sự kiện
- Sử dụng voucher tạo ra đơn hàng lớn hơn
- Khách hàng trung thành qua hệ thống điểm

### **Xây dựng thương hiệu:**
- BookStation = nơi có nhiều hoạt động thú vị
- Reputation về chất lượng sự kiện
- Word-of-mouth marketing hiệu quả

---

## 🛠️ TECHNICAL NOTES

### **Timestamps:**
- Tất cả thời gian được lưu dạng `BIGINT` (milliseconds)
- Tương thích với `System.currentTimeMillis()` trong Java
- Dễ dàng convert sang các định dạng khác nhau

### **Performance:**
- Index trên các trường quan trọng: event_id, user_id, status
- Cascade delete đảm bảo data integrity
- Soft delete cho EventCategory (is_active flag)

### **Extensibility:**
- EventHistory hỗ trợ JSON để lưu complex data
- EventType enum có thể mở rộng thêm loại sự kiện
- Gift system linh hoạt với multiple types

---

**💪 Hệ thống này sẽ biến BookStation thành một cộng đồng sách sôi động với nhiều hoạt động thú vị và ý nghĩa!**
