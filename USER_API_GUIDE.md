# HƯỚNG DẪN SỬ DỤNG API NGƯỜI DÙNG (USER API)

## 1. Tổng quan
Các API người dùng (User) tuân thủ chuẩn RESTful, trả về dữ liệu dạng JSON, sử dụng các mã trạng thái HTTP rõ ràng. Tất cả các trường hợp lỗi đều trả về thông báo chi tiết.

## 2. Danh sách API

### 2.1. Lấy danh sách người dùng (có phân trang, lọc)
- **GET** `/api/users`
- **Query params:**
  - `page` (int, mặc định 0): trang hiện tại
  - `size` (int, mặc định 10): số lượng mỗi trang
  - `full_name`, `email`, `phone_number`, `role_id`, `status` (tùy chọn): lọc theo trường tương ứng
- **Response:**
```json
{
  "status": 200,
  "message": "Thành công",
  "data": {
    "content": [
      {
        "user_id": 1,
        "full_name": "Nguyễn Văn A",
        "email": "a@gmail.com",
        "phone_number": "0123456789",
        "role_id": 2,
        "status": "ACTIVE",
        "created_at": "2024-07-01T10:00:00Z",
        "updated_at": "2024-07-01T10:00:00Z",
        "total_spent": 1000000,
        "total_point": 100
      }
    ],
    "totalElements": 100,
    "totalPages": 10,
    "page": 0,
    "size": 10
  }
}
```

### 2.2. Lấy chi tiết người dùng
- **GET** `/api/users/{id}`
- **Response:**
```json
{
  "status": 200,
  "message": "Thành công",
  "data": {
    "user_id": 1,
    "full_name": "Nguyễn Văn A",
    "email": "a@gmail.com",
    "phone_number": "0123456789",
    "role_id": 2,
    "status": "ACTIVE",
    "created_at": "2024-07-01T10:00:00Z",
    "updated_at": "2024-07-01T10:00:00Z",
    "total_spent": 1000000,
    "total_point": 100
  }
}
```

### 2.3. Tạo mới người dùng
- **POST** `/api/users`
- **Body:**
```json
{
  "full_name": "Nguyễn Văn B",
  "email": "b@gmail.com",
  "phone_number": "0987654321",
  "role_id": 2,
  "status": "ACTIVE",
  "total_spent": 0,
  "total_point": 0
}
```
- **Response:**
```json
{
  "status": 201,
  "message": "Tạo mới thành công",
  "data": { ... }
}
```

### 2.4. Cập nhật người dùng
- **PUT** `/api/users/{id}`
- **Body:** giống như tạo mới
- **Response:**
```json
{
  "status": 200,
  "message": "Cập nhật thành công",
  "data": { ... }
}
```

### 2.5. Xóa người dùng
- **DELETE** `/api/users/{id}`
- **Response:**
- 204 No Content

### 2.6. Đổi trạng thái người dùng (nếu có)
- **PATCH** `/api/users/{id}/toggle-status`
- **Response:**
```json
{
  "status": 200,
  "message": "Cập nhật trạng thái thành công",
  "data": { ... }
}
```

## 3. Quy ước mã lỗi
- 200: Thành công
- 201: Tạo mới thành công
- 204: Xóa thành công
- 400: Dữ liệu không hợp lệ
- 404: Không tìm thấy

## 4. Lưu ý
- Tất cả API trả về chuẩn JSON, có mã trạng thái và thông báo rõ ràng.
- Khi lỗi, trường `data` sẽ là null và có thông báo chi tiết.
- Đối với các trường hợp lọc nâng cao, tham khảo thêm tài liệu backend hoặc liên hệ backend.

---

# Ví dụ sử dụng với frontend (React, Axios)
```js
axios.get('/api/users?page=0&size=10')
  .then(res => console.log(res.data));

axios.post('/api/users', { full_name: 'Nguyễn Văn C', ... })
  .then(res => alert(res.data.message));
```

---
Mọi thắc mắc liên hệ backend để được hỗ trợ chi tiết.
