package com.smartgig.notification.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Map;

@Document(collection = "notifications")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDocument {
    @Id
    private String id;

    @Indexed
    private Long userId;

    private String type;
    private String title;
    private String message;
    private String actionUrl;

    @Indexed
    private boolean isRead;

    private Map<String, Object> metadata;

    @CreatedDate
    private LocalDateTime createdAt;

    private LocalDateTime readAt;

    @Indexed(expireAfterSeconds = 2592000)
    private LocalDateTime expiresAt;
}

