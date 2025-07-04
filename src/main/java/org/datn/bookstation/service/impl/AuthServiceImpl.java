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
import org.datn.bookstation.dto.response.LoginResponse;
import org.datn.bookstation.configuration.JwtUtil;

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

}
