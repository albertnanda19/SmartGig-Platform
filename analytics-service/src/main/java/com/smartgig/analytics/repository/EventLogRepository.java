package com.smartgig.analytics.repository;

import com.smartgig.analytics.document.EventLog;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface EventLogRepository extends MongoRepository<EventLog, String> {
    Optional<EventLog> findByEventId(String eventId);
}

