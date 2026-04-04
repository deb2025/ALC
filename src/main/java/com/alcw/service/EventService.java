package com.alcw.service;

import com.alcw.dto.ArchivedEventRequest;
import com.alcw.dto.UpcomingEventRequest;
import com.alcw.model.Event;

import java.util.List;

public interface EventService {
    Event createUpcomingEvent(UpcomingEventRequest request);
    Event updateUpcomingEvent(String eventId, UpcomingEventRequest request);
    Event archiveEventStatus(String eventId);
    Event updateArchivedEvent(String eventId, ArchivedEventRequest request);
    Event deleteEvent(String eventId);
    Event getEventByEventId(String eventId);
    List<Event> getEventsByStatus(String status);
    List<Event> searchEvents(String eventId, String status);
}
