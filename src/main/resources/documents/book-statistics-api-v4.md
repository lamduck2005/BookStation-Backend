# 📊 Book Statistics API v4 Documentation

## API Endpoints

### 1. 📊 Summary Statistics (GET /api/books/statistics/summary)

**Mô tả:** Trả về thống kê tổng quan số sách bán theo thời gian

**Parameters:**
- `period` (String, default: "day"): Loại thời gian - "day", "week", "month", "year", "custom"
- `fromDate` (Long, optional): Timestamp bắt đầu (bắt buộc nếu period=custom)
- `toDate` (Long, optional): Timestamp kết thúc (bắt buộc nếu period=custom)

**Logic xử lý:**

#### 1. Nếu period = "day" (mặc định)
- Trả về **30 ngày gần nhất** từ hôm nay
- Bao gồm cả ngày không có dữ liệu bán hàng (totalBooksSold = 0)
- Sắp xếp từ cũ đến mới

#### 2. Nếu period = "custom"
Tự động group data theo quy tắc:
- **≤ 31 ngày**: Group theo ngày
- **32-180 ngày**: Group theo tuần  
- **> 180 ngày**: Group theo tháng

**Response Format:**
```json
{
    "status": 200,
    "message": "Summary statistics retrieved successfully",
    "data": [
        {
            "date": "2025-07-15",        // Định dạng YYYY-MM-DD
            "totalBooksSold": 45,        // Tổng số sách bán trong ngày
            "period": "daily"            // Loại group: "daily", "weekly", "monthly"
        },
        {
            "date": "2025-07-16",
            "totalBooksSold": 0,         // Ngày không có bán hàng
            "period": "daily"
        }
    ]
}
```

**Ví dụ Request:**
```bash
# Mặc định 30 ngày gần nhất
GET /api/books/statistics/summary?period=day

# Custom khoảng thời gian
GET /api/books/statistics/summary?period=custom&fromDate=1754179200000&toDate=1755129600000
```

### 2. 📊 Detailed Statistics (GET /api/books/statistics/details)

**Mô tả:** Trả về chi tiết top sách khi user click vào điểm cụ thể trên chart

**Parameters:**
- `period` (String, required): "day", "week", "month", "year"
- `date` (Long, required): Timestamp đại diện cho khoảng thời gian
- `limit` (Integer, default: 10): Số lượng sách muốn lấy

**Response Format:**
```json
{
    "status": 200,
    "message": "Book details retrieved successfully",
    "data": [
        {
            "bookId": 1,
            "bookTitle": "Sách A",
            "bookCode": "BOOK001",
            "totalSold": 25,
            "revenue": 500000,
            "growth": 15.5,
            "growthDirection": "UP"
        }
    ]
}
```

## Implementation Notes

### Time Range Calculation
- **Day period**: Từ 00:00:00 đến 23:59:59 của ngày đó
- **Week period**: Từ thứ 2 đến Chủ nhật
- **Month period**: Từ ngày 1 đến ngày cuối tháng
- **Year period**: Từ 1/1 đến 31/12

### Data Grouping Rules (Custom Period)
```java
long daysDiff = (toDate - fromDate) / (24 * 60 * 60 * 1000L);

if (daysDiff <= 31) {
    // Group theo ngày - trả về từng ngày trong khoảng
    groupBy = "DAY";
} else if (daysDiff <= 180) {
    // Group theo tuần - trả về các tuần
    groupBy = "WEEK";  
} else {
    // Group theo tháng - trả về các tháng
    groupBy = "MONTH";
}
```

### Error Handling
- Period không hợp lệ → HTTP 400
- Custom period thiếu fromDate/toDate → HTTP 400
- Lỗi database → HTTP 500

## Database Queries

### Query cho Summary Statistics
```sql
-- Lấy dữ liệu bán hàng theo ngày
SELECT 
    DATE(FROM_UNIXTIME(od.created_time/1000)) as date,
    SUM(od.quantity) as totalBooksSold
FROM order_details od
JOIN orders o ON od.order_id = o.id
WHERE od.created_time BETWEEN ? AND ?
    AND o.status IN ('COMPLETED', 'DELIVERED')
GROUP BY DATE(FROM_UNIXTIME(od.created_time/1000))
ORDER BY date;
```

### Query cho Details Statistics
```sql
-- Lấy top sách chi tiết
SELECT 
    b.id as bookId,
    b.book_name as bookTitle,
    b.book_code as bookCode,
    SUM(od.quantity) as totalSold,
    SUM(od.quantity * od.price) as revenue
FROM order_details od
JOIN books b ON od.book_id = b.id
JOIN orders o ON od.order_id = o.id
WHERE od.created_time BETWEEN ? AND ?
    AND o.status IN ('COMPLETED', 'DELIVERED')
GROUP BY b.id, b.book_name, b.book_code
ORDER BY totalSold DESC
LIMIT ?;
```

## Version History
- **v1**: Initial API
- **v2**: Added period parameter
- **v3**: Added custom period support
- **v4**: Fixed logic - 30 days default, proper custom grouping rules
