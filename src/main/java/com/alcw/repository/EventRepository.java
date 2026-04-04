package com.alcw.repository;

import com.alcw.model.Event;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface EventRepository extends MongoRepository<Event, String> {
    Optional<Event> findByEventId(String eventId);
    List<Event> findByStatus(boolean status);
}
