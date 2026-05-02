package com.smartgig.analytics.consumer;

import com.smartgig.common.constants.KafkaTopics;
import com.smartgig.analytics.service.EventIngestionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class AllEventsConsumer {
    private final EventIngestionService ingestionService;

    @KafkaListener(
            topics = {
                    KafkaTopics.USER_REGISTERED,
                    KafkaTopics.PROJECT_CREATED,
                    KafkaTopics.PROJECT_APPLIED,
                    KafkaTopics.PROJECT_STATUS_CHANGED,
                    KafkaTopics.SKILL_TRENDING
            },
            groupId = "analytics-service-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeAllEvents(
            @Payload String rawPayload,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(value = KafkaHeaders.RECEIVED_KEY, required = false) String key,
            Acknowledgment ack
    ) {
        try {
            ingestionService.ingestEvent(topic, key, rawPayload);
            ack.acknowledge();
        } catch (Exception e) {
            throw e;
        }
    }
}

