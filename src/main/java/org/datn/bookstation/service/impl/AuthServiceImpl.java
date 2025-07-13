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
import org.datn.bookstation.util.EmailUtil;
import org.datn.bookstation.dto.request.ResetPasswordRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import jakarta.servlet.http.HttpServletRequest;

@Service
@AllArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthMapper authMapper;
    private final JwtUtil jwtUtil;
    private final EmailUtil emailUtil;


    @Override
    public ApiResponse<RegisterResponse> register(RegisterRequest request) {
        // Check email đã tồn tại chưa
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            return new ApiResponse<>(400, "Email đã tồn tại!", null);
        }

        // Tạo user mới
        User user = authMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(roleRepository.findByRoleName(RoleName.CUSTOMER).orElse(null));
        user.setStatus((byte) 1);
        userRepository.save(user);

        return new ApiResponse<>(201, "Đăng ký thành công!", authMapper.toRegisterResponse(user));
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

        User user = userOpt.get();
        // Sinh reset token
        String resetToken = jwtUtil.generateResetToken(user);

        // Lấy origin (URL FE) từ header request (Origin hoặc Referer)
        HttpServletRequest servletRequest = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String origin = servletRequest.getHeader("Origin");
        if (origin == null || origin.isEmpty()) {
            origin = servletRequest.getHeader("Referer");
        }
        if (origin == null || origin.isEmpty()) {
            origin = "http://localhost:5173"; // fallback
        }
        // loại bỏ dấu "/" cuối nếu có
        if (origin.endsWith("/")) {
            origin = origin.substring(0, origin.length() - 1);
        }

        String resetLink = origin + "/reset-password?token=" + resetToken;

        String html = "<p>Xin chào " + user.getFullName() + ",</p>"
                + "<p>Bạn vừa yêu cầu khôi phục mật khẩu. Nhấn vào link bên dưới để đặt lại mật khẩu (hiệu lực 15 phút):</p>"
                + "<p><a href='" + resetLink + "'>Khôi phục mật khẩu</a></p>"
                + "<br/><p>Nếu bạn không yêu cầu, hãy bỏ qua email này.</p>";
        try {
            emailUtil.sendHtmlEmail(user.getEmail(), "Khôi phục mật khẩu", html);
        } catch (Exception e) {
            return new ApiResponse<>(500, "Lỗi gửi email: " + e.getMessage(), null);
        }

        return new ApiResponse<>(200, "Đã gửi link khôi phục mật khẩu tới email của bạn", null);
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
}
