# HƯỚNG DẪN SỬ DỤNG API QUẢN LÝ NHÀ CUNG CẤP (SUPPLIER)

## Giới thiệu
Tài liệu này cung cấp hướng dẫn chi tiết về cách sử dụng các API liên quan đến quản lý nhà cung cấp (Supplier) trong hệ thống BookStation. Các API này được sử dụng để xem, thêm, sửa, xóa và cập nhật trạng thái của các nhà cung cấp.

## Đường dẫn cơ sở
```
/api/suppliers
```

## Danh sách API

### 1. Lấy danh sách nhà cung cấp có phân trang

#### Yêu cầu
- Phương thức: `GET`
- Đường dẫn: `/api/suppliers`
- Tham số truy vấn:
  - `page` (mặc định: 0): Trang hiện tại
  - `size` (mặc định: 10): Số lượng mục trên mỗi trang
  - `supplierName` (không bắt buộc): Lọc theo tên nhà cung cấp
  - `contactName` (không bắt buộc): Lọc theo tên liên hệ
  - `email` (không bắt buộc): Lọc theo email
  - `status` (không bắt buộc): Lọc theo trạng thái

#### Phản hồi thành công
- Định dạng dữ liệu: JSON
- Nội dung phản hồi:
```json
{
  "content": [
    {
      "id": 1,
      "supplierName": "Công ty Sách Kim Đồng",
      "contactName": "Nguyễn Văn A",
      "contactTitle": "Giám đốc kinh doanh",
      "address": "55 Quang Trung, Hai Bà Trưng, Hà Nội",
      "city": "Hà Nội",
      "region": "Miền Bắc",
      "phone": "0987654321",
      "email": "kimdong@example.com",
      "website": "http://nxbkimdong.com",
      "status": 1,
      "createdAt": "2025-06-01T10:00:00",
      "updatedAt": "2025-07-01T10:00:00",
      "createdBy": "admin",
      "updatedBy": "admin"
    },
    ...
  ],
  "totalElements": 25,
  "totalPages": 3,
  "currentPage": 0,
  "size": 10
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

### 2. Thêm nhà cung cấp mới

#### Yêu cầu
- Phương thức: `POST`
- Đường dẫn: `/api/suppliers`
- Định dạng dữ liệu: JSON
- Dữ liệu đầu vào:
```json
{
  "supplierName": "NXB Trẻ",
  "contactName": "Trần Văn B",
  "contactTitle": "Trưởng phòng kinh doanh",
  "address": "161B Lý Chính Thắng, Phường 7, Quận 3, TP.HCM",
  "city": "Hồ Chí Minh",
  "region": "Miền Nam",
  "phone": "0123456789",
  "email": "nxbtre@example.com",
  "website": "http://nxbtre.com",
  "status": 1,
  "createdBy": "admin"
}
```

#### Phản hồi thành công
- Mã trạng thái: 200 OK (không có nội dung trả về cụ thể)

#### Lỗi có thể xảy ra
- Mã trạng thái: 400 Bad Request (dữ liệu không hợp lệ)
- Mã trạng thái: 500 Internal Server Error (lỗi máy chủ nội bộ)

### 3. Cập nhật thông tin nhà cung cấp

#### Yêu cầu
- Phương thức: `PUT`
- Đường dẫn: `/api/suppliers`
- Định dạng dữ liệu: JSON
- Dữ liệu đầu vào:
```json
{
  "id": 1, // ID của nhà cung cấp cần cập nhật
  "supplierName": "Công ty Sách Kim Đồng - CN Miền Bắc",
  "contactName": "Nguyễn Văn A",
  "contactTitle": "Giám đốc kinh doanh",
  "address": "55 Quang Trung, Hai Bà Trưng, Hà Nội",
  "city": "Hà Nội",
  "region": "Miền Bắc",
  "phone": "0987654321",
  "email": "kimdong@example.com",
  "website": "http://nxbkimdong.com",
  "status": 1,
  "updatedBy": "admin"
}
```

#### Phản hồi thành công
- Mã trạng thái: 200 OK (không có nội dung trả về cụ thể)

#### Lỗi có thể xảy ra
- Mã trạng thái: 400 Bad Request (dữ liệu không hợp lệ)
- Mã trạng thái: 404 Not Found (không tìm thấy nhà cung cấp cần cập nhật)
- Mã trạng thái: 500 Internal Server Error (lỗi máy chủ nội bộ)

### 4. Xóa nhà cung cấp

#### Yêu cầu
- Phương thức: `DELETE`
- Đường dẫn: `/api/suppliers/{id}`
- Tham số đường dẫn:
  - `id`: ID của nhà cung cấp cần xóa

#### Phản hồi thành công
- Mã trạng thái: 200 OK (không có nội dung trả về cụ thể)

#### Lỗi có thể xảy ra
- Mã trạng thái: 404 Not Found (không tìm thấy nhà cung cấp cần xóa)
- Mã trạng thái: 500 Internal Server Error (lỗi máy chủ nội bộ)

### 5. Cập nhật trạng thái nhà cung cấp

#### Yêu cầu
- Phương thức: `PATCH`
- Đường dẫn: `/api/suppliers/status`
- Tham số truy vấn:
  - `id`: ID của nhà cung cấp cần cập nhật trạng thái
  - `status`: Trạng thái mới (0: Vô hiệu, 1: Kích hoạt)
  - `updatedBy`: Người thực hiện cập nhật

#### Ví dụ URL:
```
/api/suppliers/status?id=1&status=0&updatedBy=admin
```

#### Phản hồi thành công
- Mã trạng thái: 200 OK (không có nội dung trả về cụ thể)

#### Lỗi có thể xảy ra
- Mã trạng thái: 404 Not Found (không tìm thấy nhà cung cấp cần cập nhật)
- Mã trạng thái: 400 Bad Request (dữ liệu không hợp lệ)
- Mã trạng thái: 500 Internal Server Error (lỗi máy chủ nội bộ)

### 6. Lấy danh sách nhà cung cấp cho dropdown

#### Yêu cầu
- Phương thức: `GET`
- Đường dẫn: `/api/suppliers/dropdown`

#### Phản hồi thành công
- Mã trạng thái: 200 OK
- Nội dung phản hồi:
```json
{
  "status": 200,
  "message": "Lấy danh sách nhà cung cấp thành công",
  "data": [
    {
      "id": 1,
      "label": "Công ty Sách Kim Đồng"
    },
    {
      "id": 2,
      "label": "NXB Trẻ"
    },
    {
      "id": 3,
      "label": "Nhã Nam"
    },
    ...
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

## Cấu trúc của đối tượng SupplierRequest

```json
{
  "id": 1, // Bắt buộc khi cập nhật, không bắt buộc khi thêm mới
  "supplierName": "Tên nhà cung cấp", // Bắt buộc
  "contactName": "Tên người liên hệ", // Không bắt buộc
  "contactTitle": "Chức vụ người liên hệ", // Không bắt buộc
  "address": "Địa chỉ", // Không bắt buộc
  "city": "Thành phố", // Không bắt buộc
  "region": "Khu vực", // Không bắt buộc
  "phone": "Số điện thoại", // Không bắt buộc
  "email": "Email", // Không bắt buộc
  "website": "Website", // Không bắt buộc
  "status": 1, // 1: Kích hoạt, 0: Vô hiệu
  "createdBy": "admin", // Bắt buộc khi thêm mới
  "updatedBy": "admin" // Bắt buộc khi cập nhật
}
```

## Lưu ý quan trọng

1. Tất cả các yêu cầu POST và PUT phải có header `Content-Type: application/json`.
2. Trạng thái (status) của nhà cung cấp có giá trị là 1 (kích hoạt) hoặc 0 (vô hiệu).
3. API dropdown chỉ trả về các nhà cung cấp có trạng thái kích hoạt (status = 1).
4. Khi cập nhật thông tin của nhà cung cấp, cần cung cấp đầy đủ tất cả các trường thông tin (ngay cả các trường không thay đổi).
5. Đối với các lỗi không được liệt kê cụ thể, hệ thống có thể trả về mã lỗi 500 với thông báo "Lỗi máy chủ nội bộ".
6. Đảm bảo rằng định dạng email hợp lệ khi gửi dữ liệu, nếu không có thể nhận được lỗi 400 Bad Request.
