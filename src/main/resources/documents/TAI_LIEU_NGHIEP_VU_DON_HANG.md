# TÀI LIỆU NGHIỆP VỤ HỆ THỐNG QUẢN LÝ ĐÔN HÀNG

## TỔNG QUAN

Hệ thống quản lý đơn hàng BookStation được thiết kế với nghiệp vụ phù hợp cho cả bán hàng online và tại quầy. Hệ thống áp dụng quy trình quản lý trạng thái nghiêm ngặt và tích hợp các tính năng như tích điểm, flash sale, voucher.

## 1. KIỂU ĐƠN HÀNG (ORDER TYPE)

### Quy định chặt chẽ:
- **ONLINE**: Đơn hàng đặt qua website/app
- **TẠI QUẦY**: Đơn hàng tạo trực tiếp tại cửa hàng

### Validation:
- Hệ thống chỉ chấp nhận 2 loại đơn hàng này
- Mọi loại khác sẽ bị từ chối với thông báo lỗi

## 2. TRẠNG THÁI ĐƠN HÀNG VÀ CHU TRÌNH XỬ LÝ

### 2.1 Các trạng thái hiện tại:
1. **PENDING** - Chờ xác nhận
2. **CONFIRMED** - Đã xác nhận
3. **SHIPPED** - Đang vận chuyển
4. **DELIVERED** - Đã giao hàng
5. **CANCELED** - Đã hủy
6. **REFUND_REQUESTED** - Yêu cầu hoàn trả (chờ admin xem xét)
7. **REFUNDING** - Đang hoàn trả (admin đã chấp nhận)
8. **REFUNDED** - Đã hoàn trả toàn bộ
9. **RETURNED** - Đã trả hàng
10. **PARTIALLY_REFUNDED** - Hoàn trả một phần

### 2.2 Luồng chuyển trạng thái chuẩn:

#### Đơn hàng thành công:
```
PENDING → CONFIRMED → SHIPPED → DELIVERED
```

#### Đơn hàng bị hủy:
```
PENDING → CANCELED
CONFIRMED → CANCELED (trong một số trường hợp)
```

#### Đơn hàng hoàn trả:
```
DELIVERED → REFUND_REQUESTED → REFUNDING → REFUNDED (admin chấp nhận hoàn trả toàn bộ)
DELIVERED → REFUND_REQUESTED → REFUNDING → PARTIALLY_REFUNDED (admin chấp nhận hoàn trả một phần)
DELIVERED → REFUND_REQUESTED → DELIVERED (admin từ chối hoàn trả)
```

## 3. API QUẢN LÝ CHUYỂN TRẠNG THÁI ĐƠN HÀNG

### 3.1 OrderStatusTransitionService

Hệ thống có API riêng biệt để xử lý chuyển trạng thái đơn hàng:

**Endpoint**: `/api/orders/{orderId}/status-transition`
**Method**: POST

**Request Body**:
```json
{
  "currentStatus": "PENDING",
  "newStatus": "CONFIRMED", 
  "performedBy": "STAFF",
  "reason": "Đã xác nhận đơn hàng",
  "notes": "Ghi chú thêm",
  "staffId": 123,
  "trackingNumber": "TN123456789"
}
```

**Response**:
```json
{
  "success": true,
  "message": "Chuyển trạng thái thành công",
  "data": {
    "orderId": 1,
    "oldStatus": "PENDING",
    "newStatus": "CONFIRMED",
    "transitionTime": "2025-01-18T10:30:00",
    "businessImpact": {
      "pointImpact": {
        "pointsAwarded": 0,
        "pointsDeducted": 0,
        "newTotalPoints": 1500
      },
      "stockImpact": {
        "itemsRestored": [],
        "itemsReserved": []
      },
      "voucherImpact": {
        "vouchersRestored": [],
        "vouchersUsed": []
      }
    }
  }
}
```

### 3.2 Nghiệp vụ tự động khi chuyển trạng thái:

#### PENDING → CONFIRMED:
- Ghi log xác nhận đơn hàng
- Không có tác động nghiệp vụ khác

#### CONFIRMED → SHIPPED:
- Ghi log xuất kho
- Không có tác động nghiệp vụ khác

#### SHIPPED → DELIVERED:
- **Tích điểm cho khách hàng** dựa trên:
  - Tổng tiền đơn hàng
  - Rank hiện tại của khách hàng
  - Quy tắc tích điểm theo rank
- Cập nhật `totalSpent` của khách hàng
- Kiểm tra và nâng rank tự động nếu đủ điều kiện

#### Bất kỳ trạng thái nào → CANCELED:
- **Hoàn lại stock cho Flash Sale items**:
  - Tăng `stockQuantity` 
  - Giảm `soldCount`
- **Hoàn lại voucher đã sử dụng** (nếu có)
- Ghi log hủy đơn hàng

#### DELIVERED → REFUNDED (hoàn trả toàn bộ):
- **Trừ điểm đã tích từ đơn hàng này**
- **Hoàn lại stock cho tất cả Flash Sale items**
- **Hoàn lại voucher đã sử dụng**
- Cập nhật `totalSpent` của khách hàng (trừ đi)

#### DELIVERED → PARTIALLY_REFUNDED (hoàn trả một phần):
- **Trừ điểm tỷ lệ với số tiền hoàn trả**
- **Hoàn lại stock cho items được hoàn trả**
- Cập nhật `totalSpent` của khách hàng (trừ đi số tiền hoàn trả)

## 4. CHI TIẾT NGHIỆP VỤ TÍCH ĐIỂM

### 4.1 Quy tắc tích điểm:
- Chỉ tích điểm khi đơn hàng chuyển sang trạng thái **DELIVERED**
- Số điểm = Tổng tiền × Tỷ lệ theo rank
- Tỷ lệ tích điểm phụ thuộc vào rank của khách hàng

### 4.2 Quy tắc trừ điểm:
- **Hủy đơn hàng**: Trừ tất cả điểm đã tích từ đơn hàng này
- **Hoàn trả toàn bộ**: Trừ tất cả điểm đã tích từ đơn hàng này  
- **Hoàn trả một phần**: Trừ điểm tỷ lệ với số tiền hoàn trả

### 4.3 Cập nhật rank tự động:
- Sau mỗi lần tích/trừ điểm, hệ thống tự động kiểm tra rank
- Nâng rank nếu `totalSpent` đạt ngưỡng cao hơn
- Hạ rank nếu `totalSpent` giảm xuống dưới ngưỡng hiện tại

## 5. QUẢN LÝ FLASH SALE

### 5.1 Khi tạo đơn hàng:
- Kiểm tra `stockQuantity` của flash sale item
- Nếu đủ stock:
  - Giảm `stockQuantity`
  - Tăng `soldCount`
- Nếu không đủ stock: Từ chối đơn hàng

### 5.2 Khi hủy/hoàn trả đơn hàng:
- Hoàn lại `stockQuantity`
- Giảm `soldCount`
- Đảm bảo tính chính xác của số liệu

## 6. QUẢN LÝ VOUCHER

### 6.1 Áp dụng voucher:
- Kiểm tra điều kiện áp dụng
- Giảm số lượng sử dụng có thể của voucher
- Tính toán giảm giá

### 6.2 Hoàn lại voucher:
- Khi hủy/hoàn trả đơn hàng
- Tăng lại số lượng sử dụng có thể của voucher
- Cập nhật lại trạng thái voucher

## 7. API HOÀN TRẢ VÀ UPLOAD MINH CHỨNG

### 7.1 Quy trình hoàn trả mới (có admin xem xét):

#### Bước 1: Khách hàng gửi yêu cầu hoàn trả
**Endpoint**: `/api/orders/{orderId}/request-refund`
**Method**: POST

**Request Body**:
```json
{
  "userId": 123,
  "reason": "Sản phẩm bị lỗi",
  "additionalNotes": "Sách bị rách ở góc trang 15",
  "refundDetails": [
    {
      "bookId": 456,
      "refundQuantity": 1,
      "reason": "Sách bị rách",
      "evidenceImages": [
        "/uploads/refund-evidence/2025/07/image1751234567890_abc123.jpg"
      ],
      "evidenceVideos": [
        "/uploads/refund-evidence/2025/07/video1751234567892_ghi789.mp4"
      ],
      "additionalNotes": "Ảnh hưởng đến việc đọc"
    }
  ],
  "evidenceImages": [
    "/uploads/refund-evidence/2025/07/image1751234567890_abc123.jpg"
  ],
  "evidenceVideos": [
    "/uploads/refund-evidence/2025/07/video1751234567892_ghi789.mp4"
  ]
}
```

**Response**:
```json
{
  "status": 200,
  "message": "Yêu cầu hoàn trả đã được gửi thành công. Admin sẽ xem xét và phản hồi sớm nhất.",
  "data": {
    "id": 123,
    "code": "ORD-20250720-001",
    "orderStatus": "REFUND_REQUESTED",
    "cancelReason": "Sản phẩm bị lỗi"
  }
}
```

#### Bước 2: Admin xem xét và quyết định
**Admin chấp nhận hoàn trả:**
**Endpoint**: `/api/orders/admin/approve-refund`
**Method**: POST

**Request Body**:
```json
{
  "orderId": 123,
  "adminId": 456,
  "approved": true,
  "adminNotes": "Đã xác minh sản phẩm bị lỗi, chấp nhận hoàn trả"
}
```

**Admin từ chối hoàn trả:**
**Endpoint**: `/api/orders/admin/reject-refund`
**Method**: POST

**Request Body**:
```json
{
  "orderId": 123,
  "adminId": 456,
  "approved": false,
  "adminNotes": "Sản phẩm không đủ điều kiện hoàn trả theo chính sách"
}
```

**Response (cả 2 trường hợp)**:
```json
{
  "status": 200,
  "message": "Yêu cầu hoàn trả đã được chấp nhận và xử lý thành công", // hoặc "đã bị từ chối"
  "data": {
    "id": 123,
    "code": "ORD-20250720-001",
    "orderStatus": "REFUNDED", // hoặc "DELIVERED" nếu từ chối
    "cancelReason": "Đã xác minh sản phẩm bị lỗi, chấp nhận hoàn trả"
  }
}
```

### 7.2 Upload ảnh/video minh chứng cho hoàn hàng:

#### Upload chỉ ảnh:
**Endpoint**: `/api/refund-evidence/images`
**Method**: POST
**Content-Type**: `multipart/form-data`

**Request**:
```
files: MultipartFile[] (tối đa 10 ảnh, mỗi ảnh ≤ 5MB)
Định dạng: jpeg, jpg, png, gif, webp
```

#### Upload chỉ video:
**Endpoint**: `/api/refund-evidence/videos`
**Method**: POST
**Content-Type**: `multipart/form-data`

**Request**:
```
files: MultipartFile[] (tối đa 3 video, mỗi video ≤ 50MB)
Định dạng: mp4, avi, mov, wmv, webm
```

#### Upload cả ảnh và video:
**Endpoint**: `/api/refund-evidence/mixed`
**Method**: POST
**Content-Type**: `multipart/form-data`

**Request**:
```
images: MultipartFile[] (tối đa 10 ảnh)
videos: MultipartFile[] (tối đa 3 video)
```

**Response**:
```json
{
  "status": 200,
  "message": "Upload minh chứng hoàn hàng thành công",
  "data": {
    "imagePaths": [
      "/uploads/refund-evidence/2025/07/image1751234567890_abc123.jpg",
      "/uploads/refund-evidence/2025/07/image1751234567891_def456.png"
    ],
    "videoPaths": [
      "/uploads/refund-evidence/2025/07/video1751234567892_ghi789.mp4"
    ]
  }
}
```

### 7.3 Hoàn trả trực tiếp (không cần admin duyệt - dành cho trường hợp đặc biệt):

#### Hoàn trả một phần:
**Endpoint**: `/api/orders/{orderId}/partial-refund`
**Method**: POST

**Request Body**:
```json
{
  "userId": 123,
  "reason": "Sản phẩm bị lỗi",
  "refundDetails": [
    {
      "bookId": 456,
      "refundQuantity": 1,
      "reason": "Sách bị rách",
      "evidenceImages": [
        "/uploads/refund-evidence/2025/07/image1751234567890_abc123.jpg",
        "/uploads/refund-evidence/2025/07/image1751234567891_def456.png"
      ],
      "evidenceVideos": [
        "/uploads/refund-evidence/2025/07/video1751234567892_ghi789.mp4"
      ],
      "additionalNotes": "Sách bị rách ở góc trang 15, ảnh hưởng đến việc đọc"
    }
  ]
}
```

#### Hoàn trả toàn bộ:
**Endpoint**: `/api/orders/{orderId}/full-refund`
**Method**: POST

**Request Body**:
```json
{
  "userId": 123,
  "reason": "Khách hàng đổi ý"
}
```

### 7.4 Quy trình upload và hoàn trả:
1. **Frontend upload ảnh/video** → Nhận về danh sách đường dẫn
2. **Frontend gửi request hoàn trả** → Kèm theo đường dẫn ảnh/video
3. **Backend xử lý hoàn trả** → Lưu thông tin minh chứng vào database
4. **Hoàn stock và trừ điểm** → Tự động theo business logic

## 8. BẢO MẬT CHECKOUT SESSION

### 8.1 Ngăn chặn tái sử dụng checkout session:
- **Trạng thái session**: active (1), expired (0), completed (2)
- **Khi tạo đơn hàng thành công**: Session tự động chuyển sang `status = 2` (completed)
- **Validation nghiêm ngặt**: Session với `status ≠ 1` không thể tạo đơn hàng
- **Thông báo rõ ràng**: "Phiên checkout này đã được sử dụng để tạo đơn hàng. Vui lòng tạo phiên checkout mới."

### 8.2 Quy trình bảo mật:
1. **Tạo checkout session** → `status = 1` (active)
2. **Tạo đơn hàng thành công** → `status = 2` (completed)  
3. **Cố gắng tái sử dụng** → Bị từ chối với lỗi rõ ràng
4. **Frontend phải tạo session mới** → Từ cart hoặc product detail

## 9. QUY TRÌNH XỬ LÝ LỖI

### 9.1 Validation lỗi:
- Kiểm tra quyền truy cập (chỉ chủ đơn hàng hoặc staff)
- Kiểm tra trạng thái hiện tại có cho phép thao tác không
- Kiểm tra tính hợp lệ của dữ liệu

### 9.2 Rollback khi lỗi:
- Sử dụng `@Transactional` để đảm bảo tính nhất quán
- Tự động rollback khi có exception
- Ghi log chi tiết cho việc debug

## 10. LOGGING VÀ AUDIT

### 10.1 Ghi log cho mọi thao tác:
- Thời gian thực hiện
- Người thực hiện
- Trạng thái cũ và mới
- Lý do thay đổi
- Tác động nghiệp vụ

### 10.2 Tracking thay đổi:
- Sử dụng `createdBy`, `updatedBy`
- Timestamp tự động với `createdAt`, `updatedAt`
- Lưu trữ lý do trong `cancelReason`

## 11. BẢO MẬT VÀ PHÂN QUYỀN

### 11.1 Phân quyền thao tác:
- **Khách hàng**: Chỉ có thể hủy đơn hàng PENDING của chính mình
- **Staff**: Có thể thay đổi trạng thái đơn hàng
- **Admin**: Có thể thực hiện mọi thao tác

### 11.2 Validation bảo mật:
- Kiểm tra ownership của đơn hàng
- Xác thực quyền trước khi thao tác
- Ghi log mọi thao tác nhạy cảm

## 12. KẾT LUẬN

Hệ thống được thiết kế với nghiệp vụ chặt chẽ, đảm bảo:
- Tính nhất quán của dữ liệu
- Trải nghiệm người dùng tốt
- Dễ dàng mở rộng và bảo trì
- Tuân thủ các quy tắc nghiệp vụ ngành sách

Mọi thay đổi nghiệp vụ trong tương lai cần tuân thủ các nguyên tắc đã được thiết lập trong tài liệu này.
