# 📚 HỆ THỐNG MINIGAME MỞ HỘP - API DOCUMENTATION

## 🎯 LUỒNG HỆ THỐNG

### 📊 QUẢN LÝ ADMIN
1. **Tạo chiến dịch** → **Thêm phần thưởng** → **Bật chiến dịch** → **Theo dõi thống kê**

### 🎮 CLIENT (MỞ HỘP)  
1. **Xem danh sách chiến dịch** → **Chọn chiến dịch** → **Mở hộp** → **Nhận thưởng tự động** → **Xem lịch sử**

---

## 🔗 API ENDPOINTS

### 1. QUẢN LÝ CHIẾN DỊCH

#### **GET** `/api/campaigns` - Danh sách chiến dịch (Admin)
**Parameters:**
- `page` (int, default: 0)
- `size` (int, default: 10) 
- `name` (string, optional) - Tìm kiếm theo tên
- `status` (byte, optional) - Lọc theo trạng thái (1: active, 0: inactive)

**Response:**
```json
{
  "content": [
    {
      "id": 1,
      "name": "Hộp tháng 8",
      "startDate": 1692556800000,
      "endDate": 1695235200000,
      "status": 1,
      "configFreeLimit": 3,
      "configPointCost": 100,
      "description": "Chiến dịch mở hộp tháng 8",
      "totalParticipants": 150,
      "totalOpened": 450,
      "totalRewards": 1000,
      "remainingRewards": 750,
      "createdAt": 1692556800000,
      "updatedAt": 1692556800000
    }
  ],
  "page": 0,
  "size": 10,
  "totalElements": 5,
  "totalPages": 1
}
```

#### **GET** `/api/campaigns/active` - Danh sách chiến dịch đang hoạt động (Client)
**Response:**
```json
{
  "status": 200,
  "message": "Lấy danh sách chiến dịch hoạt động thành công",
  "data": [
    {
      "id": 1,
      "name": "Hộp tháng 8", 
      "startDate": 1692556800000,
      "endDate": 1695235200000,
      "configFreeLimit": 3,
      "configPointCost": 100,
      "description": "Chiến dịch mở hộp tháng 8",
      "rewards": [
        {
          "id": 1,
          "type": "VOUCHER",
          "name": "Voucher 50K",
          "probability": 5.0,
          "quantityRemaining": 100
        }
      ]
    }
  ]
}
```

#### **GET** `/api/campaigns/{id}?userId={userId}` - Chi tiết chiến dịch
**Response:**
```json
{
  "status": 200,
  "message": "Lấy thông tin chiến dịch thành công",
  "data": {
    "id": 1,
    "name": "Hộp tháng 8",
    "userFreeOpenedCount": 1,
    "userTotalOpenedCount": 5,
    "userRemainingFreeOpens": 2,
    "rewards": [...]
  }
}
```

#### **POST** `/api/campaigns` - Tạo chiến dịch mới (Admin)
**Request Body:**
```json
{
  "name": "Hộp tháng 9",
  "startDate": 1695235200000,
  "endDate": 1697913600000,
  "configFreeLimit": 5,
  "configPointCost": 50,
  "description": "Chiến dịch mở hộp tháng 9", 
  "status": 1,
  "createdBy": 1
}
```

#### **PUT** `/api/campaigns` - Cập nhật chiến dịch (Admin)
**Request Body:** (Giống POST nhưng có thêm `id`)

#### **PATCH** `/api/campaigns/status` - Bật/tắt chiến dịch (Admin)
**Parameters:**
- `id` (int) - ID chiến dịch
- `status` (byte) - Trạng thái (1: bật, 0: tắt)

#### **DELETE** `/api/campaigns/{id}` - Xóa chiến dịch (Admin)

---

### 2. QUẢN LÝ PHẦN THƯỞNG

#### **GET** `/api/rewards/campaign/{campaignId}` - Danh sách phần thưởng của chiến dịch
**Response:**
```json
{
  "status": 200,
  "message": "Lấy danh sách phần thưởng thành công",
  "data": [
    {
      "id": 1,
      "campaignId": 1,
      "type": "VOUCHER",
      "name": "Voucher giảm 50K",
      "description": "Voucher giảm 50.000đ cho đơn từ 200K",
      "voucherId": 10,
      "voucherCode": "SALE50K",
      "voucherName": "Voucher 50K",
      "quantityTotal": 200,
      "quantityRemaining": 150,
      "probability": 5.0,
      "status": 1,
      "distributedCount": 50,
      "distributedPercentage": 25.0
    },
    {
      "id": 2,
      "campaignId": 1,
      "type": "POINTS", 
      "name": "Thưởng 100 điểm",
      "pointValue": 100,
      "quantityTotal": 500,
      "quantityRemaining": 400,
      "probability": 15.0,
      "status": 1
    },
    {
      "id": 3,
      "campaignId": 1,
      "type": "NONE",
      "name": "Chúc bạn may mắn lần sau", 
      "probability": 80.0,
      "quantityTotal": 9999,
      "quantityRemaining": 9999,
      "status": 1
    }
  ]
}
```

#### **POST** `/api/rewards` - Tạo phần thưởng mới (Admin)
**Request Body:**
```json
{
  "campaignId": 1,
  "type": "VOUCHER",
  "name": "Voucher VIP 100K",
  "description": "Voucher giảm 100K cho đơn từ 500K",
  "voucherId": 15,
  "quantityTotal": 50,
  "probability": 2.0,
  "status": 1,
  "createdBy": 1
}
```

**Loại POINTS:**
```json
{
  "campaignId": 1,
  "type": "POINTS",
  "name": "Thưởng 200 điểm",
  "pointValue": 200,
  "quantityTotal": 100,
  "probability": 8.0,
  "status": 1,
  "createdBy": 1
}
```

**Loại NONE:**
```json
{
  "campaignId": 1,
  "type": "NONE", 
  "name": "Không trúng thưởng",
  "quantityTotal": 9999,
  "probability": 85.0,
  "status": 1,
  "createdBy": 1
}
```

#### **PUT** `/api/rewards` - Cập nhật phần thưởng (Admin)
#### **PATCH** `/api/rewards/status` - Bật/tắt phần thưởng (Admin)
#### **DELETE** `/api/rewards/{id}` - Xóa phần thưởng (Admin)

---

### 3. VOUCHER DROPDOWN

#### **GET** `/api/vouchers/dropdown?search={query}` - Dropdown voucher cho admin tạo phần thưởng
**Parameters:**
- `search` (string, optional) - Tìm theo mã hoặc tên voucher

**Response:**
```json
{
  "status": 200,
  "message": "Lấy danh sách voucher thành công",
  "data": [
    {
      "id": 10,
      "code": "SALE50K",
      "name": "Voucher giảm 50K", 
      "description": "Giảm 50.000đ cho đơn từ 200.000đ",
      "voucherCategory": "NORMAL",
      "discountType": "FIXED_AMOUNT",
      "discountAmount": 50000,
      "startTime": 1692556800000,
      "endTime": 1700332800000,
      "minOrderValue": 200000,
      "usageLimit": 1000,
      "usedCount": 25,
      "status": 1
    }
  ]
}
```

---

### 4. MINIGAME - MỞ HỘP

#### **POST** `/api/minigame/open-box` - 🎮 MỞ HỘP CHÍNH (Client)
**Request Body:**
```json
{
  "campaignId": 1,
  "userId": 123,
  "openType": "FREE"
}
```
hoặc
```json
{
  "campaignId": 1, 
  "userId": 123,
  "openType": "POINT"
}
```

**Response - TRÚNG THƯỞNG:**
```json
{
  "status": 200,
  "message": "Mở hộp thành công!",
  "data": {
    "success": true,
    "message": "Chúc mừng! Bạn đã trúng Voucher giảm 50K",
    "historyId": 456,
    "openType": "FREE",
    "openDate": 1692643200000,
    "pointsSpent": 0,
    "hasReward": true,
    "rewardType": "VOUCHER",
    "rewardName": "Voucher giảm 50K",
    "rewardDescription": "Giảm 50.000đ cho đơn từ 200.000đ",
    "rewardValue": 10,
    "voucherId": 10,
    "voucherCode": "SALE50K", 
    "voucherName": "Voucher 50K",
    "userRemainingFreeOpens": 2,
    "userCurrentPoints": 1500,
    "userTotalOpenedInCampaign": 2,
    "animationType": "big_win",
    "rewardImage": null
  }
}
```

**Response - KHÔNG TRÚNG:**
```json
{
  "status": 200,
  "message": "Mở hộp thành công!",
  "data": {
    "success": true,
    "message": "Chúc bạn may mắn lần sau!",
    "historyId": 457,
    "openType": "POINT",
    "openDate": 1692643260000,
    "pointsSpent": 100,
    "hasReward": false,
    "userRemainingFreeOpens": 2,
    "userCurrentPoints": 1400,
    "userTotalOpenedInCampaign": 3,
    "animationType": "lose"
  }
}
```

**Response - LỖI:**
```json
{
  "status": 400,
  "message": "Lỗi khi mở hộp: Bạn đã hết lượt mở miễn phí",
  "data": {
    "success": false,
    "message": "Bạn đã hết lượt mở miễn phí",
    "hasReward": false,
    "animationType": "error"
  }
}
```

#### **GET** `/api/minigame/history/user/{userId}?campaignId={campaignId}` - Lịch sử mở hộp của user
**Response:**
```json
{
  "status": 200,
  "message": "Lấy lịch sử mở hộp thành công", 
  "data": [
    {
      "id": 456,
      "userId": 123,
      "userName": "Lê Văn C",
      "campaignId": 1,
      "campaignName": "Hộp tháng 8",
      "openType": "FREE",
      "openDate": 1692643200000,
      "rewardId": 1,
      "rewardType": "VOUCHER",
      "rewardName": "Voucher giảm 50K",
      "rewardValue": 10,
      "voucherId": 10,
      "voucherCode": "SALE50K",
      "win": true,
      "displayResult": "Trúng: Voucher giảm 50K",
      "createdAt": 1692643200000
    }
  ]
}
```

#### **GET** `/api/minigame/stats/user/{userId}/campaign/{campaignId}` - Thống kê user trong chiến dịch
**Response:**
```json
{
  "status": 200,
  "message": "Lấy thống kê thành công",
  "data": {
    "userId": 123,
    "userName": "Lê Văn C",
    "campaignId": 1,
    "campaignName": "Hộp tháng 8",
    "freeOpenedCount": 1,
    "totalOpenedCount": 5,
    "remainingFreeOpens": 2,
    "totalWins": 2,
    "totalLoses": 3,
    "winRate": 40.0,
    "totalVouchersWon": 1,
    "totalPointsWon": 200,
    "totalPointsSpent": 400,
    "createdAt": 1692556800000,
    "updatedAt": 1692643200000
  }
}
```

---

## ⚙️ BUSINESS LOGIC

### 🎲 LOGIC MỞ HỘP
1. **Validate chiến dịch** → Kiểm tra thời gian và trạng thái
2. **Validate user** → Kiểm tra quyền và điều kiện mở hộp  
3. **Random phần thưởng** → Dựa trên xác suất + boost theo số lần mở
4. **Xử lý phần thưởng** → Tự động cộng điểm/voucher vào tài khoản
5. **Lưu lịch sử** → Tracking đầy đủ cho báo cáo

### 📈 TỶ LỆ TRÚNG THƯỞNG BOOST
- **Công thức:** `boost = min(totalOpenedCount * 0.5%, 20%)`
- **Ví dụ:** Mở 10 lần = +5% tỷ lệ trúng, tối đa 40 lần = +20%
- **Áp dụng:** Chỉ cho phần thưởng có giá trị (VOUCHER, POINTS), không áp dụng cho NONE

### 🎁 XỬ LÝ PHẦN THƯỞNG TỰ ĐỘNG
- **VOUCHER:** Tự động thêm vào `user_voucher`, user có thể sử dụng ngay
- **POINTS:** Tự động cộng vào `user.total_point` + tạo lịch sử trong `point`  
- **NONE:** Không có xử lý gì, chỉ hiển thị thông báo

### 🔒 VALIDATION
- **FREE:** Kiểm tra còn lượt free không (`freeOpenedCount < configFreeLimit`)
- **POINT:** Kiểm tra đủ điểm không (`user.totalPoint >= configPointCost`)
- **Campaign:** Phải đang active và trong thời gian hiệu lực
- **Reward:** Phải còn số lượng (`quantityRemaining > 0`)

---

## 📋 NOTES

### ⏱️ TIMESTAMP
- Tất cả thời gian đều sử dụng **milliseconds since Unix epoch**
- VD: `1692556800000` = 2023-08-21 00:00:00 UTC

### 🎨 ANIMATION TYPE
- `"win"` - Trúng thưởng điểm
- `"big_win"` - Trúng thưởng voucher  
- `"lose"` - Không trúng gì
- `"error"` - Lỗi hệ thống

### 📊 ENUM VALUES
- **RewardType:** `"VOUCHER"`, `"POINTS"`, `"NONE"`
- **BoxOpenType:** `"FREE"`, `"POINT"`
- **Status:** `1` = Active, `0` = Inactive
