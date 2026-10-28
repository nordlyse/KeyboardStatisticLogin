package com.nordlyse.wordfrequency.keystrike;

import java.util.List;

public record PaceMark(
        String from,
        String to,
        long seenCount,
        double meanMillis,
        double errorFrequency,
        List<WordCrossing> words) {

    public PaceMark {
        words = words == null ? List.of() : List.copyOf(words);
    }
}
