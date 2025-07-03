# HƯỚNG DẪN SỬ DỤNG API QUẢN LÝ HẠNG (RANK)

## Giới thiệu
Tài liệu này cung cấp hướng dẫn chi tiết về cách sử dụng các API liên quan đến quản lý hạng (Rank) trong hệ thống BookStation. Các API này được sử dụng để xem, thêm, sửa, xóa và cập nhật trạng thái của các hạng thành viên.

## Đường dẫn cơ sở
```
/api/ranks
```

## Danh sách API

### 1. Lấy danh sách hạng có phân trang

#### Yêu cầu
- Phương thức: `GET`
- Đường dẫn: `/api/ranks`
- Tham số truy vấn:
  - `page` (mặc định: 0): Trang hiện tại
  - `size` (mặc định: 5): Số lượng mục trên mỗi trang
  - `name` (không bắt buộc): Lọc theo tên hạng
  - `status` (không bắt buộc): Lọc theo trạng thái (1: Kích hoạt, 0: Vô hiệu)

#### Phản hồi thành công
- Mã trạng thái: 200 OK
- Nội dung phản hồi:
```json
{
  "status": 200,
  "message": "Thành công",
  "data": {
    "content": [
      {
        "id": 1,
        "rankName": "Bạc",
        "pointsRequired": 100,
        "discountPercent": 5,
        "status": 1,
        "createdAt": "2025-07-01T10:00:00",
        "updatedAt": "2025-07-01T10:00:00",
        "createdBy": "admin",
        "updatedBy": "admin"
      },
      ...
    ],
    "totalElements": 10,
    "totalPages": 2,
    "currentPage": 0,
    "size": 5
  }
}
```

#### Lỗi có thể xảy ra
- Mã trạng thái: 500 Internal Server Error
- Nội dung phản hồi:
```json
{
  "status": 500,
  "message": "Lỗi máy chủ nội bộ",
  "data": null
}
```

### 2. Lấy thông tin chi tiết của một hạng

#### Yêu cầu
- Phương thức: `GET`
- Đường dẫn: `/api/ranks/{id}`
- Tham số đường dẫn:
  - `id`: ID của hạng cần lấy thông tin

#### Phản hồi thành công
- Mã trạng thái: 200 OK
- Nội dung phản hồi:
```json
{
  "status": 200,
  "message": "Thành công",
  "data": {
    "id": 1,
    "rankName": "Bạc",
    "pointsRequired": 100,
    "discountPercent": 5,
    "status": 1,
    "createdAt": "2025-07-01T10:00:00",
    "updatedAt": "2025-07-01T10:00:00",
    "createdBy": "admin",
    "updatedBy": "admin"
  }
}
```

#### Lỗi có thể xảy ra
- Mã trạng thái: 404 Not Found
- Nội dung phản hồi:
```json
{
  "status": 404,
  "message": "Không tìm thấy",
  "data": null
}
```

### 3. Thêm hạng mới

#### Yêu cầu
- Phương thức: `POST`
- Đường dẫn: `/api/ranks`
- Định dạng dữ liệu: JSON
- Dữ liệu đầu vào:
```json
{
  "rankName": "Vàng",
  "pointsRequired": 500,
  "discountPercent": 10,
  "status": 1,
  "createdBy": "admin"
}
```

#### Phản hồi thành công
- Mã trạng thái: 201 Created
- Nội dung phản hồi:
```json
{
  "status": 201,
  "message": "Tạo mới thành công",
  "data": {
    "id": 3,
    "rankName": "Vàng",
    "pointsRequired": 500,
    "discountPercent": 10,
    "status": 1,
    "createdAt": "2025-07-03T14:30:00",
    "updatedAt": null,
    "createdBy": "admin",
    "updatedBy": null
  }
}
```

#### Lỗi có thể xảy ra
- Mã trạng thái: 400 Bad Request (tên hạng đã tồn tại)
- Nội dung phản hồi:
```json
{
  "status": 400,
  "message": "Tên hạng đã tồn tại",
  "data": null
}
```

- Mã trạng thái: 404 Not Found
- Nội dung phản hồi:
```json
{
  "status": 404,
  "message": "Không tìm thấy",
  "data": null
}
```

### 4. Cập nhật thông tin hạng

#### Yêu cầu
- Phương thức: `PUT`
- Đường dẫn: `/api/ranks/{id}`
- Tham số đường dẫn:
  - `id`: ID của hạng cần cập nhật
- Định dạng dữ liệu: JSON
- Dữ liệu đầu vào:
```json
{
  "id": 1,
  "rankName": "Bạc Mới",
  "pointsRequired": 150,
  "discountPercent": 7,
  "status": 1,
  "updatedBy": "admin"
}
```

#### Phản hồi thành công
- Mã trạng thái: 200 OK
- Nội dung phản hồi:
```json
{
  "status": 200,
  "message": "Cập nhật thành công",
  "data": {
    "id": 1,
    "rankName": "Bạc Mới",
    "pointsRequired": 150,
    "discountPercent": 7,
    "status": 1,
    "createdAt": "2025-07-01T10:00:00",
    "updatedAt": "2025-07-03T15:00:00",
    "createdBy": "admin",
    "updatedBy": "admin"
  }
}
```

#### Lỗi có thể xảy ra
- Mã trạng thái: 404 Not Found
- Nội dung phản hồi:
```json
{
  "status": 404,
  "message": "Không tìm thấy",
  "data": null
}
```

### 5. Xóa hạng

#### Yêu cầu
- Phương thức: `DELETE`
- Đường dẫn: `/api/ranks/{id}`
- Tham số đường dẫn:
  - `id`: ID của hạng cần xóa

#### Phản hồi thành công
- Mã trạng thái: 204 No Content (không có nội dung trả về)

#### Lỗi có thể xảy ra
- Mã trạng thái: 404 Not Found (nếu không tìm thấy hạng cần xóa)
- Mã trạng thái: 500 Internal Server Error (nếu có lỗi xảy ra trong quá trình xóa)

### 6. Cập nhật trạng thái hạng

#### Yêu cầu
- Phương thức: `PATCH`
- Đường dẫn: `/api/ranks/{id}/toggle-status`
- Tham số đường dẫn:
  - `id`: ID của hạng cần cập nhật trạng thái

#### Phản hồi thành công
- Mã trạng thái: 200 OK
- Nội dung phản hồi:
```json
{
  "status": 200,
  "message": "Cập nhật trạng thái thành công",
  "data": {
    "id": 1,
    "rankName": "Bạc",
    "pointsRequired": 100,
    "discountPercent": 5,
    "status": 0, // Trạng thái đã được chuyển từ 1 sang 0 hoặc ngược lại
    "createdAt": "2025-07-01T10:00:00",
    "updatedAt": "2025-07-03T16:00:00",
    "createdBy": "admin",
    "updatedBy": "admin"
  }
}
```

#### Lỗi có thể xảy ra
- Mã trạng thái: 404 Not Found
- Nội dung phản hồi:
```json
{
  "status": 404,
  "message": "Không tìm thấy",
  "data": null
}
```

### 7. Lấy danh sách hạng cho dropdown

#### Yêu cầu
- Phương thức: `GET`
- Đường dẫn: `/api/ranks/dropdown`

#### Phản hồi thành công
- Mã trạng thái: 200 OK
- Nội dung phản hồi:
```json
{
  "status": 200,
  "message": "Lấy danh sách hạng thành công",
  "data": [
    {
      "id": 1,
      "label": "Bạc"
    },
    {
      "id": 2,
      "label": "Vàng"
    },
    {
      "id": 3,
      "label": "Kim Cương"
    }
  ]
}
```

#### Lỗi có thể xảy ra
- Mã trạng thái: 500 Internal Server Error
- Nội dung phản hồi:
```json
{
  "status": 500,
  "message": "Lỗi máy chủ nội bộ",
  "data": null
}
```

## Lưu ý quan trọng

1. Tất cả các yêu cầu POST và PUT phải có header `Content-Type: application/json`.
2. Trạng thái (status) của hạng có giá trị là 1 (kích hoạt) hoặc 0 (vô hiệu).
3. API dropdown chỉ trả về các hạng có trạng thái kích hoạt (status = 1).
4. Khi cập nhật trạng thái thông qua API toggle-status, trạng thái sẽ tự động chuyển từ 0 sang 1 hoặc từ 1 sang 0.
5. Đối với các lỗi không được liệt kê cụ thể, hệ thống có thể trả về mã lỗi 500 với thông báo "Lỗi máy chủ nội bộ".
