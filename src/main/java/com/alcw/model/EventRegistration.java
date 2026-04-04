package com.alcw.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document(collection = "event_registrations")
public class EventRegistration {

    @Id
    private String id;

    private String eventId;
    private String eventName;

    private String name;
    private String emailId;
    private String contactNumber;
    private String occupation;
    private String institute;
    private RegistrationReason reason;
    private String otherReason;

    private boolean reminderSent;
    private LocalDateTime createdAt = LocalDateTime.now();

    public enum RegistrationReason {
        social_media,
        friends,
        university,
        other
    }
}
