# Hướng dẫn tích hợp Upload ảnh vào Spring Boot Backend

## Tổng quan
Frontend cần upload ảnh cho Event và các module khác. Hiện tại đang dùng Upload Server riêng (port 3001), nhưng muốn **tích hợp trực tiếp vào Backend API (port 8080)** để giảm complexity.

## Yêu cầu từ Frontend

### 1. Endpoints cần thiết

#### 1.1. Upload multiple images cho Event
```
POST /api/upload/event-images
Content-Type: multipart/form-data

Body:
- images: File[] (tối đa 5 ảnh)

Response:
{
  "success": true,
  "urls": [
    "http://localhost:8080/uploads/events/2025/06/image1751234567890_abc123.jpg",
    "http://localhost:8080/uploads/events/2025/06/image1751234567891_def456.png"
  ],
  "message": "Upload successful"
}
```

#### 1.2. Upload single image cho Event
```
POST /api/upload/event-image
Content-Type: multipart/form-data

Body:
- image: File

Response:
{
  "success": true,
  "url": "http://localhost:8080/uploads/events/2025/06/image1751234567890_abc123.jpg",
  "message": "Upload successful"
}
```

#### 1.3. Delete image
```
DELETE /api/upload/event-image
Content-Type: application/json

Body:
{
  "imageUrl": "http://localhost:8080/uploads/events/2025/06/image1751234567890_abc123.jpg"
}

Response:
{
  "success": true,
  "message": "Image deleted successfully"
}
```

### 2. Cấu trúc thư mục lưu file

```
uploads/
├── events/
│   ├── 2025/
│   │   ├── 01/
│   │   │   ├── image1735234567890_abc123.jpg
│   │   │   └── image1735234567891_def456.png
│   │   ├── 02/
│   │   └── 06/
│   └── 2024/
├── users/           (future: avatar upload)
├── products/        (future: product images)
└── categories/      (future: category images)
```

### 3. Quy tắc đặt tên file

```
Format: image{timestamp}_{randomId}.{extension}
Example: image1751234567890_abc123def.jpg

- timestamp: milliseconds since epoch
- randomId: random string 10-15 chars
- extension: jpg, png, gif, webp
```

### 4. Validation rules

#### 4.1. File types
- Chỉ chấp nhận: `image/jpeg`, `image/jpg`, `image/png`, `image/gif`, `image/webp`

#### 4.2. File size
- Tối đa: **5MB** per file

#### 4.3. Image dimensions
- Tối thiểu: **200x200px**
- Không giới hạn tối đa (frontend sẽ tự compress)

#### 4.4. Số lượng
- Multiple upload: tối đa **5 ảnh** một lần
- Single upload: **1 ảnh**

### 5. Error handling

#### 5.1. Validation errors
```json
{
  "error": "File type not allowed. Please upload JPG, PNG, GIF, or WebP images.",
  "code": "INVALID_FILE_TYPE"
}
```

#### 5.2. File size errors
```json
{
  "error": "File size too large. Maximum size is 5MB.",
  "code": "FILE_TOO_LARGE"
}
```

#### 5.3. Dimension errors
```json
{
  "error": "Image dimensions too small. Minimum size is 200x200px.",
  "code": "INVALID_DIMENSIONS"
}
```

## Implementation Guide

### 1. Dependencies cần thêm (pom.xml)

```xml
<!-- File upload -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>

<!-- Image processing (optional) -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>
```

### 2. Configuration (application.yml)

```yaml
spring:
  servlet:
    multipart:
      max-file-size: 5MB
      max-request-size: 25MB  # 5 files * 5MB
      
upload:
  path: uploads/
  base-url: http://localhost:8080/uploads/
  
server:
  port: 8080
```

### 3. Controller structure

```java
@RestController
@RequestMapping("/api/upload")
@CrossOrigin(origins = "http://localhost:5173")  // Frontend URL
public class UploadController {

    @PostMapping("/event-images")
    public ResponseEntity<?> uploadEventImages(@RequestParam("images") MultipartFile[] files) {
        // Implementation
    }

    @PostMapping("/event-image") 
    public ResponseEntity<?> uploadEventImage(@RequestParam("image") MultipartFile file) {
        // Implementation
    }

    @DeleteMapping("/event-image")
    public ResponseEntity<?> deleteEventImage(@RequestBody DeleteImageRequest request) {
        // Implementation
    }
}
```

### 4. Service structure

```java
@Service
public class FileUploadService {
    
    public List<String> saveEventImages(MultipartFile[] files) {
        // Validate files
        // Save to filesystem
        // Return URLs
    }
    
    public String saveEventImage(MultipartFile file) {
        // Validate file
        // Save to filesystem  
        // Return URL
    }
    
    public boolean deleteImage(String imageUrl) {
        // Extract filename from URL
        // Delete from filesystem
        // Return success status
    }
    
    private void validateImage(MultipartFile file) {
        // Check file type
        // Check file size
        // Check image dimensions
    }
}
```

### 5. Static file serving

```java
@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/");
    }
}
```

## Tích hợp với Event Entity

### 1. Event entity cần field:

```java
@Entity
public class Event {
    // ... existing fields
    
    @ElementCollection
    @CollectionTable(name = "event_images")
    private List<String> imageUrls = new ArrayList<>();
    
    // getters, setters
}
```

### 2. EventDto cần field:

```java
public class EventCreateRequest {
    // ... existing fields
    
    private List<String> imageUrls;  // URLs from upload
    
    // getters, setters
}
```

## Testing

### 1. Test endpoints với Postman:

```
POST http://localhost:8080/api/upload/event-images
- Form-data: images = [file1.jpg, file2.png]

POST http://localhost:8080/api/upload/event-image  
- Form-data: image = file1.jpg

DELETE http://localhost:8080/api/upload/event-image
- JSON: {"imageUrl": "http://localhost:8080/uploads/events/2025/06/image123.jpg"}
```

### 2. Frontend sẽ test:

```javascript
// Upload và nhận URLs
const response = await uploadMultipleEventImages(files);
// response.urls = ["http://localhost:8080/uploads/events/2025/06/image123.jpg"]

// Gửi Event với imageUrls
const eventData = {
  eventName: "Test Event",
  // ... other fields
  imageUrls: response.urls  // URLs từ upload
};
await createEvent(eventData);
```

## Lưu ý quan trọng

### 1. CORS Configuration
- Frontend chạy port **5173**
- Backend cần allow CORS cho domain này

### 2. File paths
- Development: lưu trong thư mục `uploads/` trong project
- Production: cân nhắc lưu trên cloud storage

### 3. Database
- **KHÔNG** lưu file binary vào DB
- **CHỈ** lưu URLs/paths vào DB

### 4. Security
- Validate file types nghiêm ngặt
- Scan virus nếu cần thiết
- Rate limiting cho upload endpoints

### 5. Performance
- Compress images nếu quá lớn
- Tạo thumbnails nếu cần
- Cleanup orphaned files

## Expected Timeline

1. **Phase 1**: Basic upload endpoints (1-2 days)
2. **Phase 2**: File validation & error handling (1 day)  
3. **Phase 3**: Integration với Event APIs (1 day)
4. **Phase 4**: Testing & optimization (1 day)

## Frontend changes needed

Frontend sẽ thay đổi từ:
```javascript
// Cũ: gọi Upload Server
const response = await axios.post('http://localhost:3001/api/upload/event-images', formData);

// Mới: gọi Backend API  
const response = await axios.post('http://localhost:8080/api/upload/event-images', formData);
```

---

**Contact**: Nếu có thắc mắc về requirements, please ping Frontend team.
