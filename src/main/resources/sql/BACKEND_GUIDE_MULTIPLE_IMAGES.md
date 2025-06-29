# HƯỚNG DẪN CHO BACKEND DEVELOPER: XỬ LÝ NHIỀU ẢNH CHO EVENT (CÁCH ĐƠN GIẢN)

## 📋 Tóm tắt thay đổi

Frontend hiện tại hỗ trợ upload và quản lý **NHIỀU ẢNH** cho mỗi Event thay vì chỉ 1 ảnh như trước. Backend cần cập nhật để:

1. **Nhận mảng imageUrls** từ frontend
2. **Lưu trữ thành string** (cách nhau bằng dấu phẩy) trong database
3. **Trả về mảng imageUrls** trong response (split string thành array)

---

## 🔄 Thay đổi cần thiết

### 1. Database Schema (ĐƠN GIẢN)
```sql
-- Chỉ cần tăng độ dài cột image_url hiện tại:
ALTER TABLE events MODIFY COLUMN image_url VARCHAR(2000);

-- Hoặc nếu chưa có cột:
ALTER TABLE events ADD COLUMN image_url VARCHAR(2000);

-- Không cần tạo table mới hay JSON column!
```

### 2. Format lưu trữ trong database:
```
-- 1 ảnh (như cũ):
"https://yourdomain.com/uploads/events/2024/06/image1.jpg"

-- Nhiều ảnh (mới) - cách nhau bằng dấu phẩy:
"https://yourdomain.com/uploads/events/2024/06/image1.jpg,https://yourdomain.com/uploads/events/2024/06/image2.jpg,https://yourdomain.com/uploads/events/2024/06/image3.jpg"

-- Rỗng:
"" hoặc NULL
```

### 3. API Request/Response Format

#### Create/Update Event Request (Frontend gửi mảng):
```json
{
    "eventName": "Sự kiện mùa hè",
    "description": "Mô tả sự kiện...",
    "eventType": "SEASONAL",
    "eventCategoryId": 1,
    "status": "PUBLISHED",
    "startDate": 1698768000000,
    "endDate": 1698854400000,
    "maxParticipants": 100,
    "imageUrls": [
        "https://yourdomain.com/uploads/events/2024/06/image1.jpg",
        "https://yourdomain.com/uploads/events/2024/06/image2.jpg",
        "https://yourdomain.com/uploads/events/2024/06/image3.jpg"
    ],
    "location": "TP.HCM",
    "rules": "Quy định...",
    "isOnline": false
}
```

**Backend xử lý:**
```java
// Nhận mảng từ frontend
List<String> imageUrls = request.getImageUrls();

// Convert thành string để lưu database
String imageUrlsString = "";
if (imageUrls != null && !imageUrls.isEmpty()) {
    imageUrlsString = String.join(",", imageUrls);
}

// Lưu vào database
event.setImageUrl(imageUrlsString);
```

#### Get Events Response (Backend trả mảng):
```json
{
    "data": {
        "content": [
            {
                "id": 1,
                "name": "Sự kiện mùa hè",
                "description": "Mô tả...",
                "eventType": "SEASONAL",
                "eventTypeName": "Theo mùa",
                "categoryId": 1,
                "categoryName": "Giải trí",
                "startDate": 1698768000000,
                "endDate": 1698854400000,
                "maxParticipants": 100,
                "currentParticipants": 25,
                "imageUrl": "https://yourdomain.com/uploads/events/2024/06/image1.jpg",
                "imageUrls": [
                    "https://yourdomain.com/uploads/events/2024/06/image1.jpg",
                    "https://yourdomain.com/uploads/events/2024/06/image2.jpg",
                    "https://yourdomain.com/uploads/events/2024/06/image3.jpg"
                ],
                "status": 1,
                "location": "TP.HCM",
                "rules": "Quy định...",
                "isOnline": false
            }
        ],
        "totalPages": 10,
        "totalElements": 95,
        "pageNumber": 0,
        "pageSize": 10,
        "last": false
    }
}
```

**Backend xử lý:**
```java
// Đọc string từ database
String imageUrlsString = event.getImageUrl();

// Convert thành mảng để trả frontend
List<String> imageUrls = new ArrayList<>();
if (imageUrlsString != null && !imageUrlsString.isEmpty()) {
    imageUrls = Arrays.asList(imageUrlsString.split(","));
}

// Set response
response.setImageUrls(imageUrls);
response.setImageUrl(imageUrls.isEmpty() ? "" : imageUrls.get(0)); // Ảnh đầu tiên
```

---

## 📁 Upload API Endpoints

### 1. Upload Single Image
```
POST /api/upload/event-image
Content-Type: multipart/form-data

FormData:
- image: File
- folder: "events"

Response:
{
    "success": true,
    "url": "https://yourdomain.com/uploads/events/2024/06/image_timestamp.jpg",
    "message": "Upload successful"
}
```

### 2. Upload Multiple Images
```
POST /api/upload/event-images
Content-Type: multipart/form-data

FormData:
- images: File[] (multiple files)
- folder: "events"

Response:
{
    "success": true,
    "urls": [
        "https://yourdomain.com/uploads/events/2024/06/image1_timestamp.jpg",
        "https://yourdomain.com/uploads/events/2024/06/image2_timestamp.jpg",
        "https://yourdomain.com/uploads/events/2024/06/image3_timestamp.jpg"
    ],
    "message": "Upload successful"
}
```

### 3. Delete Image
```
DELETE /api/upload/event-image
Content-Type: application/json

{
    "imageUrl": "https://yourdomain.com/uploads/events/2024/06/image_timestamp.jpg"
}

Response:
{
    "success": true,
    "message": "Image deleted successfully"
}
```

---

## 🗂️ File Structure (Server)

```
server/
├── uploads/
│   ├── events/              # Ảnh events
│   │   ├── 2024/
│   │   │   ├── 06/
│   │   │   │   ├── event_1_1698768000000.jpg
│   │   │   │   ├── event_1_1698768000001.jpg
│   │   │   │   └── event_2_1698768000002.jpg
│   │   └── thumbnails/      # Thumbnails (optional)
│   ├── products/            # Ảnh products (tương lai)
│   └── users/               # Ảnh users (tương lai)
```

---

## 💡 Implementation Tips (ĐƠN GIẢN)

### 1. Backend Code Example (Java)
```java
// Request DTO
public class CreateEventRequest {
    private String eventName;
    private List<String> imageUrls; // Frontend gửi mảng
    // ... other fields
    
    // Getters and setters
}

// Response DTO  
public class EventResponse {
    private Long id;
    private String eventName;
    private List<String> imageUrls; // Trả về mảng cho frontend
    private String imageUrl; // Ảnh đầu tiên (backward compatible)
    // ... other fields
}

// Service layer
@Service
public class EventService {
    
    public Event createEvent(CreateEventRequest request) {
        Event event = new Event();
        
        // Convert mảng thành string để lưu database
        String imageUrlsString = "";
        if (request.getImageUrls() != null && !request.getImageUrls().isEmpty()) {
            imageUrlsString = String.join(",", request.getImageUrls());
        }
        
        event.setImageUrl(imageUrlsString); // Lưu string vào database
        // ... set other fields
        
        return eventRepository.save(event);
    }
    
    public List<EventResponse> getEvents() {
        List<Event> events = eventRepository.findAll();
        
        return events.stream().map(event -> {
            EventResponse response = new EventResponse();
            
            // Convert string thành mảng để trả frontend
            List<String> imageUrls = new ArrayList<>();
            if (event.getImageUrl() != null && !event.getImageUrl().isEmpty()) {
                imageUrls = Arrays.asList(event.getImageUrl().split(","));
            }
            
            response.setImageUrls(imageUrls);
            response.setImageUrl(imageUrls.isEmpty() ? "" : imageUrls.get(0)); // Ảnh đầu tiên
            // ... set other fields
            
            return response;
        }).collect(Collectors.toList());
    }
}
```

### 2. Validation đơn giản
```java
@Component
public class ImageUrlValidator {
    private static final int MAX_IMAGES = 5;
    private static final int MAX_URL_LENGTH = 500;
    private static final int MAX_TOTAL_LENGTH = 2000;
    
    public void validate(List<String> imageUrls) {
        if (imageUrls == null || imageUrls.isEmpty()) {
            return; // OK - no images
        }
        
        // Check số lượng ảnh
        if (imageUrls.size() > MAX_IMAGES) {
            throw new ValidationException("Maximum " + MAX_IMAGES + " images allowed");
        }
        
        // Check độ dài mỗi URL
        for (String url : imageUrls) {
            if (url.length() > MAX_URL_LENGTH) {
                throw new ValidationException("URL too long: " + url);
            }
        }
        
        // Check tổng độ dài khi join
        String combined = String.join(",", imageUrls);
        if (combined.length() > MAX_TOTAL_LENGTH) {
            throw new ValidationException("Total URLs length too long");
        }
    }
}
```

### 3. Entity class đơn giản
```java
@Entity
@Table(name = "events")
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "image_url", length = 2000)
    private String imageUrl; // Lưu string: "url1,url2,url3"
    
    // ... other fields
    
    // Getters and setters
    public String getImageUrl() {
        return imageUrl;
    }
    
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
```

---

## 🔒 Security & Best Practices

1. **File Validation**: Kiểm tra loại file, kích thước, tên file
2. **Unique Naming**: Tránh conflict tên file
3. **Path Security**: Prevent directory traversal
4. **Cleanup**: Xóa file cũ khi update/delete event
5. **Rate Limiting**: Giới hạn số request upload

---

## 🚀 Migration Script (ĐƠN GIẢN)

```sql
-- Tăng độ dài cột hiện tại để chứa nhiều URLs
ALTER TABLE events MODIFY COLUMN image_url VARCHAR(2000);

-- Data cũ không cần migration! Vì:
-- - "single_url" vẫn hợp lệ 
-- - split(",") với 1 URL sẽ trả về array có 1 element
-- - join(",") với 1 element sẽ trả về chính URL đó

-- Nếu muốn test với data hiện tại:
-- UPDATE events SET image_url = CONCAT(image_url, ',', image_url) 
-- WHERE image_url IS NOT NULL AND image_url != '';
-- (Tạo duplicate để test multiple images)
```

## ✅ Testing Checklist (ĐƠN GIẢN)

- [ ] Upload 1 ảnh → Lưu: "url1" ✓
- [ ] Upload nhiều ảnh → Lưu: "url1,url2,url3" ✓
- [ ] Get event với 1 ảnh → Trả: ["url1"] ✓  
- [ ] Get event với nhiều ảnh → Trả: ["url1","url2","url3"] ✓
- [ ] Backward compatibility → Data cũ vẫn hoạt động ✓
- [ ] Validation → Giới hạn 5 ảnh, 2000 chars ✓
- [ ] Empty case → "", null, [] đều OK ✓

## 🎯 Tại sao cách này tốt:

1. **Đơn giản nhất**: Chỉ cần split/join string
2. **Không phá vỡ**: Data cũ vẫn hoạt động 100%
3. **Ít code**: Không cần thay đổi entity, migration phức tạp
4. **Performance**: Không JOIN, không parse JSON
5. **Debug dễ**: Có thể đọc trực tiếp trong database
6. **Universal**: Hoạt động với mọi database (MySQL, PostgreSQL, SQLite...)

---

📞 **Contact**: Nếu có thắc mắc về implementation đơn giản này, liên hệ Frontend team.
