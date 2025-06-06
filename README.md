
# BookStation Backend

---

## 📌 Mục lục
- [1. Giới thiệu](#1-giới-thiệu)
- [2. Công nghệ sử dụng](#2-công-nghệ-sử-dụng)
- [3. Cấu trúc dự án](#3-cấu-trúc-dự-án)
- [4. Hướng dẫn chạy](#4-hướng-dẫn-chạy)
- [5. Quy tắc đặt tên (Naming Convention)](#5-quy-tắc-đặt-tên-naming-convention)
- [6. Thông tin khác](#6-thông-tin-khác)

---

## 1. Giới thiệu
<!-- Để trống -->

## 2. Công nghệ sử dụng
<!-- Để trống -->

## 3. Cấu trúc dự án
<!-- Để trống -->

## 4. Hướng dẫn chạy
<!-- Để trống -->

## 5. Quy tắc đặt tên (Naming Convention)

### 5.1 Package
- Tất cả chữ thường, không dấu, không gạch dưới, không viết hoa.
- Nếu nhiều từ, viết liền hoặc tách bằng dấu chấm cho rõ nghĩa.
- **Ví dụ:**
  - `com.example.attendance`
  - `com.example.attendance.user.staff`
  - `com.example.attendance.dto.request`

### 5.2 Class & File
- Viết hoa chữ cái đầu mỗi từ (PascalCase/UpperCamelCase).
- Tên file trùng tên class.
- **Ví dụ:**
  - `UserController.java`
  - `AttendanceService.java`
  - `UserCreateRequest.java`

### 5.3 Interface
- Giống class, thường kết thúc bằng "able", "er", "Service", "Repository",...
- **Ví dụ:**
  - `UserRepository`
  - `AttendanceService`

### 5.4 Biến (Variable)
- camelCase, chữ thường, từ thứ 2 viết hoa chữ cái đầu.
- **Ví dụ:**
  - `userName`
  - `attendanceList`

### 5.5 Hằng số (Constant)
- Chữ in hoa, các từ cách nhau bằng dấu gạch dưới (_).
- **Ví dụ:**
  - `MAX_ATTENDANCE`
  - `DEFAULT_ROLE`

### 5.6 Method (Hàm)
- camelCase, động từ đứng đầu, rõ nghĩa.
- **Ví dụ:**
  - `getUserById()`
  - `calculateAttendanceRate()`

### 5.7 DTO (Data Transfer Object)
- Kết thúc bằng `Request` hoặc `Response`.
- Nếu nhiều loại, chia package con: `dto.request`, `dto.response`.
- **Ví dụ:**
  - `UserLoginRequest`
  - `UserLoginResponse`
  - `AttendanceSummaryResponse`

### 5.8 Đặt tên package nhiều từ
- Viết liền: `userstaff`
- Hoặc tách rõ nghĩa: `user.staff`
- **Không dùng:** `user_staff`, `UserStaff`, `userStaff`, `user-staff`, `user staff`

### 5.9 Lưu ý chung
- Không dùng tiếng Việt, không viết tắt khó hiểu.
- Tên phải rõ ràng, mô tả đúng chức năng.
- Không dùng ký tự đặc biệt, trừ dấu gạch dưới cho hằng số.

#### Ví dụ tổng hợp

com.example.attendance.dto.request.UserCreateRequest
com.example.attendance.dto.response.UserResponse
com.example.attendance.controller.UserController
com.example.attendance.service.AttendanceService
com.example.attendance.entity.User

---

## 6. Thông tin khác
<!-- Để trống -->
