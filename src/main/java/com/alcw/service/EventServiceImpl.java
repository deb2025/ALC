package com.alcw.service;

import com.alcw.dto.ArchivedEventRequest;
import com.alcw.dto.UpcomingEventRequest;
import com.alcw.model.Event;
import com.alcw.repository.EventRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

    private static final List<String> ALLOWED_IMAGE_EXTENSIONS = List.of("jpg", "jpeg", "png");

    private final EventRepository eventRepository;
    private final SequenceGeneratorService sequenceGeneratorService;
    private final CloudinaryService cloudinaryService;
    private final ObjectMapper objectMapper;

    @Override
    public Event createUpcomingEvent(UpcomingEventRequest request) {
        validateUpcomingRequest(request, true);

        Event event = new Event();
        event.setEventId("ALCWBEID" + String.format("%04d", sequenceGeneratorService.generateSequence(Event.SEQUENCE_NAME)));
        event.setStatus(true);
        mapUpcomingRequest(event, request);
        return eventRepository.save(event);
    }

    @Override
    public Event updateUpcomingEvent(String eventId, UpcomingEventRequest request) {
        Event event = getRequiredEvent(eventId);
        if (!event.isStatus()) {
            throw new IllegalArgumentException("Event is archieved, use archived edit API.");
        }
        validateUpcomingRequest(request, false);
        mapUpcomingRequest(event, request);
        event.setUpdatedAt(LocalDateTime.now());
        return eventRepository.save(event);
    }

    @Override
    public Event archiveEventStatus(String eventId) {
        Event event = getRequiredEvent(eventId);
        if (!event.isStatus()) {
            throw new IllegalArgumentException("Event is already archieved.");
        }
        event.setStatus(false);
        event.setUpdatedAt(LocalDateTime.now());
        return eventRepository.save(event);
    }

    @Override
    public Event updateArchivedEvent(String eventId, ArchivedEventRequest request) {
        Event event = getRequiredEvent(eventId);
        if (event.isStatus()) {
            throw new IllegalArgumentException("Event is upcoming. Change status to archieved first.");
        }

        validateArchivedRequest(request, false);
        mapArchivedRequest(event, request);
        event.setUpdatedAt(LocalDateTime.now());
        return eventRepository.save(event);
    }

    @Override
    public Event deleteEvent(String eventId) {
        Event event = getRequiredEvent(eventId);
        eventRepository.delete(event);
        return event;
    }

    @Override
    public Event getEventByEventId(String eventId) {
        return getRequiredEvent(eventId);
    }

    @Override
    public List<Event> getEventsByStatus(String status) {
        boolean parsedStatus = parseStatus(status);
        return eventRepository.findByStatus(parsedStatus);
    }

    @Override
    public List<Event> searchEvents(String eventId, String status) {
        Boolean parsedStatus = null;
        if (StringUtils.hasText(status)) {
            parsedStatus = parseStatus(status);
        }

        if (StringUtils.hasText(eventId) && parsedStatus != null) {
            Event event = getRequiredEvent(eventId);
            if (event.isStatus() == parsedStatus) {
                return List.of(event);
            }
            return List.of();
        }

        if (StringUtils.hasText(eventId)) {
            return List.of(getRequiredEvent(eventId));
        }

        if (parsedStatus != null) {
            return eventRepository.findByStatus(parsedStatus);
        }

        return eventRepository.findAll();
    }

    private Event getRequiredEvent(String eventId) {
        return eventRepository.findByEventId(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found with event_id: " + eventId));
    }

    private void mapUpcomingRequest(Event event, UpcomingEventRequest request) {
        if (request.getImage() != null && !request.getImage().isEmpty()) {
            validateImageFile(request.getImage(), "image");
            event.setImage(cloudinaryService.uploadFile(request.getImage()));
        }
        if (request.getTypeOfEvent() != null) event.setTypeOfEvent(request.getTypeOfEvent());
        if (request.getEventName() != null) event.setEventName(request.getEventName());
        if (request.getHostedBy() != null) event.setHostedBy(request.getHostedBy());
        if (request.getEventType() != null) event.setEventType(request.getEventType());
        if (request.getEventDate() != null) event.setEventDate(request.getEventDate());
        if (request.getEventStartTime() != null) event.setEventStartTime(request.getEventStartTime());
        if (request.getEventEndTime() != null) event.setEventEndTime(request.getEventEndTime());
        if (request.getEventOverview() != null) event.setEventOverview(request.getEventOverview());
        if (request.getEventSpeakerOverview() != null) event.setEventSpeakerOverview(request.getEventSpeakerOverview());
    }

    private void mapArchivedRequest(Event event, ArchivedEventRequest request) {
        if (request.getImage1() != null && !request.getImage1().isEmpty()) {
            validateImageFile(request.getImage1(), "image1");
            event.setImage1(cloudinaryService.uploadFile(request.getImage1()));
        }
        if (request.getImage2() != null && !request.getImage2().isEmpty()) {
            validateImageFile(request.getImage2(), "image2");
            event.setImage2(cloudinaryService.uploadFile(request.getImage2()));
        }
        if (request.getArchievedEventName() != null) event.setArchievedEventName(cleanValue(request.getArchievedEventName()));
        if (request.getArchievedEventSpeakerName() != null) event.setArchievedEventSpeakerName(cleanValue(request.getArchievedEventSpeakerName()));
        if (request.getArchievedEventSpeakerDesignation() != null) event.setArchievedEventSpeakerDesignation(cleanValue(request.getArchievedEventSpeakerDesignation()));
        if (StringUtils.hasText(request.getArchievedEventDate())) {
            try {
                event.setArchievedEventDate(LocalDate.parse(cleanValue(request.getArchievedEventDate())));
            } catch (DateTimeParseException ex) {
                throw new IllegalArgumentException("archieved_event_date must be in yyyy-MM-dd format.");
            }
        }

        if (StringUtils.hasText(request.getArcheivedEventDetails())) {
            try {
                event.setArcheivedEventDetails(objectMapper.readValue(cleanValue(request.getArcheivedEventDetails()), new TypeReference<>() {}));
            } catch (Exception ex) {
                throw new IllegalArgumentException("archeived_event_details must be valid JSON object.");
            }
        }

        if (StringUtils.hasText(request.getArchievedEventKeyTakeaways())) {
            try {
                event.setArchievedEventKeyTakeaways(objectMapper.readValue(cleanValue(request.getArchievedEventKeyTakeaways()), new TypeReference<>() {}));
            } catch (Exception ex) {
                throw new IllegalArgumentException("archieved_event_key_takeaways must be valid JSON array of strings.");
            }
        }
    }

    private void validateUpcomingRequest(UpcomingEventRequest request, boolean isCreate) {
        if (isCreate && (request.getImage() == null || request.getImage().isEmpty())) {
            throw new IllegalArgumentException("image is required.");
        }
        if (isCreate && !StringUtils.hasText(request.getTypeOfEvent())) {
            throw new IllegalArgumentException("type_of_event is required.");
        }
        if (isCreate && !StringUtils.hasText(request.getEventName())) {
            throw new IllegalArgumentException("event_name is required.");
        }
        if (isCreate && request.getHostedBy() == null) {
            throw new IllegalArgumentException("hosted_by is required.");
        }
        if (isCreate && request.getEventType() == null) {
            throw new IllegalArgumentException("event_type is required.");
        }
        if (isCreate && request.getEventDate() == null) {
            throw new IllegalArgumentException("event_date is required.");
        }
        if (isCreate && request.getEventStartTime() == null) {
            throw new IllegalArgumentException("event_start_time is required.");
        }
        if (isCreate && request.getEventEndTime() == null) {
            throw new IllegalArgumentException("event_end_time is required.");
        }
        if (isCreate && !StringUtils.hasText(request.getEventOverview())) {
            throw new IllegalArgumentException("event_overview is required.");
        }
        if (isCreate && !StringUtils.hasText(request.getEventSpeakerOverview())) {
            throw new IllegalArgumentException("event_speaker_overview is required.");
        }
    }

    private void validateArchivedRequest(ArchivedEventRequest request, boolean isCreate) {
        if (isCreate && !StringUtils.hasText(request.getArchievedEventName())) {
            throw new IllegalArgumentException("archieved_event_name is required.");
        }
        if (isCreate && !StringUtils.hasText(request.getArchievedEventSpeakerName())) {
            throw new IllegalArgumentException("archieved_event_speaker_name is required.");
        }
        if (isCreate && !StringUtils.hasText(request.getArchievedEventSpeakerDesignation())) {
            throw new IllegalArgumentException("archieved_event_speaker_designation is required.");
        }
        if (isCreate && !StringUtils.hasText(request.getArchievedEventDate())) {
            throw new IllegalArgumentException("archieved_event_date is required.");
        }
        if (isCreate && (request.getImage1() == null || request.getImage1().isEmpty())) {
            throw new IllegalArgumentException("image_1 is required.");
        }
        if (isCreate && !StringUtils.hasText(request.getArcheivedEventDetails())) {
            throw new IllegalArgumentException("archeived_event_details is required.");
        }
        if (isCreate && !StringUtils.hasText(request.getArchievedEventKeyTakeaways())) {
            throw new IllegalArgumentException("archieved_event_key_takeaways is required.");
        }
    }

    private void validateImageFile(MultipartFile file, String fieldName) {
        String originalFilename = file.getOriginalFilename();
        if (!StringUtils.hasText(originalFilename) || !originalFilename.contains(".")) {
            throw new IllegalArgumentException(fieldName + " must be a valid image file (jpg, jpeg, png).");
        }

        String extension = originalFilename.substring(originalFilename.lastIndexOf('.') + 1)
                .toLowerCase(Locale.ROOT);

        if (!ALLOWED_IMAGE_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException(fieldName + " must be jpg, jpeg, or png only.");
        }
    }

    private boolean parseStatus(String status) {
        String normalized = cleanValue(status).toLowerCase(Locale.ROOT);
        if ("upcoming".equals(normalized)) {
            return true;
        }
        if ("archieved".equals(normalized) || "archived".equals(normalized)) {
            return false;
        }
        throw new IllegalArgumentException("status must be either upcoming or archieved.");
    }

    private String cleanValue(String value) {
        if (value == null) {
            return null;
        }
        String cleaned = value.trim();
        if (cleaned.length() >= 2 && cleaned.startsWith("\"") && cleaned.endsWith("\"")) {
            cleaned = cleaned.substring(1, cleaned.length() - 1);
        }
        return cleaned;
    }
}
