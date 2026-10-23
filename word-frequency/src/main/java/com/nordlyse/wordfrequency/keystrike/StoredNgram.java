package com.nordlyse.wordfrequency.keystrike;

record StoredNgram(NgramTransition transition, float[] features) {

    StoredNgram {
        features = features == null ? new float[NgramAccumulator.DIMENSION] : features.clone();
    }
}
