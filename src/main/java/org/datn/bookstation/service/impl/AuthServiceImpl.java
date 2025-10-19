package org.datn.bookstation.service.impl;

import org.datn.bookstation.dto.request.RegisterRequest;
import org.datn.bookstation.dto.response.ApiResponse;
import org.datn.bookstation.dto.response.RegisterResponse;
import org.datn.bookstation.entity.User;
import org.datn.bookstation.entity.enums.RoleName;
import org.datn.bookstation.mapper.AuthMapper;
import org.datn.bookstation.repository.RoleRepository;
import org.datn.bookstation.repository.UserRepository;
import org.datn.bookstation.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;
import java.util.Optional;
import org.datn.bookstation.dto.request.LoginRequest;
import org.datn.bookstation.dto.request.ForgotPasswordRequest;
import org.datn.bookstation.dto.response.LoginResponse;
import org.datn.bookstation.configuration.JwtUtil;
import org.datn.bookstation.dto.request.ResetPasswordRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import jakarta.servlet.http.HttpServletRequest;
import org.datn.bookstation.dto.response.TokenValidationResponse;

@Service
@AllArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthMapper authMapper;
    private final JwtUtil jwtUtil;


    @Override
    public ApiResponse<RegisterResponse> register(RegisterRequest request) {
        // Check email đã tồn tại chưa
        Optional<User> existUserOpt = userRepository.findByEmail(request.getEmail());
        if (existUserOpt.isPresent()) {
            User user = existUserOpt.get();
            if(user.getEmailVerified() == 0) {
                return new ApiResponse<>(400, "Email đã được đăng ký nhưng chưa được xác nhận. Vui lòng kiểm tra email để xác nhận tài khoản.", null);
            }
            return new ApiResponse<>(400, "Tài khoản với email này đã tồn tại trên hệ thống.", null);
        }

        // Tạo user mới
        User user = authMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(roleRepository.findByRoleName(RoleName.CUSTOMER).orElse(null));
        user.setStatus((byte) 1);
        user.setEmailVerified((byte) 1); // Email được coi như đã xác nhận (không cần xác thực)
        userRepository.save(user);

        // Không cần gửi email xác nhận nữa - tài khoản đã sẵn sàng sử dụng
        return new ApiResponse<>(201, "Đăng ký thành công! Tài khoản đã sẵn sàng sử dụng.", authMapper.toRegisterResponse(user));
    }

    @Override
    public ApiResponse<LoginResponse> login(LoginRequest request) {
        Optional<User> userOpt = userRepository.findByEmail(request.getEmail());
        if (userOpt.isEmpty()) {
            return new ApiResponse<>(401, "Sai email hoặc mật khẩu!", null);
        }
        User user = userOpt.get();
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return new ApiResponse<>(401, "Sai email hoặc mật khẩu!", null);
        }
        
        // Kiểm tra tài khoản có bị vô hiệu hóa không
        if (user.getStatus() != null && user.getStatus() == 0) {
            return new ApiResponse<>(403, "Tài khoản của bạn đã bị vô hiệu hóa. Vui lòng liên hệ admin để được hỗ trợ.", null);
        }
        
        // Không cần kiểm tra email verification nữa - tất cả user đều có thể đăng nhập
        
        String token = jwtUtil.generateToken(user);
        LoginResponse res = new LoginResponse(token, authMapper.toRegisterResponse(user));
        return new ApiResponse<>(200, "Đăng nhập thành công!", res);
    }

    @Override
    public ApiResponse<Void> forgotPassword(ForgotPasswordRequest request) {
        // Kiểm tra email tồn tại
        Optional<User> userOpt = userRepository.findByEmail(request.getEmail());
        if (userOpt.isEmpty()) {
            return new ApiResponse<>(404, "Email không tồn tại", null);
        }

        // Không cần gửi email reset password nữa
        // Chỉ trả về thông báo thành công
        return new ApiResponse<>(200, "Yêu cầu khôi phục mật khẩu đã được ghi nhận. Vui lòng liên hệ admin để được hỗ trợ.", null);
    }

    @Override
    public ApiResponse<Void> resetPassword(ResetPasswordRequest request) {
        // Validate token & type
        if (!jwtUtil.validateToken(request.getToken()) || !jwtUtil.isResetToken(request.getToken())) {
            return new ApiResponse<>(400, "Token không hợp lệ hoặc đã hết hạn, vui lòng thử lại!", null);
        }

        Integer userId = jwtUtil.extractUserId(request.getToken());
        if (userId == null) {
            return new ApiResponse<>(400, "Token không chứa thông tin user", null);
        }

        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return new ApiResponse<>(404, "Người dùng không tồn tại", null);
        }

        User user = userOpt.get();
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        return new ApiResponse<>(200, "Khôi phục mật khẩu thành công", null);
    }

    @Override
    public ApiResponse<Void> verifyEmail(String token) {
        // Email verification đã bị vô hiệu hóa - tất cả user đều được coi như đã xác nhận email
        return new ApiResponse<>(200, "Email verification không cần thiết nữa. Tài khoản đã sẵn sàng sử dụng.", null);
    }

    @Override
    public ApiResponse<TokenValidationResponse> validateToken(String token) {
        // 1. Validate JWT token format
        if (!jwtUtil.validateToken(token)) {
            return new ApiResponse<>(401, "Token không hợp lệ", null);
        }

        // 2. Extract userId từ token
        Integer userId = jwtUtil.extractUserId(token);
        if (userId == null) {
            return new ApiResponse<>(401, "Token không chứa thông tin user", null);
        }

        // 3. Query database để lấy thông tin user HIỆN TẠI
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return new ApiResponse<>(404, "Người dùng không tồn tại", null);
        }

        User currentUser = userOpt.get();

        // 4. Kiểm tra status
        if (currentUser.getStatus() != null && currentUser.getStatus() == 0) {
            return new ApiResponse<>(403, "Tài khoản đã bị vô hiệu hóa", null);
        }

        // 5. Kiểm tra email verification (chỉ với user mới)
        if (currentUser.getEmailVerified() != null && currentUser.getEmailVerified() != 1) {
            return new ApiResponse<>(403, "Email chưa được xác nhận", null);
        }

        // 6. So sánh với JWT để phát hiện thay đổi
        try {
            // Sử dụng các method public có sẵn
            String jwtRole = jwtUtil.extractRole(token);
            Byte jwtStatus = jwtUtil.extractStatus(token);
            Byte jwtEmailVerified = jwtUtil.extractEmailVerified(token);
            
            // So sánh role
            if (!currentUser.getRole().getRoleName().name().equals(jwtRole)) {
                return new ApiResponse<>(401, "Vai trò đã thay đổi từ " + jwtRole + " thành " + currentUser.getRole().getRoleName().name() + ", vui lòng đăng nhập lại", null);
            }
            
            // So sánh status
            if (jwtStatus != null && !jwtStatus.equals(currentUser.getStatus())) {
                if (currentUser.getStatus() == 0) {
                    return new ApiResponse<>(401, "Tài khoản đã bị vô hiệu hóa, vui lòng đăng nhập lại", null);
                } else {
                    return new ApiResponse<>(401, "Trạng thái tài khoản đã thay đổi, vui lòng đăng nhập lại", null);
                }
            }
            
            // So sánh emailVerified
            if (jwtEmailVerified != null && !jwtEmailVerified.equals(currentUser.getEmailVerified())) {
                if (currentUser.getEmailVerified() == 1) {
                    return new ApiResponse<>(401, "Email đã được xác nhận, vui lòng đăng nhập lại", null);
                } else {
                    return new ApiResponse<>(401, "Trạng thái xác nhận email đã thay đổi, vui lòng đăng nhập lại", null);
                }
            }
            
        } catch (Exception e) {
            // Nếu không parse được JWT, coi như có thay đổi
            return new ApiResponse<>(401, "Token không hợp lệ, vui lòng đăng nhập lại", null);
        }

        // 7. Tạo response với thông tin mới nhất từ database (không có thay đổi)
        TokenValidationResponse response = new TokenValidationResponse();
        response.setValid(true);
        response.setUserId(currentUser.getId());
        response.setEmail(currentUser.getEmail());
        response.setFullName(currentUser.getFullName());
        response.setRole(currentUser.getRole().getRoleName().name());
        response.setStatus(currentUser.getStatus());
        response.setEmailVerified(currentUser.getEmailVerified());
        response.setPhoneNumber(currentUser.getPhoneNumber());

        return new ApiResponse<>(200, "Token hợp lệ", response);
    }
}
