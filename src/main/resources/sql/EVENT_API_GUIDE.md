# 🎯 API REFERENCE - EVENT SYSTEM

## 📋 **QUICK START:**

### **1. Tạo EventCategory**
```http
POST /api/event-categories
Content-Type: application/json

{
  "categoryName": "Hoạt động Tích điểm",
  "description": "Check-in hàng ngày, điểm danh nhận point",
  "iconUrl": "/icons/checkin.svg",
  "isActive": true
}
```

### **2. Tạo Event**
```http
POST /api/events
Content-Type: application/json

{
  "eventName": "Check-in hàng ngày tháng 7",
  "description": "Điểm danh mỗi ngày nhận 10 point",
  "eventType": "DAILY_CHECKIN",
  "eventCategoryId": 1,
  "status": "PUBLISHED",
  "startDate": 1719590400000,
  "endDate": 1722268800000,
  "maxParticipants": 1000,
  "rules": "1 lần điểm danh/ngày, nhận 10 point/lần",
  "isOnline": true
}
```

### **3. User Tham gia**
```http
POST /api/admin/event-participants/{eventId}/join?userId={userId}
```

### **4. User Điểm danh (Hoàn thành)**
```http
POST /api/admin/event-participants/{participantId}/complete
```

### **5. Cộng Point**
```http
POST /api/points
Content-Type: application/json

{
  "email": "user@example.com",
  "pointEarned": 10,
  "description": "Điểm danh hàng ngày - Ngày 28/06/2025"
}
```

## 🔄 **WORKFLOW "ĐIỂM DANH NHẬN POINT":**

```
1. Admin tạo Event (eventType: PROMOTION, category: Tích điểm)
   ↓
2. User join event → EventParticipant (status: JOINED)
   ↓  
3. User điểm danh → API /complete → (status: COMPLETED)
   ↓
4. System auto cộng point → User nhận được point
   ↓
5. User có thể claim quà (nếu có)
```

## 📊 **ENUM VALUES:**

### **EventType:**
```java
BOOK_LAUNCH,        // Sự kiện ra mắt sách mới
AUTHOR_MEET,        // Gặp gỡ tác giả
READING_CHALLENGE,  // Thử thách đọc sách
BOOK_FAIR,          // Hội chợ sách
SEASONAL_EVENT,     // Sự kiện theo mùa
PROMOTION,          // Sự kiện khuyến mãi
CONTEST,            // Cuộc thi
WORKSHOP,           // Hội thảo
DAILY_CHECKIN,      // Điểm danh hàng ngày ⭐
LOYALTY_PROGRAM,    // Chương trình khách hàng thân thiết ⭐
POINT_EARNING,      // Sự kiện tích điểm ⭐
OTHER               // Khác
```

### **EventStatus:**
```java
DRAFT,              // Bản nháp
PUBLISHED,          // Đã công bố
ONGOING,            // Đang diễn ra
COMPLETED,          // Đã kết thúc
CANCELLED           // Đã hủy
```

### **ParticipantStatus:**
```java
JOINED,             // Vừa tham gia
IN_PROGRESS,        // Đang thực hiện nhiệm vụ  
COMPLETED,          // Hoàn thành (đã điểm danh)
FAILED              // Không hoàn thành
```

## 🎯 **RESPONSE EXAMPLES:**

### **Tham gia thành công:**
```json
{
  "status": 201,
  "message": "Tham gia sự kiện thành công",
  "data": {
    "id": 1,
    "completionStatus": "JOINED",
    "joinedAt": 1719590400000
  }
}
```

### **Điểm danh thành công:**
```json
{
  "status": 200,
  "message": "Hoàn thành nhiệm vụ thành công", 
  "data": {
    "id": 1,
    "completionStatus": "COMPLETED"
  }
}
```

## ⚠️ **LƯU Ý:**

- **eventType** = Hình thức tổ chức (cố định)
- **eventCategory** = Chủ đề nội dung (linh hoạt)
- **Điểm danh** = Chuyển status từ JOINED → COMPLETED
- **Point** được cộng tự động sau khi COMPLETED
