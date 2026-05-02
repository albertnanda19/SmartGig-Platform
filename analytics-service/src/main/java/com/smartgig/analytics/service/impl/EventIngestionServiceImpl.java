package com.smartgig.analytics.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartgig.analytics.document.EventLog;
import com.smartgig.analytics.repository.EventLogRepository;
import com.smartgig.analytics.service.EventIngestionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventIngestionServiceImpl implements EventIngestionService {
    private final EventLogRepository eventLogRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void ingestEvent(String topic, String key, String rawPayload) {
        Map<String, Object> payload = parse(rawPayload);
        String eventId = payload.get("eventId") == null ? key : payload.get("eventId").toString();
        if (eventId != null && eventLogRepository.findByEventId(eventId).isPresent()) {
            return;
        }
        eventLogRepository.save(EventLog.builder()
                .eventType(topic)
                .eventId(eventId)
                .payload(payload)
                .expiresAt(LocalDateTime.now(ZoneOffset.UTC).plusDays(90))
                .build());
    }

    private Map<String, Object> parse(String raw) {
        if (raw == null || raw.isBlank()) {
            return Map.of();
        }
        try {
            return objectMapper.readValue(raw, new TypeReference<>() {});
        } catch (Exception e) {
            return Map.of("raw", raw);
        }
    }
}

