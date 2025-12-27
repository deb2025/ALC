package com.alcw.service;



import com.alcw.dto.UpdateProfileDTO;
import com.alcw.dto.UserDTO;
import com.alcw.model.User;
import org.springframework.web.multipart.MultipartFile;

public interface UserService {
    String registerUser(UserDTO userDTO);
    User verifyOTP(String email, String otp);
    User loginUser(String email, String password);
    User loginWithPatronId(String patronId, String password);
    String resendOTP(String email);
    boolean isPasswordValid(String password);
    User getUserByEmail(String email); // Add this method
    User updateProfile(String email, UpdateProfileDTO updateDto, MultipartFile image);
}

