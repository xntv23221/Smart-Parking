package com.smartparking.common.util;

/**
 * Minimal input sanitation utilities to reduce data quality issues.
 *
 * <p>Validation should reject invalid data. Sanitization should keep valid data consistent.</p>
 */
public final class Sanitizer {
    private Sanitizer() {
    }

    public static String trimToNull(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    public static String normalizeUsername(String username) {
        String v = trimToNull(username);
        return v == null ? null : v.toLowerCase();
    }
}

