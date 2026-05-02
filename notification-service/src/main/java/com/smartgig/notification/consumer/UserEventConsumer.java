package com.smartgig.notification.consumer;

import com.smartgig.common.constants.KafkaTopics;
import com.smartgig.notification.dto.NotificationMessage;
import com.smartgig.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class UserEventConsumer {
    private final NotificationService notificationService;

    @KafkaListener(
            topics = KafkaTopics.USER_REGISTERED,
            groupId = "notification-service-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleUserRegistered(Object payload, Acknowledgment ack) {
        try {
            Map<String, Object> event = payload instanceof Map ? (Map<String, Object>) payload : Map.of();
            String eventId = value(event, "eventId");
            if (notificationService.isEventProcessed(eventId)) {
                ack.acknowledge();
                return;
            }
            Long userId = longValue(event, "userId");
            notificationService.createAndSendNotification(NotificationMessage.builder()
                    .userId(userId)
                    .type("WELCOME")
                    .title("Welcome to SmartGig!")
                    .message("Complete your profile to start getting matched with projects")
                    .actionUrl("/profile/setup")
                    .metadata(Map.of())
                    .build());
            notificationService.markEventProcessed(eventId);
            ack.acknowledge();
        } catch (Exception e) {
            throw e;
        }
    }

    private String value(Map<String, Object> map, String key) {
        Object v = map.get(key);
        return v == null ? null : v.toString();
    }

    private Long longValue(Map<String, Object> map, String key) {
        Object v = map.get(key);
        if (v == null) return null;
        if (v instanceof Number n) return n.longValue();
        return Long.parseLong(v.toString());
    }
}

