package com.smartgig.notification.service.impl;

import com.smartgig.common.exception.ResourceNotFoundException;
import com.smartgig.notification.document.NotificationDocument;
import com.smartgig.notification.dto.NotificationMessage;
import com.smartgig.notification.dto.response.NotificationResponse;
import com.smartgig.notification.repository.NotificationRepository;
import com.smartgig.notification.service.NotificationService;
import com.smartgig.notification.websocket.WebSocketSessionManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {
    private final NotificationRepository notificationRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final WebSocketSessionManager sessionManager;
    private final MongoTemplate mongoTemplate;
    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public NotificationDocument createAndSendNotification(NotificationMessage message) {
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        NotificationDocument saved = notificationRepository.save(NotificationDocument.builder()
                .userId(message.getUserId())
                .type(message.getType())
                .title(message.getTitle())
                .message(message.getMessage())
                .actionUrl(message.getActionUrl())
                .metadata(message.getMetadata())
                .isRead(false)
                .readAt(null)
                .expiresAt(now.plusDays(30))
                .build());

        if (sessionManager.isUserOnline(saved.getUserId())) {
            messagingTemplate.convertAndSendToUser(
                    saved.getUserId().toString(),
                    "/queue/notifications",
                    toResponse(saved)
            );
        }
        return saved;
    }

    @Override
    public Page<NotificationResponse> getNotificationsForUser(Long userId, Pageable pageable) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable).map(this::toResponse);
    }

    @Override
    public void markAsRead(Long userId, String notificationId) {
        NotificationDocument doc = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));
        if (!Objects.equals(doc.getUserId(), userId)) {
            throw new ResourceNotFoundException("Notification not found");
        }
        if (!doc.isRead()) {
            doc.setRead(true);
            doc.setReadAt(LocalDateTime.now(ZoneOffset.UTC));
            notificationRepository.save(doc);
        }
    }

    @Override
    public void markAllAsRead(Long userId) {
        Query q = Query.query(Criteria.where("userId").is(userId).and("isRead").is(false));
        Update u = new Update().set("isRead", true).set("readAt", LocalDateTime.now(ZoneOffset.UTC));
        mongoTemplate.updateMulti(q, u, NotificationDocument.class);
    }

    @Override
    public long getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    @Override
    public boolean isEventProcessed(String eventId) {
        if (eventId == null || eventId.isBlank()) {
            return false;
        }
        return Boolean.TRUE.equals(stringRedisTemplate.hasKey(processedKey(eventId)));
    }

    @Override
    public void markEventProcessed(String eventId) {
        if (eventId == null || eventId.isBlank()) {
            return;
        }
        stringRedisTemplate.opsForValue().set(processedKey(eventId), "1", Duration.ofHours(24));
    }

    private String processedKey(String eventId) {
        return "notification:processed:" + eventId;
    }

    private NotificationResponse toResponse(NotificationDocument doc) {
        return NotificationResponse.builder()
                .id(doc.getId())
                .userId(doc.getUserId())
                .type(doc.getType())
                .title(doc.getTitle())
                .message(doc.getMessage())
                .actionUrl(doc.getActionUrl())
                .isRead(doc.isRead())
                .metadata(doc.getMetadata())
                .createdAt(doc.getCreatedAt())
                .readAt(doc.getReadAt())
                .build();
    }
}

