package com.smartgig.common.util;

import java.text.Normalizer;
import java.util.Locale;

public final class SlugUtil {
    private SlugUtil() {
        throw new IllegalStateException("Utility class");
    }

    public static String toSlug(String input) {
        if (input == null) {
            return null;
        }
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFKD);
        String lower = normalized.toLowerCase(Locale.ROOT);
        String cleaned = lower.replaceAll("[^a-z0-9\\s-]", "");
        String dashed = cleaned.trim().replaceAll("\\s+", "-");
        return dashed.replaceAll("-{2,}", "-");
    }
}

