# 📸 REFUND EVIDENCE UPLOAD API DOCUMENTATION

## 🎯 **MỤC ĐÍCH**
API cho phép khách hàng upload ảnh/video làm bằng chứng khi yêu cầu hoàn hàng.

---

## 📋 **CÁC ENDPOINT AVAILABLE**

### 1. **Upload Ảnh (Images Only)**
```
POST /api/refund-evidence/images
Content-Type: multipart/form-data
```

**Request Body:**
```javascript
{
  "files": [File, File, ...] // Array of image files (max 5 files)
}
```

**Frontend Example (React/Vue):**
```javascript
const formData = new FormData();
formData.append('files', imageFile1);
formData.append('files', imageFile2);

const response = await fetch('/api/refund-evidence/images', {
  method: 'POST',
  body: formData
});
```

**Response:**
```json
{
  "status": 200,
  "message": "Upload thành công",
  "data": [
    "/uploads/refund-evidence/2025/07/image1751234567890_abc123.jpg",
    "/uploads/refund-evidence/2025/07/image1751234567891_def456.png"
  ]
}
```

### 2. **Upload Video (Videos Only)**
```
POST /api/refund-evidence/videos
Content-Type: multipart/form-data
```

**Request Body:**
```javascript
{
  "files": [File] // Array of video files (max 5 files)
}
```

**Response:**
```json
{
  "status": 200,
  "message": "Upload thành công",
  "data": [
    "/uploads/refund-evidence/2025/07/video1751234567892_ghi789.mp4"
  ]
}
```

### 3. **Upload Hỗn Hợp (Mixed Images & Videos)**
```
POST /api/refund-evidence/mixed
Content-Type: multipart/form-data
```

**Request Body:**
```javascript
{
  "images": [File, File],  // Array of image files (optional)
  "videos": [File]         // Array of video files (optional)
}
```

**Response:**
```json
{
  "status": 200,
  "message": "Upload thành công",
  "data": {
    "imageUrls": [
      "/uploads/refund-evidence/2025/07/image1751234567890_abc123.jpg",
      "/uploads/refund-evidence/2025/07/image1751234567891_def456.png"
    ],
    "videoUrls": [
      "/uploads/refund-evidence/2025/07/video1751234567892_ghi789.mp4"
    ]
  }
}
```

---

## 📝 **QUY TẮC VALIDATION**

### **Ảnh (Images):**
- **Formats:** JPG, JPEG, PNG, GIF, WEBP
- **Max Size:** 5MB per file
- **Min Dimensions:** 200x200 pixels
- **Max Files:** 5 files per request

### **Video (Videos):**
- **Formats:** MP4, AVI, MOV, WMV
- **Max Size:** 50MB per file
- **Max Files:** 5 files per request

### **Common Rules:**
- **Max Total Files:** 5 files per request
- **Allowed Module:** `refund-evidence` ✅

---

## 🚨 **ERROR RESPONSES**

```json
{
  "status": 400,
  "message": "Định dạng file không được hỗ trợ",
  "data": null
}
```

```json
{
  "status": 400,
  "message": "Kích thước file vượt quá giới hạn (5MB)",
  "data": null
}
```

```json
{
  "status": 400,
  "message": "Quá nhiều file. Tối đa 5 files",
  "data": null
}
```

```json
{
  "status": 500,
  "message": "Invalid module. Allowed modules: reviews, orders, categories, events, users, products, refund-evidence",
  "data": null
}
```

---

## 💡 **FRONTEND IMPLEMENTATION GUIDE**

### **React Example:**
```jsx
const uploadRefundEvidence = async (files) => {
  const formData = new FormData();
  
  files.forEach(file => {
    formData.append('files', file);
  });

  try {
    const response = await fetch('/api/refund-evidence/images', {
      method: 'POST',
      body: formData,
      headers: {
        // Don't set Content-Type manually, let browser set it
      }
    });
    
    const result = await response.json();
    
    if (result.status === 200) {
      console.log('Upload URLs:', result.data);
      return result.data;
    } else {
      throw new Error(result.message);
    }
  } catch (error) {
    console.error('Upload failed:', error.message);
    throw error;
  }
};
```

### **Vue Example:**
```javascript
async uploadEvidence(files) {
  const formData = new FormData();
  
  files.forEach(file => {
    formData.append('files', file);
  });

  try {
    const { data } = await this.$axios.post('/api/refund-evidence/images', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    });
    
    return data.data; // Array of URLs
  } catch (error) {
    console.error('Upload error:', error.response?.data?.message);
    throw error;
  }
}
```

---

## 🔧 **FILE STORAGE STRUCTURE**
```
uploads/
└── refund-evidence/
    └── 2025/
        └── 07/
            ├── image1751234567890_abc123.jpg
            ├── image1751234567891_def456.png
            └── video1751234567892_ghi789.mp4
```

---

## ✅ **CHECKLIST FOR FRONTEND**

- [ ] Sử dụng `FormData` để upload files
- [ ] Validate file types trước khi upload  
- [ ] Validate file size (5MB cho ảnh, 50MB cho video)
- [ ] Handle error responses correctly
- [ ] Display progress bar cho user experience
- [ ] Store returned URLs để submit cùng refund request

**🎯 Frontend chỉ cần gọi API với module `refund-evidence` là được!**
