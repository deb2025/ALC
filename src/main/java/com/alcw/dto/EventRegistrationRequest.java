package com.alcw.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.alcw.model.EventRegistration;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class EventRegistrationRequest {

    @NotBlank(message = "name is required")
    private String name;

    @NotBlank(message = "email_id is required")
    @Email(message = "email_id must be a valid email")
    @JsonProperty("email_id")
    private String emailId;

    @NotBlank(message = "contact_number is required")
    @JsonProperty("contact_number")
    private String contactNumber;

    @NotBlank(message = "occupation is required")
    private String occupation;

    @NotBlank(message = "institute is required")
    private String institute;

    @NotNull(message = "reason is required")
    private EventRegistration.RegistrationReason reason;

    @JsonProperty("other_reason")
    private String otherReason;
}

