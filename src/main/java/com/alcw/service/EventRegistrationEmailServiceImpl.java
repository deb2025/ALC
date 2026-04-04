package com.alcw.service;

import brevo.ApiException;
import com.alcw.model.Event;
import com.alcw.model.EventRegistration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventRegistrationEmailServiceImpl implements EventRegistrationEmailService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("hh:mm a");

    private final BrevoEmailClient brevoEmailClient;
    private final TemplateEngine templateEngine;

    @Override
    public void sendRegistrationConfirmation(Event event, EventRegistration registration) {
        Context context = buildContext(event, registration, null);
        String html = templateEngine.process("event-registration-confirmation", context);
        sendToParticipant(registration.getEmailId(), "Registration Confirmed | " + event.getEventName(), html);
    }

    @Override
    public void sendBroadcastEmail(Event event, List<EventRegistration> registrations, String subject, String content) {
        for (EventRegistration registration : registrations) {
            Context context = buildContext(event, registration, content);
            String html = templateEngine.process("event-participant-broadcast", context);
            sendToParticipant(registration.getEmailId(), subject, html);
        }
    }

    @Override
    public void sendReminderEmail(Event event, List<EventRegistration> registrations) {
        for (EventRegistration registration : registrations) {
            Context context = buildContext(event, registration, null);
            String html = templateEngine.process("event-reminder", context);
            sendToParticipant(registration.getEmailId(), "Reminder: " + event.getEventName() + " in 2 days", html);
        }
    }

    private Context buildContext(Event event, EventRegistration registration, String content) {
        Context context = new Context();
        context.setVariable("name", registration.getName());
        context.setVariable("eventName", event.getEventName());
        context.setVariable("eventDate", event.getEventDate() == null ? "TBA" : event.getEventDate().format(DATE_FORMATTER));
        context.setVariable("eventStartTime", event.getEventStartTime() == null ? "TBA" : event.getEventStartTime().format(TIME_FORMATTER));
        context.setVariable("eventImage", event.getImage());
        context.setVariable("eventOverview", event.getEventOverview());
        context.setVariable("content", content);
        return context;
    }

    private void sendToParticipant(String email, String subject, String html) {
        try {
            brevoEmailClient.sendEmail(email, subject, html, Collections.emptyList(), null);
        } catch (ApiException ex) {
            log.error("Failed to send event mail to {}: {}", email, ex.getResponseBody(), ex);
        }
    }
}
