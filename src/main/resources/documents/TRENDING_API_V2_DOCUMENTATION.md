# 🔥 API TRENDING PRODUCTS - DOCUMENTATION

## 📋 TỔNG QUAN
API lấy danh sách sản phẩm xu hướng sử dụng **POST method** với request body.

**Endpoint duy nhất:**
- **POST** `/api/books/trending` - Tất cả queries gửi trong body

**Lý do dùng POST cho việc "lấy danh sách":**
- ✅ URL clean, không dài dòng
- ✅ Parameters có cấu trúc rõ ràng trong JSON
- ✅ Type safety và validation tốt hơn
- ✅ Dễ mở rộng thêm filters phức tạp

Hỗ trợ 2 loại trending:
1. **DAILY_TRENDING**: Xu hướng theo ngày (dựa trên sales + reviews + recency)
2. **HOT_DISCOUNT**: Sách hot giảm sốc (flash sale + discount cao)

## 🚀 **ENDPOINT**

### **POST /api/books/trending** 
```json
{
  "type": "DAILY_TRENDING",         // "DAILY_TRENDING" hoặc "HOT_DISCOUNT"
  "page": 0,                        // Trang hiện tại (default: 0)
  "size": 10,                       // Số sách mỗi trang (default: 10, max: 50)
  "categoryId": null,               // ID danh mục (chỉ cho HOT_DISCOUNT)
  "minPrice": null,                 // Giá tối thiểu
  "maxPrice": null,                 // Giá tối đa
  "minDiscountPercentage": null,    // % giảm giá tối thiểu (chỉ cho HOT_DISCOUNT)
  "flashSaleOnly": false            // Chỉ flash sale (chỉ cho HOT_DISCOUNT)
}
```

## 📊 **SAMPLE REQUESTS**

### 1. **Preview Mode - Hiển thị vài sách đầu (5 items)**
```bash
POST /api/books/trending
Content-Type: application/json

{
  "type": "DAILY_TRENDING",
  "page": 0,
  "size": 5          // Chỉ lấy 5 sách cho preview
}
```
**💡 Use case:** Trang chủ hiển thị vài sách trending, có nút "Xem thêm"

### 2. **Full List Mode - Hiển thị danh sách đầy đủ**
```bash
POST /api/books/trending
Content-Type: application/json

{
  "type": "DAILY_TRENDING",
  "page": 0,
  "size": 20         // Lấy nhiều sách hơn cho trang chi tiết
}
```
**💡 Use case:** Trang trending riêng hoặc khi user ấn "Xem thêm"

### 3. **Hot Discount Preview**
```bash
POST /api/books/trending
Content-Type: application/json

{
  "type": "HOT_DISCOUNT",
  "page": 0,
  "size": 5          // Preview hot discount books
}
```

### 4. **Hot Discount với filters - Full list**
```bash
POST /api/books/trending
Content-Type: application/json

{
  "type": "HOT_DISCOUNT",
  "categoryId": 1,
  "minDiscountPercentage": 30,
  "flashSaleOnly": true,
  "minPrice": 50000,
  "maxPrice": 500000,
  "page": 0,
  "size": 20         // Full list với filters
}
```

### 5. **Pagination - Load more**
```bash
POST /api/books/trending
Content-Type: application/json

{
  "type": "DAILY_TRENDING",
  "page": 1,         // Trang tiếp theo
  "size": 10         // Số items mỗi lần load more
}
```
**➡️ Trả về:** Sách flash sale ở danh mục 1, giảm giá >= 30%

### 4. **DAILY_TRENDING với phân trang và giá**
```bash
GET /api/books/trending?type=DAILY_TRENDING&page=1&size=20&minPrice=50000&maxPrice=200000
```
**➡️ Trả về:** 20 sách trending trang 2, giá từ 50k-200k VND

### 5. **Chỉ flash sale**
```bash
GET /api/books/trending?type=HOT_DISCOUNT&flashSaleOnly=true
```
**➡️ Trả về:** Chỉ sách đang trong flash sale
```bash
GET /api/books/trending?page=0&size=20&minPrice=50000&maxPrice=200000
```
## 🎯 **BUSINESS LOGIC**

### **Trending Algorithm:**
```
Score = (Sales 40%) + (Reviews 30%) + (Recency 20%) + (Flash Sale 10%)
```

**Chi tiết tính điểm:**
- **Sales Score (40%)**: Dựa trên số lượng đã bán + số đơn hàng trong 30 ngày
- **Review Score (30%)**: Rating trung bình + số lượng đánh giá  
- **Recency Score (20%)**: Sách mới (< 30 ngày) được ưu tiên
- **Flash Sale Bonus (10%)**: Sách đang sale được cộng điểm

**Điều kiện lọc:**
- Chỉ sách active (`status = 1`)
- Còn hàng (`stockQuantity > 0`)
- Đơn hàng hoàn thành (`orderStatus = COMPLETED`)

## 📋 **RESPONSE FORMAT**
```json
{
  "status": 200,
  "message": "Lấy danh sách sản phẩm xu hướng thành công",
  "data": {
    "content": [
      {
        "id": 1,
        "bookName": "Atomic Habits",
        "description": "Cuốn sách về xây dựng thói quen tích cực",
        "price": 180000,
        "originalPrice": 200000,
        "discountPercentage": 10,
        "stockQuantity": 50,
        "imageUrl": "https://example.com/book1.jpg",
        "bookCode": "BOOK001",
        "publicationDate": 1640995200000,
        "categoryId": 1,
        "categoryName": "Phát triển bản thân",
        "authors": [
          {
            "id": 1,
            "authorName": "James Clear",
            "biography": "Tác giả bestseller",
            "birthDate": 1985,
            "status": 1
          }
        ],
        "supplierId": 1,
        "supplierName": "Nhà sách Fahasa",
        "rating": 4.8,
        "reviewCount": 245,
        "soldCount": 180,
        "orderCount": 95,
        "trendingScore": 8.7,
        "trendingRank": 1,
        "isInFlashSale": true,
        "flashSalePrice": 160000,
        "flashSaleStockQuantity": 20,
        "createdAt": 1640995200000,
        "updatedAt": 1751357213276
      }
    ],
    "totalElements": 25,
    "pageNumber": 0,
    "pageSize": 10,
    "totalPages": 3
  }
}
```

## 🔄 **FRONTEND INTEGRATION**

### **JavaScript Examples:**

#### **1. Function tổng quát cho mọi trending query**
```javascript
async function getTrendingBooks(trendingRequest) {
  try {
    const response = await fetch('/api/books/trending', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(trendingRequest)
    });
    
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }
    
    return await response.json();
  } catch (error) {
    console.error('Error fetching trending books:', error);
    throw error;
  }
}
```

#### **2. DAILY_TRENDING - Sách bán chạy theo ngày**
```javascript
// Cơ bản
async function getDailyTrending(page = 0, size = 10) {
  const request = {
    type: 'DAILY_TRENDING',
    page: page,
    size: size
  };
  
  return await getTrendingBooks(request);
}

// Với filter giá
async function getDailyTrendingWithPrice(minPrice, maxPrice) {
  const request = {
    type: 'DAILY_TRENDING',
    page: 0,
    size: 15,
    minPrice: minPrice,
    maxPrice: maxPrice
  };
  
  return await getTrendingBooks(request);
}
```

#### **3. HOT_DISCOUNT - Sách giảm giá sốc**
```javascript
// Hot discount cơ bản
async function getHotDiscount(page = 0, size = 10) {
  const request = {
    type: 'HOT_DISCOUNT',
    page: page,
    size: size
  };
  
  return await getTrendingBooks(request);
}

// Flash sale trong danh mục cụ thể
async function getFlashSaleByCategory(categoryId, minDiscount = 20) {
  const request = {
    type: 'HOT_DISCOUNT',
    categoryId: categoryId,
    flashSaleOnly: true,
    minDiscountPercentage: minDiscount,
    page: 0,
    size: 15
  };
  
  return await getTrendingBooks(request);
}

// Hot discount với full filters
async function getHotDiscountFiltered(filters) {
  const request = {
    type: 'HOT_DISCOUNT',
    page: 0,
    size: 20,
    ...filters  // categoryId, minPrice, maxPrice, minDiscountPercentage, flashSaleOnly
  };
  
  return await getTrendingBooks(request);
}
```

#### **4. Axios Examples**
```javascript
import axios from 'axios';

// Axios wrapper
async function getTrendingBooksAxios(trendingRequest) {
  try {
    const response = await axios.post('/api/books/trending', trendingRequest);
    return response.data;
  } catch (error) {
    console.error('Error:', error.response?.data || error.message);
    throw error;
  }
}

// Sử dụng
async function example() {
  // Daily trending
  const dailyTrending = await getTrendingBooksAxios({
    type: 'DAILY_TRENDING',
    page: 0,
    size: 10
  });

  // Hot discount với filters
  const hotBooks = await getTrendingBooksAxios({
    type: 'HOT_DISCOUNT',
    categoryId: 1,
    minDiscountPercentage: 30,
    flashSaleOnly: true,
    page: 0,
    size: 15
  });
}
```

#### **5. React Component Example - Preview + Full List**
```javascript
import { useState, useEffect } from 'react';

function TrendingBooksSection() {
  const [books, setBooks] = useState([]);
  const [loading, setLoading] = useState(false);
  const [isPreviewMode, setIsPreviewMode] = useState(true);
  const [totalItems, setTotalItems] = useState(0);
  const [currentPage, setCurrentPage] = useState(0);

  // Function to fetch trending books
  const fetchTrendingBooks = async (isPreview = true, page = 0) => {
    setLoading(true);
    try {
      const request = {
        type: 'DAILY_TRENDING',
        page: page,
        size: isPreview ? 5 : 20  // 5 for preview, 20 for full list
      };
      
      const response = await fetch('/api/books/trending', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(request)
      });
      
      const data = await response.json();
      
      if (page === 0) {
        setBooks(data.data.content);
      } else {
        // Append for pagination/load more
        setBooks(prev => [...prev, ...data.data.content]);
      }
      
      setTotalItems(data.data.totalElements);
      setCurrentPage(page);
    } catch (error) {
      console.error('Error fetching trending books:', error);
    } finally {
      setLoading(false);
    }
  };

  // Load preview on component mount
  useEffect(() => {
    fetchTrendingBooks(true, 0);
  }, []);

  // Handle "Xem thêm" button click
  const handleViewMore = () => {
    setIsPreviewMode(false);
    fetchTrendingBooks(false, 0);  // Load full list from page 0
  };

  // Handle "Thu gọn" button click  
  const handleCollapse = () => {
    setIsPreviewMode(true);
    fetchTrendingBooks(true, 0);   // Back to preview mode
  };

  // Handle load more for pagination
  const handleLoadMore = () => {
    fetchTrendingBooks(false, currentPage + 1);
  };

  return (
    <div className="trending-section">
      <h2>📚 Sách Trending</h2>
      
      {/* Books Grid */}
      <div className="books-grid">
        {books.map(book => (
          <div key={book.id} className="book-card">
            <img src={book.imageUrl} alt={book.bookName} />
            <h3>{book.bookName}</h3>
            <p className="price">{book.price.toLocaleString()}đ</p>
            <p className="sold">Đã bán: {book.soldCount}</p>
            {book.discountPercentage > 0 && (
              <span className="discount">-{book.discountPercentage}%</span>
            )}
          </div>
        ))}
      </div>

      {/* Loading */}
      {loading && <p>Đang tải...</p>}

      {/* Action Buttons */}
      <div className="actions">
        {isPreviewMode && totalItems > 5 && (
          <button 
            onClick={handleViewMore}
            className="btn-primary"
            disabled={loading}
          >
            Xem thêm ({totalItems - 5} sách khác)
          </button>
        )}

        {!isPreviewMode && (
          <>
            <button 
              onClick={handleCollapse}
              className="btn-secondary"
            >
              Thu gọn
            </button>
            
            {/* Show load more if có thêm pages */}
            {books.length < totalItems && (
              <button 
                onClick={handleLoadMore}
                className="btn-outline"
                disabled={loading}
              >
                Tải thêm ({totalItems - books.length} sách khác)
              </button>
            )}
          </>
        )}
      </div>
    </div>
  );
}
```

#### **6. Homepage Integration Example**
```javascript
// Homepage component với multiple trending sections
function Homepage() {
  return (
    <div className="homepage">
      {/* Daily Trending Preview */}
      <TrendingSection 
        type="DAILY_TRENDING" 
        title="📈 Trending hôm nay"
        previewSize={5}
        fullSize={20}
      />

      {/* Hot Discount Preview */}
      <TrendingSection 
        type="HOT_DISCOUNT" 
        title="🔥 Giảm giá sốc"
        previewSize={5}
        fullSize={15}
        extraFilters={{ minDiscountPercentage: 20 }}
      />

      {/* Flash Sale Preview */}
      <TrendingSection 
        type="HOT_DISCOUNT" 
        title="⚡ Flash Sale"
        previewSize={4}
        fullSize={12}
        extraFilters={{ flashSaleOnly: true }}
      />
    </div>
  );
}

    fetchBooks();
  }, [trendingType]);

  return (
    <div>
      <div className="trending-tabs">
        <button 
          className={trendingType === 'DAILY_TRENDING' ? 'active' : ''}
          onClick={() => setTrendingType('DAILY_TRENDING')}
        >
          📈 Xu hướng hôm nay
        </button>
        <button 
          className={trendingType === 'HOT_DISCOUNT' ? 'active' : ''}
          onClick={() => setTrendingType('HOT_DISCOUNT')}
        >
          🔥 Giảm sốc
        </button>
      </div>
      
      {loading ? (
        <div>Loading...</div>
      ) : (
        <div className="books-grid">
          {books.map(book => (
            <div key={book.id} className="book-card">
              <h3>{book.bookName}</h3>
              <p>Giá: {book.price.toLocaleString()} VND</p>
              <p>Trending Score: {book.trendingScore}</p>
              {book.isInFlashSale && <span className="flash-sale">⚡ FLASH SALE</span>}
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
```

## 🚀 **QUICK START CHO FRONTEND**

### **Copy-paste ready functions:**

```javascript
// 1. Universal trending function
async function getTrendingBooks(trendingRequest) {
  const response = await fetch('/api/books/trending', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(trendingRequest)
  });
  return await response.json();
}

// 2. Preview mode - Homepage (5 items)
async function getTrendingPreview(type = 'DAILY_TRENDING') {
  return await getTrendingBooks({
    type: type,
    page: 0,
    size: 5        // Chỉ 5 items cho preview
  });
}

// 3. Full list mode - After "Xem thêm" (20 items)
async function getTrendingFullList(type = 'DAILY_TRENDING', page = 0) {
  return await getTrendingBooks({
    type: type,
    page: page,
    size: 20       // Nhiều items hơn cho full list
  });
}

// 4. Load more / Pagination
async function loadMoreTrending(type, currentPage, itemsPerPage = 10) {
  return await getTrendingBooks({
    type: type,
    page: currentPage + 1,
    size: itemsPerPage
  });
}

// 5. Hot discount preview for homepage
async function getHotDiscountPreview() {
  return await getTrendingBooks({
    type: 'HOT_DISCOUNT',
    page: 0,
    size: 5,
    minDiscountPercentage: 20    // Chỉ lấy sách giảm ít nhất 20%
  });
}

// 6. Flash sale preview
async function getFlashSalePreview() {
  return await getTrendingBooks({
    type: 'HOT_DISCOUNT',
    flashSaleOnly: true,
    page: 0,
    size: 5
  });
}
```

### **Common UI Patterns:**

```javascript
// Pattern 1: Homepage với preview + "Xem thêm"
async function loadHomepageTrending() {
  // Load preview trên homepage
  const preview = await getTrendingPreview('DAILY_TRENDING');
  displayPreview(preview.data.content);
  
  // Show "Xem thêm" button if có nhiều data hơn
  if (preview.data.totalElements > 5) {
    showViewMoreButton();
  }
}

// Pattern 2: Khi user click "Xem thêm"
async function onViewMoreClick() {
  const fullList = await getTrendingFullList('DAILY_TRENDING');
  displayFullList(fullList.data.content);
  
  // Setup pagination nếu cần
  if (fullList.data.totalPages > 1) {
    setupPagination(fullList.data);
  }
}

// Pattern 3: Load more (infinite scroll)
let currentPage = 0;
async function onLoadMore() {
  const moreData = await loadMoreTrending('DAILY_TRENDING', currentPage);
  appendToList(moreData.data.content);
  currentPage++;
}
```

### **TypeScript Interface:**
```typescript
interface TrendingRequest {
  type: 'DAILY_TRENDING' | 'HOT_DISCOUNT';
  page?: number;
  size?: number;
  categoryId?: number;
  minPrice?: number;
  maxPrice?: number;
  minDiscountPercentage?: number;
  flashSaleOnly?: boolean;
}
```

---

## 🔍 **VALIDATION RULES**

### **Query Parameters:**
- `page`: >= 0 (default: 0)
- `size`: 1-50 (default: 10)  
- `minPrice`: >= 0 (optional)
- `maxPrice`: >= minPrice (optional)

### **Error Responses:**
```json
{
  "status": 400,
  "message": "Invalid page number",
  "data": null
}
```

## 🎯 **FALLBACK STRATEGY**

### **Khi không có đủ dữ liệu trending:**
1. **Fallback theo sách mới nhất** (createdAt DESC)
2. **Ưu tiên giá hợp lý** (50k-200k VND)
3. **Stock nhiều** (>= 50)
4. **Luôn đảm bảo có dữ liệu** từ database thực tế
5. **Empty result**: Trả về empty list khi không có sách nào

## 💡 **PERFORMANCE & CACHING**

### **Cache Strategy:**
- Cache time: 6 giờ
- Cache key: dựa trên parameters
- Auto invalidate khi có đơn hàng mới hoàn thành

### **Optimization:**
- Sử dụng pagination để tránh load quá nhiều data
- Index trên `createdAt`, `price`, `stockQuantity`
- Lazy loading cho authors và images

---

**✅ API này đảm bảo:**
- Dữ liệu trending chính xác từ database thực tế
- Thuật toán xếp hạng thông minh
- Fallback strategy khi thiếu dữ liệu
- Performance cao với caching
- Easy integration cho frontend

## 🎯 **API DESIGN RATIONALE**

### **Tại sao chỉ dùng POST cho trending API?**

#### **1. Consistency (Nhất quán)**
- Tất cả trending queries đều dùng POST
- Không cần phân biệt simple vs complex queries
- Dễ nhớ và sử dụng cho frontend developers

#### **2. Clean URLs**
```
✅ POST /api/books/trending
❌ GET /api/books/trending?type=HOT_DISCOUNT&categoryId=1&minPrice=50000&maxPrice=300000&minDiscountPercentage=30&flashSaleOnly=true&page=0&size=10
```

#### **3. Structured Data**
- Request body có structure rõ ràng
- Type safety tốt với TypeScript
- Easy validation với Bean Validation
- Dễ serialize/deserialize

#### **4. Flexibility**
- Dễ thêm/sửa parameters mà không ảnh hưởng URL
- Hỗ trợ nested objects trong tương lai
- Complex filtering logic

#### **5. Best Practices**
- Elasticsearch, GraphQL, Google APIs đều dùng POST cho complex queries
- Industry standard cho search APIs
- RESTful không nghiêm ngặt khi có lý do kỹ thuật

#### **6. Performance**
- Cache key dựa trên hash của request body
- Server logs clean hơn
- No URL length limits

## 💡 **PREVIEW vs FULL LIST - BEST PRACTICES**

### **Khi nào dùng Preview Mode?**
- ✅ **Homepage**: Hiển thị 3-5 sách trending để không làm quá tải trang chủ
- ✅ **Sidebar**: Recommend books trong các trang khác
- ✅ **Widget**: Trending books component nhỏ
- ✅ **Mobile**: Tiết kiệm space và data usage

### **Khi nào dùng Full List Mode?**
- ✅ **Trang riêng**: `/trending`, `/hot-deals` dedicated pages
- ✅ **Sau khi user click "Xem thêm"**: User đã thể hiện interest
- ✅ **Search/Filter**: User đang actively tìm kiếm
- ✅ **Desktop**: Có đủ space để hiển thị nhiều items

### **Recommended Sizes:**
```javascript
const TRENDING_SIZES = {
  PREVIEW: {
    homepage: 5,        // Homepage sections
    sidebar: 3,         // Sidebar widgets  
    mobile: 4,          // Mobile screens
    related: 6          // Related books section
  },
  FULL_LIST: {
    desktop: 20,        // Desktop full list
    mobile: 12,         // Mobile full list
    pagination: 10,     // Items per page
    loadMore: 8         // Load more increment
  }
};
```

### **UX Flow Example:**
```
1. Homepage loads → Show 5 trending books (PREVIEW)
2. User sees "Xem thêm 25 sách khác" button
3. User clicks → Navigate to /trending OR expand inline
4. Show 20 books (FULL_LIST) with pagination
5. User can "Thu gọn" back to preview OR load more
```

### **Performance Tips:**
- 🚀 **Cache preview data** aggressively (15 minutes)
- 🚀 **Full list cache** shorter (5 minutes) vì có nhiều variations
- 🚀 **Preload** full list data khi user hover "Xem thêm" button
- 🚀 **Lazy load images** trong full list để load nhanh hơn

---

## 🔄 **"XEM THÊM" LOGIC - CHI TIẾT CHO FRONTEND**

### **🎯 Phương pháp: Lần 1 lấy thông tin tổng, Lần 2 dùng thông tin đó**

#### **STEP 1: Lần đầu load (Preview) - Lấy thông tin tổng**
```javascript
// Frontend gọi API lần đầu với size nhỏ
const previewRequest = {
  type: 'DAILY_TRENDING',
  page: 0,
  size: 5    // Chỉ lấy 5 sách để preview
};

const previewResponse = await fetch('/api/books/trending', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify(previewRequest)
});

const previewData = await previewResponse.json();
```

#### **RESPONSE chứa thông tin tổng:**
```json
{
  "status": 200,
  "data": {
    "content": [/* 5 sách */],
    "totalElements": 127,     // ← KEY: Tổng có 127 sách trending
    "totalPages": 26,         // ← Nếu chia theo size=5
    "pageNumber": 0,
    "pageSize": 5,
    "hasNext": true
  }
}
```

#### **STEP 2: Frontend lưu thông tin và hiển thị button**
```javascript
// Lưu thông tin từ response
const totalBooks = previewData.data.totalElements;  // 127
const currentlyShowing = previewData.data.content.length;  // 5

// Hiển thị button "Xem thêm" với số lượng còn lại
const remaining = totalBooks - currentlyShowing;  // 127 - 5 = 122
document.getElementById('viewMoreBtn').textContent = 
  `Xem thêm (${remaining} sách khác)`;
```

#### **STEP 3: Khi user click "Xem thêm" - Dùng thông tin đã có**
```javascript
async function onViewMoreClick() {
  // KHÔNG cần gọi API thêm để biết tổng số!
  // Dùng totalBooks đã lưu từ lần gọi đầu tiên
  
  const sizeToFetch = Math.min(totalBooks, 50);  // Lấy tối đa 50 hoặc tất cả
  
  const fullListRequest = {
    type: 'DAILY_TRENDING',
    page: 0,
    size: sizeToFetch    // ← Dùng totalBooks từ lần 1
  };
  
  const fullResponse = await fetch('/api/books/trending', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(fullListRequest)
  });
  
  const fullData = await fullResponse.json();
  displayBooks(fullData.data.content);  // Hiển thị tất cả sách
}
```

### **💡 STRATEGIES CHO "XEM THÊM":**

#### **Strategy A: Lấy tất cả một lần (nếu <= 100 sách)**
```javascript
const onViewMore = async () => {
  if (totalBooks <= 100) {
    // Lấy hết tất cả
    const request = { type: 'DAILY_TRENDING', page: 0, size: totalBooks };
    const response = await getTrendingBooks(request);
    displayAllBooks(response.data.content);
  } else {
    // Nếu quá nhiều, lấy batch lớn hơn
    const request = { type: 'DAILY_TRENDING', page: 0, size: 50 };
    const response = await getTrendingBooks(request);
    displayBooks(response.data.content);
    showLoadMoreIfNeeded(response.data);
  }
};
```

#### **Strategy B: Lấy từng batch cố định**
```javascript
const onViewMore = async () => {
  // Luôn lấy 20 sách mỗi lần, bất kể tổng số
  const request = { type: 'DAILY_TRENDING', page: 0, size: 20 };
  const response = await getTrendingBooks(request);
  
  displayBooks(response.data.content);
  
  // Hiển thị "Load more" nếu còn data
  if (response.data.content.length < totalBooks) {
    showLoadMoreButton();
  }
};
```

### **🏗️ COMPLETE IMPLEMENTATION EXAMPLE:**
```javascript
class TrendingManager {
  constructor() {
    this.totalBooks = 0;
    this.currentBooks = [];
    this.isPreviewMode = true;
  }

  // STEP 1: Load preview và lưu thông tin tổng
  async loadPreview() {
    const request = { type: 'DAILY_TRENDING', page: 0, size: 5 };
    const response = await this.callAPI(request);
    
    // Lưu thông tin từ response đầu tiên
    this.totalBooks = response.data.totalElements;  // ← KEY INFO
    this.currentBooks = response.data.content;
    
    this.displayBooks(this.currentBooks);
    this.updateViewMoreButton();
  }

  // STEP 2: Update button với thông tin đã có
  updateViewMoreButton() {
    const remaining = this.totalBooks - this.currentBooks.length;
    const btn = document.getElementById('viewMoreBtn');
    
    if (remaining > 0) {
      btn.textContent = `Xem thêm (${remaining} sách khác)`;
      btn.style.display = 'block';
    } else {
      btn.style.display = 'none';
    }
  }

  // STEP 3: Xem thêm - dùng totalBooks đã lưu
  async onViewMoreClick() {
    this.isPreviewMode = false;
    
    // Quyết định size dựa trên totalBooks đã biết
    let sizeToFetch;
    if (this.totalBooks <= 50) {
      sizeToFetch = this.totalBooks;  // Lấy hết
    } else {
      sizeToFetch = 30;  // Lấy batch lớn hơn
    }
    
    const request = { type: 'DAILY_TRENDING', page: 0, size: sizeToFetch };
    const response = await this.callAPI(request);
    
    this.currentBooks = response.data.content;
    this.displayBooks(this.currentBooks);
    this.updateViewMoreButton();
  }

  async callAPI(request) {
    const response = await fetch('/api/books/trending', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(request)
    });
    return await response.json();
  }
}

// Sử dụng
const trendingManager = new TrendingManager();
trendingManager.loadPreview();  // Load 5 sách + lưu totalElements
```

### **⚡ TÓM TẮT FLOW:**
```
1. Load preview (size: 5) → Response có totalElements: 127
2. Frontend lưu: totalBooks = 127
3. Hiển thị: "Xem thêm (122 sách khác)"
4. User click → Gọi API với size dựa trên totalBooks đã lưu
5. Không cần gọi API thêm để biết tổng số!
```

---
## 🎛️ **FILTER APIs - CHO FRONTEND DROPDOWNS**

### **🔗 APIs liên quan để tạo filter UI:**

#### **1. Lấy danh sách danh mục cho dropdown**
```bash
GET /api/categories/dropdown
```

**Response:**
```json
{
  "status": 200,
  "message": "Lấy danh sách danh mục thành công",
  "data": [
    {
      "id": 1,
      "name": "Văn học"
    },
    {
      "id": 2,
      "name": "Kinh tế - Kinh doanh"
    },
    {
      "id": 3,
      "name": "Kỹ năng sống"
    },
    {
      "id": 4,
      "name": "Thiếu nhi"
    },
    {
      "id": 5,
      "name": "Học ngoại ngữ"
    }
  ]
}
```

#### **2. Frontend Implementation cho Filters**

##### **React Filter Component Example:**
```javascript
import { useState, useEffect } from 'react';

function TrendingBooksWithFilters() {
  const [categories, setCategories] = useState([]);
  const [filters, setFilters] = useState({
    type: 'DAILY_TRENDING',
    page: 0,
    size: 20,
    categoryId: null,
    minPrice: null,
    maxPrice: null,
    minDiscountPercentage: null,
    flashSaleOnly: false
  });
  const [books, setBooks] = useState([]);
  const [loading, setLoading] = useState(false);

  // Load categories for dropdown
  useEffect(() => {
    const fetchCategories = async () => {
      try {
        const response = await fetch('/api/categories/dropdown');
        const data = await response.json();
        setCategories(data.data);
      } catch (error) {
        console.error('Error fetching categories:', error);
      }
    };

    fetchCategories();
  }, []);

  // Fetch books when filters change
  useEffect(() => {
    const fetchBooks = async () => {
      setLoading(true);
      try {
        const response = await fetch('/api/books/trending', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(filters)
        });
        const data = await response.json();
        setBooks(data.data.content);
      } catch (error) {
        console.error('Error fetching books:', error);
      } finally {
        setLoading(false);
      }
    };

    fetchBooks();
  }, [filters]);

  // Handle filter changes
  const updateFilter = (key, value) => {
    setFilters(prev => ({
      ...prev,
      [key]: value,
      page: 0  // Reset to first page when filter changes
    }));
  };

  return (
    <div className="trending-with-filters">
      {/* Filter Panel */}
      <div className="filters">
        <h3>🎛️ Bộ lọc</h3>

        {/* Trending Type */}
        <div className="filter-group">
          <label>Loại trending:</label>
          <select 
            value={filters.type} 
            onChange={(e) => updateFilter('type', e.target.value)}
          >
            <option value="DAILY_TRENDING">Xu hướng hàng ngày</option>
            <option value="HOT_DISCOUNT">Giảm giá sốc</option>
          </select>
        </div>

        {/* Category Filter */}
        <div className="filter-group">
          <label>Danh mục:</label>
          <select 
            value={filters.categoryId || ''} 
            onChange={(e) => updateFilter('categoryId', e.target.value ? parseInt(e.target.value) : null)}
          >
            <option value="">Tất cả danh mục</option>
            {categories.map(category => (
              <option key={category.id} value={category.id}>
                {category.name}
              </option>
            ))}
          </select>
        </div>

        {/* Price Range */}
        <div className="filter-group">
          <label>Khoảng giá:</label>
          <input 
            type="number" 
            placeholder="Giá từ" 
            value={filters.minPrice || ''} 
            onChange={(e) => updateFilter('minPrice', e.target.value ? parseInt(e.target.value) : null)}
          />
          <input 
            type="number" 
            placeholder="Giá đến" 
            value={filters.maxPrice || ''} 
            onChange={(e) => updateFilter('maxPrice', e.target.value ? parseInt(e.target.value) : null)}
          />
        </div>

        {/* Hot Discount Specific Filters */}
        {filters.type === 'HOT_DISCOUNT' && (
          <>
            <div className="filter-group">
              <label>Giảm giá tối thiểu (%):</label>
              <input 
                type="number" 
                placeholder="VD: 20" 
                value={filters.minDiscountPercentage || ''} 
                onChange={(e) => updateFilter('minDiscountPercentage', e.target.value ? parseInt(e.target.value) : null)}
              />
            </div>

            <div className="filter-group">
              <label>
                <input 
                  type="checkbox" 
                  checked={filters.flashSaleOnly} 
                  onChange={(e) => updateFilter('flashSaleOnly', e.target.checked)}
                />
                Chỉ Flash Sale
              </label>
            </div>
          </>
        )}

        {/* Clear Filters */}
        <button 
          className="btn-clear"
          onClick={() => setFilters({
            type: 'DAILY_TRENDING',
            page: 0,
            size: 20,
            categoryId: null,
            minPrice: null,
            maxPrice: null,
            minDiscountPercentage: null,
            flashSaleOnly: false
          })}
        >
          🗑️ Xóa bộ lọc
        </button>
      </div>

      {/* Books Results */}
      <div className="books-results">
        {loading ? (
          <p>Đang tải...</p>
        ) : (
          <div className="books-grid">
            {books.map(book => (
              <div key={book.id} className="book-card">
                <img src={book.imageUrl} alt={book.bookName} />
                <h3>{book.bookName}</h3>
                <p className="price">{book.price.toLocaleString()}đ</p>
                <p className="category">{book.categoryName}</p>
                {book.discountPercentage > 0 && (
                  <span className="discount">-{book.discountPercentage}%</span>
                )}
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
```

##### **Vanilla JavaScript Filter Implementation:**
```javascript
// Utility functions for filters
class TrendingFilters {
  constructor() {
    this.categories = [];
    this.filters = {
      type: 'DAILY_TRENDING',
      page: 0,
      size: 20,
      categoryId: null,
      minPrice: null,
      maxPrice: null,
      minDiscountPercentage: null,
      flashSaleOnly: false
    };
  }

  // Load categories cho dropdown
  async loadCategories() {
    try {
      const response = await fetch('/api/categories/dropdown');
      const data = await response.json();
      this.categories = data.data;
      this.renderCategoryDropdown();
    } catch (error) {
      console.error('Error loading categories:', error);
    }
  }

  // Render category dropdown
  renderCategoryDropdown() {
    const select = document.getElementById('categoryFilter');
    select.innerHTML = '<option value="">Tất cả danh mục</option>';
    
    this.categories.forEach(category => {
      const option = document.createElement('option');
      option.value = category.id;
      option.textContent = category.name;
      select.appendChild(option);
    });
  }

  // Update filter và fetch books
  updateFilter(key, value) {
    this.filters[key] = value;
    this.filters.page = 0; // Reset page
    this.fetchBooks();
  }

  // Fetch books với filters hiện tại
  async fetchBooks() {
    try {
      const response = await fetch('/api/books/trending', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(this.filters)
      });
      const data = await response.json();
      this.renderBooks(data.data.content);
    } catch (error) {
      console.error('Error fetching books:', error);
    }
  }

  // Render books list
  renderBooks(books) {
    const container = document.getElementById('booksContainer');
    container.innerHTML = books.map(book => `
      <div class="book-card">
        <img src="${book.imageUrl}" alt="${book.bookName}" />
        <h3>${book.bookName}</h3>
        <p class="price">${book.price.toLocaleString()}đ</p>
        <p class="category">${book.categoryName}</p>
        ${book.discountPercentage > 0 ? 
          `<span class="discount">-${book.discountPercentage}%</span>` : ''}
      </div>
    `).join('');
  }

  // Initialize filters
  init() {
    this.loadCategories();
    this.fetchBooks();
    
    // Setup event listeners
    document.getElementById('typeFilter').addEventListener('change', (e) => {
      this.updateFilter('type', e.target.value);
    });
    
    document.getElementById('categoryFilter').addEventListener('change', (e) => {
      this.updateFilter('categoryId', e.target.value ? parseInt(e.target.value) : null);
    });
    
    document.getElementById('minPrice').addEventListener('input', (e) => {
      this.updateFilter('minPrice', e.target.value ? parseInt(e.target.value) : null);
    });
    
    document.getElementById('maxPrice').addEventListener('input', (e) => {
      this.updateFilter('maxPrice', e.target.value ? parseInt(e.target.value) : null);
    });
  }
}

// Usage
const filters = new TrendingFilters();
filters.init();
```

### **📝 Filter HTML Template:**
```html
<div class="trending-filters">
  <h3>🎛️ Bộ lọc sách trending</h3>
  
  <div class="filter-row">
    <label>Loại:</label>
    <select id="typeFilter">
      <option value="DAILY_TRENDING">Xu hướng hàng ngày</option>
      <option value="HOT_DISCOUNT">Giảm giá sốc</option>
    </select>
  </div>

  <div class="filter-row">
    <label>Danh mục:</label>
    <select id="categoryFilter">
      <option value="">Tất cả danh mục</option>
      <!-- Categories loaded dynamically -->
    </select>
  </div>

  <div class="filter-row">
    <label>Khoảng giá:</label>
    <input type="number" id="minPrice" placeholder="Từ" />
    <input type="number" id="maxPrice" placeholder="Đến" />
  </div>

  <div class="filter-row" id="discountFilters" style="display: none;">
    <label>Giảm giá tối thiểu (%):</label>
    <input type="number" id="minDiscount" placeholder="VD: 20" />
    
    <label>
      <input type="checkbox" id="flashSaleOnly" />
      Chỉ Flash Sale
    </label>
  </div>
  
  <button id="clearFilters">🗑️ Xóa bộ lọc</button>
</div>

<div id="booksContainer" class="books-grid">
  <!-- Books rendered here -->
</div>
```

---
