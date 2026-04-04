package com.alcw.repository;

import com.alcw.model.EventRegistration;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface EventRegistrationRepository extends MongoRepository<EventRegistration, String> {
    List<EventRegistration> findByEventIdOrderByCreatedAtDesc(String eventId);
    Optional<EventRegistration> findByEventIdAndEmailId(String eventId, String emailId);
    List<EventRegistration> findByEventIdAndReminderSentFalse(String eventId);
}
