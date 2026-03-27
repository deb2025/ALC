package com.alcw.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RegistrationWindowRequest {

    @NotNull(message = "registration_open_from is required")
    @JsonProperty("registration_open_from")
    private LocalDateTime registrationOpenFrom;

    @NotNull(message = "registration_open_until is required")
    @JsonProperty("registration_open_until")
    private LocalDateTime registrationOpenUntil;
}

