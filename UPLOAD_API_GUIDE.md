# Upload API Documentation

## Overview
The Upload API has been refactored to support multiple modules (events, users, products, categories, orders, reviews) instead of being limited to events only.

## Generic Endpoints (Recommended)

### Upload Multiple Images
```
POST /api/upload/images/{module}
```
- **Parameters:**
  - `module` (path): Module name (events, users, products, categories, orders, reviews)
  - `images` (form-data): Array of image files

**Example:**
```bash
curl -X POST "http://localhost:8080/api/upload/images/products" \
  -F "images=@product1.jpg" \
  -F "images=@product2.jpg"
```

### Upload Single Image
```
POST /api/upload/image/{module}
```
- **Parameters:**
  - `module` (path): Module name (events, users, products, categories, orders, reviews)
  - `image` (form-data): Single image file

**Example:**
```bash
curl -X POST "http://localhost:8080/api/upload/image/users" \
  -F "image=@avatar.jpg"
```

### Delete Image
```
DELETE /api/upload/image
```
- **Body:**
```json
{
  "imageUrl": "http://localhost:8080/uploads/products/2025/06/image1234567890_abc123.jpg"
}
```

## Directory Structure
Images are organized by module and date:
```
uploads/
├── events/
│   └── 2025/
│       └── 06/
│           └── image1234567890_abc123.jpg
├── products/
│   └── 2025/
│       └── 06/
│           └── image1234567890_def456.jpg
├── users/
│   └── 2025/
│       └── 06/
│           └── image1234567890_ghi789.jpg
└── ...
```

## Response Format

### Success Response (Multiple Upload)
```json
{
  "success": true,
  "urls": [
    "http://localhost:8080/uploads/products/2025/06/image1234567890_abc123.jpg",
    "http://localhost:8080/uploads/products/2025/06/image1234567890_def456.jpg"
  ],
  "message": "Upload successful"
}
```

### Success Response (Single Upload)
```json
{
  "success": true,
  "url": "http://localhost:8080/uploads/users/2025/06/image1234567890_abc123.jpg",
  "message": "Upload successful"
}
```

### Error Response
```json
{
  "error": "Invalid module. Allowed modules: events, users, products, categories, orders, reviews",
  "code": "INVALID_MODULE"
}
```

## Validation Rules
- **File Types:** JPG, JPEG, PNG, GIF, WebP
- **File Size:** Maximum 5MB per file
- **Image Dimensions:** Minimum 200x200 pixels
- **Max Files:** Maximum 5 files per request
- **Allowed Modules:** events, users, products, categories, orders, reviews

## Backward Compatibility
Old endpoints are still available but marked as deprecated:
- `POST /api/upload/event-images` → Use `POST /api/upload/images/events`
- `POST /api/upload/event-image` → Use `POST /api/upload/image/events`
- `DELETE /api/upload/event-image` → Use `DELETE /api/upload/image`

## Frontend Usage Examples

### JavaScript/Axios
```javascript
// Upload product images
const formData = new FormData();
formData.append('images', file1);
formData.append('images', file2);

const response = await axios.post('/api/upload/images/products', formData, {
  headers: {
    'Content-Type': 'multipart/form-data'
  }
});

// Upload user avatar
const avatarData = new FormData();
avatarData.append('image', avatarFile);

const avatarResponse = await axios.post('/api/upload/image/users', avatarData, {
  headers: {
    'Content-Type': 'multipart/form-data'
  }
});
```

### React
```jsx
const handleUpload = async (files, module) => {
  const formData = new FormData();
  files.forEach(file => {
    formData.append('images', file);
  });

  try {
    const response = await fetch(`/api/upload/images/${module}`, {
      method: 'POST',
      body: formData
    });
    
    const result = await response.json();
    if (result.success) {
      console.log('Uploaded URLs:', result.urls);
    }
  } catch (error) {
    console.error('Upload failed:', error);
  }
};
```
