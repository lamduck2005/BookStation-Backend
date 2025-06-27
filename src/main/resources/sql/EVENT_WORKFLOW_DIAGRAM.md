# 📊 WORKFLOW DIAGRAM - SIÊU ĐƠN GIẢN

## 🎯 **SỰ KIỆN: "VIẾT 3 REVIEW - NHẬN VOUCHER 100K"**

```
📅 TIMELINE 7 NGÀY:

NGÀY 1: SETUP SỰ KIỆN
┌─────────────────────────────────────────────────────┐
│ 👨‍💼 ADMIN                                            │
│ ┌─────────────┐    ┌─────────────┐    ┌───────────┐ │
│ │event_category│ ──>│    event    │ ──>│event_gift │ │
│ │"Cuộc thi"   │    │"Review sách"│    │"Voucher"  │ │
│ │   ID=1      │    │    ID=1     │    │   ID=1    │ │
│ └─────────────┘    └─────────────┘    └───────────┘ │
└─────────────────────────────────────────────────────┘

NGÀY 2: USER THAM GIA  
┌─────────────────────────────────────────────────────┐
│ 👤 USER 101 (Nguyễn Văn A)                          │
│ ┌─────────────────┐                                 │
│ │event_participant│ "JOINED"                        │
│ │ user_id = 101   │                                 │
│ │ event_id = 1    │                                 │
│ │     ID=1        │                                 │
│ └─────────────────┘                                 │
└─────────────────────────────────────────────────────┘

NGÀY 3-5: THỰC HIỆN NHIỆM VỤ
┌─────────────────────────────────────────────────────┐
│ 👤 USER VIẾT REVIEW                                  │
│ ✅ Review 1: "Sapiens"                              │
│ ✅ Review 2: "Atomic Habits"                        │
│ ✅ Review 3: "Rich Dad Poor Dad"                    │
└─────────────────────────────────────────────────────┘

NGÀY 5: HOÀN THÀNH & CLAIM
┌─────────────────────────────────────────────────────┐
│ 🎉 USER HOÀN THÀNH                                   │
│ ┌─────────────────┐    ┌─────────────────┐          │
│ │event_participant│    │event_gift_claim │          │
│ │    "COMPLETED"  │ ──>│   "PENDING"     │          │
│ │                 │    │     ID=1        │          │
│ └─────────────────┘    └─────────────────┘          │
└─────────────────────────────────────────────────────┘

NGÀY 6: NHẬN QUÀ
┌─────────────────────────────────────────────────────┐
│ 💳 TỰ ĐỘNG CỘNG VOUCHER                              │
│ ┌─────────────────┐    ┌─────────────────┐          │
│ │event_gift_claim │    │  user_voucher   │          │
│ │   "DELIVERED"   │ ──>│ voucher_id = 1  │          │
│ │                 │    │  user_id = 101  │          │
│ └─────────────────┘    └─────────────────┘          │
└─────────────────────────────────────────────────────┘

NGÀY 7: KẾT THÚC
┌─────────────────────────────────────────────────────┐
│ 🏁 ADMIN KẾT THÚC SỰ KIỆN                            │
│ ┌─────────────┐    ┌─────────────────┐              │
│ │    event    │    │ event_history   │              │
│ │"COMPLETED"  │ ──>│  "COMPLETED"    │              │
│ │             │    │     ID=1        │              │
│ └─────────────┘    └─────────────────┘              │
└─────────────────────────────────────────────────────┘
```

---

## 🔍 **DATA FLOW ĐƠN GIẢN:**

### **📊 Trạng thái các bảng sau sự kiện:**

```sql
-- 1. event_category
ID=1 | category_name="Cuộc thi review" | is_active=1

-- 2. event  
ID=1 | event_name="Đăng Review Nhận Quà" | status="COMPLETED" | current_participants=1

-- 3. event_gift
ID=1 | gift_name="Voucher 100K" | quantity=100 | remaining_quantity=99

-- 4. event_participant  
ID=1 | event_id=1 | user_id=101 | completion_status="COMPLETED"

-- 5. event_gift_claim
ID=1 | participant_id=1 | gift_id=1 | claim_status="DELIVERED" 

-- 6. event_history
ID=1 | event_id=1 | action_type="COMPLETED" | description="Sự kiện thành công"

-- 7. user_voucher (bảng có sẵn)
ID=1 | user_id=101 | voucher_id=1 | source_type="EVENT_GIFT"
```

---

## 🎯 **MAPPING THỰC TẾ:**

### **🏢 Góc nhìn Business:**
```
Tôi muốn tổ chức sự kiện review sách:
├─ Tạo event_category ✅
├─ Tạo event ✅  
├─ Tạo event_gift ✅
└─ Publish → User có thể tham gia ✅
```

### **👤 Góc nhìn User:**
```
Tôi thấy sự kiện hay, muốn tham gia:
├─ Đăng ký → event_participant ✅
├─ Làm nhiệm vụ → completion_status="COMPLETED" ✅
├─ Claim quà → event_gift_claim ✅
└─ Nhận voucher → user_voucher ✅
```

### **👨‍💼 Góc nhìn Admin:**
```
Tôi muốn theo dõi sự kiện:
├─ Xem số người tham gia → event.current_participants
├─ Xem ai hoàn thành → event_participant.completion_status  
├─ Xem ai claim quà → event_gift_claim.claim_status
└─ Xem lịch sử → event_history
```

---

## 💡 **KẾT LUẬN SIÊU ĐƠN GIẢN:**

### **🎪 Sự kiện = 6 bước:**
1. **Admin setup** (3 bảng: category, event, gift)
2. **User join** (1 bảng: participant)  
3. **User complete** (update participant)
4. **User claim** (1 bảng: gift_claim)
5. **Auto deliver** (update user_voucher)
6. **Admin close** (1 bảng: history)

### **📝 Nhớ công thức:**
```
1 EVENT = 1 GOAL + 1 REWARD + N USERS
```

**→ Workflow này áp dụng được cho MỌI loại sự kiện của BookStation! 🚀**

### **🔄 Các sự kiện khác chỉ thay đổi:**
- **Goal**: Review sách → Đọc 10 cuốn → Mua sách mới → Check-in cửa hàng
- **Reward**: Voucher → Điểm → Sách → Kindle  
- **Logic**: Vẫn 6 bước như trên!

**Rõ ràng chưa bạn? 😊**
