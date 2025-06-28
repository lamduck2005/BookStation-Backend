# 🚀 EVENT SYSTEM - EXAMPLES

## 📋 **VÍ DỤ CỤ THỂ CÁC LOẠI SỰ KIỆN:**

---

## 🎯 **1. SỰ KIỆN ĐIỂM DANH HÀNG NGÀY**

### **Setup:**
```json
// EventCategory
{
  "categoryName": "Hoạt động Tích điểm",
  "description": "Check-in, điểm danh nhận point", 
  "iconUrl": "/icons/checkin.svg"
}

// Event  
{
  "eventName": "Check-in hàng ngày tháng 7",
  "eventType": "DAILY_CHECKIN",
  "eventCategoryId": 6,
  "rules": "Điểm danh 1 lần/ngày, nhận 10 point",
  "startDate": "2025-07-01",
  "endDate": "2025-07-31",
  "isOnline": true
}

// EventGift
{
  "giftName": "10 Point hàng ngày",
  "giftType": "POINT",
  "pointValue": 10,
  "totalQuantity": 31000, // 1000 users × 31 ngày
  "isActive": true
}
```

### **User Journey:**
```
1. User join event → status: JOINED
2. Mỗi ngày user bấm "Điểm danh" → API /complete → status: COMPLETED  
3. System auto cộng 10 point
4. Reset lại status JOINED cho ngày hôm sau (business logic)
```

---

## 📚 **2. THỬ THÁCH ĐỌC SÁCH**

### **Setup:**
```json
// Event
{
  "eventName": "Thử thách đọc 30 cuốn trong 3 tháng",
  "eventType": "READING_CHALLENGE", 
  "eventCategoryId": 2, // "Thử thách Đọc sách"
  "rules": "Đọc 30 cuốn, review tối thiểu 100 từ/cuốn",
  "maxParticipants": 500
}

// Multiple EventGift
[
  {"giftName": "Voucher 100K", "condition": "Đọc 10 cuốn", "quantity": 500},
  {"giftName": "Kindle Paperwhite", "condition": "Đọc 20 cuốn", "quantity": 100}, 
  {"giftName": "Bộ sách bestseller", "condition": "Đọc 30 cuốn", "quantity": 50},
  {"giftName": "Gặp tác giả", "condition": "Top 3 review hay nhất", "quantity": 3}
]
```

---

## 🏆 **3. CUỘC THI REVIEW**

### **Setup:**
```json
// Event
{
  "eventName": "Cuộc thi Review sách hay nhất 2025",
  "eventType": "CONTEST",
  "eventCategoryId": 1, // "Cuộc thi Review"
  "rules": "Viết review 500+ từ, chọn sách từ danh sách",
  "startDate": "2025-07-01", 
  "endDate": "2025-07-31"
}

// EventGift
[
  {"giftName": "Macbook Pro", "condition": "Giải nhất", "quantity": 1},
  {"giftName": "iPad", "condition": "Giải nhì", "quantity": 1}, 
  {"giftName": "Voucher 500K", "condition": "Giải ba", "quantity": 1},
  {"giftName": "Voucher 100K", "condition": "Top 20", "quantity": 17}
]
```

---

## 👥 **4. GẶP GỠ TÁC GIẢ**

### **Setup:**
```json
// Event
{
  "eventName": "Gặp gỡ Nguyễn Nhật Ánh",
  "eventType": "AUTHOR_MEET",
  "eventCategoryId": 3, // "Gặp gỡ Tác giả"
  "location": "Nhà Văn hóa Thanh Niên, TP.HCM",
  "maxParticipants": 200,
  "isOnline": false
}

// EventGift
[
  {"giftName": "Vé tham dự + ký tặng", "condition": "100 người đầu", "quantity": 100},
  {"giftName": "Chụp ảnh với tác giả", "condition": "50 người đầu", "quantity": 50},
  {"giftName": "Bộ sách có chữ ký", "condition": "Tất cả người tham dự", "quantity": 200}
]
```

---

## 🎉 **5. SỰ KIỆN KHUYẾN MÃI MÙA**

### **Setup:**
```json
// Event  
{
  "eventName": "Black Friday 2025 - Săn sách giá sốc",
  "eventType": "SEASONAL_EVENT",
  "eventCategoryId": 4, // "Khuyến mãi Mùa"
  "rules": "Mua từ 3 cuốn được quay số may mắn",
  "startDate": "2025-11-29",
  "endDate": "2025-11-29"
}

// EventGift
[
  {"giftName": "iPhone 16", "condition": "Giải đặc biệt", "quantity": 1},
  {"giftName": "Voucher 1M", "condition": "Giải nhất", "quantity": 10},
  {"giftName": "Voucher 500K", "condition": "Giải nhì", "quantity": 50}, 
  {"giftName": "Voucher 100K", "condition": "Giải ba", "quantity": 500}
]
```

---

## 🎓 **6. WORKSHOP KỸ NĂNG**

### **Setup:**
```json
// Event
{
  "eventName": "Workshop: Kỹ năng đọc hiệu quả",
  "eventType": "WORKSHOP", 
  "eventCategoryId": 5, // "Workshop Kỹ năng"
  "location": "BookStation HCM",
  "maxParticipants": 30,
  "rules": "Tham dự đầy đủ 3 buổi"
}

// EventGift
[
  {"giftName": "Chứng chỉ hoàn thành", "condition": "Hoàn thành khóa học", "quantity": 30},
  {"giftName": "Bộ sách kỹ năng", "condition": "Điểm cao nhất", "quantity": 3},
  {"giftName": "50 Point thưởng", "condition": "Tham dự đủ", "quantity": 30}
]
```

---

## 🔄 **PARTICIPANT STATUS FLOW:**

```
JOINED ────────────▶ IN_PROGRESS ────────────▶ COMPLETED
  │                      │                        │
  │                      │                        ▼
  │                      ▼                   [Nhận Point/Quà]
  ▼                   FAILED                      
DROPPED              (không hoàn thành)

* JOINED: Vừa tham gia sự kiện
* IN_PROGRESS: Đang thực hiện nhiệm vụ (optional, có thể skip)
* COMPLETED: Hoàn thành → Điểm danh thành công → Nhận point
* FAILED: Không hoàn thành → Không được gì
```

## 💡 **BEST PRACTICES:**

✅ **Đặt tên event rõ ràng** (có thời gian, mục tiêu)  
✅ **Rules chi tiết** (điều kiện, cách thức, thời hạn)  
✅ **Phân tier quà** (từ dễ đến khó, nhiều người đến ít người)  
✅ **Backup plan** (tăng/giảm số lượng quà theo tình hình)  
✅ **Communication** (thông báo rõ ràng cho user)  

**→ Đa dạng, linh hoạt, phù hợp mọi nhu cầu business! 🎯**
