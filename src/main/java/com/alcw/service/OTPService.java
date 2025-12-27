package com.alcw.service;


import com.alcw.dto.UserDTO;

public interface OTPService {
    String generateOTP(String email);
    void validateOTP(String email, String otp);
    void clearOTP(String email);
    void storeUserData(String email, UserDTO userDTO); // New method
    UserDTO getUserData(String email); // New method
    void clearUserData(String email); // New method
}
