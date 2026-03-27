package com.alcw.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class RegistrationWindowResponse {
    private String eventId;
    private LocalDateTime registrationOpenFrom;
    private LocalDateTime registrationOpenUntil;
    private long remainingSeconds;
}
