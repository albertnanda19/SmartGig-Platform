package com.smartgig.intelligence.util;

public final class ScoringUtil {
    private ScoringUtil() {
        throw new IllegalStateException();
    }

    public static double clamp01(double value) {
        if (value < 0.0) {
            return 0.0;
        }
        if (value > 1.0) {
            return 1.0;
        }
        return value;
    }

    public static double normalize(double value, double min, double max) {
        if (max <= min) {
            return 0.0;
        }
        return clamp01((value - min) / (max - min));
    }
}

