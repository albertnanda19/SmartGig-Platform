package com.smartgig.notification.consumer;

import com.smartgig.common.constants.KafkaTopics;
import com.smartgig.notification.dto.NotificationMessage;
import com.smartgig.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class ProjectEventConsumer {
    private final NotificationService notificationService;

    @KafkaListener(
            topics = KafkaTopics.PROJECT_APPLIED,
            groupId = "notification-service-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleProjectApplied(@Payload Object payload, Acknowledgment ack) {
        try {
            Map<String, Object> event = payload instanceof Map ? (Map<String, Object>) payload : Map.of();
            String eventId = value(event, "eventId");
            if (notificationService.isEventProcessed(eventId)) {
                ack.acknowledge();
                return;
            }

            Long projectId = longValue(event, "projectId");
            Long freelancerId = longValue(event, "freelancerId");
            String freelancerUsername = value(event, "freelancerUsername");
            Long applicationId = longValue(event, "applicationId");
            Long clientId = longValue(event, "projectClientId");

            if (clientId != null) {
                notificationService.createAndSendNotification(
                        NotificationMessage.builder()
                                .userId(clientId)
                                .type("PROJECT_APPLIED")
                                .title("New Application Received")
                                .message(freelancerUsername + " applied to your project")
                                .actionUrl("/projects/" + projectId + "/applications")
                                .metadata(Map.of(
                                        "projectId", projectId,
                                        "freelancerId", freelancerId,
                                        "applicationId", applicationId
                                ))
                                .build()
                );
            }

            notificationService.markEventProcessed(eventId);
            ack.acknowledge();
        } catch (Exception e) {
            throw e;
        }
    }

    @KafkaListener(
            topics = KafkaTopics.PROJECT_STATUS_CHANGED,
            groupId = "notification-service-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleProjectStatusChanged(@Payload Object payload, Acknowledgment ack) {
        try {
            Map<String, Object> event = payload instanceof Map ? (Map<String, Object>) payload : Map.of();
            String eventId = value(event, "eventId");
            if (notificationService.isEventProcessed(eventId)) {
                ack.acknowledge();
                return;
            }

            Long projectId = longValue(event, "projectId");
            Long clientId = longValue(event, "clientId");
            Long affectedFreelancerId = longValue(event, "affectedFreelancerId");
            String prev = value(event, "previousStatus");
            String next = value(event, "newStatus");

            if ("IN_REVIEW".equals(prev) && "IN_PROGRESS".equals(next) && affectedFreelancerId != null) {
                notificationService.createAndSendNotification(NotificationMessage.builder()
                        .userId(affectedFreelancerId)
                        .type("APPLICATION_ACCEPTED")
                        .title("You've been selected!")
                        .message("You have been selected for a project")
                        .actionUrl("/projects/" + projectId)
                        .metadata(Map.of("projectId", projectId))
                        .build());
            }

            if ("IN_PROGRESS".equals(prev) && "COMPLETED".equals(next) && affectedFreelancerId != null) {
                notificationService.createAndSendNotification(NotificationMessage.builder()
                        .userId(affectedFreelancerId)
                        .type("PROJECT_COMPLETED")
                        .title("Project completed")
                        .message("A project you worked on has been marked completed")
                        .actionUrl("/projects/" + projectId)
                        .metadata(Map.of("projectId", projectId))
                        .build());
            }

            if (!"CANCELLED".equals(prev) && "CANCELLED".equals(next) && clientId != null) {
                notificationService.createAndSendNotification(NotificationMessage.builder()
                        .userId(clientId)
                        .type("SYSTEM")
                        .title("Project cancelled")
                        .message("Your project has been cancelled")
                        .actionUrl("/projects/" + projectId)
                        .metadata(Map.of("projectId", projectId))
                        .build());
            }

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

