package com.alcw.service;

import com.alcw.dto.EventParticipantsBroadcastRequest;
import com.alcw.dto.EventRegistrationRequest;
import com.alcw.dto.RegistrationWindowRequest;
import com.alcw.dto.RegistrationWindowResponse;
import com.alcw.model.Event;
import com.alcw.model.EventRegistration;
import com.alcw.repository.EventRegistrationRepository;
import com.alcw.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventRegistrationServiceImpl implements EventRegistrationService {

    private final EventRepository eventRepository;
    private final EventRegistrationRepository eventRegistrationRepository;
    private final EventRegistrationEmailService eventRegistrationEmailService;

    @Override
    public EventRegistration registerForUpcomingEvent(String eventId, EventRegistrationRequest request) {
        Event event = getUpcomingEventOrThrow(eventId);
        validateRegistrationWindow(event);
        validateRegistrationRequest(request);

        eventRegistrationRepository.findByEventIdAndEmailId(eventId, request.getEmailId().trim().toLowerCase(Locale.ROOT))
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("This email is already registered for the event.");
                });

        EventRegistration registration = new EventRegistration();
        registration.setEventId(event.getEventId());
        registration.setEventName(event.getEventName());
        registration.setName(request.getName().trim());
        registration.setEmailId(request.getEmailId().trim().toLowerCase(Locale.ROOT));
        registration.setContactNumber(request.getContactNumber().trim());
        registration.setOccupation(request.getOccupation().trim());
        registration.setInstitute(request.getInstitute().trim());
        registration.setReason(request.getReason());
        registration.setOtherReason(StringUtils.hasText(request.getOtherReason()) ? request.getOtherReason().trim() : null);

        EventRegistration saved = eventRegistrationRepository.save(registration);
        eventRegistrationEmailService.sendRegistrationConfirmation(event, saved);
        return saved;
    }

    @Override
    public List<EventRegistration> getParticipants(String eventId) {
        Event event = getEventOrThrow(eventId);
        return eventRegistrationRepository.findByEventIdOrderByCreatedAtDesc(event.getEventId());
    }

    @Override
    public RegistrationWindowResponse updateRegistrationWindow(String eventId, RegistrationWindowRequest request) {
        Event event = getUpcomingEventOrThrow(eventId);

        if (request.getRegistrationOpenUntil().isBefore(request.getRegistrationOpenFrom())) {
            throw new IllegalArgumentException("registration_open_until must be after registration_open_from");
        }

        event.setRegistrationOpenFrom(request.getRegistrationOpenFrom());
        event.setRegistrationOpenUntil(request.getRegistrationOpenUntil());
        event.setUpdatedAt(LocalDateTime.now());
        Event saved = eventRepository.save(event);

        return toWindowResponse(saved);
    }

    @Override
    public void sendBroadcastToParticipants(String eventId, EventParticipantsBroadcastRequest request) {
        Event event = getEventOrThrow(eventId);
        List<EventRegistration> participants = eventRegistrationRepository.findByEventIdOrderByCreatedAtDesc(event.getEventId());
        if (participants.isEmpty()) {
            throw new IllegalArgumentException("No participants registered for this event.");
        }
        eventRegistrationEmailService.sendBroadcastEmail(event, participants, request.getSubject().trim(), request.getContent().trim());
    }

    @Override
    @Scheduled(cron = "${app.events.reminder.cron:0 0 9 * * *}")
    public void sendScheduledReminders() {
        LocalDate targetDate = LocalDate.now().plusDays(2);
        List<Event> upcomingEvents = eventRepository.findByStatus(true)
                .stream()
                .filter(event -> targetDate.equals(event.getEventDate()))
                .toList();

        for (Event event : upcomingEvents) {
            List<EventRegistration> pending = eventRegistrationRepository.findByEventIdAndReminderSentFalse(event.getEventId());
            if (pending.isEmpty()) {
                continue;
            }

            eventRegistrationEmailService.sendReminderEmail(event, pending);
            pending.forEach(reg -> reg.setReminderSent(true));
            eventRegistrationRepository.saveAll(pending);
            log.info("Sent reminder emails for event {} to {} participants", event.getEventId(), pending.size());
        }
    }

    private Event getEventOrThrow(String eventId) {
        return eventRepository.findByEventId(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found for event_id: " + eventId));
    }

    private Event getUpcomingEventOrThrow(String eventId) {
        Event event = getEventOrThrow(eventId);
        if (!event.isStatus()) {
            throw new IllegalArgumentException("Registration is allowed only for upcoming events.");
        }
        return event;
    }

    private void validateRegistrationWindow(Event event) {
        LocalDateTime now = LocalDateTime.now();
        if (event.getRegistrationOpenFrom() != null && now.isBefore(event.getRegistrationOpenFrom())) {
            throw new IllegalArgumentException("Registration has not opened yet.");
        }
        if (event.getRegistrationOpenUntil() != null && now.isAfter(event.getRegistrationOpenUntil())) {
            throw new IllegalArgumentException("Registration is closed for this event.");
        }
    }

    private void validateRegistrationRequest(EventRegistrationRequest request) {
        if (request.getReason() == EventRegistration.RegistrationReason.other && !StringUtils.hasText(request.getOtherReason())) {
            throw new IllegalArgumentException("other_reason is required when reason is other");
        }
        if (request.getReason() != EventRegistration.RegistrationReason.other && StringUtils.hasText(request.getOtherReason())) {
            throw new IllegalArgumentException("other_reason must be empty unless reason is other");
        }

        String number = request.getContactNumber().trim();
        if (!number.matches("^[+]?[-() 0-9]{7,20}$")) {
            throw new IllegalArgumentException("contact_number must be a valid international phone number");
        }
    }

    private RegistrationWindowResponse toWindowResponse(Event event) {
        long remainingSeconds = -1;
        if (event.getRegistrationOpenUntil() != null) {
            remainingSeconds = Math.max(0, Duration.between(LocalDateTime.now(), event.getRegistrationOpenUntil()).getSeconds());
        }
        return new RegistrationWindowResponse(
                event.getEventId(),
                event.getRegistrationOpenFrom(),
                event.getRegistrationOpenUntil(),
                remainingSeconds
        );
    }
}
