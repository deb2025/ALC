package com.alcw.service;




import brevoApi.TransactionalEmailsApi;
import brevoModel.SendSmtpEmail;
import brevoModel.SendSmtpEmailAttachment;
import brevoModel.SendSmtpEmailSender;
import brevoModel.SendSmtpEmailTo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import brevo.ApiException;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

/**
 * Wrapper around Brevo SDK TransactionalEmailsApi with improved logging/error details.
 */
@Component
public class BrevoEmailClient {

    private static final Logger logger = LoggerFactory.getLogger(BrevoEmailClient.class);

    private final TransactionalEmailsApi transactionalEmailsApi;

    @Value("${brevo.sender.email}")
    private String defaultSenderEmail;

    @Value("${brevo.sender.name}")
    private String defaultSenderName;

    public BrevoEmailClient(TransactionalEmailsApi transactionalEmailsApi) {
        this.transactionalEmailsApi = transactionalEmailsApi;
    }

    public void sendEmail(String toEmail,
                          String subject,
                          String htmlContent,
                          List<Attachment> attachments,
                          Map<String, Object> params) throws ApiException {

        SendSmtpEmail request = new SendSmtpEmail();
        SendSmtpEmailSender sender = new SendSmtpEmailSender();
        sender.setEmail(defaultSenderEmail);
        sender.setName(defaultSenderName);
        request.setSender(sender);

        List<SendSmtpEmailTo> toList = new ArrayList<>();
        SendSmtpEmailTo to = new SendSmtpEmailTo();
        to.setEmail(toEmail);
        toList.add(to);
        request.setTo(toList);

        request.setSubject(subject);
        request.setHtmlContent(htmlContent);

        if (params != null && !params.isEmpty()) {
            request.setParams(params);
        }

        if (attachments != null && !attachments.isEmpty()) {
            List<SendSmtpEmailAttachment> sdkAtts = new ArrayList<>();
            for (Attachment att : attachments) {
                SendSmtpEmailAttachment sdkAtt = new SendSmtpEmailAttachment();
                sdkAtt.setName(att.getName());

                if (att.getContent() != null) {
                    // SDK expects byte[] for content
                    sdkAtt.setContent(att.getContent());
                } else if (att.getBase64String() != null) {
                    byte[] bytes = Base64.getDecoder().decode(att.getBase64String());
                    sdkAtt.setContent(bytes);
                } else if (att.getUrl() != null) {
                    // try reflective setUrl (some SDK versions support it)
                    try {
                        var method = SendSmtpEmailAttachment.class.getMethod("setUrl", String.class);
                        method.invoke(sdkAtt, att.getUrl());
                    } catch (NoSuchMethodException nsme) {
                        // fallback: fetch bytes from public URL and set content
                        try {
                            byte[] bytes = fetchBytesFromUrl(att.getUrl());
                            if (bytes != null) sdkAtt.setContent(bytes);
                        } catch (Exception ex) {
                            logger.warn("Could not fetch attachment from URL {}: {}", att.getUrl(), ex.getMessage());
                        }
                    } catch (Exception e) {
                        logger.warn("Attachment url reflection error: {}", e.getMessage());
                    }
                }

                sdkAtts.add(sdkAtt);
            }
            request.setAttachment(sdkAtts);
        }

        try {
            logger.debug("Sending email to={} subject={} sender={}", toEmail, subject, defaultSenderEmail);
            transactionalEmailsApi.sendTransacEmail(request);
            logger.info("Sent email via Brevo to {}", toEmail);
        } catch (ApiException ex) {
            // Log full ApiException details (status, body, headers) to find root cause quickly
            String responseBody = ex.getResponseBody();
            int code = ex.getCode();
            Map<String, List<String>> headers = ex.getResponseHeaders();
            logger.error("Brevo ApiException (code={}): responseBody={}", code, responseBody);
            logger.debug("Brevo ApiException headers={}", headers);

            // Helpful explanation for common status codes
            String human = switch (code) {
                case 401 -> "Unauthorized - API key is missing/invalid. Check BREVO_API_KEY.";
                case 403 -> "Forbidden - account/permission issue. Contact Brevo support or check account.";
                case 400 -> "Bad request - payload invalid (attachments, emails). Inspect response body for details.";
                case 413 -> "Payload too large - attachments exceed Brevo limits.";
                case 429 -> "Rate limit - too many requests. Implement retry/backoff.";
                default -> "Brevo returned error code " + code;
            };

            // throw a RuntimeException with all details so your global handler sees it
            throw new ApiException(code,
                    "Brevo ApiException: " + human + " | responseBody=" + responseBody,
                    ex.getResponseHeaders(),
                    ex.getResponseBody());
        } catch (Exception e) {
            logger.error("Unexpected error while sending email via Brevo: {}", e.getMessage(), e);
            throw new RuntimeException("Unexpected error while sending email via Brevo: " + e.getMessage(), e);
        }
    }

    private byte[] fetchBytesFromUrl(String fileUrl) {
        InputStream in = null;
        try {
            URL url = new URL(fileUrl);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setConnectTimeout(10_000);
            con.setReadTimeout(20_000);
            con.setRequestMethod("GET");
            con.connect();
            int code = con.getResponseCode();
            if (code >= 200 && code < 300) {
                in = con.getInputStream();
                return in.readAllBytes();
            } else {
                logger.warn("Failed to fetch URL {} status={}", fileUrl, code);
            }
        } catch (Exception e) {
            logger.warn("Error fetching URL {}: {}", fileUrl, e.getMessage());
        } finally {
            if (in != null) try { in.close(); } catch (Exception ignored) {}
        }
        return null;
    }

    // Attachment DTO
    public static class Attachment {
        private final String name;
        private final byte[] content;      // raw bytes preferred
        private final String base64String; // alternative
        private final String url;          // optional public URL

        public Attachment(String name, byte[] content, String base64String, String url) {
            this.name = name;
            this.content = content;
            this.base64String = base64String;
            this.url = url;
        }

        public String getName() { return name; }
        public byte[] getContent() { return content; }
        public String getBase64String() { return base64String; }
        public String getUrl() { return url; }

        public static Attachment fromBytes(String name, byte[] content) {
            return new Attachment(name, content, null, null);
        }
        public static Attachment fromBase64(String name, String base64) {
            return new Attachment(name, null, base64, null);
        }
        public static Attachment fromUrl(String name, String url) {
            return new Attachment(name, null, null, url);
        }
    }
}



