package com.smartgig.analytics.service;

public interface EventIngestionService {
    void ingestEvent(String topic, String key, String rawPayload);
}

