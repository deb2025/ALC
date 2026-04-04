package com.alcw.service;


import com.alcw.dto.AdminLoginRequest;
import com.alcw.dto.AdminLoginResponse;
import com.alcw.exception.InvalidCredentialsException;
import com.alcw.model.Admin;
import com.alcw.repository.AdminRepository;
import com.alcw.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminAuthService {
    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AdminLoginResponse login(AdminLoginRequest request) {
        Admin admin = adminRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid credentials"));

        if (!admin.isEnabled()) {
            throw new InvalidCredentialsException("Admin not enabled. Contact ALC Admin Team");
        }

        if (!passwordEncoder.matches(request.getPassword(), admin.getPassword())) {
            throw new InvalidCredentialsException("Invalid credentials");
        }

        String token = jwtUtil.generateToken(admin);
        return new AdminLoginResponse("Admin Logged in", admin.getUsername(), token);
    }
}
