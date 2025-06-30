# 🚀 FRONTEND PARAMETER GUIDE - BACKEND UPLOAD INTEGRATION

## 📋 PARAMETER NAMES CHÍNH XÁC

### 1. SINGLE IMAGE UPLOAD
```
Endpoint: POST /api/upload/event-image
Parameter name: "image" (CHÍNH XÁC)
```

### 2. MULTIPLE IMAGES UPLOAD
```
Endpoint: POST /api/upload/event-images
Parameter name: "images" (CHÍNH XÁC)
```

### 3. DELETE IMAGE
```
Endpoint: DELETE /api/upload/event-image
Body: JSON với field "imageUrl"
```

## 🔧 FRONTEND CODE EXAMPLES

### JavaScript/Fetch API
```javascript
// SINGLE IMAGE UPLOAD
const uploadSingleImage = async (file) => {
    const formData = new FormData();
    formData.append('image', file);  // ← Parameter name "image"
    
    const response = await fetch('http://localhost:8080/api/upload/event-image', {
        method: 'POST',
        body: formData
    });
    
    return await response.json();
};

// MULTIPLE IMAGES UPLOAD
const uploadMultipleImages = async (files) => {
    const formData = new FormData();
    files.forEach(file => {
        formData.append('images', file);  // ← Parameter name "images"
    });
    
    const response = await fetch('http://localhost:8080/api/upload/event-images', {
        method: 'POST',
        body: formData
    });
    
    return await response.json();
};

// DELETE IMAGE
const deleteImage = async (imageUrl) => {
    const response = await fetch('http://localhost:8080/api/upload/event-image', {
        method: 'DELETE',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({ imageUrl })
    });
    
    return await response.json();
};
```

### React/Axios
```javascript
// SINGLE IMAGE UPLOAD
const uploadSingleImage = async (file) => {
    const formData = new FormData();
    formData.append('image', file);  // ← Parameter name "image"
    
    const response = await axios.post('http://localhost:8080/api/upload/event-image', formData, {
        headers: {
            'Content-Type': 'multipart/form-data'
        }
    });
    
    return response.data;
};

// MULTIPLE IMAGES UPLOAD
const uploadMultipleImages = async (files) => {
    const formData = new FormData();
    files.forEach(file => {
        formData.append('images', file);  // ← Parameter name "images"
    });
    
    const response = await axios.post('http://localhost:8080/api/upload/event-images', formData, {
        headers: {
            'Content-Type': 'multipart/form-data'
        }
    });
    
    return response.data;
};

// DELETE IMAGE
const deleteImage = async (imageUrl) => {
    const response = await axios.delete('http://localhost:8080/api/upload/event-image', {
        data: { imageUrl }
    });
    
    return response.data;
};
```

## 📨 RESPONSE FORMATS

### Success Response (Single Upload)
```json
{
    "success": true,
    "url": "http://localhost:8080/uploads/events/filename.jpg",
    "message": "Upload successful"
}
```

### Success Response (Multiple Upload)
```json
{
    "success": true,
    "urls": [
        "http://localhost:8080/uploads/events/file1.jpg",
        "http://localhost:8080/uploads/events/file2.jpg"
    ],
    "message": "Upload successful"
}
```

### Error Response
```json
{
    "error": "File type not supported",
    "code": "INVALID_FILE_TYPE"
}
```

## ⚠️ VALIDATION RULES

- **File types**: JPG, JPEG, PNG, GIF, WebP
- **Max file size**: 10MB per file
- **Max files**: 10 files per multiple upload
- **Image dimensions**: Max 4000x4000 pixels

## 🧪 TESTING COMMANDS

### Test với PowerShell
```powershell
# Test single upload
$file = "C:\path\to\image.jpg"
$form = @{image = Get-Item $file}
Invoke-RestMethod -Uri "http://localhost:8080/api/upload/event-image" -Method Post -Form $form

# Test multiple upload
$files = @(Get-Item "C:\path\to\image1.jpg", "C:\path\to\image2.jpg")
$form = @{}
foreach ($file in $files) {
    $form["images"] = $file
}
Invoke-RestMethod -Uri "http://localhost:8080/api/upload/event-images" -Method Post -Form $form
```

### Test với curl
```bash
# Single upload
curl -X POST http://localhost:8080/api/upload/event-image -F "image=@image.jpg"

# Multiple upload
curl -X POST http://localhost:8080/api/upload/event-images -F "images=@image1.jpg" -F "images=@image2.jpg"

# Delete
curl -X DELETE http://localhost:8080/api/upload/event-image -H "Content-Type: application/json" -d '{"imageUrl":"http://localhost:8080/uploads/events/filename.jpg"}'
```

## 🔍 TROUBLESHOOTING

### Common Issues:

1. **400 Bad Request - "Required request part 'image' is not present"**
   - ✅ Fix: Use parameter name "image" for single upload
   - ✅ Fix: Use parameter name "images" for multiple upload

2. **400 Bad Request - "File type not supported"**
   - ✅ Fix: Only use JPG, JPEG, PNG, GIF, WebP files

3. **404 Not Found**
   - ✅ Fix: Make sure backend is running on port 8080
   - ✅ Fix: Check endpoint URLs are correct

4. **CORS Error**
   - ✅ Backend already configured for http://localhost:5173
   - ✅ If using different port, update CORS in backend

## 🎯 QUICK START CHECKLIST

- [ ] Backend running on port 8080
- [ ] Use parameter name "image" for single upload
- [ ] Use parameter name "images" for multiple upload
- [ ] File types: JPG, JPEG, PNG, GIF, WebP only
- [ ] Max file size: 10MB
- [ ] Test with browser debug tool first
- [ ] Check response format matches expected structure

## 📞 SUPPORT

If you encounter issues:
1. Check backend logs for detailed error messages
2. Test with the provided browser debug tool
3. Verify parameter names match exactly
4. Ensure file types and sizes are within limits
