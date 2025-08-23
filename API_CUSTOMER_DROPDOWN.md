# API Dropdown Khách Hàng - Chi Tiết

## Endpoint
```
GET /api/users/dropdown
```

## Mô tả
API này được sử dụng để lấy danh sách khách hàng (role CUSTOMER) dạng dropdown với thông tin id, tên và email. API hỗ trợ tìm kiếm theo tên hoặc email của khách hàng.

## Query Parameters
| Parameter | Type | Required | Mô tả |
|-----------|------|----------|-------|
| search | String | No | Từ khóa tìm kiếm (có thể là tên hoặc email khách hàng). Nếu không truyền hoặc rỗng thì trả về tất cả khách hàng active |

## Request Examples

### 1. Lấy tất cả khách hàng
```http
GET /api/users/dropdown
```

### 2. Tìm kiếm theo tên
```http
GET /api/users/dropdown?search=Nguyễn Văn A
```

### 3. Tìm kiếm theo email
```http
GET /api/users/dropdown?search=nguyenvana@gmail.com
```

### 4. Tìm kiếm với từ khóa một phần
```http
GET /api/users/dropdown?search=nguyen
```

## Response Format

### Success Response (200 OK)
```json
{
  "status": 200,
  "message": "Lấy danh sách khách hàng thành công",
  "data": [
    {
      "id": 1,
      "name": "Nguyễn Văn A",
      "email": "nguyenvana@gmail.com"
    },
    {
      "id": 2,
      "name": "Trần Thị B",
      "email": "tranthib@gmail.com"
    }
  ]
}
```

### Empty Response (Không tìm thấy kết quả)
```json
{
  "status": 200,
  "message": "Lấy danh sách khách hàng thành công",
  "data": []
}
```

## Quy tắc lọc dữ liệu

1. **Role Filter**: Chỉ trả về user có role là CUSTOMER
2. **Status Filter**: Chỉ trả về user có status = 1 (Active)
3. **Search Logic**: 
   - Nếu không truyền `search` hoặc truyền giá trị rỗng → trả về tất cả khách hàng active
   - Nếu truyền `search` → tìm kiếm trong cả `fullName` và `email` (case insensitive)
   - Sử dụng phương thức `contains()` để tìm kiếm một phần

## Ví dụ sử dụng trong Frontend

### JavaScript/Axios
```javascript
// Lấy tất cả khách hàng
const getAllCustomers = async () => {
  try {
    const response = await axios.get('/api/users/dropdown');
    console.log(response.data.data); // Array of customers
  } catch (error) {
    console.error('Error:', error);
  }
};

// Tìm kiếm khách hàng
const searchCustomers = async (searchTerm) => {
  try {
    const response = await axios.get(`/api/users/dropdown?search=${searchTerm}`);
    console.log(response.data.data); // Filtered customers
  } catch (error) {
    console.error('Error:', error);
  }
};
```

### React Hook
```jsx
import { useState, useEffect } from 'react';

const useCustomerDropdown = () => {
  const [customers, setCustomers] = useState([]);
  const [loading, setLoading] = useState(false);

  const fetchCustomers = async (search = '') => {
    setLoading(true);
    try {
      const response = await fetch(`/api/users/dropdown${search ? `?search=${search}` : ''}`);
      const result = await response.json();
      setCustomers(result.data);
    } catch (error) {
      console.error('Error fetching customers:', error);
    } finally {
      setLoading(false);
    }
  };

  return { customers, loading, fetchCustomers };
};
```

## Database Schema Tham Khảo

Bảng `user`:
```sql
CREATE TABLE user (
  id INT PRIMARY KEY AUTO_INCREMENT,
  email VARCHAR(100) NOT NULL,
  password VARCHAR(255) NOT NULL,
  role_id INT NOT NULL,
  full_name VARCHAR(100) NOT NULL,
  phone_number VARCHAR(20),
  status TINYINT DEFAULT 1, -- 1: Active, 0: Inactive
  created_at BIGINT,
  updated_at BIGINT,
  FOREIGN KEY (role_id) REFERENCES role(id)
);
```

Bảng `role`:
```sql
CREATE TABLE role (
  id INT PRIMARY KEY AUTO_INCREMENT,
  role_name ENUM('ADMIN', 'STAFF', 'CUSTOMER') NOT NULL,
  description TEXT,
  status TINYINT DEFAULT 1
);
```

## Notes
- API này được thiết kế đặc biệt cho dropdown/select component trong frontend
- Chỉ trả về khách hàng đang hoạt động (status = 1)
- Tìm kiếm không phân biệt hoa thường
- Response luôn trả về mảng, ngay cả khi không có kết quả (mảng rỗng)
