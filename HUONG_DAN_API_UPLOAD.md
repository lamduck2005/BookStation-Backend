# Hướng Dẫn API Upload Ảnh

## Tóm tắt nhanh
API này dùng để upload và xóa ảnh cho các module: events, users, products, categories, orders, reviews

## 2 API chính cần biết

### 1. Upload nhiều ảnh (CHÍNH)
```
POST /api/upload/images/{module}
```

**Ví dụ thực tế:**
```bash
# Upload ảnh sản phẩm
curl -X POST "http://localhost:8080/api/upload/images/products" \
  -F "images=@anh1.jpg" \
  -F "images=@anh2.jpg"
```

**Kết quả trả về:**
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

### 2. Xóa ảnh
```
DELETE /api/upload/image
```

**Ví dụ thực tế:**
```bash
curl -X DELETE "http://localhost:8080/api/upload/image" \
  -H "Content-Type: application/json" \
  -d '{
    "imageUrl": "http://localhost:8080/uploads/products/2025/06/image1234567890_abc123.jpg"
  }'
```

## Frontend JavaScript đơn giản

### Upload nhiều ảnh
```javascript
const uploadImages = async (files, module = 'products') => {
  const formData = new FormData();
  
  // Thêm từng file vào formData
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
      console.log('Upload thành công!');
      console.log('Các URL ảnh:', result.urls);
      return result.urls; // Trả về danh sách URL
    }
  } catch (error) {
    console.error('Upload thất bại:', error);
  }
};

// Cách sử dụng
const fileInput = document.querySelector('input[type="file"]');
fileInput.addEventListener('change', (e) => {
  const files = Array.from(e.target.files);
  uploadImages(files, 'products'); // Thay 'products' bằng module cần thiết
});
```

### Xóa ảnh
```javascript
const deleteImage = async (imageUrl) => {
  try {
    const response = await fetch('/api/upload/image', {
      method: 'DELETE',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({ imageUrl })
    });
    
    if (response.ok) {
      console.log('Xóa ảnh thành công!');
    }
  } catch (error) {
    console.error('Xóa ảnh thất bại:', error);
  }
};

// Cách sử dụng
deleteImage('http://localhost:8080/uploads/products/2025/06/image1234567890_abc123.jpg');
```

## Các module có thể dùng
- `events` - Sự kiện
- `users` - Người dùng
- `products` - Sản phẩm
- `categories` - Danh mục
- `orders` - Đơn hàng
- `reviews` - Đánh giá

## Giới hạn
- Chỉ chấp nhận: JPG, JPEG, PNG, GIF, WebP
- Tối đa 5MB/ảnh
- Tối đa 5 ảnh/lần upload
- Kích thước tối thiểu: 200x200 pixel

## Lưu ý quan trọng
- Ảnh sẽ được lưu vào thư mục `uploads/{module}/năm/tháng/`
- URL trả về có thể dùng trực tiếp trong frontend
- Nhớ thay `{module}` bằng tên module thực tế (products, users, events...)
