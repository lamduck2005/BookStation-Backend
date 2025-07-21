# 📦 LUỒNG HOÀN HÀNG COMPLETE GUIDE

## 🎯 **OVERVIEW**

Hệ thống BookStation hỗ trợ **2 luồng hoàn hàng chính:**

1. **🔄 Luồng Khách Hàng Yêu Cầu** (Customer Request Flow)
2. **⚡ Luồng Admin Trực Tiếp** (Direct Admin Refund)

---

## 📋 **LUỒNG 1: KHÁCH HÀNG YÊU CẦU HOÀN HÀNG**

### **Step 1: Upload Bằng Chứng (Optional)**

**Upload ảnh minh chứng:**
```
POST /api/refund-evidence/images
Content-Type: multipart/form-data
```

**Request Body:**
```javascript
const formData = new FormData();
formData.append('files', imageFile1);
formData.append('files', imageFile2);
// Maximum 5 files, 5MB each
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

**Upload video minh chứng:**
```
POST /api/refund-evidence/videos
Content-Type: multipart/form-data
```

**Request Body:**
```javascript
const formData = new FormData();
formData.append('files', videoFile1);
// Maximum 5 files, 50MB each
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

---

### **Step 2: Gửi Yêu Cầu Hoàn Hàng**

```
POST /api/orders/{orderId}/request-refund
Content-Type: application/json
```

**Request Body:**
```json
{
  "userId": 123,
  "reason": "Sản phẩm bị lỗi, không đúng mô tả",
  "additionalNotes": "Sách bị rách trang, in mờ chữ",
  "refundDetails": [
    {
      "bookId": 45,
      "refundQuantity": 1,
      "reason": "Sách bị rách trang 15-20",
      "evidenceImages": [
        "/uploads/refund-evidence/2025/07/image1751234567890_abc123.jpg",
        "/uploads/refund-evidence/2025/07/image1751234567891_def456.png"
      ],
      "evidenceVideos": [
        "/uploads/refund-evidence/2025/07/video1751234567892_ghi789.mp4"
      ],
      "additionalNotes": "Đã bảo quản cẩn thận, lỗi từ nhà cung cấp"
    },
    {
      "bookId": 67,
      "refundQuantity": 2,
      "reason": "Sai màu bìa, không đúng như hình",
      "evidenceImages": [
        "/uploads/refund-evidence/2025/07/image1751234567893_xyz789.jpg"
      ],
      "evidenceVideos": [],
      "additionalNotes": "Đặt bìa xanh nhưng nhận được bìa đỏ"
    }
  ],
  "evidenceImages": [
    "/uploads/refund-evidence/2025/07/image1751234567894_general.jpg"
  ],
  "evidenceVideos": []
}
```

**Response:**
```json
{
  "status": 200,
  "message": "Yêu cầu hoàn trả đã được gửi thành công. Admin sẽ xem xét và phản hồi sớm nhất.",
  "data": {
    "id": 1001,
    "code": "ORD-20250721-001",
    "orderStatus": "REFUND_REQUESTED",
    "userId": 123,
    "totalAmount": 299000,
    "refundAmount": 150000,
    // ... other order details
  }
}
```

---

### **Step 3A: Admin Chấp Nhận Yêu Cầu**

```
POST /api/orders/admin/approve-refund
Content-Type: application/json
```

**Request Body:**
```json
{
  "orderId": 1001,
  "adminId": 5,
  "approved": true,
  "adminNotes": "Đã xem xét bằng chứng, chấp nhận hoàn trả. Sản phẩm thực sự bị lỗi từ nhà cung cấp."
}
```

**Response:**
```json
{
  "status": 200,
  "message": "Đã chấp nhận yêu cầu hoàn trả",
  "data": {
    "id": 1001,
    "code": "ORD-20250721-001", 
    "orderStatus": "REFUNDING",
    "refundAmount": 150000,
    // ... other order details
  }
}
```

---

### **Step 3B: Admin Từ Chối Yêu Cầu**

```
POST /api/orders/admin/reject-refund  
Content-Type: application/json
```

**Request Body:**
```json
{
  "orderId": 1001,
  "adminId": 5,
  "approved": false,
  "adminNotes": "Sản phẩm không có dấu hiệu lỗi sản xuất. Có thể do sử dụng không đúng cách. Từ chối hoàn trả."
}
```

**Response:**
```json
{
  "status": 200,
  "message": "Đã từ chối yêu cầu hoàn trả",
  "data": {
    "id": 1001,
    "code": "ORD-20250721-001",
    "orderStatus": "DELIVERED", // Trở về trạng thái cũ
    // ... other order details
  }
}
```

---

## ⚡ **LUỒNG 2: ADMIN HOÀN HÀNG TRỰC TIẾP**

### **Option A: Hoàn Hàng Một Phần**

```
POST /api/orders/{orderId}/partial-refund?userId={userId}&reason={reason}
Content-Type: application/json
```

**URL Example:**
```
POST /api/orders/1001/partial-refund?userId=123&reason=San pham bi loi
```

**Request Body:**
```json
[
  {
    "bookId": 45,
    "refundQuantity": 1,
    "reason": "Sách bị rách không thể sử dụng",
    "evidenceImages": [
      "/uploads/refund-evidence/2025/07/image1751234567890_abc123.jpg"
    ],
    "evidenceVideos": [],
    "additionalNotes": "Admin đã kiểm tra trực tiếp"
  }
]
```

**Response:**
```json
{
  "status": 200,
  "message": "Hoàn trả một phần thành công",
  "data": {
    "id": 1001,
    "code": "ORD-20250721-001",
    "orderStatus": "PARTIALLY_REFUNDED",
    "totalAmount": 299000,
    "refundAmount": 89000,
    // ... other order details
  }
}
```

---

### **Option B: Hoàn Hàng Toàn Bộ**

```
POST /api/orders/{orderId}/full-refund?userId={userId}&reason={reason}
Content-Type: application/json
```

**URL Example:**
```
POST /api/orders/1001/full-refund?userId=123&reason=Khach hang khong hai long
```

**No Request Body Required**

**Response:**
```json
{
  "status": 200,
  "message": "Hoàn trả toàn bộ thành công",
  "data": {
    "id": 1001,
    "code": "ORD-20250721-001",
    "orderStatus": "REFUNDED",
    "totalAmount": 299000,
    "refundAmount": 299000,
    // ... other order details
  }
}
```

---

## 🔄 **TRẠNG THÁI ĐỔI LUỒNG**

### **Luồng Khách Hàng Yêu Cầu:**
```
DELIVERED → REFUND_REQUESTED → REFUNDING → REFUNDED
                    ↓
                DELIVERED (nếu từ chối)
```

### **Luồng Admin Trực Tiếp:**
```
DELIVERED → PARTIALLY_REFUNDED (partial)
         → REFUNDED (full)
```

---

## 🚨 **ERROR RESPONSES**

### **Upload Evidence Errors:**
```json
{
  "status": 400,
  "message": "Quá nhiều file. Tối đa 5 files",
  "data": null
}
```

```json
{
  "status": 400,
  "message": "Kích thước file vượt quá giới hạn (5MB cho ảnh, 50MB cho video)",
  "data": null
}
```

### **Request Refund Errors:**
```json
{
  "status": 400,
  "message": "Chỉ có thể hoàn trả đơn hàng đã giao",
  "data": null
}
```

```json
{
  "status": 403,
  "message": "Bạn không có quyền hoàn trả đơn hàng này",
  "data": null
}
```

### **Admin Decision Errors:**
```json
{
  "status": 400,
  "message": "Đơn hàng không ở trạng thái REFUND_REQUESTED",
  "data": null
}
```

---

## 💡 **BUSINESS LOGIC**

### **Khi Hoàn Hàng Xảy Ra:**
- ✅ **Stock:** Khôi phục số lượng kho
- ✅ **Sold Count:** Trừ sold count (chỉ nếu đã delivered trước đó)  
- ✅ **Points:** Trừ điểm đã tích (nếu có)
- ✅ **Voucher:** Hoàn lại voucher đã sử dụng
- ✅ **User Rank:** Cập nhật lại rank sau khi trừ tổng chi tiêu

### **Validation Rules:**
- Chỉ được hoàn hàng đơn đã **DELIVERED**
- User chỉ có thể hoàn hàng của chính mình
- Admin có thể hoàn hàng bất kỳ đơn nào
- Số lượng hoàn trả không được vượt quá số lượng đã mua

---

## 🎯 **FRONTEND IMPLEMENTATION**

### **1. Upload Evidence First:**
```javascript
const uploadEvidence = async (files) => {
  const formData = new FormData();
  files.forEach(file => formData.append('files', file));
  
  const response = await fetch('/api/refund-evidence/images', {
    method: 'POST',
    body: formData
  });
  
  const result = await response.json();
  return result.data; // Array of URLs
};
```

### **2. Submit Refund Request:**
```javascript
const requestRefund = async (orderId, refundData) => {
  const response = await fetch(`/api/orders/${orderId}/request-refund`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(refundData)
  });
  
  return await response.json();
};
```

### **3. Admin Approve/Reject:**
```javascript
const adminDecision = async (decision) => {
  const endpoint = decision.approved ? 'approve-refund' : 'reject-refund';
  
  const response = await fetch(`/api/orders/admin/${endpoint}`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(decision)
  });
  
  return await response.json();
};
```

---

## ✅ **TESTING CHECKLIST**

### **Customer Flow:**
- [ ] Upload evidence images → get URLs ✅
- [ ] Upload evidence videos → get URLs ✅
- [ ] Submit refund request → status REFUND_REQUESTED ✅
- [ ] Admin approve → status REFUNDING ✅
- [ ] Admin reject → status back to DELIVERED ✅

### **Admin Direct Flow:**
- [ ] Partial refund → status PARTIALLY_REFUNDED ✅
- [ ] Full refund → status REFUNDED ✅
- [ ] Stock restored ✅
- [ ] Points deducted ✅
- [ ] Vouchers refunded ✅

**🎯 Hệ thống hoàn hàng complete và ready for production!**
