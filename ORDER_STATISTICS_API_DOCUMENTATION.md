# 📊 Order Statistics APIs Documentation

## 🎯 Tổng quan
Hệ thống Order Statistics APIs cung cấp **2-tier architecture** để lấy thống kê đơn hàng với 2 mức độ chi tiết:

1. **TIER 1 - Summary API**: Dữ liệu tổng quan cho charts/graphs
2. **TIER 2 - Details API**: Chi tiết đơn hàng khi click vào điểm cụ thể

---

## 🔥 TIER 1: Order Statistics Summary API

### **Endpoint**
```
GET /api/orders/statistics/summary
```

### **Mục đích**
- Lấy dữ liệu tổng quan để hiển thị trên charts/graphs
- Trả về metrics theo ngày: tổng đơn hàng, đơn hoàn thành, hủy, hoàn trả, doanh thu thuần, AOV
- Hỗ trợ 5 loại period + custom range

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

// Trả về: 7 ngày gần nhất, mỗi ngày 1 record
```

#### **2. Weekly Summary**
```javascript
// Request  
GET /api/orders/statistics/summary?period=week

// Trả về: Các tuần gần đây, mỗi tuần 1 record
```

#### **3. Monthly Summary**
```javascript
// Request
GET /api/orders/statistics/summary?period=month

// Trả về: Các tháng gần đây, mỗi tháng 1 record
```

#### **4. Quarterly Summary**
```javascript
// Request
GET /api/orders/statistics/summary?period=quarter

// Trả về: Các quý gần đây, mỗi quý 1 record
```

#### **5. Yearly Summary**
```javascript
// Request
GET /api/orders/statistics/summary?period=year

// Trả về: Các năm gần đây, mỗi năm 1 record
```

#### **6. Custom Date Range**
```javascript
// Request - Lấy 30 ngày từ 1/8/2025 đến 30/8/2025
const fromDate = new Date('2025-08-01').getTime(); // 1722441600000
const toDate = new Date('2025-08-30').getTime();   // 1724947200000

GET /api/orders/statistics/summary?period=day&fromDate=${fromDate}&toDate=${toDate}

// Trả về: 30 records, mỗi ngày 1 record trong khoảng thời gian
```

### **📋 Response Format**

```json
{
  "status": 200,
  "message": "Lấy thống kê tổng quan thành công",
  "data": [
    {
      "date": "2025-08-25",           // Ngày (YYYY-MM-DD)
      "totalOrders": 45,              // Tổng số đơn hàng
      "completedOrders": 38,          // Đơn DELIVERED
      "canceledOrders": 5,            // Đơn CANCELED
      "refundedOrders": 2,            // Đơn PARTIALLY_REFUNDED + REFUNDED
      "netRevenue": 2450000.00,       // Doanh thu thuần (VND)
      "aov": 54444.44,                // Average Order Value
      "period": "daily",              // Loại period
      "dateRange": "2025-08-25",      // Mô tả khoảng thời gian
      // Các trường bổ sung tùy theo period:
      "startDate": "2025-08-25",      // Ngày bắt đầu
      "endDate": "2025-08-25"         // Ngày kết thúc
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
      "dateRange": "2025-08-24"
    }
    // ... more records
  ]
}
```

### **📊 Response cho từng Period Type**

#### **Daily Period Response**
```json
{
  "date": "2025-08-25",
  "period": "daily",
  "dateRange": "2025-08-25"
  // + metrics
}
```

#### **Weekly Period Response**  
```json
{
  "date": "2025-08-19",           // Monday của tuần
  "period": "weekly", 
  "dateRange": "2025-08-19 to 2025-08-25",
  "weekNumber": 34,
  "year": 2025,
  "startDate": "2025-08-19",
  "endDate": "2025-08-25"
  // + metrics
}
```

#### **Monthly Period Response**
```json
{
  "date": "2025-08-01",           // Ngày đầu tháng
  "period": "monthly",
  "dateRange": "August 2025", 
  "monthNumber": 8,
  "monthName": "Tháng Tám",
  "year": 2025,
  "startDate": "2025-08-01",
  "endDate": "2025-08-31"
  // + metrics
}
```

#### **Quarterly Period Response**
```json
{
  "date": "2025-07-01",           // Ngày đầu quý
  "period": "quarterly",
  "dateRange": "Quý 3 năm 2025",
  "quarter": 3,
  "year": 2025, 
  "startDate": "2025-07-01",
  "endDate": "2025-09-30"
  // + metrics
}
```

#### **Yearly Period Response**
```json
{
  "date": "2025-01-01",           // Ngày đầu năm
  "period": "yearly",
  "dateRange": "Year 2025",
  "year": 2025,
  "startDate": "2025-01-01", 
  "endDate": "2025-12-31"
  // + metrics
}
```

---

## 🔍 TIER 2: Order Statistics Details API

### **Endpoint**
```
GET /api/orders/statistics/details
```

### **Mục đích**
- Lấy danh sách đơn hàng chi tiết trong một khoảng thời gian cụ thể
- Sử dụng khi user click vào điểm trên chart để xem details
- Trả về thông tin đơn hàng: mã, khách hàng, sản phẩm, trạng thái

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
const clickDate = new Date('2025-08-25 10:30:00').getTime(); // Bất kỳ thời điểm nào trong ngày

// Request
GET /api/orders/statistics/details?period=day&date=${clickDate}&limit=20

// Trả về: Top 20 đơn hàng trong ngày 25/8/2025
```

#### **2. Xem đơn hàng trong tuần**
```javascript
// Scenario: User click vào điểm tuần 34 năm 2025 trên chart
const weekTimestamp = new Date('2025-08-22').getTime(); // Bất kỳ ngày nào trong tuần

// Request  
GET /api/orders/statistics/details?period=week&date=${weekTimestamp}&limit=15

// Trả về: Top 15 đơn hàng từ thứ 2 (19/8) đến chủ nhật (25/8)
```

#### **3. Xem đơn hàng trong tháng**
```javascript
// Scenario: User click vào điểm tháng 8/2025 trên chart
const monthTimestamp = new Date('2025-08-15').getTime(); // Bất kỳ ngày nào trong tháng

// Request
GET /api/orders/statistics/details?period=month&date=${monthTimestamp}&limit=50

// Trả về: Top 50 đơn hàng trong tháng 8/2025
```

#### **4. Xem đơn hàng trong quý**
```javascript
// Scenario: User click vào điểm quý 3/2025 trên chart  
const quarterTimestamp = new Date('2025-08-01').getTime(); // Bất kỳ ngày nào trong quý

// Request
GET /api/orders/statistics/details?period=quarter&date=${quarterTimestamp}&limit=100

// Trả về: Top 100 đơn hàng trong quý 3 (7-9/2025)
```

#### **5. Xem đơn hàng trong năm**
```javascript
// Scenario: User click vào điểm năm 2025 trên chart
const yearTimestamp = new Date('2025-06-01').getTime(); // Bất kỳ ngày nào trong năm

// Request
GET /api/orders/statistics/details?period=year&date=${yearTimestamp}&limit=200

// Trả về: Top 200 đơn hàng trong năm 2025
```

### **📋 Response Format**

```json
{
  "status": 200,
  "message": "Lấy chi tiết đơn hàng thành công",
  "data": [
    {
      "orderCode": "ORD-2025080001",         // Mã đơn hàng
      "customerName": "Nguyễn Văn A",        // Tên khách hàng
      "customerEmail": "nguyenvana@email.com", // Email khách hàng
      "totalAmount": 550000.00,              // Tổng giá trị đơn hàng
      "orderStatus": "DELIVERED",            // Trạng thái đơn hàng
      "createdAt": 1724572800000,            // Timestamp tạo đơn
      "productInfo": "Lập trình Java cơ bản (ISBN:978-123456789, ID:15), Spring Boot in Action (ISBN:978-987654321, ID:28)" // Danh sách sản phẩm
    },
    {
      "orderCode": "ORD-2025080002", 
      "customerName": "Trần Thị B",
      "customerEmail": "tranthib@email.com",
      "totalAmount": 320000.00,
      "orderStatus": "PROCESSING",
      "createdAt": 1724569200000,
      "productInfo": "Clean Code (ISBN:978-111222333, ID:42)"
    }
    // ... more orders
  ]
}
```

---

## 🚨 Error Handling

### **Invalid Period**
```json
// Request: ?period=invalid
{
  "status": 400,
  "message": "Period không hợp lệ. Chỉ chấp nhận: day, week, month, quarter, year",
  "data": []
}
```

### **Missing Required Parameters for Details API**
```json
// Request: /details?period=day (thiếu date)
{
  "status": 400,
  "message": "Tham số 'date' là bắt buộc cho API chi tiết",
  "data": []
}
```

### **Invalid Date Range**
```json
// Request: ?period=day&fromDate=future&toDate=past
{
  "status": 400,
  "message": "Khoảng thời gian không hợp lệ: fromDate phải nhỏ hơn toDate",
  "data": []
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

 
 
### **2. Period Selector**
```javascript
const periodOptions = [
  { value: 'day', label: 'Theo ngày', description: '7 ngày gần nhất' },
  { value: 'week', label: 'Theo tuần', description: 'Các tuần gần đây' },
  { value: 'month', label: 'Theo tháng', description: 'Các tháng gần đây' },
  { value: 'quarter', label: 'Theo quý', description: 'Các quý gần đây' },
  { value: 'year', label: 'Theo năm', description: 'Các năm gần đây' }
];
```
 -

## 🔗 Order Status Reference

| Status | Description | Trong Summary |
|--------|-------------|---------------|
| `PENDING` | Chờ xác nhận | totalOrders |
| `CONFIRMED` | Đã xác nhận | totalOrders |
| `PROCESSING` | Đang xử lý | totalOrders |
| `SHIPPED` | Đã gửi hàng | totalOrders |
| `DELIVERED` | Đã giao hàng | **completedOrders** |
| `CANCELED` | Đã hủy | **canceledOrders** |
| `PARTIALLY_REFUNDED` | Hoàn trả một phần | **refundedOrders** |
| `REFUNDED` | Hoàn trả toàn bộ | **refundedOrders** |

---

