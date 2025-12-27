package com.alcw.service;

import com.alcw.dto.PasswordResetDTO;
import com.alcw.exception.InvalidCredentialsException;
import com.alcw.model.User;
import com.alcw.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import brevo.ApiException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private static final Logger logger = LoggerFactory.getLogger(PasswordResetService.class);

    private final UserRepository userRepository;
    private final BrevoEmailClient brevoClient;
    private final PasswordEncoder passwordEncoder;

    // If you want to override sender for this service specifically you can inject props here:

    public void requestReset(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new InvalidCredentialsException("User not found"));

        String token = UUID.randomUUID().toString();
        user.setResetToken(token);
        user.setResetTokenExpiry(LocalDateTime.now().plusHours(1));
        userRepository.save(user);

        sendResetEmail(user, token);
    }

    public void resetPassword(PasswordResetDTO dto) {
        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            throw new InvalidCredentialsException("Passwords do not match");
        }

        User user = userRepository.findByResetToken(dto.getToken())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid token"));

        if (user.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new InvalidCredentialsException("Token has expired");
        }

        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        user.setResetToken(null);
        user.setResetTokenExpiry(null);
        userRepository.save(user);
    }

    private void sendResetEmail(User user, String token) {
        // Build reset link (keep your chosen URL)
        String resetLink = "https://deb2025.github.io/password_reset_page/?token=" + token;

        String emailContent =
                "<!DOCTYPE html>" +
                        "<html>" +
                        "<head>" +
                        "<meta charset=\"UTF-8\">" +
                        "<title>Password Reset</title>" +
                        "<style>" +
                        "body { margin:0; padding:0; background-color:#f4f6f8; font-family:Arial, sans-serif; }" +
                        ".container { max-width:600px; margin:30px auto; background:#fff; border-radius:8px; box-shadow:0 4px 12px rgba(0,0,0,0.1); overflow:hidden; }" +
                        ".header { background:#2c3e50; text-align:center; padding:20px; }" +
                        ".header img { max-width:120px; }" +
                        ".content { padding:30px 20px; color:#333; }" +
                        ".content h3 { margin-top:0; color:#2c3e50; font-size:22px; }" +
                        ".content p { font-size:15px; line-height:1.6; margin-bottom:18px; }" +
                        ".btn { display:inline-block; padding:12px 24px; background:#ADD8E6; color:#e74c3c; text-decoration:none; border-radius:4px; font-weight:bold; }" +
                        ".footer { background:#ecf0f1; text-align:center; padding:15px; font-size:13px; color:#7f8c8d; }" +
                        "</style>" +
                        "</head>" +
                        "<body>" +
                        "<div class=\"container\">" +
                        "<div class=\"header\">" +
                        "<img src=\"https://ik.imagekit.io/zqaaupwx1/alc-logo.jpg?updatedAt=1757949096793\" alt=\"ALC Logo\" />" +
                        "</div>" +
                        "<div class=\"content\">" +
                        "<h3>[Art Law Communion] Password Reset</h3>" +
                        "<p>There has been a request for a password reset for:</p>" +
                        "<p><strong>Site Name:</strong> Art Law Communion</p>" +
                        "<p><strong>Username:</strong> " + user.getEmail() + "</p>" +
                        "<p>If you did not request a password reset, you can safely ignore this email.</p>" +
                        "<p style=\"text-align:center; margin:30px 0;\">" +
                        "<a class=\"btn\" href=\"" + resetLink + "\">Reset Your Password</a>" +
                        "</p>" +
                        "</div>" +
                        "<div class=\"footer\">" +
                        "© 2025 Art Law Communion • <a href=\"https://artlawcommunion.org\" style=\"color:#7f8c8d; text-decoration:none;\">Visit our website</a>" +
                        "</div>" +
                        "</div>" +
                        "</body>" +
                        "</html>";

        try {
            // Send using BrevoEmailClient (no attachments)
            brevoClient.sendEmail(
                    user.getEmail(),
                    "[Art Law Communion] Password Reset",
                    emailContent,
                    Collections.emptyList(),
                    null
            );
            logger.info("Password reset email queued/sent via Brevo to {}", user.getEmail());
        } catch (ApiException apiEx) {
            // Log Brevo response details for debugging
            logger.error("Brevo ApiException while sending password reset to {}: code={}, responseBody={}",
                    user.getEmail(), apiEx.getCode(), apiEx.getResponseBody(), apiEx);
            // Optionally wrap into a domain exception to show friendly message downstream
            throw new RuntimeException("Failed to send reset email: " + safeMessage(apiEx), apiEx);
        } catch (Exception e) {
            logger.error("Unexpected error while sending password reset to {}: {}", user.getEmail(), e.getMessage(), e);
            throw new RuntimeException("Failed to send reset email", e);
        }
    }

    private String safeMessage(ApiException ex) {
        if (ex == null) return "Unknown Brevo error";
        return "Brevo error code=" + ex.getCode() + " body=" + ex.getResponseBody();
    }
}

