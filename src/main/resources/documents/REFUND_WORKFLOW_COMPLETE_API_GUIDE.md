# 📋 HƯỚNG DẪN API HOÀN HÀNG TOÀN DIỆN - FRONTEND INTEGRATION

## 🎯 **LUỒNG HOÀN HÀNG COMPLETE**

```mermaid
graph TD
    A[User tạo yêu cầu hoàn hàng] --> B[POST /api/orders/{id}/request-refund]
    B --> C[RefundRequest.status = PENDING<br/>Order.orderStatus = REFUND_REQUESTED]
    C --> D[Admin xem danh sách: GET /api/refunds/pending]
    D --> E{Admin quyết định}
    E -->|Chấp nhận| F[POST /api/refunds/{id}/approve]
    E -->|Từ chối| G[POST /api/refunds/{id}/reject]
    F --> H[RefundRequest.status = APPROVED<br/>Order.orderStatus = REFUNDING]
    G --> I[RefundRequest.status = REJECTED<br/>Order.orderStatus = DELIVERED]
    H --> J[POST /api/refunds/{id}/process]
    J --> K[RefundRequest.status = COMPLETED<br/>Order.orderStatus = REFUNDED/PARTIALLY_REFUNDED<br/>✅ Cộng stock, trừ sold count, trừ điểm]
```

⚠️ **IMPORTANT FIX:** 
- **Frontend gọi API cũ:** `POST /api/orders/{orderId}/request-refund` (not /api/refunds)
- **Request Body:** `orderId` có trong URL path → KHÔNG cần trong request body
- **Validation fixed:** Process refund now accepts Order.status = REFUNDING (after approve)

🔧 **API REQUEST BODY ĐÃ SỬA:**
- ✅ ĐÚNG: `RefundRequestDto` với `userId`, `reason`, `refundDetails`
- ❌ SAI: Bỏ `orderId` khỏi request body (đã có trong URL path)

---

## 📝 **1. USER TẠO YÊU CẦU HOÀN HÀNG**

### **🔹 Step 1: Upload minh chứng (Optional)**

```http
POST /api/refund-evidence/mixed
Content-Type: multipart/form-data
Authorization: Bearer {user_token}

Form Data:
- images: File[] (tối đa 10 ảnh, .jpg/.jpeg/.png/.webp, max 5MB/file)
- videos: File[] (tối đa 3 video, .mp4/.avi/.mov, max 50MB/file)
```

**Response Success:**
```json
{
  "status": 200,
  "message": "Upload minh chứng hoàn hàng thành công",
  "data": {
    "imagePaths": [
      "/uploads/refund-evidence/2025/07/image1751234567890_abc123.jpg",
      "/uploads/refund-evidence/2025/07/image1751234567891_def456.jpg"
    ],
    "videoPaths": [
      "/uploads/refund-evidence/2025/07/video1751234567892_ghi789.mp4"
    ]
  }
}
```

**Response Error:**
```json
{
  "status": 400,
  "message": "File quá lớn. Kích thước tối đa cho ảnh là 5MB",
  "data": null
}
```

### **🔹 Step 2: Kiểm tra điều kiện hoàn hàng**

```http
GET /api/refunds/validate/{orderId}/{userId}
Authorization: Bearer {user_token}
```

**Response - Có thể hoàn:**
```json
{
  "status": 200,
  "message": "Đơn hàng có thể được hoàn trả",
  "data": {
    "canRefund": true,
    "orderStatus": "DELIVERED",
    "orderCode": "ORD-2025072201",
    "deliveredDate": 1751234567890,
    "refundDeadline": 1751234567890,
    "remainingDays": 7
  }
}
```

**Response - Không thể hoàn:**
```json
{
  "status": 400,
  "message": "Chỉ có thể hoàn trả đơn hàng đã giao thành công",
  "data": {
    "canRefund": false,
    "orderStatus": "SHIPPED",
    "reason": "Đơn hàng chưa được giao"
  }
}
```

### **🔹 Step 3: Tạo yêu cầu hoàn hàng**

⚠️ **UPDATED API ENDPOINT (THỰC TẾ FRONTEND ĐANG DÙNG):**

```http
POST /api/orders/{orderId}/request-refund
Content-Type: application/json
Authorization: Bearer {user_token}
```

**Request Body - Hoàn trả một phần:**
```json
{
  "userId": 123,
  "reason": "Sản phẩm bị lỗi và không đúng mô tả",
  "additionalNotes": "Sách bị rách ở bìa và có vết nước. Tôi đã chụp ảnh minh chứng.",
  "refundDetails": [
    {
      "bookId": 45,
      "refundQuantity": 1,
      "reason": "Sách bị rách không thể sử dụng",
      "evidenceImages": [
        "/uploads/refund-evidence/2025/07/image1751234567890_abc123.jpg"
      ],
      "evidenceVideos": [],
      "additionalNotes": "Rách ở góc trang 15-20"
    },
    {
      "bookId": 46,
      "refundQuantity": 2,
      "reason": "Không đúng phiên bản như mô tả",
      "evidenceImages": [
        "/uploads/refund-evidence/2025/07/image1751234567891_def456.jpg"
      ],
      "evidenceVideos": [],
      "additionalNotes": "Phiên bản cũ thay vì mới như quảng cáo"
    }
  ],
  "evidenceImages": [
    "/uploads/refund-evidence/2025/07/image1751234567890_abc123.jpg",
    "/uploads/refund-evidence/2025/07/image1751234567891_def456.jpg"
  ],
  "evidenceVideos": [
    "/uploads/refund-evidence/2025/07/video1751234567892_ghi789.mp4"
  ]
}
```

**Request Body - Hoàn trả toàn bộ:**
```json
{
  "userId": 123,
  "reason": "Toàn bộ đơn hàng không đúng như mô tả trên website",
  "additionalNotes": "Cả đơn hàng đều sai thông tin, cần hoàn trả toàn bộ",
  "refundDetails": [], // ❌ SAI: Không được để trống với API này!
  "evidenceImages": [
    "/uploads/refund-evidence/2025/07/image1751234567890_abc123.jpg"
  ],
  "evidenceVideos": []
}
```

⚠️ **LƯU Ý QUAN TRỌNG:**
- `orderId` đã có trong URL path `/{orderId}/request-refund` → KHÔNG cần trong body
- `refundDetails` KHÔNG được để trống - phải chỉ định từng sản phẩm cụ thể để hoàn
- Với hoàn trả toàn bộ: phải liệt kê TẤT CẢ sản phẩm trong đơn hàng trong `refundDetails`

**Validation Rules:**
- ✅ `orderId`: Phải tồn tại và thuộc về user
- ✅ `refundType`: "PARTIAL" hoặc "FULL"
- ✅ `reason`: Enum values - "PRODUCT_DEFECT", "NOT_AS_DESCRIBED", "DAMAGED_SHIPPING", "WRONG_ITEM", "QUALITY_ISSUE", "OTHER"
- ✅ `customerNote`: Bắt buộc, 10-1000 ký tự
- ✅ `evidenceImages`: Tối đa 10 files, optional
- ✅ `evidenceVideos`: Tối đa 3 files, optional
- ✅ `refundItems`: Bắt buộc với PARTIAL, mỗi `refundQuantity` ≤ số lượng đã mua

**Response Success:**
```json
{
  "status": 201,
  "message": "Tạo yêu cầu hoàn trả thành công. Admin sẽ xem xét và phản hồi trong 24-48h.",
  "data": {
    "refundRequestId": 15,
    "orderId": 1001,
    "orderCode": "ORD-2025072201",
    "userFullName": "Nguyễn Văn A",
    "refundType": "PARTIAL",
    "refundStatus": "PENDING",
    "refundStatusDisplay": "Chờ phê duyệt",
    "orderStatus": "REFUND_REQUESTED",
    "orderStatusDisplay": "Yêu cầu hoàn trả",
    "totalRefundAmount": 150000,
    "estimatedProcessTime": "24-48 giờ",
    "createdAt": 1751234567890,
    "trackingCode": "REF-2025072215"
  }
}
```

**Response Error - Đã có yêu cầu:**
```json
{
  "status": 400,
  "message": "Đơn hàng này đã có yêu cầu hoàn trả đang xử lý",
  "data": {
    "existingRefundId": 12,
    "existingStatus": "PENDING",
    "createdAt": 1751234567890
  }
}
```

---

## 📝 **2. USER THEO DÕI YÊU CẦU HOÀN HÀNG**

### **🔹 API: Lấy danh sách yêu cầu của user**

```http
GET /api/refunds/user/{userId}?page=0&size=10&status=ALL
Authorization: Bearer {user_token}
```

**Query Parameters:**
- `page`: Trang (default: 0)
- `size`: Số lượng/trang (default: 10, max: 50)
- `status`: "ALL", "PENDING", "APPROVED", "REJECTED", "COMPLETED"

**Response:**
```json
{
  "status": 200,
  "message": "Lấy danh sách yêu cầu hoàn trả thành công",
  "data": {
    "content": [
      {
        "refundRequestId": 15,
        "orderId": 1001,
        "orderCode": "ORD-2025072201",
        "refundType": "PARTIAL",
        "refundStatus": "PENDING",
        "refundStatusDisplay": "Chờ phê duyệt",
        "orderStatus": "REFUND_REQUESTED",
        "reason": "PRODUCT_DEFECT",
        "reasonDisplay": "Sản phẩm bị lỗi",
        "customerNote": "Sách bị rách ở bìa và có vết nước",
        "adminNote": null,
        "totalRefundAmount": 150000,
        "evidenceCount": {
          "images": 2,
          "videos": 1
        },
        "createdAt": 1751234567890,
        "approvedAt": null,
        "completedAt": null,
        "estimatedProcessTime": "24-48 giờ",
        "trackingCode": "REF-2025072215"
      }
    ],
    "pageNumber": 0,
    "pageSize": 10,
    "totalElements": 1,
    "totalPages": 1
  }
}
```

### **🔹 API: Chi tiết yêu cầu hoàn trả**

```http
GET /api/refunds/{refundRequestId}?userId={userId}
Authorization: Bearer {user_token}
```

**Response:**
```json
{
  "status": 200,
  "message": "Lấy chi tiết yêu cầu hoàn trả thành công",
  "data": {
    "refundRequestId": 15,
    "orderId": 1001,
    "orderCode": "ORD-2025072201",
    "orderDetails": {
      "userFullName": "Nguyễn Văn A",
      "userPhone": "0901234567",
      "orderDate": 1751234567890,
      "deliveredDate": 1751234567890,
      "totalAmount": 300000,
      "shippingFee": 30000
    },
    "refundInfo": {
      "refundType": "PARTIAL",
      "refundStatus": "PENDING",
      "refundStatusDisplay": "Chờ phê duyệt",
      "reason": "PRODUCT_DEFECT",
      "reasonDisplay": "Sản phẩm bị lỗi",
      "customerNote": "Sách bị rách ở bìa và có vết nước",
      "adminNote": null,
      "totalRefundAmount": 150000,
      "trackingCode": "REF-2025072215"
    },
    "refundItems": [
      {
        "bookId": 45,
        "bookTitle": "Sách Lập Trình Java",
        "bookImage": "/uploads/books/java-book.jpg",
        "unitPrice": 75000,
        "purchasedQuantity": 2,
        "refundQuantity": 1,
        "refundAmount": 75000,
        "reason": "Sách bị rách không thể sử dụng"
      },
      {
        "bookId": 46,
        "bookTitle": "Sách Spring Boot",
        "bookImage": "/uploads/books/spring-book.jpg",
        "unitPrice": 85000,
        "purchasedQuantity": 2,
        "refundQuantity": 2,
        "refundAmount": 170000,
        "reason": "Không đúng phiên bản như mô tả"
      }
    ],
    "evidenceFiles": {
      "images": [
        {
          "url": "/uploads/refund-evidence/2025/07/image1751234567890_abc123.jpg",
          "fileName": "evidence_1.jpg",
          "uploadedAt": 1751234567890
        }
      ],
      "videos": [
        {
          "url": "/uploads/refund-evidence/2025/07/video1751234567892_ghi789.mp4",
          "fileName": "evidence_video.mp4",
          "uploadedAt": 1751234567890
        }
      ]
    },
    "timeline": [
      {
        "status": "CREATED",
        "statusDisplay": "Đã tạo yêu cầu",
        "timestamp": 1751234567890,
        "note": "Khách hàng đã gửi yêu cầu hoàn trả"
      },
      {
        "status": "PENDING",
        "statusDisplay": "Chờ phê duyệt",
        "timestamp": 1751234567890,
        "note": "Yêu cầu đang được admin xem xét"
      }
    ],
    "createdAt": 1751234567890,
    "estimatedProcessTime": "24-48 giờ"
  }
}
```

---

## 📝 **3. ADMIN XEM DANH SÁCH YÊU CẦU HOÀN HÀNG**

### **🔹 API: Danh sách yêu cầu chờ phê duyệt**

```http
GET /api/refunds/pending?page=0&size=20&sortBy=createdAt&sortDir=desc
Authorization: Bearer {admin_token}
```

**Query Parameters:**
- `page`: Trang (default: 0)
- `size`: Số lượng/trang (default: 20, max: 100)
- `sortBy`: "createdAt", "totalRefundAmount", "orderCode"
- `sortDir`: "asc", "desc"

**Response:**
```json
{
  "status": 200,
  "message": "Lấy danh sách yêu cầu hoàn trả chờ phê duyệt thành công",
  "data": {
    "content": [
      {
        "refundRequestId": 15,
        "orderId": 1001,
        "orderCode": "ORD-2025072201",
        "userInfo": {
          "userId": 123,
          "fullName": "Nguyễn Văn A",
          "phone": "0901234567",
          "email": "nguyenvana@gmail.com"
        },
        "refundInfo": {
          "refundType": "PARTIAL",
          "refundStatus": "PENDING",
          "refundStatusDisplay": "Chờ phê duyệt",
          "orderStatus": "REFUND_REQUESTED",
          "reason": "PRODUCT_DEFECT",
          "reasonDisplay": "Sản phẩm bị lỗi",
          "totalRefundAmount": 150000,
          "trackingCode": "REF-2025072215"
        },
        "orderInfo": {
          "orderDate": 1751234567890,
          "deliveredDate": 1751234567890,
          "totalAmount": 300000,
          "daysSinceDelivery": 3
        },
        "evidenceCount": {
          "images": 2,
          "videos": 1,
          "hasEvidence": true
        },
        "priority": "HIGH", // HIGH, MEDIUM, LOW based on amount and days
        "createdAt": 1751234567890,
        "waitingDays": 2
      }
    ],
    "pageNumber": 0,
    "pageSize": 20,
    "totalElements": 25,
    "totalPages": 2,
    "summary": {
      "totalPendingRequests": 25,
      "totalRefundAmount": 3750000,
      "averageWaitingDays": 1.8,
      "highPriorityCount": 5
    }
  }
}
```

### **🔹 API: Tất cả yêu cầu hoàn hàng (với filter)**

```http
GET /api/refunds/all?page=0&size=20&status=ALL&refundType=ALL&dateFrom=1751234567890&dateTo=1751234567890&minAmount=0&maxAmount=1000000&sortBy=createdAt&sortDir=desc
Authorization: Bearer {admin_token}
```

**Query Parameters:**
- `status`: "ALL", "PENDING", "APPROVED", "REJECTED", "COMPLETED"
- `refundType`: "ALL", "PARTIAL", "FULL"
- `dateFrom`, `dateTo`: Timestamp range
- `minAmount`, `maxAmount`: Số tiền hoàn range
- `userId`: Filter theo user cụ thể
- `orderCode`: Tìm theo mã đơn hàng

---

## 📝 **4. ADMIN PHÊ DUYỆT YÊU CẦU HOÀN HÀNG**

### **🔹 API: Chi tiết để phê duyệt**

```http
GET /api/refunds/{refundRequestId}/admin-detail
Authorization: Bearer {admin_token}
```

**Response:** Tương tự user detail nhưng có thêm:
```json
{
  "data": {
    // ... all user detail fields
    "adminInfo": {
      "canApprove": true,
      "canReject": true,
      "requiresManagerApproval": false, // if amount > 1M
      "riskLevel": "LOW", // LOW, MEDIUM, HIGH
      "fraudScore": 0.15,
      "customerHistory": {
        "totalOrders": 25,
        "totalRefunds": 2,
        "refundRate": 8.0,
        "lastRefundDays": 45
      }
    }
  }
}
```

### **🔹 API: Chấp nhận yêu cầu hoàn trả**

```http
POST /api/refunds/{refundRequestId}/approve?adminId={adminId}
Content-Type: application/json
Authorization: Bearer {admin_token}
```

**Request Body:**
```json
{
  "adminNote": "Yêu cầu hợp lệ. Khách hàng đã cung cấp đầy đủ minh chứng. Sản phẩm thực sự bị lỗi.",
  "approvedRefundAmount": 150000, // Có thể điều chỉnh số tiền hoàn
  "needsPhysicalReturn": true, // Có cần trả hàng về kho không
  "returnAddress": "Kho BookStation - 123 Đường ABC, Quận 1, TP.HCM",
  "expectedReturnDays": 7
}
```

**Validation Rules:**
- ✅ RefundRequest phải có status `PENDING`
- ✅ Admin phải có quyền phê duyệt
- ✅ `adminNote`: Bắt buộc, 10-1000 ký tự
- ✅ `approvedRefundAmount`: ≤ `totalRefundAmount` ban đầu
- ✅ `needsPhysicalReturn`: Boolean
- ✅ Nếu amount > 1M: Cần manager approval

**Response Success:**
```json
{
  "status": 200,
  "message": "Phê duyệt yêu cầu hoàn trả thành công",
  "data": {
    "refundRequestId": 15,
    "orderId": 1001,
    "orderCode": "ORD-2025072201",
    "refundStatus": "APPROVED",
    "refundStatusDisplay": "Đã phê duyệt",
    "orderStatus": "REFUNDING",
    "orderStatusDisplay": "Đang hoàn tiền",
    "approvedRefundAmount": 150000,
    "adminNote": "Yêu cầu hợp lệ. Khách hàng đã cung cấp đầy đủ minh chứng.",
    "approvedAt": 1751234567890,
    "approvedByName": "Admin Nguyễn Thị B",
    "approvedById": 5,
    "needsPhysicalReturn": true,
    "returnInstructions": "Vui lòng gửi sản phẩm về địa chỉ: Kho BookStation - 123 Đường ABC, Quận 1, TP.HCM trong vòng 7 ngày.",
    "nextStep": "PROCESS_REFUND",
    "nextStepDisplay": "Xử lý hoàn tiền"
  }
}
```

### **🔹 API: Từ chối yêu cầu hoàn trả**

```http
POST /api/refunds/{refundRequestId}/reject?adminId={adminId}
Content-Type: application/json
Authorization: Bearer {admin_token}
```

**Request Body:**
```json
{
  "rejectReason": "INSUFFICIENT_EVIDENCE",
  "rejectReasonDisplay": "Minh chứng không đủ",
  "adminNote": "Hình ảnh không rõ ràng và không thể chứng minh sản phẩm bị lỗi do vận chuyển. Vui lòng cung cấp thêm minh chứng.",
  "suggestedAction": "Khách hàng có thể gửi lại yêu cầu với minh chứng rõ ràng hơn."
}
```

**Validation Rules:**
- ✅ RefundRequest phải có status `PENDING`
- ✅ `rejectReason`: "INSUFFICIENT_EVIDENCE", "POLICY_VIOLATION", "DAMAGED_BY_USER", "EXPIRED_RETURN_PERIOD", "OTHER"
- ✅ `adminNote`: Bắt buộc, 10-1000 ký tự

**Response:**
```json
{
  "status": 200,
  "message": "Đã từ chối yêu cầu hoàn trả",
  "data": {
    "refundRequestId": 15,
    "orderId": 1001,
    "orderCode": "ORD-2025072201",
    "refundStatus": "REJECTED",
    "refundStatusDisplay": "Đã từ chối",
    "orderStatus": "DELIVERED",
    "orderStatusDisplay": "Đã giao hàng",
    "rejectReason": "INSUFFICIENT_EVIDENCE",
    "rejectReasonDisplay": "Minh chứng không đủ",
    "adminNote": "Hình ảnh không rõ ràng và không thể chứng minh sản phẩm bị lỗi",
    "rejectedAt": 1751234567890,
    "rejectedByName": "Admin Nguyễn Thị B",
    "rejectedById": 5,
    "canResubmit": true,
    "resubmitInstructions": "Khách hàng có thể gửi lại yêu cầu với minh chứng rõ ràng hơn."
  }
}
```

---

## 📝 **5. ADMIN XỬ LÝ HOÀN TIỀN**

### **🔹 API: Xử lý hoàn tiền sau phê duyệt**

```http
POST /api/refunds/{refundRequestId}/process?adminId={adminId}
Content-Type: application/json
Authorization: Bearer {admin_token}
```

**Request Body:**
```json
{
  "processType": "AUTOMATIC", // "AUTOMATIC" hoặc "MANUAL"
  "paymentMethod": "BANK_TRANSFER", // "BANK_TRANSFER", "E_WALLET", "CASH", "STORE_CREDIT"
  "bankInfo": {
    "bankName": "Vietcombank",
    "accountNumber": "1234567890",
    "accountName": "NGUYEN VAN A",
    "transferNote": "Hoàn tiền đơn hàng ORD-2025072201"
  },
  "processingNote": "Đã xử lý hoàn tiền qua chuyển khoản. Khách hàng sẽ nhận tiền trong 1-2 ngày làm việc.",
  "refundFeeDeduction": 0, // Phí xử lý hoàn tiền (nếu có)
  "finalRefundAmount": 150000
}
```

**Validation Rules:**
- ✅ RefundRequest phải có status `APPROVED`
- ✅ Admin phải có quyền xử lý hoàn tiền
- ✅ `finalRefundAmount` ≤ `approvedRefundAmount`

**Response:**
```json
{
  "status": 200,
  "message": "Xử lý hoàn trả thành công",
  "data": {
    "refundRequestId": 15,
    "orderId": 1001,
    "orderCode": "ORD-2025072201",
    "refundStatus": "COMPLETED",
    "refundStatusDisplay": "Hoàn thành",
    "orderStatus": "PARTIALLY_REFUNDED", // hoặc "REFUNDED" nếu FULL
    "orderStatusDisplay": "Hoàn tiền một phần",
    "finalRefundAmount": 150000,
    "refundFeeDeduction": 0,
    "paymentMethod": "BANK_TRANSFER",
    "processingNote": "Đã xử lý hoàn tiền qua chuyển khoản",
    "completedAt": 1751234567890,
    "processedByName": "Admin Nguyễn Thị B",
    "processedById": 5,
    "transactionId": "TXN-2025072215-REF",
    "estimatedReceiptTime": "1-2 ngày làm việc",
    "businessLogicApplied": {
      "stockRestored": true,
      "soldCountUpdated": true,
      "pointsDeducted": true,
      "vouchersRestored": true
    }
  }
}
```

## 📝 **6. ADMIN HOÀN HÀNG TRỰC TIẾP (BYPASS)**

### **🔹 API: Hoàn hàng một phần trực tiếp**

```http
POST /api/refunds/admin/partial-refund
Content-Type: application/json
Authorization: Bearer {admin_token}
```

**Request Body:**
```json
{
  "adminId": 5,
  "reason": "COMPENSATION", 
  "reasonDisplay": "Bồi thường cho khách hàng",
  "adminNote": "Khách hàng phản ánh qua hotline về chất lượng sản phẩm. Xử lý bồi thường để giữ mối quan hệ.",
  "refundItems": [
    {
      "bookId": 45,
      "refundQuantity": 1,
      "refundReason": "Chất lượng không đạt yêu cầu"
    }
  ],
  "paymentMethod": "STORE_CREDIT",
  "skipApproval": true
}
```

### **🔹 API: Hoàn hàng toàn bộ trực tiếp**

```http
POST /api/refunds/admin/full-refund
Content-Type: application/json
Authorization: Bearer {admin_token}
```

**Request Body:**
```json
{
  "orderId": 1001,
  "adminId": 5,
  "reason": "OPERATIONAL_ERROR",
  "reasonDisplay": "Lỗi vận hành",
  "adminNote": "Nhầm lẫn trong quá trình đóng gói. Hoàn trả toàn bộ đơn hàng.",
  "paymentMethod": "BANK_TRANSFER",
  "skipApproval": true
}
```

**Response tương tự process API nhưng:**
- `refundStatus`: Trực tiếp `COMPLETED`
- `orderStatus`: Trực tiếp `REFUNDED` hoặc `PARTIALLY_REFUNDED`
- `approvedAt` = `completedAt` = hiện tại

---

## 📝 **7. CÁC API HỖ TRỢ**

### **🔹 API: Thống kê hoàn hàng**

```http
GET /api/refunds/statistics?period=LAST_30_DAYS&adminId={adminId}
Authorization: Bearer {admin_token}
```

**Response:**
```json
{
  "status": 200,
  "message": "Lấy thống kê hoàn hàng thành công",
  "data": {
    "summary": {
      "totalRequests": 125,
      "pendingRequests": 25,
      "approvedRequests": 80,
      "rejectedRequests": 20,
      "completedRequests": 75,
      "totalRefundAmount": 18750000,
      "averageProcessingTime": 1.8 // days
    },
    "trends": {
      "dailyRequests": [
        { "date": "2025-07-21", "count": 5, "amount": 750000 },
        { "date": "2025-07-22", "count": 8, "amount": 1200000 }
      ]
    },
    "topReasons": [
      { "reason": "PRODUCT_DEFECT", "count": 45, "percentage": 36.0 },
      { "reason": "NOT_AS_DESCRIBED", "count": 30, "percentage": 24.0 }
    ]
  }
}
```

### **🔹 API: Export báo cáo**

```http
GET /api/refunds/export?format=EXCEL&dateFrom=1751234567890&dateTo=1751234567890&status=ALL
Authorization: Bearer {admin_token}
```

---

## ⚠️ **8. XỬ LÝ LỖI VÀ EDGE CASES**

### **🔹 Case 1: Order không thể hoàn trả**
```json
{
  "status": 400,
  "message": "Đơn hàng không thể hoàn trả",
  "data": {
    "errorCode": "ORDER_NOT_REFUNDABLE",
    "currentStatus": "CANCELED",
    "allowedStatuses": ["DELIVERED"],
    "canRefundAfter": null
  }
}
```

### **🔹 Case 2: Quá hạn hoàn trả**
```json
{
  "status": 400,
  "message": "Đã quá hạn hoàn trả sản phẩm",
  "data": {
    "errorCode": "REFUND_PERIOD_EXPIRED",
    "deliveredDate": 1751234567890,
    "deadlineDate": 1751234567890,
    "overdueDays": 3,
    "refundPolicy": "Chỉ được hoàn trả trong vòng 7 ngày kể từ khi nhận hàng"
  }
}
```

### **🔹 Case 3: Số lượng hoàn vượt quá**
```json
{
  "status": 400,
  "message": "Số lượng hoàn trả vượt quá số lượng đã mua",
  "data": {
    "errorCode": "INVALID_REFUND_QUANTITY",
    "invalidItems": [
      {
        "bookId": 45,
        "purchasedQuantity": 2,
        "requestedRefundQuantity": 3,
        "availableRefundQuantity": 2
      }
    ]
  }
}
```

### **🔹 Case 4: Thiếu quyền admin**
```json
{
  "status": 403,
  "message": "Không có quyền thực hiện thao tác này",
  "data": {
    "errorCode": "INSUFFICIENT_ADMIN_PERMISSION",
    "requiredRole": "ADMIN_REFUND_HANDLER",
    "currentRole": "ADMIN_SUPPORT"
  }
}
```

---
