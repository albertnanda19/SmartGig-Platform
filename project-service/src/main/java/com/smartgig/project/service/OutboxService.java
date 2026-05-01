package com.smartgig.project.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartgig.project.entity.OutboxEvent;
import com.smartgig.project.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
@RequiredArgsConstructor
public class OutboxService {
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;
    private final OutboxPublisher publisher;

    @Transactional
    public void enqueue(String eventType, String eventKey, Object payload) {
        String json;
        try {
            json = objectMapper.writeValueAsString(payload);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to serialize outbox payload");
        }

        outboxEventRepository.save(OutboxEvent.builder()
                .eventType(eventType)
                .eventKey(eventKey)
                .payload(json)
                .createdAt(LocalDateTime.now(ZoneOffset.UTC))
                .build());

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                publisher.publishPending();
            }
        });
    }
}

