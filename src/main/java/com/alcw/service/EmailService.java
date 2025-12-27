package com.alcw.service;


import com.alcw.model.ContactSubmission;
import com.alcw.model.User;

public interface EmailService {
    void sendOTPEmail(String email, String name, String otp);
    void sendWelcomeEmail(User user);
    void sendUserConfirmation(String email, String name, ContactSubmission.ContactSubject subject, String fileUrl);
    void sendAdminNotification(String adminEmail, String name, String userEmail,
                               ContactSubmission.ContactSubject subject, String message, String fileUrl);
}
