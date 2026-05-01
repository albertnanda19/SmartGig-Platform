package com.smartgig.common.util;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public final class DateUtil {
    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_DATE_TIME;

    private DateUtil() {
        throw new IllegalStateException("Utility class");
    }

    public static String toIso8601(LocalDateTime value) {
        if (value == null) {
            return null;
        }
        return value.atOffset(ZoneOffset.UTC).format(ISO);
    }

    public static LocalDateTime fromIso8601(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return LocalDateTime.parse(value, ISO);
    }

    public static LocalDateTime nowUtc() {
        return LocalDateTime.now(ZoneOffset.UTC);
    }
}

