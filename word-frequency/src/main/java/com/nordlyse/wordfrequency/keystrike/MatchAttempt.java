package com.nordlyse.wordfrequency.keystrike;

import java.util.List;

public record MatchAttempt(List<KeystrikeRequest> keys, String sentence) {

    public MatchAttempt(List<KeystrikeRequest> keys) {
        this(keys, null);
    }
}
