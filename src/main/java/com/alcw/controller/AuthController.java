package com.alcw.controller;


import com.alcw.dto.*;
import com.alcw.model.ResendOTPRequest;
import com.alcw.model.User;
import com.alcw.model.PasswordResetRequest;
import com.alcw.service.PasswordResetService;
import com.alcw.util.JwtUtil;
import com.alcw.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final JwtUtil jwtUtil;
    private final PasswordResetService passwordResetService;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody UserDTO userDTO) {
        // Changed: Now returns a message instead of user data
        String message = authService.registerUser(userDTO);
        return ResponseEntity.ok(new MessageResponse(message));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOTP(@Valid @RequestBody OTPRequest otpRequest) {
        User user = authService.verifyOTP(otpRequest.getEmail(), otpRequest.getOtp());
        return ResponseEntity.ok(new AuthResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getOccupation().toString(),
                "OTP verified successfully"
        ));
    }

    @PostMapping("/resend-otp")
    public ResponseEntity<?> resendOTP(@Valid @RequestBody ResendOTPRequest resendOTPRequest) {
        String message = authService.resendOTP(resendOTPRequest.getEmail());
        return ResponseEntity.ok(new MessageResponse(message));
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@Valid @RequestBody LoginRequest loginRequest) {
        User user = authService.loginUser(loginRequest);
        String token = jwtUtil.generateToken(user);
        return ResponseEntity.ok(new AuthResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getOccupation().toString(),
                token,
                user.getMembershipId()
        ));
    }

    @PostMapping("/login-with-patron-id")
    public ResponseEntity<?> loginWithPatronId(@Valid @RequestBody PatronLoginRequest request) {
        User user = authService.loginWithPatronId(request);
        String token = jwtUtil.generateToken(user);
        return ResponseEntity.ok(new AuthResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getOccupation().toString(),
                "Login successful",
                token,
                user.getMembershipId(),
                user.getProfileImageUrl()
        ));
    }

    @PostMapping("/request-password-reset")
    public ResponseEntity<?> requestPasswordReset(@Valid @RequestBody PasswordResetRequest request) {
        passwordResetService.requestReset(request.getEmail());
        return ResponseEntity.ok(Map.of("message", "Password reset email sent"));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody PasswordResetDTO dto) {
        passwordResetService.resetPassword(dto);
        return ResponseEntity.ok(Map.of("message", "Password updated successfully"));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser() {
        // In JWT implementation, logout is handled client-side by discarding the token
        return ResponseEntity.ok(new MessageResponse("Logout successful"));
    }
}
