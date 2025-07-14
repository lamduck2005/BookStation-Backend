package org.datn.bookstation.controller;

import lombok.AllArgsConstructor;
import org.datn.bookstation.dto.request.UserRequest;
import org.datn.bookstation.dto.response.ApiResponse;
import org.datn.bookstation.dto.response.PaginationResponse;
import org.datn.bookstation.dto.response.UserResponse;
import org.datn.bookstation.entity.User;
import org.datn.bookstation.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Optional;
import java.util.List;
import java.util.stream.Collectors;
import org.datn.bookstation.dto.response.DropdownOptionResponse;

@RestController
@AllArgsConstructor
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    // Lấy danh sách user (phân trang, lọc)
    @GetMapping
    public ResponseEntity<ApiResponse<PaginationResponse<UserResponse>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String full_name,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String phone_number,
            @RequestParam(required = false) Integer role_id,
            @RequestParam(required = false) String status
    ) {
        PaginationResponse<UserResponse> users = userService.getAllWithPagination(
                page, size, full_name, email, phone_number, role_id, status
        );
        ApiResponse<PaginationResponse<UserResponse>> response = new ApiResponse<>(HttpStatus.OK.value(), "Thành công", users);
        return ResponseEntity.ok(response);
    }

    /**
     * API lấy danh sách user dạng dropdown (id, name) cho frontend làm khoá ngoại
     */
    @GetMapping("/dropdown")
    public ResponseEntity<ApiResponse<List<DropdownOptionResponse>>> getDropdownUsers() {
        List<DropdownOptionResponse> dropdown = userService.getActiveUsers().stream()
            .map(user -> new DropdownOptionResponse(user.getId(), user.getFullName()))
            .collect(Collectors.toList());
        ApiResponse<List<DropdownOptionResponse>> response = new ApiResponse<>(HttpStatus.OK.value(), "Lấy danh sách user thành công", dropdown);
        return ResponseEntity.ok(response);
    }

    // Lấy chi tiết user
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> getById(@PathVariable Integer id) {
        Optional<UserResponse> user = userService.getUserResponseById(id);
        if (user.isPresent()) {
            return ResponseEntity.ok(new ApiResponse<>(200, "Thành công", user.get()));
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>(404, "Không tìm thấy", null));
    }

    // Tạo mới user
    @PostMapping
    public ResponseEntity<ApiResponse<UserResponse>> add(@RequestBody UserRequest userRequest) {
        ApiResponse<UserResponse> response = userService.add(userRequest);
        if (response.getStatus() == 400) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(201, "Tạo mới thành công", response.getData()));
    }

    // Cập nhật user
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> update(@PathVariable Integer id, @RequestBody UserRequest userRequest) {
        ApiResponse<UserResponse> response = userService.update(userRequest, id);
        if (response.getStatus() == 404) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        if (response.getStatus() == 400) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
        return ResponseEntity.ok(new ApiResponse<>(200, "Cập nhật thành công", response.getData()));
    }

    // Xóa user
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        userService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // Đổi trạng thái user (nếu có)
    @PatchMapping("/{id}/toggle-status")
    public ResponseEntity<ApiResponse<UserResponse>> toggleStatus(@PathVariable Integer id) {
        ApiResponse<UserResponse> response = userService.toggleStatus(id);
        if (response.getStatus() == 404) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        return ResponseEntity.ok(new ApiResponse<>(200, "Cập nhật trạng thái thành công", response.getData()));
    }
}
