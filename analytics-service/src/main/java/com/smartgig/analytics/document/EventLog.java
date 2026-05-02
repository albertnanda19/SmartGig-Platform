package com.smartgig.analytics.document;

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

@Document(collection = "event_logs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventLog {
    @Id
    private String id;

    @Indexed
    private String eventType;

    @Indexed
    private String eventId;

    private Map<String, Object> payload;

    @Indexed
    @CreatedDate
    private LocalDateTime receivedAt;

    @Indexed(expireAfterSeconds = 7776000)
    private LocalDateTime expiresAt;
}

