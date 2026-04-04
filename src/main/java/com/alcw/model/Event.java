package com.alcw.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@Data
@Document(collection = "events")
public class Event {
    public static final String SEQUENCE_NAME = "event_sequence";

    @Id
    private String id;

    private String eventId; // ALCWBEID0001
    private boolean status; // true = upcoming, false = archieved
    private String statusLabel;

    // Upcoming fields
    private String image;
    private String typeOfEvent;
    private String eventName;
    private Boolean hostedBy;
    private Boolean eventType;
    private LocalDate eventDate;
    private LocalTime eventStartTime;
    private LocalTime eventEndTime;
    private String eventOverview;
    private String eventSpeakerOverview;

    private LocalDateTime registrationOpenFrom;
    private LocalDateTime registrationOpenUntil;

    // Archieved fields
    private String archievedEventName;
    private String archievedEventSpeakerName;
    private String archievedEventSpeakerDesignation;
    private LocalDate archievedEventDate;
    private String image1;
    private Map<String, Object> archeivedEventDetails;
    private String image2;
    private List<String> archievedEventKeyTakeaways;

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();
}
