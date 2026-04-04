package com.alcw.dto;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class UpcomingEventRequest {
    private MultipartFile image;
    private String typeOfEvent;
    private String eventName;
    private Boolean hostedBy;
    private Boolean eventType;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate eventDate;

    @DateTimeFormat(pattern = "HH:mm")
    private LocalTime eventStartTime;

    @DateTimeFormat(pattern = "HH:mm")
    private LocalTime eventEndTime;

    private String eventOverview;
    private String eventSpeakerOverview;
}
