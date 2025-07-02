# NGHIỆP VỤ SẢN PHẨM XU HƯỚNG (TRENDING PRODUCTS)

## 📋 TỔNG QUAN
API `/api/books/trending` cung cấp danh sách các sản phẩm sách đang có xu hướng cao dựa trên các tiêu chí phân tích dữ liệu kinh doanh thực tế.

## 🎯 ĐỊNH NGHĨA SẢN PHẨM XU HƯỚNG

### Một sản phẩm được coi là "xu hướng" khi:

1. **Doanh số bán hàng tốt trong thời gian gần đây**
   - Được đặt mua nhiều trong 30 ngày qua
   - Có tần suất mua cao (số lượng đơn hàng)
   - Tổng số lượng bán ra lớn

2. **Tương tác tích cực từ khách hàng**
   - Có đánh giá/review tích cực (rating >= 4.0)
   - Số lượng review nhiều (thể hiện sự quan tâm)

3. **Trạng thái hoạt động**
   - Sản phẩm đang active (status = 1)
   - Còn hàng trong kho (stock_quantity > 0)

4. **Thời gian tạo gần đây** (tùy chọn)
   - Sản phẩm mới ra mắt cũng có thể trending

## 📊 THUẬT TOÁN XẾP HẠNG TRENDING

### Công thức tính điểm trending:
```
Trending Score = (Sales Score × 0.4) + (Review Score × 0.3) + (Recency Score × 0.2) + (Flash Sale Bonus × 0.1)
```

### Chi tiết các thành phần:

#### 1. Sales Score (40% trọng số)
- **Số đơn hàng trong 30 ngày**: Càng nhiều đơn hàng càng cao điểm
- **Tổng số lượng bán**: Số lượng sách đã bán
- **Tần suất mua**: Thường xuyên được đặt mua

```sql
Sales Score = (total_orders_30d × 0.5) + (total_quantity_sold_30d × 0.3) + (order_frequency × 0.2)
```

#### 2. Review Score (30% trọng số)
- **Rating trung bình**: >= 4.0 điểm tối đa, thang 5
- **Số lượng review**: Nhiều review = nhiều quan tâm
- **Review gần đây**: Review trong 30 ngày qua có trọng số cao hơn

```sql
Review Score = (avg_rating × 0.6) + (review_count × 0.4)
```

#### 3. Recency Score (20% trọng số)
- **Thời gian tạo sản phẩm**: Sản phẩm mới có điểm cao hơn
- **Hoạt động gần đây**: Cập nhật thông tin, thay đổi giá

#### 4. Flash Sale Bonus (10% trọng số)
- **Đang trong flash sale**: +20% điểm
- **Từng có flash sale**: +10% điểm

## 🔍 TIÊU CHÍ LỌC DỮ LIỆU

### Điều kiện bắt buộc:
1. `book.status = 1` (Active)
2. `book.stock_quantity > 0` (Còn hàng)
3. `book.price > 0` (Có giá bán)

### Thời gian phân tích:
- **Dữ liệu bán hàng**: 30 ngày gần nhất
- **Dữ liệu review**: 60 ngày gần nhất
- **Cập nhật ranking**: Mỗi 6 giờ/ngày

## 📈 CÁC TRƯỜNG HỢP BUSINESS

### Case 1: Sách bán chạy đều đặn
- Sách có doanh số ổn định hàng ngày
- Rating tốt, nhiều review tích cực
- **VD**: Sách giáo khoa, sách tham khảo phổ biến

### Case 2: Sách hot trend mới
- Sách mới phát hành, được quan tâm cao
- Có thể ít review nhưng đặt mua nhiều
- **VD**: Sách của tác giả nổi tiếng mới ra mắt

### Case 3: Sách có khuyến mãi hấp dẫn
- Đang trong flash sale hoặc có discount lớn
- Tăng đột biến về số lượng đặt mua
- **VD**: Sách giảm giá 50% trong flash sale

### Case 4: Sách theo mùa/sự kiện
- Phù hợp với thời điểm, sự kiện đặc biệt
- **VD**: Sách luyện thi vào mùa thi, sách Tết

## 🚫 LOẠI TRỪ KHỎI TRENDING

### Không được xếp hạng trending:
1. **Sách hết hàng** (`stock_quantity = 0`)
2. **Sách bị disable** (`status = 0`)
3. **Sách không có giá** (`price <= 0`)
4. **Sách có vấn đề về chất lượng**:
   - Rating trung bình < 2.0
   - Có quá nhiều review tiêu cực gần đây
5. **Sách vi phạm policy** (nếu có trường đánh dấu)

## 📋 API RESPONSE FORMAT

### Response Structure:
```json
{
    "status": 200,
    "message": "Lấy danh sách sản phẩm xu hướng thành công",
    "data": {
        "content": [
            {
                "id": 1,
                "bookName": "Tên sách",
                "description": "Mô tả",
                "price": 200000,
                "originalPrice": 250000,
                "discountPercentage": 20,
                "stockQuantity": 50,
                "imageUrl": "url_image",
                "category": {
                    "id": 1,
                    "categoryName": "Văn học"
                },
                "authors": [
                    {
                        "id": 1,
                        "authorName": "Tác giả"
                    }
                ],
                "rating": 4.5,
                "reviewCount": 120,
                "soldCount": 85,
                "trendingScore": 8.7,
                "trendingRank": 1,
                "isInFlashSale": true,
                "flashSalePrice": 180000
            }
        ],
        "totalElements": 20,
        "pageNumber": 0,
        "pageSize": 10,
        "totalPages": 2
    }
}
```

### Trending Score Explanation:
- **0-3**: Ít được quan tâm
- **3-5**: Bình thường
- **5-7**: Khá hot
- **7-8.5**: Rất hot
- **8.5-10**: Cực kỳ trending

## ⚙️ CẤU HÌNH VÀ THAM SỐ

### Query Parameters:
- `page`: Trang hiện tại (default: 0)
- `size`: Số sản phẩm mỗi trang (default: 10, max: 50)
- `categoryId`: Lọc theo danh mục (optional)
- `minPrice`: Giá tối thiểu (optional)
- `maxPrice`: Giá tối đa (optional)

### Cấu hình hệ thống:
```properties
# Trending configuration
trending.analysis.days=30
trending.min.orders=5
trending.min.rating=2.0
trending.cache.hours=6
trending.max.results=100
```

## 🔄 CẬP NHẬT DỮ LIỆU

### Tần suất cập nhật:
1. **Real-time**: Khi có đơn hàng mới, review mới (SMART INVALIDATION)
2. **Batch job**: Tính toán lại trending score mỗi 6 giờ
3. **Manual**: Admin có thể trigger cập nhật manually

### 🔥 SMART REAL-TIME INVALIDATION:
- **Đơn hàng mới**: Invalidate khi quantity >= 5 hoặc mỗi 10 đơn hàng
- **Review mới**: Invalidate khi rating >= 4.0 hoặc <= 2.0 (review quan trọng)
- **Flash sale**: Invalidate ngay lập tức (impact lớn)
- **Threshold**: Tránh invalidate quá thường xuyên, gây overhead

### Cache Strategy:
- Cache kết quả trending 6 giờ
- Invalidate cache thông minh khi có thay đổi quan trọng
- Separate cache cho từng category
- **Fallback cache**: Cache riêng cho sản phẩm dự phòng

### 🔥 FALLBACK ALGORITHM - Khi chưa có đủ dữ liệu:
1. **Ưu tiên sách mới** (createdAt DESC) - 60% trọng số
2. **Giá cả hợp lý** (50k-200k VND) - 20% trọng số  
3. **Stock nhiều** (>= 50) - 20% trọng số
4. **Trending score < 5.0** để phân biệt với trending thực

### Fallback Cases:
- **Website mới**: Chưa có đơn hàng, review → Hiển thị sách mới nhất
- **Category ít sản phẩm**: Bổ sung từ sách active khác
- **Thời gian ít hoạt động**: Đảm bảo luôn có list để hiển thị

## 📊 MONITORING VÀ ANALYTICS

### Metrics cần theo dõi:
1. **API Performance**: Response time, error rate
2. **Business Metrics**:
   - Số sản phẩm trending mỗi ngày
   - Conversion rate từ trending → purchase
   - Click-through rate trên trending products
3. **Data Quality**:
   - Số sản phẩm đủ điều kiện trending
   - Distribution của trending scores
4. **🔥 Cache Metrics**:
   - Cache hit rate
   - Số lần invalidation (orders, reviews, flash sales)
   - Fallback usage rate
   - Real-time invalidation effectiveness

### 🔥 FALLBACK SUCCESS METRICS:
- **Coverage**: 100% requests có ít nhất 1 sản phẩm
- **Quality**: 80%+ fallback products có interaction trong 7 ngày
- **Performance**: Fallback response time < 200ms

## 🎯 SUCCESS CRITERIA

### API được coi là thành công khi:
1. **Accuracy**: 80%+ sản phẩm trending thực sự có performance tốt
2. **Performance**: Response time < 500ms
3. **Business Impact**: 
   - Tăng 15% conversion rate so với sản phẩm thường
   - Click-through rate > 20% trên trending section
4. **User Satisfaction**: 
   - Feedback tích cực từ frontend team
   - Ít complaint về "sản phẩm trending không đúng"

---

## 📝 NOTES

**Lý do dựa vào các metrics này:**
1. **Doanh số**: Phản ánh sự yêu thích thực tế của khách hàng
2. **Review**: Thể hiện chất lượng và sự hài lòng
3. **Thời gian**: Trend thường liên quan đến độ mới
4. **Flash sale**: Tạo momentum mua hàng

**Không dùng metrics không đáng tin cậy:**
- View count (có thể fake)
- Wishlist count (ít ý nghĩa thương mại)
- Social media mentions (ngoài scope của hệ thống)

Nghiệp vụ này đảm bảo API trending products có cơ sở khoa học, phản ánh đúng xu hướng thị trường và mang lại giá trị kinh doanh thực tế.
