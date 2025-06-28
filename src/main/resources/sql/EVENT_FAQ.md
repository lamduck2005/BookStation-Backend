# ❓ EVENT SYSTEM - FAQ

## 🤔 **CÂU HỎI THƯỜNG GẶP:**

### **Q1: EventType và EventCategory khác nhau như thế nào?**
**A:** 
- **EventType** = Hình thức tổ chức (PROMOTION, CONTEST, WORKSHOP...) - Cố định
- **EventCategory** = Chủ đề nội dung (Sách thiếu nhi, Hoạt động tích điểm...) - Linh hoạt

**Ví dụ:** 
- Sự kiện "Điểm danh hàng ngày" → Type: `DAILY_CHECKIN`, Category: `Hoạt động tích điểm`
- Sự kiện "Cuộc thi review sách thiếu nhi" → Type: `CONTEST`, Category: `Sách thiếu nhi`

---

### **Q2: Làm sao tạo sự kiện "điểm danh nhận point"?**
**A:** 
```json
{
  "eventName": "Check-in hàng ngày",
  "eventType": "DAILY_CHECKIN",          // Điểm danh hàng ngày
  "eventCategoryId": 6,                  // "Hoạt động tích điểm"
  "rules": "Điểm danh 1 lần/ngày nhận 10 point",
  "isOnline": true
}
```

---

### **Q3: User điểm danh như thế nào?**
**A:** 
1. User join event → Status: `JOINED`
2. User bấm "Điểm danh" → API `/complete` → Status: `COMPLETED`  
3. System tự động cộng point

---

### **Q4: Tại sao cần 6 bảng, không gộp lại được không?**
**A:** Mỗi bảng có vai trò riêng:
- `event_category` → Phân loại (như danh mục sản phẩm)
- `event` → Thông tin sự kiện  
- `event_gift` → Quà tặng (1 event có nhiều quà)
- `event_participant` → Người tham gia (1 user tham gia nhiều event)
- `event_gift_claim` → Nhận quà (user chọn giao hàng/nhận tại shop)
- `event_history` → Audit trail (ai làm gì khi nào)

---

### **Q5: User có thể tham gia nhiều event cùng lúc không?**
**A:** Có! Ví dụ:
- Event 1: "Check-in hàng ngày" → Status: COMPLETED
- Event 2: "Thử thách đọc sách" → Status: IN_PROGRESS  
- Event 3: "Contest review" → Status: JOINED

---

### **Q6: Làm sao biết user đã điểm danh chưa?**
**A:** Check `completion_status` trong bảng `event_participant`:
- `JOINED` = Chưa điểm danh
- `IN_PROGRESS` = Đang thực hiện (optional)
- `COMPLETED` = Đã điểm danh → Nhận point
- `FAILED` = Không hoàn thành → Không được gì

---

### **Q7: Point được cộng tự động hay thủ công?**
**A:** Nên tự động! Khi user COMPLETED → System auto call API cộng point:
```java
pointService.addEventRewardPoint(userEmail, pointAmount, eventId);
```

---

### **Q8: Sự kiện có thể có nhiều loại quà không?**
**A:** Có! Ví dụ event "Thử thách đọc 50 cuốn":
- Đọc 10 cuốn → Voucher 50K
- Đọc 25 cuốn → Bookmark + Voucher 100K  
- Đọc 50 cuốn → Kindle + Bộ sách
- Top 3 → Gặp tác giả

---

### **Q9: User có thể từ chối nhận quà không?**
**A:** Có! Quà không tự động gửi, user phải:
1. Claim quà → `event_gift_claim` 
2. Chọn cách nhận (ship về nhà / nhận tại shop)
3. Admin approve và xử lý

---

### **Q10: Làm sao track ai thay đổi gì?**
**A:** Dùng bảng `event_history`:
```sql
-- Ai publish event?
-- Ai thay đổi số lượng quà?  
-- Khi nào event chuyển status?
SELECT * FROM event_history WHERE event_id = 1;
```

## 🎯 **KẾT LUẬN:**
Hệ thống Event của BookStation được thiết kế **linh hoạt, có kiểm soát và dễ mở rộng**. Mỗi thành phần đều có lý do tồn tại rõ ràng! 💪
