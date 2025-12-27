package com.alcw.service;


import com.alcw.dto.LoginRequest;
import com.alcw.dto.PatronLoginRequest;
import com.alcw.dto.UserDTO;
import com.alcw.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserService userService;

    @Override
    public String registerUser(UserDTO userDTO) {
        return userService.registerUser(userDTO); // Now returns message
    }

    @Override
    public User verifyOTP(String email, String otp) {
        return userService.verifyOTP(email, otp);
    }

    @Override
    public User loginUser(LoginRequest loginRequest) {
        return userService.loginUser(loginRequest.getEmail(), loginRequest.getPassword());
    }

    @Override
    public User loginWithPatronId(PatronLoginRequest request) {
        return userService.loginWithPatronId(request.getPatronId(), request.getPassword());
    }

    @Override
    public String resendOTP(String email) {
        return userService.resendOTP(email);
    }
}
