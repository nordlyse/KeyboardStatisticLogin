package com.nordlyse.wordfrequency.keystrike;

public record NgramTransition(
        int orderSize,
        String sequence,
        long seenCount,
        long errorCount,
        double errorFrequency,
        double[] stepMillis) {

    public NgramTransition {
        stepMillis = stepMillis == null ? new double[0] : stepMillis.clone();
    }
}
