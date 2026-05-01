package com.smartgig.common.constants;

public final class KafkaTopics {
    public static final String USER_REGISTERED = "user.registered";
    public static final String PROJECT_CREATED = "project.created";
    public static final String PROJECT_APPLIED = "project.applied";
    public static final String PROJECT_STATUS_CHANGED = "project.status.changed";
    public static final String SKILL_TRENDING = "skill.trending";
    public static final String NOTIFICATION_SEND = "notification.send";

    private KafkaTopics() {
        throw new IllegalStateException("Utility class");
    }
}

