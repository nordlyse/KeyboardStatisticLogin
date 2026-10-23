package com.nordlyse.wordfrequency.keystrike;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "keystrike")
public record KeystrikeSettings(int maxOrder, String profile) {

    public KeystrikeSettings {
        if (maxOrder == 0) {
            maxOrder = NgramAccumulator.MAX_ORDER;
        }
        if (profile == null || profile.isBlank()) {
            profile = "local";
        }
        if (maxOrder < 1 || maxOrder > NgramAccumulator.MAX_ORDER) {
            throw new IllegalArgumentException("keystrike.max-order must be from 1 to 10");
        }
    }
}
