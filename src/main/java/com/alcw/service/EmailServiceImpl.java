package com.alcw.service;

import com.alcw.model.ContactSubmission;
import com.alcw.model.User;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import brevo.ApiException;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.HashMap;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {
    private static final Logger logger = LoggerFactory.getLogger(EmailServiceImpl.class);

    private final BrevoEmailClient brevoClient;
    private final TemplateEngine templateEngine;

    // Brevo sender configured in properties (brevo.sender.email)
    // @Value("${brevo.sender.email}") private String fromEmail; // not needed here

    @Override
    public void sendOTPEmail(String email, String name, String otp) {
        Context context = new Context();
        context.setVariable("name", name);
        context.setVariable("otp", otp);
        String htmlContent = templateEngine.process("otp-email", context);

        try {
            brevoClient.sendEmail(email, "Email Verification", htmlContent, Collections.emptyList(), null);
            logger.info("OTP email sent to {}", email);
        } catch (ApiException apiEx) {
            logger.error("Brevo ApiException while sending OTP to {}: code={}, body={}",
                    email, apiEx.getCode(), apiEx.getResponseBody(), apiEx);
            throw new RuntimeException("Failed to send OTP email via Brevo: " + safe(apiEx), apiEx);
        } catch (Exception e) {
            logger.error("Unexpected error sending OTP to {}: {}", email, e.getMessage(), e);
            throw new RuntimeException("Failed to send OTP email via Brevo", e);
        }
    }

    @Override
    public void sendWelcomeEmail(User user) {
        Context context = new Context();
        context.setVariable("name", user.getName());
        context.setVariable("membershipId", user.getMembershipId());
        String htmlContent = templateEngine.process("welcome-email", context);

        List<BrevoEmailClient.Attachment> attachments = new ArrayList<>();
        try (InputStream is = getClass().getResourceAsStream("/static/docs/ALC_Brochure.pdf")) {
            if (is != null) {
                byte[] bytes = is.readAllBytes();
                attachments.add(BrevoEmailClient.Attachment.fromBytes("ALC_Welcome_Brochure.pdf", bytes));
            }
        } catch (Exception ex) {
            logger.warn("Could not load brochure from classpath: {}", ex.getMessage());
        }

        try {
            brevoClient.sendEmail(user.getEmail(), "Welcome to the Art Law Communion", htmlContent, attachments, null);
            logger.info("Welcome email sent to {}", user.getEmail());
        } catch (ApiException apiEx) {
            logger.error("Brevo ApiException while sending welcome to {}: code={}, body={}",
                    user.getEmail(), apiEx.getCode(), apiEx.getResponseBody(), apiEx);
            throw new RuntimeException("Failed to send welcome email via Brevo: " + safe(apiEx), apiEx);
        } catch (Exception e) {
            logger.error("Unexpected error sending welcome to {}: {}", user.getEmail(), e.getMessage(), e);
            throw new RuntimeException("Failed to send welcome email via Brevo", e);
        }
    }

    @Override
    public void sendUserConfirmation(String email, String name,
                                     ContactSubmission.ContactSubject subject, String fileUrl) {
        Context context = new Context();
        context.setVariable("name", name);
        context.setVariable("subject", subject.name());
        String htmlContent = templateEngine.process("contact-user-email", context);

        List<BrevoEmailClient.Attachment> attachments = new ArrayList<>();
        if (fileUrl != null && !fileUrl.isBlank()) {
            BrevoEmailClient.Attachment att = createAttachmentFromUrlWithProperName(fileUrl);
            if (att != null) attachments.add(att);
            else logger.warn("Skipping user confirmation attachment because it couldn't be created from URL: {}", fileUrl);
        }

        try {
            brevoClient.sendEmail(email, "Thank you for your " + subject.name().toLowerCase(), htmlContent, attachments, null);
            logger.info("User confirmation sent to {}", email);
        } catch (ApiException apiEx) {
            logger.error("Brevo ApiException while sending user confirmation to {}: code={}, body={}",
                    email, apiEx.getCode(), apiEx.getResponseBody(), apiEx);
            throw new RuntimeException("Failed to send confirmation email via Brevo: code=" + apiEx.getCode() +
                    " body=" + apiEx.getResponseBody(), apiEx);
        } catch (Exception e) {
            logger.error("Unexpected error sending user confirmation to {}: {}", email, e.getMessage(), e);
            throw new RuntimeException("Failed to send confirmation email via Brevo", e);
        }
    }

    @Override
    public void sendAdminNotification(String adminEmail, String name, String userEmail,
                                      ContactSubmission.ContactSubject subject, String messageContent, String fileUrl) {
        if (adminEmail == null || adminEmail.isBlank()) {
            logger.error("Admin email is not configured; cannot send admin notification.");
            throw new IllegalArgumentException("Admin email not configured");
        }

        Context context = new Context();
        context.setVariable("name", name);
        context.setVariable("email", userEmail);
        context.setVariable("subject", subject.name());
        context.setVariable("message", messageContent);
        context.setVariable("fileUrl", fileUrl);
        String htmlContent = templateEngine.process("contact-admin-email", context);

        List<BrevoEmailClient.Attachment> attachments = new ArrayList<>();
        if (fileUrl != null && !fileUrl.isBlank()) {
            BrevoEmailClient.Attachment att = createAttachmentFromUrlWithProperName(fileUrl);
            if (att != null) attachments.add(att);
            else logger.warn("Skipping admin attachment because it couldn't be created from URL: {}", fileUrl);
        }

        try {
            brevoClient.sendEmail(adminEmail, "New " + subject.name().toLowerCase() + " from " + name, htmlContent, attachments, null);
            logger.info("Admin notification sent to {}", adminEmail);
        } catch (ApiException apiEx) {
            logger.error("Brevo ApiException while sending admin notification to {}: code={}, body={}",
                    adminEmail, apiEx.getCode(), apiEx.getResponseBody(), apiEx);
            throw new RuntimeException("Failed to send admin notification via Brevo: code=" + apiEx.getCode() +
                    " body=" + apiEx.getResponseBody(), apiEx);
        } catch (Exception e) {
            logger.error("Unexpected error sending admin notification to {}: {}", adminEmail, e.getMessage(), e);
            throw new RuntimeException("Failed to send admin notification via Brevo", e);
        }
    }


    private String safe(ApiException ex) {
        if (ex == null) return "unknown Brevo error";
        return "code=" + ex.getCode() + " body=" + ex.getResponseBody();
    }

    private BrevoEmailClient.Attachment createAttachmentFromUrlWithProperName(String fileUrl) {
        if (fileUrl == null || fileUrl.isBlank()) return null;

        try {
            // 1) Try to extract filename from URL path (strip query params)
            String filename = deriveFilenameFromUrl(fileUrl);
            if (filename != null && filename.contains(".")) {
                // good - use URL-based attachment with matching extension
                return BrevoEmailClient.Attachment.fromUrl(filename, fileUrl);
            }

            // 2) No extension found in URL — attempt to detect from Content-Type header
            String contentType = fetchContentType(fileUrl);
            String ext = detectExtensionFromContentType(contentType);
            if (ext != null) {
                String name = (filename != null ? filename : "attachment") + "." + ext;
                return BrevoEmailClient.Attachment.fromUrl(name, fileUrl);
            }

            // 3) Fallback: fetch bytes and attach as bytes (choose default extension if none)
            byte[] bytes = fetchBytesFromUrl(fileUrl);
            String fallbackExt = (ext != null ? ext : "bin");
            String fallbackName = (filename != null ? filename : "attachment") + "." + fallbackExt;
            return BrevoEmailClient.Attachment.fromBytes(fallbackName, bytes);

        } catch (Exception e) {
            // As ultimate fallback, fetch bytes and attach them with .bin
            try {
                byte[] bytes = fetchBytesFromUrl(fileUrl);
                return BrevoEmailClient.Attachment.fromBytes("attachment.bin", bytes);
            } catch (Exception ex) {
                // give up - return null so caller can skip attachment
                logger.warn("Failed to create attachment from URL {}: {}", fileUrl, ex.getMessage());
                return null;
            }
        }
    }

    private String deriveFilenameFromUrl(String rawUrl) {
        try {
            // Remove query and fragment
            URL url = new URL(rawUrl);
            String path = url.getPath(); // already decoded by URL
            if (path == null || path.isEmpty()) return null;
            String lastSegment = path.substring(path.lastIndexOf('/') + 1);
            // decode percent-encoding
            lastSegment = URLDecoder.decode(lastSegment, StandardCharsets.UTF_8);
            if (lastSegment.isBlank()) return null;
            return lastSegment;
        } catch (Exception e) {
            logger.debug("deriveFilenameFromUrl failed for {}: {}", rawUrl, e.getMessage());
            return null;
        }
    }

    private String fetchContentType(String rawUrl) {
        HttpURLConnection con = null;
        try {
            URL url = new URL(rawUrl);
            con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("HEAD");
            con.setConnectTimeout(10_000);
            con.setReadTimeout(10_000);
            con.connect();
            String contentType = con.getContentType();
            if (contentType != null) {
                // e.g., "application/pdf; charset=UTF-8" -> take first part
                int idx = contentType.indexOf(';');
                if (idx > 0) contentType = contentType.substring(0, idx).trim();
                return contentType;
            }
        } catch (Exception e) {
            logger.debug("fetchContentType failed for {}: {}", rawUrl, e.getMessage());
        } finally {
            if (con != null) try { con.disconnect(); } catch (Exception ignored) {}
        }
        return null;
    }

    private byte[] fetchBytesFromUrl(String fileUrl) throws Exception {
        try (InputStream in = new URL(fileUrl).openStream()) {
            return in.readAllBytes();
        }
    }

    // map common content-types to extensions
    private static final Map<String,String> COMMON_CT_TO_EXT = new HashMap<>();
    static {
        COMMON_CT_TO_EXT.put("application/pdf","pdf");
        COMMON_CT_TO_EXT.put("image/jpeg","jpg");
        COMMON_CT_TO_EXT.put("image/jpg","jpg");
        COMMON_CT_TO_EXT.put("image/png","png");
        COMMON_CT_TO_EXT.put("image/gif","gif");
        COMMON_CT_TO_EXT.put("application/zip","zip");
        COMMON_CT_TO_EXT.put("text/plain","txt");
        // add more if you need
    }

    private String detectExtensionFromContentType(String contentType) {
        if (contentType == null) return null;
        return COMMON_CT_TO_EXT.get(contentType.toLowerCase());
    }
}



