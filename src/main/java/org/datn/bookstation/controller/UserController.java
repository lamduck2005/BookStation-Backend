package org.datn.bookstation.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;

import org.datn.bookstation.dto.request.UserRequest;
import org.datn.bookstation.dto.request.UserRoleRequest;
import org.datn.bookstation.dto.response.ApiResponse;
import org.datn.bookstation.dto.response.PaginationResponse;
import org.datn.bookstation.dto.response.UserResponse;
import org.datn.bookstation.entity.User;
import org.datn.bookstation.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.datn.bookstation.repository.UserRankRepository;

import java.util.List;
import java.util.Optional;
import java.util.List;
import java.util.stream.Collectors;
import org.datn.bookstation.dto.response.DropdownOptionResponse;

import org.datn.bookstation.entity.UserRank;
import org.datn.bookstation.repository.PointRepository;
import org.datn.bookstation.repository.UserRepository;
import org.springframework.data.repository.query.Param;

import com.microsoft.sqlserver.jdbc.spatialdatatypes.Point;

@RestController
@AllArgsConstructor
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    private final PasswordEncoder passwordEncoder;
    @Autowired
    private UserRankRepository userRankRepo;
    @Autowired
    private UserRepository userRepository;

    // Lấy danh sách user (phân trang, lọc)
    @GetMapping
    public ResponseEntity<ApiResponse<PaginationResponse<UserResponse>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String full_name,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String phone_number,
            @RequestParam(required = false) Integer role_id,
            @RequestParam(required = false) String status) {
        PaginationResponse<UserResponse> users = userService.getAllWithPagination(
                page, size, full_name, email, phone_number, role_id, status);
        ApiResponse<PaginationResponse<UserResponse>> response = new ApiResponse<>(HttpStatus.OK.value(), "Thành công",
                users);
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
        ApiResponse<List<DropdownOptionResponse>> response = new ApiResponse<>(HttpStatus.OK.value(),
                "Lấy danh sách user thành công", dropdown);
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

    @GetMapping("/userIdByEmail")
    public ResponseEntity<ApiResponse<Integer>> getUserIdByEmail(@RequestParam String email) {
        return userRepository.findByEmail(email)
                .map(u -> ResponseEntity.ok(new ApiResponse<>(200, "Lấy ID thành công", u.getId())))
                .orElseGet(() -> ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponse<>(404, "Không tìm thấy người dùng", null)));
    }

    // Tạo mới user
    @PostMapping
    public ResponseEntity<ApiResponse<UserResponse>> add(@RequestBody UserRequest userRequest) {
        ApiResponse<UserResponse> response = userService.add(userRequest);
        if (response.getStatus() == 400) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(201, "Tạo mới thành công", response.getData()));
    }

    // Cập nhật user
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> update(@PathVariable Integer id,
            @RequestBody UserRequest userRequest) {
        ApiResponse<UserResponse> response = userService.update(userRequest, id);
        if (response.getStatus() == 404) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        if (response.getStatus() == 400) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
        return ResponseEntity.ok(new ApiResponse<>(200, "Cập nhật thành công", response.getData()));
    }

    @PutMapping("/profile/{id}")
    public ResponseEntity<ApiResponse<User>> update(@PathVariable Integer id, @RequestBody User user) {
        System.out.println(user.toString());
        ApiResponse<User> response = userService.updateClient(user, id);
        if (response.getStatus() == 404) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        if (response.getStatus() == 400) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
        return ResponseEntity.ok(new ApiResponse<>(200, "Cập nhật thông tin thành công", response.getData()));
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

    @GetMapping("/profile/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> setProfile(@PathVariable Integer id) {
        try {
            Optional<UserResponse> userOptional = userService.getUserResponseById(id);

            if (userOptional.isPresent()) {
                UserResponse user = userOptional.get();
                return ResponseEntity.ok(new ApiResponse<>(200, "Lấy thông tin profile thành công", user));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponse<>(404, "Không tìm thấy user với ID: " + id, null));
            }

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(500, "Lỗi khi lấy thông tin profile: " + e.getMessage(), null));
        }
    }

    @GetMapping("/userpos")
    public ResponseEntity<ApiResponse<List<UserRoleRequest>>> getUserPOS(@RequestParam(required = false) String text) {
        System.out.println(" sdsadklasdjasdkasdjaskdfkas" + text);
        return ResponseEntity.ok(userService.getUserPOS(text));
    }

    @PostMapping("/addretail")
    public ResponseEntity<ApiResponse<User>> addRetail(@RequestBody User user) {
        return ResponseEntity.ok(userService.addRetail(user));
    }

    @GetMapping("/userRank")
    public ResponseEntity<ApiResponse<List<UserRank>>> getUserRankByUserId(@RequestParam Integer userID) {
        List<UserRank> userRank = userRankRepo.getByUserId(userID);
        return ResponseEntity.ok(new ApiResponse<>(200, "Lấy thông tin hạng người dùng thành công", userRank));
    }

    @PutMapping("/userPass")
    public ResponseEntity<Boolean> updatePassword(@RequestParam Integer id,
            @RequestParam String passCu,
            @RequestParam String passMoi) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng với id: " + id));

        if (!passwordEncoder.matches(passCu, user.getPassword())) {
            return ResponseEntity.ok(false);
        }

        if (passwordEncoder.matches(passMoi, user.getPassword())) {
            return ResponseEntity.ok(false);
        }

        System.out.println("passMoi = " + passMoi);

        user.setPassword(passwordEncoder.encode(passMoi));
        userRepository.save(user);

        return ResponseEntity.ok(true);
    }

    /**
     * ✅ THÊM MỚI: API tìm kiếm khách hàng cho admin tạo đơn
     * Tìm kiếm theo tên hoặc email
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<UserResponse>>> searchCustomers(
            @RequestParam String search) {
        List<UserResponse> customers = userService.searchCustomers(search);
        ApiResponse<List<UserResponse>> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                "Tìm kiếm khách hàng thành công",
                customers);
        return ResponseEntity.ok(response);
    }
}
