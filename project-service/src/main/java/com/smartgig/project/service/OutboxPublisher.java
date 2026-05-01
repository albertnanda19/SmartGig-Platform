package com.smartgig.project.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartgig.common.constants.KafkaTopics;
import com.smartgig.project.entity.OutboxEvent;
import com.smartgig.project.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OutboxPublisher {
    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Scheduled(fixedDelayString = "30000")
    @Transactional
    public void publishPending() {
        List<OutboxEvent> pending = outboxEventRepository.findUnpublished();
        for (OutboxEvent event : pending) {
            try {
                Object payload = objectMapper.readValue(event.getPayload(), Object.class);
                String topic = mapTopic(event.getEventType());
                kafkaTemplate.send(topic, event.getEventKey(), payload);
                event.setPublishedAt(LocalDateTime.now(ZoneOffset.UTC));
                outboxEventRepository.save(event);
            } catch (Exception e) {
                log.error("Outbox publish failed: id={}, type={}", event.getId(), event.getEventType(), e);
            }
        }
    }

    private String mapTopic(String eventType) {
        return switch (eventType) {
            case "project.created" -> KafkaTopics.PROJECT_CREATED;
            case "project.applied" -> KafkaTopics.PROJECT_APPLIED;
            case "project.status.changed" -> KafkaTopics.PROJECT_STATUS_CHANGED;
            default -> eventType;
        };
    }
}

