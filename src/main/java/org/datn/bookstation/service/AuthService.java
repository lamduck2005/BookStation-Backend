package org.datn.bookstation.service;

import org.datn.bookstation.dto.request.LoginRequest;
import org.datn.bookstation.dto.request.RegisterRequest;
import org.datn.bookstation.dto.response.ApiResponse;
import org.datn.bookstation.dto.response.LoginResponse;
import org.datn.bookstation.dto.response.RegisterResponse;

public interface AuthService {
    ApiResponse<RegisterResponse> register(RegisterRequest request);
    ApiResponse<LoginResponse> login(LoginRequest request);
}
