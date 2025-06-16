package com.alcw.service;


import com.alcw.exception.InvalidCredentialsException;
import com.alcw.model.OTP;
import com.alcw.repository.OTPRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class OTPServiceImpl implements OTPService {
    private final OTPRepository otpRepository;
    private static final int OTP_LENGTH = 6;
    private static final int OTP_EXPIRY_MINUTES = 5;

    @Override
    public String generateOTP(String email) {
        // Clear any existing OTP
        otpRepository.deleteByEmail(email);

        // Generate new OTP
        String otp = generateRandomOTP();
        OTP otpEntity = new OTP();
        otpEntity.setEmail(email);
        otpEntity.setCode(otp);
        otpEntity.setExpiryTime(LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES));

        otpRepository.save(otpEntity);
        return otp;
    }

    @Override
    public void validateOTP(String email, String otp) {
        OTP otpEntity = otpRepository.findByEmail(email)
                .orElseThrow(() -> new InvalidCredentialsException("OTP not found or expired"));

        if (!otpEntity.getCode().equals(otp)) {
            throw new InvalidCredentialsException("Invalid OTP");
        }

        if (otpEntity.getExpiryTime().isBefore(LocalDateTime.now())) {
            otpRepository.delete(otpEntity);
            throw new InvalidCredentialsException("OTP expired");
        }

        // Clear OTP after successful validation
        otpRepository.delete(otpEntity);
    }

    @Override
    public void clearOTP(String email) {
        otpRepository.deleteByEmail(email);
    }

    private String generateRandomOTP() {
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < OTP_LENGTH; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }
}
