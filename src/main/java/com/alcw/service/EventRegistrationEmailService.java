package com.alcw.service;

import com.alcw.model.Event;
import com.alcw.model.EventRegistration;

import java.util.List;

public interface EventRegistrationEmailService {
    void sendRegistrationConfirmation(Event event, EventRegistration registration);
    void sendBroadcastEmail(Event event, List<EventRegistration> registrations, String subject, String content);
    void sendReminderEmail(Event event, List<EventRegistration> registrations);
}
