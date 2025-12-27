package com.alcw.service;




import com.alcw.dto.UpdateProfileDTO;
import com.alcw.dto.UserDTO;
import com.alcw.exception.DuplicateEmailException;
import com.alcw.exception.InvalidCredentialsException;
import com.alcw.model.User;
import com.alcw.repository.UserRepository;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final OTPService otpService;
    private final EmailService emailService;
    private final Cloudinary cloudinary;
    private final SequenceGeneratorService sequenceGeneratorService;

    @Override
    public String registerUser(UserDTO userDTO) {
        if (userRepository.existsByEmail(userDTO.getEmail())) {
            throw new DuplicateEmailException("Email already registered");
        }

        if (!isPasswordValid(userDTO.getPassword())) {
            throw new InvalidCredentialsException(
                    "Password must contain 8+ characters, 1 uppercase, and 1 special character"
            );
        }

        // Don't save user to database yet, just generate and send OTP
        String otp = otpService.generateOTP(userDTO.getEmail());

        // Store user data temporarily in OTP service for verification
        otpService.storeUserData(userDTO.getEmail(), userDTO);

        emailService.sendOTPEmail(userDTO.getEmail(), userDTO.getName(), otp);

        return "OTP has been sent to your email for verification";
    }

    @Override
    public boolean isPasswordValid(String password) {
        String passwordRegex = "^(?=.*[A-Z])(?=.*[!@#$%^&*]).{8,}$";
        return password.matches(passwordRegex);
    }

    @Override
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new InvalidCredentialsException("User not found"));
    }

    @Override
    public User verifyOTP(String email, String otp) {
        // First verify OTP
        otpService.validateOTP(email, otp);

        // If OTP is valid, then get user data and create the user
        UserDTO userDTO = otpService.getUserData(email);

        if (userDTO == null) {
            throw new InvalidCredentialsException("User data not found. Please register again.");
        }

        // Create and save the user
        User user = new User();
        user.setName(userDTO.getName());
        user.setEmail(userDTO.getEmail());
        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        user.setOccupation(userDTO.getOccupation());
        user.setVerified(true);

        // Generate membership ID
        int nextNumber = sequenceGeneratorService.generateSequence("user_sequence");
        String membershipId = String.format("ALCWB%04d", nextNumber);
        user.setMembershipId(membershipId);

        User savedUser = userRepository.save(user);

        // Clear the temporary user data
        otpService.clearUserData(email);

        emailService.sendWelcomeEmail(savedUser);

        return savedUser;
    }

    @Override
    public String resendOTP(String email) {
        // Check if user already exists
        if (userRepository.existsByEmail(email)) {
            throw new DuplicateEmailException("Email already registered and verified");
        }

        // Get stored user data
        UserDTO userDTO = otpService.getUserData(email);
        if (userDTO == null) {
            throw new InvalidCredentialsException("No registration found for this email. Please register first.");
        }

        // Generate and send new OTP
        String otp = otpService.generateOTP(email);
        emailService.sendOTPEmail(email, userDTO.getName(), otp);

        return "New OTP has been sent to your email";
    }

    @Override
    public User loginUser(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new InvalidCredentialsException("Invalid credentials"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new InvalidCredentialsException("Invalid credentials");
        }

        if (!user.isVerified()) {
            throw new InvalidCredentialsException("Account not verified. Please verify your email first.");
        }

        return user;
    }

    @Override
    public User loginWithPatronId(String patronId, String password) {
        User user = userRepository.findByMembershipId(patronId)
                .orElseThrow(() -> new InvalidCredentialsException("Invalid Patron ID"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new InvalidCredentialsException("Invalid password");
        }

        if (!user.isVerified()) {
            throw new InvalidCredentialsException("Account not verified");
        }

        return user;
    }

    @Override
    public User updateProfile(String email, UpdateProfileDTO updateDto, MultipartFile image) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new InvalidCredentialsException("User not found"));

        // Validate password if present - use the existing isPasswordValid method
        if (updateDto.getPassword() != null && !updateDto.getPassword().isEmpty()) {
            if (!isPasswordValid(updateDto.getPassword())) {
                throw new InvalidCredentialsException(
                        "Password must contain 8+ characters, 1 uppercase, and 1 special character");
            }
            user.setPassword(passwordEncoder.encode(updateDto.getPassword()));
        }

        // Update other fields
        if (updateDto.getName() != null && !updateDto.getName().isEmpty()) {
            user.setName(updateDto.getName());
        }

        if (updateDto.getOccupation() != null && !updateDto.getOccupation().isEmpty()) {
            user.setOccupation(User.Occupation.valueOf(updateDto.getOccupation()));
        }

        // Handle image upload
        if (image != null && !image.isEmpty()) {
            try {
                Map<?, ?> uploadResult = cloudinary.uploader().upload(
                        image.getBytes(),
                        ObjectUtils.asMap("folder", "alc_profiles")
                );
                user.setProfileImageUrl((String) uploadResult.get("secure_url"));
            } catch (Exception e) {
                throw new RuntimeException("Failed to upload image", e);
            }
        }

        return userRepository.save(user);
    }
}
