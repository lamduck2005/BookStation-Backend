package org.datn.bookstation.service;

import org.datn.bookstation.dto.request.UserRequest;
import org.datn.bookstation.dto.request.UserRoleRequest;
import org.datn.bookstation.dto.response.ApiResponse;
import org.datn.bookstation.dto.response.PaginationResponse;
import org.datn.bookstation.dto.response.UserResponse;
import org.datn.bookstation.entity.User;

import java.util.List;
import java.util.Optional;

public interface UserService {
    // Chuẩn REST: Phân trang, lọc, trả về PaginationResponse<UserResponse>
    PaginationResponse<UserResponse> getAllWithPagination(int page, int size, String fullName, String email, String phoneNumber, Integer roleId, String status);

    // Trả về UserResponse theo id
    Optional<UserResponse> getUserResponseById(Integer id);

    // Thêm mới user
    ApiResponse<UserResponse> add(UserRequest req);

    // Cập nhật user
    ApiResponse<UserResponse> update(UserRequest req, Integer id);

    void deleteById(Integer id);

    // Đổi trạng thái user (ví dụ: ACTIVE <-> INACTIVE)
    ApiResponse<UserResponse> toggleStatus(Integer id);

    ApiResponse<User> getUserByEmail(String email);

    ApiResponse<User> updateClient(User user, Integer id);

    ApiResponse<List<UserRoleRequest>> getUserPOS(String text);

    ApiResponse<User> addRetail(User req);

}
