package com.alcw.dto;


import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class EventParticipantsBroadcastRequest {

    @NotBlank(message = "subject is required")
    private String subject;

    @NotBlank(message = "content is required")
    private String content;
}
