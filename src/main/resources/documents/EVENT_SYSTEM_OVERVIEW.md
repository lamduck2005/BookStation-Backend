# 🎯 TỔNG QUAN HỆ THỐNG EVENT - BOOKSTATION

## 📋 **6 BẢNG CHÍNH VÀ VAI TRÒ:**

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│  event_category │───▶│      event      │───▶│   event_gift    │
│   (Phân loại)   │    │   (Sự kiện)     │    │   (Quà tặng)    │
└─────────────────┘    └─────────────────┘    └─────────────────┘
                                │                        │
                                ▼                        ▼
                       ┌─────────────────┐    ┌─────────────────┐
                       │event_participant│───▶│event_gift_claim │
                       │  (Người tham gia)│   │ (Nhận quà)      │
                       └─────────────────┘    └─────────────────┘
                                │
                                ▼
                       ┌─────────────────┐
                       │  event_history  │
                       │  (Lịch sử)      │
                       └─────────────────┘
```

## 🔄 **QUY TRÌNH HOẠT ĐỘNG:**

### **1. Setup Event (Admin)**
```
1. Tạo EventCategory → "Hoạt động Tích điểm"
2. Tạo Event → "Check-in hàng ngày" 
3. Thêm EventGift → "10 point", "Voucher 50K"
```

### **2. User Tham gia**
```
1. User đăng ký → EventParticipant (status: JOINED)
2. User thực hiện → EventParticipant (status: IN_PROGRESS)  
3. User hoàn thành → EventParticipant (status: COMPLETED)
4. System cộng point tự động
```

### **3. User Nhận quà**
```
1. User claim → EventGiftClaim (status: PENDING)
2. Admin approve → EventGiftClaim (status: APPROVED)
3. Ship/pickup → EventGiftClaim (status: DELIVERED)
```

## 💡 **CÁC LOẠI SỰ KIỆN PHỔ BIẾN:**

| EventType | EventCategory | Ví dụ |
|-----------|---------------|-------|
| `DAILY_CHECKIN` | "Hoạt động Tích điểm" | Check-in hàng ngày |
| `POINT_EARNING` | "Hoạt động Tích điểm" | Làm nhiệm vụ nhận point |
| `LOYALTY_PROGRAM` | "Hoạt động Tích điểm" | VIP member rewards |
| `READING_CHALLENGE` | "Thử thách Đọc sách" | Đọc 30 cuốn/tháng |
| `CONTEST` | "Cuộc thi Review" | Viết review hay nhất |
| `AUTHOR_MEET` | "Gặp gỡ Tác giả" | Buổi ký tặng sách |
| `PROMOTION` | "Khuyến mãi Mùa" | Black Friday sale |

## 🎯 **ĐIỂM MẠNH HỆ THỐNG:**

✅ **Linh hoạt**: Tạo được mọi loại sự kiện  
✅ **Có kiểm soát**: Admin approve từng bước  
✅ **Audit trail**: Theo dõi được mọi thay đổi  
✅ **Multi-reward**: 1 event có nhiều quà  
✅ **Multi-participation**: 1 user tham gia nhiều event  

## 🚀 **TÍCH HỢP VỚI POINT SYSTEM:**

```java
// Tự động cộng point khi hoàn thành event
@Override
public ApiResponse<EventParticipant> completeTask(Integer participantId) {
    // 1. Update status = COMPLETED
    participant.setCompletionStatus(ParticipantStatus.COMPLETED);
    
    // 2. Auto add points
    pointService.addEventRewardPoint(
        participant.getUser().getEmail(), 
        participant.getEvent().getPointReward()
    );
    
    return success;
}
```

**→ Hoàn chỉnh, linh hoạt, dễ mở rộng! 💪**
