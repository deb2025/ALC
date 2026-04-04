package com.alcw.service;

import com.alcw.dto.EventParticipantsBroadcastRequest;
import com.alcw.dto.EventRegistrationRequest;
import com.alcw.dto.RegistrationWindowRequest;
import com.alcw.dto.RegistrationWindowResponse;
import com.alcw.model.EventRegistration;

import java.util.List;

public interface EventRegistrationService {
    EventRegistration registerForUpcomingEvent(String eventId, EventRegistrationRequest request);
    List<EventRegistration> getParticipants(String eventId);
    RegistrationWindowResponse updateRegistrationWindow(String eventId, RegistrationWindowRequest request);
    void sendBroadcastToParticipants(String eventId, EventParticipantsBroadcastRequest request);
    void sendScheduledReminders();
}
