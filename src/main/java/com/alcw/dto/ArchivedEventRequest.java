package com.alcw.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class ArchivedEventRequest {
    private String archievedEventName;
    private String archievedEventSpeakerName;
    private String archievedEventSpeakerDesignation;
    private String archievedEventDate;

    private MultipartFile image1;
    private String archeivedEventDetails;
    private MultipartFile image2;
    private String archievedEventKeyTakeaways;
}
