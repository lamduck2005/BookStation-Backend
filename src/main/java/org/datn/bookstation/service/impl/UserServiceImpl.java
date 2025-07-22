package org.datn.bookstation.service.impl;

import lombok.AllArgsConstructor;
import org.datn.bookstation.dto.request.UserRequest;
import org.datn.bookstation.dto.request.UserRoleRequest;
import org.datn.bookstation.dto.response.ApiResponse;
import org.datn.bookstation.dto.response.PaginationResponse;
import org.datn.bookstation.dto.response.UserResponse;
import org.datn.bookstation.entity.Role;
import org.datn.bookstation.entity.User;
import org.datn.bookstation.mapper.UserMapper;
import org.datn.bookstation.repository.RoleRepository;
import org.datn.bookstation.repository.UserRepository;
import org.datn.bookstation.service.UserService;
import org.datn.bookstation.specification.UserRankSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.datn.bookstation.entity.User;

@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final RoleRepository roleRepository;
    @Override
    public PaginationResponse<UserResponse> getAllWithPagination(int page, int size, String fullName, String email, String phoneNumber, Integer roleId, String status) {
        Pageable pageable = PageRequest.of(page, size);
        // TODO: Thay bằng query động nếu cần lọc nâng cao
        Page<User> userPage = userRepository.findAll(pageable);
        List<UserResponse> content = userPage.getContent().stream().map(this::toResponse).collect(Collectors.toList());
        return PaginationResponse.<UserResponse>builder()
                .content(content)
                .pageNumber(page)
                .pageSize(size)
                .totalElements(userPage.getTotalElements())
                .totalPages(userPage.getTotalPages())
                .build();
    }

    @Override
    public Optional<UserResponse> getUserResponseById(Integer id) {
        return userRepository.findById(id).map(this::toResponse);
    }

    @Override
    public ApiResponse<UserResponse> add(UserRequest req) {
        // Validate email trùng
        if (userRepository.findByEmail(req.getEmail()).isPresent()) {
            return new ApiResponse<>(400, "Email đã tồn tại", null);
        }
        User user = new User();
        user.setFullName(req.getFull_name());
        user.setEmail(req.getEmail());
        user.setPhoneNumber(req.getPhone_number());
        user.setStatus(parseStatus(req.getStatus()));
        user.setCreatedAt(System.currentTimeMillis());
        user.setUpdatedAt(System.currentTimeMillis());
        user.setTotalSpent(req.getTotal_spent() != null ? req.getTotal_spent() : BigDecimal.ZERO);
        user.setTotalPoint(req.getTotal_point() != null ? req.getTotal_point() : 0);
        // Set role nếu có
        if (req.getRole_id() != null) {
            user.setRole(roleRepository.findById(req.getRole_id()).orElse(null));
        }
        User saved = userRepository.save(user);
        return new ApiResponse<>(201, "Tạo mới thành công", toResponse(saved));
    }

    @Override
    public ApiResponse<UserResponse> update(UserRequest req, Integer id) {
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isEmpty()) {
            return new ApiResponse<>(404, "Không tìm thấy", null);
        }
        User user = userOpt.get();
        // Nếu đổi email, check trùng
        if (req.getEmail() != null && !req.getEmail().equals(user.getEmail()) && userRepository.findByEmail(req.getEmail()).isPresent()) {
            return new ApiResponse<>(400, "Email đã tồn tại", null);
        }
        user.setFullName(req.getFull_name());
        user.setEmail(req.getEmail());
        user.setPhoneNumber(req.getPhone_number());
        user.setStatus(parseStatus(req.getStatus(), user.getStatus()));
        user.setUpdatedAt(System.currentTimeMillis());
        user.setTotalSpent(req.getTotal_spent() != null ? req.getTotal_spent() : user.getTotalSpent());
        user.setTotalPoint(req.getTotal_point() != null ? req.getTotal_point() : user.getTotalPoint());
        // Set role nếu có
        if (req.getRole_id() != null) {
            user.setRole(roleRepository.findById(req.getRole_id()).orElse(user.getRole()));
        }
        User saved = userRepository.save(user);
        return new ApiResponse<>(200, "Cập nhật thành công", toResponse(saved));
    }

    @Override
    public void deleteById(Integer id) {
        userRepository.deleteById(id);
    }

    @Override
    public ApiResponse<UserResponse> toggleStatus(Integer id) {
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isEmpty()) {
            return new ApiResponse<>(404, "Không tìm thấy", null);
        }
        User user = userOpt.get();
        user.setStatus(user.getStatus() != null && user.getStatus() == 1 ? (byte)0 : (byte)1);
        user.setUpdatedAt(System.currentTimeMillis());
        User saved = userRepository.save(user);
        return new ApiResponse<>(200, "Cập nhật trạng thái thành công", toResponse(saved));
    }

    @Override
    public ApiResponse<User> getUserByEmail(String email) {
        return new ApiResponse<>(200, "Cập nhật trạng thái thành công",userRepository.findByEmail(email).get());

    }

    @Override
    public ApiResponse<User> updateClient(User user, Integer id) {
        User userById = userRepository.findById(id).get();
        if (userById==null) {
            return new ApiResponse<>(404, "Không tìm thấy", null);
        }
        userById.setUpdatedAt(System.currentTimeMillis());
        userById.setFullName(user.getFullName());
        userById.setPhoneNumber(user.getPhoneNumber());
        User userUpdate = userRepository.save(userById);
        return new ApiResponse<>(200, "Cập nhật thông tin thành công",userUpdate);
    }

    @Override
    public ApiResponse<List<UserRoleRequest>> getUserPOS(String text) {
        Specification<User> userSpecification = UserRankSpecification.filterBy(text);
        List<User> users = userRepository.findAll( userSpecification);

        return new ApiResponse<>(200,"Lấy danh sách user thành công",userMapper.userMapper(users));
    }

    @Override
    public ApiResponse<User> addRetail(User req) {
        if (userRepository.getByPhoneNumber(req.getPhoneNumber())==null){
            Role role = roleRepository.findById(3).get();
            req.setRole(role);

         User userSave=   userRepository.save(req);
            return new ApiResponse<>(200,"Thêm khách vãng lai thành công",userSave);
        }else {
            return new ApiResponse<>(400,"Thêm khách lẻ thất bại ",null);
        }

    }

    @Override
    public List<User> getActiveUsers() {
        return userRepository.findAll().stream()
                .filter(u -> u.getStatus() != null && u.getStatus() == 1)
                .collect(Collectors.toList());
    }

    // Helper chuyển status String -> Byte
    private Byte parseStatus(String status) {
        if (status == null) return 1;
        if (status.equalsIgnoreCase("ACTIVE") || status.equals("1")) return 1;
        if (status.equalsIgnoreCase("INACTIVE") || status.equals("0")) return 0;
        try {
            return Byte.valueOf(status);
        } catch (Exception e) {
            return 1;
        }
    }
    private Byte parseStatus(String status, Byte defaultStatus) {
        if (status == null) return defaultStatus != null ? defaultStatus : 1;
        return parseStatus(status);
    }

    private UserResponse toResponse(User u) {
        UserResponse res = new UserResponse();
        res.setUser_id(u.getId());
        res.setFull_name(u.getFullName());
        res.setEmail(u.getEmail());
        res.setPhone_number(u.getPhoneNumber());
        res.setRole_id(u.getRole() != null ? u.getRole().getId() : null);
        res.setStatus(u.getStatus() != null && u.getStatus() == 1 ? "ACTIVE" : "INACTIVE");
        res.setCreated_at(formatTime(u.getCreatedAt()));
        res.setUpdated_at(formatTime(u.getUpdatedAt()));
        res.setTotal_spent(u.getTotalSpent() != null ? u.getTotalSpent() : BigDecimal.ZERO);
        res.setTotal_point(u.getTotalPoint() != null ? u.getTotalPoint() : 0);
        return res;
    }

    private String formatTime(Long millis) {
        if (millis == null) return null;
        return Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT);
    }

}
