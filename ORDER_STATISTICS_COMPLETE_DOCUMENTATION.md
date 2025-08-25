# 📊 Order Statistics APIs - Complete Documentation for Frontend

## 🎯 Tổng quan
Hệ thống Order Statistics APIs cung cấp **2-tier architecture** để lấy thống kê đơn hàng:

1. **TIER 1 - Summary API**: Dữ liệu tổng quan cho charts/graphs
2. **TIER 2 - Details API**: Chi tiết đơn hàng khi click vào điểm cụ thể

---

## 🔥 TIER 1: Order Statistics Summary API

### **Endpoint**
```
GET /api/orders/statistics/summary
```

### **Parameters**

| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| `period` | String | ❌ | "day" | Loại thời gian: "day", "week", "month", "quarter", "year" |
| `fromDate` | Long | ❌ | null | Timestamp bắt đầu (milliseconds) - Bắt buộc khi dùng custom range |
| `toDate` | Long | ❌ | null | Timestamp kết thúc (milliseconds) - Bắt buộc khi dùng custom range |

### **🎮 Các trường hợp sử dụng**

#### **1. Daily Summary (7 ngày gần nhất)**
```javascript
// Request
GET /api/orders/statistics/summary?period=day

// Response Structure
{
  "status": 200,
  "message": "Lấy thống kê tổng quan thành công",
  "data": [
    // 7 records, mỗi record = 1 ngày
    {
      "date": "2025-08-25",
      "totalOrders": 45,
      "completedOrders": 38,
      "canceledOrders": 5,
      "refundedOrders": 2,
      "netRevenue": 2450000.00,
      "aov": 54444.44,
      "period": "daily",
      "dateRange": "2025-08-25",
      "startDate": "2025-08-25",
      "endDate": "2025-08-25"
    },
    {
      "date": "2025-08-24",
      "totalOrders": 52,
      "completedOrders": 41,
      "canceledOrders": 8,
      "refundedOrders": 3,
      "netRevenue": 2890000.00,
      "aov": 55576.92,
      "period": "daily",
      "dateRange": "2025-08-24",
      "startDate": "2025-08-24",
      "endDate": "2025-08-24"
    }
    // ... 5 records nữa
  ]
}
```

#### **2. Weekly Summary**
```javascript
// Request  
GET /api/orders/statistics/summary?period=week

// Response Structure
{
  "status": 200,
  "message": "Lấy thống kê tổng quan thành công",
  "data": [
    // Nhiều records, mỗi record = 1 tuần
    {
      "date": "2025-08-19",           // Monday của tuần
      "totalOrders": 320,
      "completedOrders": 285,
      "canceledOrders": 25,
      "refundedOrders": 10,
      "netRevenue": 18500000.00,
      "aov": 57812.50,
      "period": "weekly",
      "dateRange": "2025-08-19 to 2025-08-25",
      "weekNumber": 34,
      "year": 2025,
      "startDate": "2025-08-19",
      "endDate": "2025-08-25"
    },
    {
      "date": "2025-08-12",
      "totalOrders": 295,
      "completedOrders": 250,
      "canceledOrders": 30,
      "refundedOrders": 15,
      "netRevenue": 16200000.00,
      "aov": 54915.25,
      "period": "weekly",
      "dateRange": "2025-08-12 to 2025-08-18",
      "weekNumber": 33,
      "year": 2025,
      "startDate": "2025-08-12",
      "endDate": "2025-08-18"
    }
    // ... more weeks
  ]
}
```

#### **3. Monthly Summary**
```javascript
// Request
GET /api/orders/statistics/summary?period=month

// Response Structure
{
  "status": 200,
  "message": "Lấy thống kê tổng quan thành công",
  "data": [
    // Nhiều records, mỗi record = 1 tháng
    {
      "date": "2025-08-01",           // Ngày đầu tháng
      "totalOrders": 1250,
      "completedOrders": 1050,
      "canceledOrders": 150,
      "refundedOrders": 50,
      "netRevenue": 75000000.00,
      "aov": 60000.00,
      "period": "monthly",
      "dateRange": "August 2025",
      "monthNumber": 8,
      "monthName": "Tháng Tám",
      "year": 2025,
      "startDate": "2025-08-01",
      "endDate": "2025-08-31"
    },
    {
      "date": "2025-07-01",
      "totalOrders": 1180,
      "completedOrders": 980,
      "canceledOrders": 140,
      "refundedOrders": 60,
      "netRevenue": 68500000.00,
      "aov": 58050.85,
      "period": "monthly",
      "dateRange": "July 2025",
      "monthNumber": 7,
      "monthName": "Tháng Bảy",
      "year": 2025,
      "startDate": "2025-07-01",
      "endDate": "2025-07-31"
    }
    // ... more months
  ]
}
```

#### **4. Quarterly Summary**
```javascript
// Request
GET /api/orders/statistics/summary?period=quarter

// Response Structure
{
  "status": 200,
  "message": "Lấy thống kê tổng quan thành công",
  "data": [
    // Nhiều records, mỗi record = 1 quý
    {
      "date": "2025-07-01",           // Ngày đầu quý
      "totalOrders": 3850,
      "completedOrders": 3200,
      "canceledOrders": 450,
      "refundedOrders": 200,
      "netRevenue": 225000000.00,
      "aov": 58441.56,
      "period": "quarterly",
      "dateRange": "Quý 3 năm 2025",
      "quarter": 3,
      "year": 2025,
      "startDate": "2025-07-01",
      "endDate": "2025-09-30"
    },
    {
      "date": "2025-04-01",
      "totalOrders": 3650,
      "completedOrders": 3050,
      "canceledOrders": 420,
      "refundedOrders": 180,
      "netRevenue": 210000000.00,
      "aov": 57534.25,
      "period": "quarterly",
      "dateRange": "Quý 2 năm 2025",
      "quarter": 2,
      "year": 2025,
      "startDate": "2025-04-01",
      "endDate": "2025-06-30"
    }
    // ... more quarters
  ]
}
```

#### **5. Yearly Summary**
```javascript
// Request
GET /api/orders/statistics/summary?period=year

// Response Structure
{
  "status": 200,
  "message": "Lấy thống kê tổng quan thành công",
  "data": [
    // Nhiều records, mỗi record = 1 năm
    {
      "date": "2025-01-01",           // Ngày đầu năm
      "totalOrders": 15500,
      "completedOrders": 13200,
      "canceledOrders": 1800,
      "refundedOrders": 500,
      "netRevenue": 950000000.00,
      "aov": 61290.32,
      "period": "yearly",
      "dateRange": "Year 2025",
      "year": 2025,
      "startDate": "2025-01-01",
      "endDate": "2025-12-31"
    },
    {
      "date": "2024-01-01",
      "totalOrders": 14200,
      "completedOrders": 12000,
      "canceledOrders": 1700,
      "refundedOrders": 500,
      "netRevenue": 820000000.00,
      "aov": 57746.48,
      "period": "yearly",
      "dateRange": "Year 2024",
      "year": 2024,
      "startDate": "2024-01-01",
      "endDate": "2024-12-31"
    }
    // ... more years
  ]
}
```

#### **6. Custom Date Range**
```javascript
// Request - Lấy 30 ngày từ 1/8/2025 đến 30/8/2025
const fromDate = new Date('2025-08-01').getTime(); // 1722441600000
const toDate = new Date('2025-08-30').getTime();   // 1724947200000

GET /api/orders/statistics/summary?period=day&fromDate=${fromDate}&toDate=${toDate}

// Response Structure
{
  "status": 200,
  "message": "Lấy thống kê tổng quan thành công",
  "data": [
    // 30 records, mỗi record = 1 ngày trong khoảng thời gian
    {
      "date": "2025-08-30",
      "totalOrders": 48,
      "completedOrders": 42,
      "canceledOrders": 4,
      "refundedOrders": 2,
      "netRevenue": 2680000.00,
      "aov": 55833.33,
      "period": "daily",
      "dateRange": "2025-08-30",
      "startDate": "2025-08-30",
      "endDate": "2025-08-30"
    },
    {
      "date": "2025-08-29",
      "totalOrders": 51,
      "completedOrders": 45,
      "canceledOrders": 5,
      "refundedOrders": 1,
      "netRevenue": 2950000.00,
      "aov": 57843.14,
      "period": "daily",
      "dateRange": "2025-08-29",
      "startDate": "2025-08-29",
      "endDate": "2025-08-29"
    }
    // ... 28 records nữa từ 2025-08-28 xuống 2025-08-01
  ]
}
```

---

## 🔍 TIER 2: Order Statistics Details API

### **Endpoint**
```
GET /api/orders/statistics/details
```

### **Parameters**

| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| `period` | String | ✅ | - | "day", "week", "month", "quarter", "year" |
| `date` | Long | ✅ | - | Timestamp đại diện cho thời điểm cần xem |
| `limit` | Integer | ❌ | 10 | Số lượng đơn hàng muốn lấy |

### **🎮 Các trường hợp sử dụng**

#### **1. Xem đơn hàng trong ngày cụ thể**
```javascript
// Scenario: User click vào điểm ngày 25/8/2025 trên chart
const clickDate = new Date('2025-08-25 10:30:00').getTime();

// Request
GET /api/orders/statistics/details?period=day&date=${clickDate}&limit=20

// Response Structure
{
  "status": 200,
  "message": "Lấy chi tiết đơn hàng thành công",
  "data": [
    {
      "orderCode": "ORD-2025082501",
      "customerName": "Nguyễn Văn A",
      "customerEmail": "nguyenvana@email.com",
      "totalAmount": 550000.00,
      "orderStatus": "DELIVERED",
      "createdAt": 1724572800000,
      "productInfo": "Lập trình Java cơ bản (ISBN:978-123456789, ID:15), Spring Boot in Action (ISBN:978-987654321, ID:28)"
    },
    {
      "orderCode": "ORD-2025082502",
      "customerName": "Trần Thị B",
      "customerEmail": "tranthib@email.com",
      "totalAmount": 320000.00,
      "orderStatus": "PROCESSING",
      "createdAt": 1724569200000,
      "productInfo": "Clean Code (ISBN:978-111222333, ID:42)"
    }
    // ... 18 orders nữa
  ]
}
```

#### **2. Xem đơn hàng trong tuần**
```javascript
// Scenario: User click vào điểm tuần 34 năm 2025 trên chart
const weekTimestamp = new Date('2025-08-22').getTime();

// Request  
GET /api/orders/statistics/details?period=week&date=${weekTimestamp}&limit=15

// Response Structure
{
  "status": 200,
  "message": "Lấy chi tiết đơn hàng thành công",
  "data": [
    // 15 đơn hàng từ thứ 2 (19/8) đến chủ nhật (25/8)
    {
      "orderCode": "ORD-2025081901",
      "customerName": "Lê Minh C",
      "customerEmail": "leminhc@email.com",
      "totalAmount": 750000.00,
      "orderStatus": "DELIVERED",
      "createdAt": 1724054400000,
      "productInfo": "Design Patterns (ISBN:978-555666777, ID:67), The Pragmatic Programmer (ISBN:978-888999000, ID:89)"
    }
    // ... 14 orders nữa trong tuần
  ]
}
```

#### **3. Xem đơn hàng trong tháng**
```javascript
// Scenario: User click vào điểm tháng 8/2025 trên chart
const monthTimestamp = new Date('2025-08-15').getTime();

// Request
GET /api/orders/statistics/details?period=month&date=${monthTimestamp}&limit=50

// Response Structure
{
  "status": 200,
  "message": "Lấy chi tiết đơn hàng thành công",
  "data": [
    // 50 đơn hàng trong tháng 8/2025
    {
      "orderCode": "ORD-2025080101",
      "customerName": "Phạm Thu D",
      "customerEmail": "phamthud@email.com",
      "totalAmount": 1200000.00,
      "orderStatus": "DELIVERED",
      "createdAt": 1722441600000,
      "productInfo": "Effective Java (ISBN:978-123123123, ID:101), Spring Security (ISBN:978-456456456, ID:102), Database Design (ISBN:978-789789789, ID:103)"
    }
    // ... 49 orders nữa trong tháng
  ]
}
```

#### **4. Xem đơn hàng trong quý**
```javascript
// Scenario: User click vào điểm quý 3/2025 trên chart  
const quarterTimestamp = new Date('2025-08-01').getTime();

// Request
GET /api/orders/statistics/details?period=quarter&date=${quarterTimestamp}&limit=100

// Response Structure
{
  "status": 200,
  "message": "Lấy chi tiết đơn hàng thành công",
  "data": [
    // 100 đơn hàng trong quý 3 (7-9/2025)
    {
      "orderCode": "ORD-2025070101",
      "customerName": "Võ Văn E",
      "customerEmail": "vovane@email.com",
      "totalAmount": 950000.00,
      "orderStatus": "DELIVERED",
      "createdAt": 1719763200000,
      "productInfo": "Microservices Patterns (ISBN:978-321321321, ID:201), Docker Deep Dive (ISBN:978-654654654, ID:202)"
    }
    // ... 99 orders nữa trong quý
  ]
}
```

#### **5. Xem đơn hàng trong năm**
```javascript
// Scenario: User click vào điểm năm 2025 trên chart
const yearTimestamp = new Date('2025-06-01').getTime();

// Request
GET /api/orders/statistics/details?period=year&date=${yearTimestamp}&limit=200

// Response Structure
{
  "status": 200,
  "message": "Lấy chi tiết đơn hàng thành công",
  "data": [
    // 200 đơn hàng trong năm 2025
    {
      "orderCode": "ORD-2025010101",
      "customerName": "Hoàng Thị F",
      "customerEmail": "hoangthif@email.com",
      "totalAmount": 1500000.00,
      "orderStatus": "DELIVERED",
      "createdAt": 1704067200000,
      "productInfo": "System Design Interview (ISBN:978-111111111, ID:301), Kubernetes in Action (ISBN:978-222222222, ID:302), DevOps Handbook (ISBN:978-333333333, ID:303)"
    }
    // ... 199 orders nữa trong năm
  ]
}
```

---

## 🔥 COMPLETE Response Field Mapping

### **Summary API Response Fields**

| Field | Type | Description | Example |
|-------|------|-------------|---------|
| `date` | String | Ngày đại diện (YYYY-MM-DD) | "2025-08-25" |
| `totalOrders` | Integer | Tổng số đơn hàng trong period | 45 |
| `completedOrders` | Integer | Số đơn đã giao (DELIVERED) | 38 |
| `canceledOrders` | Integer | Số đơn đã hủy (CANCELED) | 5 |
| `refundedOrders` | Integer | Số đơn hoàn trả (PARTIALLY_REFUNDED + REFUNDED) | 2 |
| `netRevenue` | Double | Doanh thu thuần (VND) | 2450000.00 |
| `aov` | Double | Average Order Value (netRevenue/totalOrders) | 54444.44 |
| `period` | String | Loại period ("daily", "weekly", "monthly", etc.) | "daily" |
| `dateRange` | String | Mô tả khoảng thời gian | "2025-08-25" |
| `startDate` | String | Ngày bắt đầu period | "2025-08-25" |
| `endDate` | String | Ngày kết thúc period | "2025-08-25" |
| **Weekly-specific fields** |||
| `weekNumber` | Integer | Số tuần trong năm | 34 |
| `year` | Integer | Năm | 2025 |
| **Monthly-specific fields** |||
| `monthNumber` | Integer | Số tháng (1-12) | 8 |
| `monthName` | String | Tên tháng tiếng Việt | "Tháng Tám" |
| **Quarterly-specific fields** |||
| `quarter` | Integer | Quý (1-4) | 3 |

### **Details API Response Fields**

| Field | Type | Description | Example |
|-------|------|-------------|---------|
| `orderCode` | String | Mã đơn hàng | "ORD-2025082501" |
| `customerName` | String | Tên khách hàng | "Nguyễn Văn A" |
| `customerEmail` | String | Email khách hàng | "nguyenvana@email.com" |
| `totalAmount` | Double | Tổng giá trị đơn hàng (VND) | 550000.00 |
| `orderStatus` | String | Trạng thái đơn hàng | "DELIVERED" |
| `createdAt` | Long | Timestamp tạo đơn (milliseconds) | 1724572800000 |
| `productInfo` | String | Danh sách sản phẩm (Title, ISBN, ID) | "Lập trình Java cơ bản (ISBN:978-123456789, ID:15)" |

---

## 🚨 Error Handling & Status Codes

### **Success Response**
```json
{
  "status": 200,
  "message": "Lấy thống kê tổng quan thành công",
  "data": [...] // Array of data
}
```

### **Invalid Period**
```json
{
  "status": 400,
  "message": "Period không hợp lệ. Chỉ chấp nhận: day, week, month, quarter, year",
  "data": []
}
```

### **Missing Required Parameters (Details API)**
```json
{
  "status": 400,
  "message": "Tham số 'date' là bắt buộc cho API chi tiết",
  "data": []
}
```

### **Invalid Date Range - Period Duration Limits**

#### **🔥 Daily Period Validation**
```json
// Request: Khoảng thời gian < 1 ngày
{
  "status": 400,
  "message": "Khoảng thời gian quá nhỏ cho chế độ ngày (tối thiểu 1 ngày). Khoảng thời gian hiện tại: 0 ngày.",
  "data": []
}

// Request: Khoảng thời gian > 90 ngày  
{
  "status": 400,
  "message": "Khoảng thời gian quá lớn cho chế độ ngày (tối đa 90 ngày). Khoảng thời gian hiện tại: 120 ngày.",
  "data": []
}
```

#### **🔥 Weekly Period Validation**
```json
// Request: Khoảng thời gian < 7 ngày
{
  "status": 400,
  "message": "Khoảng thời gian quá nhỏ cho chế độ tuần (tối thiểu 7 ngày). Khoảng thời gian hiện tại: 5 ngày.",
  "data": []
}

// Request: Khoảng thời gian > 2 năm
{
  "status": 400,
  "message": "Khoảng thời gian quá lớn cho chế độ tuần (tối đa 2 năm). Khoảng thời gian hiện tại: 3 năm.",
  "data": []
}
```

#### **🔥 Monthly Period Validation**
```json
// Request: Khoảng thời gian < 28 ngày
{
  "status": 400,
  "message": "Khoảng thời gian quá nhỏ cho chế độ tháng (tối thiểu 28 ngày). Khoảng thời gian hiện tại: 20 ngày.",
  "data": []
}

// Request: Khoảng thời gian > 5 năm
{
  "status": 400,
  "message": "Khoảng thời gian quá lớn cho chế độ tháng (tối đa 5 năm). Khoảng thời gian hiện tại: 7 năm.",
  "data": []
}
```

#### **🔥 Quarterly Period Validation**
```json
// Request: Khoảng thời gian < 90 ngày
{
  "status": 400,
  "message": "Khoảng thời gian quá nhỏ cho chế độ quý (tối thiểu 90 ngày). Khoảng thời gian hiện tại: 60 ngày.",
  "data": []
}

// Request: Khoảng thời gian > 5 năm
{
  "status": 400,
  "message": "Khoảng thời gian quá lớn cho chế độ quý (tối đa 5 năm). Khoảng thời gian hiện tại: 8 năm.",
  "data": []
}
```

#### **🔥 Yearly Period Validation**
```json
// Request: Khoảng thời gian < 365 ngày
{
  "status": 400,
  "message": "Khoảng thời gian quá nhỏ cho chế độ năm (tối thiểu 365 ngày). Khoảng thời gian hiện tại: 200 ngày.",
  "data": []
}

// Request: Khoảng thời gian > 25 năm
{
  "status": 400,
  "message": "Khoảng thời gian quá lớn cho chế độ năm (tối đa 25 năm). Khoảng thời gian hiện tại: 30 năm.",
  "data": []
}
```

### **📊 Period Duration Limits Summary**

| Period Type | Minimum Duration | Maximum Duration | Example Valid Range |
|-------------|------------------|------------------|-------------------|
| **Daily** | 1 ngày | 90 ngày | 1-90 ngày |
| **Weekly** | 7 ngày (1 tuần) | 2 năm (730 ngày) | 1 tuần - 2 năm |
| **Monthly** | 28 ngày (1 tháng) | 5 năm (1825 ngày) | 1 tháng - 5 năm |
| **Quarterly** | 90 ngày (1 quý) | 5 năm (1825 ngày) | 1 quý - 5 năm |
| **Yearly** | 365 ngày (1 năm) | 25 năm (9125 ngày) | 1 năm - 25 năm |

### **🎯 Incomplete Period Handling**

**Quan trọng**: Khi khoảng thời gian không đủ 1 đơn vị period (ví dụ: chọn weekly nhưng chỉ có 5 ngày), hệ thống vẫn chấp nhận nếu đạt minimum requirements, nhưng `endDate` trong response sẽ phản ánh đúng khoảng thời gian thực tế.

#### **Ví dụ Weekly với khoảng thời gian thiếu:**
```javascript
// Request: period=week với 10 ngày (thay vì 14 ngày = 2 tuần đầy đủ)
const fromDate = new Date('2025-08-20').getTime(); // Thứ 3
const toDate = new Date('2025-08-29').getTime();   // Thứ 5 tuần sau

GET /api/orders/statistics/summary?period=week&fromDate=${fromDate}&toDate=${toDate}

// Response: Chỉ 1 week record, endDate chỉ đến 2025-08-29
{
  "status": 200,
  "data": [
    {
      "date": "2025-08-18",           // Monday của tuần chứa startDate
      "totalOrders": 85,
      "period": "weekly",
      "dateRange": "2025-08-18 to 2025-08-29",
      "weekNumber": 34,
      "year": 2025,
      "startDate": "2025-08-18",
      "endDate": "2025-08-29"        // ⚠️ Chỉ đến ngày 29, không phải 24 (Sunday)
    }
  ]
}
```

#### **Ví dụ Monthly với tháng không đầy đủ:**
```javascript
// Request: period=month với 15 ngày trong tháng 8
const fromDate = new Date('2025-08-10').getTime();
const toDate = new Date('2025-08-25').getTime();

// Response: endDate = 2025-08-25 thay vì 2025-08-31
{
  "status": 200,
  "data": [
    {
      "date": "2025-08-01",
      "period": "monthly",
      "dateRange": "August 2025",
      "startDate": "2025-08-01",
      "endDate": "2025-08-25"        // ⚠️ Phản ánh đúng range thực tế
    }
  ]
}
```

### **No Data Available**
```json
{
  "status": 200,
  "message": "Không có dữ liệu trong khoảng thời gian này",
  "data": []
}
```

### **Server Error**
```json
{
  "status": 500,
  "message": "Lỗi server khi xử lý thống kê",
  "data": []
}
```

---

## 💡 Frontend Integration Tips

### **1. Period Validation Before API Call**
```javascript
// Validation helper function - FE nên validate trước khi call API
function validatePeriodDateRange(period, fromDate, toDate) {
  const durationMs = toDate - fromDate;
  const durationDays = Math.floor(durationMs / (24 * 60 * 60 * 1000));
  const durationYears = Math.floor(durationDays / 365);
  
  const limits = {
    daily: { min: 1, max: 90, unit: 'ngày' },
    weekly: { min: 7, max: 730, unit: 'ngày' }, // 2 năm
    monthly: { min: 28, max: 1825, unit: 'ngày' }, // 5 năm  
    quarterly: { min: 90, max: 1825, unit: 'ngày' }, // 5 năm
    yearly: { min: 365, max: 9125, unit: 'ngày' } // 25 năm
  };
  
  const limit = limits[period.toLowerCase()];
  if (!limit) return { valid: false, message: "Period không hợp lệ" };
  
  if (durationDays < limit.min) {
    return { 
      valid: false, 
      message: `Khoảng thời gian quá nhỏ cho chế độ ${period} (tối thiểu ${limit.min} ${limit.unit}). Hiện tại: ${durationDays} ngày.`
    };
  }
  
  if (durationDays > limit.max) {
    return { 
      valid: false, 
      message: `Khoảng thời gian quá lớn cho chế độ ${period} (tối đa ${limit.max} ${limit.unit}). Hiện tại: ${durationDays} ngày.`
    };
  }
  
  return { valid: true, message: "Valid" };
}

// Usage example trong date picker component
function handleDateRangeChange(startDate, endDate, selectedPeriod) {
  const validation = validatePeriodDateRange(selectedPeriod, startDate.getTime(), endDate.getTime());
  
  if (!validation.valid) {
    // Hiển thị error message cho user
    showErrorMessage(validation.message);
    return false;
  }
  
  // Proceed với API call
  fetchOrderStatistics(selectedPeriod, startDate.getTime(), endDate.getTime());
  return true;
}
```

### **2. Chart Integration Example**
```javascript
// Fetch data cho chart
async function fetchOrderStatistics(period = 'day', fromDate = null, toDate = null) {
  try {
    let url = `/api/orders/statistics/summary?period=${period}`;
    if (fromDate && toDate) {
      url += `&fromDate=${fromDate}&toDate=${toDate}`;
    }
    
    const response = await fetch(url);
    const result = await response.json();
    
    if (result.status === 200) {
      // Transform data for chart
      const chartData = result.data.map(item => ({
        x: item.date,
        totalOrders: item.totalOrders,
        completedOrders: item.completedOrders,
        canceledOrders: item.canceledOrders,
        refundedOrders: item.refundedOrders,
        netRevenue: item.netRevenue,
        aov: item.aov,
        dateRange: item.dateRange,
        period: item.period,
        // Actual range info for incomplete periods
        startDate: item.startDate,
        endDate: item.endDate
      }));
      
      return chartData;
    } else {
      // Handle validation errors
      showErrorMessage(result.message);
      return [];
    }
  } catch (error) {
    console.error('Error fetching order statistics:', error);
    showErrorMessage('Lỗi kết nối API');
    return [];
  }
}

// Handle chart point click
function onChartPointClick(dataPoint, period) {
  const clickedDate = new Date(dataPoint.x).getTime();
  fetchOrderDetails(period, clickedDate, 20);
}

// Fetch details khi click vào chart
async function fetchOrderDetails(period, date, limit = 10) {
  try {
    const response = await fetch(`/api/orders/statistics/details?period=${period}&date=${date}&limit=${limit}`);
    const result = await response.json();
    
    if (result.status === 200) {
      return result.data;
    } else {
      showErrorMessage(result.message);
      return [];
    }
  } catch (error) {
    console.error('Error fetching order details:', error);
    showErrorMessage('Lỗi kết nối API');
    return [];
  }
}
```

### **3. Period Selector Component**
```javascript
const periodOptions = [
  { 
    value: 'day', 
    label: 'Theo ngày', 
    description: '7 ngày gần nhất (tối đa 90 ngày)',
    chartType: 'line',
    limits: { min: 1, max: 90 }
  },
  { 
    value: 'week', 
    label: 'Theo tuần', 
    description: 'Các tuần gần đây (tối đa 2 năm)',
    chartType: 'bar',
    limits: { min: 7, max: 730 }
  },
  { 
    value: 'month', 
    label: 'Theo tháng', 
    description: 'Các tháng gần đây (tối đa 5 năm)',
    chartType: 'bar',
    limits: { min: 28, max: 1825 }
  },
  { 
    value: 'quarter', 
    label: 'Theo quý', 
    description: 'Các quý gần đây (tối đa 5 năm)',
    chartType: 'bar',
    limits: { min: 90, max: 1825 }
  },
  { 
    value: 'year', 
    label: 'Theo năm', 
    description: 'Các năm gần đây (tối đa 25 năm)',
    chartType: 'bar',
    limits: { min: 365, max: 9125 }
  }
];
```

### **4. Custom Date Range with Validation**
```javascript
// Custom date range picker với validation
function handleCustomDateRange(startDate, endDate, selectedPeriod) {
  const fromDate = startDate.getTime();
  const toDate = endDate.getTime();
  
  // Validation trước khi gọi API
  const validation = validatePeriodDateRange(selectedPeriod, fromDate, toDate);
  if (!validation.valid) {
    alert(validation.message);
    return false;
  }
  
  // Call API với custom range
  fetchOrderStatistics(selectedPeriod, fromDate, toDate)
    .then(data => {
      if (data.length > 0) {
        updateChart(data);
        
        // Show actual date range được sử dụng
        const actualStartDate = data[0].startDate;
        const actualEndDate = data[data.length - 1].endDate;
        showDateRangeInfo(`Hiển thị dữ liệu từ ${actualStartDate} đến ${actualEndDate}`);
      } else {
        showNoDataMessage();
      }
    });
    
  return true;
}
```

### **5. Data Formatting Helpers**
```javascript
// Format currency
function formatCurrency(amount) {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND'
  }).format(amount);
}

// Format date based on period
function formatDateLabel(dateStr, period) {
  const date = new Date(dateStr);
  
  switch (period) {
    case 'daily':
      return date.toLocaleDateString('vi-VN');
    case 'weekly':
      return `Tuần ${getWeekNumber(date)}`;
    case 'monthly':
      return date.toLocaleDateString('vi-VN', { year: 'numeric', month: 'long' });
    case 'quarterly':
      return `Quý ${Math.ceil((date.getMonth() + 1) / 3)} - ${date.getFullYear()}`;
    case 'yearly':
      return date.getFullYear().toString();
    default:
      return dateStr;
  }
}

// Convert timestamp to readable date
function formatTimestamp(timestamp) {
  return new Date(timestamp).toLocaleDateString('vi-VN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  });
}

// Get week number helper
function getWeekNumber(date) {
  const firstDayOfYear = new Date(date.getFullYear(), 0, 1);
  const pastDaysOfYear = (date - firstDayOfYear) / 86400000;
  return Math.ceil((pastDaysOfYear + firstDayOfYear.getDay() + 1) / 7);
}

// Format order status for display
function formatOrderStatus(status) {
  const statusMap = {
    'PENDING': 'Chờ xác nhận',
    'CONFIRMED': 'Đã xác nhận', 
    'PROCESSING': 'Đang xử lý',
    'SHIPPED': 'Đã gửi hàng',
    'DELIVERED': 'Đã giao hàng',
    'CANCELED': 'Đã hủy',
    'PARTIALLY_REFUNDED': 'Hoàn trả một phần',
    'REFUNDED': 'Hoàn trả toàn bộ'
  };
  return statusMap[status] || status;
}

// Validate and show range info for incomplete periods
function showActualRangeInfo(data, requestedPeriod) {
  if (data.length === 0) return;
  
  const firstItem = data[0];
  const lastItem = data[data.length - 1];
  
  const actualStart = firstItem.startDate;
  const actualEnd = lastItem.endDate;
  
  // Check if this is an incomplete period
  const expectedEnd = getExpectedPeriodEnd(firstItem.startDate, requestedPeriod);
  
  if (actualEnd !== expectedEnd) {
    showWarningMessage(
      `⚠️ Dữ liệu hiển thị từ ${actualStart} đến ${actualEnd} (khoảng thời gian không đủ 1 ${requestedPeriod} hoàn chỉnh)`
    );
  }
}
```
```

---

## 🔗 Order Status Reference

| Status | Description | Trong Summary | FE Display |
|--------|-------------|---------------|------------|
| `PENDING` | Chờ xác nhận | totalOrders | "Chờ xác nhận" |
| `CONFIRMED` | Đã xác nhận | totalOrders | "Đã xác nhận" |
| `PROCESSING` | Đang xử lý | totalOrders | "Đang xử lý" |
| `SHIPPED` | Đã gửi hàng | totalOrders | "Đã gửi hàng" |
| `DELIVERED` | Đã giao hàng | **completedOrders** | "Đã giao hàng" |
| `CANCELED` | Đã hủy | **canceledOrders** | "Đã hủy" |
| `PARTIALLY_REFUNDED` | Hoàn trả một phần | **refundedOrders** | "Hoàn trả một phần" |
| `REFUNDED` | Hoàn trả toàn bộ | **refundedOrders** | "Hoàn trả toàn bộ" |

---

## 🎯 Data Validation Checklist

✅ **Summary API Response có đầy đủ:**
- `date`, `totalOrders`, `completedOrders`, `canceledOrders`, `refundedOrders`
- `netRevenue`, `aov`, `period`, `dateRange`, `startDate`, `endDate`
- Period-specific fields: `weekNumber`, `year`, `monthNumber`, `monthName`, `quarter`

✅ **Details API Response có đầy đủ:**
- `orderCode`, `customerName`, `customerEmail`
- `totalAmount`, `orderStatus`, `createdAt`, `productInfo`

✅ **Error Handling Cover:**
- Invalid period, missing required params, invalid date range
- No data available, server errors

✅ **Frontend Integration Ready:**
- Chart data mapping, period selectors, date formatters
- Custom date range handling, status reference mapping

---

**🚀 Ready for Production Integration!**
