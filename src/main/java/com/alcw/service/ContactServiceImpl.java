package com.alcw.service;
 // adjust to your package

import com.alcw.dto.ContactRequestDTO;
import com.alcw.dto.ContactResponseDTO;
import com.alcw.model.ContactSubmission;
import com.alcw.repository.ContactRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ContactServiceImpl implements ContactService {

    private static final Logger logger = LoggerFactory.getLogger(ContactServiceImpl.class);

    private final CloudinaryService cloudinaryService;
    private final EmailService emailService; // should be Brevo-backed EmailService
    private final GoogleSheetsService googleSheetsService;
    private final ContactRepository contactRepository;

    // admin recipient (not the Brevo sender). Configure in application.properties.
    @Value("${app.admin.email}")
    private String adminEmail;

    @Override
    public ContactResponseDTO processContactRequest(ContactRequestDTO request) {
        // CAPTCHA verification removed

        // Upload file if exists
        String fileUrl = null;
        if (request.getBlogFile() != null && !request.getBlogFile().isEmpty()) {
            try {
                fileUrl = cloudinaryService.uploadFile(request.getBlogFile());
            } catch (Exception ex) {
                logger.warn("Cloudinary upload failed: {}", ex.getMessage(), ex);
                // proceed without file URL; or rethrow if you want to fail the request
            }
        }

        // Save to database
        ContactSubmission submission = new ContactSubmission();
        submission.setName(request.getName());
        submission.setEmail(request.getEmail());
        submission.setSubject(request.getSubject());
        submission.setMessage(request.getMessage());
        submission.setFileUrl(fileUrl);
        contactRepository.save(submission);

        // Write to Google Sheets (wrap in try/catch so failures don't stop flow)
        try {
            googleSheetsService.writeToSheet(request.getName(), request.getEmail(),
                    request.getSubject().name(), request.getMessage(), fileUrl);
        } catch (Exception ex) {
            logger.warn("Failed to write contact to Google Sheets: {}", ex.getMessage(), ex);
        }

        // Prepare response
        ContactResponseDTO response = new ContactResponseDTO();
        response.setName(request.getName());
        response.setEmail(request.getEmail());
        response.setSubject(request.getSubject().name());
        response.setMessage(request.getMessage());
        response.setFileUrl(fileUrl);

        switch (request.getSubject()) {
            case BLOG_SUBMISSION:
            case COLLABORATION:
                // Send user confirmation and admin notification. Catch exceptions from email layer so contact flow succeeds.
                try {
                    emailService.sendUserConfirmation(request.getEmail(), request.getName(),
                            request.getSubject(), fileUrl);
                } catch (Exception ex) {
                    logger.error("Failed to send user confirmation email to {}: {}", request.getEmail(), ex.getMessage(), ex);
                    // keep going; inform user that submission was received but confirmation failed
                    response.setStatus("Thank you! We've received your submission. (Warning: confirmation email could not be sent)");
                }

                try {
                    emailService.sendAdminNotification(adminEmail, request.getName(),
                            request.getEmail(), request.getSubject(), request.getMessage(), fileUrl);
                } catch (Exception ex) {
                    logger.error("Failed to send admin notification to {}: {}", adminEmail, ex.getMessage(), ex);
                    // do not expose internal errors to end user; only log
                    if (response.getStatus() == null) {
                        response.setStatus("Thank you! We've received your submission. (Admin notification failed)");
                    }
                }

                // If no explicit status was set by catch blocks, set success message
                if (response.getStatus() == null) {
                    response.setStatus("Thank you! We've received your submission and sent a confirmation email.");
                }
                break;

            case REMARKS:
            case OTHERS:
                response.setStatus("Thank you for your feedback!");
                break;

            default:
                response.setStatus("Thank you for contacting us!");
                break;
        }

        return response;
    }
}
