package com.nordlyse.wordfrequency.keystrike;

public final class ActiveProfile {

    private static final ThreadLocal<String> CURRENT = new ThreadLocal<>();

    private ActiveProfile() {
    }

    public static void use(String profile) {
        CURRENT.set(profile);
    }

    public static void clear() {
        CURRENT.remove();
    }

    public static String orElse(String fallback) {
        String current = CURRENT.get();
        if (current == null || current.isBlank()) {
            return fallback;
        }
        return current;
    }
}
