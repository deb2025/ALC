package com.alcw.service;


import com.alcw.dto.LoginRequest;
import com.alcw.dto.PatronLoginRequest;
import com.alcw.dto.UserDTO;
import com.alcw.model.User;

public interface AuthService {
    String registerUser(UserDTO userDTO); // Changed return type to String
    User verifyOTP(String email, String otp);
    User loginUser(LoginRequest loginRequest);
    User loginWithPatronId(PatronLoginRequest request);
    String resendOTP(String email); // New method
}
