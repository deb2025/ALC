package com.alcw.controller;

import com.alcw.dto.ApiResponse;
import com.alcw.model.Event;
import com.alcw.service.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Event>>> getEventsByStatus(@RequestParam("status") String status) {
        List<Event> events = eventService.getEventsByStatus(status)
                .stream()
                .map(this::mapStatusLabel)
                .toList();
        return ResponseEntity.ok(new ApiResponse<>("Events loaded successfully.", events));
    }

    @GetMapping("/{eventId}")
    public ResponseEntity<ApiResponse<Event>> getEventByEventId(@PathVariable String eventId) {
        Event event = mapStatusLabel(eventService.getEventByEventId(eventId));
        return ResponseEntity.ok(new ApiResponse<>("Event loaded successfully.", event));
    }

    private Event mapStatusLabel(Event event) {
        event.setStatusLabel(event.isStatus() ? "upcoming" : "archieved");
        return event;
    }
}
