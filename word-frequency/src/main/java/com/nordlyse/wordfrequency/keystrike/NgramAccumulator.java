package com.nordlyse.wordfrequency.keystrike;

final class NgramAccumulator {

    static final int MAX_ORDER = 10;
    static final int DIMENSION = 11;
    static final int ERROR_SLOT = 0;
    static final int SEEN_SLOT = 1;
    static final int STEP_ORIGIN = 2;
    static final int STEP_SLOTS = 9;

    private NgramAccumulator() {
    }

    static StoredNgram apply(StoredNgram current, NgramUpdate update) {
        if (!update.sample()) {
            return addError(current);
        }
        return addSample(current, update);
    }

    private static StoredNgram addSample(StoredNgram current, NgramUpdate update) {
        long seen = current == null ? 1 : current.transition().seenCount() + 1;
        long errors = current == null ? 0 : current.transition().errorCount();
        if (update.erroneous()) {
            errors++;
        }
        double[] steps = update.stepMillis();
        double[] means = new double[steps.length];
        double[] previous = current == null ? new double[0] : current.transition().stepMillis();
        for (int index = 0; index < steps.length; index++) {
            double oldMean = index < previous.length ? previous[index] : 0;
            means[index] = oldMean + (steps[index] - oldMean) / seen;
        }
        return pack(update.orderSize(), update.sequence(), seen, errors, means);
    }

    private static StoredNgram addError(StoredNgram current) {
        if (current == null || current.transition().seenCount() == 0) {
            return null;
        }
        NgramTransition previous = current.transition();
        return pack(
                previous.orderSize(),
                previous.sequence(),
                previous.seenCount(),
                previous.errorCount() + 1,
                previous.stepMillis());
    }

    private static StoredNgram pack(int orderSize, String sequence, long seen, long errors, double[] means) {
        double errorFrequency = seen == 0 ? 0 : (double) errors / seen;
        float[] features = new float[DIMENSION];
        features[ERROR_SLOT] = (float) errorFrequency;
        features[SEEN_SLOT] = (float) (Math.log1p(seen) / 10.0);
        int slots = Math.min(STEP_SLOTS, means.length);
        for (int index = 0; index < slots; index++) {
            features[STEP_ORIGIN + index] = (float) (means[index] / 1000.0);
        }
        NgramTransition transition = new NgramTransition(orderSize, sequence, seen, errors, errorFrequency, means);
        return new StoredNgram(transition, features);
    }
}
