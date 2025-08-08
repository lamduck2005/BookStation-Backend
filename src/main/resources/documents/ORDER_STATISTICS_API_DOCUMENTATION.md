# 📊 ORDER STATISTICS API DOCUMENTATION

## 🎯 **TỔNG QUAN**

Bộ API thống kê đơn hàng phục vụ dashboard admin quản lý và phân tích kinh doanh. Bao gồm 8 API chính cho các loại biểu đồ và thống kê khác nhau.

**Base URL**: `/api/orders/statistics`

---

## 📋 **DANH SÁCH API**

### 1. **📊 API THỐNG KÊ TỔNG QUAN - cho CARDS**
```http
GET /api/orders/statistics/overview
```

**Mục đích**: Hiển thị các card thống kê chính trên dashboard
**Thiết kế Frontend**:
- 📦 **Card "Đơn hàng hôm nay"**: Số lượng + so sánh với hôm qua
- 💰 **Card "Doanh thu"**: Doanh thu hôm nay/tháng + lợi nhuận ròng  
- 🚚 **Card "Vận chuyển"**: Chi phí vận chuyển + tỷ lệ COD
- 🔄 **Card "Hoàn trả/Hủy"**: Số đơn hoàn trả, hủy + tỷ lệ COD thất bại
- 📊 **Layout**: Grid 2x2 hoặc 4 cards ngang, màu sắc theo mức độ (xanh/vàng/đỏ)

**Response**:
```json
{
  "status": 200,
  "message": "Lấy thống kê tổng quan thành công",
  "data": {
    "totalOrdersToday": 45,
    "totalOrdersThisMonth": 1250,
    "revenueToday": 12500000.50,
    "revenueThisMonth": 450000000.75,
    "netProfitToday": 10000000.00,
    "netProfitThisMonth": 360000000.00,
    "totalShippingCostToday": 2500000.50,
    "totalShippingCostThisMonth": 90000000.75,
    "codRateToday": 65.5,
    "codRateThisMonth": 72.3,
    "refundedOrdersToday": 3,
    "refundedOrdersThisMonth": 25,
    "canceledOrdersToday": 2,
    "canceledOrdersThisMonth": 18,
    "failedCodOrdersToday": 5,
    "failedCodOrdersThisMonth": 42,
    "failedCodRateToday": 8.5,
    "failedCodRateThisMonth": 6.2
  }
}
```

**Cards Frontend sử dụng**:
- 📦 Tổng số đơn hôm nay/tháng này
- 💰 Doanh thu & lợi nhuận ròng
- 📉 Chi phí vận chuyển & tỷ lệ COD
- 🔄 Số đơn hoàn trả/bị hủy
- ⚠️ Tỷ lệ COD thất bại

---

### 2. **📈 API BIỂU ĐỒ DOANH THU - cho LINE CHART**
```http
GET /api/orders/statistics/revenue-chart?period=daily&days=30
```

**Mục đích**: Hiển thị xu hướng doanh thu theo thời gian
**Thiết kế Frontend**:
- 📈 **Line Chart**: Trục X là thời gian, trục Y là doanh thu
- 🔄 **Toggle buttons**: Daily/Weekly/Monthly để chuyển đổi view
- 📊 **Tooltip**: Hiển thị ngày + doanh thu + số đơn khi hover
- 📱 **Responsive**: Chart phải responsive trên mobile
- 🎨 **Style**: Đường line màu xanh dương, gradient fill bên dưới

**Parameters**:
- `period`: "daily", "weekly", "monthly" (default: "daily")
- `days`: Số ngày lấy dữ liệu (default: 30)

**Response**:
```json
{
  "status": 200,
  "message": "Lấy biểu đồ doanh thu thành công",
  "data": {
    "dataPoints": [
      {
        "date": "2025-01-01",
        "period": "Thứ 2",
        "revenue": 5000000.00,
        "orderCount": 25
      },
      {
        "date": "2025-01-02",
        "period": "Thứ 3", 
        "revenue": 7200000.00,
        "orderCount": 32
      }
    ],
    "periodType": "daily",
    "totalRevenue": 450000000.00,
    "totalOrders": 1250
  }
}
```

**Biểu đồ Frontend**: Line chart doanh thu theo thời gian

---

### 3. **🏆 API TOP SẢN PHẨM BÁN CHẠY - cho BAR CHART**
```http
GET /api/orders/statistics/top-products?period=week&limit=10
```

**Mục đích**: Hiển thị sản phẩm bán chạy nhất để quản lý inventory
**Thiết kế Frontend**:
- 📊 **Horizontal Bar Chart**: Tên sách bên trái, bar bên phải
- 🖼️ **Book thumbnails**: Hiển thị ảnh bìa sách nhỏ
- 📱 **Product cards**: Click vào bar → modal chi tiết sách
- 🎚️ **Filter dropdown**: Today/Week/Month + limit 5/10/20
- 🏆 **Ranking**: Số thứ tự #1, #2, #3 với icon medal
- 💰 **Dual axis**: Số lượng bán + doanh thu trên cùng 1 chart

**Parameters**:
- `period`: "today", "week", "month" (default: "week")
- `limit`: Số sản phẩm top (default: 10)

**Response**:
```json
{
  "status": 200,
  "message": "Lấy top sản phẩm thành công",
  "data": {
    "topProducts": [
      {
        "bookId": 123,
        "bookTitle": "Clean Code",
        "bookImage": "https://example.com/clean-code.jpg",
        "authorName": "Robert C. Martin",
        "quantitySold": 85,
        "totalRevenue": 12500000.00
      },
      {
        "bookId": 124,
        "bookTitle": "Design Patterns",
        "bookImage": "https://example.com/design-patterns.jpg", 
        "authorName": "Gang of Four",
        "quantitySold": 72,
        "totalRevenue": 10800000.00
      }
    ],
    "period": "week"
  }
}
```

**Biểu đồ Frontend**: Bar chart sản phẩm bán chạy nhất

---

### 4. **🥧 API THỐNG KÊ PHƯƠNG THỨC THANH TOÁN - cho PIE CHART**
```http
GET /api/orders/statistics/payment-methods?period=week
```

**Mục đích**: Phân tích tỷ lệ COD vs Online để điều chỉnh chính sách thanh toán
**Thiết kế Frontend**:
- 🥧 **Pie/Donut Chart**: 2 segments chính COD vs Online
- 📊 **Legend**: Bên cạnh chart với % và số tiền cụ thể
- 💡 **Insight box**: "COD chiếm 72% → cần tăng incentive thanh toán online"
- 🎨 **Colors**: COD màu cam, Online màu xanh lá
- 📱 **Mobile**: Chart nhỏ + legend dưới trên mobile

**Parameters**:
- `period`: "today", "week", "month" (default: "week")

**Response**:
```json
{
  "status": 200,
  "message": "Lấy thống kê phương thức thanh toán thành công",
  "data": {
    "paymentMethods": [
      {
        "paymentMethod": "Thanh toán khi nhận hàng",
        "orderCount": 850,
        "totalAmount": 320000000.00,
        "percentage": 72.5
      },
      {
        "paymentMethod": "Thanh toán trực tuyến", 
        "orderCount": 322,
        "totalAmount": 130000000.00,
        "percentage": 27.5
      }
    ],
    "totalOrders": 1172,
    "totalAmount": 450000000.00
  }
}
```

**Biểu đồ Frontend**: Pie chart tỷ lệ COD vs Online

---

### 5. **🗺️ API THỐNG KÊ THEO ĐỊA ĐIỂM - cho HEATMAP**
```http
GET /api/orders/statistics/locations?period=week
```

**Mục đích**: Xác định thị trường mạnh/yếu để phân bổ marketing budget
**Thiết kế Frontend**:
- 🗺️ **Vietnam Map**: SVG map với màu sắc theo intensity (0.0-1.0)
- 🎨 **Color gradient**: Trắng → Xanh nhạt → Xanh đậm
- 🔍 **Hover tooltip**: Tên tỉnh + số đơn + doanh thu + %
- 📋 **Side table**: Top 10 tỉnh thành bán chạy nhất
- 🎯 **Click action**: Click tỉnh → drill down chi tiết khu vực đó

**Parameters**:
- `period`: "today", "week", "month" (default: "week")

**Response**:
```json
{
  "status": 200,
  "message": "Lấy thống kê theo địa điểm thành công",
  "data": {
    "provinces": [
      {
        "provinceName": "Hồ Chí Minh",
        "provinceId": 79,
        "orderCount": 450,
        "totalAmount": 180000000.00,
        "percentage": 35.2,
        "intensity": 1.0
      },
      {
        "provinceName": "Hà Nội",
        "provinceId": 1, 
        "orderCount": 320,
        "totalAmount": 125000000.00,
        "percentage": 25.0,
        "intensity": 0.71
      },
      {
        "provinceName": "Đà Nẵng",
        "provinceId": 48,
        "orderCount": 85,
        "totalAmount": 32000000.00, 
        "percentage": 6.6,
        "intensity": 0.19
      }
    ],
    "totalOrders": 1280,
    "totalAmount": 450000000.00
  }
}
```

**Biểu đồ Frontend**: Heatmap theo tỉnh/thành phố (intensity 0.0-1.0)

---

### 6. **📊 API SO SÁNH DOANH THU - cho COMPARISON CARDS**
```http
GET /api/orders/statistics/revenue-comparison
```

**Mục đích**: So sánh performance hiện tại vs quá khứ để đánh giá growth
**Thiết kế Frontend**:
- 📊 **Comparison Cards**: 2 cards "Tuần này vs Tuần trước" & "Tháng này vs Tháng trước"
- 📈 **Trend arrows**: Mũi tên xanh ↗ (tăng) hoặc đỏ ↘ (giảm)
- 💯 **Growth percentage**: +13.64% hoặc -5.2% với màu sắc tương ứng
- 📱 **Progress bars**: Visual representation của tỷ lệ tăng/giảm
- 🎨 **Color coding**: Xanh cho tăng trưởng, đỏ cho giảm, xám cho stable

**Response**:
```json
{
  "status": 200,
  "message": "Lấy so sánh doanh thu thành công",
  "data": {
    "currentWeekRevenue": 125000000.00,
    "previousWeekRevenue": 110000000.00,
    "weeklyGrowthRate": 13.64,
    "weeklyGrowthDirection": "up",
    
    "currentMonthRevenue": 450000000.00,
    "previousMonthRevenue": 380000000.00,
    "monthlyGrowthRate": 18.42,
    "monthlyGrowthDirection": "up",
    
    "currentWeekOrders": 285,
    "previousWeekOrders": 245,
    "currentMonthOrders": 1250,
    "previousMonthOrders": 1050
  }
}
```

**Frontend sử dụng**: Cards so sánh tuần này vs tuần trước, tháng này vs tháng trước

---

### 7. **👥 API THỐNG KÊ KHÁCH HÀNG - cho CUSTOMER ANALYTICS**
```http
GET /api/orders/statistics/customers?period=month
```

**Mục đích**: Phân tích hành vi khách hàng để xây dựng chiến lược CRM
**Thiết kế Frontend**:
- 📊 **Customer segmentation**: Cards cho New/Returning/VIP/Risky customers
- 👑 **VIP Table**: Bảng top khách VIP với avatar, tên, tổng chi tiêu, nút "Send Voucher"
- ⚠️ **Risk Table**: Bảng khách rủi ro với risk score, failed delivery, nút "Contact"  
- 📈 **Retention Chart**: Donut chart retention rate với số % ở giữa
- 🎯 **Action buttons**: "Send marketing email", "Create VIP program"

**Parameters**:
- `period`: "today", "week", "month" (default: "month")

**Response**:
```json
{
  "status": 200,
  "message": "Lấy thống kê khách hàng thành công",
  "data": {
    "newCustomers": 125,
    "returningCustomers": 875,
    "retentionRate": 87.5,
    
    "vipCustomers": [
      {
        "userId": 456,
        "customerName": "Nguyễn Văn A",
        "email": "nguyenvana@gmail.com",
        "phone": "0987654321",
        "totalOrders": 15,
        "totalSpent": 25000000.00,
        "customerType": "vip",
        "lastOrderDate": "15/01/2025"
      }
    ],
    
    "riskyCustomers": [
      {
        "userId": 789,
        "customerName": "Trần Thị B", 
        "email": "tranthib@gmail.com",
        "phone": "0123456789",
        "totalOrders": 8,
        "customerType": "risky",
        "riskScore": 0.62,
        "failedDeliveryCount": 5
      }
    ],
    
    "totalCustomers": 1000,
    "activeCustomers": 892
  }
}
```

**Frontend sử dụng**:
- Cards: Khách hàng mới vs quay lại, Retention Rate
- Tables: Top khách hàng VIP, khách hàng rủi ro cao

---

### 8. **🎯 API GỢI Ý BÁN CHÉO - cho UPSELL/CROSS-SELL**
```http
GET /api/orders/statistics/cross-sell/123?limit=5
```

**Mục đích**: Tăng giá trị đơn hàng bằng cách gợi ý sản phẩm liên quan
**Thiết kế Frontend**:
- 🎯 **Product suggestion modal**: Popup khi xem chi tiết đơn hàng
- 📚 **Book carousel**: Slide ngang các sách gợi ý với ảnh + giá
- ⭐ **Confidence score**: "92% khách mua sách này cũng mua sách kia"
- 🛒 **Quick add buttons**: "Add to current order" cho từng sản phẩm
- 💡 **Smart reasons**: "Cùng tác giả", "Cùng thể loại", "Combo phổ biến"

**Parameters**:
- `orderId`: ID đơn hàng hiện tại (path param)
- `limit`: Số sản phẩm gợi ý (default: 5)

**Response**:
```json
{
  "status": 200,
  "message": "Lấy gợi ý bán chéo thành công",
  "data": {
    "currentOrderId": 123,
    "suggestedProducts": [
      {
        "bookId": 456,
        "bookTitle": "Refactoring",
        "bookImage": "https://example.com/refactoring.jpg",
        "authorName": "Martin Fowler",
        "price": 450000.00,
        "confidence": 0.85,
        "reason": "Khách hàng mua sách này thường mua cả sách kia"
      }
    ],
    "suggestionType": "cross_sell"
  }
}
```

**Frontend sử dụng**: Modal gợi ý sản phẩm khi xem chi tiết đơn hàng

---

## 🎨 **MAPPING FRONTEND**

| API Endpoint | Loại Biểu Đồ/Component | Mục Đích | UI Layout |
|--------------|-------------------------|----------|-----------|
| `/overview` | **Cards Dashboard** | Thống kê tổng quan | Grid 2x2 cards với color coding |
| `/revenue-chart` | **Line Chart** | Doanh thu theo thời gian | Full-width chart + period toggles |
| `/top-products` | **Bar Chart** | Top sản phẩm bán chạy | Horizontal bars + book thumbnails |
| `/payment-methods` | **Pie Chart** | Tỷ lệ thanh toán COD/Online | Donut chart + legend + insights |
| `/locations` | **Heatmap** | Phân bố đơn hàng theo tỉnh/thành | Vietnam SVG map + side table |
| `/revenue-comparison` | **Comparison Cards** | So sánh doanh thu theo thời gian | 2 cards với trend arrows |
| `/customers` | **Tables + Cards** | Phân tích khách hàng | CRM dashboard với action buttons |
| `/cross-sell/{id}` | **Product Suggestions** | Gợi ý bán chéo | Modal popup với carousel |

---

## 🚀 **FRONTEND IMPLEMENTATION GUIDE**

### **📱 Responsive Design Requirements**
- **Desktop**: Full dashboard với tất cả components
- **Tablet**: Stack charts vertically, smaller cards
- **Mobile**: Single column, collapsible sections, swipe gestures

### **🎨 Color Palette Recommendation**
```css
:root {
  --success: #10B981;     /* Xanh lá - tăng trưởng, healthy */
  --warning: #F59E0B;     /* Vàng - cảnh báo, cần chú ý */
  --critical: #EF4444;    /* Đỏ - nguy hiểm, giảm sút */
  --info: #3B82F6;        /* Xanh dương - thông tin */
  --neutral: #6B7280;     /* Xám - trung tính */
  --primary: #8B5CF6;     /* Tím - brand color */
}
```

### **📊 Chart Libraries Suggestion**
- **Charts**: ApexCharts hoặc Chart.js cho responsive
- **Maps**: D3.js hoặc Leaflet cho Vietnam heatmap  
- **Tables**: TanStack Table với sorting/filtering
- **Animations**: Framer Motion cho smooth transitions

### **⚡ Real-time Updates**
- **WebSocket**: Kết nối cho real-time alerts
- **Polling**: Refresh data mỗi 5 phút cho charts
- **Cache**: Local storage cho user preferences
- **Performance**: Lazy loading cho heavy components

---

## 🔧 **TECHNICAL DETAILS**

### **Status Codes**
- `200`: Success
- `400`: Bad Request (sai parameter)
- `500`: Internal Server Error

### **Time Periods**
- `today`: Hôm nay
- `week`: Tuần này (Thứ 2 - Chủ Nhật)
- `month`: Tháng này

### **Order Status tính doanh thu**
- `DELIVERED`: Đã giao thành công
- `PARTIALLY_REFUNDED`: Hoàn một phần

### **Order Status COD thất bại**
- `DELIVERY_FAILED`: Giao hàng thất bại
- `CANCELED`: Đã hủy
- `RETURNING_TO_WAREHOUSE`: Đang trả về kho

---

## 🚀 **ADVANCED INTELLIGENCE APIs (Phase 2)**

### 9. **⚡ API KPI TÌNH TRẠNG SỐNG CÒN - cho SURVIVAL KPIs**
```http
GET /api/orders/statistics/survival-kpis
```

**Mục đích**: Dashboard "sức khỏe" business với cảnh báo tức thời và hành động khẩn cấp
**Thiết kế Frontend**:
- 🚨 **Alert Dashboard**: Layout như "bệnh viện monitor" với các vital signs
- 🎨 **Color-coded cards**: Xanh (healthy), Vàng (warning), Đỏ (critical)
- ⚡ **Action buttons**: Nút "Tạo Flash Sale", "Send Voucher" với estimated impact
- 📊 **Real-time indicators**: Animated progress bars, pulsing alerts
- 💊 **Treatment suggestions**: "Doanh thu giảm → Tăng Flash Sale" với ROI dự kiến

**Response**:
```json
{
  "status": 200,
  "message": "Lấy KPI sống còn thành công",
  "data": {
    "dailyOrders": {
      "today": 45,
      "yesterday": 52,
      "growthRate": -13.46,
      "trend": "down",
      "alertLevel": "warning"
    },
    "weeklyRevenue": {
      "thisWeek": 125000000.00,
      "lastWeek": 156000000.00,
      "growthRate": -19.87,
      "trend": "down", 
      "alertLevel": "critical",
      "actionSuggestion": "Doanh thu giảm 20% so với tuần trước → tăng khuyến mãi Flash Sale cho 5 đầu sách hot"
    },
    "refundCancelRate": {
      "rate": 7.2,
      "threshold": 5.0,
      "alertLevel": "critical",
      "totalRefundCancel": 36,
      "totalOrders": 500,
      "actionSuggestion": "Tỷ lệ hoàn hủy 7.2% > 5% → kiểm tra chất lượng giao hàng"
    },
    "retentionRate": {
      "rate": 68.5,
      "previousRate": 72.1,
      "trend": "down",
      "returningCustomers": 342,
      "totalCustomers": 499,
      "alertLevel": "warning"
    },
    "instantActions": [
      {
        "priority": "high",
        "type": "flash_sale",
        "message": "Tạo Flash Sale 30% cho Top 5 sách hot",
        "actionUrl": "/admin/flash-sales/create",
        "estimatedImpact": "Tăng 25% doanh thu"
      },
      {
        "priority": "medium", 
        "type": "voucher_campaign",
        "message": "Gửi voucher 15% cho 150 khách VIP",
        "actionUrl": "/admin/vouchers/mass-send",
        "estimatedImpact": "Tăng retention 5%"
      }
    ]
  }
}
```

**Frontend**: Cards cảnh báo màu đỏ/vàng/xanh + nút hành động ngay

---

### 10. **🎯 API RADAR PHÁT HIỆN CƠ HỘI - cho OPPORTUNITY RADAR**
```http
GET /api/orders/statistics/opportunity-radar
```

**Mục đích**: "Radar" phát hiện cơ hội kinh doanh và khách hàng tiềm năng
**Thiết kế Frontend**:
- 🎯 **Opportunity Grid**: 4 tiles "Hot Books", "Trending", "VIP Returning", "Abandoned Carts"
- 📈 **Trend indicators**: Icon TikTok, Facebook cho trending books
- ⚠️ **Stock alerts**: "Chỉ còn 12 cuốn" với countdown timer
- 👑 **VIP pipeline**: Avatar + tên + ngày dự kiến quay lại + nút "Send Voucher"
- 🛒 **Cart recovery**: Giá trị giỏ hàng + thời gian bỏ + nút "Send Discount"

**Response**:
```json
{
  "status": 200,
  "message": "Lấy radar cơ hội thành công", 
  "data": {
    "hotTodayBooks": [
      {
        "bookId": 123,
        "bookTitle": "Atomic Habits",
        "soldToday": 25,
        "currentStock": 12,
        "stockoutRisk": "high",
        "estimatedStockoutDate": "2025-08-09",
        "reorderSuggestion": 50,
        "actionUrl": "/admin/inventory/reorder/123"
      }
    ],
    "trendingBooks": [
      {
        "bookId": 456,
        "bookTitle": "Think and Grow Rich", 
        "socialMentions": 1250,
        "trendScore": 0.89,
        "currentStock": 45,
        "suggestedOrder": 100,
        "trendSource": "TikTok, Facebook",
        "actionUrl": "/admin/inventory/bulk-order/456"
      }
    ],
    "vipReturningSoon": [
      {
        "userId": 789,
        "customerName": "Nguyễn Văn A",
        "email": "nguyenvana@gmail.com", 
        "predictedReturnDate": "2025-08-10",
        "averageOrderValue": 850000.00,
        "preferredCategories": ["Self-help", "Business"],
        "confidence": 0.92,
        "actionUrl": "/admin/customers/send-voucher/789"
      }
    ],
    "abandonedCarts": [
      {
        "userId": 101,
        "customerName": "Trần Thị B",
        "cartValue": 450000.00,
        "cartAge": "2 hours",
        "riskScore": 0.78,
        "suggestedDiscount": 10,
        "actionUrl": "/admin/marketing/recovery-email/101"
      }
    ]
  }
}
```

**Frontend**: Dashboard tiles với nút 1-click actions

---

### 11. **🗺️ API BẢN ĐỒ SỨC KHỎE ĐƠN HÀNG - cho HEALTH HEATMAP**
```http
GET /api/orders/statistics/order-health-map
```

**Mục đích**: "Bản đồ bệnh tật" của hệ thống đơn hàng - phát hiện vấn đề theo vùng
**Thiết kế Frontend**:
- 🗺️ **Vietnam Health Map**: Màu xanh (healthy), vàng (concerning), đỏ (critical)
- 🚛 **Shipping scoreboard**: Bảng xếp hạng đơn vị vận chuyển theo success rate
- ⚠️ **Risk order table**: Bảng đơn hàng có nguy cơ với nút "Call Now", "Expedite"
- 📊 **Health score meter**: Gauge chart 0-100 cho overall health
- 🎯 **Action zones**: Click vùng đỏ → "Tăng marketing tại Hà Nội"

**Response**:
```json
{
  "status": 200,
  "message": "Lấy bản đồ sức khỏe đơn hàng thành công",
  "data": {
    "regionHealth": [
      {
        "provinceName": "Hồ Chí Minh",
        "provinceId": 79,
        "healthScore": 0.95,
        "orderGrowth": 15.2,
        "deliverySuccessRate": 94.5,
        "status": "excellent",
        "alertLevel": "green"
      },
      {
        "provinceName": "Hà Nội", 
        "provinceId": 1,
        "healthScore": 0.65,
        "orderGrowth": -30.1,
        "deliverySuccessRate": 87.2,
        "status": "concerning",
        "alertLevel": "red",
        "actionSuggestion": "Khu vực Hà Nội giảm 30% → tăng marketing địa phương"
      }
    ],
    "shippingPartners": [
      {
        "partnerName": "Giao Hàng Nhanh",
        "successRate": 96.2,
        "avgDeliveryTime": 2.5,
        "failureReasons": ["Khách không nghe máy", "Sai địa chỉ"],
        "alertLevel": "green"
      }
    ],
    "riskOrders": [
      {
        "orderId": 12345,
        "customerName": "Lê Văn C",
        "riskType": "COD_BOMB", 
        "riskScore": 0.85,
        "daysOverdue": 2,
        "orderValue": 650000.00,
        "actionUrl": "/admin/orders/contact/12345"
      },
      {
        "orderId": 12346,
        "customerName": "Phạm Thị D",
        "riskType": "DELAYED_SHIPPING",
        "riskScore": 0.72,
        "expectedDelivery": "2025-08-05",
        "currentDelay": 3,
        "actionUrl": "/admin/orders/expedite/12346"
      }
    ]
  }
}
```

**Frontend**: Heatmap Vietnam + tables + risk alerts

---

### 12. **💎 API MÁY QUẢN TRỊ LỢI NHUẬN - cho PROFIT OPTIMIZER**
```http
GET /api/orders/statistics/profit-optimizer
```

**Mục đích**: Tối ưu hóa lợi nhuận - tìm sản phẩm "ngọc thô" và cơ hội tăng giá
**Thiết kế Frontend**:
- 💎 **Hidden gems table**: Sách lợi nhuận cao nhưng bán ít + nút "Boost Marketing"
- 📊 **Profit matrix**: Bubble chart (X: Sales volume, Y: Profit margin, Size: Revenue)
- 💰 **Pricing opportunities**: Bảng đề xuất tăng giá với risk level và expected ROI
- 🎯 **Category performance**: Bar chart lợi nhuận theo thể loại
- ⚡ **Quick actions**: "Apply suggested prices", "Create marketing campaign"

**Response**:
```json
{
  "status": 200,
  "message": "Lấy tối ưu lợi nhuận thành công",
  "data": {
    "highMarginLowSale": [
      {
        "bookId": 789,
        "bookTitle": "Advanced Java Programming",
        "profitMargin": 45.5,
        "unitsSold": 5,
        "potentialRevenue": 2500000.00,
        "marketingBudgetSuggestion": 500000.00,
        "expectedROI": 400,
        "actionUrl": "/admin/marketing/boost/789"
      }
    ],
    "profitAnalysis": {
      "totalProfit": 125000000.00,
      "profitMargin": 28.5,
      "topProfitCategory": "Technology Books",
      "underperformingCategories": ["Poetry", "Art Books"]
    },
    "pricingOpportunities": [
      {
        "bookId": 456,
        "currentPrice": 250000.00,
        "competitorAvgPrice": 280000.00,
        "suggestedPrice": 270000.00,
        "potentialProfit": "Tăng 8% lợi nhuận",
        "riskLevel": "low"
      }
    ]
  }
}
```

---

### 13. **🚨 API CẢNH BÁO TỨC THỜI - cho REAL-TIME ALERTS**
```http
GET /api/orders/statistics/real-time-alerts
```

**Mục đích**: Hệ thống cảnh báo "112" cho business - alerts quan trọng không được bỏ lỡ
**Thiết kế Frontend**:
- 🚨 **Alert Center**: Notification panel với badges đỏ cho critical alerts
- 🔔 **Toast notifications**: Pop-up góc màn hình cho alerts mới
- 📋 **Alert list**: Phân loại Critical/Warning/Info với icon và priority
- ⏰ **Auto-refresh**: Polling mỗi 30 giây hoặc WebSocket real-time
- 🎯 **One-click actions**: Từng alert có nút hành động trực tiếp

**Response**:
```json
{
  "status": 200,
  "message": "Lấy cảnh báo thời gian thực thành công",
  "data": {
    "criticalAlerts": [
      {
        "id": "alert_001",
        "type": "COD_UNCONFIRMED",
        "severity": "high",
        "title": "15 đơn COD chưa xác nhận sau 24h",
        "message": "⚠️ 15 đơn COD chưa xác nhận sau 24h → dễ bị bom",
        "affectedCount": 15,
        "totalValue": 12500000.00,
        "actionUrl": "/admin/orders/cod-confirmation",
        "createdAt": "2025-08-07T06:30:00",
        "priority": 1
      },
      {
        "id": "alert_002", 
        "type": "LOW_INVENTORY",
        "severity": "medium",
        "title": "Sách hot sắp hết hàng",
        "message": "🔥 Ấn phẩm Harry Potter bản giới hạn chỉ còn 8 cuốn",
        "bookId": 999,
        "currentStock": 8,
        "dailyAvgSales": 3,
        "stockoutEstimate": "3 days",
        "actionUrl": "/admin/inventory/reorder/999",
        "priority": 2
      }
    ],
    "warningAlerts": [
      {
        "id": "alert_003",
        "type": "REVENUE_DROP",
        "severity": "medium", 
        "title": "Doanh thu khu vực giảm mạnh",
        "message": "📉 Khu vực Hà Nội giảm 30% doanh thu so với tuần trước",
        "region": "Hà Nội",
        "dropPercentage": -30.1,
        "actionUrl": "/admin/marketing/regional-campaign",
        "priority": 3
      }
    ],
    "infoAlerts": [
      {
        "id": "alert_004",
        "type": "TREND_OPPORTUNITY",
        "severity": "low",
        "title": "Cơ hội trending mới",
        "message": "📈 Sách 'Tâm lý học đám đông' đang viral TikTok",
        "socialMentions": 2500,
        "actionUrl": "/admin/inventory/trend-analysis",
        "priority": 4
      }
    ],
    "alertStats": {
      "totalActive": 28,
      "critical": 2,
      "warning": 8,
      "info": 18,
      "resolved24h": 15
    }
  }
}
```

**Frontend**: Toast notifications + Alert center + Priority badges

---

### 14. **⚡ API HÀNH ĐỘNG NGAY - cho ACTION CENTER**
```http
POST /api/orders/statistics/quick-actions
```

**Mục đích**: "Control center" thực hiện hành động kinh doanh tức thời - không cần qua nhiều bước
**Thiết kế Frontend**:
- ⚡ **Quick Action Buttons**: Big buttons "Create Flash Sale", "Send VIP Vouchers", "Bulk Order"
- 🎮 **Gaming UI**: Buttons như game với countdown timers và visual effects
- 📊 **Impact preview**: "Expected: +25% revenue" trước khi execute
- ✅ **Confirmation modal**: "Tạo Flash Sale 30% cho 5 sách hot?" với estimated impact
- 📈 **Real-time feedback**: Progress bar khi executing + success animation

**Request Body**:
```json
{
  "actionType": "CREATE_FLASH_SALE",
  "parameters": {
    "bookIds": [123, 456, 789],
    "discountPercentage": 30,
    "duration": 24,
    "targetRegion": "all"
  }
}
```

**Response**:
```json
{
  "status": 200,
  "message": "Hành động được thực hiện thành công",
  "data": {
    "actionId": "action_001",
    "actionType": "CREATE_FLASH_SALE",
    "status": "completed",
    "result": {
      "flashSaleId": 789,
      "flashSaleName": "Flash Sale Auto - Top Books 30%",
      "startTime": "2025-08-07T07:00:00",
      "endTime": "2025-08-08T07:00:00", 
      "expectedImpact": "Tăng 25% doanh thu trong 24h",
      "trackingUrl": "/admin/flash-sales/789"
    },
    "executedAt": "2025-08-07T06:45:00"
  }
}
```

**Các Action Types**:
- `CREATE_FLASH_SALE`: Tạo flash sale tự động
- `SEND_VIP_VOUCHER`: Gửi voucher cho khách VIP
- `BULK_INVENTORY_ORDER`: Đặt hàng số lượng lớn
- `AUTO_COD_CALL`: Tự động gọi xác nhận COD
- `RECOVERY_EMAIL_CAMPAIGN`: Chiến dịch email giỏ hàng bỏ
- `REGIONAL_MARKETING_BOOST`: Đẩy marketing khu vực

---

## 🎨 **ADVANCED DASHBOARD MAPPING**

| API Endpoint | Component | Mục Đích | UI Design |
|--------------|-----------|----------|-----------|
| `/survival-kpis` | **Alert Cards + Action Buttons** | KPI sống còn + hành động ngay | Hospital monitor style với vital signs |
| `/opportunity-radar` | **Opportunity Grid + 1-Click Actions** | Phát hiện cơ hội kinh doanh | 4-tile grid với action buttons |
| `/order-health-map` | **Vietnam Heatmap + Risk Tables** | Sức khỏe đơn hàng theo vùng | Interactive map + data tables |
| `/profit-optimizer` | **Profit Analytics + Suggestions** | Tối ưu lợi nhuận thông minh | Bubble chart + pricing tables |
| `/real-time-alerts` | **Alert Center + Toast Notifications** | Cảnh báo thời gian thực | Notification panel + popups |
| `/quick-actions` | **Action Center + Workflow Automation** | Hành động tức thời 1-click | Gaming-style action buttons |

---

## 📱 **COMPLETE DASHBOARD LAYOUT SUGGESTION**

```
┌─────────────────────────────────────────────────────────────────┐
│                    📊 BOOKSTATION ADMIN DASHBOARD              │
├─────────────────────────────────────────────────────────────────┤
│  🚨 SURVIVAL KPIs (API #9)                    🔔 ALERTS (API #13) │
│  ┌─────┐ ┌─────┐ ┌─────┐ ┌─────┐              ┌─────────────────┐ │
│  │📦 45│ │💰12M│ │🔄7.2%│ │👑68%│              │⚠️ 15 COD unconf │ │
│  │▼13% │ │▼20% │ │ RED │ │▼ 4%│              │🔥 8 books left  │ │
│  └─────┘ └─────┘ └─────┘ └─────┘              └─────────────────┘ │
├─────────────────────────────────────────────────────────────────┤
│  📈 REVENUE CHART (API #2)          🏆 TOP PRODUCTS (API #3)     │
│  ┌─────────────────────────────────┐  ┌─────────────────────────┐ │
│  │    💰 Revenue Trend             │  │ #1 📚 Atomic Habits     │ │
│  │  ╭─╮                           │  │ #2 📚 Think & Grow Rich │ │
│  │ ╱   ╲                          │  │ #3 📚 Clean Code        │ │
│  │╱     ╲─╲                       │  │ #4 📚 Design Patterns   │ │
│  └─────────────────────────────────┘  └─────────────────────────┘ │
├─────────────────────────────────────────────────────────────────┤
│  🎯 OPPORTUNITY RADAR (API #10)                                  │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐ ┌─────────────┐ │
│  │🔥 HOT TODAY │ │📈 TRENDING  │ │👑 VIP RETURN│ │🛒 ABANDONED │ │
│  │12 cuốn còn  │ │TikTok viral │ │3 ngày nữa   │ │2 giờ trước  │ │
│  │⚡Reorder    │ │⚡Order 100  │ │⚡Send Voucher│ │⚡10% Discount│ │
│  └─────────────┘ └─────────────┘ └─────────────┘ └─────────────┘ │
├─────────────────────────────────────────────────────────────────┤
│  🗺️ HEALTH MAP (API #11)              🥧 PAYMENT (API #4)        │
│  ┌─────────────────────────┐          ┌───────────────────────────┐ │
│  │     🗺️ Vietnam Map       │          │       💳 COD vs Online    │ │
│  │  🟢 HCM: Excellent      │          │   🥧     72%      28%     │ │
│  │  🔴 HN: Critical        │          │        COD      Online    │ │
│  │  🟡 DN: Warning         │          └───────────────────────────┘ │
│  └─────────────────────────┘                                      │
└─────────────────────────────────────────────────────────────────┘
```

---

## 💡 **FRONTEND DEVELOPMENT PRIORITIES**

### **Phase 1 - Core Dashboard (Week 1)**
1. ✅ **Basic Cards** (API #1) - Overview statistics
2. ✅ **Revenue Chart** (API #2) - Line chart with time filters  
3. ✅ **Top Products** (API #3) - Horizontal bar chart

### **Phase 2 - Analytics (Week 2)**
4. ✅ **Payment Methods** (API #4) - Pie chart
5. ✅ **Location Heatmap** (API #5) - Vietnam SVG map
6. ✅ **Revenue Comparison** (API #6) - Trend comparison cards

### **Phase 3 - Intelligence (Week 3)**
7. ✅ **Survival KPIs** (API #9) - Alert dashboard
8. ✅ **Opportunity Radar** (API #10) - 4-tile opportunity grid
9. ✅ **Real-time Alerts** (API #13) - Notification system

### **Phase 4 - Advanced Features (Week 4)**
10. ✅ **Customer Analytics** (API #7) - CRM tables
11. ✅ **Health Map** (API #11) - Regional analysis  
12. ✅ **Quick Actions** (API #14) - Action center
13. ✅ **Cross-sell** (API #8) - Product suggestions

---

## 🧠 **AI BUSINESS INTELLIGENCE FEATURES**

### **Predictive Analytics**
- Dự đoán khách hàng sắp quay lại mua (ML model)
- Dự báo hết hàng dựa trên trend bán hàng
- Phân tích rủi ro COD bomb bằng behavior pattern

### **Smart Suggestions**  
- Gợi ý combo sách để upsell dựa trên purchase history
- Tối ưu giá bán theo competitor analysis
- Khuyến nghị marketing budget allocation

### **Real-time Monitoring**
- WebSocket alerts cho các sự kiện critical
- Auto-trigger actions khi threshold vượt ngưỡng
- Continuous health score calculation

### **Business Health Score**
- Overall business health: 0-100
- Breakdown theo: Sales, Inventory, Customer, Operations
- Historical health tracking và trend analysis

---

## 🚀 **ENDPOINT BASE URLs UPDATE**

**⚠️ LƯU Ý**: Advanced APIs đã được triển khai với base URL khác:

### **Basic Analytics APIs (APIs 1-8)**:
```
Base URL: /api/order-statistics/
```

### **Advanced Intelligence APIs (APIs 9-14)**:  
```
Base URL: /api/advanced-analytics/
```

**Ví dụ**:
- ✅ `GET /api/order-statistics/overview` (API #1)
- ✅ `GET /api/advanced-analytics/survival-kpis` (API #9)
- ✅ `POST /api/advanced-analytics/quick-actions` (API #14)

---
  