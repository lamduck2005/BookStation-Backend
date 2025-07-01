# USER API USAGE GUIDE

## 1. Overview
User APIs follow RESTful standards, return data in JSON format, and use clear HTTP status codes. All error cases return detailed messages.

## 2. API List

### 2.1. Get user list (with pagination and filtering)
- **GET** `/api/users`
- **Query params:**
  - `page` (int, default 0): current page
  - `size` (int, default 10): items per page
  - `full_name`, `email`, `phone_number`, `role_id`, `status` (optional): filter by corresponding fields
- **Response:**
```json
{
  "status": 200,
  "message": "Success",
  "data": {
    "content": [
      {
        "user_id": 1,
        "full_name": "Nguyen Van A",
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

### 2.2. Get user details
- **GET** `/api/users/{id}`
- **Response:**
```json
{
  "status": 200,
  "message": "Success",
  "data": {
    "user_id": 1,
    "full_name": "Nguyen Van A",
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

### 2.3. Create new user
- **POST** `/api/users`
- **Body:**
```json
{
  "full_name": "Nguyen Van B",
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
  "message": "Created successfully",
  "data": { ... }
}
```

### 2.4. Update user
- **PUT** `/api/users/{id}`
- **Body:** same as create
- **Response:**
```json
{
  "status": 200,
  "message": "Updated successfully",
  "data": { ... }
}
```

### 2.5. Delete user
- **DELETE** `/api/users/{id}`
- **Response:**
- 204 No Content

### 2.6. Toggle user status (if any)
- **PATCH** `/api/users/{id}/toggle-status`
- **Response:**
```json
{
  "status": 200,
  "message": "Status updated successfully",
  "data": { ... }
}
```

## 3. Error code conventions
- 200: Success
- 201: Created successfully
- 204: Deleted successfully
- 400: Invalid data
- 404: Not found

## 4. Notes
- All APIs return standard JSON with clear status codes and messages.
- On error, the `data` field will be null and a detailed message is provided.
- For advanced filtering, refer to backend documentation or contact backend team.

---

# Example usage with frontend (React, Axios)
```js
axios.get('/api/users?page=0&size=10')
  .then(res => console.log(res.data));

axios.post('/api/users', { full_name: 'Nguyen Van C', ... })
  .then(res => alert(res.data.message));
```

---
For any questions, contact the backend team for detailed support.
