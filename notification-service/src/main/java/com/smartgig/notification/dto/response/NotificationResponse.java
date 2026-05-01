package com.smartgig.notification.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
public class NotificationResponse {
    private String id;
    private Long userId;
    private String type;
    private String title;
    private String message;
    private String actionUrl;
    private boolean isRead;
    private Map<String, Object> metadata;
    private LocalDateTime createdAt;
    private LocalDateTime readAt;
}

