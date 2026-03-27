package com.alcw.controller;

import com.alcw.dto.*;
import com.alcw.model.Event;
import com.alcw.model.EventRegistration;
import com.alcw.service.EventRegistrationService;
import com.alcw.service.EventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/events")
@RequiredArgsConstructor
public class AdminEventController {

    private final EventService eventService;
    private final EventRegistrationService eventRegistrationService;

    @PostMapping("/upcoming")
    public ResponseEntity<ApiResponse<Event>> createUpcomingEvent(@ModelAttribute UpcomingEventRequest request) {
        Event event = eventService.createUpcomingEvent(request);
        return ResponseEntity.ok(new ApiResponse<>("Upcoming Event created.", mapStatusLabel(event)));
    }

    @PutMapping("/upcoming")
    public ResponseEntity<ApiResponse<Event>> updateUpcomingEvent(
            @RequestParam("event_id") String eventId,
            @ModelAttribute UpcomingEventRequest request
    ) {
        Event event = eventService.updateUpcomingEvent(eventId, request);
        return ResponseEntity.ok(new ApiResponse<>("Upcoming Event updated.", mapStatusLabel(event)));
    }

    @PostMapping("/archive-status")
    public ResponseEntity<ApiResponse<Event>> archiveEventStatus(@RequestParam("event_id") String eventId) {
        Event event = eventService.archiveEventStatus(eventId);
        return ResponseEntity.ok(new ApiResponse<>("Event status changed to archieved.", mapStatusLabel(event)));
    }

    @PutMapping("/archived")
    public ResponseEntity<ApiResponse<Event>> updateArchivedEvent(
            @RequestParam("event_id") String eventId,
            MultipartHttpServletRequest request
    ) {
        Event event = eventService.updateArchivedEvent(eventId, mapArchivedRequest(request));
        return ResponseEntity.ok(new ApiResponse<>("Archieved Event updated.", mapStatusLabel(event)));
    }

    @DeleteMapping
    public ResponseEntity<Map<String, String>> deleteEvent(@RequestParam("event_id") String eventId) {
        Event deleted = eventService.deleteEvent(eventId);
        return ResponseEntity.ok(Map.of(
                "event_id", deleted.getEventId(),
                "message", "Event deleted successfully"
        ));
    }

    @GetMapping({"/search", "/archived/search"})
    public ResponseEntity<ApiResponse<List<Event>>> searchEvents(
            @RequestParam(value = "event_id", required = false) String eventId,
            @RequestParam(value = "status", required = false) String status
    ) {
        List<Event> events = eventService.searchEvents(eventId, status)
                .stream()
                .map(this::mapStatusLabel)
                .toList();
        return ResponseEntity.ok(new ApiResponse<>("All events loaded", events));
    }

    @GetMapping("/participants")
    public ResponseEntity<ApiResponse<List<EventRegistration>>> getParticipantsByEventId(@RequestParam("event_id") String eventId) {
        return ResponseEntity.ok(new ApiResponse<>(
                "Participants loaded successfully.",
                eventRegistrationService.getParticipants(eventId)
        ));
    }

    @PutMapping("/registration-window")
    public ResponseEntity<ApiResponse<RegistrationWindowResponse>> updateRegistrationWindow(
            @RequestParam("event_id") String eventId,
            @Valid @RequestBody RegistrationWindowRequest request
    ) {
        return ResponseEntity.ok(new ApiResponse<>(
                "Registration window updated successfully.",
                eventRegistrationService.updateRegistrationWindow(eventId, request)
        ));
    }

    @PostMapping("/participants/notify")
    public ResponseEntity<ApiResponse<Map<String, String>>> notifyParticipants(
            @RequestParam("event_id") String eventId,
            @Valid @RequestBody EventParticipantsBroadcastRequest request
    ) {
        eventRegistrationService.sendBroadcastToParticipants(eventId, request);
        return ResponseEntity.ok(new ApiResponse<>("Broadcast email queued successfully.", Map.of("event_id", eventId)));
    }

    private ArchivedEventRequest mapArchivedRequest(MultipartHttpServletRequest request) {
        ArchivedEventRequest archivedEventRequest = new ArchivedEventRequest();
        archivedEventRequest.setArchievedEventName(request.getParameter("archievedEventName"));
        archivedEventRequest.setArchievedEventSpeakerName(request.getParameter("archievedEventSpeakerName"));
        archivedEventRequest.setArchievedEventSpeakerDesignation(request.getParameter("archievedEventSpeakerDesignation"));
        archivedEventRequest.setArchievedEventDate(request.getParameter("archievedEventDate"));
        archivedEventRequest.setArcheivedEventDetails(request.getParameter("archeivedEventDetails"));
        archivedEventRequest.setArchievedEventKeyTakeaways(request.getParameter("archievedEventKeyTakeaways"));
        archivedEventRequest.setImage1(request.getFile("image1"));
        archivedEventRequest.setImage2(request.getFile("image2"));
        return archivedEventRequest;
    }

    private Event mapStatusLabel(Event event) {
        event.setStatusLabel(event.isStatus() ? "upcoming" : "archieved");
        return event;
    }
}
